package com.idss.task5.benchmark;

import com.idss.common.model.Exam;
import com.idss.common.model.Student;
import com.idss.common.model.Timeslot;
import com.idss.task5.algorithm.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

/**
 * Automated benchmark runner for Chapter 8 of Individual Report.
 *
 * Generates synthetic datasets of increasing size (10, 30, 50, 100 exams),
 * runs all 3 algorithms 30 times each, and outputs results as JSON.
 *
 * Metrics collected:
 * - execution_time_ms (average over 30 runs)
 * - total_fatigue_penalty (average)
 * - hard_constraint_violations (always 0 if valid)
 * - soft_constraint_satisfaction_percentage
 *
 * Usage: Run via POST /api/task5/benchmark or call main() directly.
 */
public class BenchmarkRunner {

        private static final int RUNS_PER_SIZE = 5;
    private static final int[] EXAM_SIZES = {10, 30, 50};

    /**
     * Run the full benchmark suite.
     * Returns a list of result maps suitable for JSON export.
     */
    public static List<Map<String, Object>> runFullBenchmark() {
        List<Map<String, Object>> allResults = new ArrayList<>();

        for (int numExams : EXAM_SIZES) {
            System.out.println("=== Benchmarking E=" + numExams + " ===");

            // Generate synthetic data
            List<Exam> exams = generateExams(numExams);
            List<Student> students = generateStudents(numExams);
            List<Timeslot> timeslots = generateTimeslots(numExams);
            List<String> courseCodes = new ArrayList<>();
            for (Exam e : exams) courseCodes.add(e.course_code);

            // Build conflict matrix and validator
            ConflictMatrix matrix = new ConflictMatrix(students, courseCodes);
            ConstraintValidator validator = new ConstraintValidator(matrix, timeslots, exams);
            int numSlots = timeslots.size();

            // Benchmark each algorithm
            allResults.add(benchmarkAlgorithm("Greedy Largest Degree First", numExams, matrix, validator, numSlots, "GREEDY"));
            allResults.add(benchmarkAlgorithm("Simulated Annealing", numExams, matrix, validator, numSlots, "SA"));
            allResults.add(benchmarkAlgorithm("Genetic Algorithm (Hybrid + Hill Climbing)", numExams, matrix, validator, numSlots, "GA"));

            System.out.println();
        }

        return allResults;
    }

