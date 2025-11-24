package com.dental.clinic.management.working_schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dental.clinic.management.working_schedule.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.response.WorkShiftResponse;
import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;
import com.dental.clinic.management.working_schedule.service.WorkShiftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST controller for Work Shift Management.
 * Provides endpoints for CRUD operations on work shifts.
 */
@RestController
@RequestMapping("/api/v1/work-shifts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Work Shift Management", description = "APIs for managing work shifts (CRUD, filtering, reactivation)")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    /**
     * Create a new work shift.
     * 
     * @param request CreateWorkShiftRequest containing shift details
     * @return WorkShiftResponse with created shift information including auto-generated ID
     */
    @PostMapping
    @Operation(summary = "Create a new work shift", description = "Create a new work shift with the provided details.")
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
     * @return WorkShiftResponse with updated shift information
     */
    @PatchMapping("/{workShiftId}")
    @Operation(summary = "Update an existing work shift", description = "Update fields of an existing work shift by ID.")
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
    @Operation(summary = "Delete a work shift", description = "Soft delete a work shift by ID.")
    public ResponseEntity<Void> deleteWorkShift(@PathVariable String workShiftId) {
        log.info("Deleting work shift with ID: {}", workShiftId);
        workShiftService.deleteWorkShift(workShiftId);
        log.info("Work shift deleted successfully: {}", workShiftId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivate a soft-deleted work shift (Issue 7).
     * 
     * @param workShiftId The ID of the work shift to reactivate
     * @return WorkShiftResponse with reactivated shift information
     */
    @PutMapping("/{workShiftId}/reactivate")
    @Operation(summary = "Reactivate a work shift", description = "Reactivate a soft-deleted work shift by ID.")
    public ResponseEntity<WorkShiftResponse> reactivateWorkShift(@PathVariable String workShiftId) {
        log.info("Reactivating work shift with ID: {}", workShiftId);
        WorkShiftResponse response = workShiftService.reactivateWorkShift(workShiftId);
        log.info("Work shift reactivated successfully: {}", workShiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all work shifts with advanced filtering, searching, and sorting (Issues 10, 11, 12).
     * 
     * @param isActive Optional filter for active/inactive shifts (null = all shifts)
     * @param category Optional filter by category (NORMAL/NIGHT)
     * @param search Optional search keyword for shift name (case-insensitive)
     * @param sortBy Optional sort field (startTime, category, shiftName). Default: startTime
     * @param sortDirection Optional sort direction (ASC/DESC). Default: ASC
     * @return List of WorkShiftResponse
     */
    @GetMapping
    @Operation(summary = "Get all work shifts", description = "Retrieve all work shifts with optional filtering, searching, and sorting.")
    public ResponseEntity<List<WorkShiftResponse>> getAllWorkShifts(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) WorkShiftCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching work shifts - isActive: {}, category: {}, search: '{}', sortBy: {}, sortDirection: {}", 
                 isActive, category, search, sortBy, sortDirection);
        List<WorkShiftResponse> responses = workShiftService.getAllWorkShifts(
                isActive, category, search, sortBy, sortDirection);
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
    @Operation(summary = "Get work shift by ID", description = "Retrieve a specific work shift by its ID.")
    public ResponseEntity<WorkShiftResponse> getWorkShiftById(@PathVariable String workShiftId) {
        log.info("Fetching work shift with ID: {}", workShiftId);
        WorkShiftResponse response = workShiftService.getWorkShiftById(workShiftId);
        return ResponseEntity.ok(response);
    }
}
