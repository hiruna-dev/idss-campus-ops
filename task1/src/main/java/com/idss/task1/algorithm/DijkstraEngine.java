package com.idss.task1.algorithm;

import com.idss.task1.model.Edge3D;
import com.idss.task1.model.RouteSearchResult;

import java.util.*;

/**
 * Baseline Dijkstra's Shortest Path Algorithm (Student A Deliverable - LO1/LO2).
 *
 * <p>Explores the 3D multi-floor campus graph outward radially based strictly on
 * accumulated path cost g(n). Serves as the authoritative ground-truth comparator
 * during empirical benchmarking against A* Search.</p>
 *
 * <p>Time Complexity: O((V + E) log V)<br>
 * Space Complexity: O(V + E)</p>
 */
public class DijkstraEngine {

    public static final String ALGORITHM_NAME = "Dijkstra's Algorithm (Baseline)";

    public DijkstraEngine() {
    }

    /**
     * Finds the optimal shortest path from source to target using Dijkstra's algorithm.
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

        // If source is target, return instant 0-cost path
        if (sourceNodeId.equals(targetNodeId)) {
            long elapsedNano = System.nanoTime() - startTimeNano;
            return new RouteSearchResult(
                    ALGORITHM_NAME, sourceNodeId, targetNodeId, true,
                    Collections.singletonList(sourceNodeId), 0.0, 0, 1, elapsedNano / 1_000_000.0
            );
        }

        // Distance tables: g-score in seconds (primary objective) and meters
        Map<String, Double> timeCost = new HashMap<>();
        Map<String, Double> distanceMeters = new HashMap<>();
        Map<String, String> parentNode = new HashMap<>();

        // Priority Queue ordered by accumulated transit time g(u)
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.cost));
        Set<String> settled = new HashSet<>();

        // Initialize source
        timeCost.put(sourceNodeId, 0.0);
        distanceMeters.put(sourceNodeId, 0.0);
        pq.add(new NodeDistance(sourceNodeId, 0.0));

        int nodesExplored = 0;
        boolean targetFound = false;

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.nodeId;

            // Skip if already settled (since Java PriorityQueue contains duplicate stale entries)
            if (settled.contains(u)) {
                continue;
            }

            settled.add(u);
            nodesExplored++;

            // Early exit once target node is settled
            if (u.equals(targetNodeId)) {
                targetFound = true;
                break;
            }

            // Expand accessible outgoing edges with dynamic step-free filter
            List<Edge3D> neighbors = graph.getAccessibleNeighbors(u, requiresStepFree);
            for (Edge3D edge : neighbors) {
                String v = edge.getTargetNode();
                if (settled.contains(v)) {
                    continue;
                }

                double edgeTime = edge.getBaseTransitTimeSeconds();
                double edgeDist = edge.getDistanceMeters();

                double newTime = timeCost.get(u) + edgeTime;
                double oldTime = timeCost.getOrDefault(v, Double.POSITIVE_INFINITY);

                if (newTime < oldTime) {
                    timeCost.put(v, newTime);
                    distanceMeters.put(v, distanceMeters.get(u) + edgeDist);
                    parentNode.put(v, u);
                    pq.add(new NodeDistance(v, newTime));
                }
            }
        }

        long elapsedNano = System.nanoTime() - startTimeNano;
        double executionTimeMs = elapsedNano / 1_000_000.0;

        if (!targetFound) {
            return RouteSearchResult.unreachable(ALGORITHM_NAME, sourceNodeId, targetNodeId, nodesExplored, executionTimeMs);
        }

        // Reconstruct path sequence by backtracking parent pointers
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
                path, totalDist, totalTime, nodesExplored, executionTimeMs
        );
    }

    /**
     * Helper record for the PriorityQueue entries.
     */
    private static class NodeDistance {
        final String nodeId;
        final double cost;

        NodeDistance(String nodeId, double cost) {
            this.nodeId = nodeId;
            this.cost = cost;
        }
    }
}
