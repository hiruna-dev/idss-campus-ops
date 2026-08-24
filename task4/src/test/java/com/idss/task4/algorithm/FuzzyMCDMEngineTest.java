package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.RoomScore;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMCDMEngineTest {

    private static final double DELTA = 1e-9;
    private static final double[] DEFAULT_WEIGHTS = {0.25, 0.25, 0.50};

    private Room room(boolean hasAc, int noise, int accessibility) {
        Room r = new Room();
        r.room_id = "R_TEST";
        r.room_name = "Test Room";
        r.floor = 1;
        r.capacity = 60;
        r.has_ac = hasAc;
        r.noise_level = noise;
        r.accessibility_score = accessibility;
        r.is_accessible = accessibility >= 4;
        return r;
    }

    @Test
    void withZeroFuzzinessMatchesPlainWeightedSum() {
        // has_ac=1.0, noise=4/5=0.8, accessibility=5/5=1.0
        // 0.25*1.0 + 0.25*0.8 + 0.5*1.0 = 0.95
        FuzzyMCDMEngine engine = new FuzzyMCDMEngine(DEFAULT_WEIGHTS, 0.0);
        List<RoomScore> results = engine.score(List.of(room(true, 4, 5)));

        assertEquals(1, results.size());
        assertEquals(0.95, results.get(0).getScore(), DELTA);
    }

    @Test
    void boundaryRatingsGetClippedByTheSpreadNearTheScaleCeiling() {
        // has_ac=1.0, noise=5/5=1.0, accessibility=5/5=1.0 -> SAW/zero-fuzziness score would be 1.0,
        // but with fuzziness=0.1 the upper bound clips at 1.0 (can't exceed "perfect"), making the
        // TFN asymmetric and pulling the centroid below the crisp value.
        FuzzyMCDMEngine engine = new FuzzyMCDMEngine(DEFAULT_WEIGHTS, 0.1);
        List<RoomScore> results = engine.score(List.of(room(true, 5, 5)));

        assertEquals(1, results.size());
        assertEquals(0.975, results.get(0).getScore(), DELTA);
        assertTrue(results.get(0).getScore() < 1.0, "clamped upper bound should discount a perfect-rating room");
    }

    @Test
    void emptyRoomListReturnsEmptyScores() {
        FuzzyMCDMEngine engine = new FuzzyMCDMEngine();
        assertTrue(engine.score(Collections.emptyList()).isEmpty());
        assertTrue(engine.score(null).isEmpty());
    }

    @Test
    void rejectsWeightsNotSummingToOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyMCDMEngine(new double[]{0.5, 0.5, 0.5}, 0.1));
    }

    @Test
    void rejectsWrongNumberOfWeights() {
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyMCDMEngine(new double[]{0.5, 0.5}, 0.1));
    }

    @Test
    void rejectsNegativeFuzziness() {
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyMCDMEngine(DEFAULT_WEIGHTS, -0.1));
    }
}
