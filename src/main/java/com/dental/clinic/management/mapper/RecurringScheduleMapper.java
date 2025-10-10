package com.dental.clinic.management.mapper;

import com.dental.clinic.management.domain.RecurringSchedule;
import com.dental.clinic.management.dto.response.RecurringScheduleResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Mapper for RecurringSchedule entity and DTOs.
 */
@Component
public class RecurringScheduleMapper {

    /**
     * Convert RecurringSchedule entity to response DTO.
     * Resolves shift times from either predefined shift or custom times.
     *
     * @param entity RecurringSchedule entity
     * @return RecurringScheduleResponse DTO
     */
    public RecurringScheduleResponse toResponse(RecurringSchedule entity) {
        if (entity == null) {
            return null;
        }

        RecurringScheduleResponse response = new RecurringScheduleResponse();
        response.setRecurringId(entity.getRecurringId());
        response.setRecurringCode(entity.getRecurringCode());
        response.setEmployeeId(entity.getEmployeeId());
        response.setDayOfWeek(entity.getDayOfWeek() != null ? entity.getDayOfWeek().name() : null);
        response.setShiftId(entity.getShiftId());
        response.setShiftType(entity.getShiftType() != null ? entity.getShiftType().name() : null);
        response.setIsActive(entity.getIsActive());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());

        // Lazy-load employee details
        if (entity.getEmployee() != null) {
            response.setEmployeeCode(entity.getEmployee().getEmployeeCode());
            response.setEmployeeName(entity.getEmployee().getFullName());
        }

        // Resolve shift times: either from predefined shift or custom times
        LocalTime startTime;
        LocalTime endTime;

        if (entity.getWorkShift() != null) {
            // Use predefined shift times
            startTime = entity.getWorkShift().getStartTime();
            endTime = entity.getWorkShift().getEndTime();
            response.setShiftCode(entity.getWorkShift().getShiftCode());
            response.setShiftName(entity.getWorkShift().getShiftName());
        } else {
            // Use custom times
            startTime = entity.getStartTime();
            endTime = entity.getEndTime();
        }

        response.setStartTime(startTime);
        response.setEndTime(endTime);

        // Calculate duration
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            response.setDurationHours((int) duration.toHours());
        }

        return response;
    }
}
