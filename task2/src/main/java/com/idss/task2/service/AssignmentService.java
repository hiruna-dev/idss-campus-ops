package com.idss.task2.service;

import com.idss.common.config.Canonical;
import com.idss.common.model.Invigilator;
import com.idss.task2.algorithm.Assignment;
import com.idss.task2.algorithm.ConstraintValidator;
import com.idss.task2.algorithm.CostMatrixBuilder;
import com.idss.task2.algorithm.CostMatrixBuilder.CostMatrix;
import com.idss.task2.algorithm.FairnessCalculator;
import com.idss.task2.algorithm.Hungarian;
import com.idss.task2.model.AssignedInvigilator;
import com.idss.task2.model.MasterScheduleEntry;
import com.idss.task2.model.ProctorRoster;
import com.idss.task2.model.RosterEntry;
import com.idss.task2.model.RosterMetrics;
import com.idss.task2.repository.ProctorRosterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssignmentService {

    private final CostMatrixBuilder costMatrixBuilder = new CostMatrixBuilder();
    private final Hungarian hungarian = new Hungarian();
    private final ConstraintValidator constraintValidator = new ConstraintValidator();
    private final FairnessCalculator fairnessCalculator = new FairnessCalculator();
    private final ProctorRosterRepository rosterRepository;

    private volatile RosterMetrics lastMetrics;

    public AssignmentService(ProctorRosterRepository rosterRepository) {
        this.rosterRepository = rosterRepository;
    }

    public ProctorRoster assignInvigilators(List<MasterScheduleEntry> schedule,
                                            List<Invigilator> invigilators) {
        validateInputs(schedule, invigilators);

        //extracting data
        Map<String, Invigilator> invigilatorById = new HashMap<>();
        for (Invigilator inv : invigilators) {
            invigilatorById.put(inv.invigilator_id, inv);
        }
        Map<String, MasterScheduleEntry> examById = new LinkedHashMap<>();
        for (MasterScheduleEntry e : schedule) {
            examById.putIfAbsent(e.exam_id, e);
        }

        CostMatrix costMatrix = costMatrixBuilder.build(schedule, invigilators);

        long start = System.nanoTime(); //marking the algorithm start time
        int[] assignment = hungarian.solve(costMatrix.cost);
        long elapsed = System.nanoTime() - start; //marking endtime
        long executionTimeMs = elapsed / 1_000_000; //calculating exec time

        List<Assignment> rawAssignments = decodeAssignment(assignment, costMatrix,
                invigilatorById, examById);

        ConstraintValidator.ConstraintResult constraintResult =
                constraintValidator.validate(rawAssignments);
        int violations = constraintResult.violations;
        double fairnessVariance = fairnessCalculator
                .calculate(rawAssignments, invigilators).fairnessVariance;

        String status = violations > 0
                ? Canonical.STATUS_INFEASIBLE
                : Canonical.STATUS_OPTIMAL;

        ProctorRoster roster = buildRoster(rawAssignments, examById, status);
        rosterRepository.save(roster);

        RosterMetrics metrics = buildMetrics(executionTimeMs, violations,
                fairnessVariance, roster.total_shifts_allocated, status);
        lastMetrics = metrics;

        return roster;
    }

    public RosterMetrics getLastMetrics() {
        return lastMetrics;
    }

    public RosterEntry getRosterEntry(String examId) {
        ProctorRoster latest = findLatestRoster();
        if (latest == null || latest.roster == null) {
            return null;
        }
        for (RosterEntry entry : latest.roster) {
            if (examId.equals(entry.exam_id)) {
                return entry;
            }
        }
        return null;
    }

    private ProctorRoster findLatestRoster() {
        List<ProctorRoster> all = rosterRepository.findAll();
        ProctorRoster latest = null;
        for (ProctorRoster r : all) {
            if (latest == null
                    || (r.generation_timestamp != null
                        && r.generation_timestamp.compareTo(latest.generation_timestamp) > 0)) {
                latest = r;
            }
        }
        return latest;
    }

    //method for error handling
    private void validateInputs(List<MasterScheduleEntry> schedule,
                                List<Invigilator> invigilators) {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("Schedule must not be empty");
        }
        if (invigilators == null || invigilators.isEmpty()) {
            throw new IllegalArgumentException("Invigilators list must not be empty");
        }
        for (MasterScheduleEntry e : schedule) {
            if (e.required_invigilators < 1) {
                throw new IllegalArgumentException(
                        "Exam " + e.exam_id + " has required_invigilators < 1");
            }
        }
    }

    private List<Assignment> decodeAssignment(int[] assignment, CostMatrix costMatrix,
                                              Map<String, Invigilator> invigilatorById,
                                              Map<String, MasterScheduleEntry> examById) {
        List<Assignment> result = new ArrayList<>();
        for (int i = 0; i < assignment.length; i++) { //iterate row by row
            String invId = costMatrix.rowInvigilatorIds.get(i);
            int col = assignment[i]; //get the col assigned for that row
            if (col < 0 || invId == null) {
                continue;
            }
            String examId = costMatrix.columnExamIds.get(col);
            if (examId == null) {
                continue;
            }
            Invigilator inv = invigilatorById.get(invId); //get invigi info
            MasterScheduleEntry exam = examById.get(examId); //get exam info
            if (inv == null || exam == null) {
                continue;
            }
            result.add(new Assignment(inv, exam)); //add new assignment to result array
        }
        return result;
    }

    private ProctorRoster buildRoster(List<Assignment> assignments,
                                      Map<String, MasterScheduleEntry> examById,
                                      String status) {
        Map<String, List<AssignedInvigilator>> invigilatorsByExam = new LinkedHashMap<>();
        for (MasterScheduleEntry exam : examById.values()) {
            invigilatorsByExam.put(exam.exam_id, new ArrayList<>());
        }
        for (Assignment a : assignments) {
            AssignedInvigilator ai = new AssignedInvigilator();
            ai.invigilator_id = a.inv.invigilator_id;
            ai.name = a.inv.name;
            invigilatorsByExam.get(a.exam.exam_id).add(ai);
        }

        List<RosterEntry> rosterEntries = new ArrayList<>();
        int allocCounter = 1;
        int totalShifts = 0;
        for (MasterScheduleEntry exam : examById.values()) {
            List<AssignedInvigilator> assigned = invigilatorsByExam.get(exam.exam_id);
            if (!assigned.isEmpty()) {
                assigned.get(0).is_lead_invigilator = true;
            }

            RosterEntry entry = new RosterEntry();
            entry.allocation_id = String.format("ALOC_%03d", allocCounter++);
            entry.exam_id = exam.exam_id;
            entry.course_code = exam.course_code;
            entry.date = exam.date;
            entry.session = exam.session;
            entry.room_id = exam.room_id;
            entry.assigned_invigilators = assigned;
            rosterEntries.add(entry);
            totalShifts += assigned.size();
        }

        ProctorRoster roster = new ProctorRoster();
        roster.generation_timestamp = Instant.now().toString();
        roster.status = status;
        roster.total_shifts_allocated = totalShifts;
        roster.roster = rosterEntries;
        return roster;
    }

    private RosterMetrics buildMetrics(long executionTimeMs, int violations,
                                       double fairnessVariance, int totalShifts,
                                       String status) {
        long memoryKb = (Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()) / 1024;

        RosterMetrics metrics = new RosterMetrics();
        metrics.generation_timestamp = Instant.now().toString();
        metrics.algorithm_used = "Hungarian (Kuhn-Munkres)";
        metrics.execution_time_ms = executionTimeMs;
        metrics.memory_allocated_kb = memoryKb;
        metrics.status = status;
        metrics.hard_constraint_violations = violations;
        metrics.fairness_variance = fairnessVariance;
        metrics.total_shifts_allocated = totalShifts;
        return metrics;
    }
}
