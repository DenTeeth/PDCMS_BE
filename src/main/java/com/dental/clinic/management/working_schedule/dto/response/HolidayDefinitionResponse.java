package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.HolidayType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for holiday definition.
 */
public class HolidayDefinitionResponse {

    private String definitionId;
    private String holidayName;
    private HolidayType holidayType;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer totalDates; // Number of dates associated with this definition

    public HolidayDefinitionResponse() {
    }

    public HolidayDefinitionResponse(String definitionId, String holidayName, HolidayType holidayType,
            String description, LocalDateTime createdAt, LocalDateTime updatedAt, Integer totalDates) {
        this.definitionId = definitionId;
        this.holidayName = holidayName;
        this.holidayType = holidayType;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalDates = totalDates;
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

    public Integer getTotalDates() {
        return totalDates;
    }

    public void setTotalDates(Integer totalDates) {
        this.totalDates = totalDates;
    }
}
