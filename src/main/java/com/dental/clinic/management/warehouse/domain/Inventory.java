package com.dental.clinic.management.warehouse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing inventory items in warehouse.
 * Simplified version - removed pricing, unit measure, and certification.
 */
@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_name", columnNames = { "item_name" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    // === SUPPLIER RELATIONSHIP ===
    @Column(name = "supplier_id", nullable = false)
    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    // === BASIC INFO ===
    @Column(name = "item_name", length = 255, nullable = false)
    @NotNull(message = "Tên vật tư không được để trống")
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", length = 20, nullable = false)
    @NotNull(message = "Loại kho không được để trống")
    private WarehouseType warehouseType;

    @Column(name = "category", length = 100)
    private String category;

    // === STOCK MANAGEMENT ===
    @Column(name = "stock_quantity", nullable = false)
    @NotNull(message = "Số lượng tồn kho không được để trống")
    private Integer stockQuantity;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    // === EXPIRY (for COLD warehouse) ===
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // === STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // === AUDIT ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === LIFECYCLE HOOKS ===
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.ACTIVE;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === ENUMS ===
    public enum WarehouseType {
        COLD, // Kho lạnh (thuốc, vaccine) - bắt buộc expiry_date
        NORMAL // Kho thường (vật tư, dụng cụ)
    }

    public enum Status {
        ACTIVE, // Đang hoạt động
        INACTIVE, // Ngừng hoạt động
        OUT_OF_STOCK // Hết hàng
    }
}
