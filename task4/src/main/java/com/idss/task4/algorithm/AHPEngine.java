package com.idss.task4.algorithm;

/**
 * AHP (Analytic Hierarchy Process) weight derivation for Task 4's soft
 * criteria — AC comfort, Noise, Accessibility (task_4_plan.md Section 6).
 *
 * <p>Derives weights from a pairwise comparison matrix (Saaty 1–9 scale)
 * via column-normalization + row-averaging, and validates the judgments
 * with a Consistency Ratio check (CR &lt; 0.1 threshold, per Saaty).</p>
 *
 * <p><b>Role:</b> weight-derivation support only — this does not rank
 * rooms directly (task_4_plan.md Section 8.3). {@link TOPSISEngine}
 * consumes the derived weight vector.</p>
 *
 * <p><b>Time complexity:</b> O(k^2) for k criteria (k=3 here) — cheap,
 * criteria-count bound, not room-count bound. <b>Memory:</b> O(k^2) for
 * the matrix.</p>
 */
public class AHPEngine {

    /** Saaty's Random Index, indexed by matrix size n (1-based, so RANDOM_INDEX[n-1]). */
    private static final double[] RANDOM_INDEX =
            {0.0, 0.0, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

    /** Saaty's acceptability threshold: judgments are consistent enough if CR < 0.1. */
    private static final double CONSISTENCY_THRESHOLD = 0.1;

    private final double[][] matrix;
    private final int n;
    private final double[] weights;
    private final double consistencyRatio;

    /**
     * Builds the engine with the default pairwise comparison matrix
     * (task_4_plan.md Section 6): AC comfort, Noise, Accessibility —
     * accessibility judged moderately more important (2) than either
     * AC or noise, which are equally important to each other (1).
     */
    public AHPEngine() {
        this(new double[][]{
                {1.0, 1.0, 0.5},
                {1.0, 1.0, 0.5},
                {2.0, 2.0, 1.0}
        });
    }

    /**
     * Builds the engine with a custom pairwise comparison matrix
     * (for testing/benchmarking with a different criteria set).
     *
     * @param pairwiseMatrix square Saaty-scale comparison matrix
     */
    public AHPEngine(double[][] pairwiseMatrix) {
        validateSquare(pairwiseMatrix);
        this.matrix = pairwiseMatrix;
        this.n = pairwiseMatrix.length;
        this.weights = deriveWeights();
        this.consistencyRatio = computeConsistencyRatio();
    }

    private void validateSquare(double[][] m) {
        if (m == null || m.length == 0) {
            throw new IllegalArgumentException("Pairwise matrix must be non-empty");
        }
        for (double[] row : m) {
            if (row == null || row.length != m.length) {
                throw new IllegalArgumentException("Pairwise matrix must be square");
            }
        }
    }

    /**
     * Derives weights via column-normalization + row-averaging
     * (task_4_plan.md Section 6 "Derivation").
     */
    private double[] deriveWeights() {
        double[] columnSums = new double[n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                columnSums[j] += matrix[i][j];
            }
        }

        double[][] normalized = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                normalized[i][j] = matrix[i][j] / columnSums[j];
            }
        }

        double[] w = new double[n];
        for (int i = 0; i < n; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                rowSum += normalized[i][j];
            }
            w[i] = rowSum / n;
        }
        return w;
    }

    /**
     * Computes the Consistency Ratio: {@code CR = CI / RI}, where
     * {@code CI = (lambda_max - n) / (n - 1)} and lambda_max is the
     * average of {@code (matrix * weights)[i] / weights[i]} across rows.
     * A 1x1 or 2x2 matrix is always perfectly consistent (CR = 0).
     */
    private double computeConsistencyRatio() {
        if (n <= 2) {
            return 0.0;
        }

        double[] weightedSums = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += matrix[i][j] * weights[j];
            }
            weightedSums[i] = sum;
        }

        double lambdaMax = 0.0;
        for (int i = 0; i < n; i++) {
            lambdaMax += weightedSums[i] / weights[i];
        }
        lambdaMax /= n;

        double ci = (lambdaMax - n) / (n - 1);
        double ri = (n - 1 < RANDOM_INDEX.length) ? RANDOM_INDEX[n - 1] : RANDOM_INDEX[RANDOM_INDEX.length - 1];

        return ri == 0.0 ? 0.0 : ci / ri;
    }

    /** Returns the derived weight vector (defensive copy), in criteria order. */
    public double[] getWeights() {
        return weights.clone();
    }

    /** Returns the Consistency Ratio for the judgments used to build this engine. */
    public double getConsistencyRatio() {
        return consistencyRatio;
    }

    /** True if CR &lt; 0.1 (Saaty's acceptability threshold). */
    public boolean isConsistent() {
        return consistencyRatio < CONSISTENCY_THRESHOLD;
    }
}
