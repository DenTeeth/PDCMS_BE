package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for Supplier entity and DTOs.
 * Simplified mapping for basic supplier information.
 */
@Component
public class SupplierMapper {

    /**
     * Convert CreateSupplierRequest to Supplier entity.
     */
    public Supplier toEntity(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierName(request.getSupplierName());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setNotes(request.getNotes());
        return supplier;
    }

    /**
     * Update Supplier entity from UpdateSupplierRequest.
     */
    public void updateEntity(Supplier supplier, UpdateSupplierRequest request) {
        if (request.getSupplierName() != null) {
            supplier.setSupplierName(request.getSupplierName());
        }
        if (request.getPhoneNumber() != null) {
            supplier.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            supplier.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            supplier.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            supplier.setNotes(request.getNotes());
        }
    }

    /**
     * Convert Supplier entity to SupplierResponse.
     */
    public SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .phoneNumber(supplier.getPhoneNumber())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .status(supplier.getStatus())
                .notes(supplier.getNotes())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
