package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.CreateRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateEffectiveToRequest;
import com.dental.clinic.management.working_schedule.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.working_schedule.dto.response.RegistrationResponse;
import com.dental.clinic.management.working_schedule.dto.response.SlotDetailResponse;
import com.dental.clinic.management.working_schedule.service.EmployeeShiftRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * REST controller for Part-Time Flex Registration.
 * Specifically designed for PART_TIME_FLEX employees who work on flexible schedules.
 * 
 * NEW SPECIFICATION (Approval Workflow):
 * - Part-time flex employees submit registration requests with flexible date ranges
 * - Requests go to PENDING status awaiting manager approval
 * - Manager must approve before part-time flex employee can work the shift
 * - Only APPROVED registrations count toward slot quota
 */
@RestController
@RequestMapping("/api/v1/registrations/part-time-flex")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Part-Time Flex Registration", description = "APIs for part-time flex employees to register for available slots with flexible schedules")
public class EmployeeShiftRegistrationController {

    private final EmployeeShiftRegistrationService registrationService;

    /**
     * GET /api/v1/registrations/part-time-flex/available-slots
     * Get available slots for part-time flex employees to register (NEW: dynamic quota).
     *
     * Permission: VIEW_SCHEDULE_OWN (for part-time flex employees)
     * 
     * NEW SPECIFICATION:
     * - Only count APPROVED registrations from part-time flex employees
     * - Show slots with any day having availability
     * - Optional month filter (YYYY-MM) to show only slots with availability in that month
     *
     * @param month Optional month filter in YYYY-MM format (e.g., "2025-12")
     * @return List of available slots with quota info for part-time flex employees
     */
    @GetMapping("/available-slots")
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE_OWN')")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @RequestParam(required = false) String month) {
        log.info("REST request to get available slots (month filter: {})", month);
        List<AvailableSlotResponse> slots = registrationService.getAvailableSlots(month);
        return ResponseEntity.ok(slots);
    }

    /**
     * GET /api/v1/registrations/part-time/slots/{slotId}/details
     * Get detailed availability information for a specific slot.
     * Shows month-by-month breakdown to help employees make informed decisions.
     *
     * Permission: VIEW_SCHEDULE_OWN
     * 
     * @param slotId The slot ID to get details for
     * @return Detailed slot information with monthly availability breakdown
     */
    @Operation(
        summary = "Get slot details for part-time flex employees",
        description = "Retrieve detailed availability information for a specific part-time flex slot with monthly breakdown to help part-time flex employees make informed registration decisions"
    )
    @GetMapping("/slots/{slotId}/details")
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE_OWN')")
    public ResponseEntity<SlotDetailResponse> getSlotDetail(@PathVariable Long slotId) {
        log.info("REST request to get slot detail for slot {}", slotId);
        SlotDetailResponse detail = registrationService.getSlotDetail(slotId);
        return ResponseEntity.ok(detail);
    }

    /**
     * GET /api/v1/registrations/part-time-flex/slots/{slotId}/daily-availability
     * Get daily availability breakdown for a specific part-time flex slot in a given month.
     * Shows quota, registered count from part-time flex employees, and remaining slots for each working day.
     *
     * Permission: VIEW_SCHEDULE_OWN (part-time flex employees), MANAGE_PART_TIME_REGISTRATIONS (managers), MANAGE_WORK_SLOTS (admins)
     * 
     * Business Logic:
     * - Only includes days matching slot's dayOfWeek
     * - Counts APPROVED registrations from part-time flex employees covering each date
     * - Status: AVAILABLE (100% free), PARTIAL (some taken by part-time flex employees), FULL (no slots available)
     * 
     * Query Parameters:
     * - month (required): Month in YYYY-MM format (e.g., "2025-11", "2025-12")
     * 
     * Example Request:
     * GET /api/v1/registrations/part-time-flex/slots/1/daily-availability?month=2025-11
     * 
     * Example Response:
     * {
     *   "slotId": 1,
     *   "shiftName": "Ca Part-time Sáng (8h-12h)",
     *   "dayOfWeek": "MONDAY",
     *   "quota": 10,
     *   "month": "2025-11",
     *   "monthName": "November 2025",
     *   "totalWorkingDays": 4,
     *   "totalDaysAvailable": 1,
     *   "totalDaysPartial": 2,
     *   "totalDaysFull": 1,
     *   "dailyAvailability": [
     *     {
     *       "date": "2025-11-03",
     *       "dayOfWeek": "MONDAY",
     *       "quota": 10,
     *       "registered": 0,
     *       "remaining": 10,
     *       "status": "AVAILABLE"
     *     },
     *     {
     *       "date": "2025-11-10",
     *       "dayOfWeek": "MONDAY",
     *       "quota": 10,
     *       "registered": 8,
     *       "remaining": 2,
     *       "status": "PARTIAL"
     *     },
     *     ...
     *   ]
     * }
     * 
     * @param slotId The slot ID to get daily availability for
     * @param month Month in YYYY-MM format (required)
     * @return Daily availability response with per-day breakdown
     */
    @Operation(
        summary = "Get daily availability for part-time flex slots",
        description = "Retrieve day-by-day availability breakdown for a specific part-time flex slot in a given month showing quota and registered counts from part-time flex employees"
    )
    @GetMapping("/slots/{slotId}/daily-availability")
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE_OWN') or hasAuthority('MANAGE_PART_TIME_REGISTRATIONS') or hasAuthority('MANAGE_WORK_SLOTS')")
    public ResponseEntity<com.dental.clinic.management.working_schedule.dto.response.DailyAvailabilityResponse> getDailyAvailability(
            @PathVariable Long slotId,
            @RequestParam String month) {
        log.info("REST request to get daily availability for slot {} in month {}", slotId, month);
        com.dental.clinic.management.working_schedule.dto.response.DailyAvailabilityResponse response = 
            registrationService.getDailyAvailability(slotId, month);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/registrations/part-time
     * Submit registration request for part-time flex employees (NEW: goes to PENDING status).
     *
     * Permission: VIEW_SCHEDULE_OWN (for part-time flex employees)
     * 
     * NEW SPECIFICATION:
     * - Part-time flex employee provides flexible effectiveFrom and effectiveTo dates
     * - Request goes to PENDING status (not immediately active)
     * - Manager must approve before part-time flex employee can work
     * - Dates must be within slot's effective range
     *
     * Request Body:
     * {
     *   "partTimeSlotId": 1,
     *   "effectiveFrom": "2025-11-01",
     *   "effectiveTo": "2025-11-17"
     * }
     *
     * @param request Registration details with flexible dates
     * @return Created registration (status: PENDING)
     */
    @Operation(
        summary = "Submit registration request for part-time flex employee",
        description = "Create a new part-time flex slot registration with flexible date range (status: PENDING awaiting manager approval). Allows part-time flex employees to request shifts."
    )
    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE_OWN')")
    public ResponseEntity<RegistrationResponse> claimSlot(
            @Valid @RequestBody CreateRegistrationRequest request) {
        log.info("REST request to submit registration for slot {}", request.getPartTimeSlotId());
        RegistrationResponse response = registrationService.claimSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/registrations/part-time-flex
     * Get registrations for part-time flex employees (employee sees own, admin sees all or filtered).
     *
     * Permission:
     * - MANAGE_REGISTRATIONS_ALL: View all part-time flex registrations or filter by employeeId
     * - VIEW_REGISTRATIONS_OWN: Part-time flex employees view only their own registrations
     * 
     * NEW: Shows status (PENDING, APPROVED, REJECTED)
     * NEW: Supports pagination and sorting
     *
     * @param employeeId Optional filter (admin only) to view specific part-time flex employee's registrations
     * @param pageable Pagination and sorting parameters
     * @return Page of part-time flex registrations
     */
    @Operation(
        summary = "Get part-time flex registrations",
        description = "Retrieve paginated list of part-time flex registrations. Part-time flex employees see only their own registrations, admins can view all or filter by specific part-time flex employee"
    )
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGE_PART_TIME_REGISTRATIONS', 'VIEW_SCHEDULE_OWN')")
    public ResponseEntity<org.springframework.data.domain.Page<RegistrationResponse>> getRegistrations(
            @RequestParam(required = false) Integer employeeId,
            org.springframework.data.domain.Pageable pageable) {
        log.info("REST request to get registrations, filter employeeId: {}, page: {}, size: {}, sort: {}", 
                 employeeId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        org.springframework.data.domain.Page<RegistrationResponse> registrations = 
            registrationService.getRegistrations(employeeId, pageable);
        return ResponseEntity.ok(registrations);
    }

    /**
     * GET /api/v1/registrations/part-time/{registrationId}
     * Get part-time flex registration details by ID.
     * 
     * Permissions:
     * - Part-time flex employee can only view their own registrations
     * - Admin can view any part-time flex registration
     *
     * @param registrationId Part-time flex registration ID
     * @return Part-time flex registration details
     */
    @Operation(
        summary = "Get part-time flex registration by ID",
        description = "Retrieve detailed information about a specific part-time flex registration for a part-time flex employee"
    )
    @GetMapping("/{registrationId}")
    @PreAuthorize("hasAnyAuthority('MANAGE_PART_TIME_REGISTRATIONS', 'VIEW_SCHEDULE_OWN')")
    public ResponseEntity<RegistrationResponse> getRegistrationById(@PathVariable Integer registrationId) {
        log.info("REST request to get registration details: {}", registrationId);
        RegistrationResponse registration = registrationService.getRegistrationById(registrationId);
        return ResponseEntity.ok(registration);
    }

    /**
     * DELETE /api/v1/registrations/part-time/{registrationId}
     * Cancel part-time flex registration (soft delete - set isActive = false).
     *
     * Permission:
     * - MANAGE_PART_TIME_REGISTRATIONS: Can cancel any part-time flex registration
     * - VIEW_SCHEDULE_OWN: Part-time flex employees can cancel only their own registrations
     * 
     * NEW: Part-time flex employees can only cancel PENDING registrations
     *
     * @param registrationId Part-time flex registration ID to cancel
     * @return 204 No Content
     */
    @Operation(
        summary = "Cancel part-time flex registration",
        description = "Cancel a part-time flex registration by soft deletion. Part-time flex employees can only cancel their own PENDING registrations."
    )
    @DeleteMapping("/{registrationId}")
    @PreAuthorize("hasAnyAuthority('MANAGE_PART_TIME_REGISTRATIONS', 'VIEW_SCHEDULE_OWN')")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Integer registrationId) {
        log.info("REST request to cancel registration {}", registrationId);
        registrationService.cancelRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/v1/registrations/part-time/{registrationId}/effective-to
     * Update effectiveTo date for part-time flex registration (admin extends deadline).
     *
     * Permission: MANAGE_REGISTRATIONS_ALL
     *
     * @param registrationId Part-time flex registration ID
     * @param request        New effectiveTo date
     * @return Updated part-time flex registration
     */
    @Operation(
        summary = "Update effective-to date for part-time flex registration",
        description = "Update the effectiveTo date of a part-time flex registration (admin function to extend deadline for part-time flex employees)"
    )
    @PatchMapping("/{registrationId}/effective-to")
    @PreAuthorize("hasAuthority('MANAGE_PART_TIME_REGISTRATIONS')")
    public ResponseEntity<RegistrationResponse> updateEffectiveTo(
            @PathVariable Integer registrationId,
            @Valid @RequestBody UpdateEffectiveToRequest request) {
        log.info("REST request to update effectiveTo for registration {}: {}", registrationId, request);
        RegistrationResponse response = registrationService.updateEffectiveTo(registrationId, request);
        return ResponseEntity.ok(response);
    }
}
