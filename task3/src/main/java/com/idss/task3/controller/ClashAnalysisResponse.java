package com.idss.task3.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ClashAnalysisResponse {
    @JsonProperty("minimum_sessions")
    private int minimumSessions;
    @JsonProperty("sessions_used")
    private int sessionsUsed;
    @JsonProperty("lower_bound")
    private int lowerBound;
    @JsonProperty("upper_bound")
    private int upperBound;
    @JsonProperty("clash_pairs")
    private int clashPairs;
    @JsonProperty("session_groups")
    private List<List<String>> sessionGroups;

    public ClashAnalysisResponse() {}

    public int getMinimumSessions() { return minimumSessions; }
    public void setMinimumSessions(int minimumSessions) { this.minimumSessions = minimumSessions; }

    public int getSessionsUsed() { return sessionsUsed; }
    public void setSessionsUsed(int sessionsUsed) { this.sessionsUsed = sessionsUsed; }

    public int getLowerBound() { return lowerBound; }
    public void setLowerBound(int lowerBound) { this.lowerBound = lowerBound; }

    public int getUpperBound() { return upperBound; }
    public void setUpperBound(int upperBound) { this.upperBound = upperBound; }

    public int getClashPairs() { return clashPairs; }
    public void setClashPairs(int clashPairs) { this.clashPairs = clashPairs; }

    public List<List<String>> getSessionGroups() { return sessionGroups; }
    public void setSessionGroups(List<List<String>> sessionGroups) { this.sessionGroups = sessionGroups; }
}
