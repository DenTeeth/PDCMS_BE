package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;

/**
 * DTO for updating an existing work shift.
 * All fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkShiftRequest {

    @Size(max = 100, message = "Shift name must not exceed 100 characters")
    private String shiftName;

    private LocalTime startTime;

    private LocalTime endTime;

    private WorkShiftCategory category;
}
