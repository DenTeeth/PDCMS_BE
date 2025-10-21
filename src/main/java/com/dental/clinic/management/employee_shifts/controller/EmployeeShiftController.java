package com.dental.clinic.management.employee_shifts.controller;

import com.dental.clinic.management.employee_shifts.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.employee_shifts.service.EmployeeShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Employee Shift Management.
 * Provides endpoints for managing employee shift assignments and schedules.
 */
@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    /**
     * Get shifts for calendar view within a date range.
     * 
     * @param startDate  Start date (inclusive), format: YYYY-MM-DD
     * @param endDate    End date (inclusive), format: YYYY-MM-DD
     * @param employeeId Optional employee ID filter
     * @return List of employee shifts
     */
    @GetMapping

    public ResponseEntity<List<EmployeeShiftResponseDto>> getShiftsForCalendarView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long employeeId) {
        log.info("Fetching shifts for calendar view - startDate: {}, endDate: {}, employeeId: {}",
                startDate, endDate, employeeId);
        List<EmployeeShiftResponseDto> responses = employeeShiftService.getShiftsForCalendarView(
                startDate, endDate, employeeId);
        log.info("Retrieved {} shifts", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get shifts summary for reporting/analytics.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @return Shift summary data (placeholder - to be implemented)
     */
    @GetMapping("/summary")

    public ResponseEntity<String> getShiftsSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching shifts summary - startDate: {}, endDate: {}", startDate, endDate);
        // TODO: Implement summary/aggregation logic
        return ResponseEntity.ok("Summary endpoint - To be implemented");
    }

    /**
     * Get a specific employee shift by ID.
     * 
     * @param employeeShiftId The employee shift ID to retrieve
     * @return EmployeeShiftResponseDto with shift details
     */
    @GetMapping("/{employeeShiftId}")

    public ResponseEntity<EmployeeShiftResponseDto> getShiftById(@PathVariable UUID employeeShiftId) {
        log.info("Fetching employee shift with ID: {}", employeeShiftId);
        EmployeeShiftResponseDto response = employeeShiftService.getShiftDetails(employeeShiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new employee shift.
     * 
     * @param request CreateShiftRequestDto containing shift details
     * @return EmployeeShiftResponseDto with created shift information
     */
    @PostMapping

    public ResponseEntity<EmployeeShiftResponseDto> createShift(
            @Valid @RequestBody CreateShiftRequestDto request) {
        log.info("Creating employee shift for employee: {}", request.getEmployeeId());
        EmployeeShiftResponseDto response = employeeShiftService.createManualShift(request);
        log.info("Employee shift created successfully with ID: {}", response.getEmployeeShiftId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing employee shift.
     * 
     * @param employeeShiftId The ID of the employee shift to update
     * @param request         UpdateShiftRequestDto containing fields to update
     * @return EmployeeShiftResponseDto with updated shift information
     */
    @PatchMapping("/{employeeShiftId}")

    public ResponseEntity<EmployeeShiftResponseDto> updateShift(
            @PathVariable UUID employeeShiftId,
            @Valid @RequestBody UpdateShiftRequestDto request) {
        log.info("Updating employee shift with ID: {}", employeeShiftId);
        EmployeeShiftResponseDto response = employeeShiftService.updateShift(employeeShiftId, request);
        log.info("Employee shift updated successfully: {}", employeeShiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an employee shift (soft delete).
     * 
     * @param employeeShiftId The ID of the employee shift to cancel
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{employeeShiftId}")

    public ResponseEntity<Void> cancelShift(@PathVariable UUID employeeShiftId) {
        log.info("Cancelling employee shift with ID: {}", employeeShiftId);
        employeeShiftService.cancelShift(employeeShiftId);
        log.info("Employee shift cancelled successfully: {}", employeeShiftId);
        return ResponseEntity.noContent().build();
    }
}
