package com.idss.common.model;

/**
 * Exam entity (group_data_contracts.md Section 3).
 * Consumed by Task 3, Task 4, Task 5. Field names match the canonical JSON
 * contract byte-for-byte; do not rename.
 */
public class Exam {
    public String exam_id;
    public String course_code;
    public String course_title;
    public int duration_hours;
    public int student_count;
    public int year;
    public String department;
    public boolean requires_accessibility;
}
