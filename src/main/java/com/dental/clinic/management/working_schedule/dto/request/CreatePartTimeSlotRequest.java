package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartTimeSlotRequest {

    @NotBlank(message = "Work shift ID is required")
    private String workShiftId;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @NotNull(message = "Quota is required")
    @Min(value = 1, message = "Quota must be at least 1")
    private Integer quota;
}
