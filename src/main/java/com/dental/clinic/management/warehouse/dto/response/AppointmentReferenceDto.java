package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Appointment Reference DTO
 * Lightweight DTO for appointment code search/selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReferenceDto {
    
    /**
     * Appointment ID
     */
    private Long appointmentId;
    
    /**
     * Appointment code (e.g., APT-2025-001)
     */
    private String appointmentCode;
    
    /**
     * Patient ID
     */
    private Integer patientId;
    
    /**
     * Patient full name
     */
    private String patientName;
    
    /**
     * Appointment start time
     */
    private LocalDateTime appointmentDate;
    
    /**
     * Appointment status (SCHEDULED, CONFIRMED, IN_PROGRESS, etc.)
     */
    private String status;
    
    /**
     * Comma-separated service names
     */
    private String services;
    
    /**
     * Display label for dropdown
     * e.g., "APT-2025-001 - Nguyễn Văn A (2025-12-25 09:00)"
     */
    private String displayLabel;
}
