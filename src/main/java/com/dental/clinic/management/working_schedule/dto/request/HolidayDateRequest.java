package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for creating/updating a holiday date.
 */
public class HolidayDateRequest {

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @NotBlank(message = "Definition ID is required")
    private String definitionId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public HolidayDateRequest() {
    }

    public HolidayDateRequest(LocalDate holidayDate, String definitionId, String description) {
        this.holidayDate = holidayDate;
        this.definitionId = definitionId;
        this.description = description;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
