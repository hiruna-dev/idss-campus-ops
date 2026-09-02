package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.RoomScore;

import java.util.ArrayList;
import java.util.List;

/**
 * SAW (Simple Additive Weighting) — investigated baseline comparator
 * (task_4_plan.md Section 8.1). Not used in production ranking; TOPSIS
 * remains the selected algorithm (Section 8.6). Implemented so Chapter 8/9
 * has a real baseline to benchmark TOPSIS/AHP/Fuzzy MCDM against, rather
 * than a theoretical description with nothing to measure.
 *
 * <p><b>Mechanism:</b> {@code score(room) = Sum(weight_i * normalized_i)}
 * across the three soft criteria. Uses the same normalization as
 * {@link TOPSISEngine} (has_ac to 1.0/0.0, noise_level/5, accessibility_score/5)
 * so the two are comparable on identical input — SAW has no reference point
 * (no ideal-best/ideal-worst step), which is its documented limitation
 * (Ch.3 Section 3.1).</p>
 *
 * <p><b>Time complexity:</b> O(n*m), n = eligible rooms, m = 3 criteria —
 * smallest constant of the four candidates (no distance or fuzzy arithmetic).
 * <b>Memory:</b> O(n*m).</p>
 */
public class SAWEngine {

    private static final int NUM_CRITERIA = 3;
    private static final int AC = 0;
    private static final int NOISE = 1;
    private static final int ACCESSIBILITY = 2;

    private final double[] weights;

    /** Constructs the engine with the default AHP-derived weights. */
    public SAWEngine() {
        this(new double[]{0.25, 0.25, 0.50});
    }

    /**
     * Constructs the engine with custom weights (for testing/benchmarking).
     *
     * @param weights {ac, noise, accessibility} weights, must sum to ~1.0
     */
    public SAWEngine(double[] weights) {
        if (weights == null || weights.length != NUM_CRITERIA) {
            throw new IllegalArgumentException("Expected " + NUM_CRITERIA + " weights (ac, noise, accessibility)");
        }
        double sum = weights[0] + weights[1] + weights[2];
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Weights must sum to 1.0, got " + sum);
        }
        this.weights = weights.clone();
    }

    /** Returns the current weight vector (defensive copy). */
    public double[] getWeights() {
        return weights.clone();
    }

    /**
     * Scores a list of eligible rooms via plain weighted sum.
     *
     * @param eligibleRooms rooms that passed the hard-constraint filter
     * @return list of RoomScore objects with SAW additive scores
     */
    public List<RoomScore> score(List<Room> eligibleRooms) {
        List<RoomScore> results = new ArrayList<>();
        if (eligibleRooms == null || eligibleRooms.isEmpty()) {
            return results;
        }

        for (Room room : eligibleRooms) {
            double acNorm = room.has_ac ? 1.0 : 0.0;
            double noiseNorm = room.noise_level / 5.0;
            double accessibilityNorm = room.accessibility_score / 5.0;

            double score = weights[AC] * acNorm
                    + weights[NOISE] * noiseNorm
                    + weights[ACCESSIBILITY] * accessibilityNorm;

            score = Math.round(score * 10000.0) / 10000.0;
            results.add(new RoomScore(room.room_id, score));
        }

        return results;
    }
}
