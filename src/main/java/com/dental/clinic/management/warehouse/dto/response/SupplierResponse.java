package com.dental.clinic.management.warehouse.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for Supplier.
 */
public class SupplierResponse {

    private Long id;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Integer suppliedItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SupplierResponse() {
    }

    public SupplierResponse(Long id, String supplierName, String contactPerson, String phone,
            String email, String address, Integer suppliedItemCount,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.suppliedItemCount = suppliedItemCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getSuppliedItemCount() {
        return suppliedItemCount;
    }

    public void setSuppliedItemCount(Integer suppliedItemCount) {
        this.suppliedItemCount = suppliedItemCount;
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
}
