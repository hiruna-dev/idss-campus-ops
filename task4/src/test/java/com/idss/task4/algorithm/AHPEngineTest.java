package com.idss.task4.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates AHPEngine against the hand-derived weights and Consistency
 * Ratio in task_4_plan.md Section 6.
 */
class AHPEngineTest {

    private static final double DELTA = 1e-9;

    @Test
    void derivesTheDocumentedWeightsFromTheDefaultMatrix() {
        AHPEngine engine = new AHPEngine();
        double[] weights = engine.getWeights();

        assertEquals(0.25, weights[0], DELTA, "AC comfort weight");
        assertEquals(0.25, weights[1], DELTA, "Noise weight");
        assertEquals(0.50, weights[2], DELTA, "Accessibility weight");
    }

    @Test
    void weightsSumToOne() {
        AHPEngine engine = new AHPEngine();
        double sum = 0.0;
        for (double w : engine.getWeights()) {
            sum += w;
        }
        assertEquals(1.0, sum, DELTA);
    }

    @Test
    void defaultMatrixIsPerfectlyConsistent() {
        AHPEngine engine = new AHPEngine();

        assertEquals(0.0, engine.getConsistencyRatio(), DELTA);
        assertTrue(engine.isConsistent());
    }

    @Test
    void inconsistentJudgmentsAreFlagged() {
        // A deliberately contradictory matrix: A >> B, B >> C, but C >> A.
        double[][] contradictory = {
                {1.0, 9.0, 1.0 / 9.0},
                {1.0 / 9.0, 1.0, 9.0},
                {9.0, 1.0 / 9.0, 1.0}
        };
        AHPEngine engine = new AHPEngine(contradictory);

        assertTrue(engine.getConsistencyRatio() > 0.1,
                "Cyclic 9/(1/9) judgments should fail the CR < 0.1 threshold");
        assertTrue(!engine.isConsistent());
    }

    @Test
    void rejectsNonSquareMatrix() {
        double[][] notSquare = {
                {1.0, 2.0},
                {0.5, 1.0, 3.0}
        };
        try {
            new AHPEngine(notSquare);
            assertTrue(false, "expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
