package com.idss.task4.dto;

/**
 * Internal DTO pairing a room_id with its computed TOPSIS closeness coefficient.
 * Implements {@link Comparable} so a {@link java.util.PriorityQueue} can
 * maintain a max-heap ordering (highest score first).
 *
 * <p>Data-structure rationale (task_4_plan.md Section 9):
 * PriorityQueue insert is O(log k), full rank extraction is O(k log k).</p>
 */
public class RoomScore implements Comparable<RoomScore> {

    private final String roomId;
    private final double score;

    public RoomScore(String roomId, double score) {
        this.roomId = roomId;
        this.score = score;
    }

    public String getRoomId() { return roomId; }
    public double getScore() { return score; }

    /**
     * Descending order: higher score = higher priority.
     * PriorityQueue in Java is a min-heap by default,
     * so we reverse the comparison to get a max-heap.
     */
    @Override
    public int compareTo(RoomScore other) {
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return "RoomScore{roomId='" + roomId + "', score=" + String.format("%.4f", score) + "}";
    }
}
