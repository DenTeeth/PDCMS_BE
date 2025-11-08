package com.dental.clinic.management.working_schedule.mapper;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.response.OvertimeRequestDetailResponse;
import com.dental.clinic.management.working_schedule.dto.response.OvertimeRequestListResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between OvertimeRequest entity and DTOs.
 */
@Component
public class OvertimeRequestMapper {

    /**
     * Convert OvertimeRequest entity to detailed response DTO.
     * Includes all related information (employee, shift, approver).
     */
    public OvertimeRequestDetailResponse toDetailResponse(OvertimeRequest overtimeRequest) {
        return new OvertimeRequestDetailResponse(
                overtimeRequest.getRequestId(),
                mapEmployeeBasicInfo(overtimeRequest.getEmployee()),
                mapEmployeeBasicInfo(overtimeRequest.getRequestedBy()),
                overtimeRequest.getWorkDate(),
                mapWorkShiftInfo(overtimeRequest.getWorkShift()),
                overtimeRequest.getReason(),
                overtimeRequest.getStatus(),
                overtimeRequest.getApprovedBy() != null
                        ? mapEmployeeBasicInfo(overtimeRequest.getApprovedBy())
                        : null,
                overtimeRequest.getApprovedAt(),
                overtimeRequest.getRejectedReason(),
                overtimeRequest.getCancellationReason(),
                overtimeRequest.getCreatedAt());
    }

    /**
     * Convert OvertimeRequest entity to list response DTO.
     * Lighter version for paginated lists.
     */
    public OvertimeRequestListResponse toListResponse(OvertimeRequest overtimeRequest) {
        Employee employee = overtimeRequest.getEmployee();
        Employee requestedBy = overtimeRequest.getRequestedBy();
        WorkShift workShift = overtimeRequest.getWorkShift();

        return new OvertimeRequestListResponse(
                overtimeRequest.getRequestId(),
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                overtimeRequest.getWorkDate(),
                workShift.getWorkShiftId(),
                workShift.getShiftName(),
                overtimeRequest.getStatus(),
                requestedBy.getFirstName() + " " + requestedBy.getLastName(),
                overtimeRequest.getCreatedAt());
    }

    /**
     * Map Employee entity to basic info DTO.
     */
    private OvertimeRequestDetailResponse.EmployeeBasicInfo mapEmployeeBasicInfo(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new OvertimeRequestDetailResponse.EmployeeBasicInfo(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFirstName() + " " + employee.getLastName());
    }

    /**
     * Map WorkShift entity to work shift info DTO.
     */
    private OvertimeRequestDetailResponse.WorkShiftInfo mapWorkShiftInfo(WorkShift workShift) {
        if (workShift == null) {
            return null;
        }
        return new OvertimeRequestDetailResponse.WorkShiftInfo(
                workShift.getWorkShiftId(),
                workShift.getShiftName(),
                workShift.getStartTime(),
                workShift.getEndTime(),
                workShift.getDurationHours());
    }
}
