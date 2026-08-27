package com.idss.task3.algorithm;

import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.model.VertexResult;

import java.util.*;

public class DSATUR {

    public static int run(ConflictGraph graph) {
        int n = graph.getExamRegistry().size();
        if (n == 0) return 0;
        
        int[][] matrix = graph.getConflictMatrix();
        List<VertexResult> vertices = graph.getVertices();
        
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
            int maxSat = -1;
            int maxDeg = -1;
            int bestNode = -1;
            
            for (int i = 0; i < n; i++) {
                if (!colored[i]) {
                    if (satDegree[i] > maxSat) {
                        maxSat = satDegree[i];
                        maxDeg = vertices.get(i).getDegree();
                        bestNode = i;
                    } else if (satDegree[i] == maxSat) {
                        if (vertices.get(i).getDegree() > maxDeg) {
                            maxDeg = vertices.get(i).getDegree();
                            bestNode = i;
                        }
                    }
                }
            }
            
            int c = 1;
            while (adjacentColors.get(bestNode).contains(c)) {
                c++;
            }
            
            color[bestNode] = c;
            colored[bestNode] = true;
            numColored++;
            
            for (int i = 0; i < n; i++) {
                if (matrix[bestNode][i] > 0 && !colored[i]) {
                    if (adjacentColors.get(i).add(c)) {
                        satDegree[i]++;
                    }
                }
            }
        }
        
        int maxColor = 0;
        for (int i = 0; i < n; i++) {
            vertices.get(i).setColor(color[i]);
            vertices.get(i).setSessionIndex(color[i]);
            if (color[i] > maxColor) {
                maxColor = color[i];
            }
        }
        
        return maxColor;
    }
}
