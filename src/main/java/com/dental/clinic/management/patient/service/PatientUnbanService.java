package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.domain.PatientUnbanAuditLog;
import com.dental.clinic.management.patient.dto.UnbanPatientResponse;
import com.dental.clinic.management.patient.dto.AuditLogResponse;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.patient.repository.PatientUnbanAuditLogRepository;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business Rules BR-085 & BR-086: Patient Unban Service
 * 
 * BR-085: Unban Authority
 * - Receptionists are authorized to reset a patient's "No-Show" counter to zero
 * - No Manager approval required (to facilitate quick re-booking)
 * - Applies to both consecutiveNoShows and isBookingBlocked flags
 * 
 * BR-086: Mandatory Log
 * - Receptionists MUST input a specific reason for every unban action
 * - Reason is logged in audit table for accountability
 * - Managers can review unban logs to detect abuse
 * 
 * Implementation:
 * 1. Permission: RECEPTIONIST, MANAGER, or ADMIN can unban
 * 2. Validation: Reason is required (min 10 chars, max 500 chars)
 * 3. Action: Reset consecutiveNoShows to 0, set isBookingBlocked to false
 * 4. Audit: Log who, when, why, and previous no-show count
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientUnbanService {

    private final PatientRepository patientRepository;
    private final PatientUnbanAuditLogRepository auditLogRepository;

    /**
     * Unban a patient (reset no-show counter and booking block).
     * 
     * BR-085: Receptionists can unban without Manager approval.
     * BR-086: Reason is mandatory and logged for accountability.
     * 
     * @param patientId Patient ID to unban
     * @param reason Reason for unban (required, min 10 chars)
     * @return Unban result details
     * @throws ErrorResponseException if validation fails
     */
    @Transactional
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")
    public UnbanPatientResponse unbanPatient(Integer patientId, String reason) {
        
        log.info("Unban request for patient {} with reason: {}", patientId, reason);

        // 1. Validate reason (BR-086: Mandatory log requirement)
        validateReason(reason);

        // 2. Get patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy bệnh nhân với ID: " + patientId
                    );
                    pd.setTitle("Không Tìm Thấy Bệnh Nhân");
                    return new ErrorResponseException(HttpStatus.NOT_FOUND, pd, null);
                });

        // 3. Check if patient is actually blocked
        if (!patient.getIsBookingBlocked() && patient.getConsecutiveNoShows() == 0) {
            log.warn("Patient {} is not blocked, unban not needed", patientId);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Bệnh nhân này chưa bị chặn đặt lịch. Không cần mở khóa."
            );
            pd.setTitle("Bệnh Nhân Chưa Bị Chặn");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        // 4. Get current user info from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");

        // 5. Save previous state for audit
        Integer previousNoShowCount = patient.getConsecutiveNoShows();

        // 6. Perform unban (BR-085: Reset counter and block flag)
        patient.setConsecutiveNoShows(0);
        patient.setIsBookingBlocked(false);
        patient.setBookingBlockReason(null);
        patient.setBlockedAt(null);
        patientRepository.save(patient);

        log.info("Patient {} unbanned successfully by {} ({})", patientId, username, role);

        // 7. Create audit log (BR-086: Mandatory logging)
        PatientUnbanAuditLog auditLog = PatientUnbanAuditLog.builder()
                .patientId(patientId)
                .previousNoShowCount(previousNoShowCount)
                .performedBy(username)
                .performedByRole(role)
                .reason(reason.trim())
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(auditLog);

        log.info("Audit log created for patient {} unban by {}", patientId, username);

        // 8. Return result
        return UnbanPatientResponse.builder()
                .message("Mở khóa bệnh nhân thành công")
                .patientId(patientId)
                .patientName(patient.getFullName())
                .previousNoShowCount(previousNoShowCount)
                .newNoShowCount(0)
                .unbanBy(username)
                .unbanByRole(role)
                .unbanAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate unban reason.
     * 
     * BR-086: Reason is mandatory to ensure accountability.
     * 
     * @param reason Unban reason
     * @throws ErrorResponseException if validation fails
     */
    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Lễ tân bắt buộc phải nhập lý do mở khóa (VD: Khách trình bày lý do ốm, Khách cam kết không tái phạm...)"
            );
            pd.setTitle("Reason Required");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        String trimmedReason = reason.trim();

        if (trimmedReason.length() < 10) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Lý do mở khóa phải có ít nhất 10 ký tự để đảm bảo tính minh bạch."
            );
            pd.setTitle("Reason Too Short");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }

        if (trimmedReason.length() > 500) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Lý do mở khóa không được vượt quá 500 ký tự."
            );
            pd.setTitle("Reason Too Long");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, pd, null);
        }
    }

    /**
     * Get unban history for a patient.
     * 
     * @param patientId Patient ID
     * @return List of unban audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getPatientUnbanHistory(Integer patientId) {
        List<PatientUnbanAuditLog> auditLogs = auditLogRepository.findByPatientIdOrderByTimestampDesc(patientId);
        
        return auditLogs.stream()
                .map(log -> {
                    Patient patient = patientRepository.findById(log.getPatientId()).orElse(null);
                    return AuditLogResponse.builder()
                            .auditId(log.getAuditId())
                            .patientId(log.getPatientId())
                            .patientName(patient != null ? patient.getFullName() : "Unknown")
                            .previousNoShowCount(log.getPreviousNoShowCount())
                            .performedBy(log.getPerformedBy())
                            .performedByRole(log.getPerformedByRole())
                            .reason(log.getReason())
                            .timestamp(log.getTimestamp())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if patient is currently blocked.
     * 
     * @param patientId Patient ID
     * @return true if patient is blocked from booking
     */
    @Transactional(readOnly = true)
    public boolean isPatientBlocked(Integer patientId) {
        return patientRepository.findById(patientId)
                .map(Patient::getIsBookingBlocked)
                .orElse(false);
    }
}
