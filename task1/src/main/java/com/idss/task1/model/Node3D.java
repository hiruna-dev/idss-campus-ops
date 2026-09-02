package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a 3D vertex (Vault, Corridor Junction, Stair Landing, Elevator, Exam Room)
 * within the campus multi-floor building.
 */
public class Node3D {

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("node_name")
    private String nodeName;

    @JsonProperty("floor")
    private int floor;

    @JsonProperty("coordinates")
    private Coordinates3D coordinates;

    @JsonProperty("node_type")
    private String nodeType; // VAULT, JUNCTION, ELEVATOR, STAIRS, EXAM_ROOM

    @JsonProperty("is_accessible")
    private boolean isAccessible;

    @JsonProperty("adjacent_edges")
    private List<Edge3D> adjacentEdges = new ArrayList<>();

    public Node3D() {
    }

    public Node3D(String nodeId, String nodeName, int floor, Coordinates3D coordinates,
                  String nodeType, boolean isAccessible, List<Edge3D> adjacentEdges) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.floor = floor;
        this.coordinates = coordinates;
        this.nodeType = nodeType;
        this.isAccessible = isAccessible;
        if (adjacentEdges != null) {
            this.adjacentEdges = adjacentEdges;
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public Coordinates3D getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates3D coordinates) {
        this.coordinates = coordinates;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public List<Edge3D> getAdjacentEdges() {
        return adjacentEdges;
    }

    public void setAdjacentEdges(List<Edge3D> adjacentEdges) {
        this.adjacentEdges = adjacentEdges != null ? adjacentEdges : new ArrayList<>();
    }

    public void addEdge(Edge3D edge) {
        if (edge != null) {
            this.adjacentEdges.add(edge);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node3D node3D = (Node3D) o;
        return floor == node3D.floor &&
               isAccessible == node3D.isAccessible &&
               Objects.equals(nodeId, node3D.nodeId) &&
               Objects.equals(nodeName, node3D.nodeName) &&
               Objects.equals(coordinates, node3D.coordinates) &&
               Objects.equals(nodeType, node3D.nodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, nodeName, floor, coordinates, nodeType, isAccessible);
    }

    @Override
    public String toString() {
        return "Node3D{" +
                "nodeId='" + nodeId + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", floor=" + floor +
                ", nodeType='" + nodeType + '\'' +
                ", isAccessible=" + isAccessible +
                ", edgeCount=" + (adjacentEdges != null ? adjacentEdges.size() : 0) +
                '}';
    }
}
