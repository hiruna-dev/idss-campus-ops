package com.idss.task2;

import com.idss.common.model.Invigilator;
import com.idss.task2.algorithm.Assignment;
import com.idss.task2.algorithm.ConstraintValidator;
import com.idss.task2.model.MasterScheduleEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//verifies ConstraintValidator catches each hard constraint violation 
public class ConstraintValidatorTest {

    private final ConstraintValidator validator = new ConstraintValidator();

    private MasterScheduleEntry exam(String id, String course, String date, String session, int floor) {
        MasterScheduleEntry e = new MasterScheduleEntry();
        e.exam_id = id;
        e.course_code = course;
        e.date = date;
        e.session = session;
        e.floor = floor;
        e.required_invigilators = 1;
        return e;
    }

    private Invigilator inv(String id, int maxPerDay, int maxTotal,
                            List<String> restricted, List<Integer> floors,
                            List<Invigilator.Unavailability> unavail) {
        Invigilator inv = new Invigilator();
        inv.invigilator_id = id;
        inv.name = "Test " + id;
        inv.max_shifts_per_day = maxPerDay;
        inv.max_total_shifts = maxTotal;
        inv.restricted_courses = restricted;
        inv.preferred_floors = floors;
        inv.unavailability = unavail;
        return inv;
    }

    @Test
    void cleanAssignmentHasNoViolations() {
        Invigilator inv = inv("INV_01", 2, 8, List.of(), List.of(1), List.of());
        MasterScheduleEntry e = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1);

        ConstraintValidator.ConstraintResult result =
                validator.validate(List.of(new Assignment(inv, e)));

        assertEquals(0, result.violations);
        assertTrue(result.violationDescriptions.isEmpty());
    }

    @Test
    void restrictedCourseViolationIsCaught() {
        Invigilator inv = inv("INV_01", 2, 8, List.of("PDSA201"), List.of(1), List.of());
        MasterScheduleEntry e = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1);

        ConstraintValidator.ConstraintResult result =
                validator.validate(List.of(new Assignment(inv, e)));

        assertEquals(1, result.violations);
        assertTrue(result.violationDescriptions.get(0).contains("restricted"));
    }

    @Test
    void unavailabilityViolationIsCaught() {
        Invigilator.Unavailability u = new Invigilator.Unavailability();
        u.date = "2026-08-20";
        u.session = "Morning";
        Invigilator inv = inv("INV_01", 2, 8, List.of(), List.of(1), List.of(u));
        MasterScheduleEntry e = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1);

        ConstraintValidator.ConstraintResult result =
                validator.validate(List.of(new Assignment(inv, e)));

        assertEquals(1, result.violations);
        assertTrue(result.violationDescriptions.get(0).contains("unavailable"));
    }

    @Test
    void maxShiftsPerDayExceededIsCaught() {
        Invigilator inv = inv("INV_01", 1, 8, List.of(), List.of(1), List.of());
        MasterScheduleEntry e1 = exam("EX_1", "C1", "2026-08-20", "Morning", 1);
        MasterScheduleEntry e2 = exam("EX_2", "C2", "2026-08-20", "Afternoon", 1);

        //max_shifts_per_day=1, assigning 2 on the same date
        ConstraintValidator.ConstraintResult result =
                validator.validate(List.of(new Assignment(inv, e1), new Assignment(inv, e2)));

        assertEquals(1, result.violations);
        assertTrue(result.violationDescriptions.get(0).contains("max_shifts_per_day"));
    }

    @Test
    void maxTotalShiftsExceededIsCaught() {
        Invigilator inv = inv("INV_01", 5, 1, List.of(), List.of(1), List.of());
        MasterScheduleEntry e1 = exam("EX_1", "C1", "2026-08-20", "Morning", 1);
        MasterScheduleEntry e2 = exam("EX_2", "C2", "2026-08-21", "Morning", 1);

        //max_total_shifts=1, assigning 2 total
        ConstraintValidator.ConstraintResult result =
                validator.validate(List.of(new Assignment(inv, e1), new Assignment(inv, e2)));

        assertEquals(1, result.violations);
        assertTrue(result.violationDescriptions.get(0).contains("max_total_shifts"));
    }
}
