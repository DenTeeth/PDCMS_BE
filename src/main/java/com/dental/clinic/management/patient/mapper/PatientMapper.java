
package com.dental.clinic.management.patient.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.dto.request.CreatePatientRequest;
import com.dental.clinic.management.patient.dto.request.ReplacePatientRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientRequest;
import com.dental.clinic.management.patient.dto.response.PatientInfoResponse;

/**
 * Mapper for Patient entity and DTOs
 */
@Component
public class PatientMapper {

    /**
     * Convert Patient entity to PatientInfoResponse DTO
     */
    public PatientInfoResponse toPatientInfoResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        PatientInfoResponse response = new PatientInfoResponse();

        response.setPatientId(patient.getPatientId());
        response.setPatientCode(patient.getPatientCode());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setFullName(patient.getFirstName() + " " + patient.getLastName());
        response.setEmail(patient.getEmail());
        response.setPhone(patient.getPhone());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setAddress(patient.getAddress());
        response.setGender(patient.getGender() != null ? patient.getGender().name() : null);
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setAllergies(patient.getAllergies());
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        response.setGuardianName(patient.getGuardianName());
        response.setGuardianPhone(patient.getGuardianPhone());
        response.setGuardianRelationship(patient.getGuardianRelationship());
        response.setGuardianCitizenId(patient.getGuardianCitizenId());
        response.setIsActive(patient.getIsActive());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());

        // Map booking block fields (BR-043, BR-044, BR-005)
        response.setIsBookingBlocked(patient.getIsBookingBlocked());
        response.setBookingBlockReason(
                patient.getBookingBlockReason() != null ? patient.getBookingBlockReason().name() : null);
        response.setBookingBlockNotes(patient.getBookingBlockNotes());
        response.setBlockedBy(patient.getBlockedBy());
        response.setBlockedAt(patient.getBlockedAt());
        response.setConsecutiveNoShows(patient.getConsecutiveNoShows());

        // Map account-related fields
        if (patient.getAccount() != null) {
            response.setHasAccount(true);
            response.setAccountId(patient.getAccount().getAccountId());
            response.setAccountStatus(
                    patient.getAccount().getStatus() != null ? patient.getAccount().getStatus().name() : null);
            response.setIsEmailVerified(patient.getAccount().getIsEmailVerified());
        } else {
            response.setHasAccount(false);
        }

        return response;
    }

    /**
     * Convert CreatePatientRequest to Patient entity
     */
    public Patient toPatient(CreatePatientRequest request) {
        if (request == null) {
            return null;
        }

        Patient patient = new Patient();

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAddress(request.getAddress());
        patient.setGender(request.getGender());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setGuardianName(request.getGuardianName());
        patient.setGuardianPhone(request.getGuardianPhone());
        patient.setGuardianRelationship(request.getGuardianRelationship());
        patient.setGuardianCitizenId(request.getGuardianCitizenId());

        return patient;
    }

    /**
     * Update Patient entity from UpdatePatientRequest (PATCH - partial update)
     * Only update non-null fields
     */
    public void updatePatientFromRequest(UpdatePatientRequest request, Patient patient) {
        if (request == null || patient == null) {
            return;
        }

        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            patient.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            patient.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }
        if (request.getMedicalHistory() != null) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getGuardianName() != null) {
            patient.setGuardianName(request.getGuardianName());
        }
        if (request.getGuardianPhone() != null) {
            patient.setGuardianPhone(request.getGuardianPhone());
        }
        if (request.getGuardianRelationship() != null) {
            patient.setGuardianRelationship(request.getGuardianRelationship());
        }
        if (request.getGuardianCitizenId() != null) {
            patient.setGuardianCitizenId(request.getGuardianCitizenId());
        }
        if (request.getIsActive() != null) {
            patient.setIsActive(request.getIsActive());
        }

        // Admin-only: Update booking block fields
        if (request.getIsBookingBlocked() != null) {
            patient.setIsBookingBlocked(request.getIsBookingBlocked());
        }
        if (request.getBookingBlockReason() != null) {
            try {
                com.dental.clinic.management.patient.enums.BookingBlockReason reason = com.dental.clinic.management.patient.enums.BookingBlockReason
                        .valueOf(request.getBookingBlockReason());
                patient.setBookingBlockReason(reason);
            } catch (IllegalArgumentException e) {
                // Invalid enum value - ignore or could throw validation error
            }
        }
        if (request.getBookingBlockNotes() != null) {
            patient.setBookingBlockNotes(request.getBookingBlockNotes());
        }
    }

    /**
     * Replace Patient entity from ReplacePatientRequest (PUT - full replacement)
     * All fields will be updated
     */
    public void replacePatientFromRequest(ReplacePatientRequest request, Patient patient) {
        if (request == null || patient == null) {
            return;
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAddress(request.getAddress());
        patient.setGender(request.getGender());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setIsActive(request.getIsActive());
    }
}
