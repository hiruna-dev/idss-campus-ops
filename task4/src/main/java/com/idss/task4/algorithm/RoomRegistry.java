package com.idss.task4.algorithm;

import com.idss.common.model.Room;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Room Master HashMap — {@code O(1)} lookup by {@code room_id}.
 * Populated once at startup from {@code input_room_master.json}
 * (task_4_plan.md Section 9).
 *
 * <p>Backs both the eligibility filter and the
 * {@code output_room_reference.json} lookup for Task 1.</p>
 *
 * <p><b>Data-structure choice (viva):</b> HashMap gives O(1) amortized
 * lookup by room_id vs. O(n) for a plain List scan. Since the filter
 * and reference lookup run for every exam request, this matters.</p>
 */
public class RoomRegistry {

    private final Map<String, Room> registry;

    /**
     * Constructs the registry from a list of rooms.
     * Duplicate room_ids will overwrite silently (last-write-wins).
     *
     * @param rooms list of rooms loaded from input_room_master.json
     */
    public RoomRegistry(List<Room> rooms) {
        Map<String, Room> map = new HashMap<>(rooms.size());
        for (Room room : rooms) {
            map.put(room.room_id, room);
        }
        this.registry = Collections.unmodifiableMap(map);
    }

    /**
     * O(1) lookup by canonical room_id (e.g. "R101", "LAB_3A").
     *
     * @param roomId the canonical room ID
     * @return the Room object, or null if not found
     */
    public Room get(String roomId) {
        return registry.get(roomId);
    }

    /**
     * Returns all rooms in the registry.
     *
     * @return unmodifiable collection of all rooms
     */
    public Collection<Room> getAllRooms() {
        return registry.values();
    }

    /**
     * Returns the number of rooms in the registry.
     */
    public int size() {
        return registry.size();
    }

    /**
     * Checks if a room_id exists in the registry.
     */
    public boolean contains(String roomId) {
        return registry.containsKey(roomId);
    }
}
