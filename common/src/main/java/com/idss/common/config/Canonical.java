package com.idss.common.config;

/**
 * Canonical constants and alias-mapping utilities (group_data_contracts.md
 * Section 1 and Section 5).
 *
 * <p>This is the single source of truth for legacy-to-canonical field/value
 * mapping so every task pair maps aliases identically. Per the MCF "never
 * invent field names" rule, all task modules should route unknown/legacy
 * values through these helpers before reading or writing JSON.</p>
 */
public final class Canonical {

    /** Room ID prefix used as a graph alias by Task 1 (e.g. {@code ROOM_R101}). */
    public static final String ROOM_ALIAS_PREFIX = "ROOM_";

    /** Status enum values (group_data_contracts.md Section 1). */
    public static final String STATUS_OPTIMAL    = "OPTIMAL";
    public static final String STATUS_FEASIBLE   = "FEASIBLE";
    public static final String STATUS_INFEASIBLE = "INFEASIBLE";
    public static final String STATUS_VALID      = "VALID";

    /** Session values (group_data_contracts.md Section 1). */
    public static final String SESSION_MORNING   = "Morning";
    public static final String SESSION_AFTERNOON = "Afternoon";

    private Canonical() {
        throw new AssertionError("Canonical is a constants holder; do not instantiate.");
    }

    /**
     * Strips the {@code ROOM_} alias prefix if present (group_data_contracts.md
     * Section 5). {@code "ROOM_R101"} -> {@code "R101"}; {@code "R101"} ->
     * {@code "R101"}. Null/blank input is returned unchanged.
     */
    public static String toCanonicalRoomId(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return roomId;
        }
        if (roomId.startsWith(ROOM_ALIAS_PREFIX)) {
            return roomId.substring(ROOM_ALIAS_PREFIX.length());
        }
        return roomId;
    }

    /**
     * Wraps a canonical room id with the {@code ROOM_} alias prefix
     * (group_data_contracts.md Section 5). {@code "R101"} -> {@code "ROOM_R101"}.
     * Task 5 outputs both forms; Task 1's building graph uses the aliased form.
     */
    public static String toAliasedRoomId(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return roomId;
        }
        if (roomId.startsWith(ROOM_ALIAS_PREFIX)) {
            return roomId;
        }
        // Building graph node id (input_building_graph.json) uses ROOM_LAB3A
        if ("LAB_3A".equals(roomId)) {
            return "ROOM_LAB3A";
        }
        return ROOM_ALIAS_PREFIX + roomId;
    }

    /**
     * Maps a legacy string noise level to the canonical int 1-5 scale
     * (group_data_contracts.md Section 5: low=2, medium=3, high=4, very_high=5).
     * Numeric strings and ints are passed through. Unknown values default to
     * 3 (medium) so a typo never silently produces the quietest or loudest room.
     */
    public static int toCanonicalNoiseLevel(Object raw) {
        if (raw == null) {
            return 3;
        }
        if (raw instanceof Number n) {
            return n.intValue();
        }
        String s = raw.toString().trim().toLowerCase();
        return switch (s) {
            case "low", "l"          -> 2;
            case "medium", "med", "m"-> 3;
            case "high", "h"         -> 4;
            case "very_high", "vh"   -> 5;
            default -> {
                try {
                    yield Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    yield 3;
                }
            }
        };
    }

    /**
     * Derives the Task 1 dispatch field {@code requires_step_free_access} from
     * the canonical exam field {@code requires_accessibility}
     * (group_data_contracts.md Section 1: "same boolean, same value").
     */
    public static boolean toStepFreeAccess(boolean requiresAccessibility) {
        return requiresAccessibility;
    }

    /**
     * Normalizes a score field to {@code score} (0-1 TOPSIS closeness) per
     * group_data_contracts.md Section 5. Legacy aliases {@code rank_score},
     * {@code rankScore} are unified to {@code score} by callers when reading
     * JSON; this helper just clamps the numeric value to [0,1].
     */
    public static double clampScore(double score) {
        if (score < 0.0) return 0.0;
        if (score > 1.0) return 1.0;
        return score;
    }
}
