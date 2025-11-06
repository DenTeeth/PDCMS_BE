package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Inventory statistics dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatsResponse {

    private Integer totalItems;
    private Integer lowStockItems;
    private Integer outOfStockItems;
    private Integer expiredBatches;
    private Integer expiringSoonBatches;
    private BigDecimal totalInventoryValue;
}