    /**
     * Benchmark one algorithm across 30 runs for a given exam size.
     */
    private static Map<String, Object> benchmarkAlgorithm(
            String algoName, int numExams, ConflictMatrix matrix,
            ConstraintValidator validator, int numSlots, String algoType) {

        double totalTime = 0;
        double totalFatigue = 0;
        double totalSatisfaction = 0;
        int totalViolations = 0;
        int clashFreeCount = 0;

        for (int run = 0; run < RUNS_PER_SIZE; run++) {
            AlgorithmResult result;

            switch (algoType) {
                case "GREEDY":
                    result = new GreedyScheduler(matrix, validator, numSlots).run();
                    break;
                case "SA":
                    result = new SimulatedAnnealing(matrix, validator, numSlots).run();
                    break;
                default:
                    result = new GeneticEngine(matrix, validator, numSlots).run();
                    break;
            }

            totalTime += result.executionTimeMs;
            totalFatigue += result.totalFatiguePenalty;
            totalViolations += result.hardViolations;
            totalSatisfaction += validator.calculateSatisfactionPercentage(result.bestChromosome);
            if (result.isClashFree()) clashFreeCount++;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("exam_count", numExams);
        row.put("algorithm", algoName);
        row.put("runs", RUNS_PER_SIZE);
        row.put("avg_execution_time_ms", Math.round(totalTime / RUNS_PER_SIZE * 100.0) / 100.0);
        row.put("avg_fatigue_penalty", Math.round(totalFatigue / RUNS_PER_SIZE * 100.0) / 100.0);
        row.put("avg_satisfaction_pct", Math.round(totalSatisfaction / RUNS_PER_SIZE * 100.0) / 100.0);
        row.put("clash_free_runs", clashFreeCount + "/" + RUNS_PER_SIZE);
        row.put("total_hard_violations", totalViolations);

        System.out.printf("  %-50s | E=%-3d | %.2fms | fatigue=%.1f | satisfaction=%.1f%% | clash-free=%d/%d%n",
                algoName, numExams,
                totalTime / RUNS_PER_SIZE,
                totalFatigue / RUNS_PER_SIZE,
                totalSatisfaction / RUNS_PER_SIZE,
                clashFreeCount, RUNS_PER_SIZE);

        return row;
    }

    // ─────────── Synthetic Data Generators ───────────

    /**
     * Generate N exams with realistic course codes.
     */
    private static List<Exam> generateExams(int n) {
        String[] prefixes = {"CS", "MA", "PH", "SE", "DB", "NET", "AI", "OS", "WEB", "ISM",
                             "DMS", "OOP", "DSA", "IOT", "ML", "HCI", "QA", "DEV", "CLD", "SEC"};
        List<Exam> exams = new ArrayList<>();
        Random rng = new Random(42);

        for (int i = 0; i < n; i++) {
            Exam exam = new Exam();
            exam.exam_id = "EX_" + String.format("%03d", i + 1);
            exam.course_code = prefixes[i % prefixes.length] + (100 + (i / prefixes.length) * 100 + i % 10);
            exam.course_title = "Course " + (i + 1);
            exam.duration_hours = 2 + rng.nextInt(2); // 2 or 3 hours
            exam.student_count = 20 + rng.nextInt(40); // 20-60 students
            exam.year = 1 + rng.nextInt(4);
            exam.department = "School of Computing";
            exam.requires_accessibility = rng.nextDouble() < 0.1; // 10% chance
            exams.add(exam);
        }
        return exams;
    }

    /**
     * Generate students with random enrollments (2-4 courses each).
     * Creates realistic clash density.
     */
    private static List<Student> generateStudents(int numExams) {
        Random rng = new Random(42);
        int numStudents = numExams * 5; // ~5 students per exam
        List<String> allCodes = new ArrayList<>();
        for (int i = 0; i < numExams; i++) {
            String[] prefixes = {"CS", "MA", "PH", "SE", "DB", "NET", "AI", "OS", "WEB", "ISM",
                                 "DMS", "OOP", "DSA", "IOT", "ML", "HCI", "QA", "DEV", "CLD", "SEC"};
            allCodes.add(prefixes[i % prefixes.length] + (100 + (i / prefixes.length) * 100 + i % 10));
        }

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < numStudents; i++) {
            Student s = new Student();
            s.student_id = "STU_" + String.format("%04d", i + 1);
            s.name = "Student " + (i + 1);
            s.year = 1 + rng.nextInt(4);

            // Each student takes 2-4 random courses
            int courseCount = 2 + rng.nextInt(3);
            Set<String> enrolled = new HashSet<>();
            while (enrolled.size() < courseCount && enrolled.size() < allCodes.size()) {
                enrolled.add(allCodes.get(rng.nextInt(allCodes.size())));
            }
            s.enrolled_courses = new ArrayList<>(enrolled);
            students.add(s);
        }
        return students;
    }

    /**
     * Generate enough timeslots for the exam count.
     * Rule of thumb: ~2 slots per 3 exams (morning + afternoon for several days).
     */
    private static List<Timeslot> generateTimeslots(int numExams) {
        List<Timeslot> slots = new ArrayList<>();
        int numDays = Math.max(3, (numExams / 3) + 1); // enough days
        int slotId = 1;

        for (int day = 0; day < numDays; day++) {
            String date = String.format("2026-09-%02d", day + 1);

            // Morning slot
            Timeslot morning = new Timeslot();
            morning.slot_id = "SLOT_" + String.format("%02d", slotId++);
            morning.date = date;
            morning.session = "Morning";
            morning.start_time = "09:00";
            morning.end_time = "12:00";
            morning.max_exams_parallel = 5;
            slots.add(morning);

            // Afternoon slot
            Timeslot afternoon = new Timeslot();
            afternoon.slot_id = "SLOT_" + String.format("%02d", slotId++);
            afternoon.date = date;
            afternoon.session = "Afternoon";
            afternoon.start_time = "13:30";
            afternoon.end_time = "16:30";
            afternoon.max_exams_parallel = 5;
            slots.add(afternoon);
        }
        return slots;
    }

    /**
     * Export results to JSON file.
     */
    public static void exportResults(List<Map<String, Object>> results, String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        new File(filePath).getParentFile().mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), results);
        System.out.println("Benchmark results exported to: " + filePath);
    }
}