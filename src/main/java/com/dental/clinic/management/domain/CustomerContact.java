package com.dental.clinic.management.domain;

import com.dental.clinic.management.domain.enums.CustomerContactSource;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_contacts")
public class CustomerContact {

    @Id
    @Column(name = "contact_id", length = 36)
    private String contactId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Size(max = 15)
    @Column(name = "phone", length = 15)
    private String phone;

    @Email
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private CustomerContactSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CustomerContactStatus status;

    @Column(name = "assigned_to", length = 36)
    private String assignedTo; // employee_id

    @Column(name = "converted_patient_id", length = 36)
    private String convertedPatientId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public CustomerContactSource getSource() {
        return source;
    }

    public void setSource(CustomerContactSource source) {
        this.source = source;
    }

    public CustomerContactStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerContactStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getConvertedPatientId() {
        return convertedPatientId;
    }

    public void setConvertedPatientId(String convertedPatientId) {
        this.convertedPatientId = convertedPatientId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
