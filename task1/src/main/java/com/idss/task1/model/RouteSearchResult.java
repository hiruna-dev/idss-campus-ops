package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Standardized search result container produced by pathfinding engines
 * (Dijkstra, Bellman-Ford, and A*).
 *
 * <p>Captures the reconstructed path sequence, physical distance, transit time,
 * and algorithm performance metrics (nodes explored and execution time)
 * used for empirical comparison in Chapter 8 of the Individual Report.</p>
 */
public class RouteSearchResult {

    @JsonProperty("algorithm_name")
    private String algorithmName;

    @JsonProperty("source_node")
    private String sourceNode;

    @JsonProperty("target_node")
    private String targetNode;

    @JsonProperty("is_reachable")
    private boolean reachable;

    @JsonProperty("path_sequence")
    private List<String> pathSequence = new ArrayList<>();

    @JsonProperty("total_distance_meters")
    private double totalDistanceMeters;

    @JsonProperty("total_transit_time_seconds")
    private int totalTransitTimeSeconds;

    @JsonProperty("nodes_explored")
    private int nodesExplored;

    @JsonProperty("execution_time_ms")
    private double executionTimeMs;

    public RouteSearchResult() {
    }

    public RouteSearchResult(String algorithmName, String sourceNode, String targetNode,
                             boolean reachable, List<String> pathSequence,
                             double totalDistanceMeters, int totalTransitTimeSeconds,
                             int nodesExplored, double executionTimeMs) {
        this.algorithmName = algorithmName;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.reachable = reachable;
        if (pathSequence != null) {
            this.pathSequence = pathSequence;
        }
        this.totalDistanceMeters = totalDistanceMeters;
        this.totalTransitTimeSeconds = totalTransitTimeSeconds;
        this.nodesExplored = nodesExplored;
        this.executionTimeMs = executionTimeMs;
    }

    /** Factory method for unreachable routes */
    public static RouteSearchResult unreachable(String algorithmName, String sourceNode, String targetNode, int nodesExplored, double executionTimeMs) {
        return new RouteSearchResult(algorithmName, sourceNode, targetNode, false, Collections.emptyList(), 0.0, 0, nodesExplored, executionTimeMs);
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public List<String> getPathSequence() {
        return pathSequence;
    }

    public void setPathSequence(List<String> pathSequence) {
        this.pathSequence = pathSequence != null ? pathSequence : new ArrayList<>();
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public void setTotalDistanceMeters(double totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public int getTotalTransitTimeSeconds() {
        return totalTransitTimeSeconds;
    }

    public void setTotalTransitTimeSeconds(int totalTransitTimeSeconds) {
        this.totalTransitTimeSeconds = totalTransitTimeSeconds;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }

    public void setNodesExplored(int nodesExplored) {
        this.nodesExplored = nodesExplored;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteSearchResult that = (RouteSearchResult) o;
        return reachable == that.reachable &&
               Double.compare(that.totalDistanceMeters, totalDistanceMeters) == 0 &&
               totalTransitTimeSeconds == that.totalTransitTimeSeconds &&
               Objects.equals(algorithmName, that.algorithmName) &&
               Objects.equals(sourceNode, that.sourceNode) &&
               Objects.equals(targetNode, that.targetNode) &&
               Objects.equals(pathSequence, that.pathSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithmName, sourceNode, targetNode, reachable, pathSequence);
    }

    @Override
    public String toString() {
        return "RouteSearchResult{" +
                "algorithm='" + algorithmName + '\'' +
                ", reachable=" + reachable +
                ", distance=" + totalDistanceMeters + "m" +
                ", time=" + totalTransitTimeSeconds + "s" +
                ", nodesExplored=" + nodesExplored +
                ", executionTime=" + executionTimeMs + "ms" +
                ", path=" + pathSequence +
                '}';
    }
}
