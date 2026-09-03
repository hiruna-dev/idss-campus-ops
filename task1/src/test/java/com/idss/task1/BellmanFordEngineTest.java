package com.idss.task1;

import com.idss.task1.algorithm.BellmanFordEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.algorithm.DijkstraEngine;
import com.idss.task1.model.RouteSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BellmanFordEngine (Student A Deliverable).
 * Validates theoretical algorithm correctness and equivalence against Dijkstra baseline.
 */
class BellmanFordEngineTest {

    private BuildingGraph graph;
    private BellmanFordEngine bellmanFord;
    private DijkstraEngine dijkstra;

    @BeforeEach
    void setUp() throws IOException {
        graph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
        bellmanFord = new BellmanFordEngine();
        dijkstra = new DijkstraEngine();
    }

    @Test
    @DisplayName("Should find valid shortest path from Vault to Room R101")
    void testShortestPathToR101() {
        RouteSearchResult bfResult = bellmanFord.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);

        assertNotNull(bfResult);
        assertTrue(bfResult.isReachable());
        assertEquals("VAULT_G01", bfResult.getPathSequence().get(0));
        assertEquals("ROOM_R101", bfResult.getPathSequence().get(bfResult.getPathSequence().size() - 1));
    }

    @Test
    @DisplayName("Bellman-Ford and Dijkstra MUST produce the EXACT same distance and transit time")
    void testEquivalenceWithDijkstra() {
        String[] targetRooms = {"ROOM_R101", "ROOM_R102", "ROOM_R201", "ROOM_R202", "ROOM_LAB3A"};

        for (String target : targetRooms) {
            // Test Foot Courier (stairs allowed)
            RouteSearchResult dFoot = dijkstra.findShortestPath(graph, "VAULT_G01", target, false);
            RouteSearchResult bfFoot = bellmanFord.findShortestPath(graph, "VAULT_G01", target, false);

            assertTrue(dFoot.isReachable() && bfFoot.isReachable(), "Target " + target + " should be reachable");
            assertEquals(dFoot.getTotalTransitTimeSeconds(), bfFoot.getTotalTransitTimeSeconds(),
                    "Transit time to " + target + " must match between Dijkstra and Bellman-Ford");
            assertEquals(dFoot.getTotalDistanceMeters(), bfFoot.getTotalDistanceMeters(), 0.001,
                    "Distance to " + target + " must match between Dijkstra and Bellman-Ford");

            // Test Step-Free Trolley (elevators only)
            RouteSearchResult dStepFree = dijkstra.findShortestPath(graph, "VAULT_G01", target, true);
            RouteSearchResult bfStepFree = bellmanFord.findShortestPath(graph, "VAULT_G01", target, true);

            assertEquals(dStepFree.isReachable(), bfStepFree.isReachable());
            if (dStepFree.isReachable()) {
                assertEquals(dStepFree.getTotalTransitTimeSeconds(), bfStepFree.getTotalTransitTimeSeconds(),
                        "Step-free transit time to " + target + " must match between Dijkstra and Bellman-Ford");
                assertEquals(dStepFree.getTotalDistanceMeters(), bfStepFree.getTotalDistanceMeters(), 0.001,
                        "Step-free distance to " + target + " must match between Dijkstra and Bellman-Ford");
            }
        }
    }

    @Test
    @DisplayName("Should enforce step-free elevator path for trolley delivery")
    void testStepFreeRouting() {
        RouteSearchResult result = bellmanFord.findShortestPath(graph, "VAULT_G01", "ROOM_R101", true);
        assertNotNull(result);
        assertTrue(result.isReachable());
        assertTrue(result.getPathSequence().contains("ELEV_G"), "Must use Ground Elevator");
        assertFalse(result.getPathSequence().contains("STAIR_G_EAST"), "Must NOT use stairs");
    }

    @Test
    @DisplayName("Should return unreachable for invalid destination")
    void testUnreachableNode() {
        RouteSearchResult result = bellmanFord.findShortestPath(graph, "VAULT_G01", "INVALID_ROOM", false);
        assertNotNull(result);
        assertFalse(result.isReachable());
    }
}
