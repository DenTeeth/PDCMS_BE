package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a fixed shift registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFixedRegistrationRequest {

    @JsonProperty("employee_id")
    @NotNull(message = "Employee ID is required")
    private Integer employeeId;

    @JsonProperty("work_shift_id")
    @NotBlank(message = "Work shift ID is required")
    private String workShiftId;

    @JsonProperty("days_of_week")
    @NotEmpty(message = "Days of week cannot be empty")
    private List<Integer> daysOfWeek;

    @JsonProperty("effective_from")
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @JsonProperty("effective_to")
    private LocalDate effectiveTo; // null = permanent
}
