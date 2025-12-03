package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for API 8.5: Add Procedure to Clinical Record
 *
 * Purpose: Record a procedure/service performed during the appointment
 * Authorization: WRITE_CLINICAL_RECORD
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProcedureRequest {

    /**
     * Service ID from service catalog
     * Required - Links to services table
     */
    @NotNull(message = "Service ID is required")
    @Positive(message = "Service ID must be positive")
    private Long serviceId;

    /**
     * Optional link to treatment plan item
     * If provided, creates passive link (no status update)
     * Status updates happen via appointment completion or API 5.6
     */
    private Long patientPlanItemId;

    /**
     * Tooth number or identifier
     * Examples: "36", "21", "ALL" (for full arch)
     * Optional - not all procedures are tooth-specific
     */
    @Size(max = 10, message = "Tooth number must not exceed 10 characters")
    private String toothNumber;

    /**
     * Detailed description of what was performed
     * Required by schema (clinical_record_procedures.procedure_description NOT
     * NULL)
     *
     * Examples:
     * - "Tram xoang II mat O-D, rang 36, mau A3, lot MTA"
     * - "Lay cao rang toan ham, sieu am rang so 3"
     * - "Nho rang khon ham duoi ben phai, khau 2 mui"
     */
    @NotBlank(message = "Procedure description is required")
    @Size(min = 3, max = 1000, message = "Procedure description must be between 3 and 1000 characters")
    private String procedureDescription;

    /**
     * Additional clinical notes
     * Optional - for extra observations or follow-up instructions
     *
     * Examples:
     * - "Benh nhan khong dau, hen tai kham sau 1 tuan"
     * - "Can theo doi tinh trang sau 3 ngay"
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
