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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);
    private static final int MAX_REFINEMENT_ITERS = 5;

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
        //edge case: empty schedule returns a clean feasible roster without running hungarian
        if (schedule == null || schedule.isEmpty()) {
            return buildEmptyRoster(Canonical.STATUS_FEASIBLE, null, null);
        }

        validateInputs(schedule, invigilators);

        //edge case: total required shifts exceeds total invigilator capacity
        int totalRequired = 0;
        for (MasterScheduleEntry e : schedule) totalRequired += e.required_invigilators;
        int totalCapacity = 0;
        for (Invigilator inv : invigilators) totalCapacity += inv.max_total_shifts;
        if (totalRequired > totalCapacity) {
            return buildEmptyRoster(Canonical.STATUS_INFEASIBLE,
                    "Insufficient invigilator capacity: " + totalRequired
                            + " shifts required, " + totalCapacity + " available",
                    "Add more invigilators or increase max_total_shifts");
        }

        //extracting data
        Map<String, Invigilator> invigilatorById = new HashMap<>();
        for (Invigilator inv : invigilators) {
            invigilatorById.put(inv.invigilator_id, inv);
        }
        Map<String, MasterScheduleEntry> examById = new LinkedHashMap<>();
        for (MasterScheduleEntry e : schedule) {
            examById.putIfAbsent(e.exam_id, e);
        }

        //iterative refinement: rebuild cost matrix with prior shift counts so +10/+1 penalties accumulate
        Map<String, Integer> priorTotalShifts = new HashMap<>();
        Map<String, Integer> priorShiftsByDay = new HashMap<>();
        List<Assignment> rawAssignments = new ArrayList<>();

        long start = System.nanoTime(); //benchmark the full refinement loop
        for (int iter = 0; iter < MAX_REFINEMENT_ITERS; iter++) {
            CostMatrix costMatrix = costMatrixBuilder.build(
                    schedule, invigilators, priorTotalShifts, priorShiftsByDay);
            int[] assignment = hungarian.solve(costMatrix.cost);
            List<Assignment> newAssignments = decodeAssignment(assignment, costMatrix,
                    invigilatorById, examById);

            //stop if the assignment stopped changing
            if (assignmentsEqual(rawAssignments, newAssignments)) {
                rawAssignments = newAssignments;
                break; //converged
            }
            rawAssignments = newAssignments;

            //update prior shifts from current assignment for next iteration
            priorTotalShifts.clear();
            priorShiftsByDay.clear();
            for (Assignment a : rawAssignments) {
                priorTotalShifts.merge(a.inv.invigilator_id, 1, Integer::sum);
                priorShiftsByDay.merge(a.inv.invigilator_id + "|" + a.exam.date, 1, Integer::sum);
            }
        }
        long elapsed = System.nanoTime() - start;
        long executionTimeMs = elapsed / 1_000_000;

        //greedy fill-up: row replication can assign the same invigilator to multiple slots of the same exam,
        //which dedup removes leaving exams under-staffed. fill those gaps with available invigilators.
        fillUnderStaffedExams(rawAssignments, invigilators, examById, invigilatorById);

        //remove assignments that violate hard constraints so they dont appear in the roster
        rawAssignments.removeIf(a -> constraintValidator.hasHardViolation(a.inv, a.exam));

        ConstraintValidator.ConstraintResult constraintResult =
                constraintValidator.validate(rawAssignments);
        int violations = constraintResult.violations;
        double fairnessVariance = fairnessCalculator
                .calculate(rawAssignments, invigilators).fairnessVariance;

        //check if any exam has fewer invigilators than required after filtering
        Map<String, Integer> assignedCount = new HashMap<>();
        for (Assignment a : rawAssignments) {
            assignedCount.merge(a.exam.exam_id, 1, Integer::sum);
        }
        List<String> underStaffed = new ArrayList<>();
        for (MasterScheduleEntry exam : examById.values()) {
            if (assignedCount.getOrDefault(exam.exam_id, 0) < exam.required_invigilators) {
                underStaffed.add(exam.exam_id);
            }
        }

        String status;
        String reason = null;
        String remedy = null;
        if (!underStaffed.isEmpty()) {
            status = Canonical.STATUS_INFEASIBLE;
            reason = "Exams could not be fully staffed: " + String.join(", ", underStaffed);
            remedy = "Add invigilators or remove course restrictions for these exams";
        } else if (violations > 0) {
            status = Canonical.STATUS_INFEASIBLE;
            reason = constraintResult.violationDescriptions.get(0);
        } else {
            status = Canonical.STATUS_OPTIMAL;
        }

        ProctorRoster roster = buildRoster(rawAssignments, examById, status, reason, remedy);
        // Non-fatal: assignment computation must not be blocked by database
        // availability (master_context_file.md Section 2.4), matching task1/task3/task5.
        try {
            rosterRepository.save(roster);
        } catch (Exception e) {
            log.warn("Failed to persist proctor roster to MongoDB (result still returned to caller): {}", e.getMessage());
        }

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

    //greedy fill-up for under-staffed exams after hungarian + dedup
    private void fillUnderStaffedExams(List<Assignment> assignments,
                                       List<Invigilator> invigilators,
                                       Map<String, MasterScheduleEntry> examById,
                                       Map<String, Invigilator> invigilatorById) {
        //count current assignments per exam and per invigilator
        Map<String, Integer> assignedPerExam = new HashMap<>();
        Map<String, Integer> shiftsPerInv = new HashMap<>();
        Map<String, Integer> shiftsPerDay = new HashMap<>();
        Set<String> assignedPairs = new HashSet<>();
        for (Assignment a : assignments) {
            assignedPerExam.merge(a.exam.exam_id, 1, Integer::sum);
            shiftsPerInv.merge(a.inv.invigilator_id, 1, Integer::sum);
            shiftsPerDay.merge(a.inv.invigilator_id + "|" + a.exam.date, 1, Integer::sum);
            assignedPairs.add(a.inv.invigilator_id + "|" + a.exam.exam_id);
        }

        for (MasterScheduleEntry exam : examById.values()) {
            int needed = exam.required_invigilators - assignedPerExam.getOrDefault(exam.exam_id, 0);
            if (needed <= 0) continue;

            //find invigilators who can take this exam, sorted by fewest current shifts (workload balancing)
            List<Invigilator> candidates = new ArrayList<>();
            for (Invigilator inv : invigilators) {
                if (assignedPairs.contains(inv.invigilator_id + "|" + exam.exam_id)) continue;
                if (shiftsPerInv.getOrDefault(inv.invigilator_id, 0) >= inv.max_total_shifts) continue;
                if (constraintValidator.hasHardViolation(inv, exam)) continue;
                if (shiftsPerDay.getOrDefault(inv.invigilator_id + "|" + exam.date, 0) >= inv.max_shifts_per_day) continue;
                candidates.add(inv);
            }
            candidates.sort(java.util.Comparator.comparingInt(
                    i -> shiftsPerInv.getOrDefault(i.invigilator_id, 0)));

            for (int i = 0; i < needed && i < candidates.size(); i++) {
                Invigilator inv = candidates.get(i);
                assignments.add(new Assignment(inv, exam));
                assignedPerExam.merge(exam.exam_id, 1, Integer::sum);
                shiftsPerInv.merge(inv.invigilator_id, 1, Integer::sum);
                assignedPairs.add(inv.invigilator_id + "|" + exam.exam_id);
            }
        }
    }

    //checks if two assignment lists contain the same (invigilator, exam) pairs
    private boolean assignmentsEqual(List<Assignment> a, List<Assignment> b) {
        if (a.size() != b.size()) return false;
        Set<String> aKeys = new HashSet<>();
        for (Assignment x : a) aKeys.add(x.inv.invigilator_id + "|" + x.exam.exam_id);
        for (Assignment x : b) {
            if (!aKeys.contains(x.inv.invigilator_id + "|" + x.exam.exam_id)) return false;
        }
        return true;
    }

    private List<Assignment> decodeAssignment(int[] assignment, CostMatrix costMatrix,
                                              Map<String, Invigilator> invigilatorById,
                                              Map<String, MasterScheduleEntry> examById) {
        List<Assignment> result = new ArrayList<>();
        Set<String> seen = new HashSet<>(); //dedup (invigilator, exam) pairs from row replication
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
            String pairKey = invId + "|" + examId;
            if (!seen.add(pairKey)) continue; //skip duplicate (invigilator, exam) pair
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
                                      String status, String reason, String remedy) {
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
        roster.reason = reason;
        roster.suggested_remedy = remedy;
        return roster;
    }

    //builds a roster for early-return cases (empty schedule or insufficient capacity)
    private ProctorRoster buildEmptyRoster(String status, String reason, String remedy) {
        ProctorRoster roster = new ProctorRoster();
        roster.generation_timestamp = Instant.now().toString();
        roster.status = status;
        roster.total_shifts_allocated = 0;
        roster.roster = new ArrayList<>();
        roster.reason = reason;
        roster.suggested_remedy = remedy;

        //set metrics so /benchmark still returns something
        lastMetrics = buildMetrics(0, 0, 0.0, 0, status);
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
