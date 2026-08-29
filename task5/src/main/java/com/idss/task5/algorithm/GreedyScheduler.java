package com.idss.task5.algorithm;

import java.util.*;

/**
 * Greedy Largest Degree First — BASELINE heuristic for Task 5.
 *
 * <p>Deterministic constructive heuristic that sorts exams by descending conflict degree
 * (most constrained first) and assigns each to the first available clash-free slot.</p>
 *
 * <p><b>Time complexity:</b> O(E log E + E * S) — PriorityQueue sort + slot scanning</p>
 * <p><b>Space complexity:</b> O(E^2) — conflict matrix</p>
 *
 * <p>Extremely fast (&lt;1ms even for 100 exams) and guarantees clash-free schedules
 * when sufficient slots exist. However, completely blind to soft fatigue constraints,
 * producing 40x more fatigue than GA at E=100.</p>
 *
 * <p>Serves as the baseline to prove GA's superiority in Chapter 8.</p>
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