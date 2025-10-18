package com.dental.clinic.management.workforce_management.infrastructure.web;

import com.dental.clinic.management.workforce_management.application.dto.CreateWorkShiftRequest;
import com.dental.clinic.management.workforce_management.application.dto.WorkShiftResponse;
import com.dental.clinic.management.workforce_management.application.usecase.WorkShiftUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Work Shift management.
 */
@RestController
@RequestMapping("/api/workforce/work-shifts")
@RequiredArgsConstructor
public class WorkShiftController {

    private final WorkShiftUseCase workShiftUseCase;

    /**
     * Create a new work shift.
     * @param request the create request
     * @return ResponseEntity with created work shift
     */
    @PostMapping
    public ResponseEntity<WorkShiftResponse> createWorkShift(@Valid @RequestBody CreateWorkShiftRequest request) {
        WorkShiftResponse response = workShiftUseCase.createWorkShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all active work shifts.
     * @return List of active work shifts
     */
    @GetMapping
    public ResponseEntity<List<WorkShiftResponse>> getAllActiveWorkShifts() {
        List<WorkShiftResponse> responses = workShiftUseCase.getAllActiveWorkShifts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get work shift by ID.
     * @param workShiftId the work shift ID
     * @return ResponseEntity with work shift data
     */
    @GetMapping("/{workShiftId}")
    public ResponseEntity<WorkShiftResponse> getWorkShiftById(@PathVariable String workShiftId) {
        WorkShiftResponse response = workShiftUseCase.getWorkShiftById(workShiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update work shift.
     * @param workShiftId the work shift ID
     * @param request the update request
     * @return ResponseEntity with updated work shift
     */
    @PutMapping("/{workShiftId}")
    public ResponseEntity<WorkShiftResponse> updateWorkShift(
            @PathVariable String workShiftId,
            @Valid @RequestBody CreateWorkShiftRequest request) {
        WorkShiftResponse response = workShiftUseCase.updateWorkShift(workShiftId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate work shift.
     * @param workShiftId the work shift ID
     * @return ResponseEntity
     */
    @DeleteMapping("/{workShiftId}")
    public ResponseEntity<Void> deactivateWorkShift(@PathVariable String workShiftId) {
        workShiftUseCase.deactivateWorkShift(workShiftId);
        return ResponseEntity.noContent().build();
    }
}