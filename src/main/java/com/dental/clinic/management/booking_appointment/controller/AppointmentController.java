package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentResponse;
import com.dental.clinic.management.booking_appointment.dto.request.AvailableTimesRequest;
import com.dental.clinic.management.booking_appointment.dto.response.AvailableTimesResponse;
import com.dental.clinic.management.booking_appointment.service.AppointmentAvailabilityService;
import com.dental.clinic.management.booking_appointment.service.AppointmentCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

}
