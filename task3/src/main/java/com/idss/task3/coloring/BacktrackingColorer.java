package com.idss.task3.coloring;

import com.idss.task3.graph.ConflictGraph;

import java.util.*;

/**
 * Backtracking Graph Colorer implementation.
 * 
 * Algorithm per Spec Section 4.3:
 * 1. Exact search algorithm capped at 15 vertices.
 * 2. Increments k from lower bound upward.
 * 3. Uses recursive backtracking to attempt k-coloring.
 * 4. Returns the first k for which a valid coloring exists (exact chromatic number).
 */
public class BacktrackingColorer implements GraphColorer {
    private static final int MAX_VERTICES_CAP = 15;

    @Override
    public ColoringResult color(ConflictGraph graph) {
        List<String> registry = graph.getExamRegistry();
        int n = registry.size();
        if (n == 0) {
            return new ColoringResult(Collections.emptyMap(), 0);
        }

        if (n > MAX_VERTICES_CAP) {
            throw new IllegalArgumentException(
                "Backtracking colorer is capped at " + MAX_VERTICES_CAP + " vertices (graph has " + n + " vertices)."
            );
        }

        int[][] matrix = graph.getConflictMatrix();
        int[] color = new int[n];

        // Increment k from 1 to n until exact chromatic number k is found
        for (int k = 1; k <= n; k++) {
            Arrays.fill(color, -1);
            if (solve(0, color, k, matrix, n)) {
                Map<String, Integer> colorOf = new HashMap<>();
                for (int i = 0; i < n; i++) {
                    colorOf.put(registry.get(i), color[i]);
                }
                return new ColoringResult(colorOf, k);
            }
        }

        // Fallback (should not be reached if graph is valid)
        Map<String, Integer> colorOf = new HashMap<>();
        for (int i = 0; i < n; i++) {
            colorOf.put(registry.get(i), i + 1);
        }
        return new ColoringResult(colorOf, n);
    }

    private boolean solve(int node, int[] color, int maxColors, int[][] matrix, int n) {
        if (node == n) {
            return true;
        }

        for (int c = 1; c <= maxColors; c++) {
            if (isValid(node, c, color, matrix, n)) {
                color[node] = c;
                if (solve(node + 1, color, maxColors, matrix, n)) {
                    return true;
                }
                color[node] = -1;
            }
        }
        return false;
    }

    private boolean isValid(int node, int c, int[] color, int[][] matrix, int n) {
        for (int i = 0; i < n; i++) {
            if (matrix[node][i] > 0 && color[i] == c) {
                return false;
            }
        }
        return true;
    }
}
