package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for individual prescription item
 *
 * Business Rules:
 * - itemName: REQUIRED (even if medication not in warehouse)
 * - itemMasterId: Optional (NULL for medications not in inventory)
 * If provided, must exist in item_masters and is_active = true
 * - quantity: Must be greater than 0
 * - dosageInstructions: Full instructions including duration
 * Example: "Sang 1 vien, Toi 1 vien - Dung du 5 ngay"
 *
 * Note: We don't have separate durationDays field (scope creep)
 * Duration is encoded in dosageInstructions text
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemRequest {

    /**
     * Link to warehouse inventory (optional)
     * NULL if medication is not in inventory (buy from external pharmacy)
     * If provided, must be valid and active item_master_id
     */
    private Integer itemMasterId;

    /**
     * Medication name (REQUIRED)
     * Must be provided even if itemMasterId is NULL
     * Example: "Amoxicillin 500mg", "Panadol Extra"
     */
    @NotBlank(message = "Item name is required for all prescription items")
    @Size(max = 255, message = "Item name must not exceed 255 characters")
    private String itemName;

    /**
     * Quantity prescribed
     * Example: 10 (pills), 1 (tube), 2 (bottles)
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    /**
     * Full dosage instructions including duration
     * Example: "Sang 1 vien, Chieu 1 vien, Toi 1 vien (5 ngay)"
     * Example: "Uong khi dau, cach nhau 4-6h, toi da 3 ngay"
     */
    @Size(max = 1000, message = "Dosage instructions must not exceed 1000 characters")
    private String dosageInstructions;
}
