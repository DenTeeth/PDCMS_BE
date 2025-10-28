package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("employee_id")
    @NotNull(message = "Employee ID is required")
    private Integer employeeId;

    @JsonProperty("work_date")
    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @JsonProperty("work_shift_id")
    @NotBlank(message = "Work shift ID is required")
    @Size(max = 20, message = "Work shift ID must not exceed 20 characters")
    private String workShiftId;

    @JsonProperty("notes")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
