package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reference Code Validation Response
 * Returns validation result and related entity info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceCodeValidation {
    
    /**
     * Whether the reference code exists in the system
     */
    private Boolean exists;
    
    /**
     * Detected type: APPOINTMENT, REQUEST, CUSTOM
     */
    private String type;
    
    /**
     * Whether the reference code is valid and can be used
     */
    private Boolean valid;
    
    /**
     * Validation message or warning
     */
    private String message;
    
    /**
     * Related entity information (nested object)
     */
    private RelatedEntityInfo relatedEntity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedEntityInfo {
        /**
         * Appointment code (if type is APPOINTMENT)
         */
        private String appointmentCode;
        
        /**
         * Appointment ID
         */
        private Long appointmentId;
        
        /**
         * Patient name
         */
        private String patientName;
        
        /**
         * Appointment date/time
         */
        private LocalDateTime appointmentDate;
        
        /**
         * Appointment status
         */
        private String status;
        
        /**
         * Services included in appointment
         */
        private String services;
    }
}
