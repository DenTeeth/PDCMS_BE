
package com.dental.clinic.management.patient.controller;

import com.dental.clinic.management.patient.dto.request.CreatePatientRequest;
import com.dental.clinic.management.patient.dto.request.ReplacePatientRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientRequest;
import com.dental.clinic.management.patient.dto.response.PatientInfoResponse;
import com.dental.clinic.management.patient.dto.ToothStatusResponse;
import com.dental.clinic.management.patient.dto.UpdateToothStatusRequest;
import com.dental.clinic.management.patient.dto.UpdateToothStatusResponse;
import com.dental.clinic.management.patient.dto.UnbanPatientRequest;
import com.dental.clinic.management.patient.dto.UnbanPatientResponse;
import com.dental.clinic.management.patient.dto.AuditLogResponse;
import com.dental.clinic.management.patient.dto.DuplicatePatientCheckResult;
import com.dental.clinic.management.patient.dto.BlacklistPatientRequest;
import com.dental.clinic.management.patient.dto.BlacklistPatientResponse;
import com.dental.clinic.management.patient.service.PatientService;
import com.dental.clinic.management.patient.service.PatientUnbanService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing patients
 */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient Management", description = "APIs for managing patients with RBAC (Admin and authorized roles only)")
public class PatientController {

    private final PatientService patientService;
    private final PatientUnbanService patientUnbanService;
    private final com.dental.clinic.management.patient.service.DuplicatePatientDetectionService duplicateDetectionService;
    private final com.dental.clinic.management.patient.service.PatientBlacklistService blacklistService;

    public PatientController(
            PatientService patientService,
            PatientUnbanService patientUnbanService,
            com.dental.clinic.management.patient.service.DuplicatePatientDetectionService duplicateDetectionService,
            com.dental.clinic.management.patient.service.PatientBlacklistService blacklistService) {
        this.patientService = patientService;
        this.patientUnbanService = patientUnbanService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.blacklistService = blacklistService;
    }

