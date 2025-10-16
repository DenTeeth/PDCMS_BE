
package com.dental.clinic.management.contact_history.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.dental.clinic.management.contact_history.enums.ContactHistoryAction;
import java.time.LocalDateTime;

/**
 * Contact interaction / audit record.
 */
@Entity
@Table(name = "contact_history")
public class ContactHistory {

    @Id
    @Column(name = "history_id", length = 36)
    private String historyId;

    @NotBlank
    @Size(max = 36)
    @Column(name = "contact_id", length = 36, nullable = false)
    private String contactId;

    @Size(max = 36)
    @Column(name = "employee_id", length = 36)
    private String employeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private ContactHistoryAction action;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ContactHistory() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters / setters
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
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

    // equality by historyId (consistent with Employee entity style)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ContactHistory))
            return false;
        ContactHistory that = (ContactHistory) o;
        return historyId != null && historyId.equals(that.historyId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ContactHistory{" +
                "historyId='" + historyId + '\'' +
                ", contactId='" + contactId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", action=" + action +
                ", createdAt=" + createdAt +
                '}';
    }
}
