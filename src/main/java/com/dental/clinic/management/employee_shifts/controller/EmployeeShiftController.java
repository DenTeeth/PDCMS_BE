package com.dental.clinic.management.employee_shifts.controller;

import com.dental.clinic.management.employee_shifts.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.employee_shifts.service.EmployeeShiftService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Employee Shift Management.
 * Provides endpoints for managing employee shift assignments and schedules.
 */
@RestController
@RequestMapping("/api/v1/employee-shifts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Shift Management", description = "APIs for managing employee shift schedules with RBAC (Role-Based Access Control)")
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
    @Operation(summary = "Get shifts for calendar view", description = "Retrieve employee shifts within a date range. Permissions: VIEW_SHIFTS_ALL (all employees) or VIEW_SHIFTS_OWN (own shifts only)")
    @ApiMessage("Get shifts successfully")
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
    @Operation(summary = "Get shifts summary", description = "Get aggregated shift statistics for dashboard and reporting (Admin/Manager only)")
    @ApiMessage("Get shifts summary successfully")
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
    @Operation(summary = "Get shift by ID", description = "Retrieve detailed information of a specific shift by its ID")
    @ApiMessage("Get shift details successfully")
    public ResponseEntity<EmployeeShiftResponseDto> getShiftById(
            @Parameter(description = "Employee Shift ID (Format: EMSyyMMddSEQ)", required = true) @PathVariable String employeeShiftId) {
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
    @Operation(summary = "Create new shift", description = "Manually create a new employee shift assignment (Admin/Manager only, requires CREATE_SHIFTS permission)")
    @ApiMessage("Create shift successfully")
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
    @Operation(summary = "Update shift", description = "Update shift status and/or notes (Admin/Manager only, requires UPDATE_SHIFTS permission)")
    @ApiMessage("Update shift successfully")
    public ResponseEntity<EmployeeShiftResponseDto> updateShift(
            @Parameter(description = "Employee Shift ID (Format: EMSyyMMddSEQ)", required = true) @PathVariable String employeeShiftId,
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
    @Operation(summary = "Cancel shift", description = "Cancel an employee shift by setting its status to CANCELLED (Admin/Manager only, requires DELETE_SHIFTS permission)")
    @ApiMessage("Cancel shift successfully")
    public ResponseEntity<Void> cancelShift(
            @Parameter(description = "Employee Shift ID (Format: EMSyyMMddSEQ)", required = true) @PathVariable String employeeShiftId) {
        log.info("Cancelling employee shift with ID: {}", employeeShiftId);
        employeeShiftService.cancelShift(employeeShiftId);
        log.info("Employee shift cancelled successfully: {}", employeeShiftId);
        return ResponseEntity.noContent().build();
    }
}
