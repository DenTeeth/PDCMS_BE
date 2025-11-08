package com.dental.clinic.management.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a supplier (nhà cung cấp).
 * Suppliers have N-N relationship with ItemMaster (one supplier can provide
 * many items).
 */
@Entity
@Table(name = "suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_name", columnNames = "supplier_name")
})
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "supplier_name", length = 100, nullable = false)
    @NotBlank(message = "Supplier name cannot be empty")
    private String supplierName;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    @Email(message = "Email is invalid")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // === RELATIONSHIP: Items (N-N) - Mapped by ItemMaster ===
    @ManyToMany(mappedBy = "compatibleSuppliers")
    @JsonIgnore
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

    // === CONSTRUCTORS ===

    public Supplier() {
    }

    public Supplier(Long supplierId, String supplierName, String contactPerson, String phone,
            String email, String address, List<ItemMaster> compatibleItems,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.compatibleItems = compatibleItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === GETTERS AND SETTERS ===

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ItemMaster> getCompatibleItems() {
        return compatibleItems;
    }

    public void setCompatibleItems(List<ItemMaster> compatibleItems) {
        this.compatibleItems = compatibleItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === HELPER METHODS ===

    /**
     * Get ID (alias for supplierId for consistency with mappers).
     */
    public Long getId() {
        return supplierId;
    }

    /**
     * Get supplied items (alias for compatibleItems).
     */
    public List<ItemMaster> getSuppliedItems() {
        return compatibleItems;
    }
}
