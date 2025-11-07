package com.dental.clinic.management.customer_contact.dto.response;

import com.dental.clinic.management.contact_history.dto.response.ContactHistoryResponse;
import com.dental.clinic.management.customer_contact.enums.CustomerContactSource;
import com.dental.clinic.management.customer_contact.enums.CustomerContactStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Customer contact info (including optional history list).
 */
public class ContactInfoResponse {
    private String contactId;
    private String fullName;
    private String phone;
    private String email;
    private CustomerContactSource source;
    private CustomerContactStatus status;
    private Integer assignedTo;
    private Integer convertedPatientId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ContactHistoryResponse> history;

    public ContactInfoResponse() {
    }

    public ContactInfoResponse(String contactId, String fullName, String phone, String email,
            CustomerContactSource source, CustomerContactStatus status,
            Integer assignedTo, Integer convertedPatientId, String notes,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            List<ContactHistoryResponse> history) {
        this.contactId = contactId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.source = source;
        this.status = status;
        this.assignedTo = assignedTo;
        this.convertedPatientId = convertedPatientId;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.history = history;
    }

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

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getConvertedPatientId() {
        return convertedPatientId;
    }

    public void setConvertedPatientId(Integer convertedPatientId) {
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

    public List<ContactHistoryResponse> getHistory() {
        return history;
    }

    public void setHistory(List<ContactHistoryResponse> history) {
        this.history = history;
    }
}