    /**
     * {@code GET  /patients} : get all active patients with pagination
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of patients in body
     */
    @GetMapping("")
    @Operation(summary = "Get all active patients", description = "Retrieve a paginated list of active patients only")
    @ApiMessage("Lấy danh sách bệnh nhân đang hoạt động thành công")
    public ResponseEntity<Page<PatientInfoResponse>> getAllActivePatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "patientCode") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Page<PatientInfoResponse> response = patientService.getAllActivePatients(page, size, sortBy, sortDirection);
        return ResponseEntity.ok().body(response);
    }

    /**
     * {@code GET  /patients/admin/all} : get ALL patients including deleted ones
     * This endpoint is for admin management purposes only
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of patients in body
     */
    @GetMapping("/admin/all")
    @Operation(summary = "Get all patients (Admin)", description = "Retrieve all patients including deleted ones (Admin only)")
    @ApiMessage("Lấy tất cả bệnh nhân bao gồm đã xóa thành công")
    public ResponseEntity<Page<PatientInfoResponse>> getAllPatientsIncludingDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "patientCode") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Page<PatientInfoResponse> response = patientService.getAllPatientsIncludingDeleted(page, size, sortBy,
                sortDirection);
        return ResponseEntity.ok().body(response);
    }

    /**
     * {@code GET  /patients/:patientCode} : get active patient by patient code
     *
     * @param patientCode the code of the patient to retrieve
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and patient
     *         in body
     */
    @GetMapping("/{patientCode}")
    @Operation(summary = "Get patient by code", description = "Get active patient details by patient code")
    @ApiMessage("Lấy thông tin bệnh nhân theo mã thành công")
    public ResponseEntity<PatientInfoResponse> getActivePatientByCode(
            @Parameter(description = "Patient code (e.g., PAT001)", required = true) @PathVariable("patientCode") String patientCode) {
        PatientInfoResponse response = patientService.getActivePatientByCode(patientCode);
        return ResponseEntity.ok(response);
    }

    /**
     * {@code GET  /patients/admin/:patientCode} : get patient by code including
     * deleted ones
     * This endpoint is for admin management purposes only
     *
     * @param patientCode the code of the patient to retrieve
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and patient
     *         in body
     */
    @GetMapping("/admin/{patientCode}")
    @Operation(summary = "Get patient by code (Admin)", description = "Get patient details including deleted ones (Admin only)")
    @ApiMessage("Lấy thông tin bệnh nhân bao gồm đã xóa thành công")
    public ResponseEntity<PatientInfoResponse> getPatientByCodeIncludingDeleted(
            @Parameter(description = "Patient code (e.g., PAT001)", required = true) @PathVariable("patientCode") String patientCode) {
        PatientInfoResponse response = patientService.getPatientByCodeIncludingDeleted(patientCode);
        return ResponseEntity.ok(response);
    }

    /**
     * {@code POST  /patients} : create a new patient
     *
     * @param request the patient information to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new patient
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("")
    @Operation(summary = "Create new patient", description = "Create a new patient record (Admin or authorized roles only)")
    @ApiMessage("Tạo bệnh nhân thành công")
    public ResponseEntity<PatientInfoResponse> createPatient(@Valid @RequestBody CreatePatientRequest request)
            throws URISyntaxException {
        PatientInfoResponse result = patientService.createPatient(request);
        return ResponseEntity
                .created(new URI("/api/v1/patients/" + result.getPatientCode()))
                .body(result);
    }

    /**
     * {@code PATCH  /patients/:patientCode} : Partial updates given fields of an
     * existing patient
     * Field will be updated only if value is not null
     *
     * @param patientCode the code of the patient to update
     * @param request     the patient information to update
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated patient
     */
    @PatchMapping("/{patientCode}")
    @Operation(summary = "Update patient (partial)", description = "Update specific fields of a patient (null fields are ignored)")
    @ApiMessage("Cập nhật bệnh nhân thành công")
    public ResponseEntity<PatientInfoResponse> updatePatient(
            @Parameter(description = "Patient code", required = true) @PathVariable("patientCode") String patientCode,
            @Valid @RequestBody UpdatePatientRequest request) {
        PatientInfoResponse result = patientService.updatePatient(patientCode, request);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code PUT  /patients/:patientCode} : Replace (full update) an existing
     * patient
     * All fields will be updated with the provided values
     *
     * @param patientCode the code of the patient to replace
     * @param request     the patient information to replace
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the replaced patient
     */
    @PutMapping("/{patientCode}")
    @Operation(summary = "Replace patient (full update)", description = "Replace entire patient data (all fields required)")
    @ApiMessage("Thay thế thông tin bệnh nhân thành công")
    public ResponseEntity<PatientInfoResponse> replacePatient(
            @Parameter(description = "Patient code", required = true) @PathVariable("patientCode") String patientCode,
            @Valid @RequestBody ReplacePatientRequest request) {
        PatientInfoResponse result = patientService.replacePatient(patientCode, request);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code DELETE  /patients/:patientCode} : soft delete the patient by patient
     * code
     *
     * @param patientCode the code of the patient to delete
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
     */
    @DeleteMapping("/{patientCode}")
    @Operation(summary = "Delete patient (soft delete)", description = "Soft delete patient by setting isActive to false")
    @ApiMessage("Xóa bệnh nhân thành công")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient code", required = true) @PathVariable("patientCode") String patientCode) {
        patientService.deletePatient(patientCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET  /patients/:patientId/tooth-status} : get all tooth statuses for a
     * patient
     * API 8.9 - Used for Odontogram visualization
     * Only returns abnormal teeth - teeth not in response are considered HEALTHY
     *
     * @param patientId the patient ID
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of tooth statuses
     */
    @GetMapping("/{patientId}/tooth-status")
    @Operation(summary = "Get patient tooth status", description = "Get all abnormal tooth conditions for Odontogram visualization (API 8.9)")
    @ApiMessage("Lấy trạng thái răng thành công")
    public ResponseEntity<List<ToothStatusResponse>> getToothStatus(
            @Parameter(description = "Patient ID", required = true) @PathVariable("patientId") Integer patientId) {
        List<ToothStatusResponse> response = patientService.getToothStatus(patientId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * {@code PUT  /patients/:patientId/tooth-status/:toothNumber} : update tooth
     * status (OLD PATH PARAMETER STYLE - KEPT FOR BACKWARD COMPATIBILITY)
     * API 8.10 - Updates tooth status with automatic history tracking
     *
     * @param patientId   the patient ID
     * @param toothNumber the tooth number
     * @param request     the update request
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the
     *         updated tooth status
     */
    @PutMapping("/{patientId}/tooth-status/{toothNumber}")
    @Operation(summary = "Update tooth status (path param style)", description = "Update tooth status with automatic history tracking - OLD ENDPOINT (API 8.10)")
    @ApiMessage("Cập nhật trạng thái răng thành công")
    public ResponseEntity<UpdateToothStatusResponse> updateToothStatusWithPathParam(
            @Parameter(description = "Patient ID", required = true) @PathVariable("patientId") Integer patientId,
            @Parameter(description = "Tooth number", required = true) @PathVariable("toothNumber") String toothNumber,
            @Valid @RequestBody UpdateToothStatusRequest request) {

        // For now, using a hardcoded employee ID (will be replaced with
        // SecurityUtils.getCurrentUser())
        Integer changedBy = 1;

        UpdateToothStatusResponse response = patientService.updateToothStatus(patientId, toothNumber, request,
                changedBy);
        return ResponseEntity.ok().body(response);
    }

    /**
     * {@code PUT  /patients/:patientId/tooth-status} : update tooth status
     * API 8.10 - Updates tooth status with toothNumber in request body (NEW
     * STANDARD)
     * Frontend sends toothNumber in body, not path parameter
     *
     * Business Logic:
     * - If tooth status doesn't exist: CREATE new record
     * - If tooth status exists: UPDATE existing record
     * - If status = HEALTHY: DELETE record (tooth returns to default state)
     *
     * @param patientId the patient ID
     * @param request   the update request (includes toothNumber, status, notes)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the
     *         updated tooth status, or null if deleted (HEALTHY)
     */
    @PutMapping("/{patientId}/tooth-status")
    @Operation(summary = "Update tooth status (body param style)", description = "Update tooth status with toothNumber in body - NEW STANDARD (API 8.10)")
    @ApiMessage("Cập nhật trạng thái răng thành công")
    public ResponseEntity<UpdateToothStatusResponse> updateToothStatus(
            @Parameter(description = "Patient ID", required = true) @PathVariable("patientId") Integer patientId,
            @Valid @RequestBody com.dental.clinic.management.clinical_records.dto.UpdateToothStatusRequest request) {

        // For now, using a hardcoded employee ID (will be replaced with
        // SecurityUtils.getCurrentUser())
        Integer changedBy = 1;

        // Convert clinical_records DTO (with toothNumber validation) to patient DTO
        com.dental.clinic.management.patient.dto.UpdateToothStatusRequest patientRequest = com.dental.clinic.management.patient.dto.UpdateToothStatusRequest
                .builder()
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();

        UpdateToothStatusResponse response = patientService.updateToothStatus(
                patientId,
                request.getToothNumber(),
                patientRequest,
                changedBy);
        return ResponseEntity.ok().body(response);
    }

    /**
     * {@code POST  /patients/:id/unban} : Unban a patient (reset no-show count and
     * booking block)
     *
     * BR-085: Receptionist has authority to unban without Manager approval
     * BR-086: Must log reason for accountability
     *
     * @param patientId the patient ID to unban
     * @param request   the unban request with reason
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and unban
     *         details
     */
    @PostMapping("/{id}/unban")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")
    @ApiMessage("Mở khóa bệnh nhân thành công")
    @Operation(summary = "Unban patient", description = "Receptionist/Manager/Admin can unban a patient and reset no-show count. Requires reason (10-500 chars) for audit log.")
    public ResponseEntity<UnbanPatientResponse> unbanPatient(
            @PathVariable("id") Integer patientId,
            @Valid @RequestBody UnbanPatientRequest request) {

        UnbanPatientResponse response = patientUnbanService.unbanPatient(patientId, request.getReason());
        return ResponseEntity.ok(response);
    }

    /**
     * {@code GET  /patients/:id/unban-history} : Get unban history for a patient
     *
     * @param patientId the patient ID
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and audit log
     *         list
     */
    @GetMapping("/{id}/unban-history")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")
    @ApiMessage("Lấy lịch sử mở khóa bệnh nhân")
    @Operation(summary = "Get patient unban history", description = "Get audit log of all unban actions for a specific patient")
    public ResponseEntity<List<AuditLogResponse>> getUnbanHistory(@PathVariable("id") Integer patientId) {

        List<AuditLogResponse> history = patientUnbanService.getPatientUnbanHistory(patientId);
        return ResponseEntity.ok(history);
    }

    /**
     * {@code GET  /patients/check-duplicate} : Check for duplicate patients
     *
     * BR-043: Check for duplicates by Name + DOB or Phone
     *
     * @param firstName   First name
     * @param lastName    Last name
     * @param dateOfBirth Date of birth (YYYY-MM-DD)
     * @param phone       Phone number (optional)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and duplicate
     *         check result
     */
    @GetMapping("/check-duplicate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('MANAGER')")
    @ApiMessage("Kiểm tra bệnh nhân trùng")
    @Operation(summary = "Check for duplicate patients", description = "Check if a patient with similar information already exists. Returns potential matches by Name+DOB or Phone.")
    public ResponseEntity<DuplicatePatientCheckResult> checkDuplicate(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam java.time.LocalDate dateOfBirth,
            @RequestParam(required = false) String phone) {

        DuplicatePatientCheckResult result = duplicateDetectionService.checkForDuplicates(
                firstName, lastName, dateOfBirth, phone);
        return ResponseEntity.ok(result);
    }

    /**
     * {@code POST  /patients/:id/blacklist} : Add patient to blacklist
     *
     * BR-044: Mandatory predefined reason when blacklisting
     *
     * @param patientId the patient ID to blacklist
     * @param request   the blacklist request with reason
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and blacklist
     *         details
     */
    @PostMapping("/{id}/blacklist")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @ApiMessage("Thêm bệnh nhân vào blacklist thành công")
    @Operation(summary = "Blacklist patient", description = "Manager/Admin can add a patient to blacklist with mandatory predefined reason. Patient will also be blocked from booking.")
    public ResponseEntity<BlacklistPatientResponse> blacklistPatient(
            @PathVariable("id") Integer patientId,
            @Valid @RequestBody BlacklistPatientRequest request) {

        BlacklistPatientResponse response = blacklistService.blacklistPatient(
                patientId, request.getReason(), request.getNotes());
        return ResponseEntity.ok(response);
    }

    /**
     * {@code DELETE  /patients/:id/blacklist} : Remove patient from blacklist
     *
     * @param patientId the patient ID to remove from blacklist
     * @param reason    Reason for removal (audit trail)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and removal
     *         details
     */
    @DeleteMapping("/{id}/blacklist")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @ApiMessage("Xóa bệnh nhân khỏi blacklist")
    @Operation(summary = "Remove from blacklist", description = "Manager/Admin can remove a patient from blacklist")
    public ResponseEntity<BlacklistPatientResponse> removeFromBlacklist(
            @PathVariable("id") Integer patientId,
            @RequestParam(required = false) String reason) {

        BlacklistPatientResponse response = blacklistService.removeFromBlacklist(patientId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * {@code GET  /patients/me/profile} : Get current patient profile (Patient
     * Portal API for Mobile App)
     *
     * Patient can access their own profile with full details including:
     * - Personal information (name, email, phone, DOB, address)
     * - Medical information (medical history, allergies)
     * - Emergency contact
     * - Guardian information (for minors)
     * - Booking status
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and patient
     *         profile
     */
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @ApiMessage("Lấy thông tin bệnh nhân cho ứng dụng di động thành công")
    @Operation(summary = "Get current patient profile", description = "Patient can view their own full profile details (for mobile app)")
    public ResponseEntity<com.dental.clinic.management.patient.dto.response.PatientDetailResponse> getCurrentPatientProfile(
            @Parameter(hidden = true) org.springframework.security.core.Authentication authentication) {

        String username = authentication.getName();
        com.dental.clinic.management.patient.dto.response.PatientDetailResponse response = patientService
                .getCurrentPatientProfile(username);
        return ResponseEntity.ok(response);
    }
}
