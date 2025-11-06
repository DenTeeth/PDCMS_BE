package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Response DTO for Category with hierarchical structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String categoryName;
    private WarehouseType warehouseType;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private Integer itemCount;
    private List<CategoryResponse> subCategories;
}

