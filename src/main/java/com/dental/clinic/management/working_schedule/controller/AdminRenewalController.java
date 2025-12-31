package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.working_schedule.dto.request.FinalizeRenewalRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRenewalResponse;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import com.dental.clinic.management.working_schedule.service.ShiftRenewalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin shift renewal management.
 * <p>
 * ADMIN TWO-STEP WORKFLOW:
 * 1. Employee responds CONFIRMED (via P7 Employee API)
 * 2. Admin reviews and negotiates extension duration with employee
 * 3. Admin calls Finalize API with custom effective_to date (3 months, 1 year,
 * etc.)
 * 4. System creates new registration and updates status to FINALIZED
 */
@RestController
@RequestMapping("/api/v1/admin/registrations/renewals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Shift Renewal", description = "Admin APIs for finalizing shift renewal requests")
public class AdminRenewalController {

        private final ShiftRenewalService renewalService;

        /**
         * Get all renewal requests across all employees (Admin/Manager dashboard).
         * <p>
         * OPTIONAL FILTERS:
         * - status: PENDING_ACTION, CONFIRMED, DECLINED, FINALIZED, EXPIRED
         * - employeeId: Filter by specific employee
         * <p>
         * PERMISSION: VIEW_RENEWAL_ALL (Admin/Manager only)
         *
         * @param status     optional status filter
         * @param employeeId optional employee ID filter
         * @return list of renewal requests matching filters
         */
        @Operation(summary = "Get all renewal requests (Admin/Manager)", description = "Retrieve all renewal requests across all employees with optional filters. " +
                        "Requires VIEW_RENEWAL_ALL permission.", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping
        @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_RENEWAL_ALL + "')")
        public ResponseEntity<List<ShiftRenewalResponse>> getAllRenewals(
                        @Parameter(description = "Filter by renewal status (PENDING_ACTION, CONFIRMED, DECLINED, FINALIZED, EXPIRED)") @RequestParam(required = false) RenewalStatus status,
                        @Parameter(description = "Filter by employee ID") @RequestParam(required = false) Integer employeeId) {

                log.info("GET /api/v1/admin/registrations/renewals - Status: {}, EmployeeId: {}", status, employeeId);

                List<ShiftRenewalResponse> renewals = renewalService.getAllRenewals(status, employeeId);

                log.info("Found {} renewal requests", renewals.size());
                return ResponseEntity.ok(renewals);
        }

        /**
         * Finalize a renewal request with custom effective_to date.
         * <p>
         * PREREQUISITES:
         * - Renewal status must be CONFIRMED (employee agreed)
         * - Admin has negotiated extension duration with employee
         * <p>
         * WORKFLOW:
         * 1. Validate newEffectiveTo > old effective_to
         * 2. Lock old fixed_shift_registration (FOR UPDATE)
         * 3. Deactivate old: SET is_active = FALSE
         * 4. Create new registration:
         * - effective_from = old_effective_to + 1 day
         * - effective_to = admin-specified date (from request body)
         * - Copy work_shift, employee, registration_days
         * 5. Update renewal status to FINALIZED
         * <p>
         * USE CASES:
         * - 3-month trial extension: old_to + 3 months
         * - Standard 1-year renewal: old_to + 1 year
         * - Custom period: based on project/contract terms
         *
         * @param request FinalizeRenewalRequest with renewalRequestId and
         *                newEffectiveTo
         * @return ShiftRenewalResponse with FINALIZED status
         */
        @Operation(summary = "Finalize shift renewal (Admin)", description = "Admin finalizes employee's confirmed renewal with custom effective_to date. "
                        +
                        "Creates new extended registration and deactivates old one.", security = @SecurityRequirement(name = "bearerAuth"))
        @PreAuthorize("hasAuthority('" + AuthoritiesConstants.MANAGE_FIXED_REGISTRATIONS + "')")
        @PostMapping("/finalize")
        public ResponseEntity<ShiftRenewalResponse> finalizeRenewal(
                        @Parameter(description = "Finalize renewal request with custom effective_to date", required = true) @Valid @RequestBody FinalizeRenewalRequest request) {

                log.info("POST /api/v1/admin/registrations/renewals/finalize - Renewal ID: {}, New Effective To: {}",
                                request.getRenewalRequestId(), request.getNewEffectiveTo());

                ShiftRenewalResponse response = renewalService.finalizeRenewal(request);

                log.info("Finalized renewal {} successfully. New registration created.", request.getRenewalRequestId());
                return ResponseEntity.ok(response);
        }
}
