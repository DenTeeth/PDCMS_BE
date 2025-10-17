package com.dental.clinic.management.work_shifts.dto.request;

import com.dental.clinic.management.work_shifts.enums.WorkShiftCategory;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

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
