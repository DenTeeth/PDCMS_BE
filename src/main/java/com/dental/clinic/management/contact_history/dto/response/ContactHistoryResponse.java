package com.dental.clinic.management.contact_history.dto.response;

import java.time.LocalDateTime;

import com.dental.clinic.management.contact_history.enums.ContactHistoryAction;

/**
 * Response DTO for a contact interaction / history record.
 */
public class ContactHistoryResponse {
    private String historyId;
    private String contactId;
    private Integer employeeId;
    private ContactHistoryAction action;
    private String content;
    private LocalDateTime createdAt;
    private String employeeName; // optional, can be filled/enriched by service

    public ContactHistoryResponse() {
    }

    public ContactHistoryResponse(String historyId, String contactId, Integer employeeId,
            ContactHistoryAction action, String content, LocalDateTime createdAt,
            String employeeName) {
        this.historyId = historyId;
        this.contactId = contactId;
        this.employeeId = employeeId;
        this.action = action;
        this.content = content;
        this.createdAt = createdAt;
        this.employeeName = employeeName;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public ContactHistoryAction getAction() {
        return action;
    }

    public void setAction(ContactHistoryAction action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
