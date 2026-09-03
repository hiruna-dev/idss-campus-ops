package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.RoomScore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fuzzy MCDM — LO3 heuristic/approximation comparator
 * (task_4_plan.md Section 8.4). Not used in production ranking; TOPSIS
 * remains the selected algorithm (Section 8.6). This exists to satisfy
 * the coursework's heuristic/uncertainty-modeling requirement and to
 * give Chapter 8/9 a real comparator to benchmark against.
 *
 * <p><b>Mechanism:</b> each room's subjective criteria (noise,
 * accessibility) are represented as a {@link TFN} rather than a single
 * crisp number — a "3/5 noise" rating is treated as plausibly anywhere
 * in a band around 3, not as exactly 3. {@code has_ac} is objectively
 * measurable (a room either has AC or doesn't), so it is fuzzified as a
 * degenerate TFN with zero spread. Each criterion's TFN is scaled by its
 * AHP-derived weight, the three weighted TFNs are summed into one
 * aggregate fuzzy score per room, and that is defuzzified via the
 * centroid method {@code (low+mid+high)/3} into the room's crisp score.</p>
 *
 * <p><b>Contrast with TOPSIS:</b> this produces an absolute score per
 * room — it never compares one room's weighted matrix against another's
 * ideal-best/ideal-worst — so, unlike TOPSIS (Section 8.2), it cannot
 * suffer rank reversal when the eligible room set changes. The trade-off
 * is that it doesn't reward being closest to the best available option;
 * it only rewards being good in absolute terms.</p>
 *
 * <p><b>Time complexity:</b> O(n·m) — same order as TOPSIS/SAW, but a
 * larger constant since every criterion score is a 3-tuple with its own
 * arithmetic instead of a single double (task_4_plan.md Section 8.4).
 * <b>Memory:</b> ~3x SAW's, since each (room, criterion) score is a TFN
 * triple rather than one double.</p>
 */
public class FuzzyMCDMEngine {

    private static final int NUM_CRITERIA = 3;
    private static final int AC = 0;
    private static final int NOISE = 1;
    private static final int ACCESSIBILITY = 2;

    /** Default fuzziness spread applied to subjective (rated 1-5) criteria, in normalized [0,1] units. */
    public static final double DEFAULT_FUZZINESS = 0.1;

    private final double[] weights;
    private final double fuzziness;

    /** Constructs the engine with the default AHP-derived weights and default fuzziness spread. */
    public FuzzyMCDMEngine() {
        this(new double[]{0.25, 0.25, 0.50}, DEFAULT_FUZZINESS);
    }

    /**
     * Constructs the engine with custom weights and fuzziness spread
     * (for testing/benchmarking).
     *
     * @param weights   {ac, noise, accessibility} weights, must sum to ~1.0
     * @param fuzziness spread applied around each subjective criterion's
     *                  normalized crisp value (0 = behaves like SAW, no uncertainty modeled)
     */
    public FuzzyMCDMEngine(double[] weights, double fuzziness) {
        if (weights == null || weights.length != NUM_CRITERIA) {
            throw new IllegalArgumentException("Expected " + NUM_CRITERIA + " weights (ac, noise, accessibility)");
        }
        double sum = weights[0] + weights[1] + weights[2];
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Weights must sum to 1.0, got " + sum);
        }
        if (fuzziness < 0) {
            throw new IllegalArgumentException("fuzziness must be non-negative");
        }
        this.weights = weights.clone();
        this.fuzziness = fuzziness;
    }

    /** Returns the current weight vector (defensive copy). */
    public double[] getWeights() {
        return weights.clone();
    }

    /**
     * Scores a list of eligible rooms via fuzzy MCDM.
     *
     * @param eligibleRooms rooms that passed the hard-constraint filter
     * @return list of RoomScore objects with defuzzified (centroid) scores
     */
    public List<RoomScore> score(List<Room> eligibleRooms) {
        List<RoomScore> results = new ArrayList<>();
        if (eligibleRooms == null || eligibleRooms.isEmpty()) {
            return results;
        }

        for (Room room : eligibleRooms) {
            TFN[] criteria = fuzzify(room);

            TFN aggregate = TFN.crisp(0.0);
            for (int j = 0; j < NUM_CRITERIA; j++) {
                aggregate = aggregate.add(criteria[j].scale(weights[j]));
            }

            double score = Math.round(aggregate.centroid() * 10000.0) / 10000.0;
            results.add(new RoomScore(room.room_id, score));
        }

        return results;
    }

    /**
     * Builds the per-criterion TFNs for one room. {@code has_ac} is
     * objective (no spread); noise and accessibility are subjective
     * 1-5 ratings, so a symmetric spread is applied around the
     * normalized crisp value and clamped to [0, 1].
     */
    private TFN[] fuzzify(Room room) {
        double acCrisp = room.has_ac ? 1.0 : 0.0;
        double noiseCrisp = room.noise_level / 5.0;
        double accessibilityCrisp = room.accessibility_score / 5.0;

        TFN[] criteria = new TFN[NUM_CRITERIA];
        criteria[AC] = TFN.crisp(acCrisp);
        criteria[NOISE] = spread(noiseCrisp);
        criteria[ACCESSIBILITY] = spread(accessibilityCrisp);
        return criteria;
    }

    private TFN spread(double crisp) {
        double low = clamp01(crisp - fuzziness);
        double high = clamp01(crisp + fuzziness);
        return new TFN(low, crisp, high);
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
