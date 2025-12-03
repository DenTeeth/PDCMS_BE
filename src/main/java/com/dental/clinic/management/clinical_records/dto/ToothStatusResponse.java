package com.dental.clinic.management.clinical_records.dto;

import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Patient Tooth Status (Odontogram)
 * API 8.9 and 8.10
 *
 * Format: yyyy-MM-dd HH:mm:ss for timestamps
 *
 * @author Dental Clinic System
 * @since API 8.9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothStatusResponse {

    private Integer toothStatusId;

    private Integer patientId;

    /**
     * Tooth number in FDI notation (international standard)
     * Examples: "11" (upper right central incisor), "36" (lower left first molar)
     * Valid range: 11-18, 21-28, 31-38, 41-48
     */
    private String toothNumber;

    /**
     * Current status of the tooth
     * See ToothConditionEnum for all possible values
     */
    private ToothConditionEnum status;

    /**
     * Optional notes about the tooth condition
     * Max length: 1000 characters
     */
    private String notes;

    /**
     * Timestamp when this tooth status was first recorded
     * Format: yyyy-MM-dd HH:mm:ss
     */
    private String recordedAt;

    /**
     * Timestamp of last update
     * Format: yyyy-MM-dd HH:mm:ss
     */
    private String updatedAt;
}
