package com.idss.task2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Task 5's persisted schedule entries carry a Mongo-generated "id" field this model
// doesn't need — ignore unknown fields rather than fail deserialization on it.
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasterScheduleEntry {
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
}
