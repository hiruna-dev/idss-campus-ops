package com.idss.task5.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Canonical room ID mapping utility.
 * Handles R101 <-> ROOM_R101 alias for Task 1 compatibility.
 * Per group_data_contracts.md:
 * - room_id: R101 (canonical, used by Task 2/4/5)
 * - canonical_room_id: ROOM_R101 (alias for Task 1 graph nodes)
 */
public class CanonicalMapper {

    private static final Map<String, String> ROOM_TO_CANONICAL = new HashMap<>();

    static {
        ROOM_TO_CANONICAL.put("R101", "ROOM_R101");
        ROOM_TO_CANONICAL.put("LAB_3A", "ROOM_LAB3A");
        ROOM_TO_CANONICAL.put("R205", "ROOM_R205");
    }

    /**
     * Convert room_id (R101) to canonical_room_id (ROOM_R101).
     * Auto-generates if no mapping exists.
     */
    public static String toCanonical(String roomId) {
        if (roomId == null) return null;
        String canonical = ROOM_TO_CANONICAL.get(roomId);
        if (canonical != null) return canonical;
        String stripped = roomId.startsWith("ROOM_") ? roomId.substring(5) : roomId;
        return "ROOM_" + stripped;
    }

    /**
     * Convert canonical_room_id (ROOM_R101) to room_id (R101).
     * Accepts both formats.
     */
    public static String toRoomId(String canonicalOrRoomId) {
        if (canonicalOrRoomId == null) return null;
        if (!canonicalOrRoomId.startsWith("ROOM_")) return canonicalOrRoomId;
        return canonicalOrRoomId.substring(5);
    }
}