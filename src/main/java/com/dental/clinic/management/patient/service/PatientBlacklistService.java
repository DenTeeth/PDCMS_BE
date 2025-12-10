package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.dto.BlacklistPatientResponse;
import com.dental.clinic.management.patient.enums.PatientBlacklistReason;
import com.dental.clinic.management.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;

/**
 * BR-044: Patient Blacklist Service
 * 
 * Business Rule: When adding a patient to "Blacklist", staff MUST select
 * a predefined reason (no free-text allowed).
 * 
 * Purpose: Standardize blacklist reasons, prevent abuse, maintain accountability
 * 
 * Authorization: Only MANAGER or ADMIN can blacklist patients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientBlacklistService {

    private final PatientRepository patientRepository;

    /**
     * Add a patient to blacklist with mandatory predefined reason.
     * 
     * BR-044: Reason must be selected from PatientBlacklistReason enum.
     * 
     * @param patientId Patient ID to blacklist
     * @param reason Predefined reason (required)
     * @param notes Additional notes (optional)
     * @return BlacklistPatientResponse with details
     * @throws ErrorResponseException if validation fails
     */
    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public BlacklistPatientResponse blacklistPatient(
            Integer patientId, 
            PatientBlacklistReason reason, 
            String notes) {
        
        log.info("Blacklist request for patient {} with reason: {}", patientId, reason);

        // 1. Validate reason (BR-044: Mandatory predefined reason)
        if (reason == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Lý do blacklist bắt buộc phải chọn từ danh sách định sẵn."
            );
            pd.setTitle("Blacklist Reason Required");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        // 2. Get patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy bệnh nhân với ID: " + patientId
                    );
                    pd.setTitle("Patient Not Found");
                    return new ErrorResponseException(HttpStatus.NOT_FOUND, pd, null);
                });

        // 3. Check if already blacklisted
        if (patient.getIsBlacklisted()) {
            log.warn("Patient {} is already blacklisted", patientId);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Bệnh nhân này đã bị blacklist rồi."
            );
            pd.setTitle("Patient Already Blacklisted");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        // 4. Get current user info from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 5. Perform blacklist
        patient.setIsBlacklisted(true);
        patient.setBlacklistReason(reason);
        patient.setBlacklistNotes(notes != null ? notes.trim() : null);
        patient.setBlacklistedBy(username);
        patient.setBlacklistedAt(LocalDateTime.now());

        // Also block booking if not already blocked
        if (!patient.getIsBookingBlocked()) {
            patient.setIsBookingBlocked(true);
            patient.setBookingBlockReason("Bệnh nhân bị blacklist: " + reason.getDisplayName());
            patient.setBlockedAt(LocalDateTime.now());
        }

        patientRepository.save(patient);

        log.warn("Patient {} BLACKLISTED by {} for reason: {}", 
                 patientId, username, reason.getDisplayName());

        // 6. Return result
        return BlacklistPatientResponse.builder()
                .message("Đã thêm bệnh nhân vào blacklist thành công")
                .patientId(patientId)
                .patientName(patient.getFullName())
                .blacklistReason(reason)
                .blacklistReasonDisplay(reason.getDisplayName())
                .notes(notes)
                .blacklistedBy(username)
                .blacklistedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Remove a patient from blacklist.
     * 
     * @param patientId Patient ID to remove from blacklist
     * @param reason Reason for removal (audit trail)
     * @return BlacklistPatientResponse with details
     * @throws ErrorResponseException if validation fails
     */
    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public BlacklistPatientResponse removeFromBlacklist(Integer patientId, String reason) {
        
        log.info("Remove from blacklist request for patient {}", patientId);

        // 1. Get patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy bệnh nhân với ID: " + patientId
                    );
                    pd.setTitle("Patient Not Found");
                    return new ErrorResponseException(HttpStatus.NOT_FOUND, pd, null);
                });

        // 2. Check if actually blacklisted
        if (!patient.getIsBlacklisted()) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Bệnh nhân này không có trong blacklist."
            );
            pd.setTitle("Patient Not Blacklisted");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        // 3. Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 4. Remove from blacklist (keep history in logs)
        PatientBlacklistReason previousReason = patient.getBlacklistReason();
        
        patient.setIsBlacklisted(false);
        patient.setBlacklistReason(null);
        patient.setBlacklistNotes(null);
        patient.setBlacklistedBy(null);
        patient.setBlacklistedAt(null);

        patientRepository.save(patient);

        log.info("Patient {} removed from blacklist by {}, reason: {}", 
                 patientId, username, reason);

        // 5. Return result
        return BlacklistPatientResponse.builder()
                .message("Đã xóa bệnh nhân khỏi blacklist")
                .patientId(patientId)
                .patientName(patient.getFullName())
                .blacklistReason(previousReason)
                .blacklistReasonDisplay(previousReason != null ? previousReason.getDisplayName() : null)
                .notes(reason)
                .blacklistedBy(username)
                .blacklistedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if a patient is blacklisted.
     * 
     * @param patientId Patient ID
     * @return true if blacklisted
     */
    @Transactional(readOnly = true)
    public boolean isPatientBlacklisted(Integer patientId) {
        return patientRepository.findById(patientId)
                .map(Patient::getIsBlacklisted)
                .orElse(false);
    }
}
