package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.Patient;
import com.dental.clinic.management.dto.request.CreatePatientRequest;
import com.dental.clinic.management.dto.request.ReplacePatientRequest;
import com.dental.clinic.management.dto.request.UpdatePatientRequest;
import com.dental.clinic.management.dto.response.PatientInfoResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.PatientMapper;
import com.dental.clinic.management.repository.PatientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import java.util.UUID;

/**
 * Service for managing patients
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * Get all ACTIVE patients only (isActive = true) with pagination and sorting
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + VIEW_PATIENT + "')")
    public Page<PatientInfoResponse> getAllActivePatients(
            int page, int size, String sortBy, String sortDirection) {

        // Validate inputs
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Filter only active patients using Specification
        Specification<Patient> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        Page<Patient> patientPage = patientRepository.findAll(spec, pageable);

        return patientPage.map(patientMapper::toPatientInfoResponse);
    }

    /**
     * Get ALL patients including deleted ones (Admin only)
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + VIEW_PATIENT + "')")
    public Page<PatientInfoResponse> getAllPatientsIncludingDeleted(
            int page, int size, String sortBy, String sortDirection) {

        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Patient> patientPage = patientRepository.findAll(pageable);

        return patientPage.map(patientMapper::toPatientInfoResponse);
    }

    /**
     * Get active patient by patient code
     *
     * @param patientCode the patient code
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + VIEW_PATIENT + "')")
    public PatientInfoResponse getActivePatientByCode(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        if (!patient.getIsActive()) {
            throw new BadRequestAlertException(
                    "Patient is inactive",
                    "Patient",
                    "patientinactive");
        }

        return patientMapper.toPatientInfoResponse(patient);
    }

    /**
     * Get patient by code including deleted ones (Admin only)
     *
     * @param patientCode the patient code
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + VIEW_PATIENT + "')")
    public PatientInfoResponse getPatientByCodeIncludingDeleted(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        return patientMapper.toPatientInfoResponse(patient);
    }

    /**
     * Create a new patient
     *
     * @param request the patient information
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + CREATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse createPatient(CreatePatientRequest request) {
        // Convert DTO to entity
        Patient patient = patientMapper.toPatient(request);

        // Generate unique IDs
        patient.setPatientId(UUID.randomUUID().toString());
        patient.setPatientCode(generatePatientCode());
        patient.setIsActive(true);

        // Save to database
        Patient savedPatient = patientRepository.save(patient);

        return patientMapper.toPatientInfoResponse(savedPatient);
    }

    /**
     * Update patient (PATCH - partial update)
     *
     * @param patientCode the patient code
     * @param request     the update information
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + UPDATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse updatePatient(String patientCode, UpdatePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Update only non-null fields
        patientMapper.updatePatientFromRequest(request, patient);

        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toPatientInfoResponse(updatedPatient);
    }

    /**
     * Replace patient (PUT - full replacement)
     *
     * @param patientCode the patient code
     * @param request     the replacement information
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasAuthority('" + UPDATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse replacePatient(String patientCode, ReplacePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Replace all fields
        patientMapper.replacePatientFromRequest(request, patient);

        Patient replacedPatient = patientRepository.save(patient);

        return patientMapper.toPatientInfoResponse(replacedPatient);
    }

    /**
     * Soft delete patient
     *
     * @param patientCode the patient code
     */
    @PreAuthorize("hasAuthority('" + DELETE_PATIENT + "')")
    @Transactional
    public void deletePatient(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Soft delete
        patient.setIsActive(false);
        patientRepository.save(patient);
    }

    /**
     * Generate unique patient code (PAT001, PAT002, ...)
     * Synchronized to prevent duplicate codes in concurrent requests
     */
    private synchronized String generatePatientCode() {
        long count = patientRepository.count();
        String code;
        do {
            count++;
            code = String.format("PAT%03d", count);
        } while (patientRepository.findOneByPatientCode(code).isPresent());

        return code;
    }
}
