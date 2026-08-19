package com.idss.common.model;

/**
 * Room master entity (group_data_contracts.md Section 3).
 * Task 4 internal input. {@code noise_level} is an int 1-5 (5 = quietest),
 * never a string. {@code is_accessible} is owned by Task 4
 * (output_room_reference.json).
 */
public class Room {
    public String room_id;
    public String room_name;
    public int floor;
    public int capacity;
    public boolean has_ac;
    public int noise_level;
    public int accessibility_score;
    public boolean is_accessible;
}
