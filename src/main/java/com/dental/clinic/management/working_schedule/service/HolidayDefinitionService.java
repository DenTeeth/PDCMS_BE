package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.working_schedule.domain.HolidayDefinition;
import com.dental.clinic.management.working_schedule.dto.request.HolidayDefinitionRequest;
import com.dental.clinic.management.working_schedule.dto.response.HolidayDefinitionResponse;
import com.dental.clinic.management.working_schedule.enums.HolidayType;
import com.dental.clinic.management.working_schedule.mapper.HolidayDefinitionMapper;
import com.dental.clinic.management.working_schedule.repository.HolidayDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing holiday definitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HolidayDefinitionService {

    private final HolidayDefinitionRepository holidayDefinitionRepository;
    private final HolidayDefinitionMapper holidayDefinitionMapper;

    /**
     * Create a new holiday definition.
     */
    public HolidayDefinitionResponse createHolidayDefinition(HolidayDefinitionRequest request) {
        log.info("Creating holiday definition: {}", request.getHolidayName());

        // Check if holiday name already exists
        if (holidayDefinitionRepository.existsByHolidayName(request.getHolidayName())) {
            throw new com.dental.clinic.management.exception.holiday.DuplicateHolidayDefinitionException(
                request.getHolidayName());
        }

        // Generate unique definition ID: HDF_001, HDF_002, etc.
        String definitionId = generateDefinitionId();
        log.info("Generated definition ID: {}", definitionId);

        HolidayDefinition definition = holidayDefinitionMapper.toEntity(request);
        definition.setDefinitionId(definitionId);
        HolidayDefinition savedDefinition = holidayDefinitionRepository.save(definition);

        log.info("Holiday definition created successfully: {}", savedDefinition.getDefinitionId());
        return holidayDefinitionMapper.toResponse(savedDefinition);
    }

    /**
     * Get all holiday definitions.
     */
    @Transactional(readOnly = true)
    public List<HolidayDefinitionResponse> getAllHolidayDefinitions() {
        log.info("Fetching all holiday definitions");
        
        return holidayDefinitionRepository.findAll()
            .stream()
            .map(holidayDefinitionMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get holiday definition by ID.
     */
    @Transactional(readOnly = true)
    public HolidayDefinitionResponse getHolidayDefinitionById(String definitionId) {
        log.info("Fetching holiday definition: {}", definitionId);

        HolidayDefinition definition = holidayDefinitionRepository.findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "HOLIDAY_DEFINITION_NOT_FOUND",
                "Holiday definition not found with ID: " + definitionId));

        return holidayDefinitionMapper.toResponse(definition);
    }

    /**
     * Get holiday definitions by type.
     */
    @Transactional(readOnly = true)
    public List<HolidayDefinitionResponse> getHolidayDefinitionsByType(HolidayType holidayType) {
        log.info("Fetching holiday definitions by type: {}", holidayType);

        return holidayDefinitionRepository.findByHolidayType(holidayType)
            .stream()
            .map(holidayDefinitionMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update a holiday definition.
     */
    public HolidayDefinitionResponse updateHolidayDefinition(
            String definitionId, HolidayDefinitionRequest request) {
        
        log.info("Updating holiday definition: {}", definitionId);

        HolidayDefinition definition = holidayDefinitionRepository.findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "HOLIDAY_DEFINITION_NOT_FOUND",
                "Holiday definition not found with ID: " + definitionId));

        // Check if new name conflicts with another definition
        if (!definition.getHolidayName().equals(request.getHolidayName()) &&
            holidayDefinitionRepository.existsByHolidayName(request.getHolidayName())) {
            throw new com.dental.clinic.management.exception.holiday.DuplicateHolidayDefinitionException(
                request.getHolidayName());
        }

        holidayDefinitionMapper.updateEntity(definition, request);
        HolidayDefinition updatedDefinition = holidayDefinitionRepository.save(definition);

        log.info("Holiday definition updated successfully: {}", definitionId);
        return holidayDefinitionMapper.toResponse(updatedDefinition);
    }

    /**
     * Delete a holiday definition.
     * This will cascade delete all associated holiday dates.
     */
    public void deleteHolidayDefinition(String definitionId) {
        log.info("Deleting holiday definition: {}", definitionId);

        HolidayDefinition definition = holidayDefinitionRepository.findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "HOLIDAY_DEFINITION_NOT_FOUND",
                "Holiday definition not found with ID: " + definitionId));

        int datesCount = definition.getHolidayDates() != null ? 
                         definition.getHolidayDates().size() : 0;

        holidayDefinitionRepository.delete(definition);

        log.info("Holiday definition deleted: {} (with {} associated dates)", 
                 definitionId, datesCount);
    }

    /**
     * Generate unique holiday definition ID.
     * Format: HDF_001, HDF_002, etc.
     */
    private String generateDefinitionId() {
        String prefix = "HDF_";
        
        // Find all existing definitions with this prefix
        List<HolidayDefinition> existing = holidayDefinitionRepository
            .findByDefinitionIdStartingWithOrderByDefinitionIdDesc(prefix);
        
        if (existing.isEmpty()) {
            return prefix + "001";
        }
        
        // Extract sequence number from last ID (e.g., HDF_001 -> 001)
        String lastId = existing.get(0).getDefinitionId();
        String sequencePart = lastId.substring(prefix.length());
        
        try {
            int nextSequence = Integer.parseInt(sequencePart) + 1;
            return prefix + String.format("%03d", nextSequence);
        } catch (NumberFormatException e) {
            log.error("Failed to parse sequence from holiday definition ID: {}", lastId, e);
            // Fallback: count existing + 1
            return prefix + String.format("%03d", existing.size() + 1);
        }
    }
}
