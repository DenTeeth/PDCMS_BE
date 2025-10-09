package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateRecurringScheduleRequest;
import com.dental.clinic.management.dto.request.UpdateRecurringScheduleRequest;
import com.dental.clinic.management.dto.response.RecurringScheduleResponse;
import com.dental.clinic.management.service.RecurringScheduleService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing recurring schedules (weekly patterns for full-time employees).
 * Admin-only access.
 */
@RestController
@RequestMapping("/api/v1/recurring-schedules")
@Tag(name = "Recurring Schedules", description = "APIs for managing weekly recurring schedules (Full-time staff)")
public class RecurringScheduleController {

    private final RecurringScheduleService recurringService;

    public RecurringScheduleController(RecurringScheduleService recurringService) {
        this.recurringService = recurringService;
    }

    /**
     * Create new recurring schedule (weekly pattern).
     * 
     * @param request Create recurring schedule request
     * @return Created recurring schedule response
     */
    @PostMapping
    @ApiMessage("Lịch cố định đã được tạo thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create recurring schedule", description = "Create weekly recurring schedule (Admin only, Full-time employees)")
    public ResponseEntity<RecurringScheduleResponse> createRecurringSchedule(@Valid @RequestBody CreateRecurringScheduleRequest request) {
        RecurringScheduleResponse response = recurringService.createRecurringSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update recurring schedule.
     * Cannot change employeeId or dayOfWeek (delete and recreate instead).
     * 
     * @param recurringId Recurring schedule ID
     * @param request Update request
     * @return Updated recurring schedule response
     */
    @PutMapping("/{recurringId}")
    @ApiMessage("Lịch cố định đã được cập nhật thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update recurring schedule", description = "Update times/shift of recurring schedule (Admin only)")
    public ResponseEntity<RecurringScheduleResponse> updateRecurringSchedule(
            @PathVariable String recurringId,
            @Valid @RequestBody UpdateRecurringScheduleRequest request) {
        RecurringScheduleResponse response = recurringService.updateRecurringSchedule(recurringId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle active/inactive status.
     * Used to temporarily disable/enable recurring schedule.
     * 
     * @param recurringId Recurring schedule ID
     * @param isActive New active status
     * @return Updated recurring schedule response
     */
    @PatchMapping("/{recurringId}/toggle")
    @ApiMessage("Trạng thái lịch cố định đã được cập nhật")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Toggle recurring schedule", description = "Enable/disable recurring schedule (Admin only)")
    public ResponseEntity<RecurringScheduleResponse> toggleRecurringSchedule(
            @PathVariable String recurringId,
            @RequestParam boolean isActive) {
        RecurringScheduleResponse response = recurringService.toggleRecurringSchedule(recurringId, isActive);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recurring schedule by ID.
     * 
     * @param recurringId Recurring schedule ID
     * @return Recurring schedule response
     */
    @GetMapping("/{recurringId}")
    @ApiMessage("Lấy thông tin lịch cố định thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR')")
    @Operation(summary = "Get recurring schedule", description = "Get recurring schedule by ID (Admin/HR)")
    public ResponseEntity<RecurringScheduleResponse> getRecurringScheduleById(@PathVariable String recurringId) {
        RecurringScheduleResponse response = recurringService.getRecurringScheduleById(recurringId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all recurring schedules for an employee (weekly pattern).
     * 
     * @param employeeId Employee ID
     * @param includeInactive Include inactive schedules
     * @param page Page number (default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Page of recurring schedules
     */
    @GetMapping
    @ApiMessage("Lấy danh sách lịch cố định thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR')")
    @Operation(summary = "Get recurring schedules", description = "Get all recurring schedules for an employee (Admin/HR)")
    public ResponseEntity<Page<RecurringScheduleResponse>> getRecurringSchedulesByEmployee(
            @RequestParam String employeeId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RecurringScheduleResponse> response = recurringService.getAllRecurringSchedulesByEmployee(
            employeeId, includeInactive, page, size
        );
        return ResponseEntity.ok(response);
    }
}
