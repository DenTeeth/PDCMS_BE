package com.dental.clinic.management.dto.response;

import com.dental.clinic.management.domain.enums.ContactHistoryAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for a contact interaction / history record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactHistoryResponse {
    private String historyId;
    private String contactId;
    private String employeeId;
    private ContactHistoryAction action;
    private String content;
    private LocalDateTime createdAt;
    private String employeeName; // optional, can be filled/enriched by service
}
