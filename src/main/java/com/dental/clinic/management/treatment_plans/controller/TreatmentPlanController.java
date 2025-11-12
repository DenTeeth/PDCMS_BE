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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        private final com.dental.clinic.management.treatment_plans.service.CustomTreatmentPlanService customTreatmentPlanService;

        /**
         * API 5.1: Get all treatment plans for a specific patient (with pagination).
         * <p>
         * Required Permissions:
         * - VIEW_TREATMENT_PLAN_ALL: Staff can view all patients' plans
         * - VIEW_TREATMENT_PLAN_OWN: Patient can only view their own plans
         * <p>
         * Pagination: Use query params page (0-indexed) and size (default 10)
         * Example: /patients/BN-1001/treatment-plans?page=0&size=20
         *
         * @param patientCode Unique patient code
         * @param pageable    Pagination parameters (page, size, sort)
         * @return Page of treatment plan summaries with pagination metadata
         */
        @Operation(summary = "Get treatment plans for a patient (paginated)", description = "Retrieve treatment plans (contracts) for a specific patient with pagination support. "
                        +
                        "Staff with VIEW_TREATMENT_PLAN_ALL can view any patient's plans. " +
                        "Patients with VIEW_TREATMENT_PLAN_OWN can only view their own plans. " +
                        "Supports pagination via page (0-indexed) and size query parameters. " +
                        "Response includes totalElements, totalPages, and current page info.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved treatment plans"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
                        @ApiResponse(responseCode = "404", description = "Patient not found")
        })
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_ALL
                        + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_OWN
                        + "')")
        @GetMapping("/patients/{patientCode}/treatment-plans")
        public ResponseEntity<org.springframework.data.domain.Page<TreatmentPlanSummaryDTO>> getTreatmentPlans(
                        @Parameter(description = "Patient code (e.g., BN-1001)", required = true) @PathVariable String patientCode,
                        @Parameter(description = "Pagination parameters (page=0, size=10, sort=createdAt,desc)", required = false) org.springframework.data.domain.Pageable pageable) {
                log.info("REST request to get treatment plans for patient: {} (page: {}, size: {})",
                                patientCode, pageable.getPageNumber(), pageable.getPageSize());

                org.springframework.data.domain.Page<TreatmentPlanSummaryDTO> plans = treatmentPlanService
                                .getTreatmentPlansByPatient(patientCode, pageable);

                log.info("Returning {} treatment plans for patient {} (total: {}, page: {}/{})",
                                plans.getNumberOfElements(), patientCode, plans.getTotalElements(),
                                plans.getNumber() + 1, plans.getTotalPages());
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
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_ALL
                        + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_OWN
                        + "')")
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
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.CREATE_TREATMENT_PLAN + "')")
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

        /**
         * API 5.4: Create a CUSTOM treatment plan from scratch (without template).
         * <p>
         * Allows doctor to build a treatment plan manually by selecting services,
         * customizing prices, setting phases, and defining quantities.
         * <p>
         * Key Features:
         * - Quantity Expansion: Setting quantity=5 creates 5 separate items with
         * auto-incremented sequence
         * - Price Override: Allows custom pricing (must be within 50%-150% of service
         * default)
         * - Approval Workflow: Created plans have approval_status=DRAFT (requires
         * manager approval before activation)
         * - Phase Duration: Can set estimated_duration_days for each phase (V19)
         * <p>
         * Business Logic:
         * 1. Validates patient, doctor, and services exist and are active
         * 2. Validates phase numbers are unique (no duplicates)
         * 3. Validates each phase has at least 1 item
         * 4. Validates price overrides are within 50%-150% of service default
         * 5. Expands items by quantity (e.g., quantity=3 → creates 3 items with
         * sequence 1,2,3)
         * 6. Calculates total cost and validates discount ≤ total cost
         * 7. Saves plan with approval_status=DRAFT (requires API 5.9 for approval)
         * 8. Returns complete plan details
         * <p>
         * V19 Changes:
         * - Added approval_status=DRAFT by default
         * - Added estimated_duration_days to phases
         * - Items use status=PENDING (not PENDING_APPROVAL)
         * <p>
         * Required Permission: CREATE_TREATMENT_PLAN (typically Doctor or Manager)
         *
         * @param patientCode Patient's business key (e.g., "BN-1001")
         * @param request     Request body with phases, items, quantities, and custom
         *                    prices
         * @return Newly created custom treatment plan with complete details (201
         *         CREATED)
         */
        @Operation(summary = "Create custom treatment plan (without template)", description = "Create a fully customized treatment plan by manually selecting services, setting prices, defining phases, and specifying quantities. "
                        +
                        "KEY FEATURES: " +
                        "(1) Quantity Expansion: quantity=5 creates 5 items with names 'Service (Lần 1)', 'Service (Lần 2)', etc. "
                        +
                        "(2) Price Override: Can customize prices but must be within 50%-150% of service default. " +
                        "(3) Approval Workflow: Created with approval_status=DRAFT, requires manager approval via API 5.9 before activation. "
                        +
                        "(4) Phase Duration: Set estimated_duration_days for timeline calculation. " +
                        "Example: Create a custom orthodontics plan with 3 phases, 10 adjustment items (quantity=10), custom consultation price.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Custom treatment plan created successfully with approval_status=DRAFT"),
                        @ApiResponse(responseCode = "400", description = "Invalid request - duplicate phase numbers, empty phases, price out of range, or discount exceeds total cost"),
                        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions (requires CREATE_TREATMENT_PLAN)"),
                        @ApiResponse(responseCode = "404", description = "Patient, doctor, or service not found or inactive")
        })
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.CREATE_TREATMENT_PLAN + "')")
        @PostMapping("/patients/{patientCode}/treatment-plans/custom")
        public ResponseEntity<com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse> createCustomTreatmentPlan(
                        @Parameter(description = "Patient code (e.g., BN-1001)", required = true) @PathVariable String patientCode,

                        @Parameter(description = "Request body with plan name, phases, items, quantities, and custom prices", required = true) @RequestBody @jakarta.validation.Valid com.dental.clinic.management.treatment_plans.dto.request.CreateCustomPlanRequest request) {

                log.info("REST request to create CUSTOM treatment plan - Patient: {}, Doctor: {}, Phases: {}",
                                patientCode, request.getDoctorEmployeeCode(), request.getPhases().size());

                com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse response = customTreatmentPlanService
                                .createCustomPlan(patientCode, request);

                log.info("Custom treatment plan created successfully. PlanCode: {}, TotalPrice: {}, ApprovalStatus: DRAFT, TotalItems: {}",
                                response.getPlanCode(),
                                response.getTotalPrice(),
                                response.getPhases().stream()
                                                .mapToInt(p -> p.getItems().size())
                                                .sum());

                return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
        }

        /**
         * API 5.5: Get ALL treatment plans with advanced filtering and RBAC.
         * <p>
         * Smart Endpoint - Automatically filters by user role:
         * - Admin (VIEW_TREATMENT_PLAN_ALL): Can filter by
         * doctorEmployeeCode/patientCode, sees ALL plans
         * - Doctor (VIEW_TREATMENT_PLAN_OWN): Automatically filtered by createdBy =
         * currentEmployee
         * - Patient (VIEW_TREATMENT_PLAN_OWN): Automatically filtered by patient =
         * currentPatient
         * <p>
         * P0 Fixes:
         * - Uses BaseRoleConstants instead of magic numbers (2, 3)
         * - Robust RBAC logic with clear role detection
         * <p>
         * P1 Enhancements:
         * - Date range filters (startDateFrom/To, createdAtFrom/To)
         * - Search term (searches in plan name, patient name)
         * - Performance optimized (JPA Specification + JOIN FETCH)
         * <p>
         * Query Parameters:
         * - page, size, sort (standard pagination)
         * - status (PENDING, ACTIVE, COMPLETED...)
         * - approvalStatus (DRAFT, APPROVED...)
         * - planCode (exact match or prefix)
         * - doctorEmployeeCode (Admin only)
         * - patientCode (Admin only)
         * - startDateFrom/To (date range filter)
         * - createdAtFrom/To (date range filter)
         * - searchTerm (case-insensitive search)
         * <p>
         * Use Cases:
         * - Admin Dashboard: "Show all ACTIVE plans created this month"
         * - Doctor View: "My patients' treatment plans"
         * - Patient Portal: "My treatment plans"
         *
         * @param status             Filter by treatment plan status
         * @param approvalStatus     Filter by approval status (V19)
         * @param planCode           Filter by plan code (starts with)
         * @param doctorEmployeeCode Filter by doctor code (Admin only)
         * @param patientCode        Filter by patient code (Admin only)
         * @param startDateFrom      Filter start date >= this date
         * @param startDateTo        Filter start date <= this date
         * @param createdAtFrom      Filter created date >= this date
         * @param createdAtTo        Filter created date <= this date
         * @param searchTerm         Search in plan name, patient name
         * @param pageable           Pagination parameters
         * @return Page of treatment plan summaries
         */
        @Operation(summary = "Get all treatment plans with advanced filtering (API 5.5)", description = """
                        **Smart RBAC Endpoint** - Automatically filters data based on user role:

                        **Admin Mode** (VIEW_TREATMENT_PLAN_ALL):
                        - Can use `doctorEmployeeCode` and `patientCode` filters
                        - Sees ALL treatment plans in the system
                        - Example: "Show all DRAFT plans for doctor EMP001"

                        **Doctor Mode** (VIEW_TREATMENT_PLAN_OWN):
                        - Automatically filtered by `createdBy = currentEmployee`
                        - Only sees plans they created (their patients)
                        - Admin-only filters are IGNORED for security

                        **Patient Mode** (VIEW_TREATMENT_PLAN_OWN):
                        - Automatically filtered by `patient = currentPatient`
                        - Only sees their own treatment plans
                        - Admin-only filters are IGNORED for security

                        **P1 Enhancements**:
                        - Date range filters for reporting (startDate, createdAt)
                        - Search term for quick lookup (plan name, patient name)
                        - Full pagination support

                        **Performance**: Uses JPA Specification with JOIN FETCH (no N+1 problem)

                        **Example Queries**:
                        - `?status=ACTIVE&approvalStatus=APPROVED` - Active approved plans
                        - `?startDateFrom=2025-01-01&startDateTo=2025-12-31` - Plans starting in 2025
                        - `?searchTerm=orthodontics` - Search "orthodontics" in plan names
                        - `?doctorEmployeeCode=EMP001&page=0&size=20` (Admin only)
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved treatment plans with pagination metadata"),
                        @ApiResponse(responseCode = "403", description = "Access denied - user does not have VIEW_TREATMENT_PLAN_ALL or VIEW_TREATMENT_PLAN_OWN permission"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
        })
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_ALL
                        + "') or " +
                        "hasAuthority('"
                        + com.dental.clinic.management.utils.security.AuthoritiesConstants.VIEW_TREATMENT_PLAN_OWN
                        + "')")
        @GetMapping("/patient-treatment-plans")
        public ResponseEntity<Page<TreatmentPlanSummaryDTO>> getAllTreatmentPlans(
                        @Parameter(description = "Filter by treatment plan status", example = "ACTIVE") @RequestParam(required = false) com.dental.clinic.management.treatment_plans.enums.TreatmentPlanStatus status,

                        @Parameter(description = "Filter by approval status (V19)", example = "APPROVED") @RequestParam(required = false) com.dental.clinic.management.treatment_plans.domain.ApprovalStatus approvalStatus,

                        @Parameter(description = "Filter by plan code (starts with)", example = "PLAN-20250112") @RequestParam(required = false) String planCode,

                        @Parameter(description = "Filter by doctor employee code (Admin only)", example = "EMP001") @RequestParam(required = false) String doctorEmployeeCode,

                        @Parameter(description = "Filter by patient code (Admin only)", example = "BN-1001") @RequestParam(required = false) String patientCode,

                        @Parameter(description = "Filter start date FROM (yyyy-MM-dd)", example = "2025-01-01") @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDateFrom,

                        @Parameter(description = "Filter start date TO (yyyy-MM-dd)", example = "2025-12-31") @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDateTo,

                        @Parameter(description = "Filter created date FROM (yyyy-MM-dd)", example = "2025-01-01") @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate createdAtFrom,

                        @Parameter(description = "Filter created date TO (yyyy-MM-dd)", example = "2025-12-31") @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate createdAtTo,

                        @Parameter(description = "Search term (plan name, patient name)", example = "orthodontics") @RequestParam(required = false) String searchTerm,

                        @Parameter(description = "Pagination parameters (page=0, size=20, sort=createdAt,desc)") Pageable pageable) {
                log.info("REST request to get all treatment plans - status={}, approvalStatus={}, searchTerm={}",
                                status, approvalStatus, searchTerm);

                // Build request DTO
                com.dental.clinic.management.treatment_plans.dto.request.GetAllTreatmentPlansRequest request = com.dental.clinic.management.treatment_plans.dto.request.GetAllTreatmentPlansRequest
                                .builder()
                                .status(status)
                                .approvalStatus(approvalStatus)
                                .planCode(planCode)
                                .doctorEmployeeCode(doctorEmployeeCode)
                                .patientCode(patientCode)
                                .startDateFrom(startDateFrom)
                                .startDateTo(startDateTo)
                                .createdAtFrom(createdAtFrom)
                                .createdAtTo(createdAtTo)
                                .searchTerm(searchTerm)
                                .build();

                Page<TreatmentPlanSummaryDTO> plans = treatmentPlanService.getAllTreatmentPlans(request, pageable);

                log.info("Returning {} treatment plans (page {}/{}, total: {})",
                                plans.getNumberOfElements(), plans.getNumber() + 1, plans.getTotalPages(),
                                plans.getTotalElements());

                return ResponseEntity.ok(plans);
        }
}
