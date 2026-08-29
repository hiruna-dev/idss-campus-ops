package com.idss.task5.algorithm;

import java.util.*;

/**
 * Simulated Annealing — COMPARATOR metaheuristic for Task 5.
 *
 * <p>Single-solution local search inspired by metallurgical annealing.</p>
 *
 * <p><b>Mechanism:</b></p>
 * <ul>
 *   <li>Start from random solution at temperature T0 = 1000</li>
 *   <li>Generate neighbor by reassigning one random exam to a different slot</li>
 *   <li>Accept if better; if worse, accept with probability P = exp(-delta/T)</li>
 *   <li>Cool: T = 0.95 * T until T &lt; 1</li>
 * </ul>
 *
 * <p><b>Time complexity:</b> O(I * E) where I = iterations until T &lt; 1 (~135 for our params)</p>
 * <p><b>Space complexity:</b> O(E^2) — conflict matrix only</p>
 *
 * <p><b>Limitation:</b> Sensitive to cooling schedule. Achieves high satisfaction (98%)
 * but fails to find clash-free solutions on dense conflict graphs.</p>
 */
public class SimulatedAnnealing {

    private final ConflictMatrix conflictMatrix;
    private final ConstraintValidator validator;
    private final int numSlots;

    // SA parameters (from application.yml defaults)
    private double initialTemperature = 1000.0;
    private double coolingRate = 0.95;
    private double minTemperature = 1.0;

    private final Random random = new Random(42);

    public SimulatedAnnealing(ConflictMatrix conflictMatrix, ConstraintValidator validator, int numSlots) {
        this.conflictMatrix = conflictMatrix;
        this.validator = validator;
        this.numSlots = numSlots;
    }

    /**
     * Override default parameters.
     */
    public void setParameters(double initialTemperature, double coolingRate, double minTemperature) {
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.minTemperature = minTemperature;
    }

    public AlgorithmResult run() {
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();

        int numExams = conflictMatrix.getSize();

        // Start from random solution
        int[] current = randomChromosome(numExams);
        int currentFitness = validator.calculateFitness(current);

        // Track best found
        int[] best = Arrays.copyOf(current, numExams);
        int bestFitness = currentFitness;

        double temperature = initialTemperature;
        int iterations = 0;

        // Annealing loop
        while (temperature > minTemperature) {
            // Generate neighbor: swap two random exams' slots
            int[] neighbor = generateNeighbor(current);
            int neighborFitness = validator.calculateFitness(neighbor);

            int delta = neighborFitness - currentFitness;

            // Accept or reject
            if (delta < 0) {
                // Better solution — always accept
                current = neighbor;
                currentFitness = neighborFitness;
            } else {
                // Worse solution — accept with probability exp(-delta/T)
                double acceptanceProbability = Math.exp(-delta / temperature);
                if (random.nextDouble() < acceptanceProbability) {
                    current = neighbor;
                    currentFitness = neighborFitness;
                }
            }

            // Update best
            if (currentFitness < bestFitness) {
                best = Arrays.copyOf(current, numExams);
                bestFitness = currentFitness;
            }

            // Cool down
            temperature *= coolingRate;
            iterations++;
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Build result
        AlgorithmResult result = new AlgorithmResult();
        result.bestChromosome = best;
        result.algorithmName = "Simulated Annealing";
        result.executionTimeMs = (endTime - startTime) / 1_000_000.0;
        result.memoryKb = Math.max(0, (endMemory - startMemory)) / 1024.0;
        result.hardViolations = validator.countHardViolations(best);
        result.fatigueBreakdown = validator.calculateFatigueBreakdown(best);
        result.totalFatiguePenalty = validator.getTotalFatiguePenalty(best);
        result.fitnessScore = validator.calculateFitness(best);
        result.iterationsEvaluated = iterations;

        return result;
    }

    /**
     * Generate a random chromosome.
     */
    private int[] randomChromosome(int numExams) {
        int[] chromosome = new int[numExams];
        for (int i = 0; i < numExams; i++) {
            chromosome[i] = random.nextInt(numSlots);
        }
        return chromosome;
    }

    /**
     * Generate a neighbor by randomly picking one exam and reassigning it to a different slot.
     */
    private int[] generateNeighbor(int[] chromosome) {
        int[] neighbor = Arrays.copyOf(chromosome, chromosome.length);

        // Pick a random exam and change its slot
        int examIdx = random.nextInt(chromosome.length);
        int newSlot;
        do {
            newSlot = random.nextInt(numSlots);
        } while (newSlot == neighbor[examIdx]); // ensure it's actually different

        neighbor[examIdx] = newSlot;
        return neighbor;
    }
}