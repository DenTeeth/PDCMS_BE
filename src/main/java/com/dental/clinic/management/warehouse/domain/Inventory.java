package com.dental.clinic.management.warehouse.domain;

import com.dental.clinic.management.warehouse.enums.UnitOfMeasure;
import com.dental.clinic.management.warehouse.enums.WarehouseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing inventory items in warehouse.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 20)
    @NotNull(message = "Loại kho không được để trống")
    private WarehouseType warehouseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Danh mục không được để trống")
    private Category category;

    @Column(name = "item_name", length = 100, nullable = false)
    @NotNull(message = "Tên vật tư không được để trống")
    private String itemName;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    @NotNull(message = "Đơn vị tính không được để trống")
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_certified", nullable = false)
    private Boolean isCertified = false;

    @Column(name = "stock_quantity", nullable = false)
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isCertified == null) {
            isCertified = false;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        // Validate: COLD warehouse MUST have expiry date
        if (warehouseType == WarehouseType.COLD && expiryDate == null) {
            throw new IllegalStateException("Kho lạnh bắt buộc phải có ngày hết hạn");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
