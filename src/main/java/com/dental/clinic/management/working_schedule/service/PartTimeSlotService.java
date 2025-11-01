package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.response.PartTimeSlotResponse;
import com.dental.clinic.management.working_schedule.exception.QuotaViolationException;
import com.dental.clinic.management.working_schedule.exception.SlotAlreadyExistsException;
import com.dental.clinic.management.working_schedule.exception.SlotNotFoundException;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import com.dental.clinic.management.exception.work_shift.WorkShiftNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartTimeSlotService {

    private final PartTimeSlotRepository partTimeSlotRepository;
    private final WorkShiftRepository workShiftRepository;

    /**
     * Create a new part-time slot.
     */
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public PartTimeSlotResponse createSlot(CreatePartTimeSlotRequest request) {
        log.info("Creating part-time slot: shift={}, day={}, quota={}", 
                 request.getWorkShiftId(), request.getDayOfWeek(), request.getQuota());

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));

        // Check if slot already exists
        if (partTimeSlotRepository.existsByWorkShiftIdAndDayOfWeek(
                request.getWorkShiftId(), request.getDayOfWeek())) {
            throw new SlotAlreadyExistsException(request.getWorkShiftId(), request.getDayOfWeek());
        }

        // Create slot
        PartTimeSlot slot = new PartTimeSlot();
        slot.setWorkShiftId(request.getWorkShiftId());
        slot.setDayOfWeek(request.getDayOfWeek().toUpperCase());
        slot.setQuota(request.getQuota());
        slot.setIsActive(true);

        PartTimeSlot savedSlot = partTimeSlotRepository.save(slot);
        log.info("Created slot with ID: {}", savedSlot.getSlotId());

        return buildResponse(savedSlot, workShift.getShiftName());
    }

    /**
     * Get all slots with registration counts.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public List<PartTimeSlotResponse> getAllSlots() {
        log.info("Fetching all part-time slots");

        return partTimeSlotRepository.findAll().stream()
                .map(slot -> {
                    WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId())
                            .orElse(null);
                    String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";
                    return buildResponse(slot, shiftName);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update slot quota and isActive status.
     */
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public PartTimeSlotResponse updateSlot(Long slotId, UpdatePartTimeSlotRequest request) {
        log.info("Updating slot {}: quota={}, isActive={}", slotId, request.getQuota(), request.getIsActive());

        PartTimeSlot slot = partTimeSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));

        // Check quota violation
        long currentRegistered = partTimeSlotRepository.countActiveRegistrations(slotId);
        if (request.getQuota() < currentRegistered) {
            throw new QuotaViolationException(slotId, request.getQuota(), currentRegistered);
        }

        slot.setQuota(request.getQuota());
        slot.setIsActive(request.getIsActive());

        PartTimeSlot updatedSlot = partTimeSlotRepository.save(slot);
        log.info("Updated slot {}", slotId);

        WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
        String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";

        return buildResponse(updatedSlot, shiftName);
    }

    private PartTimeSlotResponse buildResponse(PartTimeSlot slot, String shiftName) {
        long registered = partTimeSlotRepository.countActiveRegistrations(slot.getSlotId());

        return PartTimeSlotResponse.builder()
                .slotId(slot.getSlotId())
                .workShiftId(slot.getWorkShiftId())
                .workShiftName(shiftName)
                .dayOfWeek(slot.getDayOfWeek())
                .quota(slot.getQuota())
                .registered(registered)
                .isActive(slot.getIsActive())
                .build();
    }
}
