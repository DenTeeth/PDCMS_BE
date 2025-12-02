package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing procedure in a clinical record (API 8.6)
 *
 * <p>Business Rules:</p>
 * <ul>
 *   <li>Can update service, toothNumber, procedureDescription, notes</li>
 *   <li>Can update patientPlanItemId (link/unlink from treatment plan)</li>
 *   <li>Does NOT update procedure status (handled by appointment completion)</li>
 *   <li>serviceId required - procedure must always reference a service</li>
 *   <li>procedureDescription required - database constraint</li>
 * </ul>
 *
 * @author Dental Clinic System
 * @since API 8.6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureRequest {

    /**
     * Service ID from catalog (required)
     * Must exist in services table and be active
     */
    @Positive(message = "Service ID must be positive")
    private Long serviceId;

    /**
     * Treatment plan item ID (optional)
     * Set to null to unlink from treatment plan
     * If provided, must exist in patient_plan_items
     */
    private Long patientPlanItemId;

    /**
     * Tooth identifier (optional)
     * Examples: "16", "36", "ALL", "21-24"
     */
    @Size(max = 10, message = "Tooth number cannot exceed 10 characters")
    private String toothNumber;

    /**
     * Detailed description of procedure performed (required)
     * Include: technique, materials, duration, observations
     * Example: "Tram xoang II mat O-D, rang 36, Composite mau A3, lot MTA"
     */
    @NotBlank(message = "Procedure description is required")
    @Size(min = 3, max = 1000, message = "Procedure description must be between 3 and 1000 characters")
    private String procedureDescription;

    /**
     * Additional clinical notes or follow-up instructions (optional)
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
