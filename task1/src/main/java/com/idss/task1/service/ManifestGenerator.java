package com.idss.task1.service;

import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;
import com.idss.task1.model.TurnByTurnStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a raw node-id path sequence (from {@code AStarEngine} / {@code DijkstraEngine})
 * into a human-readable turn-by-turn courier manifest (Student B Deliverable).
 *
 * <p>Distance and time are carried on the dedicated {@link TurnByTurnStep} fields rather
 * than embedded in the action text, so the generated {@code action} string stays purely
 * descriptive.</p>
 */
public class ManifestGenerator {

    private static final String NODE_TYPE_VAULT = "VAULT";
    private static final String NODE_TYPE_EXAM_ROOM = "EXAM_ROOM";
    private static final String EDGE_TYPE_ELEVATOR_SHAFT = "ELEVATOR_SHAFT";
    private static final String EDGE_TYPE_STAIRCASE = "STAIRCASE";

    public ManifestGenerator() {
    }

    /**
     * Builds the ordered list of turn-by-turn instructions for a resolved path sequence.
     *
     * @param graph        the 3D building graph (for node metadata + edge lookup)
     * @param pathSequence ordered node IDs from source vault to destination room
     * @return list of {@link TurnByTurnStep}, one per traversed edge; empty if the path
     *         has fewer than 2 nodes
     */
    public List<TurnByTurnStep> generate(BuildingGraph graph, List<String> pathSequence) {
        List<TurnByTurnStep> manifest = new ArrayList<>();
        if (graph == null || pathSequence == null || pathSequence.size() < 2) {
            return manifest;
        }

        for (int i = 0; i < pathSequence.size() - 1; i++) {
            String fromId = pathSequence.get(i);
            String toId = pathSequence.get(i + 1);

            Node3D fromNode = graph.getNode(fromId);
            Node3D toNode = graph.getNode(toId);
            Edge3D edge = findEdge(graph, fromId, toId);

            if (fromNode == null || toNode == null || edge == null) {
                continue;
            }

            boolean isFirstStep = (i == 0);
            boolean isLastStep = (i == pathSequence.size() - 2);

            String action = buildAction(fromNode, toNode, edge, isFirstStep, isLastStep);
            manifest.add(new TurnByTurnStep(i + 1, fromId, toId, action, edge.getBaseTransitTimeSeconds(), edge.getDistanceMeters()));
        }

        return manifest;
    }

    /**
     * Locates the directed edge actually traversed between two consecutive path nodes.
     * Searches ALL outgoing edges (not the step-free-filtered subset) since the path has
     * already been resolved by the search engine.
     */
    private Edge3D findEdge(BuildingGraph graph, String fromId, String toId) {
        for (Edge3D edge : graph.getOutgoingEdges(fromId)) {
            if (toId.equals(edge.getTargetNode())) {
                return edge;
            }
        }
        return null;
    }

    private String buildAction(Node3D fromNode, Node3D toNode, Edge3D edge, boolean isFirstStep, boolean isLastStep) {
        if (isLastStep && NODE_TYPE_EXAM_ROOM.equals(toNode.getNodeType())) {
            return "Arrive and deliver papers at " + toNode.getNodeName();
        }
        if (isFirstStep && NODE_TYPE_VAULT.equals(fromNode.getNodeType())) {
            return "Exit " + fromNode.getNodeName() + " via " + toNode.getNodeName();
        }
        if (EDGE_TYPE_ELEVATOR_SHAFT.equals(edge.getEdgeType())) {
            return "Take Elevator to Floor " + toNode.getFloor();
        }
        if (EDGE_TYPE_STAIRCASE.equals(edge.getEdgeType())) {
            String direction = toNode.getFloor() > fromNode.getFloor() ? "Ascend" : "Descend";
            return direction + " " + fromNode.getNodeName() + " to Floor " + toNode.getFloor();
        }
        return "Proceed to " + toNode.getNodeName();
    }
}
