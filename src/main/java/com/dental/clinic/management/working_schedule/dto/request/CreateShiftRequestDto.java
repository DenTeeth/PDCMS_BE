package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating a manual employee shift.
 */
public class CreateShiftRequestDto {

    @NotNull(message = "employee_id khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @JsonProperty("employee_id")
    private Integer employeeId;

    @NotNull(message = "work_date khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @JsonProperty("work_date")
    private LocalDate workDate;

    @NotNull(message = "work_shift_id khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ' trÃ¡Â»â€˜ng")
    @JsonProperty("work_shift_id")
    private String workShiftId;

    @JsonProperty("notes")
    private String notes;

    public CreateShiftRequestDto() {
    }

    public CreateShiftRequestDto(Integer employeeId, LocalDate workDate, String workShiftId, String notes) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.workShiftId = workShiftId;
        this.notes = notes;
    }

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
