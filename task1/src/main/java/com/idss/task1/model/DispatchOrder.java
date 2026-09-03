package com.idss.task1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idss.common.config.Canonical;

import java.util.Objects;

/**
 * Represents a paper delivery request (dispatch order) generated for an exam session.
 * Sourced directly from Task 5 (output_master_schedule.json) or data/input/input_dispatch_orders.json.
 */
public class DispatchOrder {

    @JsonProperty("dispatch_id")
    private String dispatchId;

    @JsonProperty("exam_id")
    private String examId;

    @JsonProperty("course_code")
    private String courseCode;

    @JsonProperty("course_title")
    private String courseTitle;

    @JsonProperty("session_date")
    private String sessionDate;

    @JsonProperty("exam_start_time")
    private String examStartTime;

    @JsonProperty("source_vault_id")
    private String sourceVaultId = "VAULT_G01";

    @JsonProperty("destination_room_id")
    private String destinationRoomId;

    @JsonProperty("destination_floor")
    private int destinationFloor;

    @JsonProperty("package_weight_kg")
    private double packageWeightKg;

    @JsonProperty("transport_mode")
    private String transportMode = "FOOT_COURIER"; // FOOT_COURIER or TROLLEY

    @JsonProperty("requires_step_free_access")
    private boolean requiresStepFreeAccess;

    @JsonProperty("security_clearance_level")
    private int securityClearanceLevel = 2;

    @JsonProperty("max_allowed_transit_seconds")
    private int maxAllowedTransitSeconds = 300;

    public DispatchOrder() {
    }

    public DispatchOrder(String dispatchId, String examId, String courseCode, String courseTitle,
                         String sessionDate, String examStartTime, String sourceVaultId,
                         String destinationRoomId, int destinationFloor, double packageWeightKg,
                         String transportMode, boolean requiresStepFreeAccess,
                         int securityClearanceLevel, int maxAllowedTransitSeconds) {
        this.dispatchId = dispatchId;
        this.examId = examId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.sessionDate = sessionDate;
        this.examStartTime = examStartTime;
        this.sourceVaultId = sourceVaultId != null ? sourceVaultId : "VAULT_G01";
        this.destinationRoomId = destinationRoomId;
        this.destinationFloor = destinationFloor;
        this.packageWeightKg = packageWeightKg;
        this.transportMode = transportMode != null ? transportMode : "FOOT_COURIER";
        this.requiresStepFreeAccess = requiresStepFreeAccess;
        this.securityClearanceLevel = securityClearanceLevel;
        this.maxAllowedTransitSeconds = maxAllowedTransitSeconds;
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

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getExamStartTime() {
        return examStartTime;
    }

    public void setExamStartTime(String examStartTime) {
        this.examStartTime = examStartTime;
    }

    public String getSourceVaultId() {
        return sourceVaultId;
    }

    public void setSourceVaultId(String sourceVaultId) {
        this.sourceVaultId = sourceVaultId;
    }

    public String getDestinationRoomId() {
        return destinationRoomId;
    }

    public void setDestinationRoomId(String destinationRoomId) {
        this.destinationRoomId = destinationRoomId;
    }

    /**
     * Returns the destination node ID formatted as graph alias (e.g. ROOM_R101).
     */
    public String getGraphDestinationNodeId() {
        return Canonical.toAliasedRoomId(destinationRoomId);
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public double getPackageWeightKg() {
        return packageWeightKg;
    }

    public void setPackageWeightKg(double packageWeightKg) {
        this.packageWeightKg = packageWeightKg;
        // Heavy packages automatically trigger trolley mode and require step-free access
        if (packageWeightKg >= 15.0) {
            this.transportMode = "TROLLEY";
            this.requiresStepFreeAccess = true;
        }
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public boolean isRequiresStepFreeAccess() {
        return requiresStepFreeAccess;
    }

    public void setRequiresStepFreeAccess(boolean requiresStepFreeAccess) {
        this.requiresStepFreeAccess = requiresStepFreeAccess;
    }

    public int getSecurityClearanceLevel() {
        return securityClearanceLevel;
    }

    public void setSecurityClearanceLevel(int securityClearanceLevel) {
        this.securityClearanceLevel = securityClearanceLevel;
    }

    public int getMaxAllowedTransitSeconds() {
        return maxAllowedTransitSeconds;
    }

    public void setMaxAllowedTransitSeconds(int maxAllowedTransitSeconds) {
        this.maxAllowedTransitSeconds = maxAllowedTransitSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispatchOrder that = (DispatchOrder) o;
        return destinationFloor == that.destinationFloor &&
               Double.compare(that.packageWeightKg, packageWeightKg) == 0 &&
               requiresStepFreeAccess == that.requiresStepFreeAccess &&
               securityClearanceLevel == that.securityClearanceLevel &&
               maxAllowedTransitSeconds == that.maxAllowedTransitSeconds &&
               Objects.equals(dispatchId, that.dispatchId) &&
               Objects.equals(examId, that.examId) &&
               Objects.equals(courseCode, that.courseCode) &&
               Objects.equals(destinationRoomId, that.destinationRoomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dispatchId, examId, courseCode, destinationRoomId, destinationFloor, requiresStepFreeAccess);
    }

    @Override
    public String toString() {
        return "DispatchOrder{" +
                "dispatchId='" + dispatchId + '\'' +
                ", examId='" + examId + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", destinationRoomId='" + destinationRoomId + '\'' +
                ", requiresStepFreeAccess=" + requiresStepFreeAccess +
                ", transportMode='" + transportMode + '\'' +
                '}';
    }
}
