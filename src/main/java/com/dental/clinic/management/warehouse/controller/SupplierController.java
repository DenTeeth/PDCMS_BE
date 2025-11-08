package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/warehouse/suppliers")
@Tag(name = "Warehouse - Suppliers", description = "APIs for managing medical suppliers")
public class SupplierController {

    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Create a new supplier.
     * POST /api/v3/warehouse/suppliers
     *
     * @param request CreateSupplierRequest body
     * @return 201 Created with SupplierResponse
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Tạo nhà cung cấp mới", description = "Tạo thông tin nhà cung cấp và vật tư")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        log.info("REST request to create supplier: {}", request.getSupplierName());
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all suppliers with pagination.
     * GET
     * /api/v3/warehouse/suppliers?page=0&size=10&sortBy=createdAt&sortDirection=DESC
     *
     * @param page          page number (0-indexed, default: 0)
     * @param size          page size (default: 10, max: 100)
     * @param sortBy        sort field name (default: newest first)
     * @param sortDirection ASC or DESC (default: DESC)
     * @return 200 OK with Page<SupplierResponse>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Lấy tất cả nhà cung cấp", description = "Lấy danh sách tất cả nhà cung cấp")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get all suppliers: page={}, size={}", page, size);
        Page<SupplierResponse> response = supplierService.getAllSuppliers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Search suppliers by keyword.
     * GET /api/v3/warehouse/suppliers/search?keyword=abc&page=0&size=10
     *
     * Search in: name, phone, email, address (case-insensitive)
     *
     * @param keyword search keyword
     * @param page    page number (0-indexed, default: 0)
     * @param size    page size (default: 10, max: 100)
     * @return 200 OK with Page<SupplierResponse>
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Tìm kiếm nhà cung cấp", description = "Tìm kiếm nhà cung cấp theo tên, email, số điện thoại")
    public ResponseEntity<Page<SupplierResponse>> searchSuppliers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("REST request to search suppliers: keyword='{}', page={}, size={}", keyword, page, size);
        Page<SupplierResponse> response = supplierService.searchSuppliers(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get supplier by ID.
     * GET /api/v3/warehouse/suppliers/{supplierId}
     *
     * @param supplierId supplier ID
     * @return 200 OK with SupplierResponse
     */
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Lấy thông tin nhà cung cấp", description = "Lấy chi tiết nhà cung cấp theo ID")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long supplierId) {
        log.info("REST request to get supplier by ID: {}", supplierId);
        SupplierResponse response = supplierService.getSupplierById(supplierId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing supplier.
     * PUT /api/v3/warehouse/suppliers/{supplierId}
     *
     * @param supplierId supplier ID
     * @param request    UpdateSupplierRequest body
     * @return 200 OK with SupplierResponse
     */
    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Cập nhật nhà cung cấp", description = "Cập nhật thông tin nhà cung cấp")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        log.info("REST request to update supplier: {}", supplierId);
        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a supplier.
     * DELETE /api/v3/warehouse/suppliers/{supplierId}
     *
     * @param supplierId supplier ID
     * @return 204 No Content
     */
    @DeleteMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Xóa nhà cung cấp", description = "Xóa nhà cung cấp (kiểm tra ràng buộc)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long supplierId) {
        log.info("REST request to delete supplier: {}", supplierId);
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }
}
