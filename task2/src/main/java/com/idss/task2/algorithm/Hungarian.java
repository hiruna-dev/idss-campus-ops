package com.idss.task2.algorithm;

import java.util.Arrays;

public class Hungarian {

    private static final int DUMMY_ROW_COST = 1_000_000;

    public int[] solve(int[][] cost) {
        if (cost.length == 0) {
            return new int[0];
        }
        int rows = cost.length;
        int cols = cost[0].length;
        int[][] square = squareMatrix(cost, rows, cols);
        int n = square.length;

        int[] u = new int[n + 1];
        int[] v = new int[n + 1];
        int[] matchRowByCol = new int[n + 1];
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            matchRowByCol[0] = i;
            int j0 = 0;
            int[] slack = new int[n + 1];
            boolean[] visitedCol = new boolean[n + 1];
            Arrays.fill(slack, Integer.MAX_VALUE);

            do {
                visitedCol[j0] = true;
                int currentRow = matchRowByCol[j0];
                int delta = Integer.MAX_VALUE;
                int nextCol = -1;

                for (int j = 1; j <= n; j++) {
                    if (!visitedCol[j]) {
                        int reduced = square[currentRow - 1][j - 1] - u[currentRow] - v[j];
                        if (reduced < slack[j]) {
                            slack[j] = reduced;
                            way[j] = j0;
                        }
                        if (slack[j] < delta) {
                            delta = slack[j];
                            nextCol = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (visitedCol[j]) {
                        u[matchRowByCol[j]] += delta;
                        v[j] -= delta;
                    } else {
                        slack[j] -= delta;
                    }
                }

                j0 = nextCol;
            } while (matchRowByCol[j0] != 0);

            do {
                int prevCol = way[j0];
                matchRowByCol[j0] = matchRowByCol[prevCol];
                j0 = prevCol;
            } while (j0 != 0);
        }

        int[] assignment = new int[n];
        for (int j = 1; j <= n; j++) {
            assignment[matchRowByCol[j] - 1] = j - 1;
        }

        int[] result = new int[rows];
        for (int i = 0; i < rows; i++) {
            result[i] = assignment[i] < cols ? assignment[i] : -1;
        }
        return result;
    }

    private int[][] squareMatrix(int[][] cost, int rows, int cols) {
        int n = Math.max(rows, cols);
        int[][] square = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < rows && j < cols) {
                    square[i][j] = cost[i][j];
                } else if (i >= rows) {
                    square[i][j] = DUMMY_ROW_COST;
                } else {
                    square[i][j] = 0;
                }
            }
        }
        return square;
    }
}
