package com.idss.task1.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idss.common.config.Canonical;
import com.idss.task1.model.DispatchOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Derives Task 1 dispatch orders from Task 5 master schedule output.
 */
public final class DispatchMapper {

    private DispatchMapper() {
    }

    public static List<DispatchOrder> fromSchedule(List<ScheduleSource> schedule) {
        List<DispatchOrder> orders = new ArrayList<>();
        if (schedule == null) {
            return orders;
        }

        int counter = 1;
        for (ScheduleSource entry : schedule) {
            if (entry.examId == null || entry.examId.isBlank()) {
                continue;
            }

            String dispatchId = "DSP_" + String.format("%03d", counter++);
            String destinationRoom = entry.canonicalRoomId != null && !entry.canonicalRoomId.isBlank()
                    ? entry.canonicalRoomId
                    : Canonical.toAliasedRoomId(entry.roomId);

            double packageWeight = entry.allocatedStudents > 30 ? 18.5 : 4.5;
            String transportMode = entry.allocatedStudents > 30 ? "TROLLEY" : "FOOT_COURIER";

            orders.add(new DispatchOrder(
                    dispatchId,
                    entry.examId,
                    entry.courseCode,
                    entry.courseTitle,
                    entry.date,
                    entry.startTime,
                    "VAULT_G01",
                    destinationRoom,
                    entry.floor,
                    packageWeight,
                    transportMode,
                    entry.requiresStepFreeAccess,
                    3,
                    300
            ));
        }
        return orders;
    }

    /** Minimal mirror of Task 5 output_master_schedule.json entries. */
    public static class ScheduleSource {
        @JsonProperty("exam_id")
        public String examId;

        @JsonProperty("course_code")
        public String courseCode;

        @JsonProperty("course_title")
        public String courseTitle;

        @JsonProperty("date")
        public String date;

        @JsonProperty("start_time")
        public String startTime;

        @JsonProperty("room_id")
        public String roomId;

        @JsonProperty("canonical_room_id")
        public String canonicalRoomId;

        @JsonProperty("floor")
        public int floor;

        @JsonProperty("allocated_students")
        public int allocatedStudents;

        @JsonProperty("requires_step_free_access")
        public boolean requiresStepFreeAccess;
    }
}
