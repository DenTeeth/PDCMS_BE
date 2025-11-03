package com.dental.clinic.management.warehouse.dto.response;

import com.dental.clinic.management.warehouse.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for inventory details.
 * Simplified version - removed pricing, unit measure, and certification.
 * Includes supplier information for FE display.
 * All fields use camelCase for FE compatibility.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    // Inventory Basic Info
    private Long inventoryId;
    private Long supplierId;
    private String itemName;
    private Inventory.WarehouseType warehouseType;
    private String category;

    // Supplier Details (for FE display)
    private String supplierName;
    private String supplierPhone;
    private String supplierEmail;
    private String supplierAddress;

    // Stock Management
    private Integer stockQuantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;

    // Expiry & Status
    private LocalDate expiryDate;
    private Inventory.Status status;
    private String notes;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
