package com.idss.task4.algorithm;

import com.idss.common.model.Room;
import com.idss.task4.dto.ExamRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hard-constraint filter for exam room eligibility
 * (task_4_plan.md Section 7).
 *
 * <p>Two hard filters are applied sequentially:
 * <ol>
 *   <li><b>Capacity:</b> {@code room.capacity >= exam.studentCount}</li>
 *   <li><b>Accessibility (conditional):</b> if
 *       {@code exam.requiresAccessibility == true}, rooms with
 *       {@code is_accessible == false} are excluded outright.</li>
 * </ol>
 *
 * <p>Rooms that survive both filters are eligible for TOPSIS scoring.</p>
 *
 * <p><b>Time complexity:</b> O(n) where n = total rooms in registry —
 * single pass, constant work per room.</p>
 */
public class FilterEngine {

    /**
     * Filters the full set of rooms against an exam request's hard constraints.
     *
     * @param allRooms  all rooms from the RoomRegistry
     * @param exam      the exam request with student_count and requires_accessibility
     * @return list of rooms that pass both hard constraints (may be empty)
     */
    public List<Room> filter(Collection<Room> allRooms, ExamRequest exam) {
        List<Room> eligible = new ArrayList<>();

        for (Room room : allRooms) {
            // Hard filter 1: capacity must accommodate all students
            if (room.capacity < exam.getStudentCount()) {
                continue;
            }

            // Hard filter 2: accessibility (conditional on the exam's requirement)
            if (exam.isRequiresAccessibility() && !room.is_accessible) {
                continue;
            }

            eligible.add(room);
        }

        return eligible;
    }
}
