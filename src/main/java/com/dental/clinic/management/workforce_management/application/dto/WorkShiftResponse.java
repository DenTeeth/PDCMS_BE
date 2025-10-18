package com.dental.clinic.management.workforce_management.application.dto;

import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.enums.WorkShiftCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Response DTO for work shift data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftResponse {

    private String workShiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private WorkShiftCategory category;
    private Boolean isActive;
    private Double durationHours;
}