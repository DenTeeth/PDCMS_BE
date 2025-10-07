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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final AppointmentMapper mapper;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;

    public AppointmentService(AppointmentRepository repository, AppointmentMapper mapper, PatientRepository patientRepository, EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.patientRepository = patientRepository;
        this.employeeRepository = employeeRepository;
    }

    @PreAuthorize("hasAuthority('" + VIEW_APPOINTMENT + "')")
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listAppointments(int page, int size, String sortBy, String sortDirection) {
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @PreAuthorize("hasAuthority('" + VIEW_APPOINTMENT + "')")
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        return mapper.toResponse(appt);
    }

    @PreAuthorize("hasAuthority('" + CREATE_APPOINTMENT + "')")
    @Transactional
    public AppointmentResponse schedule(CreateAppointmentRequest request) {
        // validate times
        if (request.getStartTime().compareTo(request.getEndTime()) >= 0) {
            throw new BadRequestAlertException("Start time must be before end time", "appointment", "invalidtime");
        }

        // validate patient and doctor
        patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BadRequestAlertException("Patient not found: " + request.getPatientId(), "appointment", "patientnotfound"));

        employeeRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BadRequestAlertException("Doctor not found: " + request.getDoctorId(), "appointment", "doctornotfound"));

        // check overlap
        List<Appointment> overlaps = repository.findOverlappingByDoctorAndDate(request.getDoctorId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new BadRequestAlertException("Doctor not available for the given time slot", "appointment", "overlap");
        }

        Appointment entity = mapper.toEntity(request);
        entity.setAppointmentId(UUID.randomUUID().toString());
        // generate simple appointment code
        entity.setAppointmentCode("APPT" + System.currentTimeMillis() % 100000);
        if (entity.getStatus() == null) entity.setStatus(com.dental.clinic.management.domain.enums.AppointmentStatus.SCHEDULED);
        Appointment saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @PreAuthorize("hasAuthority('" + UPDATE_APPOINTMENT + "')")
    @Transactional
    public AppointmentResponse update(String appointmentId, UpdateAppointmentRequest request) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));

        // if doctor/date/time updated, check overlaps
        LocalDate date = request.getAppointmentDate() != null ? request.getAppointmentDate() : appt.getAppointmentDate();
        LocalTime start = request.getStartTime() != null ? request.getStartTime() : appt.getStartTime();
        LocalTime end = request.getEndTime() != null ? request.getEndTime() : appt.getEndTime();
        String doctorId = request.getDoctorId() != null ? request.getDoctorId() : appt.getDoctorId();

        if (start != null && end != null && start.compareTo(end) >= 0) {
            throw new BadRequestAlertException("Start time must be before end time", "appointment", "invalidtime");
        }

        List<Appointment> overlaps = repository.findOverlappingByDoctorAndDate(doctorId, date, start, end);
        // exclude current
        boolean hasOtherOverlap = overlaps.stream().anyMatch(a -> !a.getAppointmentId().equals(appointmentId));
        if (hasOtherOverlap) {
            throw new BadRequestAlertException("Doctor not available for the given time slot", "appointment", "overlap");
        }

        // apply updates
        mapper.updateFromRequest(request, appt);
        Appointment saved = repository.save(appt);
        return mapper.toResponse(saved);
    }

    @PreAuthorize("hasAuthority('" + CANCEL_APPOINTMENT + "')")
    @Transactional
    public void cancel(String appointmentId) {
        Appointment appt = repository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestAlertException("Appointment not found: " + appointmentId, "appointment", "notfound"));
        appt.setStatus(com.dental.clinic.management.domain.enums.AppointmentStatus.CANCELLED);
        repository.save(appt);
    }
}
