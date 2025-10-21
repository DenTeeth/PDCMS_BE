package com.dental.clinic.management.time_off_request.mapper;

import com.dental.clinic.management.time_off_request.domain.TimeOffType;
import com.dental.clinic.management.time_off_request.dto.response.TimeOffTypeResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for TimeOffType entity <-> DTO conversions
 */
@Component
public class TimeOffTypeMapper {

    /**
     * Convert TimeOffType entity to TimeOffTypeResponse DTO
     */
    public TimeOffTypeResponse toResponse(TimeOffType entity) {
        if (entity == null) {
            return null;
        }

        return TimeOffTypeResponse.builder()
                .typeId(entity.getTypeId())
                .typeName(entity.getTypeName())
                .description(entity.getDescription())
                .requiresApproval(entity.getRequiresApproval())
                .isActive(entity.getIsActive())
                .build();
    }
}
