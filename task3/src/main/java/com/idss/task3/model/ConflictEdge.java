package com.idss.task3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConflictEdge {
    @JsonProperty("exam_a")
    private String examA;
    @JsonProperty("exam_b")
    private String examB;
    @JsonProperty("shared_students")
    private int sharedStudents;

    public ConflictEdge() {}

    public ConflictEdge(String examA, String examB, int sharedStudents) {
        this.examA = examA;
        this.examB = examB;
        this.sharedStudents = sharedStudents;
    }

    public String getExamA() { return examA; }
    public void setExamA(String examA) { this.examA = examA; }

    public String getExamB() { return examB; }
    public void setExamB(String examB) { this.examB = examB; }

    public int getSharedStudents() { return sharedStudents; }
    public void setSharedStudents(int sharedStudents) { this.sharedStudents = sharedStudents; }
}
