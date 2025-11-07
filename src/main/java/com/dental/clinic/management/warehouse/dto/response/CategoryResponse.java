package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.WarehouseType;

import java.util.List;

/**
 * Response DTO for Category with hierarchical structure.
 */
public class CategoryResponse {

    private Long id;
    private String categoryName;
    private WarehouseType warehouseType;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private Integer itemCount;
    private List<CategoryResponse> subCategories;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String categoryName, WarehouseType warehouseType,
            Long parentCategoryId, String parentCategoryName, String description,
            Integer itemCount, List<CategoryResponse> subCategories) {
        this.id = id;
        this.categoryName = categoryName;
        this.warehouseType = warehouseType;
        this.parentCategoryId = parentCategoryId;
        this.parentCategoryName = parentCategoryName;
        this.description = description;
        this.itemCount = itemCount;
        this.subCategories = subCategories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public WarehouseType getWarehouseType() {
        return warehouseType;
    }

    public void setWarehouseType(WarehouseType warehouseType) {
        this.warehouseType = warehouseType;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public List<CategoryResponse> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryResponse> subCategories) {
        this.subCategories = subCategories;
    }
}
