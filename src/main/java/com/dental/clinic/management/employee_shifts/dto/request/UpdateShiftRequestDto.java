package com.dental.clinic.management.employee_shifts.dto.request;

import com.dental.clinic.management.employee_shifts.enums.EmployeeShiftStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing employee shift.
 * All fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShiftRequestDto {

    private EmployeeShiftStatus status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
