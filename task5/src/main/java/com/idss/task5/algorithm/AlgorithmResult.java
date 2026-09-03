package com.idss.task5.algorithm;

/**
 * Result wrapper returned by all 3 algorithms (Greedy, GA, SA).
 * Contains the best chromosome found + performance metadata for benchmarking.
 */
public class AlgorithmResult {

    public int[] bestChromosome;     // chromosome[examIndex] = slotIndex
    public int fitnessScore;         // lower = better (0 = perfect)
    public int hardViolations;       // must be 0 for valid schedule
    public int totalFatiguePenalty;  // soft penalty only
    public int[] fatigueBreakdown;   // [back_to_back, same_day, consecutive_day]
    public double executionTimeMs;   // algorithm time (NOT including Mongo save)
    public double memoryKb;          // approximate memory used
    public String algorithmName;

    // GA-specific
    public int generationsEvaluated;
    public int populationSize;
    public int convergenceGeneration;

    // SA-specific
    public int iterationsEvaluated;

    public AlgorithmResult() {}

    public boolean isClashFree() {
        return hardViolations == 0;
    }
}