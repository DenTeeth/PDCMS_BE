package com.dental.clinic.management.work_schedule.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.work_schedule.domain.WorkShift;
import com.dental.clinic.management.work_schedule.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.work_schedule.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.work_schedule.dto.response.WorkShiftResponse;

/**
 * Mapper for converting between WorkShift entity and DTOs.
 */
@Component
public class WorkShiftMapper {

    /**
     * Convert CreateWorkShiftRequest to WorkShift entity.
     * Note: workShiftId will be set by the service layer.
     */
    public WorkShift toEntity(CreateWorkShiftRequest request) {
        WorkShift workShift = new WorkShift();
        // workShiftId will be set by service after generation
        workShift.setShiftName(request.getShiftName());
        workShift.setStartTime(request.getStartTime());
        workShift.setEndTime(request.getEndTime());
        workShift.setCategory(request.getCategory());
        workShift.setIsActive(true);
        return workShift;
    }

    /**
     * Update entity from UpdateWorkShiftRequest.
     * Only updates non-null fields.
     */
    public void updateEntity(WorkShift workShift, UpdateWorkShiftRequest request) {
        if (request.getShiftName() != null) {
            workShift.setShiftName(request.getShiftName());
        }
        if (request.getStartTime() != null) {
            workShift.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            workShift.setEndTime(request.getEndTime());
        }
        if (request.getCategory() != null) {
            workShift.setCategory(request.getCategory());
        }
    }

    /**
     * Convert WorkShift entity to WorkShiftResponse.
     * Includes calculated durationHours.
     */
    public WorkShiftResponse toResponse(WorkShift workShift) {
        return WorkShiftResponse.builder()
                .workShiftId(workShift.getWorkShiftId())
                .shiftName(workShift.getShiftName())
                .startTime(workShift.getStartTime())
                .endTime(workShift.getEndTime())
                .category(workShift.getCategory())
                .isActive(workShift.getIsActive())
                .durationHours(workShift.getDurationHours())
                .build();
    }
}
