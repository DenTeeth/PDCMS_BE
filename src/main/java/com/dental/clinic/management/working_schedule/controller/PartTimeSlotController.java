package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.CreatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.response.PartTimeSlotResponse;
import com.dental.clinic.management.working_schedule.service.PartTimeSlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Part-time Slot Management (Admin).
 */
@RestController
@RequestMapping("/api/v1/work-slots")
@RequiredArgsConstructor
@Slf4j
public class PartTimeSlotController {

    private final PartTimeSlotService partTimeSlotService;

    /**
     * Create a new part-time slot.
     */
    @PostMapping
    public ResponseEntity<PartTimeSlotResponse> createSlot(
            @Valid @RequestBody CreatePartTimeSlotRequest request) {
        log.info("POST /api/v1/work-slots - Creating slot");
        PartTimeSlotResponse response = partTimeSlotService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all part-time slots with registration counts.
     */
    @GetMapping
    public ResponseEntity<List<PartTimeSlotResponse>> getAllSlots() {
        log.info("GET /api/v1/work-slots - Fetching all slots");
        List<PartTimeSlotResponse> responses = partTimeSlotService.getAllSlots();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a part-time slot (quota and isActive).
     */
    @PutMapping("/{slotId}")
    public ResponseEntity<PartTimeSlotResponse> updateSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody UpdatePartTimeSlotRequest request) {
        log.info("PUT /api/v1/work-slots/{} - Updating slot", slotId);
        PartTimeSlotResponse response = partTimeSlotService.updateSlot(slotId, request);
        return ResponseEntity.ok(response);
    }
}
