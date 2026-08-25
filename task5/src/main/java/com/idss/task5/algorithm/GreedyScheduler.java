package com.idss.task5.algorithm;

import java.util.*;

/**
 * Greedy Largest Degree First — BASELINE heuristic.
 * 
 * Mechanism:
 * 1. Sort exams by degree (total clashing students) descending via PriorityQueue
 * 2. For each exam (most constrained first), assign the first slot that causes zero clashes
 * 3. If no valid slot found, pick the slot with fewest clashes (best effort)
 * 
 * Time: O(E log E + E * S) where E=exams, S=slots
 * Space: O(E^2) for conflict matrix
 * 
 * Fast but fatigue-blind — serves as baseline to prove GA's 60-80% improvement in Ch.8.
 */
public class GreedyScheduler {

    private final ConflictMatrix conflictMatrix;
    private final ConstraintValidator validator;
    private final int numSlots;

    public GreedyScheduler(ConflictMatrix conflictMatrix, ConstraintValidator validator, int numSlots) {
        this.conflictMatrix = conflictMatrix;
        this.validator = validator;
        this.numSlots = numSlots;
    }

    public AlgorithmResult run() {
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();

        int numExams = conflictMatrix.getSize();
        int[] chromosome = new int[numExams];
        Arrays.fill(chromosome, -1); // unassigned

        // Priority queue: most constrained exam first (highest degree)
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(b[1], a[1]));
        for (int i = 0; i < numExams; i++) {
            pq.offer(new int[]{i, conflictMatrix.getDegree(i)});
        }

        // Assign each exam greedily
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int examIdx = current[0];

            int bestSlot = findBestClashFreeSlot(examIdx, chromosome);
            if (bestSlot >= 0) {
                chromosome[examIdx] = bestSlot;
            } else {
                // No clash-free slot — assign slot with fewest clashes (best effort)
                chromosome[examIdx] = findLeastConflictSlot(examIdx, chromosome);
            }
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Build result
        AlgorithmResult result = new AlgorithmResult();
        result.bestChromosome = chromosome;
        result.algorithmName = "Greedy Largest Degree First";
        result.executionTimeMs = (endTime - startTime) / 1_000_000.0;
        result.memoryKb = Math.max(0, (endMemory - startMemory)) / 1024.0;
        result.hardViolations = validator.countHardViolations(chromosome);
        result.fatigueBreakdown = validator.calculateFatigueBreakdown(chromosome);
        result.totalFatiguePenalty = validator.getTotalFatiguePenalty(chromosome);
        result.fitnessScore = validator.calculateFitness(chromosome);

        return result;
    }

    /**
     * Find the first slot that causes zero clashes with already-assigned exams.
     */
    private int findBestClashFreeSlot(int examIdx, int[] chromosome) {
        for (int slot = 0; slot < numSlots; slot++) {
            if (isSlotClashFree(examIdx, slot, chromosome)) {
                return slot;
            }
        }
        return -1; // no clash-free slot found
    }

    /**
     * Check if assigning examIdx to slot causes any clash with already-assigned exams.
     */
    private boolean isSlotClashFree(int examIdx, int slot, int[] chromosome) {
        for (int other = 0; other < chromosome.length; other++) {
            if (other == examIdx || chromosome[other] != slot) continue;
            if (conflictMatrix.hasClash(examIdx, other)) return false;
        }
        return true;
    }

    /**
     * Fallback: find the slot with the fewest conflicts (best effort when no clash-free slot exists).
     */
    private int findLeastConflictSlot(int examIdx, int[] chromosome) {
        int bestSlot = 0;
        int minConflicts = Integer.MAX_VALUE;

        for (int slot = 0; slot < numSlots; slot++) {
            int conflicts = 0;
            for (int other = 0; other < chromosome.length; other++) {
                if (other == examIdx || chromosome[other] != slot) continue;
                if (conflictMatrix.hasClash(examIdx, other)) conflicts++;
            }
            if (conflicts < minConflicts) {
                minConflicts = conflicts;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }
}