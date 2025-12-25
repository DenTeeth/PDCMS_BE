package com.dental.clinic.management.clinical_records.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating actual material quantities used
 * Used by API 8.8: Update Procedure Materials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureMaterialsRequest {

    @NotEmpty(message = "Materials list cannot be empty")
    @Valid
    private List<MaterialUpdateDTO> materials;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialUpdateDTO {
        
        @NotNull(message = "Usage ID is required")
        private Long usageId;
        
        @NotNull(message = "Actual quantity is required")
        @Positive(message = "Actual quantity must be positive")
        private BigDecimal actualQuantity;
        
        private String varianceReason;
        private String notes;
    }
}
