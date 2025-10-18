package com.dental.clinic.management.workforce_management.infrastructure.persistence.mapper;

import com.dental.clinic.management.workforce_management.application.dto.CreateEmployeeShiftRegistrationRequest;
import com.dental.clinic.management.workforce_management.application.dto.EmployeeShiftRegistrationResponse;
import com.dental.clinic.management.workforce_management.domain.model.RegistrationDayOfWeek;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.EmployeeShiftRegistration;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.RegistrationDay;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between EmployeeShiftRegistration entities and DTOs.
 */
@Component
public class EmployeeShiftRegistrationMapper {

    /**
     * Convert CreateEmployeeShiftRegistrationRequest to EmployeeShiftRegistration
     * entity.
     * 
     * @param request the request DTO
     * @return EmployeeShiftRegistration entity
     */
    public EmployeeShiftRegistration toEntity(CreateEmployeeShiftRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        EmployeeShiftRegistration registration = new EmployeeShiftRegistration();
        registration.setEmployeeId(request.getEmployeeId());
        registration.setSlotId(request.getSlotId());
        registration.setEffectiveFrom(request.getEffectiveFrom());
        registration.setEffectiveTo(request.getEffectiveTo());
        registration.setIsActive(true);

        return registration;
    }

    /**
     * Convert EmployeeShiftRegistration entity to
     * EmployeeShiftRegistrationResponse.
     * 
     * @param registration     the entity
     * @param registrationDays the associated registration days
     * @return EmployeeShiftRegistrationResponse DTO
     */
    public EmployeeShiftRegistrationResponse toResponse(EmployeeShiftRegistration registration,
            Set<RegistrationDay> registrationDays) {
        if (registration == null) {
            return null;
        }

        Set<RegistrationDayOfWeek> days = registrationDays.stream()
                .map(rd -> rd.getId().getDayOfWeek())
                .collect(Collectors.toSet());

        return new EmployeeShiftRegistrationResponse(
                registration.getRegistrationId(),
                registration.getEmployeeId(),
                registration.getSlotId(),
                registration.getEffectiveFrom(),
                registration.getEffectiveTo(),
                registration.getIsActive(),
                days);
    }
}
