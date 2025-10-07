package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.ContactHistoryAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateContactHistoryRequest {

    @NotBlank
    @Size(max = 20)
    private String contactId;

    @Size(max = 36)
    private String employeeId;

    private ContactHistoryAction action;

    private String content;

    // Getters and setters
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public ContactHistoryAction getAction() { return action; }
    public void setAction(ContactHistoryAction action) { this.action = action; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
