package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.AppointmentFilterCriteria;
import com.dental.clinic.management.booking_appointment.dto.AppointmentSummaryDTO;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentResponse;
import com.dental.clinic.management.booking_appointment.dto.DatePreset;
import com.dental.clinic.management.booking_appointment.dto.request.AvailableTimesRequest;
import com.dental.clinic.management.booking_appointment.dto.response.AvailableTimesResponse;
import com.dental.clinic.management.booking_appointment.service.AppointmentAvailabilityService;
import com.dental.clinic.management.booking_appointment.service.AppointmentCreationService;
import com.dental.clinic.management.booking_appointment.service.AppointmentListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Appointment Management APIs
 * Handles appointment scheduling, availability checking, and lifecycle
 * management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentAvailabilityService availabilityService;
    private final AppointmentCreationService creationService;
    private final AppointmentListService listService;

    /**
     * P3.1: Find Available Time Slots
     *
     * GET /api/v1/appointments/available-times
     *
     * Business Logic:
     * 1. Validate date (not in past)
     * 2. Validate employee, services, participants exist and active
     * 3. Calculate total duration (sum of service durations + buffers)
     * 4. Check doctor specialization
     * 5. Filter compatible rooms (room_services V16)
     * 6. Find intersection of available time (doctor + assistants + rooms)
     * 7. Split into slots and return with available rooms
     *
     * @param request Query parameters with date, employeeCode, serviceCodes,
     *                participantCodes
     * @return Available time slots with compatible room codes
     */
    @GetMapping("/available-times")
    @PreAuthorize("hasAuthority('CREATE_APPOINTMENT')")
    public ResponseEntity<AvailableTimesResponse> findAvailableTimes(
            @Valid @ModelAttribute AvailableTimesRequest request) {

        log.info("Finding available times for date={}, employeeCode={}, services={}",
                request.getDate(), request.getEmployeeCode(), request.getServiceCodes());

        AvailableTimesResponse response = availabilityService.findAvailableTimes(request);

        return ResponseEntity.ok(response);
    }

    /**
     * P3.2: Create New Appointment
     *
     * POST /api/v1/appointments
     *
     * 9-Step Transactional Process:
     * 1. Get creator from SecurityContext
     * 2. Validate all resources (patient, doctor, room, services, participants)
     * 3. Validate doctor specializations
     * 4. Validate room compatibility (room_services V16)
     * 5. Calculate duration and end time
     * 6. Validate doctor and participant shifts
     * 7. Check conflicts (doctor, room, patient, participants)
     * 8. Insert appointment + services + participants + audit log
     * 9. Return response with nested summaries
     *
     * @param request Create appointment request body
     * @return 201 Created with appointment details
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_APPOINTMENT')")
    public ResponseEntity<CreateAppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {

        log.info("Creating appointment for patient={}, doctor={}, start={}",
                request.getPatientCode(), request.getEmployeeCode(), request.getAppointmentStartTime());

        CreateAppointmentResponse response = creationService.createAppointment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * P3.3: Get Appointment List (Dashboard View)
     *
     * GET /api/v1/appointments
     *
     * Authorization & RBAC Logic:
     * - Users with VIEW_APPOINTMENT_ALL (Receptionist/Admin):
     * Can see all appointments, use all filters freely
     *
     * - Users with VIEW_APPOINTMENT_OWN (Doctor/Patient):
     * Filters are OVERRIDDEN:
     * - Patients: See only their own appointments
     * - Doctors: See appointments where they are primary doctor OR participant
     *
     * Query Parameters:
     * - page (default: 0): Page number
     * - size (default: 10): Items per page
     * - sortBy (default: appointmentStartTime): Sort field
     * - sortDirection (default: ASC): Sort direction
     * - datePreset (TODAY, THIS_WEEK, NEXT_7_DAYS, THIS_MONTH): Quick date filter
     * - dateFrom (YYYY-MM-DD): Filter from date (inclusive)
     * - dateTo (YYYY-MM-DD): Filter to date (inclusive)
     * - today (boolean): Quick filter for today's appointments (DEPRECATED, use
     * datePreset=TODAY)
     * - status (array): Filter by status (SCHEDULED, CHECKED_IN, etc.)
     * - patientCode (string): Filter by patient code (VIEW_ALL only)
     * - patientName (string): Search by patient name (VIEW_ALL only)
     * - patientPhone (string): Search by patient phone (VIEW_ALL only)
     * - employeeCode (string): Filter by doctor code (VIEW_ALL only)
     * - roomCode (string): Filter by room code
     * - serviceCode (string): Filter by service code
     *
     * @return Paginated list of appointments with nested
     *         patient/doctor/room/services/participants
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_APPOINTMENT_ALL', 'VIEW_APPOINTMENT_OWN')")
    public ResponseEntity<Page<AppointmentSummaryDTO>> getAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentStartTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,

            // Date filters
            @RequestParam(required = false) DatePreset datePreset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean today,

            // Status filter (can be multiple)
            @RequestParam(required = false) List<String> status,

            // Entity filters
            @RequestParam(required = false) String patientCode,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientPhone,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String roomCode,
            @RequestParam(required = false) String serviceCode) {

        log.info("Fetching appointments: page={}, size={}, datePreset={}, dateFrom={}, dateTo={}, today={}, status={}",
                page, size, datePreset, dateFrom, dateTo, today, status);

        // Build filter criteria
        AppointmentFilterCriteria criteria = AppointmentFilterCriteria.builder()
                .datePreset(datePreset)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .today(today)
                .status(status)
                .patientCode(patientCode)
                .patientName(patientName)
                .patientPhone(patientPhone)
                .employeeCode(employeeCode)
                .roomCode(roomCode)
                .serviceCode(serviceCode)
                .build();

        Page<AppointmentSummaryDTO> appointments = listService.getAppointments(
                criteria, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(appointments);
    }

}
