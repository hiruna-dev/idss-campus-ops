package com.idss.task1;

import com.idss.task1.algorithm.IndexedHeap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IndexedHeap (Student B Deliverable).
 * Validates min-heap ordering, decreaseKey re-heapification, and O(1) membership tracking.
 */
class IndexedHeapTest {

    @Test
    @DisplayName("Should be empty on construction")
    void testInitiallyEmpty() {
        IndexedHeap heap = new IndexedHeap();
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertNull(heap.extractMin());
    }

    @Test
    @DisplayName("Should extract nodes in ascending key order")
    void testExtractMinOrdering() {
        IndexedHeap heap = new IndexedHeap();
        heap.insert("C", 3.0);
        heap.insert("A", 1.0);
        heap.insert("B", 2.0);
        heap.insert("E", 5.0);
        heap.insert("D", 4.0);

        assertEquals("A", heap.extractMin());
        assertEquals("B", heap.extractMin());
        assertEquals("C", heap.extractMin());
        assertEquals("D", heap.extractMin());
        assertEquals("E", heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    @DisplayName("Should track membership and size correctly")
    void testContainsAndSize() {
        IndexedHeap heap = new IndexedHeap();
        assertFalse(heap.contains("A"));
        heap.insert("A", 10.0);
        assertTrue(heap.contains("A"));
        assertEquals(1, heap.size());

        heap.extractMin();
        assertFalse(heap.contains("A"));
        assertEquals(0, heap.size());
    }

    @Test
    @DisplayName("decreaseKey should reorder the heap when a smaller priority is applied")
    void testDecreaseKeyReordersHeap() {
        IndexedHeap heap = new IndexedHeap();
        heap.insert("A", 10.0);
        heap.insert("B", 20.0);
        heap.insert("C", 30.0);

        // C was the largest; lower it below A so it becomes the new minimum
        heap.decreaseKey("C", 1.0);

        assertEquals("C", heap.extractMin());
        assertEquals("A", heap.extractMin());
        assertEquals("B", heap.extractMin());
    }

    @Test
    @DisplayName("decreaseKey should ignore attempts to raise a key")
    void testDecreaseKeyIgnoresLargerValue() {
        IndexedHeap heap = new IndexedHeap();
        heap.insert("A", 5.0);
        heap.insert("B", 10.0);

        // Attempting to "decrease" A's key to something larger must be a no-op
        heap.decreaseKey("A", 50.0);

        assertEquals("A", heap.extractMin(), "A must remain the minimum since the raise attempt should be ignored");
    }

    @Test
    @DisplayName("decreaseKey on an absent node should insert it")
    void testDecreaseKeyOnAbsentNodeInserts() {
        IndexedHeap heap = new IndexedHeap();
        heap.insert("A", 10.0);

        heap.decreaseKey("Z", 1.0);

        assertTrue(heap.contains("Z"));
        assertEquals("Z", heap.extractMin());
    }

    @Test
    @DisplayName("insert on an existing node should behave like decreaseKey, not duplicate it")
    void testInsertExistingNodeDoesNotDuplicate() {
        IndexedHeap heap = new IndexedHeap();
        heap.insert("A", 10.0);
        heap.insert("A", 2.0);

        assertEquals(1, heap.size(), "Re-inserting an existing node must not create a duplicate entry");
        assertEquals("A", heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    @DisplayName("Should grow beyond default capacity without losing heap invariants")
    void testGrowsBeyondDefaultCapacity() {
        IndexedHeap heap = new IndexedHeap(2);
        int n = 100;
        List<Integer> keys = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < n; i++) {
            int key = random.nextInt(10_000);
            keys.add(key);
            heap.insert("N" + i, key);
        }
        assertEquals(n, heap.size());

        int previous = Integer.MIN_VALUE;
        int extracted = 0;
        while (!heap.isEmpty()) {
            String nodeId = heap.extractMin();
            int idx = Integer.parseInt(nodeId.substring(1));
            int key = keys.get(idx);
            assertTrue(key >= previous, "extractMin must yield non-decreasing keys");
            previous = key;
            extracted++;
        }
        assertEquals(n, extracted);
    }
}
