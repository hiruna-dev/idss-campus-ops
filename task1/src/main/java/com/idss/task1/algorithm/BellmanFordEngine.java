package com.idss.task1.algorithm;

import com.idss.task1.model.Edge3D;
import com.idss.task1.model.RouteSearchResult;

import java.util.*;

/**
 * Theoretical Comparator: Bellman-Ford Shortest Path Algorithm (Student A Deliverable - LO1/LO2).
 *
 * <p>Iteratively relaxes all accessible edges |V| - 1 times. While slower than
 * Dijkstra and A* (O(V · E) vs O((V + E) log V)), this algorithm is implemented to
 * provide formal complexity contrast in Chapter 3 and Chapter 7 of the report.</p>
 *
 * <p>Time Complexity: O(V · E)<br>
 * Space Complexity: O(V)</p>
 */
public class BellmanFordEngine {

    public static final String ALGORITHM_NAME = "Bellman-Ford Algorithm (Theoretical Contrast)";

    public BellmanFordEngine() {
    }

    /**
     * Finds the shortest path from source to target using Bellman-Ford iteration.
     *
     * @param graph           the 3D building graph
     * @param sourceNodeId    starting landmark (e.g. "VAULT_G01")
     * @param targetNodeId    destination landmark (e.g. "ROOM_R101")
     * @param requiresStepFree whether staircase edges should be pruned
     * @return standardized {@link RouteSearchResult}
     */
    public RouteSearchResult findShortestPath(BuildingGraph graph, String sourceNodeId, String targetNodeId, boolean requiresStepFree) {
        if (graph == null || sourceNodeId == null || targetNodeId == null) {
            return RouteSearchResult.unreachable(ALGORITHM_NAME, sourceNodeId, targetNodeId, 0, 0.0);
        }

        if (!graph.containsNode(sourceNodeId) || !graph.containsNode(targetNodeId)) {
            return RouteSearchResult.unreachable(ALGORITHM_NAME, sourceNodeId, targetNodeId, 0, 0.0);
        }

        long startTimeNano = System.nanoTime();

        if (sourceNodeId.equals(targetNodeId)) {
            long elapsedNano = System.nanoTime() - startTimeNano;
            return new RouteSearchResult(
                    ALGORITHM_NAME, sourceNodeId, targetNodeId, true,
                    Collections.singletonList(sourceNodeId), 0.0, 0, 1, elapsedNano / 1_000_000.0
            );
        }

        Set<String> allNodes = graph.getAllNodeIds();
        int vertexCount = allNodes.size();

        Map<String, Double> timeCost = new HashMap<>();
        Map<String, Double> distanceMeters = new HashMap<>();
        Map<String, String> parentNode = new HashMap<>();

        // Initialize all distances to infinity
        for (String nodeId : allNodes) {
            timeCost.put(nodeId, Double.POSITIVE_INFINITY);
            distanceMeters.put(nodeId, Double.POSITIVE_INFINITY);
        }

        timeCost.put(sourceNodeId, 0.0);
        distanceMeters.put(sourceNodeId, 0.0);

        // Pre-collect all accessible directed edges
        List<GraphEdge> activeEdges = new ArrayList<>();
        for (String u : allNodes) {
            List<Edge3D> edges = graph.getAccessibleNeighbors(u, requiresStepFree);
            for (Edge3D edge : edges) {
                activeEdges.add(new GraphEdge(u, edge.getTargetNode(), edge.getBaseTransitTimeSeconds(), edge.getDistanceMeters()));
            }
        }

        int passesCompleted = 0;
        int totalRelaxations = 0;

        // Perform |V| - 1 iterations
        for (int i = 1; i < vertexCount; i++) {
            boolean updated = false;
            passesCompleted++;

            for (GraphEdge edge : activeEdges) {
                totalRelaxations++;
                double uTime = timeCost.get(edge.source);
                if (uTime == Double.POSITIVE_INFINITY) {
                    continue;
                }

                double newTime = uTime + edge.transitTime;
                if (newTime < timeCost.get(edge.target)) {
                    timeCost.put(edge.target, newTime);
                    distanceMeters.put(edge.target, distanceMeters.get(edge.source) + edge.distance);
                    parentNode.put(edge.target, edge.source);
                    updated = true;
                }
            }

            // Early termination optimization if no edge distances changed in this pass
            if (!updated) {
                break;
            }
        }

        long elapsedNano = System.nanoTime() - startTimeNano;
        double executionTimeMs = elapsedNano / 1_000_000.0;

        if (timeCost.get(targetNodeId) == Double.POSITIVE_INFINITY) {
            return RouteSearchResult.unreachable(ALGORITHM_NAME, sourceNodeId, targetNodeId, totalRelaxations, executionTimeMs);
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String curr = targetNodeId;
        while (curr != null) {
            path.add(curr);
            curr = parentNode.get(curr);
        }
        Collections.reverse(path);

        double totalDist = distanceMeters.getOrDefault(targetNodeId, 0.0);
        int totalTime = (int) Math.round(timeCost.getOrDefault(targetNodeId, 0.0));

        return new RouteSearchResult(
                ALGORITHM_NAME, sourceNodeId, targetNodeId, true,
                path, totalDist, totalTime, totalRelaxations, executionTimeMs
        );
    }

    private static class GraphEdge {
        final String source;
        final String target;
        final double transitTime;
        final double distance;

        GraphEdge(String source, String target, double transitTime, double distance) {
            this.source = source;
            this.target = target;
            this.transitTime = transitTime;
            this.distance = distance;
        }
    }
}
