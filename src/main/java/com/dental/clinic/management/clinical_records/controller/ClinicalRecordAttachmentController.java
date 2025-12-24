package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.dto.AttachmentResponse;
import com.dental.clinic.management.clinical_records.dto.UploadAttachmentResponse;
import com.dental.clinic.management.clinical_records.enums.AttachmentTypeEnum;
import com.dental.clinic.management.clinical_records.service.ClinicalRecordAttachmentService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Clinical Record Attachments API Controller
 * Module #9: Clinical Records Management
 *
 * API 8.11: POST /api/v1/clinical-records/{recordId}/attachments
 * API 8.12: GET /api/v1/clinical-records/{recordId}/attachments
 * API 8.13: DELETE /api/v1/attachments/{attachmentId}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ClinicalRecordAttachmentController {

    private final ClinicalRecordAttachmentService attachmentService;

    /**
     * API 8.11: Upload Attachment to Clinical Record
     *
     * Uploads file (X-ray, photo, document) to clinical record.
     *
     * Authorization:
     * - UPLOAD_ATTACHMENT: Doctor, Assistant, Admin
     * - RBAC check: Same as API 8.1 (VIEW_APPOINTMENT_ALL or VIEW_APPOINTMENT_OWN)
     *
     * File Validation:
     * - Max size: 10 MB
     * - Allowed types: JPEG, PNG, GIF, PDF
     *
     * Returns:
     * - 201 CREATED: File uploaded successfully
     * - 400 BAD_REQUEST: Invalid file (size/type)
     * - 404 RECORD_NOT_FOUND: Clinical record doesn't exist
     * - 403 FORBIDDEN: No permission to access this record
     */
    @PostMapping(value = "/api/v1/clinical-records/{recordId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('MANAGE_ATTACHMENTS')")
    @ApiMessage("File uploaded successfully")
    public ResponseEntity<UploadAttachmentResponse> uploadAttachment(
            @PathVariable Integer recordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("attachmentType") AttachmentTypeEnum attachmentType,
            @RequestParam(value = "description", required = false) String description) {

        log.info("API 8.11: POST /api/v1/clinical-records/{}/attachments - type: {}",
                recordId, attachmentType);

        UploadAttachmentResponse response = attachmentService.uploadAttachment(
                recordId, file, attachmentType, description);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 8.12: Get All Attachments for Clinical Record
     *
     * Retrieves list of all files attached to a clinical record.
     * Returns empty array if no attachments.
     *
     * Authorization:
     * - VIEW_ATTACHMENT: Doctor, Nurse, Admin, Patient (own records)
     * - RBAC check: Same as API 8.1 (VIEW_APPOINTMENT_ALL or VIEW_APPOINTMENT_OWN)
     *
     * Returns:
     * - 200 OK: List of attachments (empty array if none)
     * - 404 RECORD_NOT_FOUND: Clinical record doesn't exist
     * - 403 FORBIDDEN: No permission to access this record
     */
    @GetMapping("/api/v1/clinical-records/{recordId}/attachments")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_ATTACHMENT')")
    @ApiMessage("Attachments retrieved successfully")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Integer recordId) {

        log.info("API 8.12: GET /api/v1/clinical-records/{}/attachments", recordId);

        List<AttachmentResponse> response = attachmentService.getAttachments(recordId);

        return ResponseEntity.ok(response);
    }

    /**
     * API 8.13: Delete Attachment
     *
     * Deletes a file attachment from clinical record.
     * Business Rule: Only Admin or uploader can delete.
     *
     * Authorization:
     * - MANAGE_ATTACHMENTS: Doctor, Assistant, Admin
     * - Business Rule: Can only delete own uploads (except Admin)
     *
     * Returns:
     * - 204 NO_CONTENT: Attachment deleted successfully
     * - 404 ATTACHMENT_NOT_FOUND: Attachment doesn't exist
     * - 403 DELETE_DENIED: Not the uploader (non-admin)
     */
    @DeleteMapping("/api/v1/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('MANAGE_ATTACHMENTS')")
    @ApiMessage("Attachment deleted successfully")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Integer attachmentId) {

        log.info("API 8.13: DELETE /api/v1/attachments/{}", attachmentId);

        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }
}
