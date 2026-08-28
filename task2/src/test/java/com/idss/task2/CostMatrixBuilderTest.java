package com.idss.task2;

import com.idss.common.model.Invigilator;
import com.idss.task2.algorithm.CostMatrixBuilder;
import com.idss.task2.model.MasterScheduleEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        assertEquals(2, matrix.size);
        assertEquals(2, matrix.columnExamIds.size());
        assertEquals("EX_101", matrix.columnExamIds.get(0));
        assertEquals("EX_101", matrix.columnExamIds.get(1));
        assertEquals("INV_01", matrix.rowInvigilatorIds.get(0));
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

        assertEquals(3, matrix.size);
        assertEquals(3, matrix.columnExamIds.stream().filter("EX_102"::equals).count());
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
}
