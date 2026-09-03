package com.idss.task1;

import com.idss.common.util.JsonLoader;
import com.idss.task1.model.RoutingMetrics;
import com.idss.task1.service.BenchmarkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Empirical Benchmark Suite (Student B Deliverable - Subtask 5.3, LO3).
 *
 * <p>Validates {@link BenchmarkService}'s single-run campus benchmark (feeds
 * {@code GET /api/task1/benchmark}) and runs the full A* vs Dijkstra vs Bellman-Ford
 * scaling sweep (V = 10, 30, 50, 100, 500, 1000), emitting Markdown and CSV tables to
 * {@code data/shared/} for direct inclusion in Chapter 8 of the Individual Report.</p>
 */
class BenchmarkTest {

    private final BenchmarkService benchmarkService = new BenchmarkService(
            "data/input/input_building_graph.json",
            "data/shared/output_routing_metrics.json"
    );

    @Test
    @DisplayName("Campus benchmark should produce valid metrics and write output_routing_metrics.json")
    void testCampusBenchmarkWritesValidMetrics() throws IOException {
        RoutingMetrics metrics = benchmarkService.runCampusBenchmark();

        assertNotNull(metrics);
        assertEquals("OPTIMAL", metrics.getStatus());
        assertTrue(metrics.getExecutionTimeMs() >= 0, "Execution time must be non-negative");
        assertTrue(metrics.getMemoryAllocatedKb() >= 0, "Memory allocated must be non-negative");
        assertTrue(metrics.getNodesExploredPercentage() > 0 && metrics.getNodesExploredPercentage() <= 100,
                "Nodes explored percentage must be in (0, 100]");
        assertEquals(0, metrics.getHardConstraintViolations());
        assertEquals(1.0, metrics.getOptimalityRatio(), 0.01, "A* must match Dijkstra's optimal distance");

        File outputFile = JsonLoader.resolve("data/shared/output_routing_metrics.json");
        assertTrue(outputFile.exists(), "output_routing_metrics.json should be written to disk");

        RoutingMetrics reloaded = JsonLoader.load("data/shared/output_routing_metrics.json", RoutingMetrics.class);
        assertEquals(metrics.getStatus(), reloaded.getStatus());
    }

    @Test
    @DisplayName("Scaling sweep should show A* matching Dijkstra optimality and exploring no more nodes at every scale")
    void testScalingSweepCorrectnessAcrossScales() {
        List<BenchmarkService.ScalingResult> results = benchmarkService.runScalingSweep(new int[]{10, 30, 50, 100});

        assertEquals(4, results.size());
        for (BenchmarkService.ScalingResult r : results) {
            assertTrue(r.optimalityMatched, "A* distance must match Dijkstra at V=" + r.vertexCount);
            assertTrue(r.nodesExploredPercentage > 0 && r.nodesExploredPercentage <= 100,
                    "Nodes explored percentage must be in (0, 100] at V=" + r.vertexCount);
            assertTrue(r.dijkstraMs >= 0 && r.aStarMs >= 0 && r.bellmanFordMs >= 0,
                    "All timings must be non-negative at V=" + r.vertexCount);
        }
    }

    @Test
    @DisplayName("Should generate Markdown and CSV scaling tables for Chapter 8 (V=10..1000)")
    void testGenerateChapter8Tables() throws IOException {
        List<BenchmarkService.ScalingResult> results = benchmarkService.runScalingSweep();
        assertEquals(6, results.size());

        String markdown = toMarkdownTable(results);
        String csv = toCsv(results);

        System.out.println("\n=== Chapter 8: A* vs Dijkstra vs Bellman-Ford Scaling Table (Markdown) ===");
        System.out.println(markdown);
        System.out.println("=== Chapter 8: Scaling Table (CSV) ===");
        System.out.println(csv);

        writeToFile("data/shared/benchmark_task1_scaling.md", markdown);
        writeToFile("data/shared/benchmark_task1_scaling.csv", csv);

        assertTrue(JsonLoader.resolve("data/shared/benchmark_task1_scaling.md").exists());
        assertTrue(JsonLoader.resolve("data/shared/benchmark_task1_scaling.csv").exists());

        // A* should show a growing (or at least non-degrading) speedup advantage as the graph scales up
        BenchmarkService.ScalingResult smallest = results.get(0);
        BenchmarkService.ScalingResult largest = results.get(results.size() - 1);
        assertTrue(largest.aStarSpeedupVsDijkstra > 0, "A* speedup ratio must be positive at the largest scale");
        assertNotNull(smallest);
    }

    private String toMarkdownTable(List<BenchmarkService.ScalingResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("| Size (V) | Dijkstra Time (ms) | A* Time (ms) | Bellman-Ford (ms) | A* Speedup | Nodes Explored (%) |\n");
        sb.append("|---|---|---|---|---|---|\n");
        for (BenchmarkService.ScalingResult r : results) {
            sb.append(String.format("| V=%d | %.3f ms | %.3f ms | %.3f ms | %.2fx | %.2f%% |%n",
                    r.vertexCount, r.dijkstraMs, r.aStarMs, r.bellmanFordMs, r.aStarSpeedupVsDijkstra, r.nodesExploredPercentage));
        }
        return sb.toString();
    }

    private String toCsv(List<BenchmarkService.ScalingResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("vertex_count,edge_count,dijkstra_ms,a_star_ms,bellman_ford_ms,a_star_speedup,nodes_explored_percentage,optimality_matched\n");
        for (BenchmarkService.ScalingResult r : results) {
            sb.append(String.format("%d,%d,%.3f,%.3f,%.3f,%.2f,%.2f,%b%n",
                    r.vertexCount, r.edgeCount, r.dijkstraMs, r.aStarMs, r.bellmanFordMs,
                    r.aStarSpeedupVsDijkstra, r.nodesExploredPercentage, r.optimalityMatched));
        }
        return sb.toString();
    }

    private void writeToFile(String path, String content) throws IOException {
        File target = JsonLoader.resolve(path);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(target)) {
            writer.write(content);
        }
    }
}
