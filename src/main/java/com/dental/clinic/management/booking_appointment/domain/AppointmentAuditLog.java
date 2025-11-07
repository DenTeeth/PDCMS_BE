package com.dental.clinic.management.booking_appointment.domain;

import com.dental.clinic.management.booking_appointment.enums.AppointmentActionType;
import com.dental.clinic.management.booking_appointment.enums.AppointmentReasonCode;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * AppointmentAuditLog Entity
 * Tracks all important changes to appointments
 *
 * Use cases:
 * - CREATE: When appointment is first created
 * - DELAY: Same-day time change
 * - RESCHEDULE_SOURCE: Original appointment being rescheduled
 * - RESCHEDULE_TARGET: New appointment from reschedule
 * - CANCEL: Appointment cancelled
 * - STATUS_CHANGE: Status transitions (CHECKED_IN, IN_PROGRESS, COMPLETED,
 * etc.)
 */
@Entity
@Table(name = "appointment_audit_logs")
public class AppointmentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    /**
     * FK to appointments table
     */
    @Column(name = "appointment_id", nullable = false)
    private Integer appointmentId;

    /**
     * FK to employees table - who performed this action
     * NULL if system-generated
     */
    @Column(name = "changed_by_employee_id")
    private Integer performedByEmployeeId;

    /**
     * Type of action performed
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, columnDefinition = "appointment_action_type")
    private AppointmentActionType actionType;

    /**
     * Business reason for action (for DELAY, CANCEL, RESCHEDULE)
     * Optional
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", columnDefinition = "appointment_reason_code")
    private AppointmentReasonCode reasonCode;

    /**
     * Generic old value (for flexible tracking)
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * Generic new value
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Old start time (for DELAY, RESCHEDULE tracking)
     */
    @Column(name = "old_start_time")
    private LocalDateTime oldStartTime;

    /**
     * New start time
     */
    @Column(name = "new_start_time")
    private LocalDateTime newStartTime;

    /**
     * Old status (for STATUS_CHANGE tracking)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", columnDefinition = "appointment_status_enum")
    private AppointmentStatus oldStatus;

    /**
     * New status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", columnDefinition = "appointment_status_enum")
    private AppointmentStatus newStatus;

    /**
     * Optional notes from person performing action
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Timestamp of action
     */
    @Column(name = "action_timestamp")
    private LocalDateTime actionTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AppointmentAuditLog() {
    }

    public AppointmentAuditLog(Integer logId, Integer appointmentId, Integer performedByEmployeeId,
            AppointmentActionType actionType, AppointmentReasonCode reasonCode,
            String oldValue, String newValue, LocalDateTime oldStartTime,
            LocalDateTime newStartTime, AppointmentStatus oldStatus,
            AppointmentStatus newStatus, String notes, LocalDateTime actionTimestamp,
            LocalDateTime createdAt) {
        this.logId = logId;
        this.appointmentId = appointmentId;
        this.performedByEmployeeId = performedByEmployeeId;
        this.actionType = actionType;
        this.reasonCode = reasonCode;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldStartTime = oldStartTime;
        this.newStartTime = newStartTime;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.actionTimestamp = actionTimestamp;
        this.createdAt = createdAt;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPerformedByEmployeeId() {
        return performedByEmployeeId;
    }

    public void setPerformedByEmployeeId(Integer performedByEmployeeId) {
        this.performedByEmployeeId = performedByEmployeeId;
    }

    public AppointmentActionType getActionType() {
        return actionType;
    }

    public void setActionType(AppointmentActionType actionType) {
        this.actionType = actionType;
    }

    public AppointmentReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(AppointmentReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getOldStartTime() {
        return oldStartTime;
    }

    public void setOldStartTime(LocalDateTime oldStartTime) {
        this.oldStartTime = oldStartTime;
    }

    public LocalDateTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalDateTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public AppointmentStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(AppointmentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public AppointmentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(AppointmentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (actionTimestamp == null) {
            actionTimestamp = LocalDateTime.now();
        }
    }
}
