package com.dental.clinic.management.working_schedule.mapper;

import com.dental.clinic.management.working_schedule.domain.ShiftRenewalRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRenewalResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
     * Example: "Monday, Wednesday (MORNING)"
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

        String days = entity.getExpiringRegistration().getRegistrationDays().stream()
                .map(rd -> rd.getId().getDayOfWeek())
                .map(this::formatDayOfWeek)
                .collect(Collectors.joining(", "));

        String slot = entity.getExpiringRegistration().getSlotId();

        return String.format("%s (%s)", days, slot);
    }

    /**
     * Format day of week enum to readable string.
     *
     * @param day the day of week enum
     * @return formatted day
     */
    private String formatDayOfWeek(com.dental.clinic.management.working_schedule.enums.DayOfWeek day) {
        if (day == null) {
            return "";
        }

        String dayName = day.name();
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();
    }
}
