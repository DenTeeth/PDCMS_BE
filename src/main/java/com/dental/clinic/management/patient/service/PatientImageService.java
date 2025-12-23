package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.BadRequestException;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.domain.PatientImage;
import com.dental.clinic.management.patient.dto.request.CreatePatientImageRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientImageRequest;
import com.dental.clinic.management.patient.dto.response.PatientImageListResponse;
import com.dental.clinic.management.patient.dto.response.PatientImageResponse;
import com.dental.clinic.management.patient.enums.ImageType;
import com.dental.clinic.management.patient.repository.PatientImageRepository;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.patient.specification.PatientImageSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientImageService {

    private final PatientImageRepository patientImageRepository;
    private final PatientRepository patientRepository;
    private final ClinicalRecordRepository clinicalRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PatientImageResponse createPatientImage(CreatePatientImageRequest request) {
        log.info("Creating patient image for patient ID: {}", request.getPatientId());

        Integer patientIdInt = request.getPatientId().intValue();
        Patient patient = patientRepository.findById(patientIdInt)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + request.getPatientId()));

        ClinicalRecord clinicalRecord = null;
        if (request.getClinicalRecordId() != null) {
            Integer clinicalRecordIdInt = request.getClinicalRecordId().intValue();
            clinicalRecord = clinicalRecordRepository.findById(clinicalRecordIdInt)
                    .orElseThrow(() -> new NotFoundException(
                            "Clinical record not found with ID: " + request.getClinicalRecordId()));

            // Verify clinical record belongs to the patient via appointment
            if (!clinicalRecord.getAppointment().getPatientId().equals(patientIdInt)) {
                throw new BadRequestException("Clinical record does not belong to the specified patient");
            }
        }

        Integer currentEmployeeId = getCurrentEmployeeId();
        Employee uploader = null;
        if (currentEmployeeId != null) {
            uploader = employeeRepository.findById(currentEmployeeId).orElse(null);
        }

        PatientImage patientImage = PatientImage.builder()
                .patient(patient)
                .clinicalRecord(clinicalRecord)
                .imageUrl(request.getImageUrl())
                .cloudinaryPublicId(request.getCloudinaryPublicId())
                .imageType(request.getImageType())
                .description(request.getDescription())
                .capturedDate(request.getCapturedDate())
                .uploadedBy(uploader)
                .build();

        PatientImage savedImage = patientImageRepository.save(patientImage);
        log.info("Patient image created successfully with ID: {}", savedImage.getImageId());

        return mapToResponse(savedImage);
    }

    @Transactional(readOnly = true)
    public PatientImageListResponse getPatientImages(
            Long patientId,
            ImageType imageType,
            Long clinicalRecordId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {
        log.info("Fetching patient images with filters - patientId: {}, imageType: {}, page: {}, size: {}",
                patientId, imageType, page, size);

        if (patientId == null) {
            throw new BadRequestException("Patient ID is required");
        }

        Integer patientIdInt = patientId.intValue();
        if (!patientRepository.existsById(patientIdInt)) {
            throw new NotFoundException("Patient not found with ID: " + patientId);
        }

        // Authorization check: Patients can only view their own images
        verifyPatientAccess(patientIdInt);

        Specification<PatientImage> spec = PatientImageSpecification.filterImages(
                patientId, imageType, clinicalRecordId, fromDate, toDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<PatientImage> imagePage = patientImageRepository.findAll(spec, pageable);

        Page<PatientImageResponse> responsePage = imagePage.map(this::mapToResponse);

        return PatientImageListResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public PatientImageResponse getPatientImageById(Long imageId) {
        log.info("Fetching patient image by ID: {}", imageId);

        PatientImage patientImage = patientImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Patient image not found with ID: " + imageId));

        return mapToResponse(patientImage);
    }

    @Transactional
    public PatientImageResponse updatePatientImage(Long imageId, UpdatePatientImageRequest request) {
        log.info("Updating patient image ID: {}", imageId);

        PatientImage patientImage = patientImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Patient image not found with ID: " + imageId));

        if (request.getImageType() != null) {
            patientImage.setImageType(request.getImageType());
        }

        if (request.getDescription() != null) {
            patientImage.setDescription(request.getDescription());
        }

        if (request.getCapturedDate() != null) {
            patientImage.setCapturedDate(request.getCapturedDate());
        }

        if (request.getClinicalRecordId() != null) {
            Integer clinicalRecordIdInt = request.getClinicalRecordId().intValue();
            ClinicalRecord clinicalRecord = clinicalRecordRepository.findById(clinicalRecordIdInt)
                    .orElseThrow(() -> new NotFoundException(
                            "Clinical record not found with ID: " + request.getClinicalRecordId()));

            // Verify clinical record belongs to the patient via appointment
            if (!clinicalRecord.getAppointment().getPatientId().equals(patientImage.getPatient().getPatientId())) {
                throw new BadRequestException("Clinical record does not belong to the image's patient");
            }

            patientImage.setClinicalRecord(clinicalRecord);
        }

        PatientImage updatedImage = patientImageRepository.save(patientImage);
        log.info("Patient image updated successfully: {}", imageId);

        return mapToResponse(updatedImage);
    }

    @Transactional
    public void deletePatientImage(Long imageId) {
        log.info("Deleting patient image ID: {}", imageId);

        PatientImage patientImage = patientImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Patient image not found with ID: " + imageId));

        patientImageRepository.delete(patientImage);
        log.info("Patient image deleted successfully: {}", imageId);
    }

    @Transactional(readOnly = true)
    public List<PatientImageResponse> getImagesByClinicalRecord(Long clinicalRecordId) {
        log.info("Fetching images for clinical record ID: {}", clinicalRecordId);

        Integer clinicalRecordIdInt = clinicalRecordId.intValue();
        if (!clinicalRecordRepository.existsById(clinicalRecordIdInt)) {
            throw new NotFoundException("Clinical record not found with ID: " + clinicalRecordId);
        }

        List<PatientImage> images = patientImageRepository
                .findByClinicalRecordClinicalRecordIdOrderByCreatedAtDesc(clinicalRecordIdInt);

        return images.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientImageResponse> getImagesByAppointment(Long appointmentId) {
        log.info("Fetching images for appointment ID: {}", appointmentId);

        Integer appointmentIdInt = appointmentId.intValue();

        // Find clinical record for this appointment
        ClinicalRecord clinicalRecord = clinicalRecordRepository.findByAppointment_AppointmentId(appointmentIdInt)
                .orElseThrow(
                        () -> new NotFoundException("Clinical record not found for appointment ID: " + appointmentId));

        // Get all images for this clinical record
        List<PatientImage> images = patientImageRepository
                .findByClinicalRecordClinicalRecordIdOrderByCreatedAtDesc(clinicalRecord.getClinicalRecordId());

        return images.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void verifyPatientAccess(Integer patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Check if user has staff role (can view any patient's images)
        boolean isStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") 
                        || role.equals("ROLE_DENTIST") 
                        || role.equals("ROLE_NURSE")
                        || role.equals("ROLE_RECEPTIONIST"));

        if (isStaff) {
            log.debug("Staff user authorized to view patient {} images", patientId);
            return; // Staff can view any patient's images
        }

        // For patients, verify they can only access their own images
        String username = authentication.getName();
        Patient patient = patientRepository.findByAccount_Username(username)
                .orElseThrow(() -> new AccessDeniedException("Patient record not found for current user"));

        if (!patient.getPatientId().equals(patientId)) {
            log.warn("Patient {} attempted to access images for patient {}", patient.getPatientId(), patientId);
            throw new AccessDeniedException("You can only view your own images");
        }

        log.debug("Patient {} authorized to view own images", patientId);
    }

    private Integer getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return employeeRepository.findByAccount_Username(username)
                    .map(Employee::getEmployeeId)
                    .orElse(null);
        }
        return null;
    }

    private PatientImageResponse mapToResponse(PatientImage image) {
        return PatientImageResponse.builder()
                .imageId(image.getImageId())
                .patientId(image.getPatient().getPatientId().longValue())
                .patientName(image.getPatient().getFullName())
                .clinicalRecordId(
                        image.getClinicalRecord() != null ? image.getClinicalRecord().getClinicalRecordId().longValue()
                                : null)
                .imageUrl(image.getImageUrl())
                .cloudinaryPublicId(image.getCloudinaryPublicId())
                .imageType(image.getImageType())
                .description(image.getDescription())
                .capturedDate(image.getCapturedDate())
                .uploadedBy(image.getUploadedBy() != null ? image.getUploadedBy().getEmployeeId().longValue() : null)
                .uploaderName(image.getUploadedBy() != null ? image.getUploadedBy().getFullName() : null)
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
