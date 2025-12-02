package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.Room;
import com.dental.clinic.management.booking_appointment.repository.AppointmentParticipantRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.RoomRepository;
import com.dental.clinic.management.clinical_records.domain.ClinicalRecord;
import com.dental.clinic.management.clinical_records.dto.*;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
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
        private final AppointmentRepository appointmentRepository;
        private final AppointmentParticipantRepository appointmentParticipantRepository;
        private final EmployeeRepository employeeRepository;
        private final PatientRepository patientRepository;
        private final RoomRepository roomRepository;

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
                                                        .map(item -> PrescriptionItemDTO.builder()
                                                                        .prescriptionItemId(
                                                                                        item.getPrescriptionItemId())
                                                                        .itemCode(item.getItemMaster() != null
                                                                                        ? item.getItemMaster()
                                                                                                        .getItemCode()
                                                                                        : null)
                                                                        .itemName(item.getItemName())
                                                                        .quantity(item.getQuantity())
                                                                        .dosageInstructions(
                                                                                        item.getDosageInstructions())
                                                                        .createdAt(item.getCreatedAt()
                                                                                        .format(FORMATTER))
                                                                        .build())
                                                        .collect(Collectors.toList());

                                        return PrescriptionDTO.builder()
                                                        .prescriptionId(presc.getPrescriptionId())
                                                        .prescriptionNotes(presc.getPrescriptionNotes())
                                                        .createdAt(presc.getCreatedAt().format(FORMATTER))
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
                                .createdAt(record.getCreatedAt().format(FORMATTER))
                                .updatedAt(record.getUpdatedAt().format(FORMATTER))
                                .appointment(appointmentDTO)
                                .doctor(doctorDTO)
                                .patient(patientDTO)
                                .procedures(procedures)
                                .prescriptions(prescriptions)
                                .build();
        }
}
