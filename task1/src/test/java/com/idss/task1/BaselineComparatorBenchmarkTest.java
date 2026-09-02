package com.idss.task1;

import com.idss.task1.algorithm.AStarEngine;
import com.idss.task1.algorithm.BellmanFordEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.algorithm.DijkstraEngine;
import com.idss.task1.model.Coordinates3D;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;
import com.idss.task1.model.RouteSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline Comparator Profiling Suite (Student A Deliverable - Subtask 5.1).
 *
 * <p>Formally profiles and benchmarks the baseline {@link DijkstraEngine} (O((V + E) log V))
 * against the theoretical comparator {@link BellmanFordEngine} (O(V * E)) and {@link AStarEngine}
 * across varying graph sizes (V = 34 campus graph and synthetic V = 10, 30, 50, 100, 250).
 * Generates empirical proof of optimality parity and asymptotic scaling tables for
 * Chapters 3, 7, and 8 of Student A's Individual Coursework Report.</p>
 */
public class BaselineComparatorBenchmarkTest {

    private BuildingGraph campusGraph;
    private DijkstraEngine dijkstraEngine;
    private BellmanFordEngine bellmanFordEngine;
    private AStarEngine aStarEngine;

    @BeforeEach
    public void setUp() throws IOException {
        campusGraph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
        dijkstraEngine = new DijkstraEngine();
        bellmanFordEngine = new BellmanFordEngine();
        aStarEngine = new AStarEngine();
    }

    @Test
    @DisplayName("Should prove exact optimality parity across Dijkstra, Bellman-Ford, and A* on all campus exam rooms")
    public void testOptimalityParityAcrossEngines() {
        String[] targetRooms = {
                "ROOM_R101", "ROOM_R102", "ROOM_R103",
                "ROOM_R201", "ROOM_R202", "ROOM_R203",
                "ROOM_R301", "ROOM_R302", "ROOM_LAB3A"
        };

        System.out.println("\n=========================================================================================");
        System.out.println(" STUDENT A: BASELINE ALGORITHM OPTIMALITY PARITY PROOF (CAMPUS GRAPH V=34, E=74)");
        System.out.println("=========================================================================================");
        System.out.printf("%-12s | %-12s | %-12s | %-12s | %-15s | %-10s%n",
                "Destination", "Dijkstra (s)", "Bellman-F (s)", "A* (s)", "Dist Parity (m)", "Optimality");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (String room : targetRooms) {
            RouteSearchResult dRes = dijkstraEngine.findShortestPath(campusGraph, "VAULT_G01", room, false);
            RouteSearchResult bRes = bellmanFordEngine.findShortestPath(campusGraph, "VAULT_G01", room, false);
            RouteSearchResult aRes = aStarEngine.findShortestPath(campusGraph, "VAULT_G01", room, false);

            assertTrue(dRes.isReachable(), "Dijkstra must reach " + room);
            assertTrue(bRes.isReachable(), "Bellman-Ford must reach " + room);
            assertTrue(aRes.isReachable(), "A* must reach " + room);

            // Exact parity assertions (LO1 Proof of Correctness)
            assertEquals(dRes.getTotalTransitTimeSeconds(), bRes.getTotalTransitTimeSeconds(),
                    "Bellman-Ford transit time must match Dijkstra for " + room);
            assertEquals(dRes.getTotalTransitTimeSeconds(), aRes.getTotalTransitTimeSeconds(),
                    "A* transit time must match Dijkstra for " + room);
            assertEquals(dRes.getTotalDistanceMeters(), bRes.getTotalDistanceMeters(), 0.001,
                    "Bellman-Ford distance must match Dijkstra for " + room);
            assertEquals(dRes.getTotalDistanceMeters(), aRes.getTotalDistanceMeters(), 0.001,
                    "A* distance must match Dijkstra for " + room);

            System.out.printf("%-12s | %-12d | %-12d | %-12d | %-15.1f | %-10s%n",
                    room,
                    dRes.getTotalTransitTimeSeconds(),
                    bRes.getTotalTransitTimeSeconds(),
                    aRes.getTotalTransitTimeSeconds(),
                    dRes.getTotalDistanceMeters(),
                    "100.0% PARITY");
        }
        System.out.println("=========================================================================================\n");
    }

