package com.dental.clinic.management.dto.response;

import com.dental.clinic.management.domain.enums.ContactHistoryAction;
import java.time.LocalDateTime;

public class ContactHistoryResponse {
    private String historyId;
    private String contactId;
    private String employeeId;
    private ContactHistoryAction action;
    private String content;
    private LocalDateTime createdAt;

    // Getters and setters
    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public ContactHistoryAction getAction() { return action; }
    public void setAction(ContactHistoryAction action) { this.action = action; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
