package com.dental.clinic.management.employee_shifts.mapper;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee_shifts.domain.EmployeeShift;
import com.dental.clinic.management.employee_shifts.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.employee_shifts.dto.response.EmployeeBasicDto;
import com.dental.clinic.management.employee_shifts.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.employee_shifts.dto.response.WorkShiftBasicDto;
import com.dental.clinic.management.work_shifts.domain.WorkShift;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between EmployeeShift entity and DTOs.
 */
@Component
public class EmployeeShiftMapper {

    /**
     * Convert CreateShiftRequestDto to EmployeeShift entity.
     * Note: Employee, WorkShift, source, and createdBy will be set by the service
     * layer.
     */
    public EmployeeShift toEntity(CreateShiftRequestDto request) {
        EmployeeShift entity = new EmployeeShift();
        entity.setWorkDate(request.getWorkDate());
        entity.setNotes(request.getNotes());
        return entity;
    }

    /**
     * Update entity from UpdateShiftRequestDto.
     * Only updates non-null fields.
     */
    public void updateEntity(EmployeeShift entity, UpdateShiftRequestDto request) {
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    /**
     * Convert EmployeeShift entity to EmployeeShiftResponseDto.
     * Includes nested employee and work shift information.
     */
    public EmployeeShiftResponseDto toResponse(EmployeeShift entity) {
        if (entity == null) {
            return null;
        }

        return EmployeeShiftResponseDto.builder()
                .employeeShiftId(entity.getId())
                .workDate(entity.getWorkDate().toString())
                .status(entity.getStatus().name())
                .employee(toEmployeeBasicDto(entity.getEmployee()))
                .workShift(toWorkShiftBasicDto(entity.getWorkShift()))
                .notes(entity.getNotes())
                .source(entity.getSource() != null ? entity.getSource().name() : null)
                .isOvertime(entity.getIsOvertime())
                .build();
    }

    /**
     * Convert Employee entity to basic DTO.
     */
    private EmployeeBasicDto toEmployeeBasicDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeBasicDto.builder()
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .build();
    }

    /**
     * Convert WorkShift entity to basic DTO.
     */
    private WorkShiftBasicDto toWorkShiftBasicDto(WorkShift workShift) {
        if (workShift == null) {
            return null;
        }

        return WorkShiftBasicDto.builder()
                .workShiftId(workShift.getWorkShiftId())
                .shiftName(workShift.getShiftName())
                .build();
    }
}
