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
        private final com.dental.clinic.management.treatment_plans.service.TreatmentPlanDetailService treatmentPlanDetailService;
        private final com.dental.clinic.management.treatment_plans.service.TreatmentPlanCreationService treatmentPlanCreationService;

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

        /**
         * API 5.2: Get detailed information for a specific treatment plan.
         * <p>
         * Returns complete treatment plan details with nested structure:
         * - Plan metadata (code, name, status, dates, financial info)
         * - Doctor and patient information
         * - Progress summary (counts of phases/items)
         * - Phases with items and linked appointments
         * <p>
         * Required Permissions:
         * - VIEW_TREATMENT_PLAN_ALL: Staff can view all patients' plans
         * - VIEW_TREATMENT_PLAN_OWN: Patient can only view their own plans
         *
         * @param patientCode Unique patient code (e.g., "BN-1001")
         * @param planCode    Unique treatment plan code (e.g., "PLAN-20251001-001")
         * @return Complete treatment plan details with nested phases, items, and
         *         appointments
         */
        @Operation(summary = "Get detailed treatment plan information", description = "Retrieve complete details of a specific treatment plan including phases, items, and linked appointments. "
                        +
                        "Returns nested structure: Plan → Phases → Items → Appointments. " +
                        "Includes progress summary with counts of completed phases/items. " +
                        "Staff with VIEW_TREATMENT_PLAN_ALL can view any patient's plans. " +
                        "Patients with VIEW_TREATMENT_PLAN_OWN can only view their own plans.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved treatment plan details"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions or accessing another patient's plan"),
                        @ApiResponse(responseCode = "404", description = "Patient or treatment plan not found")
        })
        @GetMapping("/patients/{patientCode}/treatment-plans/{planCode}")
        public ResponseEntity<com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse> getTreatmentPlanDetail(
                        @Parameter(description = "Patient code (e.g., BN-1001)", required = true) @PathVariable String patientCode,
                        @Parameter(description = "Treatment plan code (e.g., PLAN-20251001-001)", required = true) @PathVariable String planCode) {
                log.info("REST request to get treatment plan detail - Patient: {}, Plan: {}", patientCode, planCode);

                com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse response = treatmentPlanDetailService
                                .getTreatmentPlanDetail(patientCode, planCode);

                log.info("Returning treatment plan detail for {} with {} phases", planCode,
                                response.getPhases().size());
                return ResponseEntity.ok(response);
        }

        /**
         * API 5.3: Create a new treatment plan from a template.
         * <p>
         * Creates a complete patient treatment plan by copying (snapshotting) all
         * phases,
         * items, and services from a pre-defined template package.
         * <p>
         * Business Logic:
         * 1. Validates patient, doctor, and template exist
         * 2. Validates discount ≤ total cost
         * 3. Generates unique plan code (PLAN-YYYYMMDD-XXX)
         * 4. Calculates expected end date from template duration
         * 5. Snapshots all phases and items (expands by quantity, ordered by sequence)
         * 6. Calculates total cost and final cost
         * 7. Returns complete plan details (same structure as API 5.2)
         * <p>
         * Required Permission: CREATE_TREATMENT_PLAN (typically Doctor or Manager)
         *
         * @param patientCode Patient's business key (e.g., "BN-1001")
         * @param request     Request body with template code, doctor code, discount,
         *                    payment type
         * @return Newly created treatment plan with complete details (201 CREATED)
         */
        @Operation(summary = "Create treatment plan from template", description = "Create a new patient treatment plan by copying from a template package. "
                        +
                        "Automatically generates plan code, snapshots all phases/items, calculates costs, and sets expected end date. "
                        +
                        "Example: Create 'Orthodontics 2-year package' for a patient from template 'TPL_ORTHO_METAL'. "
                        +
                        "Returns the complete plan structure (same as API 5.2) with status=PENDING.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Treatment plan created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request - discount exceeds total cost, or template inactive"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions (requires CREATE_TREATMENT_PLAN)"),
                        @ApiResponse(responseCode = "404", description = "Patient, doctor, or template not found")
        })
        @PostMapping("/patients/{patientCode}/treatment-plans")
        public ResponseEntity<com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse> createTreatmentPlan(
                        @Parameter(description = "Patient code (e.g., BN-1001)", required = true) @PathVariable String patientCode,

                        @Parameter(description = "Request body with template code, doctor code, discount, and payment type", required = true) @RequestBody @jakarta.validation.Valid com.dental.clinic.management.treatment_plans.dto.request.CreateTreatmentPlanRequest request) {

                log.info("REST request to create treatment plan - Patient: {}, Template: {}, Doctor: {}",
                                patientCode, request.getSourceTemplateCode(), request.getDoctorEmployeeCode());

                com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse response = treatmentPlanCreationService
                                .createTreatmentPlanFromTemplate(patientCode, request);

                log.info("Treatment plan created successfully. PlanCode: {}, TotalItems: {}",
                                response.getPlanCode(),
                                response.getPhases().stream()
                                                .mapToInt(p -> p.getItems().size())
                                                .sum());

                return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
        }
}
