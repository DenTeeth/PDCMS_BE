package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.exception.warehouse.DuplicateSupplierException;
import com.dental.clinic.management.exception.warehouse.SupplierNotFoundException;
import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.mapper.SupplierMapper;
import com.dental.clinic.management.warehouse.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing suppliers.
 * Only ADMIN can create/update/delete suppliers.
 * Both ADMIN and STAFF can view suppliers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    /**
     * Create a new supplier.
     * Only ADMIN can create suppliers.
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_SUPPLIER')")
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.info("Creating new supplier: {}", request.getSupplierName());

        // Check for duplicate supplier name
        if (supplierRepository.existsBySupplierName(request.getSupplierName())) {
            throw new DuplicateSupplierException(
                    "Nhà cung cấp với tên '" + request.getSupplierName() + "' đã tồn tại");
        }

        // Check for duplicate phone number
        if (supplierRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateSupplierException(
                    "Nhà cung cấp với số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại");
        }

        Supplier supplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);

        log.info("Supplier created successfully with ID: {}", savedSupplier.getSupplierId());
        return supplierMapper.toResponse(savedSupplier);
    }

    /**
     * Get all suppliers with pagination.
     * Both ADMIN and STAFF can view suppliers.
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SUPPLIER', 'CREATE_SUPPLIER')")
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        log.info("Fetching all suppliers with pagination");
        return supplierRepository.findAll(pageable)
                .map(supplierMapper::toResponse);
    }

    /**
     * Get supplier by ID.
     * Both ADMIN and STAFF can view supplier details.
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SUPPLIER', 'CREATE_SUPPLIER')")
    public SupplierResponse getSupplierById(UUID supplierId) {
        log.info("Fetching supplier by ID: {}", supplierId);
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Không tìm thấy nhà cung cấp với ID: " + supplierId));
        return supplierMapper.toResponse(supplier);
    }

    /**
     * Update an existing supplier.
     * Only ADMIN can update suppliers.
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_SUPPLIER')")
    public SupplierResponse updateSupplier(UUID supplierId, UpdateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", supplierId);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Không tìm thấy nhà cung cấp với ID: " + supplierId));

        // Check for duplicate supplier name (if changed)
        if (request.getSupplierName() != null &&
                !request.getSupplierName().equals(supplier.getSupplierName())) {
            if (supplierRepository.existsBySupplierName(request.getSupplierName())) {
                throw new DuplicateSupplierException(
                        "Nhà cung cấp với tên '" + request.getSupplierName() + "' đã tồn tại");
            }
        }

        // Check for duplicate phone number (if changed)
        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(supplier.getPhoneNumber())) {
            if (supplierRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new DuplicateSupplierException(
                        "Nhà cung cấp với số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại");
            }
        }

        supplierMapper.updateEntity(supplier, request);
        Supplier updatedSupplier = supplierRepository.save(supplier);

        log.info("Supplier updated successfully with ID: {}", supplierId);
        return supplierMapper.toResponse(updatedSupplier);
    }

    /**
     * Delete a supplier.
     * Only ADMIN can delete suppliers.
     */
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_SUPPLIER')")
    public void deleteSupplier(UUID supplierId) {
        log.info("Deleting supplier with ID: {}", supplierId);

        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException(
                    "Không tìm thấy nhà cung cấp với ID: " + supplierId);
        }

        supplierRepository.deleteById(supplierId);
        log.info("Supplier deleted successfully with ID: {}", supplierId);
    }
}
