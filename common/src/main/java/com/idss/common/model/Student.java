package com.idss.common.model;

import java.util.List;

/**
 * Student enrollment entity (group_data_contracts.md Section 3).
 * Registry source; consumed by Task 3 (clash detection) and Task 5 (timetable).
 */
public class Student {
    public String student_id;
    public String name;
    public int year;
    public List<String> enrolled_courses;
}
