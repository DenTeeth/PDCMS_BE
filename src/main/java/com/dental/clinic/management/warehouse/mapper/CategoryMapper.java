package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.Category;
import com.dental.clinic.management.warehouse.dto.request.CreateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Category entity.
 */
@Component
public class CategoryMapper {

    /**
     * Convert CreateCategoryRequest to Category entity.
     */
    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setWarehouseType(request.getWarehouseType());
        category.setDescription(request.getDescription());

        return category;
    }

    /**
     * Update entity from UpdateCategoryRequest (only non-null fields).
     */
    public void updateEntity(Category entity, UpdateCategoryRequest request) {
        if (request.getCategoryName() != null) {
            entity.setCategoryName(request.getCategoryName());
        }
        if (request.getWarehouseType() != null) {
            entity.setWarehouseType(request.getWarehouseType());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
    }

    /**
     * Convert Category to response (non-hierarchical).
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        Integer itemCount = category.getItemMasters() != null
                ? category.getItemMasters().size()
                : 0;

        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .warehouseType(category.getWarehouseType())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(
                        category.getParentCategory() != null ? category.getParentCategory().getCategoryName() : null)
                .description(category.getDescription())
                .itemCount(itemCount)
                .subCategories(null) // Not included in simple response
                .build();
    }

    /**
     * Convert Category to hierarchical response with sub-categories.
     */
    public CategoryResponse toHierarchicalResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = toResponse(category);

        // Recursively map sub-categories
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<CategoryResponse> subCategoryResponses = category.getSubCategories().stream()
                    .map(this::toHierarchicalResponse)
                    .collect(Collectors.toList());
            response.setSubCategories(subCategoryResponses);
        }

        return response;
    }
}
