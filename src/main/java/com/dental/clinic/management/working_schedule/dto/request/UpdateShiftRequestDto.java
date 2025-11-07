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
}
