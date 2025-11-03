package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.utils.ResponseMessage;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Supplier management.
 * 
 * Base URL: /api/v1/suppliers
 * 
 * Permissions:
 * - CREATE_SUPPLIER: Create new supplier (ADMIN only)
 * - VIEW_SUPPLIER: View suppliers (ADMIN and STAFF)
 * - UPDATE_SUPPLIER: Update supplier (ADMIN only)
 * - DELETE_SUPPLIER: Delete supplier (ADMIN only)
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse - Suppliers", description = "APIs for managing medical suppliers (ADMIN and STAFF)")
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Create a new supplier.
     * 
     * POST /api/v1/suppliers
     * 
     * @param request CreateSupplierRequest body
     * @return 201 Created with SupplierResponse
     */
    @PostMapping
    @Operation(summary = "Create new supplier", description = "Create a new medical supplier (ADMIN only)")
    @ApiMessage("Tạo nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        log.info("REST request to create supplier: {}", request.getSupplierName());
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all suppliers with pagination.
     * 
     * GET /api/v1/suppliers?page=0&size=10&sortBy=createdAt&sortDirection=DESC
     * 
     * @param page          page number (0-indexed, default: 0)
     * @param size          page size (default: 10, max: 100)
     * @param sortBy        sort field name (default: newest first)
     * @param sortDirection ASC or DESC (default: DESC)
     * @return 200 OK with Page<SupplierResponse>
     */
    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Retrieve paginated list of suppliers (ADMIN and STAFF)")
    @ApiMessage("Lấy danh sách nhà cung cấp thành công")
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
     * 
     * GET /api/v1/suppliers/search?keyword=abc&page=0&size=10
     * 
     * Search in: name, phone, email, address (case-insensitive, accent-insensitive)
     * 
     * @param keyword search keyword
     * @param page    page number (0-indexed, default: 0)
     * @param size    page size (default: 10, max: 100)
     * @return 200 OK with Page<SupplierResponse>
     */
    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Search suppliers by keyword in name, phone, email, address (ADMIN and STAFF)")
    @ApiMessage("Tìm kiếm nhà cung cấp thành công")
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
     * 
     * GET /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier ID
     * @return 200 OK with SupplierResponse
     */
    @GetMapping("/{supplierId}")
    @Operation(summary = "Get supplier by ID", description = "Get supplier details by ID (ADMIN and STAFF)")
    @ApiMessage("Lấy thông tin nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable Long supplierId) {
        log.info("REST request to get supplier by ID: {}", supplierId);
        SupplierResponse response = supplierService.getSupplierById(supplierId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing supplier.
     * 
     * PUT /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier ID
     * @param request    UpdateSupplierRequest body
     * @return 200 OK with SupplierResponse
     */
    @PutMapping("/{supplierId}")
    @Operation(summary = "Update supplier", description = "Update existing supplier (ADMIN only)")
    @ApiMessage("Cập nhật nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        log.info("REST request to update supplier: {}", supplierId);
        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a supplier.
     * 
     * DELETE /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier ID
     * @return 200 OK with success message
     */
    @DeleteMapping("/{supplierId}")
    @Operation(summary = "Delete supplier", description = "Delete supplier by ID (ADMIN only)")
    @ApiMessage("Xóa nhà cung cấp thành công")
    public ResponseEntity<ResponseMessage> deleteSupplier(@PathVariable Long supplierId) {
        log.info("REST request to delete supplier: {}", supplierId);
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.ok(new ResponseMessage("Xóa nhà cung cấp thành công"));
    }
}
