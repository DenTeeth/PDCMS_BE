package com.dental.clinic.management.treatment_plans.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for adding a new item to a treatment plan phase.
 * Used by API 5.7: POST /api/v1/patient-plan-phases/{phaseId}/items
 *
 * Design Philosophy:
 * - NO sequenceNumber field → Backend auto-generates (append to end of phase)
 * - This avoids sequence conflicts and gaps
 * - Quantity expansion: 1 service × 2 quantity = 2 separate items
 *
 * Use Case:
 * Doctor discovers 2 cavities during orthodontic checkup → Add FILLING_COMP
 * service × 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add new item(s) to a treatment plan phase")
public class AddItemToPhaseRequest {

    /**
     * Service code to add (from services table)
     * Example: "FILLING_COMP", "SCALING_L1"
     */
    @NotBlank(message = "Mã dịch vụ là bắt buộc")
    @Size(max = 50, message = "Mã dịch vụ không được vượt quá 50 ký tự")
    @Schema(description = "Code of the service to add (will snapshot service details)", example = "FILLING_COMP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceCode;

    /**
     * Snapshot price for this item (V21.4: OPTIONAL).
     * If not provided, will auto-fill from service default price.
     * Doctors typically omit this field (pricing managed by Finance team).
     * Price override validation removed in V21.4.
     */
    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    @Digits(integer = 10, fraction = 2, message = "Giá phải có tối đa 10 chữ số nguyên và 2 chữ số thập phân")
    @Schema(description = "Price for this item (optional, auto-fills from service default)", example = "400000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal price;

    /**
     * Quantity: How many times to add this service
     * Backend will expand: quantity=2 → create 2 separate items with sequence
     * numbers
     * Example: Doctor finds 2 cavities → quantity=2 → creates 2 items:
     * - "Trám răng Composite (Phát sinh - Lần 1)"
     * - "Trám răng Composite (Phát sinh - Lần 2)"
     */
    @NotNull(message = "Số lượng là bắt buộc")
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    @Max(value = 10, message = "Số lượng không được vượt quá 10")
    @Schema(description = "Number of times to add this service (will create separate items)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    /**
     * Optional notes explaining why this item is being added
     * Important for approval workflow: Manager needs to know reason for cost change
     * Example: "Phát hiện 2 răng sâu mặt nhai 46, 47 tại tái khám"
     */
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    @Schema(description = "Notes explaining reason for adding this item (important for approval workflow)", example = "Phát hiện 2 răng sâu mặt nhai 46, 47 tại tái khám ngày 15/01/2024", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String notes;
}
