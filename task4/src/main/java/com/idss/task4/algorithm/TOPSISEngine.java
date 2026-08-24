package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.RoomScore;

import java.util.ArrayList;
import java.util.List;

/**
 * TOPSIS (Technique for Order of Preference by Similarity to Ideal Solution)
 * scoring engine using AHP-derived weights (task_4_plan.md Sections 6-8).
 *
 * <p><b>Algorithm steps (Section 7 pseudocode):</b>
 * <ol>
 *   <li>Normalize each criterion to 0–1 range</li>
 *   <li>Apply AHP-derived weights to build the weighted normalized matrix</li>
 *   <li>Determine Ideal Best (max) and Ideal Worst (min) per criterion</li>
 *   <li>Compute Euclidean distance to ideal best and worst for each room</li>
 *   <li>Compute closeness coefficient = dist_worst / (dist_best + dist_worst)</li>
 * </ol>
 *
 * <p><b>Weights (AHP-derived, Section 6):</b><br>
 * AC comfort = 0.25, Noise = 0.25, Accessibility = 0.50<br>
 * Consistency Ratio = 0 (perfectly consistent pairwise matrix).</p>
 *
 * <p><b>Time complexity:</b> O(n·m) where n = eligible rooms, m = 3 criteria.
 * Constant is larger than SAW due to Euclidean distance computation.</p>
 *
 * <p><b>Space complexity:</b> O(n·m) for the weighted normalized matrix.</p>
 */
public class TOPSISEngine {

    /** Number of soft criteria: AC comfort, Noise, Accessibility. */
    private static final int NUM_CRITERIA = 3;

    /** Criterion indices for readability. */
    private static final int AC = 0;
    private static final int NOISE = 1;
    private static final int ACCESSIBILITY = 2;

    /**
     * AHP-derived weight vector (task_4_plan.md Section 6).
     * Derived from pairwise comparison matrix with CR = 0.
     */
    private final double[] weights;

    /**
     * Constructs the engine with the default AHP-derived weights.
     * AC = 0.25, Noise = 0.25, Accessibility = 0.50.
     */
    public TOPSISEngine() {
        this.weights = new double[]{0.25, 0.25, 0.50};
    }

    /**
     * Constructs the engine with custom weights (for testing/benchmarking).
     *
     * @param acWeight             weight for AC comfort criterion
     * @param noiseWeight          weight for noise criterion
     * @param accessibilityWeight  weight for accessibility criterion
     * @throws IllegalArgumentException if weights don't sum to ~1.0
     */
    public TOPSISEngine(double acWeight, double noiseWeight, double accessibilityWeight) {
        double sum = acWeight + noiseWeight + accessibilityWeight;
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException(
                    "Weights must sum to 1.0, got " + sum);
        }
        this.weights = new double[]{acWeight, noiseWeight, accessibilityWeight};
    }

    /**
     * Returns the current weight vector (defensive copy).
     */
    public double[] getWeights() {
        return weights.clone();
    }

    /**
     * Scores a list of eligible rooms using TOPSIS.
     *
     * <p>If 0 or 1 rooms are provided, the edge case is handled directly:
     * 0 rooms returns empty, 1 room returns score 1.0 (trivially ideal).</p>
     *
     * @param eligibleRooms rooms that passed the hard-constraint filter
     * @return list of RoomScore objects with TOPSIS closeness coefficients
     */
    public List<RoomScore> score(List<Room> eligibleRooms) {
        List<RoomScore> results = new ArrayList<>();

        if (eligibleRooms == null || eligibleRooms.isEmpty()) {
            return results;
        }

        int n = eligibleRooms.size();

        // Edge case: single room is trivially the best
        if (n == 1) {
            results.add(new RoomScore(eligibleRooms.get(0).room_id, 1.0));
            return results;
        }

        // --- Step 1: Build the normalized decision matrix ---
        // Normalization: has_ac → 1.0/0.0, noise_level/5.0, accessibility_score/5.0
        double[][] normalized = new double[n][NUM_CRITERIA];
        for (int i = 0; i < n; i++) {
            Room room = eligibleRooms.get(i);
            normalized[i][AC] = room.has_ac ? 1.0 : 0.0;
            normalized[i][NOISE] = room.noise_level / 5.0;
            normalized[i][ACCESSIBILITY] = room.accessibility_score / 5.0;
        }

        // --- Step 2: Apply AHP-derived weights → weighted normalized matrix ---
        double[][] weighted = new double[n][NUM_CRITERIA];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < NUM_CRITERIA; j++) {
                weighted[i][j] = weights[j] * normalized[i][j];
            }
        }

        // --- Step 3: Determine Ideal Best and Ideal Worst ---
        // All criteria are "higher = better" (benefit criteria)
        double[] idealBest = new double[NUM_CRITERIA];
        double[] idealWorst = new double[NUM_CRITERIA];

        for (int j = 0; j < NUM_CRITERIA; j++) {
            idealBest[j] = Double.NEGATIVE_INFINITY;
            idealWorst[j] = Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < NUM_CRITERIA; j++) {
                if (weighted[i][j] > idealBest[j]) {
                    idealBest[j] = weighted[i][j];
                }
                if (weighted[i][j] < idealWorst[j]) {
                    idealWorst[j] = weighted[i][j];
                }
            }
        }

        // --- Step 4: Compute distances and closeness coefficient ---
        for (int i = 0; i < n; i++) {
            double distBest = euclideanDistance(weighted[i], idealBest);
            double distWorst = euclideanDistance(weighted[i], idealWorst);

            // Closeness coefficient: higher = better
            double score;
            if (distBest + distWorst == 0.0) {
                // All rooms identical on all criteria — assign equal score
                score = 0.5;
            } else {
                score = distWorst / (distBest + distWorst);
            }

            // Round to 4 decimal places for clean JSON output
            score = Math.round(score * 10000.0) / 10000.0;

            results.add(new RoomScore(eligibleRooms.get(i).room_id, score));
        }

        return results;
    }

    /**
     * Computes the Euclidean distance between two vectors.
     * {@code sqrt(sum((a[i] - b[i])^2))}
     */
    private double euclideanDistance(double[] a, double[] b) {
        double sumSq = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq);
    }
}
