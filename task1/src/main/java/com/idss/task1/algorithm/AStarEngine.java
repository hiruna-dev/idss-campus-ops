package com.idss.task1.algorithm;

import com.idss.task1.model.Coordinates3D;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.RouteSearchResult;

import java.util.*;

/**
 * Primary Multi-Floor Route Optimizer: A* Search with 3D Euclidean Heuristic
 * (Student B Deliverable - LO1/LO2).
 *
 * <p>Guides expansion toward the destination using f(n) = g(n) + h(n), where g(n) is the
 * accumulated transit time in seconds from the source vault (matching the objective used
 * by {@link DijkstraEngine} and {@link BellmanFordEngine}), and h(n) is an admissible
 * estimate of the remaining transit time: the 3D Euclidean distance between node
 * coordinates, penalized by beta = 3.5 on the vertical (floor) axis, divided by
 * {@link #MAX_WALKING_SPEED_MPS}.</p>
 *
 * <p>{@link #MAX_WALKING_SPEED_MPS} is set above the fastest single edge modeled in the
 * campus graph (elevator-bay corridor segments run at 1.5 m/s), and straight-line distance
 * between two nodes never exceeds the physical corridor distance between them. Both
 * properties together keep h(n) consistent (h(u) &le; cost(u,v) + h(v)), so A* with
 * early termination on popping the goal is guaranteed to match Dijkstra's optimal
 * distance/time exactly, while exploring fewer nodes.</p>
 *
 * <p>Backed by the custom {@link IndexedHeap} open-set priority queue, giving strict
 * O(log V) decreaseKey instead of the O(V) cost incurred by removing and
 * re-inserting into {@code java.util.PriorityQueue}.</p>
 *
 * <p>Time Complexity: O((V + E) log V) worst-case, &Theta;(b^d) average-case<br>
 * Space Complexity: O(V + E)</p>
 */
public class AStarEngine {

    public static final String ALGORITHM_NAME = "A* Search Algorithm (3D Euclidean & Floor Penalty Heuristic)";

    /**
     * Conservative upper bound (m/s) on modeled traversal speed, used to convert the
     * Euclidean heuristic distance into a time estimate. Kept above the fastest single
     * edge in the campus graph so h(n) never overestimates the true remaining cost.
     */
    public static final double MAX_WALKING_SPEED_MPS = 2.0;

    public AStarEngine() {
    }

    /**
     * Finds the optimal shortest-time path from source to target using A* search.
     *
     * @param graph            the 3D building graph
     * @param sourceNodeId     starting landmark (e.g. "VAULT_G01")
     * @param targetNodeId     destination landmark (e.g. "ROOM_R101")
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

        // g-score: accumulated transit time in seconds (primary objective, matches Dijkstra/Bellman-Ford)
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> distanceMeters = new HashMap<>();
        Map<String, String> parentNode = new HashMap<>();
        Set<String> settled = new HashSet<>();

        IndexedHeap openSet = new IndexedHeap();

        gScore.put(sourceNodeId, 0.0);
        distanceMeters.put(sourceNodeId, 0.0);
        openSet.insert(sourceNodeId, heuristic(graph, sourceNodeId, targetNodeId));

        int nodesExplored = 0;
        boolean targetFound = false;

        while (!openSet.isEmpty()) {
            String u = openSet.extractMin();

            // Skip stale entries: a node can only be settled once its minimal f(n) is popped
            if (settled.contains(u)) {
                continue;
            }
            settled.add(u);
            nodesExplored++;

            // Early exit once target node is settled with its optimal g-score
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

                double tentativeG = gScore.get(u) + edge.getBaseTransitTimeSeconds();
                double bestG = gScore.getOrDefault(v, Double.POSITIVE_INFINITY);

                if (tentativeG < bestG) {
                    gScore.put(v, tentativeG);
                    distanceMeters.put(v, distanceMeters.get(u) + edge.getDistanceMeters());
                    parentNode.put(v, u);

                    double fScore = tentativeG + heuristic(graph, v, targetNodeId);
                    if (openSet.contains(v)) {
                        openSet.decreaseKey(v, fScore);
                    } else {
                        openSet.insert(v, fScore);
                    }
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
        int totalTime = (int) Math.round(gScore.getOrDefault(targetNodeId, 0.0));

        return new RouteSearchResult(
                ALGORITHM_NAME, sourceNodeId, targetNodeId, true,
                path, totalDist, totalTime, nodesExplored, executionTimeMs
        );
    }

    /**
     * Admissible 3D Euclidean heuristic with vertical floor-transition penalty (beta = 3.5),
     * converted to a time estimate via {@link #MAX_WALKING_SPEED_MPS}. Returns 0.0 (no guidance,
     * degrades gracefully to Dijkstra-equivalent behavior for that node) if coordinates are missing.
     */
    private double heuristic(BuildingGraph graph, String nodeId, String targetNodeId) {
        Coordinates3D from = graph.getCoordinates(nodeId);
        Coordinates3D to = graph.getCoordinates(targetNodeId);
        if (from == null || to == null) {
            return 0.0;
        }
        return from.distanceTo(to, Coordinates3D.DEFAULT_BETA_FLOOR_PENALTY) / MAX_WALKING_SPEED_MPS;
    }
}
