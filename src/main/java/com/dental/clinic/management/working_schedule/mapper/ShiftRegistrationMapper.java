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
     * PhÃ†Â°Ã†Â¡ng thÃ¡Â»Â©c chuyÃ¡Â»Æ’n Ã„â€˜Ã¡Â»â€¢i Entity Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ£i Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§ sang DTO.
     * 
     * @param entity Ã„ÂÃ¡Â»â€˜i tÃ†Â°Ã¡Â»Â£ng EmployeeShiftRegistration (Ã„â€˜ÃƒÂ£ cÃƒÂ³ sÃ¡ÂºÂµn registrationDays
     *               nhÃ¡Â»Â @EntityGraph)
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

        // V2: No longer has registrationDays, each registration is for one slot (one day)
        // Leave daysOfWeek empty or null
        response.setDaysOfWeek(null);

        return response;
    }
}
