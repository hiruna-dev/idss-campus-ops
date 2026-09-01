package com.idss.task2.algorithm;

import com.idss.common.model.Invigilator;
import com.idss.task2.model.MasterScheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostMatrixBuilder {


    //cost constants
    public static final int HARD_VIOLATION = 10000;
    public static final int SHIFT_LIMIT_VIOLATION = 5000;
    public static final int FLOOR_MISMATCH = 50;
    public static final int SAME_DAY_STACK = 10;
    public static final int FAIRNESS_LOAD = 1;
    public static final int DUMMY_ROW_COST = 1_000_000;

    public static final class CostMatrix {
        public final int[][] cost;
        public final List<String> rowInvigilatorIds;
        public final List<String> columnExamIds;
        public final int size;

        CostMatrix(int[][] cost, List<String> rowInvigilatorIds, List<String> columnExamIds, int size) {
            this.cost = cost;
            this.rowInvigilatorIds = rowInvigilatorIds;
            this.columnExamIds = columnExamIds;
            this.size = size;
        }
    }

    public CostMatrix build(List<MasterScheduleEntry> exams, List<Invigilator> invigilators) {
        return build(exams, invigilators, Map.of(), Map.of());
    }

    public CostMatrix build(List<MasterScheduleEntry> exams, List<Invigilator> invigilators,
                            Map<String, Integer> priorTotalShifts,
                            Map<String, Integer> priorShiftsByDay) {
        //extracting invigilators and exams
        Map<String, Invigilator> invigilatorById = new HashMap<>();
        for (Invigilator inv : invigilators) {
            invigilatorById.put(inv.invigilator_id, inv);
        }
        Map<String, MasterScheduleEntry> examById = new HashMap<>();
        for (MasterScheduleEntry e : exams) {
            examById.put(e.exam_id, e);
        }

        //extracting to columns and rows
        List<String> columnExamIds = new ArrayList<>();
        for (MasterScheduleEntry exam : exams) {
            int slots = Math.max(1, exam.required_invigilators);
            for (int s = 0; s < slots; s++) {
                columnExamIds.add(exam.exam_id);
            }
        }

        //replicate each invigilator max_total_shifts times so one invigilator can match multiple slots
        List<String> rowInvigilatorIds = new ArrayList<>();
        for (Invigilator inv : invigilators) {
            int replicas = Math.max(1, inv.max_total_shifts);
            for (int r = 0; r < replicas; r++) {
                rowInvigilatorIds.add(inv.invigilator_id);
            }
        }

        //caclulating size
        int realRows = rowInvigilatorIds.size();
        int realCols = columnExamIds.size();
        int size = Math.max(realRows, realCols);
        if (size == 0) size = 1;

        //filling this with null because we need square for hungarian algo
        while (rowInvigilatorIds.size() < size) rowInvigilatorIds.add(null);
        while (columnExamIds.size() < size) columnExamIds.add(null);

        //creating cost matrix
        int[][] cost = new int[size][size];

        //inserting data into the matrix
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                String invId = rowInvigilatorIds.get(i);
                String examId = columnExamIds.get(j);
                if (invId == null) {
                    cost[i][j] = DUMMY_ROW_COST;
                } else if (examId == null) {
                    cost[i][j] = 0;
                } else {
                    cost[i][j] = cellCost(
                            invigilatorById.get(invId),
                            examById.get(examId),
                            priorTotalShifts,
                            priorShiftsByDay);
                }
            }
        }

        return new CostMatrix(cost, rowInvigilatorIds, columnExamIds, size);
    }

    private int cellCost(Invigilator inv, MasterScheduleEntry exam,
                         Map<String, Integer> priorTotalShifts,
                         Map<String, Integer> priorShiftsByDay) {
        int cost = 0;

        //checking for restricted courses
        if (inv.restricted_courses != null && inv.restricted_courses.contains(exam.course_code)) {
            cost += HARD_VIOLATION;
        }

        //checking for invigi unavailablitiy
        if (inv.unavailability != null) {
            for (Invigilator.Unavailability u : inv.unavailability) {
                if (u.date.equals(exam.date) && u.session.equals(exam.session)) {
                    cost += HARD_VIOLATION;
                    break;
                }
            }
        }

        //checking for invigi shift limits
        String dayKey = inv.invigilator_id + "|" + exam.date;
        int shiftsToday = priorShiftsByDay.getOrDefault(dayKey, 0);
        int totalShifts = priorTotalShifts.getOrDefault(inv.invigilator_id, 0);

        if (shiftsToday >= inv.max_shifts_per_day) {
            cost += SHIFT_LIMIT_VIOLATION;
        }
        if (totalShifts >= inv.max_total_shifts) {
            cost += SHIFT_LIMIT_VIOLATION;
        }

        if (inv.preferred_floors != null && !inv.preferred_floors.contains(exam.floor)) {
            cost += FLOOR_MISMATCH;
        }

        //soft violations
        cost += SAME_DAY_STACK * shiftsToday;
        cost += FAIRNESS_LOAD * totalShifts;

        return cost;
    }
}
