package com.idss.task3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StudentEnrollment {
    @JsonProperty("student_id")
    private String studentId;
    private String name;
    private int year;
    @JsonProperty("enrolled_courses")
    private List<String> enrolledCourses;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public List<String> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<String> enrolledCourses) { this.enrolledCourses = enrolledCourses; }
}
