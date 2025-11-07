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

        return new ShiftRenewalResponse(
                entity.getRenewalId(),
                entity.getExpiringRegistration().getRegistrationId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getFullName(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getConfirmedAt(),
                entity.getCreatedAt(),
                entity.getDeclineReason(),
                entity.getExpiringRegistration().getEffectiveFrom(),
                entity.getExpiringRegistration().getEffectiveTo(),
                null, // workShiftName - not set in original
                shiftDetails,
                null // message - not set in original
        );
    }

    /**
     * Build shift details string from registration days.
     * Example: "Monday, Wednesday, Friday - Morning Shift"
     *
     * @param entity the renewal request entity
     * @return formatted shift details
     */
    private String buildShiftDetails(ShiftRenewalRequest entity) {
        if (entity.getExpiringRegistration() == null ||
                entity.getExpiringRegistration().getRegistrationDays() == null ||
                entity.getExpiringRegistration().getRegistrationDays().isEmpty()) {
            return "N/A";
        }

        // Build details from FixedShiftRegistration's days
        String days = entity.getExpiringRegistration().getRegistrationDays().stream()
                .map(day -> day.getDayOfWeek().toString())
                .collect(java.util.stream.Collectors.joining(", "));

        String shiftName = entity.getExpiringRegistration().getWorkShift() != null
                ? entity.getExpiringRegistration().getWorkShift().getShiftName()
                : "Unknown Shift";

        return String.format("%s - %s", days, shiftName);
    }
}
