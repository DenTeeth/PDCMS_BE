package com.dental.clinic.management.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * BR-043: DTO for duplicate patient detection results.
 * 
 * When creating a new patient, system checks for duplicates by:
 * - Name + Date of Birth
 * - Phone number
 * 
 * If matches found, return this DTO with merge suggestions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicatePatientCheckResult {
    
    /**
     * True if potential duplicates were found
     */
    private boolean hasDuplicates;
    
    /**
     * List of potential duplicate patients
     */
    private List<DuplicatePatientMatch> matches;
    
    /**
     * Message to display to user
     */
    private String message;
    
    /**
     * Inner class for duplicate match details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicatePatientMatch {
        private Integer patientId;
        private String patientCode;
        private String fullName;
        private LocalDate dateOfBirth;
        private String phone;
        private String email;
        private MatchType matchType;
        private int confidenceScore; // 0-100, how likely this is a duplicate
    }
    
    /**
     * Type of match found
     */
    public enum MatchType {
        NAME_AND_DOB("Trùng Tên + Ngày sinh"),
        PHONE("Trùng Số điện thoại"),
        NAME_AND_PHONE("Trùng Tên + Số điện thoại"),
        EXACT_MATCH("Trùng khớp hoàn toàn");
        
        private final String displayName;
        
        MatchType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
