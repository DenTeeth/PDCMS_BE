package com.dental.clinic.management.working_schedule.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRegistrationResponse;

/**
 * Mapper for Shift Registration entity and DTOs
 */
@Component
public class ShiftRegistrationMapper {

    /**
     * Phương thức chuyển đổi Entity đã được tải tại đây sang DTO.
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
        // V2: No longer has workShiftId directly, get from partTimeSlot if needed
        response.setWorkShiftId(null); // TODO: fetch from PartTimeSlot if needed
        response.setEffectiveFrom(entity.getEffectiveFrom());
        response.setEffectiveTo(entity.getEffectiveTo());
        response.setActive(Boolean.TRUE.equals(entity.getIsActive()));

        // V2: No longer has registrationDays, each registration is for one slot (one
        // day)
        // Leave daysOfWeek empty or null
        response.setDaysOfWeek(null);

        return response;
    }
}
