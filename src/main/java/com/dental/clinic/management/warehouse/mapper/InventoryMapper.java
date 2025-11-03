package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.Inventory;
import com.dental.clinic.management.warehouse.domain.Supplier;
import com.dental.clinic.management.warehouse.dto.request.CreateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateInventoryRequest;
import com.dental.clinic.management.warehouse.dto.response.InventoryResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Inventory entity and DTOs.
 * Simplified version - removed pricing, unit measure, and certification.
 */
@Component
public class InventoryMapper {

    /**
     * Convert CreateInventoryRequest to Inventory entity.
     */
    public Inventory toEntity(CreateInventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setSupplierId(request.getSupplierId());
        inventory.setItemName(request.getItemName());
        inventory.setWarehouseType(request.getWarehouseType());
        inventory.setCategory(request.getCategory());
        inventory.setStockQuantity(request.getStockQuantity());
        inventory.setMinStockLevel(request.getMinStockLevel());
        inventory.setMaxStockLevel(request.getMaxStockLevel());
        inventory.setExpiryDate(request.getExpiryDate());
        inventory.setStatus(request.getStatus() != null ? request.getStatus() : Inventory.Status.ACTIVE);
        inventory.setNotes(request.getNotes());
        return inventory;
    }

    /**
     * Update Inventory entity from UpdateInventoryRequest.
     * Only updates non-null fields.
     */
    public void updateEntity(Inventory inventory, UpdateInventoryRequest request) {
        if (request.getSupplierId() != null) {
            inventory.setSupplierId(request.getSupplierId());
        }
        if (request.getItemName() != null) {
            inventory.setItemName(request.getItemName());
        }
        if (request.getWarehouseType() != null) {
            inventory.setWarehouseType(request.getWarehouseType());
        }
        if (request.getCategory() != null) {
            inventory.setCategory(request.getCategory());
        }
        if (request.getStockQuantity() != null) {
            inventory.setStockQuantity(request.getStockQuantity());
        }
        if (request.getMinStockLevel() != null) {
            inventory.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            inventory.setMaxStockLevel(request.getMaxStockLevel());
        }
        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
        }
        if (request.getStatus() != null) {
            inventory.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            inventory.setNotes(request.getNotes());
        }
    }

    /**
     * Convert Inventory entity to InventoryResponse.
     * Includes supplier information for FE display.
     */
    public InventoryResponse toResponse(Inventory inventory, Supplier supplier) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .supplierId(inventory.getSupplierId())
                .itemName(inventory.getItemName())
                .warehouseType(inventory.getWarehouseType())
                .category(inventory.getCategory())
                // Supplier details
                .supplierName(supplier != null ? supplier.getSupplierName() : null)
                .supplierPhone(supplier != null ? supplier.getPhoneNumber() : null)
                .supplierEmail(supplier != null ? supplier.getEmail() : null)
                .supplierAddress(supplier != null ? supplier.getAddress() : null)
                // Stock management
                .stockQuantity(inventory.getStockQuantity())
                .minStockLevel(inventory.getMinStockLevel())
                .maxStockLevel(inventory.getMaxStockLevel())
                // Expiry & status
                .expiryDate(inventory.getExpiryDate())
                .status(inventory.getStatus())
                .notes(inventory.getNotes())
                // Audit
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
