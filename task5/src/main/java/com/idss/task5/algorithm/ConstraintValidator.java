package com.idss.task5.algorithm;

import com.idss.common.model.Exam;
import com.idss.common.model.Timeslot;
import java.util.*;

/**
 * Constraint Validator — checks hard constraints and calculates soft fatigue penalties.
 * 
 * HARD Constraints (MUST be 0 violations, weighted x1000 in fitness):
 * - No student has 2 exams in the same date+session (clash)
 * - Each exam assigned to exactly one slot
 * 
 * SOFT Constraints (MINIMIZE these):
 * - Back-to-back penalty (10): same student has exams in consecutive sessions on same day
 * - Same-day penalty (5): same student has 2 exams on same day (morning + afternoon)
 * - Consecutive-day penalty (1): exams on back-to-back days
 * 
 * Fitness formula: 1000 * hard_violations + 10 * back_to_back + 5 * same_day + 1 * consecutive_day
 */
public class ConstraintValidator {

    // Fatigue penalty weights (from MCF v2.2 / task_5_plan.md)
    public static final int HARD_WEIGHT = 1000;
    public static final int BACK_TO_BACK_WEIGHT = 10;
    public static final int SAME_DAY_WEIGHT = 5;
    public static final int CONSECUTIVE_DAY_WEIGHT = 1;

    private final ConflictMatrix conflictMatrix;
    private final List<Timeslot> timeslots;
    private final Map<String, Exam> examMap;

    // Pre-computed: which slot indices are on the same date
    private final Map<String, List<Integer>> dateToSlotIndices;
    // Pre-computed: ordered list of unique dates
    private final List<String> orderedDates;

    public ConstraintValidator(ConflictMatrix conflictMatrix, List<Timeslot> timeslots, List<Exam> exams) {
        this.conflictMatrix = conflictMatrix;
        this.timeslots = timeslots;
        this.examMap = new HashMap<>();
        for (Exam exam : exams) {
            examMap.put(exam.course_code, exam);
        }

        // Pre-compute date groupings for fast fatigue calculation
        this.dateToSlotIndices = new HashMap<>();
        Set<String> dateSet = new LinkedHashSet<>();
        for (int i = 0; i < timeslots.size(); i++) {
            String date = timeslots.get(i).date;
            dateSet.add(date);
            dateToSlotIndices.computeIfAbsent(date, k -> new ArrayList<>()).add(i);
        }
        this.orderedDates = new ArrayList<>(dateSet);
        Collections.sort(this.orderedDates);
    }

    /**
     * Count hard constraint violations.
     * A violation occurs when two clashing exams are assigned to the same slot.
     * 
     * @param chromosome chromosome[examIndex] = slotIndex
     * @return number of hard violations (0 = clash-free)
     */
    public int countHardViolations(int[] chromosome) {
        int violations = 0;
        int size = conflictMatrix.getSize();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                // If these exams clash AND are in the same slot
                if (conflictMatrix.hasClash(i, j) && chromosome[i] == chromosome[j]) {
                    violations++;
                }
            }
        }
        return violations;
    }

    /**
     * Calculate total fitness score (lower = better).
     * fitness = 1000 * hard_violations + 10 * back_to_back + 5 * same_day + 1 * consecutive_day
     */
    public int calculateFitness(int[] chromosome) {
        int hardViolations = countHardViolations(chromosome);
        int[] fatigue = calculateFatigueBreakdown(chromosome);

        return HARD_WEIGHT * hardViolations
             + BACK_TO_BACK_WEIGHT * fatigue[0]
             + SAME_DAY_WEIGHT * fatigue[1]
             + CONSECUTIVE_DAY_WEIGHT * fatigue[2];
    }

    /**
     * Calculate fatigue breakdown: [back_to_back, same_day, consecutive_day].
     * 
     * For each pair of clashing exams, check their assigned slots:
     * - Same date, consecutive sessions (Morning→Afternoon) = back-to-back (10)
     * - Same date, any two sessions = same-day (5)  
     * - Consecutive dates = consecutive-day (1)
     */
    public int[] calculateFatigueBreakdown(int[] chromosome) {
        int backToBack = 0;
        int sameDay = 0;
        int consecutiveDay = 0;
        int size = conflictMatrix.getSize();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (!conflictMatrix.hasClash(i, j)) continue;

                int slotA = chromosome[i];
                int slotB = chromosome[j];
                if (slotA == slotB) continue; // hard violation, counted separately

                Timeslot tsA = timeslots.get(slotA);
                Timeslot tsB = timeslots.get(slotB);

                if (tsA.date.equals(tsB.date)) {
                    // Same day — check if back-to-back (Morning + Afternoon)
                    sameDay++;
                    if (isBackToBack(tsA, tsB)) {
                        backToBack++;
                    }
                } else if (areConsecutiveDays(tsA.date, tsB.date)) {
                    consecutiveDay++;
                }
            }
        }

        return new int[]{backToBack, sameDay, consecutiveDay};
    }

    /**
     * Check if two timeslots on the same date are back-to-back
     * (one is Morning and the other is Afternoon).
     */
    private boolean isBackToBack(Timeslot a, Timeslot b) {
        return (a.session.equals("Morning") && b.session.equals("Afternoon"))
            || (a.session.equals("Afternoon") && b.session.equals("Morning"));
    }

    /**
     * Check if two dates are consecutive calendar days.
     * Simple check: dates are in format "2026-08-20", parse and compare.
     */
    private boolean areConsecutiveDays(String dateA, String dateB) {
        int indexA = orderedDates.indexOf(dateA);
        int indexB = orderedDates.indexOf(dateB);
        if (indexA < 0 || indexB < 0) return false;
        return Math.abs(indexA - indexB) == 1;
    }

    /**
     * Get total fatigue penalty (without hard violations).
     */
    public int getTotalFatiguePenalty(int[] chromosome) {
        int[] fatigue = calculateFatigueBreakdown(chromosome);
        return BACK_TO_BACK_WEIGHT * fatigue[0]
             + SAME_DAY_WEIGHT * fatigue[1]
             + CONSECUTIVE_DAY_WEIGHT * fatigue[2];
    }

    /**
     * Calculate soft constraint satisfaction percentage.
     * 100% = zero fatigue penalties. Based on max possible penalties.
     */
    public double calculateSatisfactionPercentage(int[] chromosome) {
        int[] fatigue = calculateFatigueBreakdown(chromosome);
        int totalPenalty = BACK_TO_BACK_WEIGHT * fatigue[0]
                         + SAME_DAY_WEIGHT * fatigue[1]
                         + CONSECUTIVE_DAY_WEIGHT * fatigue[2];

        // A same-day back-to-back pair receives both soft penalties.
        int maxPenalty = conflictMatrix.getTotalConflictEdges()
                * (BACK_TO_BACK_WEIGHT + SAME_DAY_WEIGHT);
        if (maxPenalty == 0) return 100.0;

        double satisfaction = 1.0 - (double) totalPenalty / maxPenalty;
        return Math.round(Math.max(0.0, satisfaction) * 1000.0) / 10.0;
    }
}