package com.idss.task3.benchmark;

import com.idss.task3.coloring.BacktrackingColorer;
import com.idss.task3.coloring.DsaturColorer;
import com.idss.task3.coloring.GraphColorer;
import com.idss.task3.coloring.WelshPowellColorer;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.service.Task3Service;

import java.util.*;

/**
 * Phase 4 Benchmarking Harness for Task 3 Graph Coloring Algorithms.
 * 
 * Generates deterministic synthetic graphs for V = 10, 30, 50, 100, 250, 500.
 * Restricts BacktrackingColorer to V <= 15 due to O(K^E) complexity.
 */
public class BenchmarkingHarness {

    private static final int SEED = 42;
    private static final Task3Service task3Service = new Task3Service();

    public static void main(String[] args) {
        int[] graphSizes = {10, 30, 50, 100, 250, 500};
        System.out.println("==========================================================================================================================");
        System.out.println("                                        PHASE 4 GRAPH COLORING BENCHMARK REPORT                                            ");
        System.out.println("==========================================================================================================================");
        System.out.printf("%-6s | %-15s | %-8s | %-12s | %-12s | %-10s | %-12s | %-10s | %-10s\n",
                "V", "Algorithm", "Edges", "Build (ms)", "Color (ms)", "Memory(KB)", "Sessions", "Bounds(L/U)", "Status");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");

        for (int v : graphSizes) {
            runBenchmarkForSize(v);
        }
        System.out.println("==========================================================================================================================");
    }

    public static void runBenchmarkForSize(int numVertices) {
        // Deterministic generation
        Random random = new Random(SEED + numVertices);

        long startBuild = System.nanoTime();
        List<StudentEnrollment> enrollments = generateSyntheticEnrollments(numVertices, random);
        ConflictGraphBuilder.Builder builder = new ConflictGraphBuilder.Builder();
        ConflictGraph graph = builder.build(enrollments);
        long endBuild = System.nanoTime();
        double buildTimeMs = (endBuild - startBuild) / 1e6;

        int edges = graph.getEdges().size();
        int lowerBound = task3Service.calculateLowerBound(graph);
        int upperBound = task3Service.calculateUpperBound(graph);

        // Test DSATUR
        benchmarkAlgorithm(graph, new DsaturColorer(), "DSATUR", numVertices, edges, buildTimeMs, lowerBound, upperBound, false);

        // Test Welsh-Powell
        benchmarkAlgorithm(graph, new WelshPowellColorer(), "Welsh-Powell", numVertices, edges, buildTimeMs, lowerBound, upperBound, false);

        // Test Backtracking only for V <= 15
        if (numVertices <= 15) {
            benchmarkAlgorithm(graph, new BacktrackingColorer(), "Backtracking", numVertices, edges, buildTimeMs, lowerBound, upperBound, true);
        }
    }

    private static void benchmarkAlgorithm(ConflictGraph graph, GraphColorer colorer, String algName,
                                            int v, int edges, double buildTimeMs, int lowerBound, int upperBound, boolean isExact) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        long startColor = System.nanoTime();
        GraphColorer.ColoringResult result = colorer.color(graph);
        long endColor = System.nanoTime();

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        double colorTimeMs = (endColor - startColor) / 1e6;
        long memUsedKb = Math.max(0, (memAfter - memBefore) / 1024);

        int violations = task3Service.countHardConstraintViolations(graph, result.getColorOf());
        String status = task3Service.determineStatus(result.getNumColors(), lowerBound, violations, isExact);
        String boundsStr = lowerBound + " / " + upperBound;

        System.out.printf("%-6d | %-15s | %-8d | %-12.3f | %-12.3f | %-10d | %-12d | %-10s | %-10s\n",
                v, algName, edges, buildTimeMs, colorTimeMs, memUsedKb, result.getNumColors(), boundsStr, status);
    }

    public static List<StudentEnrollment> generateSyntheticEnrollments(int numVertices, Random random) {
        List<String> courseCodes = new ArrayList<>();
        for (int i = 1; i <= numVertices; i++) {
            courseCodes.add(String.format("COURSE_%03d", i));
        }

        // Generate student enrollments creating realistic graph conflicts
        int numStudents = numVertices * 3;
        List<StudentEnrollment> enrollments = new ArrayList<>();

        for (int s = 1; s <= numStudents; s++) {
            StudentEnrollment se = new StudentEnrollment();
            se.setStudentId("STU_" + s);
            
            // Each student picks 2 to 4 courses
            int k = 2 + random.nextInt(3); // 2, 3, or 4 courses
            Set<String> studentCourses = new LinkedHashSet<>();
            while (studentCourses.size() < k) {
                studentCourses.add(courseCodes.get(random.nextInt(numVertices)));
            }
            se.setEnrolledCourses(new ArrayList<>(studentCourses));
            enrollments.add(se);
        }

        return enrollments;
    }
}
