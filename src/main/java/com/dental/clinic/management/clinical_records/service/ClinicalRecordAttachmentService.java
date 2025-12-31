package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.repository.AppointmentParticipantRepository;
import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import com.dental.clinic.management.clinical_records.domain.ClinicalRecordAttachment;
import com.dental.clinic.management.clinical_records.dto.AttachmentResponse;
import com.dental.clinic.management.clinical_records.dto.UploadAttachmentResponse;
import com.dental.clinic.management.clinical_records.enums.AttachmentTypeEnum;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordAttachmentRepository;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.service.FileStorageService;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clinical Record Attachment Service
 *
 * Handles file upload, list, and delete operations for clinical records
 * Includes RBAC checks reusing Appointment module logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalRecordAttachmentService {

    private final ClinicalRecordRepository clinicalRecordRepository;
    private final ClinicalRecordAttachmentRepository attachmentRepository;
    private final AppointmentParticipantRepository participantRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * API 8.11: Upload attachment to clinical record
     *
     * Authorization: UPLOAD_ATTACHMENT permission
     * RBAC: Same as API 8.1 (VIEW_APPOINTMENT_ALL or VIEW_APPOINTMENT_OWN)
     */
    @Transactional
    public UploadAttachmentResponse uploadAttachment(Integer recordId, MultipartFile file,
            AttachmentTypeEnum attachmentType, String description) {
        log.info("Uploading attachment for clinical record ID: {}", recordId);

        // Step 1: Load clinical record
        ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                        "Clinical record not found with ID: " + recordId));

        // Step 2: Load appointment for RBAC check
        Appointment appointment = record.getAppointment();
        if (appointment == null) {
            throw new NotFoundException("APPOINTMENT_NOT_FOUND",
                    "Appointment not found for clinical record ID: " + recordId);
        }

        // Step 3: Check RBAC (reuse from API 8.1)
        checkAccessPermission(appointment);

        // Step 4: Validate and store file
        fileStorageService.validateFile(file);
        String filePath;
        try {
            filePath = fileStorageService.storeFile(file, recordId);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Đã xảy ra lỗi khi lưu tệp: " + e.getMessage());
        }

        // Step 5: Get current employee (uploader)
        Integer employeeId = getCurrentEmployeeId();
        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId).orElse(null);
        }

        // Step 6: Create attachment record
        ClinicalRecordAttachment attachment = ClinicalRecordAttachment.builder()
                .clinicalRecord(record)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .attachmentType(attachmentType)
                .description(description)
                .uploadedBy(employee)
                .build();

        ClinicalRecordAttachment saved = attachmentRepository.save(attachment);

        log.info("Attachment uploaded successfully. ID: {}", saved.getAttachmentId());

        return UploadAttachmentResponse.builder()
                .attachmentId(saved.getAttachmentId())
                .clinicalRecordId(recordId)
                .fileName(saved.getFileName())
                .fileSize(saved.getFileSize())
                .mimeType(saved.getMimeType())
                .attachmentType(saved.getAttachmentType())
                .description(saved.getDescription())
                .uploadedAt(saved.getUploadedAt().format(FORMATTER))
                .message("File uploaded successfully")
                .build();
    }

    /**
     * API 8.12: Get all attachments for clinical record
     *
     * Authorization: VIEW_ATTACHMENT permission
     * RBAC: Same as API 8.1 (VIEW_APPOINTMENT_ALL or VIEW_APPOINTMENT_OWN)
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(Integer recordId) {
        log.info("Fetching attachments for clinical record ID: {}", recordId);

        // Step 1: Load clinical record
        ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                        "Clinical record not found with ID: " + recordId));

        // Step 2: Load appointment for RBAC check
        Appointment appointment = record.getAppointment();
        if (appointment == null) {
            throw new NotFoundException("APPOINTMENT_NOT_FOUND",
                    "Appointment not found for clinical record ID: " + recordId);
        }

        // Step 3: Check RBAC (reuse from API 8.1)
        checkAccessPermission(appointment);

        // Step 4: Fetch attachments
        List<ClinicalRecordAttachment> attachments = attachmentRepository
                .findByClinicalRecord_ClinicalRecordId(recordId);

        log.info("Found {} attachments for clinical record ID: {}", attachments.size(), recordId);

        return attachments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * API 8.13: Delete attachment
     *
     * Authorization: DELETE_ATTACHMENT permission
     * Business Rule: Only Admin or uploader can delete
     */
    @Transactional
    public void deleteAttachment(Integer attachmentId) {
        log.info("Deleting attachment ID: {}", attachmentId);

        // Step 1: Load attachment
        ClinicalRecordAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("ATTACHMENT_NOT_FOUND",
                        "Attachment not found with ID: " + attachmentId));

        // Step 2: Check permission (Admin or uploader only)
        checkDeletePermission(attachment);

        // Step 3: Delete file from filesystem
        fileStorageService.deleteFile(attachment.getFilePath());

        // Step 4: Delete database record
        attachmentRepository.delete(attachment);

        log.info("Attachment deleted successfully. ID: {}", attachmentId);
    }

    /**
     * Check if current user can access this appointment's clinical record
     * Reuses Appointment module RBAC logic (same as API 8.1)
     */
    private void checkAccessPermission(Appointment appointment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // Admin has full access
        }

        boolean hasViewAll = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("VIEW_APPOINTMENT_ALL"));

        if (hasViewAll) {
            return; // Receptionist/Manager can view all
        }

        // Check VIEW_APPOINTMENT_OWN
        boolean hasViewOwn = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("VIEW_APPOINTMENT_OWN"));

        if (hasViewOwn) {
            Integer employeeId = getCurrentEmployeeId();
            Integer patientId = getCurrentPatientId();

            // Check if user is primary doctor
            if (employeeId != null && appointment.getEmployeeId() != null &&
                    appointment.getEmployeeId().equals(employeeId)) {
                return;
            }

            // Check if user is participant (observer/nurse)
            if (employeeId != null) {
                boolean isParticipant = participantRepository
                        .findByIdAppointmentId(appointment.getAppointmentId())
                        .stream()
                        .anyMatch(ap -> ap.getId().getEmployeeId().equals(employeeId));
                if (isParticipant) {
                    return;
                }
            }

            // Check if user is patient
            if (patientId != null && appointment.getPatientId() != null &&
                    appointment.getPatientId().equals(patientId)) {
                return;
            }
        }

        throw new AccessDeniedException("Bạn không có quyền truy cập hồ sơ lâm sàng này");
    }

    /**
     * Check if current user can delete this attachment
     * Business Rule: Only Admin or uploader can delete
     */
    private void checkDeletePermission(ClinicalRecordAttachment attachment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // Admin can delete any attachment
        }

        // Check if current user is the uploader
        Integer currentEmployeeId = getCurrentEmployeeId();
        if (currentEmployeeId != null && attachment.getUploadedBy() != null &&
                attachment.getUploadedBy().getEmployeeId().equals(currentEmployeeId)) {
            return; // Uploader can delete own attachment
        }

        throw new AccessDeniedException("Bạn chỉ có thể xóa tệp đính kèm mà bạn đã tải lên");
    }

    /**
     * Get current employee ID from authentication
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElse(null);
        if (username == null) {
            return null;
        }

        return accountRepository.findOneByUsername(username)
                .filter(account -> account.getEmployee() != null)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElse(null);
    }

    /**
     * Get current patient ID from authentication
     */
    private Integer getCurrentPatientId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElse(null);
        if (username == null) {
            return null;
        }

        return accountRepository.findOneByUsername(username)
                .filter(account -> account.getPatient() != null)
                .map(account -> account.getPatient().getPatientId())
                .orElse(null);
    }

    /**
     * Map entity to DTO
     */
    private AttachmentResponse mapToResponse(ClinicalRecordAttachment attachment) {
        return AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .clinicalRecordId(attachment.getClinicalRecord().getClinicalRecordId())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .attachmentType(attachment.getAttachmentType())
                .description(attachment.getDescription())
                .uploadedBy(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getEmployeeId() : null)
                .uploadedByName(
                        attachment.getUploadedBy() != null ? attachment.getUploadedBy().getFullName() : "System")
                .uploadedAt(attachment.getUploadedAt().format(FORMATTER))
                .build();
    }
}
