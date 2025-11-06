package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.request.CreateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateCategoryRequest;
import com.dental.clinic.management.warehouse.dto.response.CategoryResponse;
import com.dental.clinic.management.warehouse.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/warehouse/categories")
@RequiredArgsConstructor
@Tag(name = "Warehouse - Categories", description = "Quản lý danh mục vật tư")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Tạo danh mục mới", description = "Tạo danh mục vật tư (hỗ trợ phân cấp)")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Cập nhật danh mục", description = "Cập nhật thông tin danh mục")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Lấy thông tin danh mục", description = "Lấy chi tiết danh mục theo ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục", description = "Lấy danh sách tất cả danh mục")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roots")
    @Operation(summary = "Lấy danh mục gốc", description = "Lấy danh sách danh mục gốc (không có parent)")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Xóa danh mục", description = "Xóa danh mục (kiểm tra ràng buộc)")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
