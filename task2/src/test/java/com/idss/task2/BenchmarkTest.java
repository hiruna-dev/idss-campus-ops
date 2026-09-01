package com.idss.task2;

import com.idss.common.model.Invigilator;
import com.idss.common.util.JsonLoader;
import com.idss.task2.algorithm.Assignment;
import com.idss.task2.algorithm.ConstraintValidator;
import com.idss.task2.algorithm.CostMatrixBuilder;
import com.idss.task2.algorithm.CostMatrixBuilder.CostMatrix;
import com.idss.task2.algorithm.FairnessCalculator;
import com.idss.task2.algorithm.Hungarian;
import com.idss.task2.model.MasterScheduleEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//pure algo benchmark
public class BenchmarkTest {

    private static final String INPUT_DIR = "../data/input/";
    private static final String OUTPUT_PATH = "../data/shared/benchmark_task2.json";
    private static final int MAX_REFINEMENT_ITERS = 5;

    private final CostMatrixBuilder costMatrixBuilder = new CostMatrixBuilder();
    private final Hungarian hungarian = new Hungarian();
    private final ConstraintValidator constraintValidator = new ConstraintValidator();
    private final FairnessCalculator fairnessCalculator = new FairnessCalculator();

    //one row in the benchmark output
    public static final class BenchmarkRow {
        public String size;
        public int exams;
        public int invigilators;
        public long execution_time_ms;
        public long memory_allocated_kb;
        public int hard_constraint_violations;
        public double fairness_variance;
        public String status;
    }

    //top level benchmark output doc
    public static final class BenchmarkReport {
        public String generation_timestamp;
        public String algorithm_used;
        public List<BenchmarkRow> results;
    }

    @Test
    void benchmarkAcrossDatasetSizes() throws Exception {
        String[][] sizes = {
                {"small",  "benchmark_schedule_small.json",  "benchmark_invigilators_small.json"},
                {"medium", "benchmark_schedule_medium.json", "benchmark_invigilators_medium.json"},
                {"large",  "benchmark_schedule_large.json",  "benchmark_invigilators_large.json"},
                {"xl",     "benchmark_schedule_xl.json",     "benchmark_invigilators_xl.json"},
        };

        List<BenchmarkRow> rows = new ArrayList<>();
        for (String[] entry : sizes) {
            String label = entry[0];
            List<MasterScheduleEntry> schedule =
                    JsonLoader.loadList(INPUT_DIR + entry[1], MasterScheduleEntry.class);
            List<Invigilator> invigilators =
                    JsonLoader.loadList(INPUT_DIR + entry[2], Invigilator.class);

            BenchmarkRow row = runOne(label, schedule, invigilators);
            rows.add(row);

            //verification checklist: 0 violations + OPTIMAL for all sizes
            assertEquals(0, row.hard_constraint_violations,
                    "size=" + label + " should have 0 hard violations");
            assertEquals("OPTIMAL", row.status,
                    "size=" + label + " should be OPTIMAL");
            assertTrue(row.fairness_variance >= 0,
                    "fairness_variance must be non-negative");
        }

        BenchmarkReport report = new BenchmarkReport();
        report.generation_timestamp = Instant.now().toString();
        report.algorithm_used = "Hungarian (Kuhn-Munkres)";
        report.results = rows;
        JsonLoader.write(OUTPUT_PATH, report);

        //print for manual inspection of O(n^3) growth
        System.out.println("=== Task 2 Benchmark ===");
        for (BenchmarkRow r : rows) {
            System.out.printf("%-6s exams=%-3d invigilators=%-3d time=%-4dms mem=%-5dKB violations=%d fairness=%.4f status=%s%n",
                    r.size, r.exams, r.invigilators, r.execution_time_ms,
                    r.memory_allocated_kb, r.hard_constraint_violations,
                    r.fairness_variance, r.status);
        }
    }

