package com.idss.task1.service;

import com.idss.common.util.JsonLoader;
import com.idss.task1.algorithm.AStarEngine;
import com.idss.task1.algorithm.BellmanFordEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.algorithm.DijkstraEngine;
import com.idss.task1.model.Coordinates3D;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;
import com.idss.task1.model.RouteSearchResult;
import com.idss.task1.model.RoutingMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Empirical Benchmarking Framework for Task 1 (Student B Deliverable - Subtask 5.3, LO3).
 *
 * <p>{@link #runCampusBenchmark()} profiles A* on the real campus graph and writes the
 * canonical {@code output_routing_metrics.json} consumed by
 * {@code RouteController#getBenchmark}. {@link #runScalingSweep()} compares A* against
 * the {@link DijkstraEngine} baseline and {@link BellmanFordEngine} theoretical contrast
 * across synthetic multi-floor graphs of increasing scale (V = 10, 30, 50, 100, 500, 1000),
 * producing the empirical time/speedup table for Chapter 8 of the Individual Report.</p>
 */
@Service
public class BenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);
    private static final int[] DEFAULT_VERTEX_SCALES = {10, 30, 50, 100, 500, 1000};
    private static final String DEFAULT_TARGET_ROOM = "ROOM_LAB3A"; // farthest room: exercises max A* pruning
    private static final int BRANCH_COUNT = 4; // corridor wings radiating from each floor's hub

    private final AStarEngine aStarEngine = new AStarEngine();
    private final DijkstraEngine dijkstraEngine = new DijkstraEngine();
    private final BellmanFordEngine bellmanFordEngine = new BellmanFordEngine();

    private final String buildingGraphPath;
    private final String outputMetricsPath;

    public BenchmarkService(
            @Value("${idss.task1.building-graph-path:data/input/input_building_graph.json}") String buildingGraphPath,
            @Value("${idss.task1.output-metrics-path:data/shared/output_routing_metrics.json}") String outputMetricsPath) {
        this.buildingGraphPath = buildingGraphPath;
        this.outputMetricsPath = outputMetricsPath;
    }

    /**
     * Benchmarks A* from the source vault to the farthest campus exam room, builds a
     * {@link RoutingMetrics} summary, writes it to {@code output_routing_metrics.json},
     * and returns it.
     */
    public RoutingMetrics runCampusBenchmark() {
        return runCampusBenchmark(DEFAULT_TARGET_ROOM, false);
    }

    public RoutingMetrics runCampusBenchmark(String targetRoom, boolean requiresStepFree) {
        BuildingGraph graph = loadCampusGraph();

        double memoryBeforeKb = usedMemoryKb();
        double aStarMs = averageExecutionTimeMs(
                () -> aStarEngine.findShortestPath(graph, "VAULT_G01", targetRoom, requiresStepFree), 20);
        RouteSearchResult aStarResult = aStarEngine.findShortestPath(graph, "VAULT_G01", targetRoom, requiresStepFree);
        double memoryAfterKb = usedMemoryKb();

        RouteSearchResult dijkstraResult = dijkstraEngine.findShortestPath(graph, "VAULT_G01", targetRoom, requiresStepFree);

        int totalVertices = Math.max(1, graph.getVertexCount());
        double nodesExploredPct = (aStarResult.getNodesExplored() * 100.0) / totalVertices;

        double optimalityRatio = 1.0;
        if (aStarResult.isReachable() && dijkstraResult.isReachable() && aStarResult.getTotalDistanceMeters() > 0) {
            optimalityRatio = dijkstraResult.getTotalDistanceMeters() / aStarResult.getTotalDistanceMeters();
        }

        boolean stepFreeSatisfied = !requiresStepFree || aStarResult.isReachable();
        int hardViolations = aStarResult.isReachable() ? 0 : 1;

        RoutingMetrics metrics = new RoutingMetrics(
                Instant.now().toString(),
                AStarEngine.ALGORITHM_NAME,
                "PDSA_26.1_v1.0",
                round3(aStarMs),
                round2(Math.max(0.0, memoryAfterKb - memoryBeforeKb)),
                round2(nodesExploredPct),
                stepFreeSatisfied ? 100.0 : 0.0,
                0,
                hardViolations,
                round2(optimalityRatio),
                aStarResult.isReachable() ? "OPTIMAL" : "INFEASIBLE"
        );

        saveMetrics(metrics);
        return metrics;
    }

    /**
     * Runs the A* vs Dijkstra vs Bellman-Ford scaling sweep across synthetic multi-floor
     * graphs of increasing vertex count (default V = 10, 30, 50, 100, 500, 1000).
     */
    public List<ScalingResult> runScalingSweep() {
        return runScalingSweep(DEFAULT_VERTEX_SCALES);
    }

    public List<ScalingResult> runScalingSweep(int[] vertexScales) {
        List<ScalingResult> results = new ArrayList<>();
        for (int v : vertexScales) {
            results.add(benchmarkScale(v));
        }
        return results;
    }

    private ScalingResult benchmarkScale(int vertexCount) {
        int floors = Math.max(1, Math.min(5, vertexCount / 20));
        BuildingGraph graph = generateSyntheticGraph(vertexCount, floors);
        String source = "NODE_0";
        String target = "NODE_" + (vertexCount - 1);

        int warmupRuns = vertexCount <= 100 ? 20 : 5;
        int benchmarkRuns = vertexCount <= 100 ? 50 : 10;

        for (int i = 0; i < warmupRuns; i++) {
            aStarEngine.findShortestPath(graph, source, target, false);
            dijkstraEngine.findShortestPath(graph, source, target, false);
            bellmanFordEngine.findShortestPath(graph, source, target, false);
        }

        double aStarMs = averageExecutionTimeMs(() -> aStarEngine.findShortestPath(graph, source, target, false), benchmarkRuns);
        double dijkstraMs = averageExecutionTimeMs(() -> dijkstraEngine.findShortestPath(graph, source, target, false), benchmarkRuns);
        double bellmanFordMs = averageExecutionTimeMs(() -> bellmanFordEngine.findShortestPath(graph, source, target, false), benchmarkRuns);

        RouteSearchResult aStarResult = aStarEngine.findShortestPath(graph, source, target, false);
        RouteSearchResult dijkstraResult = dijkstraEngine.findShortestPath(graph, source, target, false);

        double nodesExploredPct = graph.getVertexCount() > 0
                ? (aStarResult.getNodesExplored() * 100.0) / graph.getVertexCount()
                : 0.0;
        double speedup = aStarMs > 0 ? dijkstraMs / aStarMs : 1.0;
        boolean optimalityMatched = aStarResult.isReachable() && dijkstraResult.isReachable()
                && Math.abs(aStarResult.getTotalDistanceMeters() - dijkstraResult.getTotalDistanceMeters()) < 0.01;

        return new ScalingResult(
                vertexCount, graph.getEdgeCount(),
                round3(dijkstraMs), round3(aStarMs), round3(bellmanFordMs),
                round2(speedup), round2(nodesExploredPct), optimalityMatched
        );
    }

    private BuildingGraph loadCampusGraph() {
        try {
            return BuildingGraph.loadFromJson(buildingGraphPath);
        } catch (IOException e) {
            log.warn("Could not load building graph for benchmarking from '{}': {}", buildingGraphPath, e.getMessage());
            return new BuildingGraph();
        }
    }

    private void saveMetrics(RoutingMetrics metrics) {
        try {
            JsonLoader.write(outputMetricsPath, metrics);
            log.info("Saved routing benchmark metrics to '{}'", JsonLoader.resolve(outputMetricsPath).getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write routing metrics to '{}': {}", outputMetricsPath, e.getMessage());
        }
    }

    private double averageExecutionTimeMs(Supplier<RouteSearchResult> action, int runs) {
        long startNano = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            action.get();
        }
        long elapsedNano = System.nanoTime() - startNano;
        return (elapsedNano / (double) runs) / 1_000_000.0;
    }

    private double usedMemoryKb() {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024.0;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    /**
     * Synthesizes a deterministic multi-floor hub-and-spoke graph topology of the given
     * size, used only for scaling benchmarks (not correctness proofs, which use the real
     * campus graph instead).
     *
     * <p>Each floor is a central hub with {@link #BRANCH_COUNT} corridor branches radiating
     * outward in different (x, y) directions, and floors stack vertically via hub-to-hub
     * elevator shafts — mirroring the real campus's single central shaft serving multiple
     * wings per floor. This gives A* actual wrong turns to prune (sibling branches/floors
     * that lead away from the target), unlike a single corridor chain where every node lies
     * on the only path and both algorithms must visit all of them regardless of heuristic
     * guidance.</p>
     */
    private BuildingGraph generateSyntheticGraph(int totalNodes, int floors) {
        BuildingGraph graph = new BuildingGraph();
        int nodesPerFloor = Math.max(BRANCH_COUNT + 1, totalNodes / floors);
        // Ceiling division so hub + branches never undershoot nodesPerFloor: the inner
        // "nodeCounter < totalNodes" guards then cap generation at exactly totalNodes,
        // guaranteeing NODE_(totalNodes - 1) (the benchmark target) always gets created.
        int branchLength = Math.max(1, (nodesPerFloor - 1 + BRANCH_COUNT - 1) / BRANCH_COUNT);

        double[] dirX = {1.0, -1.0, 0.0, 0.0};
        double[] dirY = {0.0, 0.0, 1.0, -1.0};

        String[] hubIds = new String[floors];
        int nodeCounter = 0;

        for (int f = 0; f < floors && nodeCounter < totalNodes; f++) {
            double z = f * 4.0;
            String hubId = "NODE_" + nodeCounter;
            hubIds[f] = hubId;
            graph.addNode(new Node3D(hubId, "Hub Floor " + f, f, new Coordinates3D(0.0, 0.0, z),
                    f == 0 ? "VAULT" : "JUNCTION", true, new ArrayList<>()));
            nodeCounter++;

            for (int b = 0; b < BRANCH_COUNT && nodeCounter < totalNodes; b++) {
                String prevId = hubId;
                for (int i = 1; i <= branchLength && nodeCounter < totalNodes; i++) {
                    String nodeId = "NODE_" + nodeCounter;
                    double x = dirX[b] * i * 10.0;
                    double y = dirY[b] * i * 10.0;
                    boolean isLastOverall = (nodeCounter == totalNodes - 1);

                    graph.addNode(new Node3D(nodeId, "Branch Node " + nodeCounter, f, new Coordinates3D(x, y, z),
                            isLastOverall ? "EXAM_ROOM" : "JUNCTION", true, new ArrayList<>()));
                    graph.addEdge(prevId, new Edge3D(nodeId, 10.0, 8, "CORRIDOR", true, 1));
                    graph.addEdge(nodeId, new Edge3D(prevId, 10.0, 8, "CORRIDOR", true, 1));

                    prevId = nodeId;
                    nodeCounter++;
                }
            }
        }

        for (int f = 0; f < floors - 1; f++) {
            if (hubIds[f] != null && hubIds[f + 1] != null) {
                graph.addEdge(hubIds[f], new Edge3D(hubIds[f + 1], 4.0, 20, "ELEVATOR_SHAFT", true, 1));
                graph.addEdge(hubIds[f + 1], new Edge3D(hubIds[f], 4.0, 20, "ELEVATOR_SHAFT", true, 1));
            }
        }

        return graph;
    }

    /** One row of the V-scale comparison table (Chapter 8). */
    public static class ScalingResult {
        public final int vertexCount;
        public final int edgeCount;
        public final double dijkstraMs;
        public final double aStarMs;
        public final double bellmanFordMs;
        public final double aStarSpeedupVsDijkstra;
        public final double nodesExploredPercentage;
        public final boolean optimalityMatched;

        public ScalingResult(int vertexCount, int edgeCount, double dijkstraMs, double aStarMs, double bellmanFordMs,
                              double aStarSpeedupVsDijkstra, double nodesExploredPercentage, boolean optimalityMatched) {
            this.vertexCount = vertexCount;
            this.edgeCount = edgeCount;
            this.dijkstraMs = dijkstraMs;
            this.aStarMs = aStarMs;
            this.bellmanFordMs = bellmanFordMs;
            this.aStarSpeedupVsDijkstra = aStarSpeedupVsDijkstra;
            this.nodesExploredPercentage = nodesExploredPercentage;
            this.optimalityMatched = optimalityMatched;
        }
    }
}
