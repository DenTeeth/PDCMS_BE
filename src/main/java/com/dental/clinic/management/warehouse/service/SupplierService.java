package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import com.dental.clinic.management.warehouse.exception.DuplicateSupplierException;
import com.dental.clinic.management.warehouse.exception.SupplierNotFoundException;
import com.dental.clinic.management.warehouse.mapper.SupplierMapper;
import com.dental.clinic.management.warehouse.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.CREATE_WAREHOUSE_SUPPLIER + "')")
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.debug("Creating new supplier: {}", request.getSupplierName());

        if (supplierRepository.existsBySupplierName(request.getSupplierName())) {
            throw new DuplicateSupplierException(request.getSupplierName());
        }

        Supplier supplier = supplierMapper.toEntity(request);

        try {
            Supplier savedSupplier = supplierRepository.save(supplier);
            log.info("Created supplier with ID: {}", savedSupplier.getId());
            return supplierMapper.toResponse(savedSupplier);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSupplierException(request.getSupplierName(), e);
        }
    }

    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.UPDATE_WAREHOUSE_SUPPLIER + "')")
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        log.debug("Updating supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (request.getSupplierName() != null
                && !request.getSupplierName().equals(supplier.getSupplierName())
                && supplierRepository.existsBySupplierName(request.getSupplierName())) {
            throw new DuplicateSupplierException(request.getSupplierName());
        }

        supplierMapper.updateEntity(supplier, request);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Updated supplier with ID: {}", updatedSupplier.getId());

        return supplierMapper.toResponse(updatedSupplier);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_SUPPLIER + "')")
    public SupplierResponse getSupplierById(Long id) {
        log.debug("Getting supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_SUPPLIER + "')")
    public Page<SupplierResponse> getAllSuppliers(int page, int size, String sortBy, String sortDirection) {
        log.debug("Getting all suppliers: page={}, size={}, sortBy={}, sortDirection={}", page, size, sortBy,
                sortDirection);

        // Default sorting by createdAt if sortBy is not provided
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return supplierRepository.findAll(pageable)
                .map(supplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_SUPPLIER + "')")
    public Page<SupplierResponse> searchSuppliers(String keyword, int page, int size) {
        log.debug("Searching suppliers with keyword: {}, page={}, size={}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.trim().isEmpty()) {
            return supplierRepository.findAll(pageable)
                    .map(supplierMapper::toResponse);
        }

        return supplierRepository.searchByKeyword(keyword, pageable)
                .map(supplierMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.DELETE_WAREHOUSE_SUPPLIER + "')")
    public void deleteSupplier(Long id) {
        log.debug("Deleting supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (supplier.getSuppliedItems() != null && !supplier.getSuppliedItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete supplier with supplied items");
        }

        supplierRepository.delete(supplier);
        log.info("Deleted supplier with ID: {}", id);
    }
}
