package com.idss.task5.service;

import com.idss.common.model.Exam;
import com.idss.common.model.Student;
import com.idss.common.model.Timeslot;
import com.idss.task5.dto.MasterScheduleEntry;
import com.idss.task5.dto.RankedRoom;
import com.idss.task5.dto.RoomReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TimetableServiceTest {

    @Test
    void assignsDifferentEligibleRoomsWithinOneSlot() {
        Exam first = exam("EX_1", "COURSE_1");
        Exam second = exam("EX_2", "COURSE_2");

        Timeslot slot = new Timeslot();
        slot.slot_id = "SLOT_1";
        slot.date = "2026-08-20";
        slot.session = "Morning";
        slot.start_time = "09:00";
        slot.end_time = "12:00";
        slot.max_exams_parallel = 2;

        RankedRoom firstRoom = rankedRoom("EX_1", "R101");
        RankedRoom secondRoom = rankedRoom("EX_2", "R102");
        RoomReference firstReference = roomReference("R101");
        RoomReference secondReference = roomReference("R102");

        Map<String, Object> response = new TimetableService(null).generateTimetable(
                List.of(first, second),
                List.of(new Student()),
                List.of(slot),
                List.of(firstRoom, secondRoom),
                List.of(firstReference, secondReference),
                "GREEDY");

        @SuppressWarnings("unchecked")
        List<MasterScheduleEntry> schedule = (List<MasterScheduleEntry>) response.get("schedule");
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) response.get("metrics");

        assertEquals("OPTIMAL", response.get("status"));
        assertEquals("R101", schedule.get(0).room_id);
        assertEquals("R102", schedule.get(1).room_id);
        assertEquals(0, metrics.get("resource_constraint_violations"));
    }

    private static Exam exam(String examId, String courseCode) {
        Exam exam = new Exam();
        exam.exam_id = examId;
        exam.course_code = courseCode;
        exam.course_title = courseCode;
        exam.student_count = 10;
        return exam;
    }

    private static RankedRoom rankedRoom(String examId, String roomId) {
        RankedRoom room = new RankedRoom();
        room.exam_id = examId;
        room.room_id = roomId;
        room.rank = 1;
        room.meets_hard_constraints = true;
        return room;
    }

    private static RoomReference roomReference(String roomId) {
        RoomReference reference = new RoomReference();
        reference.room_id = roomId;
        reference.floor = 1;
        reference.is_accessible = true;
        return reference;
    }
}
