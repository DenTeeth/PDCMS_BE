package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for detailed overtime request response.
 * Used for GET /api/v1/overtime-requests/{request_id}
 */
public class OvertimeRequestDetailResponse {

    private String requestId;

    private EmployeeBasicInfo employee;

    private EmployeeBasicInfo requestedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private WorkShiftInfo workShift;

    private String reason;

    private RequestStatus status;

    private EmployeeBasicInfo approvedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private String rejectedReason;

    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // No-args constructor
    public OvertimeRequestDetailResponse() {
    }

    // All-args constructor
    public OvertimeRequestDetailResponse(String requestId, EmployeeBasicInfo employee, EmployeeBasicInfo requestedBy,
            LocalDate workDate, WorkShiftInfo workShift, String reason, RequestStatus status,
            EmployeeBasicInfo approvedBy, LocalDateTime approvedAt, String rejectedReason,
            String cancellationReason, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.employee = employee;
        this.requestedBy = requestedBy;
        this.workDate = workDate;
        this.workShift = workShift;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectedReason = rejectedReason;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
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

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public WorkShiftInfo getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShiftInfo workShift) {
        this.workShift = workShift;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Basic employee information for overtime request.
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

    /**
     * Work shift information for overtime request.
     */
    public static class WorkShiftInfo {
        private String workShiftId;
        private String shiftName;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double durationHours;

        // No-args constructor
        public WorkShiftInfo() {
        }

        // All-args constructor
        public WorkShiftInfo(String workShiftId, String shiftName, LocalTime startTime, LocalTime endTime,
                Double durationHours) {
            this.workShiftId = workShiftId;
            this.shiftName = shiftName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationHours = durationHours;
        }

        public String getWorkShiftId() {
            return workShiftId;
        }

        public void setWorkShiftId(String workShiftId) {
            this.workShiftId = workShiftId;
        }

        public String getShiftName() {
            return shiftName;
        }

        public void setShiftName(String shiftName) {
            this.shiftName = shiftName;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }

        public Double getDurationHours() {
            return durationHours;
        }

        public void setDurationHours(Double durationHours) {
            this.durationHours = durationHours;
        }
    }
}
