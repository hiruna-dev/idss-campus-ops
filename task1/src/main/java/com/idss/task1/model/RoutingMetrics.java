package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idss.common.config.Canonical;

/**
 * Encapsulates performance benchmarks and constraint quality metrics for Task 1 routing operations.
 * Outputted to data/shared/output_routing_metrics.json.
 */
public class RoutingMetrics {

    @JsonProperty("generation_timestamp")
    private String generationTimestamp;

    @JsonProperty("algorithm_used")
    private String algorithmUsed = "A* Search Algorithm (3D Euclidean & Floor Penalty Heuristic)";

    @JsonProperty("benchmark_suite_version")
    private String benchmarkSuiteVersion = "PDSA_26.1_v1.0";

    @JsonProperty("execution_time_ms")
    private double executionTimeMs;

    @JsonProperty("memory_allocated_kb")
    private double memoryAllocatedKb;

    @JsonProperty("nodes_explored_percentage")
    private double nodesExploredPercentage;

    @JsonProperty("step_free_constraint_satisfaction_percentage")
    private double stepFreeConstraintSatisfactionPercentage = 100.0;

    @JsonProperty("time_window_violations")
    private int timeWindowViolations = 0;

    @JsonProperty("hard_constraint_violations")
    private int hardConstraintViolations = 0;

    @JsonProperty("optimality_ratio")
    private double optimalityRatio = 1.0;

    @JsonProperty("status")
    private String status = Canonical.STATUS_OPTIMAL;

    public RoutingMetrics() {
    }

    public RoutingMetrics(String generationTimestamp, String algorithmUsed, String benchmarkSuiteVersion,
                          double executionTimeMs, double memoryAllocatedKb, double nodesExploredPercentage,
                          double stepFreeConstraintSatisfactionPercentage, int timeWindowViolations,
                          int hardConstraintViolations, double optimalityRatio, String status) {
        this.generationTimestamp = generationTimestamp;
        this.algorithmUsed = algorithmUsed;
        this.benchmarkSuiteVersion = benchmarkSuiteVersion;
        this.executionTimeMs = executionTimeMs;
        this.memoryAllocatedKb = memoryAllocatedKb;
        this.nodesExploredPercentage = nodesExploredPercentage;
        this.stepFreeConstraintSatisfactionPercentage = stepFreeConstraintSatisfactionPercentage;
        this.timeWindowViolations = timeWindowViolations;
        this.hardConstraintViolations = hardConstraintViolations;
        this.optimalityRatio = optimalityRatio;
        this.status = status;
    }

    public String getGenerationTimestamp() {
        return generationTimestamp;
    }

    public void setGenerationTimestamp(String generationTimestamp) {
        this.generationTimestamp = generationTimestamp;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public String getBenchmarkSuiteVersion() {
        return benchmarkSuiteVersion;
    }

    public void setBenchmarkSuiteVersion(String benchmarkSuiteVersion) {
        this.benchmarkSuiteVersion = benchmarkSuiteVersion;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public double getMemoryAllocatedKb() {
        return memoryAllocatedKb;
    }

    public void setMemoryAllocatedKb(double memoryAllocatedKb) {
        this.memoryAllocatedKb = memoryAllocatedKb;
    }

    public double getNodesExploredPercentage() {
        return nodesExploredPercentage;
    }

    public void setNodesExploredPercentage(double nodesExploredPercentage) {
        this.nodesExploredPercentage = nodesExploredPercentage;
    }

    public double getStepFreeConstraintSatisfactionPercentage() {
        return stepFreeConstraintSatisfactionPercentage;
    }

    public void setStepFreeConstraintSatisfactionPercentage(double stepFreeConstraintSatisfactionPercentage) {
        this.stepFreeConstraintSatisfactionPercentage = stepFreeConstraintSatisfactionPercentage;
    }

    public int getTimeWindowViolations() {
        return timeWindowViolations;
    }

    public void setTimeWindowViolations(int timeWindowViolations) {
        this.timeWindowViolations = timeWindowViolations;
    }

    public int getHardConstraintViolations() {
        return hardConstraintViolations;
    }

    public void setHardConstraintViolations(int hardConstraintViolations) {
        this.hardConstraintViolations = hardConstraintViolations;
    }

    public double getOptimalityRatio() {
        return optimalityRatio;
    }

    public void setOptimalityRatio(double optimalityRatio) {
        this.optimalityRatio = optimalityRatio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
