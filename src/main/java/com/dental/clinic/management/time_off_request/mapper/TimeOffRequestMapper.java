package com.dental.clinic.management.time_off_request.mapper;

import com.dental.clinic.management.time_off_request.domain.TimeOffRequest;
import com.dental.clinic.management.time_off_request.dto.response.TimeOffRequestResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for TimeOffRequest entity <-> DTO conversions
 */
@Component
public class TimeOffRequestMapper {

    /**
     * Convert TimeOffRequest entity to TimeOffRequestResponse DTO
     */
    public TimeOffRequestResponse toResponse(TimeOffRequest entity) {
        if (entity == null) {
            return null;
        }

        return TimeOffRequestResponse.builder()
                .requestId(entity.getRequestId())
                .employeeId(entity.getEmployeeId())
                .timeOffTypeId(entity.getTimeOffTypeId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .slotId(entity.getSlotId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedBy(entity.getRequestedBy())
                .requestedAt(entity.getRequestedAt())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .rejectedReason(entity.getRejectedReason())
                .cancellationReason(entity.getCancellationReason())
                .build();
    }
}
