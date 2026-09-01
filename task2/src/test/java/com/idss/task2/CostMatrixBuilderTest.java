package com.idss.task2;

import com.idss.common.model.Invigilator;
import com.idss.task2.algorithm.CostMatrixBuilder;
import com.idss.task2.model.MasterScheduleEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CostMatrixBuilderTest {

    private final CostMatrixBuilder builder = new CostMatrixBuilder();

    @Test
    void buildsValidMatrixFromSampleData() {
        MasterScheduleEntry exam = new MasterScheduleEntry();
        exam.exam_id = "EX_101";
        exam.course_code = "PDSA201";
        exam.date = "2026-08-20";
        exam.session = "Morning";
        exam.floor = 1;
        exam.required_invigilators = 2;

        Invigilator inv = new Invigilator();
        inv.invigilator_id = "INV_01";
        inv.name = "Dr. Test";
        inv.max_shifts_per_day = 2;
        inv.max_total_shifts = 8;
        inv.restricted_courses = List.of("PDSA201");
        inv.preferred_floors = List.of(1, 2);
        inv.unavailability = List.of();

        CostMatrixBuilder.CostMatrix matrix = builder.build(List.of(exam), List.of(inv));

        //inv has max_total_shifts=8 so 8 replicated rows, 2 columns → size=8
        assertEquals(8, matrix.size);
        //columnExamIds is padded to size=8 with nulls, but only 2 real exam slots
        assertEquals(2, matrix.columnExamIds.stream().filter("EX_101"::equals).count());
        assertEquals("EX_101", matrix.columnExamIds.get(0));
        assertEquals("EX_101", matrix.columnExamIds.get(1));
        assertEquals("INV_01", matrix.rowInvigilatorIds.get(0));
        //all 8 rows should reference INV_01 (row replication)
        for (int i = 0; i < 8; i++) {
            assertEquals("INV_01", matrix.rowInvigilatorIds.get(i));
        }
        assertTrue(matrix.cost[0][0] >= CostMatrixBuilder.HARD_VIOLATION);
        assertTrue(matrix.cost[0][1] >= CostMatrixBuilder.HARD_VIOLATION);
    }

    @Test
    void expandsColumnsByRequiredInvigilators() {
        MasterScheduleEntry exam = new MasterScheduleEntry();
        exam.exam_id = "EX_102";
        exam.course_code = "NET102";
        exam.date = "2026-08-20";
        exam.session = "Morning";
        exam.floor = 1;
        exam.required_invigilators = 3;

        Invigilator inv1 = new Invigilator();
        inv1.invigilator_id = "INV_01";
        inv1.max_shifts_per_day = 3;
        inv1.max_total_shifts = 9;
        inv1.restricted_courses = List.of();
        inv1.preferred_floors = List.of(1);
        inv1.unavailability = List.of();

        Invigilator inv2 = new Invigilator();
        inv2.invigilator_id = "INV_02";
        inv2.max_shifts_per_day = 3;
        inv2.max_total_shifts = 9;
        inv2.restricted_courses = List.of();
        inv2.preferred_floors = List.of(1);
        inv2.unavailability = List.of();

        CostMatrixBuilder.CostMatrix matrix = builder.build(List.of(exam), List.of(inv1, inv2));

        //2 invigilators with max_total_shifts=9 each → 18 replicated rows, 3 columns → size=18
        assertEquals(18, matrix.size);
        assertEquals(3, matrix.columnExamIds.stream().filter("EX_102"::equals).count());
        //first 9 rows are INV_01 replicas, next 9 are INV_02 replicas
        assertEquals("INV_01", matrix.rowInvigilatorIds.get(0));
        assertEquals("INV_02", matrix.rowInvigilatorIds.get(9));
    }

    @Test
    void floorMismatchAddsPenalty() {
        MasterScheduleEntry exam = new MasterScheduleEntry();
        exam.exam_id = "EX_103";
        exam.course_code = "DMS201";
        exam.date = "2026-08-20";
        exam.session = "Morning";
        exam.floor = 3;
        exam.required_invigilators = 1;

        Invigilator inv = new Invigilator();
        inv.invigilator_id = "INV_01";
        inv.max_shifts_per_day = 2;
        inv.max_total_shifts = 8;
        inv.restricted_courses = List.of();
        inv.preferred_floors = List.of(1, 2);
        inv.unavailability = List.of();

        CostMatrixBuilder.CostMatrix matrix = builder.build(List.of(exam), List.of(inv));

        assertEquals(CostMatrixBuilder.FLOOR_MISMATCH, matrix.cost[0][0]);
    }

    @Test
    void handlesNullOptionalFields() {
        MasterScheduleEntry exam = new MasterScheduleEntry();
        exam.exam_id = "EX_104";
        exam.course_code = "OOP101";
        exam.date = "2026-08-20";
        exam.session = "Morning";
        exam.floor = 1;
        exam.required_invigilators = 1;

        Invigilator inv = new Invigilator();
        inv.invigilator_id = "INV_01";
        inv.max_shifts_per_day = 2;
        inv.max_total_shifts = 8;
        inv.restricted_courses = null;
        inv.preferred_floors = null;
        inv.unavailability = null;

        CostMatrixBuilder.CostMatrix matrix = builder.build(List.of(exam), List.of(inv));

        assertEquals(0, matrix.cost[0][0]);
    }

    @Test
    void sameDayStackPenaltyAccumulatesWithPriorShifts() {
        //2 exams same date, different sessions
        MasterScheduleEntry e1 = new MasterScheduleEntry();
        e1.exam_id = "EX_1";
        e1.course_code = "C1";
        e1.date = "2026-08-20";
        e1.session = "Morning";
        e1.floor = 1;
        e1.required_invigilators = 1;

        MasterScheduleEntry e2 = new MasterScheduleEntry();
        e2.exam_id = "EX_2";
        e2.course_code = "C2";
        e2.date = "2026-08-20";
        e2.session = "Afternoon";
        e2.floor = 1;
        e2.required_invigilators = 1;

        //1 invigilator who can do 2 shifts
        Invigilator inv = new Invigilator();
        inv.invigilator_id = "INV_01";
        inv.name = "Test";
        inv.max_shifts_per_day = 2;
        inv.max_total_shifts = 2;
        inv.restricted_courses = List.of();
        inv.preferred_floors = List.of(1);
        inv.unavailability = List.of();

        //first iteration: no prior shifts, no stacking penalty
        CostMatrixBuilder.CostMatrix m1 = builder.build(
                List.of(e1, e2), List.of(inv), Map.of(), Map.of());
        //inv has 2 replicas (max_total_shifts=2), 2 slots → 2x2 matrix
        //no prior shifts so cost should be 0 for both
        assertEquals(0, m1.cost[0][0]); //replica 1 → exam 1
        assertEquals(0, m1.cost[1][1]); //replica 2 → exam 2

        //second iteration: simulate 1 prior shift on 2026-08-20
        Map<String, Integer> priorShiftsByDay = Map.of("INV_01|2026-08-20", 1);
        Map<String, Integer> priorTotalShifts = Map.of("INV_01", 1);
        CostMatrixBuilder.CostMatrix m2 = builder.build(
                List.of(e1, e2), List.of(inv), priorTotalShifts, priorShiftsByDay);
        //now same-day stacking penalty (+10) and fairness load (+1) should be visible
        assertTrue(m2.cost[0][0] >= CostMatrixBuilder.SAME_DAY_STACK,
                "expected +10 stacking penalty on second iteration");
        assertTrue(m2.cost[1][1] >= CostMatrixBuilder.SAME_DAY_STACK,
                "expected +10 stacking penalty on second iteration");
    }
}
