package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for material update operations
 * Used by API 8.8: Update Procedure Materials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureMaterialsResponse {

    private String message;
    private Integer procedureId;
    private Integer materialsUpdated;
    private List<StockAdjustmentDTO> stockAdjustments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAdjustmentDTO {
        private String itemName;
        private Double adjustment; // Positive = additional deduction, Negative = reverse
        private String reason;
    }
}
