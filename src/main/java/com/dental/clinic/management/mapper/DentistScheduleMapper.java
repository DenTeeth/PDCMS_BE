package com.dental.clinic.management.mapper;

import com.dental.clinic.management.domain.DentistWorkSchedule;
import com.dental.clinic.management.dto.response.DentistScheduleResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mapper for DentistWorkSchedule entity and DTOs.
 */
@Component
public class DentistScheduleMapper {

    /**
     * Convert DentistWorkSchedule entity to response DTO.
     * Includes lazy-loaded dentist details if available.
     * 
     * @param entity DentistWorkSchedule entity
     * @return DentistScheduleResponse DTO
     */
    public DentistScheduleResponse toResponse(DentistWorkSchedule entity) {
        if (entity == null) {
            return null;
        }

        DentistScheduleResponse response = new DentistScheduleResponse();
        response.setScheduleId(entity.getScheduleId());
        response.setScheduleCode(entity.getScheduleCode());
        response.setDentistId(entity.getDentistId());
        response.setWorkDate(entity.getWorkDate());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        
        // Calculate duration
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            Duration duration = Duration.between(entity.getStartTime(), entity.getEndTime());
            response.setDurationHours((int) duration.toHours());
        }
        
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());

        // Lazy-load dentist details only if needed (avoid N+1 query)
        if (entity.getDentist() != null) {
            response.setDentistCode(entity.getDentist().getEmployeeCode());
            response.setDentistName(entity.getDentist().getFullName());
        }

        return response;
    }
}
