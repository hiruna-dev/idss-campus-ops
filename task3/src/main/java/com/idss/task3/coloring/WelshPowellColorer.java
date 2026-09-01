package com.idss.task3.coloring;

import com.idss.task3.graph.ConflictGraph;

import java.util.*;

/**
 * Welsh-Powell Graph Colorer implementation.
 * 
 * Algorithm per Spec Section 4.2:
 * 1. Calculate static unweighted degree for each vertex.
 * 2. Sort vertices in descending order of static degree (tie-break alphabetically).
 * 3. Assign colors greedily in sorted order using lowest available valid color.
 */
public class WelshPowellColorer implements GraphColorer {

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

        // Vertices to sort
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }

        order.sort((a, b) -> {
            int cmp = Integer.compare(staticDegree[b], staticDegree[a]); // Descending degree
            if (cmp != 0) return cmp;
            return registry.get(a).compareTo(registry.get(b)); // Alphabetical tie-break
        });

        int[] color = new int[n];
        Arrays.fill(color, -1);

        for (int v : order) {
            Set<Integer> usedNeighborColors = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if (matrix[v][j] > 0 && color[j] != -1) {
                    usedNeighborColors.add(color[j]);
                }
            }

            int c = 1;
            while (usedNeighborColors.contains(c)) {
                c++;
            }
            color[v] = c;
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
