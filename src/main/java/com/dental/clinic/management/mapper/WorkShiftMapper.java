package com.dental.clinic.management.mapper;

import com.dental.clinic.management.domain.WorkShift;
import com.dental.clinic.management.dto.response.WorkShiftResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mapper for WorkShift entity and DTOs.
 */
@Component
public class WorkShiftMapper {

    /**
     * Convert WorkShift entity to response DTO.
     * 
     * @param entity WorkShift entity
     * @return WorkShiftResponse DTO
     */
    public WorkShiftResponse toResponse(WorkShift entity) {
        if (entity == null) {
            return null;
        }

        WorkShiftResponse response = new WorkShiftResponse();
        response.setShiftId(entity.getShiftId());
        response.setShiftCode(entity.getShiftCode());
        response.setShiftName(entity.getShiftName());
        response.setShiftType(entity.getShiftType() != null ? entity.getShiftType().name() : null);
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        
        // Calculate duration
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            Duration duration = Duration.between(entity.getStartTime(), entity.getEndTime());
            response.setDurationHours((int) duration.toHours());
        }
        
        response.setIsActive(entity.getIsActive());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());

        return response;
    }
}
