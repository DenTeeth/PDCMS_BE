package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.dto.request.CreateFixedRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateFixedRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.response.FixedRegistrationResponse;
import com.dental.clinic.management.working_schedule.service.FixedShiftRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * REST controller for fixed shift registration management.
 * Provides endpoints for creating, viewing, updating, and deleting fixed
 * schedules.
 *
 * Schema V14 - Luồng 1: Lịch Cố định (FULL_TIME & PART_TIME_FIXED)
 */
@RestController
@RequestMapping("/api/v1/fixed-registrations")
@RequiredArgsConstructor
@Tag(name = "Fixed Shift Registration Management", description = "APIs for managing fixed shift registrations")
public class FixedShiftRegistrationController {

    private final FixedShiftRegistrationService fixedRegistrationService;
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new fixed shift registration.
     *
     * POST /api/v1/fixed-registrations
     *
     * Authorization: Requires MANAGE_FIXED_REGISTRATIONS
     *
     * @param request creation request
     * @return created registration details
     */
    @Operation(
        summary = "Create fixed registration",
        description = "Create a new fixed shift registration for an employee with specified work shifts and date range"
    )
    @PostMapping
    public ResponseEntity<FixedRegistrationResponse> createFixedRegistration(
            @Valid @RequestBody CreateFixedRegistrationRequest request) {

        FixedRegistrationResponse response = fixedRegistrationService.createFixedRegistration(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get list of fixed shift registrations.
     *
     * GET /api/v1/fixed-registrations?employeeId=123&isActive=true
     *
     * Authorization: Requires VIEW_FIXED_REGISTRATIONS_ALL or
     * VIEW_FIXED_REGISTRATIONS_OWN
     *
     * @param employeeId     employee ID (optional for VIEW_ALL)
     * @param isActive       filter by active status (null = all, true = active only, false = inactive only)
     * @param authentication authenticated user
     * @return list of registrations
     */
    @Operation(
        summary = "Get fixed registrations",
        description = "Retrieve list of fixed shift registrations with optional filters for employee and active status"
    )
    @GetMapping
    public ResponseEntity<List<FixedRegistrationResponse>> getFixedRegistrations(
            @RequestParam(name = "employeeId", required = false) Integer employeeId,
            @RequestParam(name = "isActive", required = false) Boolean isActive,
            Authentication authentication) {

        // Get current user info
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        Integer currentEmployeeId = employeeRepository.findByAccount_Username(username)
                .map(employee -> employee.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));

        boolean hasViewAllPermission = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("VIEW_FIXED_REGISTRATIONS_ALL"));

        List<FixedRegistrationResponse> registrations = fixedRegistrationService.getFixedRegistrations(
                employeeId, currentEmployeeId, hasViewAllPermission, isActive);

        return ResponseEntity.ok(registrations);
    }

    /**
     * Update a fixed shift registration (partial update).
     *
     * PATCH /api/v1/fixed-registrations/{registrationId}
     *
     * Authorization: Requires MANAGE_FIXED_REGISTRATIONS
     *
     * @param registrationId registration ID
     * @param request        update request (all fields optional)
     * @return updated registration details
     */
    @Operation(
        summary = "Update fixed registration",
        description = "Partially update a fixed shift registration's configuration (all fields optional)"
    )
    @PatchMapping("/{registrationId}")
    public ResponseEntity<FixedRegistrationResponse> updateFixedRegistration(
            @PathVariable("registrationId") Integer registrationId,
            @Valid @RequestBody UpdateFixedRegistrationRequest request) {

        FixedRegistrationResponse response = fixedRegistrationService.updateFixedRegistration(registrationId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft delete) a fixed shift registration.
     *
     * DELETE /api/v1/fixed-registrations/{registrationId}
     *
     * Authorization: Requires MANAGE_FIXED_REGISTRATIONS
     *
     * @param registrationId registration ID
     * @return no content
     */
    @Operation(
        summary = "Delete fixed registration",
        description = "Soft delete a fixed shift registration by setting it inactive"
    )
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> deleteFixedRegistration(
            @PathVariable("registrationId") Integer registrationId) {

        fixedRegistrationService.deleteFixedRegistration(registrationId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Backfill shifts for all existing fixed registrations.
     * This endpoint should be called once to generate shifts for registrations created before auto-generation was implemented.
     *
     * POST /api/v1/fixed-registrations/backfill-shifts
     *
     * Authorization: Requires MANAGE_FIXED_REGISTRATIONS (Admin only)
     *
     * @return Summary of backfill operation
     */
    @Operation(
        summary = "Backfill shifts",
        description = "Generate shifts for all fixed registrations created before auto-generation was implemented"
    )
    @PostMapping("/backfill-shifts")
    public ResponseEntity<String> backfillShifts() {
        String summary = fixedRegistrationService.backfillShiftsForExistingRegistrations();
        return ResponseEntity.ok(summary);
    }

    /**
     * Regenerate shifts for a specific fixed registration.
     * Useful for manually fixing a single registration.
     *
     * POST /api/v1/fixed-registrations/{registrationId}/regenerate-shifts
     *
     * Authorization: Requires MANAGE_FIXED_REGISTRATIONS (Admin only)
     *
     * @param registrationId Registration ID
     * @return Number of shifts created
     */
    @Operation(
        summary = "Regenerate shifts",
        description = "Delete and regenerate all shifts for a specific fixed registration to fix incorrect data"
    )
    @PostMapping("/{registrationId}/regenerate-shifts")
    public ResponseEntity<String> regenerateShifts(
            @PathVariable("registrationId") Integer registrationId) {
        
        int shiftsCreated = fixedRegistrationService.regenerateShiftsForRegistration(registrationId);
        
        String message = String.format(" Successfully generated %d shifts for registration #%d", 
                shiftsCreated, registrationId);
        
        return ResponseEntity.ok(message);
    }
}
