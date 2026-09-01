package com.idss.task4.algorithm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TFNTest {

    private static final double DELTA = 1e-9;

    @Test
    void crispValueCentroidsToItself() {
        assertEquals(0.6, TFN.crisp(0.6).centroid(), DELTA);
    }

    @Test
    void symmetricTfnCentroidsToItsMid() {
        TFN t = new TFN(0.3, 0.5, 0.7);
        assertEquals(0.5, t.centroid(), DELTA);
    }

    @Test
    void addIsComponentWise() {
        TFN a = new TFN(0.1, 0.2, 0.3);
        TFN b = new TFN(0.05, 0.1, 0.15);
        TFN sum = a.add(b);

        assertEquals(0.15, sum.low, DELTA);
        assertEquals(0.30, sum.mid, DELTA);
        assertEquals(0.45, sum.high, DELTA);
    }

    @Test
    void scaleMultipliesAllThreeComponents() {
        TFN t = new TFN(0.2, 0.4, 0.6);
        TFN scaled = t.scale(0.5);

        assertEquals(0.10, scaled.low, DELTA);
        assertEquals(0.20, scaled.mid, DELTA);
        assertEquals(0.30, scaled.high, DELTA);
    }

    @Test
    void rejectsOutOfOrderComponents() {
        assertThrows(IllegalArgumentException.class, () -> new TFN(0.5, 0.3, 0.7));
        assertThrows(IllegalArgumentException.class, () -> new TFN(0.1, 0.6, 0.5));
    }

    @Test
    void rejectsNegativeScale() {
        TFN t = TFN.crisp(0.5);
        assertThrows(IllegalArgumentException.class, () -> t.scale(-1.0));
    }
}
