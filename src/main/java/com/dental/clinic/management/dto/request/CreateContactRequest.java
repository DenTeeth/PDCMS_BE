package com.dental.clinic.management.dto.request;

import com.dental.clinic.management.domain.enums.CustomerContactSource;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateContactRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Size(max = 15)
    private String phone;

    @Email
    @Size(max = 100)
    private String email;

    private CustomerContactSource source;

    private CustomerContactStatus status;

    @Size(max = 36)
    private String assignedTo;

    private String notes;

    // Getters and setters
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
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
