package com.idss.task2.algorithm;

import com.idss.common.model.Invigilator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//calculates fairness metrics across the final roster
public class FairnessCalculator {

    //full fairness breakdown. only fairnessVariance is serialized to json because of pre agreed upon data contracts
    public static final class FairnessResult {
        public final double fairnessVariance;
        public final int minShifts;
        public final int maxShifts;
        public final double meanShifts;
        public final double floorPreferenceSatisfactionRate;

        FairnessResult(double fairnessVariance, int minShifts, int maxShifts,
                       double meanShifts, double floorPreferenceSatisfactionRate) {
            this.fairnessVariance = fairnessVariance;
            this.minShifts = minShifts;
            this.maxShifts = maxShifts;
            this.meanShifts = meanShifts;
            this.floorPreferenceSatisfactionRate = floorPreferenceSatisfactionRate;
        }
    }

    public FairnessResult calculate(List<Assignment> assignments,
                                    List<Invigilator> invigilators) {
        Map<String, Integer> shiftsPerInvigilator = new HashMap<>();
        for (Invigilator inv : invigilators) {
            shiftsPerInvigilator.put(inv.invigilator_id, 0);
        }
        for (Assignment a : assignments) {
            shiftsPerInvigilator.merge(a.inv.invigilator_id, 1, Integer::sum);
        }

        int n = invigilators.size();
        if (n == 0) {
            return new FairnessResult(0.0, 0, 0, 0.0, 0.0);
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sum = 0;
        for (int shifts : shiftsPerInvigilator.values()) {
            min = Math.min(min, shifts);
            max = Math.max(max, shifts);
            sum += shifts;
        }
        if (min == Integer.MAX_VALUE) min = 0;
        if (max == Integer.MIN_VALUE) max = 0;
        double mean = (double) sum / n;

        double sumSq = 0.0;
        for (int shifts : shiftsPerInvigilator.values()) {
            double diff = shifts - mean;
            sumSq += diff * diff;
        }
        double variance = sumSq / n;

        //floor pref satisfaction: percentage of assignments where the exam floor is in the invigi's preferred_floors
        int floorMatches = 0;
        int totalAssignments = assignments.size();
        for (Assignment a : assignments) {
            if (a.inv.preferred_floors != null
                    && a.inv.preferred_floors.contains(a.exam.floor)) {
                floorMatches++;
            }
        }
        double floorRate = totalAssignments == 0
                ? 0.0
                : (double) floorMatches / totalAssignments;

        return new FairnessResult(variance, min, max, mean, floorRate);
    }
}
