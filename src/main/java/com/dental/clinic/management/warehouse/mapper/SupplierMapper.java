package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateSupplierRequest;
import com.dental.clinic.management.warehouse.dto.response.SupplierResponse;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(CreateSupplierRequest request) {
        if (request == null) {
            return null;
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        return supplier;
    }

    public void updateEntity(Supplier entity, UpdateSupplierRequest request) {
        if (request.getSupplierName() != null) {
            entity.setSupplierName(request.getSupplierName());
        }
        if (request.getContactPerson() != null) {
            entity.setContactPerson(request.getContactPerson());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
    }

    public SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        Integer suppliedItemCount = supplier.getSuppliedItems() != null 
            ? supplier.getSuppliedItems().size() 
            : 0;

        return SupplierResponse.builder()
            .id(supplier.getId())
            .supplierName(supplier.getSupplierName())
            .contactPerson(supplier.getContactPerson())
            .phone(supplier.getPhone())
            .email(supplier.getEmail())
            .address(supplier.getAddress())
            .suppliedItemCount(suppliedItemCount)
            .createdAt(supplier.getCreatedAt())
            .updatedAt(supplier.getUpdatedAt())
            .build();
    }
}

