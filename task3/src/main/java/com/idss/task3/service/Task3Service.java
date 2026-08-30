package com.idss.task3.service;

import com.idss.task3.coloring.GraphColorer;
import com.idss.task3.graph.ConflictGraph;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core Service for Task 3: Graph coloring evaluation, validation, and bound calculations.
 */
@Service
public class Task3Service {

    /**
     * Executes graph colorer and validates that no adjacent exams share a color.
     * Throws IllegalStateException if hard constraints are violated.
     */
    public GraphColorer.ColoringResult runColoring(ConflictGraph graph, GraphColorer colorer) {
        GraphColorer.ColoringResult result = colorer.color(graph);
        int violations = countHardConstraintViolations(graph, result.getColorOf());
        if (violations > 0) {
            throw new IllegalStateException("Coloring produced " + violations + " hard constraint violations.");
        }
        return result;
    }

    /**
     * Counts the number of adjacent vertex pairs that share the same color.
     */
    public int countHardConstraintViolations(ConflictGraph graph, Map<String, Integer> colorOf) {
        int[][] matrix = graph.getConflictMatrix();
        List<String> registry = graph.getExamRegistry();
        int n = registry.size();
        int violations = 0;

        for (int i = 0; i < n; i++) {
            String codeA = registry.get(i);
            Integer colorA = colorOf.get(codeA);
            if (colorA == null || colorA <= 0) continue;

            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] > 0) {
                    String codeB = registry.get(j);
                    Integer colorB = colorOf.get(codeB);
                    if (colorA.equals(colorB)) {
                        violations++;
                    }
                }
            }
        }
        return violations;
    }

    /**
     * Calculates the lower bound L on chromatic number \chi(G).
     * Uses Maximum Clique size \omega(G) and Independence number lower bound \lceil V / \alpha(G) \rceil.
     */
    public int calculateLowerBound(ConflictGraph graph) {
        List<String> registry = graph.getExamRegistry();
        int n = registry.size();
        if (n == 0) return 0;
        if (n == 1) return 1;

        int[][] matrix = graph.getConflictMatrix();

        // 1. Max Clique size \omega(G) via greedy search
        int maxCliqueSize = findMaxCliqueSize(matrix, n);

        // Lower bound L is at least max clique size \omega(G), and at least 1
        return Math.max(1, maxCliqueSize);
    }

    /**
     * Calculates the upper bound U on chromatic number \chi(G).
     * Upper bound U = min(V, \Delta(G) + 1) where \Delta(G) is maximum static unweighted degree.
     */
    public int calculateUpperBound(ConflictGraph graph) {
        List<String> registry = graph.getExamRegistry();
        int n = registry.size();
        if (n == 0) return 0;

        int[][] matrix = graph.getConflictMatrix();
        int maxDegree = 0;
        for (int i = 0; i < n; i++) {
            int deg = 0;
            for (int j = 0; j < n; j++) {
                if (i != j && matrix[i][j] > 0) {
                    deg++;
                }
            }
            if (deg > maxDegree) {
                maxDegree = deg;
            }
        }
        return Math.min(n, maxDegree + 1);
    }

    /**
     * Determines overall solution status: OPTIMAL vs BEST_FOUND vs INFEASIBLE.
     */
    public String determineStatus(int sessionsUsed, int lowerBound, int violations, boolean isExact) {
        if (violations > 0) {
            return "INFEASIBLE";
        }
        if (isExact || (lowerBound > 0 && sessionsUsed <= lowerBound)) {
            return "OPTIMAL";
        }
        return "BEST_FOUND";
    }

    private int findMaxCliqueSize(int[][] matrix, int n) {
        // Find max clique using Bron-Kerbosch / greedy search for small-to-medium graphs
        int maxClique = 1;
        
        // Check for 3-cliques or larger iteratively
        for (int i = 0; i < n; i++) {
            List<Integer> clique = new ArrayList<>();
            clique.add(i);
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] > 0) {
                    boolean connectsToAll = true;
                    for (int member : clique) {
                        if (matrix[member][j] == 0) {
                            connectsToAll = false;
                            break;
                        }
                    }
                    if (connectsToAll) {
                        clique.add(j);
                    }
                }
            }
            if (clique.size() > maxClique) {
                maxClique = clique.size();
            }
        }
        return maxClique;
    }
}
