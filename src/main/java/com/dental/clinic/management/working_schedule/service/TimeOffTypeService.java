package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.exception.validation.DuplicateTypeCodeException;
import com.dental.clinic.management.exception.time_off.TimeOffTypeInUseException;
import com.dental.clinic.management.exception.time_off.TimeOffTypeNotFoundException;
import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.domain.TimeOffType;
import com.dental.clinic.management.working_schedule.dto.request.CreateTimeOffTypeRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateTimeOffTypeRequest;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffTypeResponse;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.mapper.TimeOffTypeMapper;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing time-off types
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimeOffTypeService {

    private final TimeOffTypeRepository typeRepository;
    private final TimeOffRequestRepository requestRepository;
    private final TimeOffTypeMapper typeMapper;
    private final IdGenerator idGenerator;

    /**
     * GET /api/v1/time-off-types
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ cÃƒÂ¡c loÃ¡ÂºÂ¡i hÃƒÂ¬nh nghÃ¡Â»â€° phÃƒÂ©p Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng
     */
    public List<TimeOffTypeResponse> getActiveTimeOffTypes() {
        log.debug("Request to get all active time-off types");

        return typeRepository.findByIsActiveTrue()
                .stream()
                .map(typeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/v1/admin/time-off-types
     * LÃ¡ÂºÂ¥y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p (bao gÃ¡Â»â€œm inactive) - Admin view
     * 
     * @param isActive filter by active status (null = all, true = active only, false = inactive only)
     * @param isPaid filter by paid status (null = all, true = paid only, false = unpaid only)
     */
    public List<TimeOffTypeResponse> getAllTimeOffTypes(Boolean isActive, Boolean isPaid) {
        log.debug("Admin request to get all time-off types, isActive={}, isPaid={}", isActive, isPaid);

        List<TimeOffType> types;
        
        // Apply filters
        if (isActive == null && isPaid == null) {
            // No filters - get all
            types = typeRepository.findAll();
        } else if (isActive != null && isPaid != null) {
            // Both filters
            types = typeRepository.findByIsActiveAndIsPaid(isActive, isPaid);
        } else if (isActive != null) {
            // Only isActive filter
            types = typeRepository.findByIsActive(isActive);
        } else {
            // Only isPaid filter
            types = typeRepository.findByIsPaid(isPaid);
        }

        return types.stream()
                .map(typeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/v1/admin/time-off-types/{typeId}
     * LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t mÃ¡Â»â„¢t loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     */
    public TimeOffTypeResponse getTimeOffTypeById(String typeId) {
        log.debug("Request to get time-off type by ID: {}", typeId);

        TimeOffType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new TimeOffTypeNotFoundException(typeId));

        return typeMapper.toResponse(type);
    }

    /**
     * POST /api/v1/admin/time-off-types
     * TÃ¡ÂºÂ¡o loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p mÃ¡Â»â€ºi
     */
    @Transactional
    public TimeOffTypeResponse createTimeOffType(CreateTimeOffTypeRequest request) {
        log.debug("Request to create time-off type: {}", request.getTypeCode());

        // Validate unique type_code
        if (typeRepository.existsByTypeCode(request.getTypeCode())) {
            throw new DuplicateTypeCodeException(request.getTypeCode());
        }

        // Validate logic: requiresBalance vÃƒÂ  defaultDaysPerYear phÃ¡ÂºÂ£i match
        validateBalanceAndDefaultDays(request.getRequiresBalance(), request.getDefaultDaysPerYear());

        String typeId = idGenerator.generateId("TOT");

        TimeOffType type = TimeOffType.builder()
                .typeId(typeId)
                .typeCode(request.getTypeCode())
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .requiresBalance(request.getRequiresBalance())
                .defaultDaysPerYear(request.getDefaultDaysPerYear())
                .isPaid(request.getIsPaid())
                .requiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : true)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        TimeOffType saved = typeRepository.save(type);
        log.info("Created time-off type: {}", saved.getTypeId());

        return typeMapper.toResponse(saved);
    }

    /**
     * PATCH /api/v1/admin/time-off-types/{typeId}
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     */
    @Transactional
    public TimeOffTypeResponse updateTimeOffType(String typeId, UpdateTimeOffTypeRequest request) {
        log.debug("Request to update time-off type: {}", typeId);

        TimeOffType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new TimeOffTypeNotFoundException(typeId));

        // Update type_code if provided and different
        if (request.getTypeCode() != null && !request.getTypeCode().equals(type.getTypeCode())) {
            // Check unique (excluding current type)
            if (typeRepository.existsByTypeCodeAndTypeIdNot(request.getTypeCode(), typeId)) {
                throw new DuplicateTypeCodeException(request.getTypeCode());
            }
            type.setTypeCode(request.getTypeCode());
        }

        if (request.getTypeName() != null) {
            type.setTypeName(request.getTypeName());
        }

        if (request.getDescription() != null) {
            type.setDescription(request.getDescription());
        }

        if (request.getRequiresBalance() != null) {
            type.setRequiresBalance(request.getRequiresBalance());
        }

        if (request.getDefaultDaysPerYear() != null) {
            type.setDefaultDaysPerYear(request.getDefaultDaysPerYear());
        }

        if (request.getIsPaid() != null) {
            type.setIsPaid(request.getIsPaid());
        }

        if (request.getRequiresApproval() != null) {
            type.setRequiresApproval(request.getRequiresApproval());
        }

        if (request.getIsActive() != null) {
            type.setIsActive(request.getIsActive());
        }

        // Validate logic AFTER all updates: requiresBalance vÃƒÂ  defaultDaysPerYear phÃ¡ÂºÂ£i match
        validateBalanceAndDefaultDays(type.getRequiresBalance(), type.getDefaultDaysPerYear());

        TimeOffType updated = typeRepository.save(type);
        log.info("Updated time-off type: {}", typeId);

        return typeMapper.toResponse(updated);
    }

    /**
     * Helper method: Validate requiresBalance vÃƒÂ  defaultDaysPerYear logic
     * 
     * Business Rules:
     * - requiresBalance = true  Ã¢â€ â€™ defaultDaysPerYear PHÃ¡ÂºÂ¢I cÃƒÂ³ giÃƒÂ¡ trÃ¡Â»â€¹ (Ã„â€˜Ã¡Â»Æ’ dÃƒÂ¹ng cho annual reset)
     * - requiresBalance = false Ã¢â€ â€™ defaultDaysPerYear PHÃ¡ÂºÂ¢I null (vÃƒÂ¬ khÃƒÂ´ng cÃ¡ÂºÂ§n balance tracking)
     */
    private void validateBalanceAndDefaultDays(Boolean requiresBalance, Double defaultDaysPerYear) {
        // Case 1: requiresBalance = true VÃƒâ‚¬ defaultDaysPerYear = null
        if (Boolean.TRUE.equals(requiresBalance) && defaultDaysPerYear == null) {
            throw new BadRequestAlertException(
                "LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p cÃ¡ÂºÂ§n balance tracking (requiresBalance = true) PHÃ¡ÂºÂ¢I cÃƒÂ³ defaultDaysPerYear Ã„â€˜Ã¡Â»Æ’ sÃ¡Â»Â­ dÃ¡Â»Â¥ng cho annual reset. " +
                "Vui lÃƒÂ²ng set defaultDaysPerYear (vÃƒÂ­ dÃ¡Â»Â¥: 12.0 cho 12 ngÃƒÂ y phÃƒÂ©p/nÃ„Æ’m).",
                "TimeOffType",
                "MISSING_DEFAULT_DAYS"
            );
        }

        // Case 2: requiresBalance = false VÃƒâ‚¬ defaultDaysPerYear != null
        if (Boolean.FALSE.equals(requiresBalance) && defaultDaysPerYear != null) {
            throw new BadRequestAlertException(
                "LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p khÃƒÂ´ng cÃ¡ÂºÂ§n balance tracking (requiresBalance = false) KHÃƒâ€NG thÃ¡Â»Æ’ cÃƒÂ³ defaultDaysPerYear. " +
                "Field defaultDaysPerYear chÃ¡Â»â€° dÃƒÂ¹ng cho cÃƒÂ¡c loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p cÃ¡ÂºÂ§n check sÃ¡Â»â€˜ dÃ†Â° (requiresBalance = true). " +
                "Vui lÃƒÂ²ng set defaultDaysPerYear = null.",
                "TimeOffType",
                "INVALID_DEFAULT_DAYS"
            );
        }
    }

    /**
     * DELETE /api/v1/admin/time-off-types/{typeId}
     * VÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a/KÃƒÂ­ch hoÃ¡ÂºÂ¡t lÃ¡ÂºÂ¡i loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p (soft delete)
     */
    @Transactional
    public TimeOffTypeResponse toggleTimeOffTypeActive(String typeId) {
        log.debug("Request to toggle time-off type active status: {}", typeId);

        TimeOffType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new TimeOffTypeNotFoundException(typeId));

        // If deactivating, check for pending requests
        if (type.getIsActive()) {
            boolean hasPendingRequests = requestRepository
                    .existsByTimeOffTypeIdAndStatus(typeId, TimeOffStatus.PENDING);

            if (hasPendingRequests) {
                throw new TimeOffTypeInUseException(typeId);
            }
        }

        // Toggle status
        type.setIsActive(!type.getIsActive());
        TimeOffType updated = typeRepository.save(type);

        log.info("Toggled time-off type {} to is_active={}", typeId, updated.getIsActive());

        return typeMapper.toResponse(updated);
    }
}
