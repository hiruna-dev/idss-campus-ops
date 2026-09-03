package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the calculated optimal delivery path and navigation manifest for a dispatch order.
 * Persisted into MongoDB 'delivery_routes' collection.
 */
@Document(collection = "delivery_routes")
public class DeliveryRoute {

    @Id
    @JsonProperty("dispatch_id")
    private String dispatchId;

    @JsonProperty("exam_id")
    private String examId;

    @JsonProperty("course_code")
    private String courseCode;

    @JsonProperty("source_vault")
    private String sourceVault = "VAULT_G01";

    @JsonProperty("destination_room")
    private String destinationRoom;

    @JsonProperty("target_floor")
    private int targetFloor;

    @JsonProperty("requires_step_free_access")
    private boolean requiresStepFreeAccess;

    @JsonProperty("step_free_verified")
    private boolean stepFreeVerified;

    @JsonProperty("total_distance_meters")
    private double totalDistanceMeters;

    @JsonProperty("estimated_transit_time_seconds")
    private int estimatedTransitTimeSeconds;

    @JsonProperty("within_time_limit")
    private boolean withinTimeLimit;

    @JsonProperty("nodes_in_path_count")
    private int nodesInPathCount;

    @JsonProperty("hard_constraint_violations")
    private int hardConstraintViolations;

    @JsonProperty("path_sequence")
    private List<String> pathSequence = new ArrayList<>();

    @JsonProperty("turn_by_turn_manifest")
    private List<TurnByTurnStep> turnByTurnManifest = new ArrayList<>();

    public DeliveryRoute() {
    }

    public DeliveryRoute(String dispatchId, String examId, String courseCode, String sourceVault,
                         String destinationRoom, int targetFloor, boolean requiresStepFreeAccess,
                         boolean stepFreeVerified, double totalDistanceMeters,
                         int estimatedTransitTimeSeconds, boolean withinTimeLimit,
                         int nodesInPathCount, int hardConstraintViolations,
                         List<String> pathSequence, List<TurnByTurnStep> turnByTurnManifest) {
        this.dispatchId = dispatchId;
        this.examId = examId;
        this.courseCode = courseCode;
        this.sourceVault = sourceVault != null ? sourceVault : "VAULT_G01";
        this.destinationRoom = destinationRoom;
        this.targetFloor = targetFloor;
        this.requiresStepFreeAccess = requiresStepFreeAccess;
        this.stepFreeVerified = stepFreeVerified;
        this.totalDistanceMeters = totalDistanceMeters;
        this.estimatedTransitTimeSeconds = estimatedTransitTimeSeconds;
        this.withinTimeLimit = withinTimeLimit;
        this.nodesInPathCount = nodesInPathCount;
        this.hardConstraintViolations = hardConstraintViolations;
        if (pathSequence != null) {
            this.pathSequence = pathSequence;
        }
        if (turnByTurnManifest != null) {
            this.turnByTurnManifest = turnByTurnManifest;
        }
    }

    public String getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(String dispatchId) {
        this.dispatchId = dispatchId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSourceVault() {
        return sourceVault;
    }

    public void setSourceVault(String sourceVault) {
        this.sourceVault = sourceVault;
    }

    public String getDestinationRoom() {
        return destinationRoom;
    }

    public void setDestinationRoom(String destinationRoom) {
        this.destinationRoom = destinationRoom;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public void setTargetFloor(int targetFloor) {
        this.targetFloor = targetFloor;
    }

    public boolean isRequiresStepFreeAccess() {
        return requiresStepFreeAccess;
    }

    public void setRequiresStepFreeAccess(boolean requiresStepFreeAccess) {
        this.requiresStepFreeAccess = requiresStepFreeAccess;
    }

    public boolean isStepFreeVerified() {
        return stepFreeVerified;
    }

    public void setStepFreeVerified(boolean stepFreeVerified) {
        this.stepFreeVerified = stepFreeVerified;
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public void setTotalDistanceMeters(double totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public int getEstimatedTransitTimeSeconds() {
        return estimatedTransitTimeSeconds;
    }

    public void setEstimatedTransitTimeSeconds(int estimatedTransitTimeSeconds) {
        this.estimatedTransitTimeSeconds = estimatedTransitTimeSeconds;
    }

    public boolean isWithinTimeLimit() {
        return withinTimeLimit;
    }

    public void setWithinTimeLimit(boolean withinTimeLimit) {
        this.withinTimeLimit = withinTimeLimit;
    }

    public int getNodesInPathCount() {
        return nodesInPathCount;
    }

    public void setNodesInPathCount(int nodesInPathCount) {
        this.nodesInPathCount = nodesInPathCount;
    }

    public int getHardConstraintViolations() {
        return hardConstraintViolations;
    }

    public void setHardConstraintViolations(int hardConstraintViolations) {
        this.hardConstraintViolations = hardConstraintViolations;
    }

    public List<String> getPathSequence() {
        return pathSequence;
    }

    public void setPathSequence(List<String> pathSequence) {
        this.pathSequence = pathSequence != null ? pathSequence : new ArrayList<>();
    }

    public List<TurnByTurnStep> getTurnByTurnManifest() {
        return turnByTurnManifest;
    }

    public void setTurnByTurnManifest(List<TurnByTurnStep> turnByTurnManifest) {
        this.turnByTurnManifest = turnByTurnManifest != null ? turnByTurnManifest : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryRoute that = (DeliveryRoute) o;
        return targetFloor == that.targetFloor &&
               requiresStepFreeAccess == that.requiresStepFreeAccess &&
               stepFreeVerified == that.stepFreeVerified &&
               Double.compare(that.totalDistanceMeters, totalDistanceMeters) == 0 &&
               estimatedTransitTimeSeconds == that.estimatedTransitTimeSeconds &&
               withinTimeLimit == that.withinTimeLimit &&
               Objects.equals(dispatchId, that.dispatchId) &&
               Objects.equals(examId, that.examId) &&
               Objects.equals(destinationRoom, that.destinationRoom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dispatchId, examId, destinationRoom, totalDistanceMeters, estimatedTransitTimeSeconds);
    }

    @Override
    public String toString() {
        return "DeliveryRoute{" +
                "dispatchId='" + dispatchId + '\'' +
                ", examId='" + examId + '\'' +
                ", destinationRoom='" + destinationRoom + '\'' +
                ", totalDistanceMeters=" + totalDistanceMeters +
                ", estimatedTransitTimeSeconds=" + estimatedTransitTimeSeconds +
                ", stepFreeVerified=" + stepFreeVerified +
                '}';
    }
}
