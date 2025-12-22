package com.dental.clinic.management.patient.controller;

import com.dental.clinic.management.patient.dto.request.CreatePatientImageRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientImageRequest;
import com.dental.clinic.management.patient.dto.response.PatientImageListResponse;
import com.dental.clinic.management.patient.dto.response.PatientImageResponse;
import com.dental.clinic.management.patient.enums.ImageType;
import com.dental.clinic.management.patient.service.PatientImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patient-images")
@RequiredArgsConstructor
@Slf4j
public class PatientImageController {

    private final PatientImageService patientImageService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_PATIENT_IMAGES')")
    public ResponseEntity<PatientImageResponse> createPatientImage(
            @Valid @RequestBody CreatePatientImageRequest request) {
        log.info("REST request to create patient image for patient ID: {}", request.getPatientId());
        PatientImageResponse response = patientImageService.createPatientImage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT_IMAGES')")
    public ResponseEntity<PatientImageListResponse> getPatientImages(
            @PathVariable Long patientId,
            @RequestParam(required = false) ImageType imageType,
            @RequestParam(required = false) Long clinicalRecordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get patient images for patient ID: {}", patientId);
        PatientImageListResponse response = patientImageService.getPatientImages(
                patientId, imageType, clinicalRecordId, fromDate, toDate, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{imageId}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT_IMAGES')")
    public ResponseEntity<PatientImageResponse> getPatientImageById(@PathVariable Long imageId) {
        log.info("REST request to get patient image by ID: {}", imageId);
        PatientImageResponse response = patientImageService.getPatientImageById(imageId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{imageId}")
    @PreAuthorize("hasAuthority('MANAGE_PATIENT_IMAGES')")
    public ResponseEntity<PatientImageResponse> updatePatientImage(
            @PathVariable Long imageId,
            @Valid @RequestBody UpdatePatientImageRequest request) {
        log.info("REST request to update patient image ID: {}", imageId);
        PatientImageResponse response = patientImageService.updatePatientImage(imageId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('MANAGE_PATIENT_IMAGES')")
    public ResponseEntity<Void> deletePatientImage(@PathVariable Long imageId) {
        log.info("REST request to delete patient image ID: {}", imageId);
        patientImageService.deletePatientImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clinical-record/{clinicalRecordId}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT_IMAGES')")
    public ResponseEntity<List<PatientImageResponse>> getImagesByClinicalRecord(
            @PathVariable Long clinicalRecordId) {
        log.info("REST request to get images for clinical record ID: {}", clinicalRecordId);
        List<PatientImageResponse> response = patientImageService.getImagesByClinicalRecord(clinicalRecordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT_IMAGES')")
    public ResponseEntity<List<PatientImageResponse>> getImagesByAppointment(
            @PathVariable Long appointmentId) {
        log.info("REST request to get images for appointment ID: {}", appointmentId);
        List<PatientImageResponse> response = patientImageService.getImagesByAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }
}
