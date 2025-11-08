package com.dental.clinic.management.working_schedule.mapper;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.working_schedule.domain.TimeOffRequest;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffRequestResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper for TimeOffRequest entity <-> DTO conversions
 */
@Component
public class TimeOffRequestMapper {

    /**
     * Convert TimeOffRequest entity to TimeOffRequestResponse DTO
     */
    public TimeOffRequestResponse toResponse(TimeOffRequest entity) {
        if (entity == null) {
            return null;
        }

        return new TimeOffRequestResponse(
                entity.getRequestId(),
                mapEmployeeBasicInfo(entity.getEmployee()),
                mapEmployeeBasicInfo(entity.getRequestedByEmployee()),
                entity.getTimeOffTypeId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getWorkShiftId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getApprovedByEmployee() != null
                        ? mapEmployeeBasicInfo(entity.getApprovedByEmployee())
                        : null,
                entity.getApprovedAt(),
                entity.getRejectedReason(),
                entity.getCancellationReason(),
                entity.getRequestedAt());
    }

    /**
     * Map Employee entity to basic info DTO.
     */
    private TimeOffRequestResponse.EmployeeBasicInfo mapEmployeeBasicInfo(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new TimeOffRequestResponse.EmployeeBasicInfo(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFirstName() + " " + employee.getLastName());
    }
}
