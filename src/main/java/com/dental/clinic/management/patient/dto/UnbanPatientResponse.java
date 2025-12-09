package com.dental.clinic.management.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for patient unban response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnbanPatientResponse {
    
    private String message;
    private Integer patientId;
    private String patientName;
    private Integer previousNoShowCount;
    private Integer newNoShowCount;
    private String unbanBy;
    private String unbanByRole;
    private LocalDateTime unbanAt;
}
