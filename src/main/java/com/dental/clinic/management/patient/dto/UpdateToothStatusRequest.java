package com.dental.clinic.management.patient.dto;

import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating tooth status (API 8.10)
 *
 * @author Dental Clinic System
 * @since API 8.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateToothStatusRequest {

    @NotNull(message = "Status is required")
    private ToothConditionEnum status;

    private String notes;

    private String reason;
}
