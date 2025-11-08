package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for creating a new overtime request.
 * The request_id and requested_by are auto-generated from the authenticated
 * user.
 */
public class CreateOvertimeRequestDTO {

    // Optional for employee creating their own request (will be auto-filled from
    // JWT)
    // Required for admin creating request for another employee
    private Integer employeeId;

    @NotNull(message = "Work date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @NotBlank(message = "Work shift ID is required")
    private String workShiftId;

    @NotBlank(message = "Reason is required")
    private String reason;

    // Constructors
    public CreateOvertimeRequestDTO() {
    }

    /**
     * Constructor for employee self-request (no employeeId needed)
     */
    public CreateOvertimeRequestDTO(LocalDate workDate, String workShiftId, String reason) {
        this.workDate = workDate;
        this.workShiftId = workShiftId;
        this.reason = reason;
    }

    public CreateOvertimeRequestDTO(Integer employeeId, LocalDate workDate, String workShiftId, String reason) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.workShiftId = workShiftId;
        this.reason = reason;
    }

    // Getters and Setters
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
