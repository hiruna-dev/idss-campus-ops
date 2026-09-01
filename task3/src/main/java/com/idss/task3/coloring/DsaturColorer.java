package com.idss.task3.coloring;

import com.idss.task3.graph.ConflictGraph;

import java.util.*;

/**
 * DSATUR (Degree Saturation) Graph Colorer implementation.
 * 
 * Algorithm per Spec Section 4.1:
 * 1. Track saturation degree (distinct colors assigned to colored neighbors) and static unweighted degree.
 * 2. Select uncolored vertex with max saturation degree.
 * 3. Tie-break on max static unweighted degree.
 * 4. Tie-break deterministically using course code alphabetical order.
 * 5. Assign smallest available positive integer color (1, 2, ...).
 */
public class DsaturColorer implements GraphColorer {

    @Override
    public ColoringResult color(ConflictGraph graph) {
        List<String> registry = graph.getExamRegistry();
        int n = registry.size();
        if (n == 0) {
            return new ColoringResult(Collections.emptyMap(), 0);
        }

        int[][] matrix = graph.getConflictMatrix();
        
        // Static unweighted degrees
        int[] staticDegree = new int[n];
        for (int i = 0; i < n; i++) {
            int deg = 0;
            for (int j = 0; j < n; j++) {
                if (i != j && matrix[i][j] > 0) {
                    deg++;
                }
            }
            staticDegree[i] = deg;
        }

        int[] satDegree = new int[n];
        int[] color = new int[n];
        Arrays.fill(color, -1);

        List<Set<Integer>> adjacentColors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjacentColors.add(new HashSet<>());
        }

        boolean[] colored = new boolean[n];
        int numColored = 0;

        while (numColored < n) {
            int bestNode = -1;
            int maxSat = -1;
            int maxDeg = -1;
            String bestCode = null;

            for (int i = 0; i < n; i++) {
                if (!colored[i]) {
                    int sat = satDegree[i];
                    int deg = staticDegree[i];
                    String code = registry.get(i);

                    if (sat > maxSat) {
                        maxSat = sat;
                        maxDeg = deg;
                        bestCode = code;
                        bestNode = i;
                    } else if (sat == maxSat) {
                        if (deg > maxDeg) {
                            maxDeg = deg;
                            bestCode = code;
                            bestNode = i;
                        } else if (deg == maxDeg) {
                            // Deterministic tie-break: alphabetical course code
                            if (bestCode == null || code.compareTo(bestCode) < 0) {
                                bestCode = code;
                                bestNode = i;
                            }
                        }
                    }
                }
            }

            if (bestNode == -1) break;

            // Smallest available color
            int c = 1;
            while (adjacentColors.get(bestNode).contains(c)) {
                c++;
            }

            color[bestNode] = c;
            colored[bestNode] = true;
            numColored++;

            // Update saturation degree of uncolored neighbors
            for (int j = 0; j < n; j++) {
                if (matrix[bestNode][j] > 0 && !colored[j]) {
                    if (adjacentColors.get(j).add(c)) {
                        satDegree[j]++;
                    }
                }
            }
        }

        Map<String, Integer> colorOf = new HashMap<>();
        int maxColor = 0;
        for (int i = 0; i < n; i++) {
            colorOf.put(registry.get(i), color[i]);
            if (color[i] > maxColor) {
                maxColor = color[i];
            }
        }

        return new ColoringResult(colorOf, maxColor);
    }
}
