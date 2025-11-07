package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for working_schedules table
 * Represents employee time-off requests
 */
@Entity
@Table(name = "time_off_requests")
public class TimeOffRequest {

    private static IdGenerator idGenerator;

    public static void setIdGenerator(IdGenerator generator) {
        idGenerator = generator;
    }

    @Id
    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "time_off_type_id", nullable = false, length = 50)
    private String timeOffTypeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "work_shift_id", length = 50)
    private String workShiftId; // NULL if full day off, value if half-day off

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeOffStatus status = TimeOffStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private Integer requestedBy; // User ID from token

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", insertable = false, updatable = false)
    private Employee requestedByEmployee;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    private Employee approvedByEmployee;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    public TimeOffRequest() {
    }

    public TimeOffRequest(String requestId, Integer employeeId, Employee employee, String timeOffTypeId,
            LocalDate startDate, LocalDate endDate, String workShiftId, String reason,
            TimeOffStatus status, Integer requestedBy, Employee requestedByEmployee,
            LocalDateTime requestedAt, Integer approvedBy, Employee approvedByEmployee,
            LocalDateTime approvedAt, String rejectedReason, String cancellationReason) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employee = employee;
        this.timeOffTypeId = timeOffTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workShiftId = workShiftId;
        this.reason = reason;
        this.status = status;
        this.requestedBy = requestedBy;
        this.requestedByEmployee = requestedByEmployee;
        this.requestedAt = requestedAt;
        this.approvedBy = approvedBy;
        this.approvedByEmployee = approvedByEmployee;
        this.approvedAt = approvedAt;
        this.rejectedReason = rejectedReason;
        this.cancellationReason = cancellationReason;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getTimeOffTypeId() {
        return timeOffTypeId;
    }

    public void setTimeOffTypeId(String timeOffTypeId) {
        this.timeOffTypeId = timeOffTypeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public TimeOffStatus getStatus() {
        return status;
    }

    public void setStatus(TimeOffStatus status) {
        this.status = status;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Employee getRequestedByEmployee() {
        return requestedByEmployee;
    }

    public void setRequestedByEmployee(Employee requestedByEmployee) {
        this.requestedByEmployee = requestedByEmployee;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Employee getApprovedByEmployee() {
        return approvedByEmployee;
    }

    public void setApprovedByEmployee(Employee approvedByEmployee) {
        this.approvedByEmployee = approvedByEmployee;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @PrePersist
    protected void onCreate() {
        if (requestId == null && idGenerator != null) {
            this.requestId = idGenerator.generateId("TOR");
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "TimeOffRequest{" +
                "requestId='" + requestId + '\'' +
                ", employeeId=" + employeeId +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
