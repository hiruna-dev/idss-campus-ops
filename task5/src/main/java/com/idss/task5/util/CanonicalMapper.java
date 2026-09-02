package com.idss.task5.util;

import com.idss.common.config.Canonical;

/**
 * Canonical room ID mapping utility.
 * Handles R101 <-> ROOM_R101 alias for Task 1 compatibility.
 * Per group_data_contracts.md:
 * - room_id: R101 (canonical, used by Task 2/4/5)
 * - canonical_room_id: ROOM_R101 (alias for Task 1 graph nodes)
 */
public class CanonicalMapper {

    /**
     * Convert room_id (R101) to canonical_room_id (ROOM_R101).
     * Delegates to common.Canonical for consistent graph aliases.
     */
    public static String toCanonical(String roomId) {
        return Canonical.toAliasedRoomId(roomId);
    }

    public static String toRoomId(String canonicalOrRoomId) {
        return Canonical.toCanonicalRoomId(canonicalOrRoomId);
    }
}