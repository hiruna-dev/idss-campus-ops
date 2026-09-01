package com.idss.task1.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Array-backed Indexed Binary Min-Heap Priority Queue (Student B Deliverable - LO2).
 *
 * <p>Supports O(log V) {@code insert}, {@code extractMin}, and {@code decreaseKey}
 * operations plus O(1) membership lookup via an auxiliary position index map
 * ({@code pos[nodeId] -> heap index}). Used as the open-set priority queue for
 * {@link AStarEngine}, avoiding the O(V) linear scan cost that
 * {@code java.util.PriorityQueue#remove} would introduce into A* search.</p>
 */
public class IndexedHeap {

    private static final int DEFAULT_CAPACITY = 16;

    private String[] heap;
    private double[] keys;
    private final Map<String, Integer> pos = new HashMap<>();
    private int size;

    public IndexedHeap() {
        this(DEFAULT_CAPACITY);
    }

    public IndexedHeap(int initialCapacity) {
        int capacity = Math.max(initialCapacity, 1);
        this.heap = new String[capacity];
        this.keys = new double[capacity];
        this.size = 0;
    }

    /**
     * Inserts a node with the given priority key. If the node is already present,
     * this instead delegates to {@link #decreaseKey(String, double)}. O(log V).
     */
    public void insert(String nodeId, double key) {
        if (nodeId == null) {
            return;
        }
        if (pos.containsKey(nodeId)) {
            decreaseKey(nodeId, key);
            return;
        }
        ensureCapacity(size + 1);
        heap[size] = nodeId;
        keys[size] = key;
        pos.put(nodeId, size);
        siftUp(size);
        size++;
    }

    /**
     * Removes and returns the node with the smallest key, or null if the heap is empty.
     * O(log V).
     */
    public String extractMin() {
        if (isEmpty()) {
            return null;
        }
        String minNode = heap[0];
        int last = size - 1;
        swap(0, last);
        heap[last] = null;
        pos.remove(minNode);
        size--;
        if (size > 0) {
            siftDown(0);
        }
        return minNode;
    }

    /**
     * Lowers the priority key of an existing node and restores the heap invariant
     * via sift-up. If {@code newKey} is not strictly smaller than the current key,
     * or the node is absent, this call is a no-op / delegates to {@link #insert}.
     * O(log V).
     */
    public void decreaseKey(String nodeId, double newKey) {
        Integer index = pos.get(nodeId);
        if (index == null) {
            insert(nodeId, newKey);
            return;
        }
        if (newKey >= keys[index]) {
            return;
        }
        keys[index] = newKey;
        siftUp(index);
    }

    /** O(1) membership check via the position index map. */
    public boolean contains(String nodeId) {
        return pos.containsKey(nodeId);
    }

    /** O(1) emptiness check. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** O(1) current element count. */
    public int size() {
        return size;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (keys[parent] <= keys[i]) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < size && keys[left] < keys[smallest]) {
                smallest = left;
            }
            if (right < size && keys[right] < keys[smallest]) {
                smallest = right;
            }
            if (smallest == i) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        String tempNode = heap[i];
        double tempKey = keys[i];

        heap[i] = heap[j];
        keys[i] = keys[j];
        pos.put(heap[i], i);

        heap[j] = tempNode;
        keys[j] = tempKey;
        pos.put(heap[j], j);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= heap.length) {
            return;
        }
        int newCapacity = heap.length * 2;
        String[] newHeap = new String[newCapacity];
        double[] newKeys = new double[newCapacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        System.arraycopy(keys, 0, newKeys, 0, size);
        heap = newHeap;
        keys = newKeys;
    }
}
