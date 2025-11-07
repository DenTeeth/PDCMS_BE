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
    @Operation(summary = "TÃ¡ÂºÂ¡o nhÃƒÂ  cung cÃ¡ÂºÂ¥p mÃ¡Â»â€ºi", description = "TÃ¡ÂºÂ¡o thÃƒÂ´ng tin nhÃƒÂ  cung cÃ¡ÂºÂ¥p vÃ¡ÂºÂ­t tÃ†Â°")
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
    @Operation(summary = "LÃ¡ÂºÂ¥y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ nhÃƒÂ  cung cÃ¡ÂºÂ¥p", description = "LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ nhÃƒÂ  cung cÃ¡ÂºÂ¥p")
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
    @Operation(summary = "TÃƒÂ¬m kiÃ¡ÂºÂ¿m nhÃƒÂ  cung cÃ¡ÂºÂ¥p", description = "TÃƒÂ¬m kiÃ¡ÂºÂ¿m nhÃƒÂ  cung cÃ¡ÂºÂ¥p theo tÃƒÂªn, email, sÃ¡Â»â€˜ Ã„â€˜iÃ¡Â»â€¡n thoÃ¡ÂºÂ¡i")
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
    @Operation(summary = "LÃ¡ÂºÂ¥y thÃƒÂ´ng tin nhÃƒÂ  cung cÃ¡ÂºÂ¥p", description = "LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t nhÃƒÂ  cung cÃ¡ÂºÂ¥p theo ID")
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
    @Operation(summary = "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t nhÃƒÂ  cung cÃ¡ÂºÂ¥p", description = "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin nhÃƒÂ  cung cÃ¡ÂºÂ¥p")
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
    @Operation(summary = "XÃƒÂ³a nhÃƒÂ  cung cÃ¡ÂºÂ¥p", description = "XÃƒÂ³a nhÃƒÂ  cung cÃ¡ÂºÂ¥p (kiÃ¡Â»Æ’m tra rÃƒÂ ng buÃ¡Â»â„¢c)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long supplierId) {
        log.info("REST request to delete supplier: {}", supplierId);
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.noContent().build();
    }
}
