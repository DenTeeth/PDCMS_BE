package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.Room;
import com.dental.clinic.management.booking_appointment.repository.AppointmentParticipantRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.RoomRepository;
import com.dental.clinic.management.clinical_records.domain.ClinicalPrescription;
import com.dental.clinic.management.clinical_records.domain.ClinicalPrescriptionItem;
import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import com.dental.clinic.management.clinical_records.domain.ClinicalRecordProcedure;
import com.dental.clinic.management.clinical_records.domain.PatientToothStatus;
import com.dental.clinic.management.clinical_records.dto.*;
import com.dental.clinic.management.clinical_records.repository.ClinicalPrescriptionRepository;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordProcedureRepository;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordRepository;
import com.dental.clinic.management.clinical_records.repository.PatientToothStatusRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.BadRequestException;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.repository.ItemMasterRepository;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.service.domain.DentalService;
import com.dental.clinic.management.service.repository.DentalServiceRepository;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalRecordService {

        private final ClinicalRecordRepository clinicalRecordRepository;
        private final ClinicalRecordProcedureRepository procedureRepository;
        private final ClinicalPrescriptionRepository prescriptionRepository;
        private final AppointmentRepository appointmentRepository;
        private final AppointmentParticipantRepository appointmentParticipantRepository;
        private final EmployeeRepository employeeRepository;
        private final PatientRepository patientRepository;
        private final RoomRepository roomRepository;
        private final DentalServiceRepository dentalServiceRepository;
        private final PatientPlanItemRepository planItemRepository;
        private final ItemMasterRepository itemMasterRepository;
        private final PatientToothStatusRepository toothStatusRepository;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        /**
         * Get clinical record for an appointment
         *
         * Authorization Logic (reuse from Appointment module):
         * 1. Admin (ROLE_ADMIN): Can access all records
         * 2. VIEW_APPOINTMENT_ALL: Can access all records (Receptionist, Manager)
         * 3. VIEW_APPOINTMENT_OWN: Can only access if:
         * - Doctor: appointment.employee_id matches user's employee_id
         * - Patient: appointment.patient_id matches user's patient_id
         * - Observer/Nurse: appointment participant matches user's employee_id
         *
         * Returns 404 RECORD_NOT_FOUND if clinical record doesn't exist
         * Frontend uses this to show CREATE form
         */
        @Transactional(readOnly = true)
        public ClinicalRecordResponse getClinicalRecord(Integer appointmentId) {
                log.info("Fetching clinical record for appointment ID: {}", appointmentId);

                // Step 1: Load appointment (throws 404 if not found)
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new NotFoundException("APPOINTMENT_NOT_FOUND",
                                                "Appointment not found with ID: " + appointmentId));

                // Step 2: Check RBAC authorization
                checkAccessPermission(appointment);

                // Step 3: Load clinical record (throws 404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findByAppointment_AppointmentId(appointmentId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found for appointment ID: " + appointmentId));

                // Step 4: Build response with nested data
                return buildClinicalRecordResponse(record, appointment);
        }

        /**
         * Check if current user can access this appointment's clinical record
         * Reuses Appointment module RBAC logic
         */
        private void checkAccessPermission(Appointment appointment) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();

                // Check if user has ROLE_ADMIN
                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                        log.info("Admin user {} can access all clinical records", username);
                        return;
                }

                // Check for VIEW_APPOINTMENT_ALL permission
                boolean canViewAll = auth.getAuthorities().stream()
                                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("VIEW_APPOINTMENT_ALL"));

                if (canViewAll) {
                        log.info("User {} has VIEW_APPOINTMENT_ALL, can access all clinical records", username);
                        return;
                }

                // Check for VIEW_APPOINTMENT_OWN permission (must validate ownership)
                boolean hasViewOwnPermission = auth.getAuthorities().stream()
                                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("VIEW_APPOINTMENT_OWN"));

                if (!hasViewOwnPermission) {
                        log.warn("User {} has no appointment view permissions", username);
                        throw new AccessDeniedException("You do not have permission to view clinical records");
                }

                // Validate ownership for VIEW_APPOINTMENT_OWN
                // Try employee first (Doctor, Nurse, Observer)
                var employeeOpt = employeeRepository.findByAccount_Username(username);
                if (employeeOpt.isPresent()) {
                        Integer employeeId = employeeOpt.get().getEmployeeId();

                        // Check if user is the primary doctor
                        if (appointment.getEmployeeId().equals(employeeId)) {
                                log.info("Employee {} is the primary doctor for appointment {}", employeeId,
                                                appointment.getAppointmentId());
                                return;
                        }

                        // Check if user is a participant (observer/nurse)
                        boolean isParticipant = appointmentParticipantRepository
                                        .findByIdAppointmentId(appointment.getAppointmentId())
                                        .stream()
                                        .anyMatch(ap -> ap.getId().getEmployeeId().equals(employeeId));

                        if (isParticipant) {
                                log.info("Employee {} is a participant for appointment {}", employeeId,
                                                appointment.getAppointmentId());
                                return;
                        }

                        // Not primary doctor nor participant
                        log.warn("Employee {} is neither primary doctor nor participant for appointment {}", employeeId,
                                        appointment.getAppointmentId());
                        throw new AccessDeniedException(
                                        "You can only view clinical records for appointments where you are the primary doctor or a participant");
                }

                // Try patient (check if appointment belongs to this patient)
                var patientOpt = patientRepository.findByAccount_Username(username);
                if (patientOpt.isPresent()) {
                        Integer patientId = patientOpt.get().getPatientId();

                        if (appointment.getPatientId().equals(patientId)) {
                                log.info("Patient {} is viewing their own clinical record for appointment {}",
                                                patientId,
                                                appointment.getAppointmentId());
                                return;
                        }

                        log.warn("Patient {} attempted to access clinical record for different patient's appointment {}",
                                        patientId, appointment.getAppointmentId());
                        throw new AccessDeniedException("You can only view your own clinical records");
                }

                // User not found as employee or patient
                log.warn("User {} not found as employee or patient", username);
                throw new AccessDeniedException("Access Denied");
        }

        /**
         * Build nested ClinicalRecordResponse from entities
         */
        private ClinicalRecordResponse buildClinicalRecordResponse(ClinicalRecord record, Appointment appointment) {
                // Load employee (doctor)
                Employee doctor = employeeRepository.findById(appointment.getEmployeeId())
                                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND",
                                                "Employee not found with ID: " + appointment.getEmployeeId()));

                // Load patient
                Patient patient = patientRepository.findById(appointment.getPatientId())
                                .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND",
                                                "Patient not found with ID: " + appointment.getPatientId()));

                // Load room
                Room room = roomRepository.findById(appointment.getRoomId())
                                .orElseThrow(() -> new NotFoundException("ROOM_NOT_FOUND",
                                                "Room not found with ID: " + appointment.getRoomId()));

                // Build AppointmentDTO
                AppointmentDTO appointmentDTO = AppointmentDTO.builder()
                                .appointmentId(appointment.getAppointmentId())
                                .appointmentCode(appointment.getAppointmentCode())
                                .roomId(room.getRoomId())
                                .appointmentStartTime(appointment.getAppointmentStartTime().format(FORMATTER))
                                .appointmentEndTime(appointment.getAppointmentEndTime().format(FORMATTER))
                                .expectedDurationMinutes(appointment.getExpectedDurationMinutes())
                                .status(appointment.getStatus().name())
                                .notes(appointment.getNotes())
                                .build();

                // Build DoctorDTO
                DoctorDTO doctorDTO = DoctorDTO.builder()
                                .employeeId(doctor.getEmployeeId())
                                .employeeCode(doctor.getEmployeeCode())
                                .fullName(doctor.getFullName())
                                .phone(doctor.getPhone())
                                .email(null) // Email stored in Account, not Employee
                                .build();

                // Build PatientDTO
                PatientDTO patientDTO = PatientDTO.builder()
                                .patientId(patient.getPatientId())
                                .patientCode(patient.getPatientCode())
                                .fullName(patient.getFullName())
                                .phone(patient.getPhone())
                                .email(patient.getEmail())
                                .dateOfBirth(patient.getDateOfBirth() != null
                                                ? patient.getDateOfBirth().format(DATE_FORMATTER)
                                                : null)
                                .gender(patient.getGender() != null ? patient.getGender().name() : null)
                                .build();

                // Build ProcedureDTOs
                var procedures = record.getProcedures().stream()
                                .map(proc -> ProcedureDTO.builder()
                                                .procedureId(proc.getProcedureId())
                                                .serviceCode(proc.getService() != null
                                                                ? proc.getService().getServiceCode()
                                                                : null)
                                                .serviceName(proc.getService() != null
                                                                ? proc.getService().getServiceName()
                                                                : null)
                                                .patientPlanItemId(
                                                                proc.getPatientPlanItem() != null
                                                                                ? proc.getPatientPlanItem().getItemId()
                                                                                                .intValue()
                                                                                : null)
                                                .toothNumber(proc.getToothNumber())
                                                .procedureDescription(proc.getProcedureDescription())
                                                .notes(proc.getNotes())
                                                .createdAt(proc.getCreatedAt().format(FORMATTER))
                                                .build())
                                .collect(Collectors.toList());

                // Build PrescriptionDTOs
                var prescriptions = record.getPrescriptions().stream()
                                .map(presc -> {
                                        var items = presc.getItems().stream()
                                                        .map(item -> {
                                                                PrescriptionItemDTO.PrescriptionItemDTOBuilder builder = PrescriptionItemDTO
                                                                                .builder()
                                                                                .prescriptionItemId(item
                                                                                                .getPrescriptionItemId())
                                                                                .itemName(item.getItemName())
                                                                                .quantity(item.getQuantity())
                                                                                .dosageInstructions(item
                                                                                                .getDosageInstructions());

                                                                if (item.getItemMaster() != null) {
                                                                        builder.itemMasterId(item.getItemMaster()
                                                                                        .getItemMasterId().intValue())
                                                                                        .itemCode(item.getItemMaster()
                                                                                                        .getItemCode())
                                                                                        .unitName(item.getItemMaster()
                                                                                                        .getUnitOfMeasure());
                                                                }

                                                                return builder.build();
                                                        })
                                                        .collect(Collectors.toList());

                                        return PrescriptionDTO.builder()
                                                        .prescriptionId(presc.getPrescriptionId())
                                                        .clinicalRecordId(
                                                                        presc.getClinicalRecord().getClinicalRecordId())
                                                        .prescriptionNotes(presc.getPrescriptionNotes())
                                                        .createdAt(presc.getCreatedAt() != null
                                                                        ? presc.getCreatedAt().format(FORMATTER)
                                                                        : null)
                                                        .items(items)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Build final response
                return ClinicalRecordResponse.builder()
                                .clinicalRecordId(record.getClinicalRecordId())
                                .diagnosis(record.getDiagnosis())
                                .vitalSigns(record.getVitalSigns())
                                .chiefComplaint(record.getChiefComplaint())
                                .examinationFindings(record.getExaminationFindings())
                                .treatmentNotes(record.getTreatmentNotes())
                                .followUpDate(record.getFollowUpDate() != null
                                                ? record.getFollowUpDate().format(DATE_FORMATTER)
                                                : null)
                                .createdAt(record.getCreatedAt().format(FORMATTER))
                                .updatedAt(record.getUpdatedAt().format(FORMATTER))
                                .appointment(appointmentDTO)
                                .doctor(doctorDTO)
                                .patient(patientDTO)
                                .procedures(procedures)
                                .prescriptions(prescriptions)
                                .build();
        }

        /**
         * Create new clinical record for an appointment
         *
         * Authorization: Requires WRITE_CLINICAL_RECORD permission
         *
         * Business Rules:
         * 1. Appointment must exist
         * 2. Appointment must be IN_PROGRESS (checked in)
         * 3. No existing clinical record for this appointment (409 if duplicate)
         * 4. All required fields must be provided
         */
        @Transactional
        public CreateClinicalRecordResponse createClinicalRecord(CreateClinicalRecordRequest request) {
                log.info("Creating clinical record for appointment ID: {}", request.getAppointmentId());

                // Step 1: Validate appointment exists
                Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                                .orElseThrow(() -> new NotFoundException("APPOINTMENT_NOT_FOUND",
                                                "Appointment not found with ID: " + request.getAppointmentId()));

                // Step 2: Check appointment status
                if (!appointment.getStatus().name().equals("IN_PROGRESS")
                                && !appointment.getStatus().name().equals("CHECKED_IN")) {
                        throw new com.dental.clinic.management.exception.BadRequestException("INVALID_STATUS",
                                        "Cannot create clinical record. Appointment must be IN_PROGRESS or CHECKED_IN. Current status: "
                                                        + appointment.getStatus());
                }

                // Step 3: Check duplicate
                if (clinicalRecordRepository.findByAppointment_AppointmentId(request.getAppointmentId())
                                .isPresent()) {
                        throw new com.dental.clinic.management.exception.ConflictException("RECORD_ALREADY_EXISTS",
                                        "Clinical record already exists for appointment ID "
                                                        + request.getAppointmentId()
                                                        + ". Please use PUT to update.");
                }

                // Step 4: Create clinical record
                ClinicalRecord record = ClinicalRecord.builder()
                                .appointment(appointment)
                                .chiefComplaint(request.getChiefComplaint())
                                .examinationFindings(request.getExaminationFindings())
                                .diagnosis(request.getDiagnosis())
                                .treatmentNotes(request.getTreatmentNotes())
                                .vitalSigns(request.getVitalSigns())
                                .followUpDate(request.getFollowUpDate())
                                .build();

                ClinicalRecord saved = clinicalRecordRepository.save(record);

                log.info("Created clinical record ID: {} for appointment ID: {}", saved.getClinicalRecordId(),
                                request.getAppointmentId());

                return CreateClinicalRecordResponse.builder()
                                .clinicalRecordId(saved.getClinicalRecordId())
                                .appointmentId(request.getAppointmentId())
                                .createdAt(saved.getCreatedAt().format(FORMATTER))
                                .build();
        }

        /**
         * Update existing clinical record
         *
         * Authorization: Requires WRITE_CLINICAL_RECORD permission
         *
         * Business Rules:
         * 1. Record must exist
         * 2. Only update provided fields (partial update)
         * 3. Cannot update appointmentId, chiefComplaint, diagnosis (use separate API)
         * 4. Auto update updated_at timestamp
         */
        @Transactional
        public UpdateClinicalRecordResponse updateClinicalRecord(Integer recordId,
                        UpdateClinicalRecordRequest request) {
                log.info("Updating clinical record ID: {}", recordId);

                // Step 1: Load existing record
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record ID " + recordId + " does not exist"));

                // Step 2: Update only provided fields
                if (request.getExaminationFindings() != null) {
                        record.setExaminationFindings(request.getExaminationFindings());
                }

                if (request.getTreatmentNotes() != null) {
                        record.setTreatmentNotes(request.getTreatmentNotes());
                }

                if (request.getVitalSigns() != null) {
                        record.setVitalSigns(request.getVitalSigns());
                }

                if (request.getFollowUpDate() != null) {
                        record.setFollowUpDate(request.getFollowUpDate());
                }

                // Step 3: Save changes (updated_at auto-updated by @PreUpdate)
                ClinicalRecord updated = clinicalRecordRepository.save(record);

                log.info("Updated clinical record ID: {}", recordId);

                return UpdateClinicalRecordResponse.builder()
                                .clinicalRecordId(updated.getClinicalRecordId())
                                .updatedAt(updated.getUpdatedAt().format(FORMATTER))
                                .examinationFindings(updated.getExaminationFindings())
                                .treatmentNotes(updated.getTreatmentNotes())
                                .followUpDate(updated.getFollowUpDate() != null
                                                ? updated.getFollowUpDate().format(DATE_FORMATTER)
                                                : null)
                                .build();
        }

        /**
         * API 8.4: Get Procedures for Clinical Record
         *
         * Authorization Logic (SAME as API 8.1):
         * 1. Admin (ROLE_ADMIN): Can access all records
         * 2. VIEW_APPOINTMENT_ALL: Can access all records (Receptionist, Manager)
         * 3. VIEW_APPOINTMENT_OWN: Can only access if:
         * - Doctor: appointment.employee_id matches user's employee_id
         * - Patient: appointment.patient_id matches user's patient_id
         * - Observer/Nurse: appointment participant matches user's employee_id
         *
         * Returns empty list if no procedures added yet (200 OK with data: [])
         */
        @Transactional(readOnly = true)
        public java.util.List<ProcedureResponse> getProcedures(Integer recordId) {
                log.info("Fetching procedures for clinical record ID: {}", recordId);

                // Step 1: Load clinical record (throws 404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found with ID: " + recordId));

                // Step 2: Load appointment for RBAC check
                Appointment appointment = record.getAppointment();
                if (appointment == null) {
                        throw new NotFoundException("APPOINTMENT_NOT_FOUND",
                                        "Appointment not found for clinical record ID: " + recordId);
                }

                // Step 3: Check RBAC authorization (reuse from API 8.1)
                checkAccessPermission(appointment);

                // Step 4: Load procedures with service info (LEFT JOIN FETCH)
                java.util.List<ClinicalRecordProcedure> procedures = procedureRepository
                                .findByClinicalRecordIdWithService(recordId);

                log.info("Found {} procedures for clinical record ID: {}", procedures.size(), recordId);

                // Step 5: Map to DTOs
                return procedures.stream()
                                .map(this::mapToProcedureResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Map ClinicalRecordProcedure entity to ProcedureResponse DTO
         */
        private ProcedureResponse mapToProcedureResponse(ClinicalRecordProcedure procedure) {
                ProcedureResponse.ProcedureResponseBuilder builder = ProcedureResponse.builder()
                                .procedureId(procedure.getProcedureId())
                                .clinicalRecordId(procedure.getClinicalRecord().getClinicalRecordId())
                                .procedureDescription(procedure.getProcedureDescription())
                                .toothNumber(procedure.getToothNumber())
                                .notes(procedure.getNotes())
                                .createdAt(procedure.getCreatedAt());

                // Add service info if exists
                if (procedure.getService() != null) {
                        builder.serviceId(procedure.getService().getServiceId())
                                        .serviceName(procedure.getService().getServiceName())
                                        .serviceCode(procedure.getService().getServiceCode());
                }

                // Add plan item ID if exists
                if (procedure.getPatientPlanItem() != null) {
                        builder.patientPlanItemId(procedure.getPatientPlanItem().getItemId());
                }

                return builder.build();
        }

        /**
         * API 8.5: Add procedure to clinical record
         *
         * Purpose: Record a procedure/service performed during appointment
         *
         * Authorization: WRITE_CLINICAL_RECORD (Doctor, Assistant, Admin)
         *
         * Business Logic:
         * 1. Validate clinical record exists
         * 2. Validate service exists and is active
         * 3. Validate treatment plan item exists (if provided)
         * 4. Create procedure record with passive plan link
         * 5. Return procedure details with service info
         *
         * Note: Does NOT update treatment plan item status
         * Status updates handled by:
         * - Appointment completion (AppointmentStatusService)
         * - Manual update (API 5.6 UpdateItemStatus)
         */
        @Transactional
        public AddProcedureResponse addProcedure(Integer recordId, AddProcedureRequest request) {
                log.info("Adding procedure to clinical record ID: {}", recordId);

                // Step 1: Load clinical record (throws 404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found with ID: " + recordId));

                // Step 2: Validate service exists and is active
                DentalService service = dentalServiceRepository.findById(request.getServiceId())
                                .orElseThrow(() -> new NotFoundException("SERVICE_NOT_FOUND",
                                                "Service not found with ID: " + request.getServiceId()));

                if (!service.getIsActive()) {
                        throw new NotFoundException("SERVICE_NOT_FOUND",
                                        "Service ID " + request.getServiceId() + " is inactive");
                }

                log.info("Service validated: {} ({})", service.getServiceName(), service.getServiceCode());

                // Step 3: Validate treatment plan item exists (if provided)
                PatientPlanItem planItem = null;
                if (request.getPatientPlanItemId() != null) {
                        planItem = planItemRepository.findById(request.getPatientPlanItemId())
                                        .orElseThrow(() -> new NotFoundException("PLAN_ITEM_NOT_FOUND",
                                                        "Treatment plan item not found with ID: "
                                                                        + request.getPatientPlanItemId()));
                        log.info("Treatment plan item linked: ID {}", request.getPatientPlanItemId());
                }

                // Step 4: Create procedure entity
                ClinicalRecordProcedure procedure = ClinicalRecordProcedure.builder()
                                .clinicalRecord(record)
                                .service(service)
                                .patientPlanItem(planItem)
                                .toothNumber(request.getToothNumber())
                                .procedureDescription(request.getProcedureDescription())
                                .notes(request.getNotes())
                                .build();

                // Step 5: Save procedure
                ClinicalRecordProcedure savedProcedure = procedureRepository.save(procedure);
                log.info("Procedure saved with ID: {}", savedProcedure.getProcedureId());

                // Step 6: Build response
                return AddProcedureResponse.builder()
                                .procedureId(savedProcedure.getProcedureId())
                                .clinicalRecordId(record.getClinicalRecordId())
                                .serviceId(service.getServiceId())
                                .serviceName(service.getServiceName())
                                .serviceCode(service.getServiceCode())
                                .patientPlanItemId(request.getPatientPlanItemId())
                                .toothNumber(savedProcedure.getToothNumber())
                                .procedureDescription(savedProcedure.getProcedureDescription())
                                .notes(savedProcedure.getNotes())
                                .createdAt(savedProcedure.getCreatedAt())
                                .build();
        }

        /**
         * Update an existing procedure in a clinical record (API 8.6)
         *
         * <p>
         * Business Rules:
         * </p>
         * <ul>
         * <li>Validates clinical record exists</li>
         * <li>Validates procedure belongs to the specified record</li>
         * <li>Validates new service exists and is active</li>
         * <li>Validates plan item exists if provided</li>
         * <li>Updates all fields except createdAt (audit trail)</li>
         * <li>Does NOT update procedure status (separation of concerns)</li>
         * </ul>
         *
         * @param recordId    Clinical record ID
         * @param procedureId Procedure ID to update
         * @param request     Update request with new values
         * @return Updated procedure details with service info
         * @throws NotFoundException if record, procedure, service, or plan item not
         *                           found
         */
        @Transactional
        public UpdateProcedureResponse updateProcedure(Integer recordId, Integer procedureId,
                        UpdateProcedureRequest request) {
                log.info("Updating procedure ID {} in clinical record ID {}", procedureId, recordId);

                // Step 1: Validate clinical record exists
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> {
                                        log.error("Clinical record not found with ID: {}", recordId);
                                        return new NotFoundException("RECORD_NOT_FOUND");
                                });
                log.debug("Clinical record found: ID {}", record.getClinicalRecordId());

                // Step 2: Validate procedure exists and belongs to this record
                ClinicalRecordProcedure procedure = procedureRepository.findById(procedureId)
                                .orElseThrow(() -> {
                                        log.error("Procedure not found with ID: {}", procedureId);
                                        return new NotFoundException("PROCEDURE_NOT_FOUND");
                                });

                if (!procedure.getClinicalRecord().getClinicalRecordId().equals(recordId)) {
                        log.error("Procedure ID {} does not belong to clinical record ID {}",
                                        procedureId, recordId);
                        throw new NotFoundException("PROCEDURE_NOT_FOUND",
                                        "Procedure does not belong to this clinical record");
                }
                log.debug("Procedure found and belongs to record: ID {}", procedure.getProcedureId());

                // Step 3: Validate new service exists and is active
                DentalService service = dentalServiceRepository.findById(request.getServiceId())
                                .orElseThrow(() -> {
                                        log.error("Service not found with ID: {}", request.getServiceId());
                                        return new NotFoundException("SERVICE_NOT_FOUND");
                                });

                if (!service.getIsActive()) {
                        log.error("Service ID {} is inactive", request.getServiceId());
                        throw new NotFoundException("SERVICE_NOT_FOUND",
                                        "Service ID " + request.getServiceId() + " is inactive");
                }
                log.debug("Service found and active: ID {}, Name: {}", service.getServiceId(),
                                service.getServiceName());

                // Step 4: Validate plan item exists if provided (optional)
                PatientPlanItem planItem = null;
                if (request.getPatientPlanItemId() != null) {
                        planItem = planItemRepository.findById(request.getPatientPlanItemId())
                                        .orElseThrow(() -> {
                                                log.error("Treatment plan item not found with ID: {}",
                                                                request.getPatientPlanItemId());
                                                return new NotFoundException("PLAN_ITEM_NOT_FOUND");
                                        });
                        log.debug("Treatment plan item found: ID {}", planItem.getItemId());
                }

                // Step 5: Update procedure fields
                procedure.setService(service);
                procedure.setPatientPlanItem(planItem);
                procedure.setToothNumber(request.getToothNumber());
                procedure.setProcedureDescription(request.getProcedureDescription());
                procedure.setNotes(request.getNotes());
                // createdAt is NOT updated (audit trail)
                // updatedAt will be set automatically by @PreUpdate

                ClinicalRecordProcedure updatedProcedure = procedureRepository.save(procedure);
                log.info("Procedure updated successfully: ID {}", updatedProcedure.getProcedureId());

                // Step 6: Build response with service info
                return UpdateProcedureResponse.builder()
                                .procedureId(updatedProcedure.getProcedureId())
                                .clinicalRecordId(record.getClinicalRecordId())
                                .serviceId(service.getServiceId())
                                .serviceName(service.getServiceName())
                                .serviceCode(service.getServiceCode())
                                .patientPlanItemId(request.getPatientPlanItemId())
                                .toothNumber(updatedProcedure.getToothNumber())
                                .procedureDescription(updatedProcedure.getProcedureDescription())
                                .notes(updatedProcedure.getNotes())
                                .createdAt(updatedProcedure.getCreatedAt())
                                .updatedAt(updatedProcedure.getUpdatedAt())
                                .build();
        }

        /**
         * Delete a procedure from a clinical record (API 8.7)
         *
         * <p>
         * Business Rules:
         * </p>
         * <ul>
         * <li>Validates clinical record exists</li>
         * <li>Validates procedure belongs to the specified record</li>
         * <li>Soft delete or hard delete based on business requirements</li>
         * <li>Does NOT cascade to treatment plan (passive link only)</li>
         * </ul>
         *
         * @param recordId    Clinical record ID
         * @param procedureId Procedure ID to delete
         * @throws NotFoundException if record or procedure not found
         */
        @Transactional
        public void deleteProcedure(Integer recordId, Integer procedureId) {
                log.info("Deleting procedure ID {} from clinical record ID {}", procedureId, recordId);

                // Step 1: Validate clinical record exists
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> {
                                        log.error("Clinical record not found with ID: {}", recordId);
                                        return new NotFoundException("RECORD_NOT_FOUND");
                                });
                log.debug("Clinical record found: ID {}", record.getClinicalRecordId());

                // Step 2: Validate procedure exists and belongs to this record
                ClinicalRecordProcedure procedure = procedureRepository.findById(procedureId)
                                .orElseThrow(() -> {
                                        log.error("Procedure not found with ID: {}", procedureId);
                                        return new NotFoundException("PROCEDURE_NOT_FOUND");
                                });

                if (!procedure.getClinicalRecord().getClinicalRecordId().equals(recordId)) {
                        log.error("Procedure ID {} does not belong to clinical record ID {}",
                                        procedureId, recordId);
                        throw new NotFoundException("PROCEDURE_NOT_FOUND",
                                        "Procedure does not belong to this clinical record");
                }
                log.debug("Procedure found and belongs to record: ID {}", procedure.getProcedureId());

                // Step 3: Delete procedure (hard delete - no cascade to treatment plan)
                procedureRepository.delete(procedure);
                log.info("Procedure deleted successfully: ID {}", procedureId);
        }

        /**
         * Get prescription for a clinical record
         *
         * Authorization Logic (reuse from getClinicalRecord):
         * 1. Admin (ROLE_ADMIN): Can access all prescriptions
         * 2. VIEW_APPOINTMENT_ALL: Can access all prescriptions
         * 3. VIEW_APPOINTMENT_OWN: Can only access if user has permission to view the
         * appointment
         *
         * Returns 404 RECORD_NOT_FOUND if clinical record doesn't exist
         * Returns 404 PRESCRIPTION_NOT_FOUND if prescription hasn't been created yet
         */
        @Transactional(readOnly = true)
        public PrescriptionDTO getPrescription(Integer recordId) {
                log.info("Fetching prescription for clinical record ID: {}", recordId);

                // Step 1: Load clinical record (throws 404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found with ID: " + recordId));

                // Step 2: Load appointment and check RBAC authorization
                Appointment appointment = record.getAppointment();
                checkAccessPermission(appointment);

                // Step 3: Load prescription (throws 404 if not found)
                ClinicalPrescription prescription = prescriptionRepository
                                .findByClinicalRecord_ClinicalRecordId(recordId)
                                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND",
                                                "No prescription found for clinical record ID: " + recordId));

                // Step 4: Map to DTO
                return mapPrescriptionToDTO(prescription);
        }

        private PrescriptionDTO mapPrescriptionToDTO(ClinicalPrescription prescription) {
                return PrescriptionDTO.builder()
                                .prescriptionId(prescription.getPrescriptionId())
                                .clinicalRecordId(prescription.getClinicalRecord().getClinicalRecordId())
                                .prescriptionNotes(prescription.getPrescriptionNotes())
                                .createdAt(prescription.getCreatedAt() != null
                                                ? prescription.getCreatedAt().format(FORMATTER)
                                                : null)
                                .items(prescription.getItems().stream()
                                                .map(this::mapPrescriptionItemToDTO)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private PrescriptionItemDTO mapPrescriptionItemToDTO(ClinicalPrescriptionItem item) {
                PrescriptionItemDTO.PrescriptionItemDTOBuilder builder = PrescriptionItemDTO.builder()
                                .prescriptionItemId(item.getPrescriptionItemId())
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .dosageInstructions(item.getDosageInstructions());

                // Add warehouse data if item is linked to inventory
                if (item.getItemMaster() != null) {
                        builder.itemMasterId(item.getItemMaster().getItemMasterId().intValue())
                                        .itemCode(item.getItemMaster().getItemCode())
                                        .unitName(item.getItemMaster().getUnitOfMeasure());
                }

                return builder.build();
        }

        /**
         * API 8.15: Save Prescription (Create/Update with Replace Strategy)
         *
         * Business Logic:
         * 1. Validate clinical record exists
         * 2. Check RBAC (reuse checkAccessPermission from API 8.1)
         * 3. If prescription exists: Update notes, HARD DELETE all old items
         * 4. If prescription doesn't exist: Create new prescription header
         * 5. Validate each item: itemName required, itemMasterId must be valid if
         * provided
         * 6. Insert all new items from request
         * 7. Return full prescription DTO (reuse mapper from API 8.14)
         *
         * Authorization: WRITE_CLINICAL_RECORD (Doctor, Assistant, Admin)
         *
         * @param recordId Clinical record ID
         * @param request  SavePrescriptionRequest with prescriptionNotes and items
         * @return PrescriptionDTO with all items
         * @throws NotFoundException     if clinical record not found
         * @throws AccessDeniedException if user lacks permission to modify this record
         * @throws BadRequestException   if itemMasterId invalid or itemName missing
         */
        @Transactional
        public PrescriptionDTO savePrescription(Integer recordId, SavePrescriptionRequest request) {
                log.info("Saving prescription for clinical record ID: {}", recordId);

                // Step 1: Load clinical record (404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found with ID: " + recordId));

                // Step 2: Check RBAC authorization (reuse from API 8.1)
                Appointment appointment = record.getAppointment();
                checkAccessPermission(appointment);

                // Step 3: Load or create prescription header
                ClinicalPrescription prescription = prescriptionRepository
                                .findByClinicalRecord_ClinicalRecordId(recordId)
                                .orElse(null);

                if (prescription != null) {
                        // UPDATE case: Clear old items and update notes
                        log.info("Updating existing prescription ID: {}", prescription.getPrescriptionId());
                        prescription.getItems().clear(); // CASCADE DELETE will remove from DB
                        prescription.setPrescriptionNotes(request.getPrescriptionNotes());
                } else {
                        // CREATE case: New prescription
                        log.info("Creating new prescription for clinical record ID: {}", recordId);
                        prescription = ClinicalPrescription.builder()
                                        .clinicalRecord(record)
                                        .prescriptionNotes(request.getPrescriptionNotes())
                                        .build();
                }

                // Step 4: Validate and add new items
                for (PrescriptionItemRequest itemReq : request.getItems()) {
                        // Validate itemMasterId if provided
                        ItemMaster itemMaster = null;
                        if (itemReq.getItemMasterId() != null) {
                                Long itemMasterId = itemReq.getItemMasterId().longValue();
                                itemMaster = itemMasterRepository.findById(itemMasterId)
                                                .orElseThrow(() -> new NotFoundException("ITEM_NOT_FOUND",
                                                                "Item Master ID " + itemReq.getItemMasterId()
                                                                                + " does not exist"));

                                if (!itemMaster.getIsActive()) {
                                        throw new BadRequestException("ITEM_NOT_ACTIVE",
                                                        "Item Master ID " + itemReq.getItemMasterId()
                                                                        + " is not active");
                                }
                        }

                        // Build prescription item
                        ClinicalPrescriptionItem item = ClinicalPrescriptionItem.builder()
                                        .prescription(prescription)
                                        .itemMaster(itemMaster)
                                        .itemName(itemReq.getItemName())
                                        .quantity(itemReq.getQuantity())
                                        .dosageInstructions(itemReq.getDosageInstructions())
                                        .build();

                        prescription.getItems().add(item);
                }

                // Step 5: Save (cascades to items)
                ClinicalPrescription saved = prescriptionRepository.save(prescription);

                log.info("Prescription saved successfully with {} items", saved.getItems().size());

                // Step 6: Map to DTO and return (reuse mapper from API 8.14)
                return mapPrescriptionToDTO(saved);
        }

        /**
         * API 8.16: Delete Prescription
         * DELETE /api/v1/appointments/clinical-records/{recordId}/prescription
         *
         * Deletes the entire prescription and all prescription items for a clinical
         * record.
         * If inventory integration exists in the future, this should restore inventory
         * levels.
         *
         * Authorization:
         * - ROLE_ADMIN: Full access
         * - WRITE_CLINICAL_RECORD permission: Doctors who created the record
         *
         * @param recordId Clinical record ID
         * @throws NotFoundException     if clinical record not found
         * @throws AccessDeniedException if user lacks permission to modify this record
         */
        @Transactional
        public void deletePrescription(Integer recordId) {
                log.info("Deleting prescription for clinical record ID: {}", recordId);

                // Step 1: Load clinical record (404 if not found)
                ClinicalRecord record = clinicalRecordRepository.findById(recordId)
                                .orElseThrow(() -> new NotFoundException("RECORD_NOT_FOUND",
                                                "Clinical record not found with ID: " + recordId));

                // Step 2: Check RBAC authorization (same as API 8.15)
                Appointment appointment = record.getAppointment();
                checkAccessPermission(appointment);

                // Step 3: Find prescription (if exists)
                ClinicalPrescription prescription = prescriptionRepository
                                .findByClinicalRecord_ClinicalRecordId(recordId)
                                .orElse(null);

                if (prescription == null) {
                        log.info("No prescription found for clinical record ID: {}", recordId);
                        return; // No prescription to delete - success (idempotent)
                }

                // Step 4: Delete prescription (CASCADE will delete items automatically)
                log.info("Deleting prescription ID: {} with {} items",
                                prescription.getPrescriptionId(),
                                prescription.getItems().size());

                prescriptionRepository.delete(prescription);

                log.info("Prescription deleted successfully for clinical record ID: {}", recordId);

                // TODO: If inventory integration exists, restore stock levels here
                // For each item in prescription.getItems():
                // - If item.getItemMaster() != null
                // - Call inventoryService.restoreStock(itemMasterId, quantity)
        }

        /**
         * API 8.9: Get Tooth Status for Patient (Odontogram)
         *
         * Authorization:
         * - ROLE_ADMIN: Full access
         * - VIEW_PATIENT permission: Doctors, Nurses, Receptionists
         *
         * Business Logic:
         * - Only returns teeth with abnormal conditions (not HEALTHY)
         * - Teeth not in response are considered HEALTHY
         * - Empty array means all teeth are healthy
         *
         * @param patientId Patient ID
         * @return List of tooth statuses (only abnormal teeth)
         */
        @Transactional(readOnly = true)
        public java.util.List<ToothStatusResponse> getToothStatus(Integer patientId) {
                log.info("Fetching tooth status for patient ID: {}", patientId);

                // Step 1: Validate patient exists
                Patient patient = patientRepository.findById(patientId)
                                .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND",
                                                "Patient not found with ID: " + patientId));

                // Step 2: Check authorization (VIEW_PATIENT permission required)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                boolean hasViewPatient = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("VIEW_PATIENT"));

                if (!isAdmin && !hasViewPatient) {
                        throw new AccessDeniedException(
                                        "Access denied: You need VIEW_PATIENT permission to view tooth status");
                }

                // Step 3: Fetch all tooth statuses for patient
                java.util.List<PatientToothStatus> toothStatuses = toothStatusRepository
                                .findByPatient_PatientId(patientId);

                log.info("Found {} tooth statuses for patient ID: {}", toothStatuses.size(), patientId);

                // Step 4: Map to DTO (only abnormal teeth, filter out HEALTHY if any)
                return toothStatuses.stream()
                                .filter(status -> status
                                                .getStatus() != com.dental.clinic.management.patient.domain.ToothConditionEnum.HEALTHY)
                                .map(this::mapToothStatusToDTO)
                                .collect(Collectors.toList());
        }

        /**
         * API 8.10: Update Tooth Status for Patient (Odontogram)
         *
         * Authorization:
         * - ROLE_ADMIN: Full access
         * - WRITE_CLINICAL_RECORD permission: Doctors only
         *
         * Business Logic:
         * - If tooth status exists: UPDATE
         * - If tooth status doesn't exist: CREATE
         * - If status = HEALTHY: DELETE record (tooth returns to default state)
         *
         * @param patientId Patient ID
         * @param request   Update tooth status request
         * @return Updated tooth status response (or null if deleted)
         */
        @Transactional
        public ToothStatusResponse updateToothStatus(Integer patientId, UpdateToothStatusRequest request) {
                log.info("Updating tooth status for patient ID: {}, tooth: {}, status: {}",
                                patientId, request.getToothNumber(), request.getStatus());

                // Step 1: Validate patient exists
                Patient patient = patientRepository.findById(patientId)
                                .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND",
                                                "Patient not found with ID: " + patientId));

                // Step 2: Check authorization (WRITE_CLINICAL_RECORD permission required)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                boolean hasWritePermission = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("WRITE_CLINICAL_RECORD"));

                if (!isAdmin && !hasWritePermission) {
                        throw new AccessDeniedException(
                                        "Access denied: You need WRITE_CLINICAL_RECORD permission to update tooth status");
                }

                // Step 3: Check if tooth status exists
                java.util.Optional<PatientToothStatus> existingStatus = toothStatusRepository
                                .findByPatient_PatientIdAndToothNumber(patientId, request.getToothNumber());

                // Step 4: Business logic based on status
                if (request.getStatus() == com.dental.clinic.management.patient.domain.ToothConditionEnum.HEALTHY) {
                        // HEALTHY status: Delete record if exists (tooth returns to default state)
                        if (existingStatus.isPresent()) {
                                toothStatusRepository.delete(existingStatus.get());
                                log.info("Deleted tooth status for patient ID: {}, tooth: {} (set to HEALTHY)",
                                                patientId, request.getToothNumber());
                        } else {
                                log.info("No tooth status to delete for patient ID: {}, tooth: {} (already HEALTHY)",
                                                patientId, request.getToothNumber());
                        }
                        return null; // Return null for deleted/HEALTHY status
                } else {
                        // Non-HEALTHY status: Create or Update
                        PatientToothStatus toothStatus;

                        if (existingStatus.isPresent()) {
                                // UPDATE existing record
                                toothStatus = existingStatus.get();
                                toothStatus.setStatus(request.getStatus());
                                toothStatus.setNotes(request.getNotes());
                                log.info("Updating existing tooth status ID: {}", toothStatus.getToothStatusId());
                        } else {
                                // CREATE new record
                                toothStatus = PatientToothStatus.builder()
                                                .patient(patient)
                                                .toothNumber(request.getToothNumber())
                                                .status(request.getStatus())
                                                .notes(request.getNotes())
                                                .build();
                                log.info("Creating new tooth status for tooth: {}", request.getToothNumber());
                        }

                        // Save and return
                        PatientToothStatus saved = toothStatusRepository.save(toothStatus);
                        log.info("Tooth status saved successfully: ID {}", saved.getToothStatusId());

                        return mapToothStatusToDTO(saved);
                }
        }

        /**
         * Helper method: Map PatientToothStatus entity to ToothStatusResponse DTO
         */
        private ToothStatusResponse mapToothStatusToDTO(PatientToothStatus status) {
                return ToothStatusResponse.builder()
                                .toothStatusId(status.getToothStatusId())
                                .patientId(status.getPatient().getPatientId())
                                .toothNumber(status.getToothNumber())
                                .status(status.getStatus())
                                .notes(status.getNotes())
                                .recordedAt(status.getRecordedAt() != null
                                                ? status.getRecordedAt().format(FORMATTER)
                                                : null)
                                .updatedAt(status.getUpdatedAt() != null
                                                ? status.getUpdatedAt().format(FORMATTER)
                                                : null)
                                .build();
        }
}
