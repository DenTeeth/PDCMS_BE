package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for API 8.5: Add Procedure to Clinical Record
 *
 * Returns procedure details with service information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProcedureResponse {

    /**
     * Generated procedure ID
     */
    private Integer procedureId;

    /**
     * Clinical record ID (parent record)
     */
    private Integer clinicalRecordId;

    /**
     * Service ID from catalog
     */
    private Long serviceId;

    /**
     * Service name (joined from services table)
     */
    private String serviceName;

    /**
     * Service code (joined from services table)
     */
    private String serviceCode;

    /**
     * Treatment plan item ID (if linked)
     * Null if procedure not part of treatment plan
     */
    private Long patientPlanItemId;

    /**
     * Tooth number or identifier
     */
    private String toothNumber;

    /**
     * Detailed procedure description
     */
    private String procedureDescription;

    /**
     * Additional clinical notes
     */
    private String notes;

    /**
     * Timestamp when procedure was recorded
     */
    private LocalDateTime createdAt;
}
