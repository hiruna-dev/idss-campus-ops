package com.idss.task5.dto;

/**
 * Room reference lookup from Task 4's output_room_reference.json.
 * Provides floor and is_accessible for final schedule generation.
 * Task 4 owns is_accessible for room nodes (per group_data_contracts.md).
 */
public class RoomReference {
    public String room_id;
    public String room_name;
    public int floor;
    public boolean is_accessible;
}