package com.idss.task1;

import com.idss.task1.algorithm.AStarEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.algorithm.DijkstraEngine;
import com.idss.task1.model.RouteSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AStarEngine (Student B Deliverable).
 * Validates A* correctness against the Dijkstra baseline (optimality), search pruning
 * efficiency (fewer nodes explored), and step-free accessibility constraint enforcement.
 */
class AStarEngineTest {

    private static final String[] TARGET_ROOMS = {
            "ROOM_R101", "ROOM_R102", "ROOM_R103",
            "ROOM_R201", "ROOM_R202", "ROOM_R203",
            "ROOM_R301", "ROOM_R302", "ROOM_LAB3A"
    };

    private BuildingGraph graph;
    private AStarEngine aStar;
    private DijkstraEngine dijkstra;

    @BeforeEach
    void setUp() throws IOException {
        graph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
        aStar = new AStarEngine();
        dijkstra = new DijkstraEngine();
    }

    @Test
    @DisplayName("Should find valid shortest path from Vault to Room R101")
    void testShortestPathToR101() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Path to R101 should be reachable");
        assertTrue(result.getTotalDistanceMeters() > 0, "Distance should be > 0");
        assertTrue(result.getTotalTransitTimeSeconds() > 0, "Time should be > 0");
        assertEquals("VAULT_G01", result.getPathSequence().get(0), "Path must start at Vault");
        assertEquals("ROOM_R101", result.getPathSequence().get(result.getPathSequence().size() - 1), "Path must end at Room R101");
    }

    @Test
    @DisplayName("A* and Dijkstra MUST produce the EXACT same optimal distance and transit time (LO1 optimality proof)")
    void testOptimalityMatchesDijkstra() {
        for (String target : TARGET_ROOMS) {
            // Foot courier (stairs allowed)
            RouteSearchResult aFoot = aStar.findShortestPath(graph, "VAULT_G01", target, false);
            RouteSearchResult dFoot = dijkstra.findShortestPath(graph, "VAULT_G01", target, false);

            assertTrue(aFoot.isReachable() && dFoot.isReachable(), "Target " + target + " should be reachable");
            assertEquals(dFoot.getTotalTransitTimeSeconds(), aFoot.getTotalTransitTimeSeconds(),
                    "A* transit time to " + target + " must match Dijkstra");
            assertEquals(dFoot.getTotalDistanceMeters(), aFoot.getTotalDistanceMeters(), 0.001,
                    "A* distance to " + target + " must match Dijkstra");

            // Step-free trolley (elevators only)
            RouteSearchResult aStepFree = aStar.findShortestPath(graph, "VAULT_G01", target, true);
            RouteSearchResult dStepFree = dijkstra.findShortestPath(graph, "VAULT_G01", target, true);

            assertEquals(dStepFree.isReachable(), aStepFree.isReachable(), "Reachability for " + target + " must match Dijkstra");
            if (dStepFree.isReachable()) {
                assertEquals(dStepFree.getTotalTransitTimeSeconds(), aStepFree.getTotalTransitTimeSeconds(),
                        "A* step-free transit time to " + target + " must match Dijkstra");
                assertEquals(dStepFree.getTotalDistanceMeters(), aStepFree.getTotalDistanceMeters(), 0.001,
                        "A* step-free distance to " + target + " must match Dijkstra");
            }
        }
    }

    @Test
    @DisplayName("A* should explore no more nodes than Dijkstra, and strictly fewer for the farthest room (LO3 pruning proof)")
    void testExplorationEfficiency() {
        for (String target : TARGET_ROOMS) {
            RouteSearchResult aResult = aStar.findShortestPath(graph, "VAULT_G01", target, false);
            RouteSearchResult dResult = dijkstra.findShortestPath(graph, "VAULT_G01", target, false);

            assertTrue(aResult.getNodesExplored() <= dResult.getNodesExplored(),
                    "A* must not explore more nodes than Dijkstra for target " + target);
        }

        // The farthest room (opposite corner, top floor) gives the heuristic the most room to prune
        RouteSearchResult aFar = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_LAB3A", false);
        RouteSearchResult dFar = dijkstra.findShortestPath(graph, "VAULT_G01", "ROOM_LAB3A", false);

        assertTrue(aFar.getNodesExplored() < dFar.getNodesExplored(),
                "A* must explore strictly fewer nodes than Dijkstra for the farthest destination");
    }

    @Test
    @DisplayName("Should enforce step-free path (using elevator) when requiresStepFree is true")
    void testStepFreeRouting() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", true);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Step-free path to R101 should be reachable");

        assertTrue(result.getPathSequence().contains("ELEV_G"), "Step-free route MUST use Ground Elevator");
        assertTrue(result.getPathSequence().contains("ELEV_F1"), "Step-free route MUST use Floor 1 Elevator");
        assertFalse(result.getPathSequence().contains("STAIR_G_EAST"), "Step-free route must NOT contain stairs");
        assertFalse(result.getPathSequence().contains("STAIR_F1_EAST"), "Step-free route must NOT contain stairs");
    }

    @Test
    @DisplayName("Should route across multiple floors to Floor 3 Lab 3A")
    void testMultiFloorRouteToFloor3() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_LAB3A", false);

        assertNotNull(result);
        assertTrue(result.isReachable(), "Path to Lab 3A on Floor 3 should be reachable");
        assertEquals("VAULT_G01", result.getPathSequence().get(0));
        assertEquals("ROOM_LAB3A", result.getPathSequence().get(result.getPathSequence().size() - 1));
    }

    @Test
    @DisplayName("Should return unreachable result for non-existent node")
    void testNonExistentNode() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "NON_EXISTENT_ROOM", false);
        assertNotNull(result);
        assertFalse(result.isReachable(), "Non-existent destination must be marked unreachable");
        assertEquals(0.0, result.getTotalDistanceMeters());
    }

    @Test
    @DisplayName("Should handle source equals target trivially")
    void testSourceEqualsTarget() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "VAULT_G01", false);
        assertNotNull(result);
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalDistanceMeters());
        assertEquals(0, result.getTotalTransitTimeSeconds());
        assertEquals(1, result.getPathSequence().size());
    }
}
