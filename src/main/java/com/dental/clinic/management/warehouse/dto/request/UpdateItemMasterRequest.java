package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating Item Master.
 * All fields are optional - only updates non-null fields.
 */
public class UpdateItemMasterRequest {

    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Long categoryId;

    @Min(value = 0, message = "Minimum stock level must be >= 0")
    private Integer minStockLevel;

    @Min(value = 0, message = "Maximum stock level must be >= 0")
    private Integer maxStockLevel;

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
}
