package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a single turn-by-turn navigation instruction in a courier's manifest.
 */
public class TurnByTurnStep {

    @JsonProperty("step")
    private int step;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("action")
    private String action;

    @JsonProperty("time_sec")
    private int timeSec;

    @JsonProperty("distance_meters")
    private double distanceMeters;

    public TurnByTurnStep() {
    }

    public TurnByTurnStep(int step, String from, String to, String action, int timeSec, double distanceMeters) {
        this.step = step;
        this.from = from;
        this.to = to;
        this.action = action;
        this.timeSec = timeSec;
        this.distanceMeters = distanceMeters;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getTimeSec() {
        return timeSec;
    }

    public void setTimeSec(int timeSec) {
        this.timeSec = timeSec;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurnByTurnStep that = (TurnByTurnStep) o;
        return step == that.step &&
               timeSec == that.timeSec &&
               Double.compare(that.distanceMeters, distanceMeters) == 0 &&
               Objects.equals(from, that.from) &&
               Objects.equals(to, that.to) &&
               Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(step, from, to, action, timeSec);
    }

    @Override
    public String toString() {
        return "TurnByTurnStep{" +
                "step=" + step +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", action='" + action + '\'' +
                ", timeSec=" + timeSec +
                '}';
    }
}
