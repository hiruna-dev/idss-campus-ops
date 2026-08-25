package com.idss.task1;

import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.algorithm.DijkstraEngine;
import com.idss.task1.model.RouteSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DijkstraEngine (Student A Deliverable).
 * Validates baseline single-source shortest path calculation and step-free constraint enforcement.
 */
class DijkstraEngineTest {

    private BuildingGraph graph;
    private DijkstraEngine dijkstra;

    @BeforeEach
    void setUp() throws IOException {
        graph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
        dijkstra = new DijkstraEngine();
    }

    @Test
    @DisplayName("Should find valid shortest path from Vault to Room R101 for foot courier (using stairs)")
    void testFootCourierPathToR101() {
        RouteSearchResult result = dijkstra.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Path to R101 should be reachable");
        assertTrue(result.getTotalDistanceMeters() > 0, "Distance should be > 0");
        assertTrue(result.getTotalTransitTimeSeconds() > 0, "Time should be > 0");
        assertEquals("VAULT_G01", result.getPathSequence().get(0), "Path must start at Vault");
        assertEquals("ROOM_R101", result.getPathSequence().get(result.getPathSequence().size() - 1), "Path must end at Room R101");

        // Foot courier should take stairs for fastest transit
        assertTrue(result.getPathSequence().contains("STAIR_G_EAST"), "Foot courier should use east stairs");
    }

    @Test
    @DisplayName("Should enforce step-free path (using elevator) when requiresStepFree is true")
    void testStepFreePathToR101() {
        RouteSearchResult result = dijkstra.findShortestPath(graph, "VAULT_G01", "ROOM_R101", true);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Step-free path to R101 should be reachable");
        assertEquals("VAULT_G01", result.getPathSequence().get(0));
        assertEquals("ROOM_R101", result.getPathSequence().get(result.getPathSequence().size() - 1));

        // Must use Elevator and NOT stairs
        assertTrue(result.getPathSequence().contains("ELEV_G"), "Step-free route MUST use Ground Elevator");
        assertTrue(result.getPathSequence().contains("ELEV_F1"), "Step-free route MUST use Floor 1 Elevator");
        assertFalse(result.getPathSequence().contains("STAIR_G_EAST"), "Step-free route must NOT contain stairs");
        assertFalse(result.getPathSequence().contains("STAIR_F1_EAST"), "Step-free route must NOT contain stairs");
    }

    @Test
    @DisplayName("Should route across multiple floors to Floor 3 Lab 3A")
    void testMultiFloorRouteToFloor3() {
        RouteSearchResult result = dijkstra.findShortestPath(graph, "VAULT_G01", "ROOM_LAB3A", false);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Path to Lab 3A on Floor 3 should be reachable");
        assertEquals("VAULT_G01", result.getPathSequence().get(0));
        assertEquals("ROOM_LAB3A", result.getPathSequence().get(result.getPathSequence().size() - 1));
        assertTrue(result.getPathSequence().contains("STAIR_F3_WEST") || result.getPathSequence().contains("ELEV_F3"),
                "Route must reach 3rd floor vertical connectors");
    }

    @Test
    @DisplayName("Should return unreachable result for non-existent node")
    void testNonExistentNode() {
        RouteSearchResult result = dijkstra.findShortestPath(graph, "VAULT_G01", "NON_EXISTENT_ROOM", false);
        assertNotNull(result);
        assertFalse(result.isReachable(), "Non-existent destination must be marked unreachable");
        assertEquals(0.0, result.getTotalDistanceMeters());
    }

    @Test
    @DisplayName("Should handle source equals target trivially")
    void testSourceEqualsTarget() {
        RouteSearchResult result = dijkstra.findShortestPath(graph, "VAULT_G01", "VAULT_G01", false);
        assertNotNull(result);
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalDistanceMeters());
        assertEquals(0, result.getTotalTransitTimeSeconds());
        assertEquals(1, result.getPathSequence().size());
    }
}
