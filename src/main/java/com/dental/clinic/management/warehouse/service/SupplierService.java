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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<SupplierResponse> getAllSuppliers(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all suppliers with pagination: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        // Validate and sanitize inputs
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size; // Default 10 items per page

        // Default sort: newest first (updatedAt DESC, then createdAt DESC)
        Sort sort;
        if (sortBy == null || sortBy.isEmpty()) {
            sort = Sort.by(Sort.Direction.DESC, "updatedAt")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("DESC")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        }

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        return supplierRepository.findAll(pageable)
                .map(supplierMapper::toResponse);
    }

    /**
     * Search suppliers by keyword with pagination.
     * Search in: name, phone, email, address (case-insensitive,
     * accent-insensitive).
     * Both ADMIN and STAFF can search suppliers.
     * 
     * @param keyword search keyword
     * @param page    page number (0-indexed)
     * @param size    items per page (default: 10)
     * @return page of suppliers matching keyword
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SUPPLIER', 'CREATE_SUPPLIER')")
    public Page<SupplierResponse> searchSuppliers(String keyword, int page, int size) {
        log.info("Searching suppliers with keyword: '{}', page={}, size={}", keyword, page, size);

        // Validate inputs
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        // Default sort: newest first (updatedAt DESC, then createdAt DESC)
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));

        Pageable pageable = PageRequest.of(page, size, sort);

        // If keyword is empty, return all
        if (keyword == null || keyword.trim().isEmpty()) {
            return supplierRepository.findAll(pageable)
                    .map(supplierMapper::toResponse);
        }

        return supplierRepository.searchSuppliers(keyword.trim(), pageable)
                .map(supplierMapper::toResponse);
    }

    /**
     * Get supplier by ID.
     * Both ADMIN and STAFF can view supplier details.
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SUPPLIER', 'CREATE_SUPPLIER')")
    public SupplierResponse getSupplierById(Long supplierId) {
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
    public SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) {
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
    public void deleteSupplier(Long supplierId) {
        log.info("Deleting supplier with ID: {}", supplierId);

        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException(
                    "Không tìm thấy nhà cung cấp với ID: " + supplierId);
        }

        supplierRepository.deleteById(supplierId);
        log.info("Supplier deleted successfully with ID: {}", supplierId);
    }
}