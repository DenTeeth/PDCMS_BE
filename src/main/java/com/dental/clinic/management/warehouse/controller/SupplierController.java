package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @ApiMessage("Tạo nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all suppliers with pagination.
     * 
     * GET /api/v1/suppliers?page=0&size=20&sort=supplierName,asc
     * 
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort parameter (e.g., "supplierName,asc")
     * @return 200 OK with Page<SupplierResponse>
     */
    @GetMapping
    @ApiMessage("Lấy danh sách nhà cung cấp thành công")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "supplierName,asc") String sort) {

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<SupplierResponse> response = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get supplier by ID.
     * 
     * GET /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier UUID
     * @return 200 OK with SupplierResponse
     */
    @GetMapping("/{supplierId}")
    @ApiMessage("Lấy thông tin nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable UUID supplierId) {
        SupplierResponse response = supplierService.getSupplierById(supplierId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing supplier.
     * 
     * PUT /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier UUID
     * @param request    UpdateSupplierRequest body
     * @return 200 OK with SupplierResponse
     */
    @PutMapping("/{supplierId}")
    @ApiMessage("Cập nhật nhà cung cấp thành công")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a supplier.
     * 
     * DELETE /api/v1/suppliers/{supplierId}
     * 
     * @param supplierId supplier UUID
     * @return 200 OK with success message
     */
    @DeleteMapping("/{supplierId}")
    @ApiMessage("Xóa nhà cung cấp thành công")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.ok().build();
    }
}
