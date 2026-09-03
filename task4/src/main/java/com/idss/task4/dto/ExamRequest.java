package com.idss.task4.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Exam request input for Task 4 room ranking.
 * Fields match input_exam_requests.json (task_4_plan.md Section 3).
 * {@code preferred_time_slot} is carried through but not used in scoring.
 */
public class ExamRequest {

    @JsonProperty("exam_id")
    private String examId;

    @JsonProperty("course_code")
    private String courseCode;

    @JsonProperty("course_title")
    private String courseTitle;

    @JsonProperty("student_count")
    private int studentCount;

    @JsonProperty("requires_accessibility")
    private boolean requiresAccessibility;

    @JsonProperty("preferred_time_slot")
    private String preferredTimeSlot;

    public ExamRequest() {}

    public ExamRequest(String examId, String courseCode, String courseTitle,
                       int studentCount, boolean requiresAccessibility,
                       String preferredTimeSlot) {
        this.examId = examId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.studentCount = studentCount;
        this.requiresAccessibility = requiresAccessibility;
        this.preferredTimeSlot = preferredTimeSlot;
    }

    // --- Getters & Setters ---

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public boolean isRequiresAccessibility() { return requiresAccessibility; }
    public void setRequiresAccessibility(boolean requiresAccessibility) {
        this.requiresAccessibility = requiresAccessibility;
    }

    public String getPreferredTimeSlot() { return preferredTimeSlot; }
    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }

    @Override
    public String toString() {
        return "ExamRequest{examId='" + examId + "', courseCode='" + courseCode +
               "', studentCount=" + studentCount +
               ", requiresAccessibility=" + requiresAccessibility + "}";
    }
}
