package com.dental.clinic.management.treatment_plans.controller;

import com.dental.clinic.management.treatment_plans.dto.TreatmentPlanSummaryDTO;
import com.dental.clinic.management.treatment_plans.service.TreatmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Treatment Plan Management.
 * Handles endpoints for viewing and managing patient treatment plans.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Treatment Plans", description = "APIs for managing patient treatment plans (long-term contracts)")
@SecurityRequirement(name = "Bearer Authentication")
public class TreatmentPlanController {

        private final TreatmentPlanService treatmentPlanService;

        /**
         * API 5.1: Get all treatment plans for a specific patient.
         * <p>
         * Required Permissions:
         * - VIEW_TREATMENT_PLAN_ALL: Staff can view all patients' plans
         * - VIEW_TREATMENT_PLAN_OWN: Patient can only view their own plans
         *
         * @param patientCode Unique patient code
         * @return List of treatment plan summaries
         */
        @Operation(summary = "Get treatment plans for a patient", description = "Retrieve all treatment plans (contracts) for a specific patient. "
                        +
                        "Staff with VIEW_TREATMENT_PLAN_ALL can view any patient's plans. " +
                        "Patients with VIEW_TREATMENT_PLAN_OWN can only view their own plans.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved treatment plans"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
                        @ApiResponse(responseCode = "404", description = "Patient not found")
        })
        @GetMapping("/patients/{patientCode}/treatment-plans")
        public ResponseEntity<List<TreatmentPlanSummaryDTO>> getTreatmentPlans(
                        @Parameter(description = "Patient code (e.g., PT-2025-001)", required = true) @PathVariable String patientCode) {
                log.info("REST request to get treatment plans for patient: {}", patientCode);

                List<TreatmentPlanSummaryDTO> plans = treatmentPlanService.getTreatmentPlansByPatient(patientCode);

                log.info("Returning {} treatment plans for patient {}", plans.size(), patientCode);
                return ResponseEntity.ok(plans);
        }
}
