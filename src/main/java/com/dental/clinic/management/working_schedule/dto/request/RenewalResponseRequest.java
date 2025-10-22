package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for responding to a shift renewal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewalResponseRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "CONFIRMED|DECLINED", message = "Action must be either CONFIRMED or DECLINED")
    private String action; // "CONFIRMED" or "DECLINED"
}
