package com.dental.clinic.management.working_schedule.dto.request;

import com.dental.clinic.management.working_schedule.enums.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating holiday definition.
 */
public class HolidayDefinitionRequest {

    @NotBlank(message = "Definition ID is required")
    @Pattern(regexp = "^[A-Z0-9_]{1,20}$", message = "Definition ID must contain only uppercase letters, numbers, and underscores (max 20 chars)")
    private String definitionId;

    @NotBlank(message = "Holiday name is required")
    @Size(max = 100, message = "Holiday name must not exceed 100 characters")
    private String holidayName;

    @NotNull(message = "Holiday type is required")
    private HolidayType holidayType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public HolidayDefinitionRequest() {
    }

    public HolidayDefinitionRequest(String definitionId, String holidayName, HolidayType holidayType,
            String description) {
        this.definitionId = definitionId;
        this.holidayName = holidayName;
        this.holidayType = holidayType;
        this.description = description;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public HolidayType getHolidayType() {
        return holidayType;
    }

    public void setHolidayType(HolidayType holidayType) {
        this.holidayType = holidayType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
