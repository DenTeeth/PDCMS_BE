package com.dental.clinic.management.employee_shift_registrations.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.employee_shift_registrations.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.employee_shift_registrations.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.employee_shift_registrations.enums.DayOfWeek;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Shift Registration entity and DTOs
 */
@Component
public class ShiftRegistrationMapper {

    /**
     * Convert Shift Registration entity to ShiftRegistrationResponse DTO
     * Phương thức này giờ chỉ cần 1 tham số vì `registrationDays` đã có sẵn
     * trong entity nhờ @EntityGraph.
     *
     * @param shiftRegistration the entity
     */
    public ShiftRegistrationResponse toShiftRegistrationResponse(EmployeeShiftRegistration entity) {
        if (entity == null) {
            return null;
        }

        ShiftRegistrationResponse response = new ShiftRegistrationResponse();
        response.setRegistrationId(entity.getRegistrationId());
        response.setEmployeeId(entity.getEmployeeId());
        response.setSlotId(entity.getSlotId());
        response.setEffectiveFrom(entity.getEffectiveFrom());
        response.setEffectiveTo(entity.getEffectiveTo());
        response.setActive(Boolean.TRUE.equals(entity.getIsActive()));

        // Lấy danh sách days trực tiếp từ entity
        if (entity.getRegistrationDays() != null) {
            List<DayOfWeek> daysOfWeek = entity.getRegistrationDays().stream()
                    .map(rd -> rd.getId().getDayOfWeek())
                    .collect(Collectors.toList());
            response.setDaysOfWeek(daysOfWeek);
        }

        return response;
    }
}
