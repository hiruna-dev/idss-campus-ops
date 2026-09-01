package com.idss.task3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertexResult {
    @JsonProperty("exam_id")
    private String examId;
    @JsonProperty("course_code")
    private String courseCode;
    private int degree;
    private int color;
    @JsonProperty("session_index")
    private int sessionIndex;

    public VertexResult() {}

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public int getDegree() { return degree; }
    public void setDegree(int degree) { this.degree = degree; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getSessionIndex() { return sessionIndex; }
    public void setSessionIndex(int sessionIndex) { this.sessionIndex = sessionIndex; }
}
