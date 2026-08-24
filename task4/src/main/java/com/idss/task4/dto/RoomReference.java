package com.idss.task4.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Output DTO for {@code output_room_reference.json} — the lookup table
 * consumed by Task 1 and Task 5 (task_4_plan.md Section 4).
 *
 * <p>Task 1 receives {@code room_id} from Task 5's finalized schedule
 * and queries this table for physical/accessibility details needed for routing.</p>
 */
public class RoomReference {

    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("floor")
    private int floor;

    @JsonProperty("is_accessible")
    private boolean isAccessible;

    public RoomReference() {}

    public RoomReference(String roomId, String roomName, int floor, boolean isAccessible) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.floor = floor;
        this.isAccessible = isAccessible;
    }

    // --- Getters & Setters ---

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    @JsonProperty("is_accessible")
    public boolean isAccessible() { return isAccessible; }

    @JsonProperty("is_accessible")
    public void setAccessible(boolean accessible) { isAccessible = accessible; }

    @Override
    public String toString() {
        return "RoomReference{roomId='" + roomId + "', roomName='" + roomName +
               "', floor=" + floor + ", isAccessible=" + isAccessible + "}";
    }
}
