package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.RenewalResponseRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRenewalResponse;
import com.dental.clinic.management.working_schedule.service.ShiftRenewalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for part-time employee shift renewal requests.
 *
 * Provides endpoints for:
 * - Viewing pending renewal invitations
 * - Responding to renewal requests (confirm/decline)
 */
@RestController
@RequestMapping("/api/v1/registrations/renewals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shift Renewal", description = "APIs for part-time employee shift renewal management")
public class ShiftRenewalController {

    private final ShiftRenewalService renewalService;

    /**
     * Get all pending renewal requests for the authenticated employee.
     *
     * Only returns non-expired requests that are awaiting employee response.
     *
     * @return list of pending renewal requests
     */
    @Operation(summary = "Get pending renewal requests", description = "Retrieve all pending shift renewal invitations for the current employee", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending renewals"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<ShiftRenewalResponse>> getPendingRenewals() {
        Integer employeeId = getAuthenticatedEmployeeId();
        log.info("GET /api/v1/registrations/renewals/pending - Employee ID: {}", employeeId);

        List<ShiftRenewalResponse> renewals = renewalService.getPendingRenewals(employeeId);
        return ResponseEntity.ok(renewals);
    }

    /**
     * Respond to a shift renewal request.
     *
     * Employee can either confirm (extend registration) or decline the renewal.
     * If confirmed, the original shift registration is extended by 3 months.
     *
     * @param renewalId the renewal request ID
     * @param request   the response (CONFIRMED or DECLINED)
     * @return updated renewal request
     */
    @Operation(summary = "Respond to renewal request", description = "Confirm or decline a shift renewal invitation. " +
            "Confirming will extend the shift registration by 3 months.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully responded to renewal"),
            @ApiResponse(responseCode = "400", description = "Invalid request - Invalid action or request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner of this renewal"),
            @ApiResponse(responseCode = "404", description = "Renewal request not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Renewal already responded or expired")
    })
    @PatchMapping("/{renewal_id}/respond")
    public ResponseEntity<ShiftRenewalResponse> respondToRenewal(
            @Parameter(description = "Renewal request ID", required = true) @PathVariable("renewal_id") String renewalId,

            @Parameter(description = "Response action (CONFIRMED or DECLINED)", required = true) @Valid @RequestBody RenewalResponseRequest request) {
        Integer employeeId = getAuthenticatedEmployeeId();
        log.info("PATCH /api/v1/registrations/renewals/{}/respond - Employee ID: {}, Action: {}",
                renewalId, employeeId, request.getAction());

        ShiftRenewalResponse response = renewalService.respondToRenewal(
                renewalId,
                employeeId,
                request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get the authenticated employee ID from the security context.
     *
     * @return employee ID
     * @throws RuntimeException if employee ID cannot be extracted
     */
    private Integer getAuthenticatedEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Authentication context not found");
        }

        // Assuming the employee ID is stored in the principal
        // Adjust this based on your authentication implementation
        try {
            return Integer.parseInt(authentication.getName());
        } catch (NumberFormatException e) {
            log.error("Failed to parse employee ID from authentication: {}", authentication.getName());
            throw new RuntimeException("Invalid employee ID in authentication context");
        }
    }
}
