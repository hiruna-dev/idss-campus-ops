package com.idss.task1;

import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.model.Coordinates3D;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for BuildingGraph (Student A Deliverable).
 * Validates 3D spatial adjacency list construction and step-free constraint filtering.
 */
class BuildingGraphTest {

    private BuildingGraph graph;

    @BeforeEach
    void setUp() throws IOException {
        graph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
    }

    @Test
    @DisplayName("Should successfully load and construct graph from JSON")
    void testGraphLoading() {
        assertNotNull(graph, "BuildingGraph should not be null");
        assertTrue(graph.getVertexCount() > 0, "Vertex count should be > 0");
        assertTrue(graph.getEdgeCount() > 0, "Edge count should be > 0");

        // Verify key landmarks exist
        assertTrue(graph.containsNode("VAULT_G01"), "Vault node should exist");
        assertTrue(graph.containsNode("ROOM_R101"), "Room R101 should exist");
        assertTrue(graph.containsNode("ELEV_G"), "Elevator G should exist");
        assertTrue(graph.containsNode("STAIR_G_EAST"), "Stairs G East should exist");
    }

    @Test
    @DisplayName("Should correctly filter accessible edges when step-free access is required")
    void testStepFreeFiltering() {
        // When step-free is FALSE: both stairs and hallways are accessible from STAIR_G_EAST
        List<Edge3D> allEdges = graph.getAccessibleNeighbors("STAIR_G_EAST", false);
        boolean containsStairs = allEdges.stream().anyMatch(e -> !e.isStepFree());
        assertTrue(containsStairs, "All edges should include staircases when step-free is not required");

        // When step-free is TRUE: staircase edges should be pruned
        List<Edge3D> stepFreeEdges = graph.getAccessibleNeighbors("STAIR_G_EAST", true);
        boolean containsAnyStairsWhenStepFree = stepFreeEdges.stream().anyMatch(e -> !e.isStepFree());
        assertFalse(containsAnyStairsWhenStepFree, "Staircase edges must be pruned when step-free is required");
    }

    @Test
    @DisplayName("Should accurately calculate 3D Euclidean distance between floors with beta=3.5 penalty")
    void test3DDistanceCalculation() {
        Coordinates3D vaultCoords = graph.getCoordinates("VAULT_G01");
        Coordinates3D roomR101Coords = graph.getCoordinates("ROOM_R101");

        assertNotNull(vaultCoords);
        assertNotNull(roomR101Coords);

        // Vault is floor 0 (z=0), R101 is floor 1 (z=4)
        double distance = vaultCoords.distanceTo(roomR101Coords, 3.5);
        assertTrue(distance > 0, "3D distance should be positive");

        // Vertical penalty check: (3.5 * (4 - 0))^2 = 14^2 = 196
        double dx = vaultCoords.getX() - roomR101Coords.getX();
        double dy = vaultCoords.getY() - roomR101Coords.getY();
        double dz = 3.5 * (vaultCoords.getZ() - roomR101Coords.getZ());
        double expected = Math.sqrt(dx * dx + dy * dy + dz * dz);

        assertEquals(expected, distance, 0.001, "Distance should match 3D formula with beta=3.5");
    }

    @Test
    @DisplayName("Should retrieve node metadata accurately")
    void testNodeMetadata() {
        Node3D vault = graph.getNode("VAULT_G01");
        assertNotNull(vault);
        assertEquals(0, vault.getFloor());
        assertEquals("VAULT", vault.getNodeType());
        assertTrue(vault.isAccessible());

        Node3D r301 = graph.getNode("ROOM_R301");
        assertNotNull(r301);
        assertEquals(3, r301.getFloor());
        assertEquals("EXAM_ROOM", r301.getNodeType());
    }
}
