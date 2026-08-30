package com.idss.task3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Output model for Clash Analysis matching Section 6 exact JSON schema.
 */
public class ClashAnalysisOutput {

    @JsonProperty("minimum_sessions")
    private int minimumSessions;

    @JsonProperty("sessions_used")
    private int sessionsUsed;

    @JsonProperty("lower_bound")
    private int lowerBound;

    @JsonProperty("upper_bound")
    private int upperBound;

    @JsonProperty("coloring_valid")
    private boolean coloringValid;

    @JsonProperty("hard_constraint_violations")
    private int hardConstraintViolations;

    @JsonProperty("clash_pairs")
    private List<ConflictEdge> clashPairs;

    @JsonProperty("session_groups")
    private List<List<String>> sessionGroups;

    public ClashAnalysisOutput() {}

    public int getMinimumSessions() { return minimumSessions; }
    public void setMinimumSessions(int minimumSessions) { this.minimumSessions = minimumSessions; }

    public int getSessionsUsed() { return sessionsUsed; }
    public void setSessionsUsed(int sessionsUsed) { this.sessionsUsed = sessionsUsed; }

    public int getLowerBound() { return lowerBound; }
    public void setLowerBound(int lowerBound) { this.lowerBound = lowerBound; }

    public int getUpperBound() { return upperBound; }
    public void setUpperBound(int upperBound) { this.upperBound = upperBound; }

    public boolean isColoringValid() { return coloringValid; }
    public void setColoringValid(boolean coloringValid) { this.coloringValid = coloringValid; }

    public int getHardConstraintViolations() { return hardConstraintViolations; }
    public void setHardConstraintViolations(int hardConstraintViolations) { this.hardConstraintViolations = hardConstraintViolations; }

    public List<ConflictEdge> getClashPairs() { return clashPairs; }
    public void setClashPairs(List<ConflictEdge> clashPairs) { this.clashPairs = clashPairs; }

    public List<List<String>> getSessionGroups() { return sessionGroups; }
    public void setSessionGroups(List<List<String>> sessionGroups) { this.sessionGroups = sessionGroups; }
}
