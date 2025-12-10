package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.dto.DuplicatePatientCheckResult;
import com.dental.clinic.management.patient.dto.DuplicatePatientCheckResult.DuplicatePatientMatch;
import com.dental.clinic.management.patient.dto.DuplicatePatientCheckResult.MatchType;
import com.dental.clinic.management.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BR-043: Duplicate Patient Detection Service
 * 
 * Business Rule: System must check for duplicate patients when creating new records:
 * - Check by Name + Date of Birth
 * - Check by Phone Number
 * 
 * If duplicates found, suggest merging records to staff.
 * 
 * Purpose: Prevent duplicate patient records, maintain data integrity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DuplicatePatientDetectionService {

    private final PatientRepository patientRepository;

    /**
     * Check for duplicate patients by name + DOB and phone number.
     * 
     * BR-043: System must check for duplicates before creating new patient.
     * 
     * @param firstName First name
     * @param lastName Last name
     * @param dateOfBirth Date of birth
     * @param phone Phone number (optional)
     * @return DuplicatePatientCheckResult with matches (if any)
     */
    @Transactional(readOnly = true)
    public DuplicatePatientCheckResult checkForDuplicates(
            String firstName, 
            String lastName, 
            LocalDate dateOfBirth, 
            String phone) {
        
        log.debug("Checking for duplicate patients: {} {}, DOB: {}, Phone: {}", 
                  firstName, lastName, dateOfBirth, phone);

        List<DuplicatePatientMatch> allMatches = new ArrayList<>();

        // 1. Check by Name + Date of Birth
        if (firstName != null && lastName != null && dateOfBirth != null) {
            List<Patient> nameAndDobMatches = patientRepository.findByNameAndDateOfBirth(
                    firstName.trim(), lastName.trim(), dateOfBirth);
            
            for (Patient patient : nameAndDobMatches) {
                DuplicatePatientMatch match = buildMatch(patient, MatchType.NAME_AND_DOB);
                
                // Higher confidence if phone also matches
                if (phone != null && phone.equals(patient.getPhone())) {
                    match.setMatchType(MatchType.EXACT_MATCH);
                    match.setConfidenceScore(95);
                }
                
                allMatches.add(match);
            }
        }

        // 2. Check by Phone Number
        if (phone != null && !phone.trim().isEmpty()) {
            List<Patient> phoneMatches = patientRepository.findByPhoneNumber(phone.trim());
            
            for (Patient patient : phoneMatches) {
                // Avoid duplicate entries (already added in name+DOB check)
                boolean alreadyAdded = allMatches.stream()
                        .anyMatch(m -> m.getPatientId().equals(patient.getPatientId()));
                
                if (!alreadyAdded) {
                    DuplicatePatientMatch match = buildMatch(patient, MatchType.PHONE);
                    
                    // Check if name also matches (higher confidence)
                    if (firstName != null && lastName != null &&
                        firstName.equalsIgnoreCase(patient.getFirstName()) &&
                        lastName.equalsIgnoreCase(patient.getLastName())) {
                        match.setMatchType(MatchType.NAME_AND_PHONE);
                        match.setConfidenceScore(85);
                    }
                    
                    allMatches.add(match);
                }
            }
        }

        // 3. Build result
        boolean hasDuplicates = !allMatches.isEmpty();
        String message = buildMessage(allMatches.size());

        log.info("Duplicate check result: {} potential matches found", allMatches.size());

        return DuplicatePatientCheckResult.builder()
                .hasDuplicates(hasDuplicates)
                .matches(allMatches)
                .message(message)
                .build();
    }

    /**
     * Build a duplicate match object from a patient entity.
     */
    private DuplicatePatientMatch buildMatch(Patient patient, MatchType matchType) {
        int confidenceScore = calculateConfidenceScore(matchType);
        
        return DuplicatePatientMatch.builder()
                .patientId(patient.getPatientId())
                .patientCode(patient.getPatientCode())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .matchType(matchType)
                .confidenceScore(confidenceScore)
                .build();
    }

    /**
     * Calculate confidence score based on match type.
     * 
     * @param matchType Type of match
     * @return Confidence score (0-100)
     */
    private int calculateConfidenceScore(MatchType matchType) {
        switch (matchType) {
            case EXACT_MATCH:
                return 95; // Name + DOB + Phone match
            case NAME_AND_DOB:
                return 80; // High confidence
            case NAME_AND_PHONE:
                return 85; // High confidence
            case PHONE:
                return 60; // Medium confidence (could be family member)
            default:
                return 50;
        }
    }

    /**
     * Build user-friendly message based on number of matches.
     */
    private String buildMessage(int matchCount) {
        if (matchCount == 0) {
            return "Không tìm thấy bệnh nhân trùng.";
        } else if (matchCount == 1) {
            return "Tìm thấy 1 bệnh nhân có thông tin tương tự. Vui lòng kiểm tra trước khi tạo mới.";
        } else {
            return String.format("Tìm thấy %d bệnh nhân có thông tin tương tự. Vui lòng kiểm tra trước khi tạo mới.", matchCount);
        }
    }

    /**
     * Check if the new patient data matches an existing patient exactly.
     * Used to prevent creating duplicate if user ignores warning.
     * 
     * @param firstName First name
     * @param lastName Last name
     * @param dateOfBirth Date of birth
     * @param phone Phone number
     * @return true if exact match found
     */
    @Transactional(readOnly = true)
    public boolean hasExactMatch(String firstName, String lastName, LocalDate dateOfBirth, String phone) {
        
        List<Patient> nameAndDobMatches = patientRepository.findByNameAndDateOfBirth(
                firstName.trim(), lastName.trim(), dateOfBirth);
        
        // Check if any of the name+DOB matches also has the same phone
        if (phone != null && !phone.trim().isEmpty()) {
            boolean exactMatch = nameAndDobMatches.stream()
                    .anyMatch(p -> phone.equals(p.getPhone()));
            
            if (exactMatch) {
                log.warn("Exact duplicate detected: {} {}, DOB: {}, Phone: {}", 
                         firstName, lastName, dateOfBirth, phone);
                return true;
            }
        }
        
        return false;
    }
}
