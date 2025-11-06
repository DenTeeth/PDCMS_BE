package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * Response DTO for Item Master summary with calculated stock levels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemMasterSummaryResponse {

    private Long id;
    private String itemName;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer minStockLevel;
    private Integer maxStockLevel;

    // Calculated fields
    private Integer totalQuantityOnHand;
    private String stockStatus; // LOW_STOCK, NORMAL, OVERSTOCK, OUT_OF_STOCK
    private Integer activeBatchCount;
    private Integer expiringSoonCount;
}

