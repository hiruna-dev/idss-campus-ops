package com.idss.common.model;

/**
 * Timeslot entity (group_data_contracts.md Section 3).
 * Registry source; consumed by Task 5. {@code session} is "Morning" or
 * "Afternoon" (group_data_contracts.md Section 1).
 */
public class Timeslot {
    public String slot_id;
    public String date;
    public String session;
    public String start_time;
    public String end_time;
    public int max_exams_parallel;
}
