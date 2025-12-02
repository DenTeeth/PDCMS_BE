package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.dto.AddProcedureRequest;
import com.dental.clinic.management.clinical_records.dto.AddProcedureResponse;
import com.dental.clinic.management.clinical_records.dto.ClinicalRecordResponse;
import com.dental.clinic.management.clinical_records.dto.ProcedureResponse;
import com.dental.clinic.management.clinical_records.service.ClinicalRecordService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Clinical Records API Controller
 * Module #9: Clinical Records Management
 *
 * API 8.1: GET /api/v1/appointments/{appointmentId}/clinical-record
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class ClinicalRecordController {

    private final ClinicalRecordService clinicalRecordService;

    /**
     * API 8.1: Get Clinical Record for Appointment
     *
     * Authorization:
     * - ROLE_ADMIN: Full access to all records
     * - VIEW_APPOINTMENT_ALL: Access to all records (Receptionist, Manager)
     * - VIEW_APPOINTMENT_OWN: Access only to related records (Doctor, Patient,
     * Observer)
     *
     * Returns:
     * - 200 OK: Clinical record found (with nested appointment, doctor, patient,
     * procedures, prescriptions)
     * - 404 RECORD_NOT_FOUND: No clinical record for this appointment (frontend
     * shows CREATE form)
     * - 404 APPOINTMENT_NOT_FOUND: Appointment doesn't exist
     * - 403 UNAUTHORIZED_ACCESS: User doesn't have permission to view this
     * appointment
     *
     * @param appointmentId The appointment ID
     * @return ClinicalRecordResponse with full nested data
     */
    @GetMapping("/{appointmentId}/clinical-record")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_APPOINTMENT_ALL') or hasAuthority('VIEW_APPOINTMENT_OWN')")
    @ApiMessage("Get clinical record successfully")
    public ResponseEntity<ClinicalRecordResponse> getClinicalRecord(
            @PathVariable Integer appointmentId) {

        log.info("API 8.1: GET /api/v1/appointments/{}/clinical-record", appointmentId);

        ClinicalRecordResponse response = clinicalRecordService.getClinicalRecord(appointmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * API 8.4: Get Procedures for Clinical Record
     *
     * Retrieves all procedures performed during a clinical visit.
     * This API is typically called after loading clinical record detail to display
     * the "Work Done" table.
     *
     * Authorization:
     * - ROLE_ADMIN: Full access to all records
     * - VIEW_APPOINTMENT_ALL: Access to all records (Receptionist, Manager)
     * - VIEW_APPOINTMENT_OWN: Access only to related records (Doctor, Patient,
     * Observer)
     *
     * Returns:
     * - 200 OK: List of procedures (empty array if none added yet)
     * - 404 RECORD_NOT_FOUND: Clinical record doesn't exist
     * - 403 UNAUTHORIZED_ACCESS: User doesn't have permission to view this record
     *
     * @param recordId The clinical record ID
     * @return List of ProcedureResponse with service information
     */
    @GetMapping("/clinical-records/{recordId}/procedures")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_APPOINTMENT_ALL') or hasAuthority('VIEW_APPOINTMENT_OWN')")
    @ApiMessage("Procedures retrieved successfully")
    public ResponseEntity<List<ProcedureResponse>> getProcedures(
            @PathVariable Integer recordId) {

        log.info("API 8.4: GET /api/v1/appointments/clinical-records/{}/procedures", recordId);

        List<ProcedureResponse> procedures = clinicalRecordService.getProcedures(recordId);

        return ResponseEntity.ok(procedures);
    }

    /**
     * API 8.5: Add Procedure to Clinical Record
     *
     * Records a procedure/service performed during the appointment.
     * This API allows doctors to document work done in real-time or post-visit.
     *
     * Business Logic:
     * 1. Validates clinical record exists
     * 2. Validates service exists and is active
     * 3. Creates passive link to treatment plan item (if provided)
     * 4. Does NOT update treatment plan item status (handled by appointment completion or API 5.6)
     *
     * Authorization:
     * - WRITE_CLINICAL_RECORD: Doctor, Assistant, Admin
     *
     * Returns:
     * - 201 CREATED: Procedure added successfully
     * - 404 RECORD_NOT_FOUND: Clinical record doesn't exist
     * - 404 SERVICE_NOT_FOUND: Service doesn't exist or is inactive
     * - 404 PLAN_ITEM_NOT_FOUND: Treatment plan item doesn't exist (if provided)
     * - 400 VALIDATION_ERROR: Invalid request body
     *
     * @param recordId The clinical record ID
     * @param request Procedure details (service, plan item, description, notes)
     * @return AddProcedureResponse with created procedure details
     */
    @PostMapping("/clinical-records/{recordId}/procedures")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('WRITE_CLINICAL_RECORD')")
    @ApiMessage("Procedure added successfully")
    public ResponseEntity<AddProcedureResponse> addProcedure(
            @PathVariable Integer recordId,
            @Valid @RequestBody AddProcedureRequest request) {

        log.info("API 8.5: POST /api/v1/appointments/clinical-records/{}/procedures", recordId);

        AddProcedureResponse response = clinicalRecordService.addProcedure(recordId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
