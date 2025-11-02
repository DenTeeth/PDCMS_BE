package com.dental.clinic.management.warehouse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a medical supplier.
 * Only ADMIN can create/update/delete suppliers.
 */
@Entity
@Table(name = "suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_name", columnNames = { "supplier_name" }),
        @UniqueConstraint(name = "uk_supplier_phone", columnNames = { "phone_number" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "supplier_name", length = 100, nullable = false)
    @NotNull(message = "Tên nhà cung cấp không được để trống")
    private String supplierName;

    @Column(name = "phone_number", length = 15, nullable = false)
    @NotNull(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    @NotNull(message = "Địa chỉ không được để trống")
    private String address;

    @Column(name = "certification_number", length = 50)
    private String certificationNumber;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
