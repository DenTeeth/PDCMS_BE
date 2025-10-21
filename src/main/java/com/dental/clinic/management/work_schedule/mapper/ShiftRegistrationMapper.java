package com.dental.clinic.management.work_schedule.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.work_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.work_schedule.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.work_schedule.enums.DayOfWeek;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Shift Registration entity and DTOs
 */
@Component
public class ShiftRegistrationMapper {

    /**
     * Phương thức chuyển đổi Entity đã được tải đầy đủ sang DTO.
     * 
     * @param entity Đối tượng EmployeeShiftRegistration (đã có sẵn registrationDays
     *               nhờ @EntityGraph)
     * @return ShiftRegistrationResponse DTO
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
        if (entity.getRegistrationDays() != null && !entity.getRegistrationDays().isEmpty()) {
            List<DayOfWeek> daysOfWeek = entity.getRegistrationDays().stream()
                    .map(rd -> rd.getId().getDayOfWeek())
                    .collect(Collectors.toList());
            response.setDaysOfWeek(daysOfWeek);
        }

        return response;
    }
}
