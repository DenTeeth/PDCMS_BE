package com.dental.clinic.management.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    
    private Long auditId;
    private Integer patientId;
    private String patientName;
    private Integer previousNoShowCount;
    private String performedBy;
    private String performedByRole;
    private String reason;
    private LocalDateTime timestamp;
}