    private BenchmarkRow runOne(String label, List<MasterScheduleEntry> schedule,
                                List<Invigilator> invigilators) {
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

        System.gc(); //stabilize baseline so the delta reflects algo memory
        long memBefore = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        long start = System.nanoTime();
        for (int iter = 0; iter < MAX_REFINEMENT_ITERS; iter++) {
            CostMatrix matrix = costMatrixBuilder.build(
                    schedule, invigilators, priorTotalShifts, priorShiftsByDay);
            int[] assignment = hungarian.solve(matrix.cost);
            List<Assignment> newAssignments = decode(assignment, matrix,
                    invigilatorById, examById);

            if (assignmentsEqual(rawAssignments, newAssignments)) {
                rawAssignments = newAssignments;
                break; //converged
            }
            rawAssignments = newAssignments;

            priorTotalShifts.clear();
            priorShiftsByDay.clear();
            for (Assignment a : rawAssignments) {
                priorTotalShifts.merge(a.inv.invigilator_id, 1, Integer::sum);
                priorShiftsByDay.merge(a.inv.invigilator_id + "|" + a.exam.date, 1, Integer::sum);
            }
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        //greedy fill-up: same as AssignmentService, fills under-staffed exams after hungarian + dedup
        fillUnderStaffedExams(rawAssignments, invigilators, examById);

        long memAfter = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        ConstraintValidator.ConstraintResult constraintResult =
                constraintValidator.validate(rawAssignments);
        double fairnessVariance = fairnessCalculator
                .calculate(rawAssignments, invigilators).fairnessVariance;

        String status = constraintResult.violations > 0 ? "INFEASIBLE" : "OPTIMAL";

        BenchmarkRow row = new BenchmarkRow();
        row.size = label;
        row.exams = schedule.size();
        row.invigilators = invigilators.size();
        row.execution_time_ms = elapsedMs;
        row.memory_allocated_kb = Math.max(0, (memAfter - memBefore) / 1024);
        row.hard_constraint_violations = constraintResult.violations;
        row.fairness_variance = fairnessVariance;
        row.status = status;
        return row;
    }

    //greedy fill-up for under-staffed exams after hungarian + dedup
    private void fillUnderStaffedExams(List<Assignment> assignments,
                                       List<Invigilator> invigilators,
                                       Map<String, MasterScheduleEntry> examById) {
        Map<String, Integer> assignedPerExam = new HashMap<>();
        Map<String, Integer> shiftsPerInv = new HashMap<>();
        Set<String> assignedPairs = new HashSet<>();
        for (Assignment a : assignments) {
            assignedPerExam.merge(a.exam.exam_id, 1, Integer::sum);
            shiftsPerInv.merge(a.inv.invigilator_id, 1, Integer::sum);
            assignedPairs.add(a.inv.invigilator_id + "|" + a.exam.exam_id);
        }

        for (MasterScheduleEntry exam : examById.values()) {
            int needed = exam.required_invigilators - assignedPerExam.getOrDefault(exam.exam_id, 0);
            if (needed <= 0) continue;

            List<Invigilator> candidates = new ArrayList<>();
            for (Invigilator inv : invigilators) {
                if (assignedPairs.contains(inv.invigilator_id + "|" + exam.exam_id)) continue;
                if (shiftsPerInv.getOrDefault(inv.invigilator_id, 0) >= inv.max_total_shifts) continue;
                if (constraintValidator.hasHardViolation(inv, exam)) continue;
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

    //mirrors AssignmentService.decodeAssignment - kept local so the benchmark doesnt couple to the spring service
    private List<Assignment> decode(int[] assignment, CostMatrix matrix,
                                    Map<String, Invigilator> invigilatorById,
                                    Map<String, MasterScheduleEntry> examById) {
        List<Assignment> result = new ArrayList<>();
        Set<String> seen = new HashSet<>(); //dedup (invigilator, exam) pairs from row replication
        for (int i = 0; i < assignment.length; i++) {
            String invId = matrix.rowInvigilatorIds.get(i);
            int col = assignment[i];
            if (col < 0 || invId == null) {
                continue;
            }
            String examId = matrix.columnExamIds.get(col);
            if (examId == null) {
                continue;
            }
            String pairKey = invId + "|" + examId;
            if (!seen.add(pairKey)) continue; //skip duplicate pair
            Invigilator inv = invigilatorById.get(invId);
            MasterScheduleEntry exam = examById.get(examId);
            if (inv == null || exam == null) {
                continue;
            }
            result.add(new Assignment(inv, exam));
        }
        return result;
    }
}
