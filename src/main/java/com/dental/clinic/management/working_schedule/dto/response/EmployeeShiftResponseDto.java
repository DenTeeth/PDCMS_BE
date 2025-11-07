package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for employee shift details.
 * Used for both single shift detail and calendar list views.
 */
public class EmployeeShiftResponseDto {

    @JsonProperty("employee_shift_id")
    private String employeeShiftId;

    @JsonProperty("employee")
    private EmployeeBasicDto employee;

    @JsonProperty("work_date")
    private LocalDate workDate;

    @JsonProperty("work_shift")
    private WorkShiftBasicDto workShift;

    @JsonProperty("source")
    private ShiftSource source;

    @JsonProperty("status")
    private ShiftStatus status;

    @JsonProperty("is_overtime")
    private Boolean isOvertime;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_by_name")
    private String createdByName;

    @JsonProperty("source_ot_request_id")
    private String sourceOtRequestId;

    @JsonProperty("source_off_request_id")
    private String sourceOffRequestId;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public EmployeeShiftResponseDto() {
    }

    public EmployeeShiftResponseDto(String employeeShiftId, EmployeeBasicDto employee, LocalDate workDate,
            WorkShiftBasicDto workShift, ShiftSource source, ShiftStatus status,
            Boolean isOvertime, Integer createdBy, String createdByName,
            String sourceOtRequestId, String sourceOffRequestId, String notes,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.employeeShiftId = employeeShiftId;
        this.employee = employee;
        this.workDate = workDate;
        this.workShift = workShift;
        this.source = source;
        this.status = status;
        this.isOvertime = isOvertime;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.sourceOtRequestId = sourceOtRequestId;
        this.sourceOffRequestId = sourceOffRequestId;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getEmployeeShiftId() {
        return employeeShiftId;
    }

    public void setEmployeeShiftId(String employeeShiftId) {
        this.employeeShiftId = employeeShiftId;
    }

    public EmployeeBasicDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeBasicDto employee) {
        this.employee = employee;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public WorkShiftBasicDto getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShiftBasicDto workShift) {
        this.workShift = workShift;
    }

    public ShiftSource getSource() {
        return source;
    }

    public void setSource(ShiftSource source) {
        this.source = source;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public Boolean getIsOvertime() {
        return isOvertime;
    }

    public void setIsOvertime(Boolean isOvertime) {
        this.isOvertime = isOvertime;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getSourceOtRequestId() {
        return sourceOtRequestId;
    }

    public void setSourceOtRequestId(String sourceOtRequestId) {
        this.sourceOtRequestId = sourceOtRequestId;
    }

    public String getSourceOffRequestId() {
        return sourceOffRequestId;
    }

    public void setSourceOffRequestId(String sourceOffRequestId) {
        this.sourceOffRequestId = sourceOffRequestId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Nested DTO for basic employee information.
     */
    public static class EmployeeBasicDto {

        @JsonProperty("employee_id")
        private Integer employeeId;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("position")
        private String position;

        public EmployeeBasicDto() {
        }

        public EmployeeBasicDto(Integer employeeId, String fullName, String position) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.position = position;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    /**
     * Nested DTO for basic work shift information.
     */
    public static class WorkShiftBasicDto {

        @JsonProperty("work_shift_id")
        private String workShiftId;

        @JsonProperty("shift_name")
        private String shiftName;

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("end_time")
        private String endTime;

        public WorkShiftBasicDto() {
        }

        public WorkShiftBasicDto(String workShiftId, String shiftName, String startTime, String endTime) {
            this.workShiftId = workShiftId;
            this.shiftName = shiftName;
            this.startTime = startTime;
            this.endTime = endTime;
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

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
}
