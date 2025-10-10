package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CancelDentistScheduleRequest;
import com.dental.clinic.management.dto.request.CreateDentistScheduleRequest;
import com.dental.clinic.management.dto.request.UpdateDentistScheduleRequest;
import com.dental.clinic.management.dto.response.DentistScheduleResponse;
import com.dental.clinic.management.service.DentistWorkScheduleService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for managing dentist work schedules (part-time flexible
 * registration).
 */
@RestController
@RequestMapping("/api/v1/dentist-schedules")
@Tag(name = "Dentist Schedules", description = "APIs for part-time dentist schedule management")
public class DentistScheduleController {

    private final DentistWorkScheduleService scheduleService;

    public DentistScheduleController(DentistWorkScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Create new dentist schedule (self-registration).
     *
     * @param request Create schedule request
     * @return Created schedule response
     */
    @PostMapping
    @ApiMessage("Lịch làm việc đã được đăng ký thành công")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create dentist schedule", description = "Register new work schedule (Part-time dentists only)")
    public ResponseEntity<DentistScheduleResponse> createSchedule(
            @Valid @RequestBody CreateDentistScheduleRequest request) {
        DentistScheduleResponse response = scheduleService.createDentistSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update dentist schedule.
     * Dentist can only update their own schedules, Admin can update any.
     *
     * @param scheduleId Schedule ID
     * @param request    Update schedule request
     * @return Updated schedule response
     */
    @PutMapping("/{scheduleId}")
    @ApiMessage("Lịch làm việc đã được cập nhật thành công")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update dentist schedule", description = "Update existing schedule (Owner or Admin)")
    public ResponseEntity<DentistScheduleResponse> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody UpdateDentistScheduleRequest request) {
        DentistScheduleResponse response = scheduleService.updateDentistSchedule(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel dentist schedule.
     * Cannot cancel BOOKED schedules (patient already registered).
     *
     * @param scheduleId Schedule ID
     * @param request    Cancel request with reason
     * @return Success message
     */
    @DeleteMapping("/{scheduleId}/cancel")
    @ApiMessage("Lịch làm việc đã được hủy thành công")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Cancel dentist schedule", description = "Cancel AVAILABLE schedule (Cannot cancel BOOKED)")
    public ResponseEntity<Void> cancelSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody CancelDentistScheduleRequest request) {
        scheduleService.cancelDentistSchedule(scheduleId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get dentist schedule by ID.
     *
     * @param scheduleId Schedule ID
     * @return Schedule response
     */
    @GetMapping("/{scheduleId}")
    @ApiMessage("Lấy thông tin lịch làm việc thành công")
    @Operation(summary = "Get dentist schedule", description = "Get schedule by ID (All roles)")
    public ResponseEntity<DentistScheduleResponse> getScheduleById(@PathVariable String scheduleId) {
        DentistScheduleResponse response = scheduleService.getDentistScheduleById(scheduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all schedules for a dentist with pagination.
     *
     * @param dentistId Dentist ID
     * @param startDate Start date filter (optional)
     * @param endDate   End date filter (optional)
     * @param page      Page number (default: 0)
     * @param size      Page size (default: 10, max: 100)
     * @return Page of dentist schedules
     */
    @GetMapping
    @ApiMessage("Lấy danh sách lịch làm việc thành công")
    @Operation(summary = "Get dentist schedules", description = "Get all schedules for a dentist with optional date range filter")
    public ResponseEntity<Page<DentistScheduleResponse>> getSchedulesByDentist(
            @RequestParam String dentistId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }

        Page<DentistScheduleResponse> response = scheduleService.getAllSchedulesByDentist(
                dentistId, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available schedules (calendar view for appointment booking).
     *
     * @param startDate Start date
     * @param endDate   End date
     * @param page      Page number (default: 0)
     * @param size      Page size (default: 20, max: 100)
     * @return Page of available schedules
     */
    @GetMapping("/available")
    @ApiMessage("Lấy lịch trống thành công")
    @Operation(summary = "Get available schedules", description = "Get AVAILABLE schedules for appointment booking (Calendar view)")
    public ResponseEntity<Page<DentistScheduleResponse>> getAvailableSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DentistScheduleResponse> response = scheduleService.getAvailableSchedules(
                startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }
}
