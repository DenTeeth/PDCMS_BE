package com.dental.clinic.management.patient.dto;

import com.dental.clinic.management.patient.enums.PatientBlacklistReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * BR-044: Response DTO for blacklist operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistPatientResponse {
    
    private String message;
    private Integer patientId;
    private String patientName;
    private PatientBlacklistReason blacklistReason;
    private String blacklistReasonDisplay;
    private String notes;
    private String blacklistedBy;
    private LocalDateTime blacklistedAt;
}
