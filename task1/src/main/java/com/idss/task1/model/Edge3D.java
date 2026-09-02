package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a directed connection between two spatial nodes in the campus building.
 * Contains physical transit metrics and accessibility constraints.
 */
public class Edge3D {

    @JsonProperty("target_node")
    private String targetNode;

    @JsonProperty("distance_meters")
    private double distanceMeters;

    @JsonProperty("base_transit_time_seconds")
    private int baseTransitTimeSeconds;

    @JsonProperty("edge_type")
    private String edgeType; // CORRIDOR, DOORWAY, STAIRCASE, ELEVATOR_SHAFT

    @JsonProperty("is_step_free")
    private boolean isStepFree;

    @JsonProperty("security_clearance_required")
    private int securityClearanceRequired;

    public Edge3D() {
    }

    public Edge3D(String targetNode, double distanceMeters, int baseTransitTimeSeconds,
                  String edgeType, boolean isStepFree, int securityClearanceRequired) {
        this.targetNode = targetNode;
        this.distanceMeters = distanceMeters;
        this.baseTransitTimeSeconds = baseTransitTimeSeconds;
        this.edgeType = edgeType;
        this.isStepFree = isStepFree;
        this.securityClearanceRequired = securityClearanceRequired;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public int getBaseTransitTimeSeconds() {
        return baseTransitTimeSeconds;
    }

    public void setBaseTransitTimeSeconds(int baseTransitTimeSeconds) {
        this.baseTransitTimeSeconds = baseTransitTimeSeconds;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public boolean isStepFree() {
        return isStepFree;
    }

    public void setStepFree(boolean stepFree) {
        isStepFree = stepFree;
    }

    public int getSecurityClearanceRequired() {
        return securityClearanceRequired;
    }

    public void setSecurityClearanceRequired(int securityClearanceRequired) {
        this.securityClearanceRequired = securityClearanceRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge3D edge3D = (Edge3D) o;
        return Double.compare(edge3D.distanceMeters, distanceMeters) == 0 &&
               baseTransitTimeSeconds == edge3D.baseTransitTimeSeconds &&
               isStepFree == edge3D.isStepFree &&
               securityClearanceRequired == edge3D.securityClearanceRequired &&
               Objects.equals(targetNode, edge3D.targetNode) &&
               Objects.equals(edgeType, edge3D.edgeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetNode, distanceMeters, baseTransitTimeSeconds, edgeType, isStepFree, securityClearanceRequired);
    }

    @Override
    public String toString() {
        return "Edge3D{" +
                "targetNode='" + targetNode + '\'' +
                ", distanceMeters=" + distanceMeters +
                ", baseTransitTimeSeconds=" + baseTransitTimeSeconds +
                ", edgeType='" + edgeType + '\'' +
                ", isStepFree=" + isStepFree +
                '}';
    }
}
