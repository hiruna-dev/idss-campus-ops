package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a 3D spatial coordinate vector (x, y, z) in meters.
 * Used for Euclidean distance calculations and A* heuristic estimations.
 */
public class Coordinates3D {

    public static final double DEFAULT_BETA_FLOOR_PENALTY = 3.5;

    @JsonProperty("x")
    private double x;

    @JsonProperty("y")
    private double y;

    @JsonProperty("z")
    private double z;

    public Coordinates3D() {
    }

    public Coordinates3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Computes the 3D Euclidean distance with a vertical floor transition penalty factor (beta = 3.5).
     * Formula: sqrt((x1 - x2)^2 + (y1 - y2)^2 + (beta * (z1 - z2))^2)
     */
    public double distanceTo(Coordinates3D other) {
        return distanceTo(other, DEFAULT_BETA_FLOOR_PENALTY);
    }

    /**
     * Computes the 3D Euclidean distance with a custom vertical penalty factor beta.
     */
    public double distanceTo(Coordinates3D other, double beta) {
        if (other == null) {
            return 0.0;
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = beta * (this.z - other.z);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates3D that = (Coordinates3D) o;
        return Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0 &&
               Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Coordinates3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
