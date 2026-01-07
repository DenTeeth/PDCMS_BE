package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating editable material quantity before deduction
 * Allows users to customize material quantity for specific procedure step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaterialQuantityRequest {

    @NotNull(message = "Usage ID is required")
    private Long usageId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;
}
