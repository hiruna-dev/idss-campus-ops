package com.idss.task4.algorithm;

/**
 * Triangular Fuzzy Number: {@code (low, mid, high)}, used by
 * {@link FuzzyMCDMEngine} to model uncertainty in subjective criteria
 * (task_4_plan.md Section 9 — "one instance per (room, criterion) pair").
 *
 * <p>A crisp value {@code v} is not exact — {@code low} and {@code high}
 * bound how far off it plausibly is (e.g. a "3/5 noise" rating), and
 * {@code mid} is the most-likely value. Arithmetic is component-wise,
 * which is the standard simplification for triangular fuzzy numbers
 * (exact for addition and scalar multiplication).</p>
 */
public final class TFN {

    public final double low;
    public final double mid;
    public final double high;

    public TFN(double low, double mid, double high) {
        if (low > mid || mid > high) {
            throw new IllegalArgumentException(
                    "TFN requires low <= mid <= high, got (" + low + ", " + mid + ", " + high + ")");
        }
        this.low = low;
        this.mid = mid;
        this.high = high;
    }

    /** A crisp (non-fuzzy) value represented as a degenerate TFN. */
    public static TFN crisp(double value) {
        return new TFN(value, value, value);
    }

    /** Component-wise sum: (a.low+b.low, a.mid+b.mid, a.high+b.high). */
    public TFN add(TFN other) {
        return new TFN(this.low + other.low, this.mid + other.mid, this.high + other.high);
    }

    /** Scalar multiplication: scales all three components (scalar must be >= 0 to preserve ordering). */
    public TFN scale(double scalar) {
        if (scalar < 0) {
            throw new IllegalArgumentException("TFN.scale requires a non-negative scalar (weights are >= 0)");
        }
        return new TFN(this.low * scalar, this.mid * scalar, this.high * scalar);
    }

    /**
     * Defuzzifies via the centroid (mean of the three points) method:
     * {@code (low + mid + high) / 3} — the method named in task_4_plan.md
     * Section 8.4.
     */
    public double centroid() {
        return (low + mid + high) / 3.0;
    }

    @Override
    public String toString() {
        return String.format("TFN(%.4f, %.4f, %.4f)", low, mid, high);
    }
}
