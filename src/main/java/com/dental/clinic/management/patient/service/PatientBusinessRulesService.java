package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.dto.request.CreatePatientRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Business Rules Validation Service for Patient Module
 * 
 * Implements:
 * - Rule #14: Guardian required for minors (<16 years old)
 */
@Service
public class PatientBusinessRulesService {

    /**
     * Rule #14: Validate guardian information for minors
     * 
     * Business Rule: Patients under 16 years old MUST have guardian information
     * 
     * @param request CreatePatientRequest or UpdatePatientRequest
     * @param dateOfBirth Patient's date of birth
     * @throws BadRequestAlertException if validation fails
     */
    public void validateGuardianForMinor(Object request, LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return; // Skip validation if DOB not provided
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        
        if (age < 16) {
            // Patient is a minor - guardian info is REQUIRED
            String guardianName = null;
            
            if (request instanceof CreatePatientRequest) {
                guardianName = ((CreatePatientRequest) request).getGuardianName();
            } else if (request instanceof com.dental.clinic.management.patient.dto.request.UpdatePatientRequest) {
                guardianName = ((com.dental.clinic.management.patient.dto.request.UpdatePatientRequest) request).getGuardianName();
            }
            
            if (guardianName == null || guardianName.trim().isEmpty()) {
                throw new BadRequestAlertException(
                    String.format("Bệnh nhân dưới 16 tuổi phải có thông tin người giám hộ. Tuổi hiện tại: %d", age),
                    "Patient",
                    "guardianRequired"
                );
            }
        }
    }

    /**
     * Rule #14: Validate guardian info when updating patient's date of birth
     * 
     * @param patient Existing patient entity
     * @param newDateOfBirth New date of birth being set
     */
    public void validateGuardianForExistingPatient(Patient patient, LocalDate newDateOfBirth) {
        if (newDateOfBirth == null) {
            return;
        }

        int newAge = Period.between(newDateOfBirth, LocalDate.now()).getYears();
        
        if (newAge < 16) {
            // Check if guardian info already exists
            if (patient.getGuardianName() == null || patient.getGuardianName().trim().isEmpty()) {
                throw new BadRequestAlertException(
                    String.format("Không thể cập nhật sinh nhật làm bệnh nhân dưới 16 tuổi mà không có thông tin người giám hộ. Tuổi mới: %d", newAge),
                    "Patient",
                    "guardianRequiredForAgeUpdate"
                );
            }
        }
    }

    /**
     * Helper method: Calculate patient age from date of birth
     * 
     * @param dateOfBirth Patient's date of birth
     * @return Age in years
     */
    public int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
