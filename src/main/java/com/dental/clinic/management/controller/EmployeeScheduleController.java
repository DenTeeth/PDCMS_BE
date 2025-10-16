package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.UpdateEmployeeScheduleStatusRequest;
import com.dental.clinic.management.dto.response.EmployeeScheduleResponse;
import com.dental.clinic.management.service.EmployeeScheduleService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for managing employee schedules (attendance tracking).
 * HR/Admin access for status updates.
 */
@RestController
@RequestMapping("/api/v1/employee-schedules")
@Tag(name = "Employee Schedules", description = "APIs for employee attendance tracking")
public class EmployeeScheduleController {

    private final EmployeeScheduleService scheduleService;

    public EmployeeScheduleController(EmployeeScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Update employee schedule status (attendance tracking).
     * HR/Admin can mark PRESENT, LATE, ABSENT, ON_LEAVE.
     *
     * @param scheduleId Schedule ID
     * @param request    Update status request
     * @return Updated schedule response
     */
    @PatchMapping("/{scheduleId}/status")
    @ApiMessage("Trạng thái điểm danh đã được cập nhật")

    @Operation(summary = "Update attendance status", description = "Update employee attendance status (Admin/HR only)")
    public ResponseEntity<EmployeeScheduleResponse> updateScheduleStatus(
            @PathVariable String scheduleId,
            @Valid @RequestBody UpdateEmployeeScheduleStatusRequest request) {
        EmployeeScheduleResponse response = scheduleService.updateScheduleStatus(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get employee schedule by ID.
     *
     * @param scheduleId Schedule ID
     * @return Schedule response
     */
    @GetMapping("/{scheduleId}")
    @ApiMessage("Lấy thông tin lịch làm việc thành công")

    @Operation(summary = "Get employee schedule", description = "Get employee schedule by ID (Admin/HR)")
    public ResponseEntity<EmployeeScheduleResponse> getScheduleById(@PathVariable String scheduleId) {
        EmployeeScheduleResponse response = scheduleService.getEmployeeScheduleById(scheduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all schedules for an employee with date range filter.
     *
     * @param employeeId Employee ID
     * @param startDate  Start date
     * @param endDate    End date
     * @param page       Page number (default: 0)
     * @param size       Page size (default: 10, max: 100)
     * @return Page of employee schedules
     */
    @GetMapping("/employee/{employeeId}")
    @ApiMessage("Lấy lịch làm việc của nhân viên thành công")

    @Operation(summary = "Get employee schedules", description = "Get all schedules for an employee (Admin/HR)")
    public ResponseEntity<Page<EmployeeScheduleResponse>> getSchedulesByEmployee(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EmployeeScheduleResponse> response = scheduleService.getAllSchedulesByEmployee(
                employeeId, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all schedules for a specific date (daily attendance view).
     *
     * @param workDate Work date
     * @param page     Page number (default: 0)
     * @param size     Page size (default: 50, max: 100)
     * @return Page of schedules for that date
     */
    @GetMapping("/date")
    @ApiMessage("Lấy danh sách điểm danh theo ngày thành công")

    @Operation(summary = "Get schedules by date", description = "Get all employee schedules for a specific date (Admin/HR)")
    public ResponseEntity<Page<EmployeeScheduleResponse>> getSchedulesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<EmployeeScheduleResponse> response = scheduleService.getSchedulesByDate(workDate, page, size);
        return ResponseEntity.ok(response);
    }
}
