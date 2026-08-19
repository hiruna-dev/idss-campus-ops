package com.idss.common.model;

import java.util.List;

/**
 * Invigilator entity (group_data_contracts.md Section 4, Task 2 input).
 * Field names match the canonical JSON contract byte-for-byte.
 */
public class Invigilator {
    public String invigilator_id;
    public String name;
    public int max_shifts_per_day;
    public int max_total_shifts;
    public List<String> restricted_courses;
    public List<Integer> preferred_floors;
    public List<Unavailability> unavailability;

    /** A single unavailable date+session slot for an invigilator. */
    public static class Unavailability {
        public String date;
        public String session;
    }
}
