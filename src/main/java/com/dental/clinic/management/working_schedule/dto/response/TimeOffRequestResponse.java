package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for TimeOffRequest
 */
public class TimeOffRequestResponse {

    private String requestId;

    private EmployeeBasicInfo employee;

    private EmployeeBasicInfo requestedBy;

    private String timeOffTypeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String workShiftId;

    private String reason;

    private TimeOffStatus status;

    private EmployeeBasicInfo approvedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private String rejectedReason;

    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;

    // No-args constructor
    public TimeOffRequestResponse() {
    }

    // All-args constructor
    public TimeOffRequestResponse(String requestId, EmployeeBasicInfo employee, EmployeeBasicInfo requestedBy,
            String timeOffTypeId, LocalDate startDate, LocalDate endDate, String workShiftId, String reason,
            TimeOffStatus status, EmployeeBasicInfo approvedBy, LocalDateTime approvedAt, String rejectedReason,
            String cancellationReason, LocalDateTime requestedAt) {
        this.requestId = requestId;
        this.employee = employee;
        this.requestedBy = requestedBy;
        this.timeOffTypeId = timeOffTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workShiftId = workShiftId;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectedReason = rejectedReason;
        this.cancellationReason = cancellationReason;
        this.requestedAt = requestedAt;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public EmployeeBasicInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeBasicInfo employee) {
        this.employee = employee;
    }

    public EmployeeBasicInfo getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(EmployeeBasicInfo requestedBy) {
        this.requestedBy = requestedBy;
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

    public EmployeeBasicInfo getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(EmployeeBasicInfo approvedBy) {
        this.approvedBy = approvedBy;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    /**
     * Basic employee information for time-off request.
     */
    public static class EmployeeBasicInfo {
        private Integer employeeId;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String fullName;

        // No-args constructor
        public EmployeeBasicInfo() {
        }

        // All-args constructor
        public EmployeeBasicInfo(Integer employeeId, String employeeCode, String firstName, String lastName,
                String fullName) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = fullName;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
