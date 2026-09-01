package com.idss.task4.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Output DTO representing a single row in {@code output_room_rankings.json}.
 * One row per eligible room per exam (task_4_plan.md Section 4).
 *
 * <p>Consumed by Task 5 to pick the best room for each exam.
 * Fields match the group_data_contracts.md Section 4 contract exactly.</p>
 */
public class RoomRanking {

    @JsonProperty("exam_id")
    private String examId;

    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("rank")
    private int rank;

    @JsonProperty("score")
    private double score;

    @JsonProperty("meets_hard_constraints")
    private boolean meetsHardConstraints;

    public RoomRanking() {}

    public RoomRanking(String examId, String roomId, int rank,
                       double score, boolean meetsHardConstraints) {
        this.examId = examId;
        this.roomId = roomId;
        this.rank = rank;
        this.score = score;
        this.meetsHardConstraints = meetsHardConstraints;
    }

    // --- Getters & Setters ---

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public boolean isMeetsHardConstraints() { return meetsHardConstraints; }
    public void setMeetsHardConstraints(boolean meetsHardConstraints) {
        this.meetsHardConstraints = meetsHardConstraints;
    }

    @Override
    public String toString() {
        return "RoomRanking{examId='" + examId + "', roomId='" + roomId +
               "', rank=" + rank + ", score=" + String.format("%.4f", score) +
               ", meetsHardConstraints=" + meetsHardConstraints + "}";
    }
}
