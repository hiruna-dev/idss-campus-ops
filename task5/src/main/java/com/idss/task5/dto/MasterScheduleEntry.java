package com.idss.task5.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

/**
 * Master schedule entry — primary output of Task 5.
 * BYTE-IDENTICAL to Task 2's input_master_schedule.json.
 * Stored in MongoDB collection: master_schedules.
 * 
 * Indexes:
 * - exam_id (unique)
 * - compound date+session+room_id (unique, prevents double-booking)
 */
@Document(collection = "master_schedules")
@CompoundIndexes({
    @CompoundIndex(name = "date_session_room", def = "{'date': 1, 'session': 1, 'room_id': 1}", unique = true)
})
public class MasterScheduleEntry {

    @Id
    public String id;

    @Indexed(unique = true)
    public String exam_id;

    public String course_code;
    public String course_title;
    public String date;
    public String session;
    public String start_time;
    public String end_time;
    public String room_id;
    public String canonical_room_id;
    public int floor;
    public int allocated_students;
    public int required_invigilators;
    public boolean requires_accessibility;
    public boolean requires_step_free_access;

    /**
     * Derivation: required_invigilators = max(1, ceil(students/30))
     */
    public static int calculateInvigilators(int studentCount) {
        return Math.max(1, (int) Math.ceil(studentCount / 30.0));
    }
}