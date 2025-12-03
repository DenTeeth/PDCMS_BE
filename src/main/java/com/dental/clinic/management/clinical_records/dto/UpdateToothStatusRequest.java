package com.dental.clinic.management.clinical_records.dto;

import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating patient tooth status (Odontogram)
 * API 8.10
 *
 * Business Rules:
 * - toothNumber must be valid FDI notation
 * - If status = HEALTHY, the record will be deleted (tooth returns to default
 * state)
 * - If record exists, it will be updated
 * - If record doesn't exist, it will be created
 *
 * @author Dental Clinic System
 * @since API 8.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateToothStatusRequest {

    /**
     * Tooth number in FDI notation (international standard)
     * Must match pattern: 11-18, 21-28, 31-38, 41-48
     * Examples: "11", "36", "48"
     */
    @NotBlank(message = "Tooth number is required")
    @Pattern(regexp = "^(1[1-8]|2[1-8]|3[1-8]|4[1-8])$", message = "Invalid tooth number. Must be FDI notation (11-18, 21-28, 31-38, 41-48)")
    @Size(max = 10, message = "Tooth number must not exceed 10 characters")
    private String toothNumber;

    /**
     * Status of the tooth
     * If set to HEALTHY, the tooth status record will be deleted
     */
    @NotNull(message = "Tooth status is required")
    private ToothConditionEnum status;

    /**
     * Optional notes about the tooth condition
     * Max length: 1000 characters
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
