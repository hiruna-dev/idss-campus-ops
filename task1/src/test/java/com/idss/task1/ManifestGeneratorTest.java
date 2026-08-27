package com.idss.task1;

import com.idss.task1.algorithm.AStarEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.model.RouteSearchResult;
import com.idss.task1.model.TurnByTurnStep;
import com.idss.task1.service.ManifestGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ManifestGenerator (Student B Deliverable).
 * Validates turn-by-turn instruction generation against real A* resolved paths.
 */
class ManifestGeneratorTest {

    private BuildingGraph graph;
    private AStarEngine aStar;
    private ManifestGenerator manifestGenerator;

    @BeforeEach
    void setUp() throws IOException {
        graph = BuildingGraph.loadFromJson("data/input/input_building_graph.json");
        aStar = new AStarEngine();
        manifestGenerator = new ManifestGenerator();
    }

    @Test
    @DisplayName("Should generate one manifest step per traversed edge, with sequential step numbers")
    void testStepCountAndSequencing() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        assertEquals(result.getPathSequence().size() - 1, manifest.size());
        for (int i = 0; i < manifest.size(); i++) {
            assertEquals(i + 1, manifest.get(i).getStep());
            assertEquals(result.getPathSequence().get(i), manifest.get(i).getFrom());
            assertEquals(result.getPathSequence().get(i + 1), manifest.get(i).getTo());
        }
    }

    @Test
    @DisplayName("Manifest total time and distance must equal the RouteSearchResult totals")
    void testManifestTotalsMatchRouteResult() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_LAB3A", false);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        int totalTime = manifest.stream().mapToInt(TurnByTurnStep::getTimeSec).sum();
        double totalDistance = manifest.stream().mapToDouble(TurnByTurnStep::getDistanceMeters).sum();

        assertEquals(result.getTotalTransitTimeSeconds(), totalTime);
        assertEquals(result.getTotalDistanceMeters(), totalDistance, 0.001);
    }

    @Test
    @DisplayName("First step should describe exiting the vault")
    void testFirstStepIsVaultExit() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        assertFalse(manifest.isEmpty());
        assertTrue(manifest.get(0).getAction().startsWith("Exit"), "First step must describe exiting the vault");
    }

    @Test
    @DisplayName("Last step should describe delivery arrival at the exam room")
    void testLastStepIsDeliveryArrival() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        TurnByTurnStep lastStep = manifest.get(manifest.size() - 1);
        assertTrue(lastStep.getAction().contains("Arrive and deliver papers"), "Last step must describe delivery arrival");
        assertTrue(lastStep.getAction().contains("Lecture Theatre 101"), "Last step must name the destination room");
    }

    @Test
    @DisplayName("Step-free route should mention the Elevator transit action")
    void testStepFreeRouteMentionsElevator() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", true);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        boolean hasElevatorStep = manifest.stream().anyMatch(step -> step.getAction().contains("Elevator"));
        assertTrue(hasElevatorStep, "Step-free manifest must contain an elevator transit instruction");
    }

    @Test
    @DisplayName("Foot courier route via stairs should mention Ascend/Descend Stairwell action")
    void testFootCourierRouteMentionsStairs() {
        RouteSearchResult result = aStar.findShortestPath(graph, "VAULT_G01", "ROOM_R101", false);
        List<TurnByTurnStep> manifest = manifestGenerator.generate(graph, result.getPathSequence());

        boolean hasStairStep = manifest.stream().anyMatch(step ->
                step.getAction().startsWith("Ascend") || step.getAction().startsWith("Descend"));
        assertTrue(hasStairStep, "Foot courier manifest should use stairs and mention Ascend/Descend");
    }

    @Test
    @DisplayName("Should return an empty manifest for a null, empty, or single-node path")
    void testDegeneratePaths() {
        assertTrue(manifestGenerator.generate(graph, null).isEmpty());
        assertTrue(manifestGenerator.generate(graph, Collections.emptyList()).isEmpty());
        assertTrue(manifestGenerator.generate(graph, Collections.singletonList("VAULT_G01")).isEmpty());
    }
}
