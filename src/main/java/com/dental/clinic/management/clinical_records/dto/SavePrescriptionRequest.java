package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for API 8.15: Save Prescription (Create/Update)
 *
 * Replace Strategy: Clears all existing items and saves new ones
 *
 * Business Rules:
 * - prescriptionNotes: Optional field for doctor's notes
 * - items: Must not be empty (use DELETE API to remove prescription)
 * - Each item must have itemName (required even if not in warehouse)
 * - itemMasterId is nullable (for medications not in inventory)
 *
 * Authorization: WRITE_CLINICAL_RECORD (Doctor, Assistant, Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavePrescriptionRequest {

    /**
     * Doctor's notes about the prescription
     * Examples: "Kieng do chua cay, uong nhieu nuoc", "Tai kham sau 5 ngay"
     */
    @Size(max = 2000, message = "Prescription notes must not exceed 2000 characters")
    private String prescriptionNotes;

    /**
     * List of prescription items (medications)
     * Must contain at least one item
     * If you want to delete prescription, use DELETE API instead
     */
    @NotEmpty(message = "Prescription must contain at least one item. Use DELETE API to remove prescription.")
    @Valid
    private List<PrescriptionItemRequest> items;
}
