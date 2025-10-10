package com.dental.clinic.management.mapper;

import com.dental.clinic.management.domain.EmployeeSchedule;
import com.dental.clinic.management.dto.response.EmployeeScheduleResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mapper for EmployeeSchedule entity and DTOs.
 */
@Component
public class EmployeeScheduleMapper {

    /**
     * Convert EmployeeSchedule entity to response DTO.
     * Includes attendance tracking calculations.
     *
     * @param entity EmployeeSchedule entity
     * @return EmployeeScheduleResponse DTO
     */
    public EmployeeScheduleResponse toResponse(EmployeeSchedule entity) {
        if (entity == null) {
            return null;
        }

        EmployeeScheduleResponse response = new EmployeeScheduleResponse();
        response.setScheduleId(entity.getScheduleId());
        response.setScheduleCode(entity.getScheduleCode());
        response.setEmployeeId(entity.getEmployeeId());
        response.setWorkDate(entity.getWorkDate());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setActualStartTime(entity.getActualStartTime());
        response.setActualEndTime(entity.getActualEndTime());
        response.setNotes(entity.getNotes());
        response.setRecurringId(entity.getRecurringId());
        response.setDentistScheduleId(entity.getDentistScheduleId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        // Lazy-load employee details
        if (entity.getEmployee() != null) {
            response.setEmployeeCode(entity.getEmployee().getEmployeeCode());
            response.setEmployeeName(entity.getEmployee().getFullName());
        }

        // Calculate scheduled hours
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            Duration scheduledDuration = Duration.between(entity.getStartTime(), entity.getEndTime());
            response.setScheduledHours((int) scheduledDuration.toHours());
        }

        // Calculate actual hours
        if (entity.getActualStartTime() != null && entity.getActualEndTime() != null) {
            Duration actualDuration = Duration.between(entity.getActualStartTime(), entity.getActualEndTime());
            response.setActualHours((int) actualDuration.toHours());
        }

        // Calculate late minutes
        if (entity.getActualStartTime() != null && entity.getStartTime() != null) {
            if (entity.getActualStartTime().isAfter(entity.getStartTime())) {
                Duration lateDuration = Duration.between(entity.getStartTime(), entity.getActualStartTime());
                response.setLateMinutes((int) lateDuration.toMinutes());
            } else {
                response.setLateMinutes(0);
            }
        }

        // Calculate overtime minutes
        if (entity.getActualEndTime() != null && entity.getEndTime() != null) {
            if (entity.getActualEndTime().isAfter(entity.getEndTime())) {
                Duration overtimeDuration = Duration.between(entity.getEndTime(), entity.getActualEndTime());
                response.setOvertimeMinutes((int) overtimeDuration.toMinutes());
            } else {
                response.setOvertimeMinutes(0);
            }
        }

        return response;
    }
}
