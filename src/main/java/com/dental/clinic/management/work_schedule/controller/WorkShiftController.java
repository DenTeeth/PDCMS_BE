package com.dental.clinic.management.work_schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dental.clinic.management.work_schedule.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.work_schedule.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.work_schedule.dto.response.WorkShiftResponse;
import com.dental.clinic.management.work_schedule.service.WorkShiftService;

import java.util.List;

/**
 * REST controller for Work Shift Management.
 * Provides endpoints for CRUD operations on work shifts.
 */
@RestController
@RequestMapping("/api/v1/work-shifts")
@RequiredArgsConstructor
@Slf4j
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    /**
     * Create a new work shift.
     * 
     * @param request CreateWorkShiftRequest containing shift details
     * @return WorkShiftResponse with created shift information including auto-generated ID
     */
    @PostMapping

    public ResponseEntity<WorkShiftResponse> createWorkShift(
            @Valid @RequestBody CreateWorkShiftRequest request) {
        log.info("Creating work shift with name: {}", request.getShiftName());
        WorkShiftResponse response = workShiftService.createWorkShift(request);
        log.info("Work shift created successfully with ID: {}", response.getWorkShiftId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing work shift.
     * 
     * @param workShiftId The ID of the work shift to update
     * @param request UpdateWorkShiftRequest containing fields to update
     * @return WorkShiftResponse with updated shift information
     */
    @PatchMapping("/{workShiftId}")

    public ResponseEntity<WorkShiftResponse> updateWorkShift(
            @PathVariable String workShiftId,
            @Valid @RequestBody UpdateWorkShiftRequest request) {
        log.info("Updating work shift with ID: {}", workShiftId);
        WorkShiftResponse response = workShiftService.updateWorkShift(workShiftId, request);
        log.info("Work shift updated successfully: {}", workShiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft delete) a work shift.
     * 
     * @param workShiftId The ID of the work shift to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{workShiftId}")

    public ResponseEntity<Void> deleteWorkShift(@PathVariable String workShiftId) {
        log.info("Deleting work shift with ID: {}", workShiftId);
        workShiftService.deleteWorkShift(workShiftId);
        log.info("Work shift deleted successfully: {}", workShiftId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all work shifts with optional filtering by active status.
     * 
     * @param isActive Optional filter for active/inactive shifts (null = all shifts)
     * @return List of WorkShiftResponse
     */
    @GetMapping

    public ResponseEntity<List<WorkShiftResponse>> getAllWorkShifts(
            @RequestParam(required = false) Boolean isActive) {
        log.info("Fetching work shifts with isActive filter: {}", isActive);
        List<WorkShiftResponse> responses = workShiftService.getAllWorkShifts(isActive);
        log.info("Retrieved {} work shifts", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific work shift by ID.
     * 
     * @param workShiftId The ID of the work shift to retrieve
     * @return WorkShiftResponse with shift details
     */
    @GetMapping("/{workShiftId}")

    public ResponseEntity<WorkShiftResponse> getWorkShiftById(@PathVariable String workShiftId) {
        log.info("Fetching work shift with ID: {}", workShiftId);
        WorkShiftResponse response = workShiftService.getWorkShiftById(workShiftId);
        return ResponseEntity.ok(response);
    }
}
