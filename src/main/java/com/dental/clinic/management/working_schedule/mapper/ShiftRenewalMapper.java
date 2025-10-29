package com.dental.clinic.management.working_schedule.mapper;

import com.dental.clinic.management.working_schedule.domain.ShiftRenewalRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRenewalResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for ShiftRenewalRequest entity.
 */
@Component
public class ShiftRenewalMapper {

    /**
     * Map ShiftRenewalRequest entity to response DTO.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public ShiftRenewalResponse toResponse(ShiftRenewalRequest entity) {
        if (entity == null) {
            return null;
        }

        String shiftDetails = buildShiftDetails(entity);

        return ShiftRenewalResponse.builder()
                .renewalId(entity.getRenewalId())
                .expiringRegistrationId(entity.getExpiringRegistration().getRegistrationId() != null
                        ? Integer.parseInt(entity.getExpiringRegistration().getRegistrationId())
                        : null)
                .employeeId(entity.getEmployee().getEmployeeId())
                .employeeName(entity.getEmployee().getFullName())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .expiresAt(entity.getExpiresAt())
                .confirmedAt(entity.getConfirmedAt())
                .createdAt(entity.getCreatedAt())
                .effectiveFrom(entity.getExpiringRegistration().getEffectiveFrom())
                .effectiveTo(entity.getExpiringRegistration().getEffectiveTo())
                .shiftDetails(shiftDetails)
                .build();
    }

    /**
     * Build shift details string from registration days.
     * Example: "Slot ID: 123"
     *
     * @param entity the renewal request entity
     * @return formatted shift details
     */
    private String buildShiftDetails(ShiftRenewalRequest entity) {
        if (entity.getExpiringRegistration() == null ||
                entity.getExpiringRegistration().getPartTimeSlotId() == null) {
            return "N/A";
        }

        // V2: Get slot details instead of registration days
        // This would require injecting PartTimeSlotRepository and WorkShiftRepository
        // For now, return simple format
        return String.format("Slot ID: %d", entity.getExpiringRegistration().getPartTimeSlotId());
    }
}
