package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new overtime request.
 * The request_id and requested_by are auto-generated from the authenticated user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOvertimeRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Integer employeeId;

    @NotNull(message = "Work date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @NotBlank(message = "Work shift ID is required")
    private String workShiftId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
