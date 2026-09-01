package com.idss.task2;

import com.idss.task2.algorithm.Hungarian;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HungarianTest {

    private final Hungarian hungarian = new Hungarian();

    @Test
    void simple3x3KnownMatrix() {
        int[][] cost = {
            {4, 1, 3},
            {2, 0, 5},
            {3, 2, 2}
        };
        int[] assignment = hungarian.solve(cost);
        assertEquals(3, assignment.length);
        int total = totalCost(cost, assignment);
        assertEquals(5, total);
        assertEquals(1, assignment[0]);
        assertEquals(0, assignment[1]);
        assertEquals(2, assignment[2]);
    }

    @Test
    void forbiddenAssignmentAvoided() {
        int[][] cost = {
            {10000, 1,    100,  100},
            {2,     100,  100,  100},
            {100,   100,  1,    100},
            {100,   100,  100,  1}
        };
        int[] assignment = hungarian.solve(cost);
        assertEquals(4, assignment.length);
        assertNotEquals(0, assignment[0]);
        int total = totalCost(cost, assignment);
        assertEquals(5, total);
    }

    @Test
    void nonSquareMoreSlotsThanRows() {
        int[][] cost = {
            {1, 2, 3, 4},
            {5, 6, 7, 8}
        };
        int[] assignment = hungarian.solve(cost);
        assertEquals(2, assignment.length);
        assertEquals(0, assignment[0]);
        assertEquals(1, assignment[1]);
        int total = totalCost(cost, assignment);
        assertEquals(7, total);
    }

    @Test
    void identityMatrixDiagonalAssignment() {
        int[][] cost = {
            {0,   100, 100},
            {100, 0,   100},
            {100, 100, 0}
        };
        int[] assignment = hungarian.solve(cost);
        assertEquals(3, assignment.length);
        assertEquals(0, assignment[0]);
        assertEquals(1, assignment[1]);
        assertEquals(2, assignment[2]);
        int total = totalCost(cost, assignment);
        assertEquals(0, total);
    }

    @Test
    void emptyMatrixReturnsEmpty() {
        int[] assignment = hungarian.solve(new int[0][]);
        assertEquals(0, assignment.length);
    }

    private int totalCost(int[][] cost, int[] assignment) {
        int total = 0;
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] >= 0 && assignment[i] < cost[i].length) {
                total += cost[i][assignment[i]];
            }
        }
        return total;
    }
}
