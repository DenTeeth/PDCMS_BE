package com.dental.clinic.management.workforce_management.domain.service;

import com.dental.clinic.management.workforce_management.application.dto.CreateWorkShiftRequest;
import com.dental.clinic.management.workforce_management.application.dto.WorkShiftResponse;

import java.util.List;

/**
 * Domain service interface for work shift business logic.
 */
public interface WorkShiftService {

    /**
     * Create a new work shift.
     * @param request the create request
     * @return WorkShiftResponse
     */
    WorkShiftResponse createWorkShift(CreateWorkShiftRequest request);

    /**
     * Get all active work shifts.
     * @return List of active work shifts
     */
    List<WorkShiftResponse> getAllActiveWorkShifts();

    /**
     * Get work shift by ID.
     * @param workShiftId the work shift ID
     * @return WorkShiftResponse
     */
    WorkShiftResponse getWorkShiftById(String workShiftId);

    /**
     * Update work shift.
     * @param workShiftId the work shift ID
     * @param request the update request
     * @return WorkShiftResponse
     */
    WorkShiftResponse updateWorkShift(String workShiftId, CreateWorkShiftRequest request);

    /**
     * Deactivate work shift.
     * @param workShiftId the work shift ID
     */
    void deactivateWorkShift(String workShiftId);
}
