package com.dental.clinic.management.employee_shifts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new employee shift.
 * This assigns an employee to a specific work shift on a given date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShiftRequestDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @NotBlank(message = "Work shift ID is required")
    @Size(max = 20, message = "Work shift ID must not exceed 20 characters")
    private String workShiftId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
