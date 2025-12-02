package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.dto.CreateClinicalRecordRequest;
import com.dental.clinic.management.clinical_records.dto.CreateClinicalRecordResponse;
import com.dental.clinic.management.clinical_records.dto.UpdateClinicalRecordRequest;
import com.dental.clinic.management.clinical_records.dto.UpdateClinicalRecordResponse;
import com.dental.clinic.management.clinical_records.service.ClinicalRecordService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/clinical-records")
@RequiredArgsConstructor
public class ClinicalRecordWriteController {

    private final ClinicalRecordService clinicalRecordService;

    /**
     * API 9.2: Create Clinical Record
     *
     * Creates a new clinical record for an appointment. This is the first step when
     * patient sits in the dental chair. System records chief complaint, vital signs
     * and initial diagnosis.
     *
     * Authorization: Requires WRITE_CLINICAL_RECORD permission (Doctor, Assistant,
     * Admin)
     *
     * Business Rules:
     * 1. Appointment must exist
     * 2. Appointment must be IN_PROGRESS or CHECKED_IN
     * 3. No existing clinical record for this appointment (409 if duplicate)
     * 4. All required fields must be provided
     *
     * Returns:
     * - 201 CREATED: Record created successfully
     * - 409 CONFLICT: Record already exists for this appointment
     * - 400 BAD_REQUEST: Invalid appointment status or missing required fields
     * - 404 NOT_FOUND: Appointment not found
     * - 403 FORBIDDEN: No WRITE_CLINICAL_RECORD permission
     *
     * @param request CreateClinicalRecordRequest
     * @return CreateClinicalRecordResponse with recordId, appointmentId, createdAt
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('WRITE_CLINICAL_RECORD')")
    @ApiMessage("Clinical record created successfully")
    public ResponseEntity<CreateClinicalRecordResponse> createClinicalRecord(
            @Valid @RequestBody CreateClinicalRecordRequest request) {

        log.info("API 9.2: POST /api/v1/clinical-records - appointmentId: {}", request.getAppointmentId());

        CreateClinicalRecordResponse response = clinicalRecordService.createClinicalRecord(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 9.3: Update Clinical Record
     *
     * Updates existing clinical record. Usually used when doctor wants to update
     * examination findings after X-ray results, or update vital signs.
     *
     * Authorization: Requires WRITE_CLINICAL_RECORD permission
     *
     * Business Rules:
     * 1. Record must exist
     * 2. Only update provided fields (partial update)
     * 3. Cannot update appointmentId, chiefComplaint, diagnosis
     * 4. Auto update updated_at timestamp
     *
     * Returns:
     * - 200 OK: Record updated successfully
     * - 404 NOT_FOUND: Record not found
     * - 403 FORBIDDEN: No WRITE_CLINICAL_RECORD permission
     *
     * @param recordId Clinical record ID
     * @param request  UpdateClinicalRecordRequest
     * @return UpdateClinicalRecordResponse with recordId, updatedAt, updated fields
     */
    @PutMapping("/{recordId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('WRITE_CLINICAL_RECORD')")
    @ApiMessage("Clinical record updated successfully")
    public ResponseEntity<UpdateClinicalRecordResponse> updateClinicalRecord(
            @PathVariable Integer recordId,
            @Valid @RequestBody UpdateClinicalRecordRequest request) {

        log.info("API 9.3: PUT /api/v1/clinical-records/{}", recordId);

        UpdateClinicalRecordResponse response = clinicalRecordService.updateClinicalRecord(recordId, request);

        return ResponseEntity.ok(response);
    }
}
