package com.dental.clinic.management.dto.response;

import com.dental.clinic.management.domain.enums.CustomerContactSource;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import java.time.LocalDateTime;

public class ContactInfoResponse {
    private String contactId;
    private String fullName;
    private String phone;
    private String email;
    private CustomerContactSource source;
    private CustomerContactStatus status;
    private String assignedTo;
    private String convertedPatientId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public CustomerContactSource getSource() { return source; }
    public void setSource(CustomerContactSource source) { this.source = source; }
    public CustomerContactStatus getStatus() { return status; }
    public void setStatus(CustomerContactStatus status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getConvertedPatientId() { return convertedPatientId; }
    public void setConvertedPatientId(String convertedPatientId) { this.convertedPatientId = convertedPatientId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
