package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.dto.ClinicalRecordResponse;
import com.dental.clinic.management.clinical_records.service.ClinicalRecordService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
