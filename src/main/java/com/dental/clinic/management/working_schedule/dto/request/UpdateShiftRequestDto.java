package com.dental.clinic.management.working_schedule.dto.request;

import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for updating an employee shift.
 */
public class UpdateShiftRequestDto {

    @JsonProperty("status")
    private ShiftStatus status;

    @JsonProperty("notes")
    private String notes;

    public UpdateShiftRequestDto() {
    }

    public UpdateShiftRequestDto(ShiftStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
