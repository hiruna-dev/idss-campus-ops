package com.idss.task5.dto;

import com.idss.common.model.Exam;
import com.idss.common.model.Student;
import com.idss.common.model.Timeslot;

import java.util.List;

/**
 * Optional request payload for timetable generation.
 * An empty object keeps the existing local-file development mode.
 */
public class TimetableRequest {
    public List<Exam> exams;
    public List<Student> students;
    public List<Timeslot> timeslots;
    public List<RankedRoom> room_rankings;
    public List<RoomReference> room_references;

    public boolean hasData() {
        return exams != null
                || students != null
                || timeslots != null
                || room_rankings != null
                || room_references != null;
    }
}
