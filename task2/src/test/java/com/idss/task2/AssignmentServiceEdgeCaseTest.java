package com.idss.task2;

import com.idss.common.model.Invigilator;
import com.idss.task2.model.MasterScheduleEntry;
import com.idss.task2.model.ProctorRoster;
import com.idss.task2.repository.ProctorRosterRepository;
import com.idss.task2.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//edge case tests for AssignmentService (Day 5 of the task2 implementation plan)
public class AssignmentServiceEdgeCaseTest {

    private AssignmentService service;

    @BeforeEach
    void setUp() {
        ProctorRosterRepository repo = mock(ProctorRosterRepository.class);
        when(repo.save(org.mockito.ArgumentMatchers.any(ProctorRoster.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        service = new AssignmentService(repo);
    }

    private MasterScheduleEntry exam(String id, String course, String date,
                                     String session, int floor, int required) {
        MasterScheduleEntry e = new MasterScheduleEntry();
        e.exam_id = id;
        e.course_code = course;
        e.date = date;
        e.session = session;
        e.floor = floor;
        e.required_invigilators = required;
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
    void emptyScheduleReturnsFeasibleEmptyRoster() {
        ProctorRoster roster = service.assignInvigilators(List.of(),
                List.of(inv("INV_01", 2, 8, List.of(), List.of(1), List.of())));

        assertEquals("FEASIBLE", roster.status);
        assertTrue(roster.roster.isEmpty());
        assertEquals(0, roster.total_shifts_allocated);
        assertNull(roster.reason);
    }

    @Test
    void insufficientCapacityReturnsInfeasible() {
        //3 exams needing 2 each = 6 total shifts, but only 1 invigilator with max_total_shifts=2
        MasterScheduleEntry e1 = exam("EX_1", "C1", "2026-08-20", "Morning", 1, 2);
        MasterScheduleEntry e2 = exam("EX_2", "C2", "2026-08-20", "Morning", 1, 2);
        MasterScheduleEntry e3 = exam("EX_3", "C3", "2026-08-20", "Morning", 1, 2);
        Invigilator inv = inv("INV_01", 5, 2, List.of(), List.of(1), List.of());

        ProctorRoster roster = service.assignInvigilators(List.of(e1, e2, e3), List.of(inv));

        assertEquals("INFEASIBLE", roster.status);
        assertTrue(roster.reason.contains("Insufficient"));
        assertNotNull(roster.suggested_remedy);
        assertTrue(roster.roster.isEmpty());
    }

    @Test
    void allRestrictedReturnsInfeasibleWithUnderStaffedExam() {
        //both invigilators restricted from PDSA201 so the exam gets 0 invigilators
        MasterScheduleEntry exam = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1, 1);
        Invigilator inv1 = inv("INV_01", 2, 8, List.of("PDSA201"), List.of(1), List.of());
        Invigilator inv2 = inv("INV_02", 2, 8, List.of("PDSA201"), List.of(1), List.of());

        ProctorRoster roster = service.assignInvigilators(List.of(exam), List.of(inv1, inv2));

        assertEquals("INFEASIBLE", roster.status);
        assertTrue(roster.reason.contains("EX_1"));
        assertEquals(1, roster.roster.size());
        assertTrue(roster.roster.get(0).assigned_invigilators.isEmpty());
    }

    @Test
    void singleExamSingleInvigilatorAssignsCorrectly() {
        MasterScheduleEntry exam = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1, 1);
        Invigilator inv = inv("INV_01", 2, 8, List.of(), List.of(1), List.of());

        ProctorRoster roster = service.assignInvigilators(List.of(exam), List.of(inv));

        assertEquals("OPTIMAL", roster.status);
        assertEquals(1, roster.roster.size());
        assertEquals(1, roster.roster.get(0).assigned_invigilators.size());
        assertTrue(roster.roster.get(0).assigned_invigilators.get(0).is_lead_invigilator);
    }

    @Test
    void nullOptionalFieldsDoesNotNpe() {
        MasterScheduleEntry exam = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1, 1);
        Invigilator inv = inv("INV_01", 2, 8, null, null, null);

        ProctorRoster roster = service.assignInvigilators(List.of(exam), List.of(inv));

        assertEquals("OPTIMAL", roster.status);
        assertEquals(1, roster.roster.get(0).assigned_invigilators.size());
    }

    @Test
    void duplicateExamIdsProcessedOnce() {
        MasterScheduleEntry exam1 = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1, 1);
        MasterScheduleEntry exam1Dup = exam("EX_1", "PDSA201", "2026-08-20", "Morning", 1, 1);
        Invigilator inv = inv("INV_01", 2, 8, List.of(), List.of(1), List.of());

        ProctorRoster roster = service.assignInvigilators(List.of(exam1, exam1Dup), List.of(inv));

        //only one roster entry for EX_1 (duplicates collapsed via putIfAbsent)
        assertEquals(1, roster.roster.size());
        assertEquals("EX_1", roster.roster.get(0).exam_id);
    }

    @Test
    void sameDayExamsAssignedToOneInvigilatorWhenCapacityAllows() {
        //2 exams same date, different sessions. 1 invigilator who can do 2 shifts
        MasterScheduleEntry e1 = exam("EX_1", "C1", "2026-08-20", "Morning", 1, 1);
        MasterScheduleEntry e2 = exam("EX_2", "C2", "2026-08-20", "Afternoon", 1, 1);
        Invigilator inv = inv("INV_01", 2, 2, List.of(), List.of(1), List.of());

        ProctorRoster roster = service.assignInvigilators(List.of(e1, e2), List.of(inv));

        assertEquals("OPTIMAL", roster.status);
        //both exams should be assigned to INV_01 (row replication allows multiple slots per invigilator)
        assertEquals("INV_01", roster.roster.get(0).assigned_invigilators.get(0).invigilator_id);
        assertEquals("INV_01", roster.roster.get(1).assigned_invigilators.get(0).invigilator_id);
    }
}
