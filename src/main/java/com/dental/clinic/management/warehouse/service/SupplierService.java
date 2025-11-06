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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

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
    public SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request) {
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
    public SupplierResponse getSupplierById(UUID id) {
        log.debug("Getting supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_SUPPLIER + "')")
    public List<SupplierResponse> getAllSuppliers() {
        log.debug("Getting all suppliers");

        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_WAREHOUSE_SUPPLIER + "')")
    public List<SupplierResponse> searchSuppliers(String keyword) {
        log.debug("Searching suppliers with keyword: {}", keyword);

        return supplierRepository.searchByKeyword(keyword).stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.DELETE_WAREHOUSE_SUPPLIER + "')")
    public void deleteSupplier(UUID id) {
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
