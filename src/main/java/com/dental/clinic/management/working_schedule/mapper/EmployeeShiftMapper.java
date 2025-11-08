package com.dental.clinic.management.working_schedule.mapper;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeShiftResponseDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for EmployeeShift entity to DTOs.
 */
@Component
public class EmployeeShiftMapper {

    private final AccountRepository accountRepository;

    public EmployeeShiftMapper(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Convert EmployeeShift entity to response DTO.
     * 
     * @param shift employee shift entity
     * @return response DTO
     */
    public EmployeeShiftResponseDto toResponseDto(EmployeeShift shift) {
        if (shift == null) {
            return null;
        }

        // Fetch creator name
        String createdByName = null;
        if (shift.getCreatedBy() != null) {
            createdByName = accountRepository.findById(shift.getCreatedBy())
                    .map(account -> {
                        if (account.getEmployee() != null) {
                            return account.getEmployee().getFullName();
                        }
                        return account.getUsername();
                    })
                    .orElse("Unknown");
        }

        return new EmployeeShiftResponseDto(
                shift.getEmployeeShiftId(),
                mapEmployeeBasic(shift),
                shift.getWorkDate(),
                mapWorkShiftBasic(shift),
                shift.getSource(),
                shift.getStatus(),
                shift.getIsOvertime(),
                shift.getCreatedBy(),
                createdByName,
                shift.getSourceOtRequestId(),
                shift.getSourceOffRequestId(),
                shift.getNotes(),
                shift.getCreatedAt(),
                shift.getUpdatedAt());
    }

    /**
     * Map employee basic information.
     */
    private EmployeeShiftResponseDto.EmployeeBasicDto mapEmployeeBasic(EmployeeShift shift) {
        if (shift.getEmployee() == null) {
            return null;
        }

        return new EmployeeShiftResponseDto.EmployeeBasicDto(
                shift.getEmployee().getEmployeeId(),
                shift.getEmployee().getFullName(),
                shift.getEmployee().getEmploymentType() != null
                        ? shift.getEmployee().getEmploymentType().name()
                        : null);
    }

    /**
     * Map work shift basic information.
     */
    private EmployeeShiftResponseDto.WorkShiftBasicDto mapWorkShiftBasic(EmployeeShift shift) {
        if (shift.getWorkShift() == null) {
            return null;
        }

        return new EmployeeShiftResponseDto.WorkShiftBasicDto(
                shift.getWorkShift().getWorkShiftId(),
                shift.getWorkShift().getShiftName(),
                shift.getWorkShift().getStartTime() != null
                        ? shift.getWorkShift().getStartTime().toString()
                        : null,
                shift.getWorkShift().getEndTime() != null
                        ? shift.getWorkShift().getEndTime().toString()
                        : null);
    }
}
