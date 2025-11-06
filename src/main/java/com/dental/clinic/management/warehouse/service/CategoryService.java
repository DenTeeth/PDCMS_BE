package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.warehouse.domain.Category;
import com.dental.clinic.management.warehouse.dto.request.CreateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.response.CategoryResponse;
import com.dental.clinic.management.warehouse.exception.CategoryNotFoundException;
import com.dental.clinic.management.warehouse.mapper.CategoryMapper;
import com.dental.clinic.management.warehouse.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Category management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Create a new category.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.CREATE_WAREHOUSE_CATEGORY + "')")
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.debug("Creating new category: {}", request.getCategoryName());

        Category category = categoryMapper.toEntity(request);

        // Set parent category if provided
        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Update an existing category.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.UPDATE_WAREHOUSE_CATEGORY + "')")
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.debug("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        categoryMapper.updateEntity(category, request);

        // Update parent category if provided
        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category with ID: {}", updatedCategory.getId());

        return categoryMapper.toResponse(updatedCategory);
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_CATEGORY + "')")
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Getting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        return categoryMapper.toResponse(category);
    }

    /**
     * Get all categories.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_CATEGORY + "')")
    public List<CategoryResponse> getAllCategories() {
        log.debug("Getting all categories");

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get root categories (categories without parent).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_CATEGORY + "')")
    public List<CategoryResponse> getRootCategories() {
        log.debug("Getting root categories");

        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(categoryMapper::toHierarchicalResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete category.
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.DELETE_WAREHOUSE_CATEGORY + "')")
    public void deleteCategory(UUID id) {
        log.debug("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Check if category has sub-categories
        if (category.hasSubCategories()) {
            throw new IllegalStateException("Cannot delete category with sub-categories");
        }

        // Check if category has items
        if (category.getItemMasters() != null && !category.getItemMasters().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with items");
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", id);
    }
}
