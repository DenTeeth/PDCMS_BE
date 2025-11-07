package com.dental.clinic.management.warehouse.dto.response;

import java.math.BigDecimal;

/**
 * Response DTO for Inventory statistics dashboard.
 */
public class InventoryStatsResponse {

    private Integer totalItems;
    private Integer lowStockItems;
    private Integer outOfStockItems;
    private Integer expiredBatches;
    private Integer expiringSoonBatches;
    private BigDecimal totalInventoryValue;

    public InventoryStatsResponse() {
    }

    public InventoryStatsResponse(Integer totalItems, Integer lowStockItems, Integer outOfStockItems,
            Integer expiredBatches, Integer expiringSoonBatches, BigDecimal totalInventoryValue) {
        this.totalItems = totalItems;
        this.lowStockItems = lowStockItems;
        this.outOfStockItems = outOfStockItems;
        this.expiredBatches = expiredBatches;
        this.expiringSoonBatches = expiringSoonBatches;
        this.totalInventoryValue = totalInventoryValue;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Integer lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public Integer getOutOfStockItems() {
        return outOfStockItems;
    }

    public void setOutOfStockItems(Integer outOfStockItems) {
        this.outOfStockItems = outOfStockItems;
    }

    public Integer getExpiredBatches() {
        return expiredBatches;
    }

    public void setExpiredBatches(Integer expiredBatches) {
        this.expiredBatches = expiredBatches;
    }

    public Integer getExpiringSoonBatches() {
        return expiringSoonBatches;
    }

    public void setExpiringSoonBatches(Integer expiringSoonBatches) {
        this.expiringSoonBatches = expiringSoonBatches;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }
}
