package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for overtime request list item.
 * Lighter version for GET /api/v1/overtime-requests (paginated list)
 * Contains only essential information for list view.
 */
public class OvertimeRequestListResponse {

    private String requestId;

    private Integer employeeId;

    private String employeeCode;

    private String employeeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private String workShiftId;

    private String shiftName;

    private RequestStatus status;

    private String requestedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // No-args constructor
    public OvertimeRequestListResponse() {
    }

    // All-args constructor
    public OvertimeRequestListResponse(String requestId, Integer employeeId, String employeeCode, String employeeName,
            LocalDate workDate, String workShiftId, String shiftName, RequestStatus status,
            String requestedByName, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.workDate = workDate;
        this.workShiftId = workShiftId;
        this.shiftName = shiftName;
        this.status = status;
        this.requestedByName = requestedByName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
