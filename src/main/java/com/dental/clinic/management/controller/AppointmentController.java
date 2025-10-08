package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateAppointmentRequest;
import com.dental.clinic.management.dto.request.UpdateAppointmentRequest;
import com.dental.clinic.management.dto.response.AppointmentResponse;
import com.dental.clinic.management.service.AppointmentService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointment Management", description = "APIs for scheduling and managing appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping("")
    @Operation(summary = "List appointments", description = "List appointments with pagination")
    @ApiMessage("List appointments successfully")
    public ResponseEntity<Page<AppointmentResponse>> listAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) java.time.LocalDate appointmentDate,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) com.dental.clinic.management.domain.enums.AppointmentStatus status,
            @RequestParam(required = false) com.dental.clinic.management.domain.enums.AppointmentType type) {

        Page<AppointmentResponse> resp = service.listAppointments(page, size, sortBy, sortDirection, appointmentDate, doctorId, status, type);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment", description = "Get appointment by id")
    @ApiMessage("Get appointment successfully")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable String appointmentId) {
        AppointmentResponse resp = service.getAppointmentById(appointmentId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("")
    @Operation(summary = "Schedule appointment", description = "Create a new appointment")
    @ApiMessage("Schedule appointment successfully")
    public ResponseEntity<AppointmentResponse> schedule(@Valid @RequestBody CreateAppointmentRequest request) throws URISyntaxException {
        AppointmentResponse resp = service.schedule(request);
        return ResponseEntity.created(new URI("/api/v1/appointments/" + resp.getAppointmentId())).body(resp);
    }

    @PatchMapping("/{appointmentId}")
    @Operation(summary = "Update appointment", description = "Update an appointment (partial)")
    @ApiMessage("Update appointment successfully")
    public ResponseEntity<AppointmentResponse> update(@PathVariable String appointmentId, @Valid @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse resp = service.update(appointmentId, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment")
    @ApiMessage("Cancel appointment successfully")
    public ResponseEntity<Void> cancel(@PathVariable String appointmentId) {
        service.cancel(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
