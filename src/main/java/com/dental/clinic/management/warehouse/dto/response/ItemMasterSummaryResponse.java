package com.dental.clinic.management.warehouse.dto.response;

/**
 * Response DTO for Item Master summary with calculated stock levels.
 */
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

    public ItemMasterSummaryResponse() {
    }

    public ItemMasterSummaryResponse(Long id, String itemName, String description, Long categoryId,
            String categoryName, Integer minStockLevel, Integer maxStockLevel,
            Integer totalQuantityOnHand, String stockStatus, Integer activeBatchCount,
            Integer expiringSoonCount) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.minStockLevel = minStockLevel;
        this.maxStockLevel = maxStockLevel;
        this.totalQuantityOnHand = totalQuantityOnHand;
        this.stockStatus = stockStatus;
        this.activeBatchCount = activeBatchCount;
        this.expiringSoonCount = expiringSoonCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(Integer maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public Integer getTotalQuantityOnHand() {
        return totalQuantityOnHand;
    }

    public void setTotalQuantityOnHand(Integer totalQuantityOnHand) {
        this.totalQuantityOnHand = totalQuantityOnHand;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public Integer getActiveBatchCount() {
        return activeBatchCount;
    }

    public void setActiveBatchCount(Integer activeBatchCount) {
        this.activeBatchCount = activeBatchCount;
    }

    public Integer getExpiringSoonCount() {
        return expiringSoonCount;
    }

    public void setExpiringSoonCount(Integer expiringSoonCount) {
        this.expiringSoonCount = expiringSoonCount;
    }
}
