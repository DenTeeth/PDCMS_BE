package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.Appointment;
import com.dental.clinic.management.dto.request.CreateAppointmentRequest;
import com.dental.clinic.management.dto.request.UpdateAppointmentRequest;
import com.dental.clinic.management.dto.response.AppointmentResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.AppointmentMapper;
import com.dental.clinic.management.repository.AppointmentRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dental.clinic.management.domain.enums.AppointmentStatus;
import com.dental.clinic.management.domain.enums.AppointmentType;
import com.dental.clinic.management.domain.DentistWorkSchedule;
import com.dental.clinic.management.domain.enums.DentistWorkScheduleStatus;
import com.dental.clinic.management.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.domain.enums.DentistWorkScheduleStatus;
import com.dental.clinic.management.dto.request.RescheduleAppointmentRequest;
import com.dental.clinic.management.domain.enums.AppointmentType;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.domain.DentistWorkSchedule;
import com.dental.clinic.management.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.repository.DentistWorkScheduleRepository;
import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;





@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final AppointmentMapper mapper;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final DentistWorkScheduleRepository dentistWorkScheduleRepository;

    public AppointmentService(AppointmentRepository repository,
                              AppointmentMapper mapper,
                              PatientRepository patientRepository,
                              EmployeeRepository employeeRepository,
                              DentistWorkScheduleRepository dentistWorkScheduleRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.patientRepository = patientRepository;
        this.employeeRepository = employeeRepository;
        this.dentistWorkScheduleRepository = dentistWorkScheduleRepository;
    }

    @PreAuthorize("(hasAnyRole('RECEPTIONIST','ADMIN') and hasAuthority('" + VIEW_APPOINTMENT + "'))")
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listAppointments(int page,
                                                      int size,
                                                      String sortBy,
                                                      String sortDirection,
                                                      LocalDate appointmentDate,
                                                      String doctorId,
                                                      AppointmentStatus status,
                                                      AppointmentType type) {
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        // If any filter provided, use filtered query
        if (appointmentDate != null || doctorId != null || status != null || type != null) {
            return repository.findByOptionalFilters(appointmentDate, doctorId, status, type, pageable).map(mapper::toResponse);
        }
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @PreAuthorize("(hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR') and hasAuthority('" + VIEW_APPOINTMENT + "'))")
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        return mapper.toResponse(appt);
    }

    @PreAuthorize("(hasAnyRole('RECEPTIONIST','ADMIN') and hasAuthority('" + VIEW_APPOINTMENT + "'))")
    @Transactional(readOnly = true)
    public java.util.List<AvailableSlotResponse> getAvailableSlots(String doctorId, java.time.LocalDate date) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new BadRequestAlertException("doctorId is required", "appointment", "missing_doctor");
        }
        if (date == null) {
            throw new BadRequestAlertException("date is required", "appointment", "missing_date");
        }

    // fetch dentist schedules for the date (only AVAILABLE or BOOKED)
    java.util.List<DentistWorkSchedule> schedules = dentistWorkScheduleRepository.findByDentistIdAndWorkDate(doctorId, date);
    schedules.removeIf(s -> s.getStatus() == DentistWorkScheduleStatus.CANCELLED || s.getStatus() == DentistWorkScheduleStatus.EXPIRED);
    java.util.List<AvailableSlotResponse> slots = new java.util.ArrayList<>();
        if (schedules == null || schedules.isEmpty()) return slots;

    // fetch existing appointments for the doctor and date (exclude cancelled)
    java.util.List<Appointment> appointments = repository.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
        .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
        .collect(java.util.stream.Collectors.toList());

        for (DentistWorkSchedule s : schedules) {
            java.time.LocalTime cur = s.getStartTime();
            while (!cur.plusMinutes(30).isAfter(s.getEndTime())) {
                java.time.LocalTime slotStart = cur;
                java.time.LocalTime slotEnd = cur.plusMinutes(30);
                boolean conflict = appointments.stream().anyMatch(a -> a.getStartTime().isBefore(slotEnd) && a.getEndTime().isAfter(slotStart));
                if (!conflict) {
                    slots.add(new AvailableSlotResponse(doctorId, date, slotStart));
                }
                cur = cur.plusMinutes(30);
            }
        }

        return slots;
    }

    @PreAuthorize("hasAuthority('" + CREATE_APPOINTMENT + "') or hasRole('ADMIN')")
    @Transactional
    public AppointmentResponse schedule(CreateAppointmentRequest request) {
        // validate times
        if (request.getStartTime().compareTo(request.getEndTime()) >= 0) {
            throw new BadRequestAlertException("Start time must be before end time", "appointment", "invalidtime");
        }

        // appointment_date must be >= today
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestAlertException("Appointment date must be today or later", "appointment", "invalid_date");
        }

        // validate patient and doctor
        patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BadRequestAlertException("Patient not found: " + request.getPatientId(), "appointment", "patientnotfound"));
        if (!patientRepository.existsById(request.getPatientId())) {
            throw new BadRequestAlertException("Patient not found: " + request.getPatientId(), "appointment", "patientnotfound");
        }
        if (!employeeRepository.existsById(request.getDoctorId())) {
            throw new BadRequestAlertException("Doctor not found: " + request.getDoctorId(), "appointment", "doctornotfound");
        }

        employeeRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BadRequestAlertException("Doctor not found: " + request.getDoctorId(), "appointment", "doctornotfound"));

        // check overlap
        List<Appointment> overlaps = repository.findOverlappingByDoctorAndDate(request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new BadRequestAlertException("Doctor not available for the given time slot", "appointment", "overlap");
        }

        // Check dentist work schedules for availability on that date
        java.util.List<DentistWorkSchedule> schedules = dentistWorkScheduleRepository.findByDentistIdAndWorkDate(request.getDoctorId(), request.getAppointmentDate());
        if (schedules == null || schedules.isEmpty()) {
            throw new BadRequestAlertException("Doctor is not available on this date", "appointment", "doctor_not_available");
        }

        boolean withinWorkingHours = schedules.stream().anyMatch(s -> !s.getStartTime().isAfter(request.getStartTime()) && !s.getEndTime().isBefore(request.getEndTime()));
        if (!withinWorkingHours) {
            throw new BadRequestAlertException("Appointment time is outside doctor's working hours", "appointment", "outside_working_hours");
        }

    Appointment entity = mapper.toEntity(request);
    // appointment_id should be full APT + YYYYMMDD + SEQ (no dashes)
    DateTimeFormatter dfFull = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter dfShort = DateTimeFormatter.ofPattern("yyMMdd");
    String datePartFull = request.getAppointmentDate().format(dfFull);
    String datePartShort = request.getAppointmentDate().format(dfShort);
    long seq = repository.countByDoctorIdAndAppointmentDate(request.getDoctorId(), request.getAppointmentDate()) + 1;
    String seqPart = String.format("%04d", seq);
    String generatedId = "APT" + datePartFull + seqPart; // full PK
    String generatedCode = "APT" + datePartShort + seqPart; // shortened code (YYMMDD)
    entity.setAppointmentId(generatedId);
    entity.setAppointmentCode(generatedCode);
    if (entity.getStatus() == null) entity.setStatus(AppointmentStatus.SCHEDULED);
        // createdBy from security token if present
        SecurityUtil.getCurrentUserLogin().ifPresent(entity::setCreatedBy);
        Appointment saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AppointmentResponse confirm(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestAlertException("Only SCHEDULED appointments can be confirmed", "appointment", "invalid_status");
        }
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.setConfirmedAt(java.time.LocalDateTime.now());
        repository.save(appt);
        return mapper.toResponse(appt);
    }

    @Transactional
    public AppointmentResponse complete(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestAlertException("Only CONFIRMED appointments can be completed", "appointment", "invalid_status");
        }
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.setCompletedAt(java.time.LocalDateTime.now());
        Appointment saved = repository.save(appt);
        // Trigger treatment plan creation if EXAMINATION
        if (saved.getType() == AppointmentType.EXAMINATION) {
            // TreatmentPlan creation - stubbed
            // treatmentPlanService.createFromAppointment(saved);
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    public AppointmentResponse noShow(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestAlertException("Only CONFIRMED appointments can be marked as no-show", "appointment", "invalid_status");
        }
        appt.setStatus(AppointmentStatus.NO_SHOW);
        Appointment saved = repository.save(appt);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancel(String appointmentId, String cancellationReason) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        if (appt.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestAlertException("Cannot cancel a completed appointment", "appointment", "invalid_status");
        }
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancelledAt(java.time.LocalDateTime.now());
        appt.setCancellationReason(cancellationReason);
        Appointment saved = repository.save(appt);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AppointmentResponse reschedule(String appointmentId, RescheduleAppointmentRequest request) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        if (!(appt.getStatus() == AppointmentStatus.SCHEDULED || appt.getStatus() == AppointmentStatus.CONFIRMED)) {
            throw new BadRequestAlertException("Only SCHEDULED or CONFIRMED appointments can be rescheduled", "appointment", "invalid_status");
        }

        // Validate times
        if (request.getStartTime().compareTo(request.getEndTime()) >= 0) {
            throw new BadRequestAlertException("Start time must be before end time", "appointment", "invalidtime");
        }
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestAlertException("Appointment date must be today or later", "appointment", "invalid_date");
        }

        // Check dentist work schedules for availability on the new date
        java.util.List<DentistWorkSchedule> schedules = dentistWorkScheduleRepository.findByDentistIdAndWorkDate(appt.getDoctorId(), request.getAppointmentDate());
        if (schedules == null || schedules.isEmpty()) {
            throw new BadRequestAlertException("Doctor is not available on this date", "appointment", "doctor_not_available");
        }

        // Validate new time is within doctor's working hours
        boolean withinWorkingHours = schedules.stream().anyMatch(s -> !s.getStartTime().isAfter(request.getStartTime()) && !s.getEndTime().isBefore(request.getEndTime()));
        if (!withinWorkingHours) {
            throw new BadRequestAlertException("Appointment time is outside doctor's working hours", "appointment", "outside_working_hours");
        }

        // Check overlaps with other appointments (excluding this one)
        List<Appointment> overlaps = repository.findOverlappingByDoctorAndDateExcluding(appt.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), appointmentId);
        if (!overlaps.isEmpty()) {
            throw new BadRequestAlertException("Doctor not available for the new time slot", "appointment", "overlap");
        }

        // Log old schedule into notes
        String prev = String.format("Rescheduled from %s %s-%s. ", appt.getAppointmentDate(), appt.getStartTime(), appt.getEndTime());
        appt.setNotes((appt.getNotes() != null ? appt.getNotes() + "\n" : "") + prev + (request.getNotes() != null ? request.getNotes() : ""));

        // Apply new schedule and reset status to SCHEDULED
        appt.setAppointmentDate(request.getAppointmentDate());
        appt.setStartTime(request.getStartTime());
        appt.setEndTime(request.getEndTime());
        appt.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = repository.save(appt);
        return mapper.toResponse(saved);
    }

    @PreAuthorize("(hasAnyRole('RECEPTIONIST','ADMIN') and hasAuthority('" + UPDATE_APPOINTMENT + "'))")
    @Transactional
    public AppointmentResponse update(String appointmentId, UpdateAppointmentRequest request) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));

        // If the appointment is already in a terminal state, it must not be modified
        AppointmentStatus existingStatus = appt.getStatus();
        if (existingStatus == AppointmentStatus.COMPLETED || existingStatus == AppointmentStatus.CANCELLED || existingStatus == AppointmentStatus.NO_SHOW) {
            throw new BadRequestAlertException("Appointment is final and cannot be modified", "appointment", "final_status");
        }

        // Determine resulting status (if request changes status)
        AppointmentStatus resultingStatus = request.getStatus() != null ? request.getStatus() : appt.getStatus();

        // Validate status transition if status is changing
        if (request.getStatus() != null) {
            AppointmentStatus currentStatus = appt.getStatus();
            AppointmentStatus nextStatus = request.getStatus();
            if (!isValidStatusTransition(currentStatus, nextStatus)) {
                throw new BadRequestAlertException("Invalid status transition from " + currentStatus + " to " + nextStatus, "appointment", "invalid_status_flow");
            }
        }

        // Prevent changing patient_id or doctor_id when resulting status != SCHEDULED
        if (resultingStatus != null && resultingStatus != AppointmentStatus.SCHEDULED) {
            if (request.getPatientId() != null && !request.getPatientId().equals(appt.getPatientId())) {
                throw new BadRequestAlertException("Cannot change patient_id when appointment status is not SCHEDULED", "appointment", "immutable");
            }
            if (request.getDoctorId() != null && !request.getDoctorId().equals(appt.getDoctorId())) {
                throw new BadRequestAlertException("Cannot change doctor_id when appointment status is not SCHEDULED", "appointment", "immutable");
            }
        }

        // Determine effective date/time/doctor for validation (use existing values when not provided in request)
        LocalDate date = request.getAppointmentDate() != null ? request.getAppointmentDate() : appt.getAppointmentDate();
        LocalTime start = request.getStartTime() != null ? request.getStartTime() : appt.getStartTime();
        LocalTime end = request.getEndTime() != null ? request.getEndTime() : appt.getEndTime();
        String doctorId = request.getDoctorId() != null ? request.getDoctorId() : appt.getDoctorId();

        // Validate times
        if (start != null && end != null && start.compareTo(end) >= 0) {
            throw new BadRequestAlertException("Start time must be before end time", "appointment", "invalidtime");
        }

        // Ensure date/start/end are present for overlap checking
        if (date == null || start == null || end == null) {
            throw new BadRequestAlertException("Appointment date/start/end must be provided", "appointment", "incomplete_datetime");
        }

        // Check overlaps excluding the appointment being updated
        List<Appointment> overlaps = repository.findOverlappingByDoctorAndDateExcluding(doctorId, date, start, end, appointmentId);
        if (!overlaps.isEmpty()) {
            throw new BadRequestAlertException("Doctor not available for the given time slot", "appointment", "overlap");
        }

        // Apply updates and persist
        mapper.updateFromRequest(request, appt);
        Appointment saved = repository.save(appt);
        return mapper.toResponse(saved);
    }

    @PreAuthorize("(hasRole('ADMIN') and hasAuthority('" + DELETE_APPOINTMENT + "'))")
    @Transactional
    public void cancel(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
    appt.setStatus(AppointmentStatus.CANCELLED);
        repository.save(appt);
    }

    /**
     * Validate allowed status transitions. Returns true if transitioning from current -> next is permitted.
     */
    // SCHEDULED → CONFIRMED (khách đi được)) → COMPLETED (happy case)
    // SCHEDULED → CONFIRMED → CANCELLED (khách bảo đi được, nhưng cancel vì bận)
    // SCHEDULED → CONFIRMED → NO_SHOW (khách bảo đi được, nhưng k lên)
    // SCHEDULED → CANCELLED(khách bảo k đi được luôn)
    private boolean isValidStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        // If either side is null, let other validations handle it (do not block here)
        if (current == null || next == null) return true;

        // Terminal states cannot transition to others
        if (current == AppointmentStatus.COMPLETED || current == AppointmentStatus.CANCELLED || current == AppointmentStatus.NO_SHOW) {
            return false;
        }

        switch (current) {
            case SCHEDULED:
                // Allowed from SCHEDULED:
                // - remain SCHEDULED
                // - go to CONFIRMED
                // - go to CANCELLED (direct cancel)
                // NOT allowed: SCHEDULED -> NO_SHOW or SCHEDULED -> COMPLETED directly
                return next == AppointmentStatus.SCHEDULED || next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.CANCELLED;
            case CONFIRMED:
                // Allowed from CONFIRMED:
                // - remain CONFIRMED
                // - go to COMPLETED
                // - go to CANCELLED
                // - go to NO_SHOW
                return next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED || next == AppointmentStatus.NO_SHOW;
            default:
                return false;
        }
    }
}
