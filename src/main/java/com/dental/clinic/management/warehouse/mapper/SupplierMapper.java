package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for Supplier entity and DTOs.
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
        supplier.setAddress(request.getAddress());
        supplier.setCertificationNumber(request.getCertificationNumber());
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
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }
        if (request.getCertificationNumber() != null) {
            supplier.setCertificationNumber(request.getCertificationNumber());
        }
        if (request.getIsVerified() != null) {
            supplier.setIsVerified(request.getIsVerified());
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
                .address(supplier.getAddress())
                .certificationNumber(supplier.getCertificationNumber())
                .isVerified(supplier.getIsVerified())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
