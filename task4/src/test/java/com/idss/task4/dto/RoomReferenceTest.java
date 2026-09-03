package com.idss.task4.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test: RoomReference's accessibility flag must serialize as a
 * single {@code is_accessible} field. The isAccessible() getter's implicit
 * Jackson property name ("accessible") previously didn't match the field's
 * explicit @JsonProperty("is_accessible"), so both were emitted.
 */
class RoomReferenceTest {

    @Test
    void serializesExactlyFourFieldsNoDuplicateAccessibilityKey() throws Exception {
        RoomReference ref = new RoomReference("R101", "Lecture Theatre 101", 1, true);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree(mapper.writeValueAsString(ref));

        int fieldCount = 0;
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); it.next()) {
            fieldCount++;
        }
        assertEquals(4, fieldCount, "expected exactly room_id, room_name, floor, is_accessible");

        assertTrue(node.has("is_accessible"));
        assertTrue(node.get("is_accessible").asBoolean());
        assertFalse(node.has("accessible"), "stray 'accessible' field should not be emitted");
    }
}
