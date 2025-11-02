package com.dental.clinic.management.warehouse.domain;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a product category.
 * Categories are specific to warehouse types (COLD or NORMAL).
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name_warehouse", columnNames = { "category_name", "warehouse_type" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "category_name", length = 50, nullable = false)
    @NotNull(message = "Tên danh mục không được để trống")
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 20)
    @NotNull(message = "Loại kho không được để trống")
    private WarehouseType warehouseType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
