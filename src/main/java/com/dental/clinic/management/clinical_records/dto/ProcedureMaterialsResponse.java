package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for procedure material usage details
 * Used by API 8.7: Get Procedure Materials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureMaterialsResponse {

    private Integer procedureId;
    private String serviceName;
    private String serviceCode;
    private String toothNumber;
    
    // Material deduction status
    private Boolean materialsDeducted;
    private LocalDateTime deductedAt;
    private String deductedBy;
    private Integer storageTransactionId;
    
    // Material items
    private List<MaterialItemDTO> materials;
    
    // Cost summary (requires VIEW_WAREHOUSE_COST permission)
    private BigDecimal totalPlannedCost;
    private BigDecimal totalActualCost;
    private BigDecimal costVariance;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialItemDTO {
        private Long usageId;
        private Long itemMasterId;
        private String itemCode;
        private String itemName;
        private String categoryName;
        
        // Quantities
        private BigDecimal plannedQuantity;
        private BigDecimal actualQuantity;
        private BigDecimal varianceQuantity;
        private String varianceReason;
        private String unitName;
        
        // Cost (requires VIEW_WAREHOUSE_COST permission, null otherwise)
        private BigDecimal unitPrice;
        private BigDecimal totalPlannedCost;
        private BigDecimal totalActualCost;
        
        // Stock status
        private String stockStatus; // OK, LOW, OUT_OF_STOCK
        private Integer currentStock;
        
        // Audit
        private LocalDateTime recordedAt;
        private String recordedBy;
        private String notes;
    }
}
