package com.dental.clinic.management.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a supplier (nhà cung cấp).
 * Suppliers have N-N relationship with ItemMaster (one supplier can provide
 * many items).
 */
@Entity
@Table(name = "suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_name", columnNames = "supplier_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "supplier_name", length = 100, nullable = false)
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String supplierName;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // === RELATIONSHIP: Items (N-N) - Mapped by ItemMaster ===
    @ManyToMany(mappedBy = "compatibleSuppliers")
    @JsonIgnore
    @Builder.Default
    private List<ItemMaster> compatibleItems = new ArrayList<>();

    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === HELPER METHODS ===

    /**
     * Get ID (alias for supplierId for consistency with mappers).
     */
    public UUID getId() {
        return supplierId;
    }

    /**
     * Get supplied items (alias for compatibleItems).
     */
    public List<ItemMaster> getSuppliedItems() {
        return compatibleItems;
    }
}
