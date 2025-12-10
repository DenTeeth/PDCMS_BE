package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.patient.domain.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Business Rules Service for Allergy Checking
 * 
 * Implements:
 * - Rule #16: Red alert if prescribed medication matches patient's allergy history
 */
@Service
public class AllergyCheckService {

    /**
     * Rule #16: Check for allergy conflicts between prescribed medications and patient allergies
     * 
     * Business Rule: System must warn if any prescribed medication matches patient's known allergies
     * 
     * @param patient Patient with allergy history
     * @param medicationNames List of medication names being prescribed
     * @return AllergyWarning object with conflict details (null if no conflicts)
     */
    public AllergyWarning checkAllergyConflict(Patient patient, List<String> medicationNames) {
        if (patient == null || patient.getAllergies() == null || patient.getAllergies().trim().isEmpty()) {
            return null; // No allergy data to check
        }

        if (medicationNames == null || medicationNames.isEmpty()) {
            return null; // No medications to check
        }

        // Parse allergies (comma-separated list)
        String[] allergies = patient.getAllergies().toLowerCase().split("[,;]");
        List<AllergyConflict> conflicts = new ArrayList<>();

        // Check each medication against all known allergies
        for (String medication : medicationNames) {
            if (medication == null || medication.trim().isEmpty()) {
                continue;
            }

            String medLower = medication.toLowerCase().trim();

            for (String allergy : allergies) {
                String allergyTrimmed = allergy.trim();
                if (allergyTrimmed.isEmpty()) {
                    continue;
                }

                // Check if medication contains allergy keyword or vice versa
                if (medLower.contains(allergyTrimmed) || allergyTrimmed.contains(medLower)) {
                    conflicts.add(AllergyConflict.builder()
                        .medicationName(medication)
                        .allergyKeyword(allergyTrimmed)
                        .message(String.format("CẢNH BÁO: Thuốc '%s' có thể xung đột với dị ứng '%s'", 
                            medication, allergyTrimmed))
                        .build());
                }
            }
        }

        if (conflicts.isEmpty()) {
            return null;
        }

        return AllergyWarning.builder()
            .hasConflict(true)
            .patientCode(patient.getPatientCode())
            .patientName(patient.getFullName())
            .patientAllergies(patient.getAllergies())
            .conflicts(conflicts)
            .conflictCount(conflicts.size())
            .build();
    }

    /**
     * AllergyWarning DTO - Contains details about allergy conflicts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergyWarning {
        private Boolean hasConflict;
        private String patientCode;
        private String patientName;
        private String patientAllergies;
        private Integer conflictCount;
        private List<AllergyConflict> conflicts;
    }

    /**
     * AllergyConflict DTO - Details of a single medication-allergy conflict
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergyConflict {
        private String medicationName;
        private String allergyKeyword;
        private String message;
    }
}