    @Test
    @DisplayName("Should profile execution latency and scaling of Dijkstra vs Bellman-Ford across synthetic scales")
    public void testScalingProfilingDijkstraVsBellmanFord() {
        int[] vertexCounts = {10, 30, 50, 100, 250};
        int warmupRuns = 50;
        int benchmarkRuns = 100;

        System.out.println("\n=========================================================================================");
        System.out.println(" STUDENT A: EMPIRICAL COMPLEXITY PROFILING: DIJKSTRA vs BELLMAN-FORD (LO1/LO3)");
        System.out.println("=========================================================================================");
        System.out.printf("%-8s | %-8s | %-16s | %-16s | %-12s | %-14s%n",
                "Scale V", "Edges E", "Dijkstra (µs)", "Bellman-Ford (µs)", "Speedup", "Big-O Contrast");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (int v : vertexCounts) {
            int floors = Math.max(1, v / 10);
            BuildingGraph synthGraph = generateSyntheticGraph(v, floors);
            String source = "NODE_0";
            String target = "NODE_" + (v - 1);

            // Warmup JVM JIT compiler
            for (int i = 0; i < warmupRuns; i++) {
                dijkstraEngine.findShortestPath(synthGraph, source, target, false);
                bellmanFordEngine.findShortestPath(synthGraph, source, target, false);
            }

            // Benchmark Dijkstra
            long startD = System.nanoTime();
            for (int i = 0; i < benchmarkRuns; i++) {
                dijkstraEngine.findShortestPath(synthGraph, source, target, false);
            }
            long elapsedD = System.nanoTime() - startD;
            double avgDijkstraMicros = (elapsedD / (double) benchmarkRuns) / 1_000.0;

            // Benchmark Bellman-Ford
            long startB = System.nanoTime();
            for (int i = 0; i < benchmarkRuns; i++) {
                bellmanFordEngine.findShortestPath(synthGraph, source, target, false);
            }
            long elapsedB = System.nanoTime() - startB;
            double avgBellmanMicros = (elapsedB / (double) benchmarkRuns) / 1_000.0;

            double speedup = (avgDijkstraMicros > 0) ? (avgBellmanMicros / avgDijkstraMicros) : 1.0;

            assertTrue(avgDijkstraMicros <= avgBellmanMicros * 1.5,
                    "Dijkstra O((V+E)log V) must outperform Bellman-Ford O(V*E) as graph scales");

            System.out.printf("%-8d | %-8d | %-16.2f | %-16.2f | %-11.2fx | %-14s%n",
                    synthGraph.getVertexCount(),
                    synthGraph.getEdgeCount(),
                    avgDijkstraMicros,
                    avgBellmanMicros,
                    speedup,
                    "O(E log V) < O(VE)");
        }
        System.out.println("=========================================================================================\n");
    }

    /**
     * Synthesizes a deterministic multi-floor corridor graph topology.
     */
    private BuildingGraph generateSyntheticGraph(int totalNodes, int floors) {
        BuildingGraph graph = new BuildingGraph();
        int nodesPerFloor = Math.max(1, totalNodes / floors);

        for (int i = 0; i < totalNodes; i++) {
            int floor = i / nodesPerFloor;
            double x = (i % nodesPerFloor) * 10.0;
            double y = (i % 2 == 0) ? 0.0 : 15.0;
            double z = floor * 4.0;

            Node3D node = new Node3D(
                    "NODE_" + i,
                    "Synthetic Corridor Node " + i,
                    floor,
                    new Coordinates3D(x, y, z),
                    (i == 0) ? "VAULT" : (i == totalNodes - 1 ? "EXAM_ROOM" : "JUNCTION"),
                    true,
                    new ArrayList<>()
            );
            graph.addNode(node);
        }

        // Horizontal corridor edges
        for (int i = 0; i < totalNodes; i++) {
            if ((i + 1) % nodesPerFloor != 0 && (i + 1) < totalNodes) {
                Edge3D forward = new Edge3D("NODE_" + (i + 1), 10.0, 8, "CORRIDOR", true, 1);
                Edge3D backward = new Edge3D("NODE_" + i, 10.0, 8, "CORRIDOR", true, 1);
                graph.addEdge("NODE_" + i, forward);
                graph.addEdge("NODE_" + (i + 1), backward);
            }
        }

        // Vertical floor transition edges (Elevators + Stairs)
        for (int f = 0; f < floors - 1; f++) {
            int nodeFloorA = f * nodesPerFloor;
            int nodeFloorB = (f + 1) * nodesPerFloor;
            if (nodeFloorB < totalNodes) {
                // Elevator edge
                graph.addEdge("NODE_" + nodeFloorA, new Edge3D("NODE_" + nodeFloorB, 4.0, 20, "ELEVATOR_SHAFT", true, 1));
                graph.addEdge("NODE_" + nodeFloorB, new Edge3D("NODE_" + nodeFloorA, 4.0, 20, "ELEVATOR_SHAFT", true, 1));

                // Staircase edge
                int stairA = Math.min(nodeFloorA + 1, totalNodes - 1);
                int stairB = Math.min(nodeFloorB + 1, totalNodes - 1);
                graph.addEdge("NODE_" + stairA, new Edge3D("NODE_" + stairB, 6.0, 18, "STAIRCASE", false, 1));
                graph.addEdge("NODE_" + stairB, new Edge3D("NODE_" + stairA, 6.0, 18, "STAIRCASE", false, 1));
            }
        }

        return graph;
    }
}
