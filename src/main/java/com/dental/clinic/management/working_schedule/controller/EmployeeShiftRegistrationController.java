package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.CreateRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateEffectiveToRequest;
import com.dental.clinic.management.working_schedule.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.working_schedule.dto.response.RegistrationResponse;
import com.dental.clinic.management.working_schedule.service.EmployeeShiftRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Employee Shift Registration (V2 - Quota-based Slots).
 * Employee claims individual slots, each slot has quota limit.
 */
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftRegistrationController {

    private final EmployeeShiftRegistrationService registrationService;

    /**
     * GET /api/v1/shift-registrations/available-slots
     * Get available slots for employee to claim (not full, not already registered).
     *
     * Permission: VIEW_AVAILABLE_SLOTS
     *
     * @return List of available slots with quota info
     */
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots() {
        log.info("REST request to get available slots");
        List<AvailableSlotResponse> slots = registrationService.getAvailableSlots();
        return ResponseEntity.ok(slots);
    }

    /**
     * POST /api/v1/shift-registrations
     * Claim a slot (employee registers for a slot).
     *
     * Permission: CREATE_REGISTRATION
     *
     * Validation:
     * - Slot must exist and be active
     * - Slot must not be full (registered < quota)
     * - Employee must not have active registration for this slot
     * - effectiveFrom defaults to today if null
     *
     * @param request Slot ID to claim
     * @return Created registration
     */
    @PostMapping
    public ResponseEntity<RegistrationResponse> claimSlot(
            @Valid @RequestBody CreateRegistrationRequest request) {
        log.info("REST request to claim slot {}", request.getPartTimeSlotId());
        RegistrationResponse response = registrationService.claimSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/shift-registrations
     * Get registrations (employee sees own, admin sees all or filtered).
     *
     * Permission:
     * - MANAGE_REGISTRATIONS_ALL: View all or filter by employeeId
     * - VIEW_REGISTRATIONS_OWN: View only own registrations
     *
     * @param employeeId Optional filter (admin only)
     * @return List of registrations
     */
    @GetMapping
    public ResponseEntity<List<RegistrationResponse>> getRegistrations(
            @RequestParam(required = false) Integer employeeId) {
        log.info("REST request to get registrations, filter employeeId: {}", employeeId);
        List<RegistrationResponse> registrations = registrationService.getRegistrations(employeeId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * DELETE /api/v1/shift-registrations/{registrationId}
     * Cancel registration (soft delete - set isActive = false).
     *
     * Permission:
     * - MANAGE_REGISTRATIONS_ALL: Can cancel any registration
     * - CANCEL_REGISTRATION_OWN: Can cancel only own registrations
     *
     * @param registrationId Registration ID to cancel
     * @return 204 No Content
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Integer registrationId) {
        log.info("REST request to cancel registration {}", registrationId);
        registrationService.cancelRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/v1/shift-registrations/{registrationId}/effective-to
     * Update effectiveTo date (admin extends deadline).
     *
     * Permission: MANAGE_REGISTRATIONS_ALL
     *
     * @param registrationId Registration ID
     * @param request        New effectiveTo date
     * @return Updated registration
     */
    @PatchMapping("/{registrationId}/effective-to")
    public ResponseEntity<RegistrationResponse> updateEffectiveTo(
            @PathVariable Integer registrationId,
            @Valid @RequestBody UpdateEffectiveToRequest request) {
        log.info("REST request to update effectiveTo for registration {}: {}", registrationId, request);
        RegistrationResponse response = registrationService.updateEffectiveTo(registrationId, request);
        return ResponseEntity.ok(response);
    }
}
