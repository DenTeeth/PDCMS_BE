package com.dental.clinic.management.dto.response;

import com.dental.clinic.management.domain.enums.CustomerContactSource;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Customer contact info (including optional history list).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private List<ContactHistoryResponse> history;
}
