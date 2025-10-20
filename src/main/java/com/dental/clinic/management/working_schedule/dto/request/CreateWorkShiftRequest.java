package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;

/**
 * DTO for creating a new work shift.
 * Note: shiftId is auto-generated based on time of day.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkShiftRequest {

    @NotBlank(message = "Shift name is required")
    @Size(max = 100, message = "Shift name must not exceed 100 characters")
    private String shiftName;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Category is required")
    private WorkShiftCategory category;
}
