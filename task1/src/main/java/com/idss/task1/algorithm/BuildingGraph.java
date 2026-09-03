package com.idss.task1.algorithm;

import com.idss.common.util.JsonLoader;
import com.idss.task1.model.Coordinates3D;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;

import java.io.IOException;
import java.util.*;

/**
 * 3D Multi-Floor Campus Graph Representation (LO2).
 *
 * <p>Backed by a custom 3D Adjacency List ({@code Map<String, List<Edge3D>>}) consuming
 * strictly O(V + E) RAM. Provides O(1) node lookup and dynamic step-free edge filtering
 * during algorithm path expansions.</p>
 */
public class BuildingGraph {

    private final Map<String, Node3D> nodeRegistry = new HashMap<>();
    private final Map<String, List<Edge3D>> adjacencyList = new HashMap<>();

    public BuildingGraph() {
    }

    /**
     * Adds a node and indexes its outgoing edges in the adjacency list.
     */
    public void addNode(Node3D node) {
        if (node == null || node.getNodeId() == null) {
            return;
        }
        nodeRegistry.put(node.getNodeId(), node);
        adjacencyList.putIfAbsent(node.getNodeId(), new ArrayList<>());
        if (node.getAdjacentEdges() != null) {
            for (Edge3D edge : node.getAdjacentEdges()) {
                addEdge(node.getNodeId(), edge);
            }
        }
    }

    /**
     * Adds a directed edge from sourceNodeId to target node.
     */
    public void addEdge(String sourceNodeId, Edge3D edge) {
        if (sourceNodeId == null || edge == null) {
            return;
        }
        adjacencyList.computeIfAbsent(sourceNodeId, k -> new ArrayList<>()).add(edge);
    }

    /**
     * Retrieves node metadata in O(1) time.
     */
    public Node3D getNode(String nodeId) {
        return nodeRegistry.get(nodeId);
    }

    /**
     * Returns true if the node exists in the campus graph.
     */
    public boolean containsNode(String nodeId) {
        return nodeRegistry.containsKey(nodeId);
    }

    /**
     * Retrieves all outgoing edges from a given node.
     */
    public List<Edge3D> getOutgoingEdges(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * Accessibility Engine Filter (Student A Responsibility).
     *
     * <p>Returns accessible outgoing edges from {@code sourceNodeId}. If
     * {@code requiresStepFree} is true, all staircase edges (where {@code is_step_free == false})
     * are pruned, enforcing elevator-only and step-free corridor transit.</p>
     */
    public List<Edge3D> getAccessibleNeighbors(String sourceNodeId, boolean requiresStepFree) {
        List<Edge3D> edges = adjacencyList.get(sourceNodeId);
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }

        List<Edge3D> accessibleEdges = new ArrayList<>();
        for (Edge3D edge : edges) {
            // If dispatch requires step-free access, prune non-step-free edges (e.g. staircases)
            if (requiresStepFree && !edge.isStepFree()) {
                continue;
            }
            accessibleEdges.add(edge);
        }
        return accessibleEdges;
    }

    /**
     * Retrieves the 3D coordinates for a node in O(1) time.
     */
    public Coordinates3D getCoordinates(String nodeId) {
        Node3D node = nodeRegistry.get(nodeId);
        return node != null ? node.getCoordinates() : null;
    }

    public int getVertexCount() {
        return nodeRegistry.size();
    }

    public int getEdgeCount() {
        int count = 0;
        for (List<Edge3D> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count;
    }

    public Set<String> getAllNodeIds() {
        return Collections.unmodifiableSet(nodeRegistry.keySet());
    }

    public Map<String, Node3D> getNodeRegistry() {
        return Collections.unmodifiableMap(nodeRegistry);
    }

    /**
     * Loads and builds a BuildingGraph instance from a JSON file (e.g. data/input/input_building_graph.json).
     */
    public static BuildingGraph loadFromJson(String filePath) throws IOException {
        List<Node3D> nodes = JsonLoader.loadList(filePath, Node3D.class);
        BuildingGraph graph = new BuildingGraph();
        for (Node3D node : nodes) {
            graph.addNode(node);
        }
        return graph;
    }
}
