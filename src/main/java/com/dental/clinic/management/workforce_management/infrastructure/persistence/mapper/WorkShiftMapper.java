package com.dental.clinic.management.workforce_management.infrastructure.persistence.mapper;

import com.dental.clinic.management.workforce_management.application.dto.CreateWorkShiftRequest;
import com.dental.clinic.management.workforce_management.application.dto.WorkShiftResponse;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.WorkShift;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between WorkShift entity and DTOs.
 */
@Component
public class WorkShiftMapper {

    /**
     * Convert CreateWorkShiftRequest to WorkShift entity.
     * @param request the request DTO
     * @return WorkShift entity
     */
    public WorkShift toEntity(CreateWorkShiftRequest request) {
        if (request == null) {
            return null;
        }

        WorkShift workShift = new WorkShift();
        workShift.setShiftName(request.getShiftName());
        workShift.setStartTime(request.getStartTime());
        workShift.setEndTime(request.getEndTime());
        workShift.setCategory(request.getCategory());
        workShift.setIsActive(true); // Default to active

        return workShift;
    }

    /**
     * Convert WorkShift entity to WorkShiftResponse.
     * @param workShift the entity
     * @return WorkShiftResponse DTO
     */
    public WorkShiftResponse toResponse(WorkShift workShift) {
        if (workShift == null) {
            return null;
        }

        return new WorkShiftResponse(
            workShift.getWorkShiftId(),
            workShift.getShiftName(),
            workShift.getStartTime(),
            workShift.getEndTime(),
            workShift.getCategory(),
            workShift.getIsActive(),
            workShift.getDurationHours()
        );
    }
}