package com.dental.clinic.management.clinical_records.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for updating an existing procedure (API 8.6)
 *
 * <p>Returns complete procedure details after successful update</p>
 *
 * @author Dental Clinic System
 * @since API 8.6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureResponse {

    /**
     * Procedure ID (primary key)
     */
    private Integer procedureId;

    /**
     * Parent clinical record ID
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
     * Treatment plan item ID (null if not linked)
     */
    private Long patientPlanItemId;

    /**
     * Tooth identifier
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
     * Original creation timestamp (unchanged by update)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
