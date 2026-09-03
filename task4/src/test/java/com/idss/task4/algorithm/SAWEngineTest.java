package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.RoomScore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates SAWEngine's weighted-sum scoring against hand-calculated
 * examples (task_4_plan.md Section 8.1).
 */
class SAWEngineTest {

    private static final double DELTA = 1e-9;

    private Room room(String id, boolean hasAc, int noise, int accessibility) {
        Room r = new Room();
        r.room_id = id;
        r.room_name = id;
        r.floor = 0;
        r.capacity = 100;
        r.has_ac = hasAc;
        r.noise_level = noise;
        r.accessibility_score = accessibility;
        r.is_accessible = accessibility >= 4;
        return r;
    }

    @Test
    void emptyInputReturnsEmptyList() {
        SAWEngine engine = new SAWEngine();
        assertTrue(engine.score(new ArrayList<>()).isEmpty());
        assertTrue(engine.score(null).isEmpty());
    }

    @Test
    void scoresMatchHandCalculatedWeightedSum() {
        // weights = {0.25, 0.25, 0.50}; room: has_ac=true, noise=4, accessibility=5
        // score = 0.25*1.0 + 0.25*(4/5) + 0.50*(5/5) = 0.25 + 0.20 + 0.50 = 0.95
        SAWEngine engine = new SAWEngine();
        List<RoomScore> results = engine.score(List.of(room("R1", true, 4, 5)));

        assertEquals(1, results.size());
        assertEquals(0.95, results.get(0).getScore(), DELTA);
    }

    @Test
    void betterRoomOnAllCriteriaScoresHigher() {
        SAWEngine engine = new SAWEngine();
        Room strong = room("STRONG", true, 5, 5);
        Room weak = room("WEAK", false, 1, 1);

        List<RoomScore> results = engine.score(List.of(strong, weak));
        double strongScore = results.stream().filter(r -> r.getRoomId().equals("STRONG")).findFirst().get().getScore();
        double weakScore = results.stream().filter(r -> r.getRoomId().equals("WEAK")).findFirst().get().getScore();

        assertTrue(strongScore > weakScore);
    }

    @Test
    void customWeightsMustSumToOne() {
        try {
            new SAWEngine(new double[]{0.5, 0.5, 0.5});
            assertTrue(false, "expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
