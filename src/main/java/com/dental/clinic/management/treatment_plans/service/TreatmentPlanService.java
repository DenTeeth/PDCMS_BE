package com.dental.clinic.management.treatment_plans.service;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.dto.DoctorInfoDTO;
import com.dental.clinic.management.treatment_plans.dto.TreatmentPlanSummaryDTO;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing patient treatment plans.
 * Handles business logic and RBAC for treatment plan operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanService {

    private final PatientTreatmentPlanRepository treatmentPlanRepository;
    private final PatientRepository patientRepository;

    /**
     * Get all treatment plans for a specific patient.
     * <p>
     * RBAC Logic:
     * - VIEW_TREATMENT_PLAN_ALL: Staff can view all patients' plans
     * - VIEW_TREATMENT_PLAN_OWN: Patient can only view their own plans
     *
     * @param patientCode Unique patient code
     * @return List of treatment plan summaries
     * @throws IllegalArgumentException if patient not found
     * @throws AccessDeniedException    if user doesn't have permission
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanSummaryDTO> getTreatmentPlansByPatient(String patientCode) {
        log.info("Getting treatment plans for patient: {}", patientCode);

        // STEP 1: Find patient by code
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> {
                    log.error("Patient not found with code: {}", patientCode);
                    return new IllegalArgumentException("Patient not found with code: " + patientCode);
                });

        // STEP 2: RBAC - Check permissions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        boolean hasViewAllPermission = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("VIEW_TREATMENT_PLAN_ALL"));

        boolean hasViewOwnPermission = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("VIEW_TREATMENT_PLAN_OWN"));

        log.debug("User has VIEW_TREATMENT_PLAN_ALL: {}", hasViewAllPermission);
        log.debug("User has VIEW_TREATMENT_PLAN_OWN: {}", hasViewOwnPermission);

        // If user has VIEW_ALL permission, allow access
        if (hasViewAllPermission) {
            log.info("User has VIEW_TREATMENT_PLAN_ALL permission, allowing access");
        }
        // If user only has VIEW_OWN permission, verify they own the patient record
        else if (hasViewOwnPermission) {
            Integer currentAccountId = getCurrentAccountId(authentication);
            Integer patientAccountId = patient.getAccount() != null ? patient.getAccount().getAccountId() : null;

            log.debug("Current account ID: {}, Patient account ID: {}", currentAccountId, patientAccountId);

            if (patientAccountId == null || !patientAccountId.equals(currentAccountId)) {
                log.warn("Access denied: User {} trying to view treatment plans of patient {} (different account)",
                        currentAccountId, patientCode);
                throw new AccessDeniedException("You can only view your own treatment plans");
            }
            log.info("User verified as owner of patient record, allowing access");
        }
        // No valid permission
        else {
            log.warn("Access denied: User does not have VIEW_TREATMENT_PLAN_ALL or VIEW_TREATMENT_PLAN_OWN permission");
            throw new AccessDeniedException("You do not have permission to view treatment plans");
        }

        // STEP 3: Query treatment plans with JOIN FETCH (avoid N+1)
        List<PatientTreatmentPlan> plans = treatmentPlanRepository.findByPatientIdWithDoctor(patient.getPatientId());
        log.info("Found {} treatment plans for patient {}", plans.size(), patientCode);

        // STEP 4: Convert to DTOs
        return plans.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Extract account ID from JWT token.
     *
     * @param authentication Spring Security authentication object
     * @return Account ID from token
     */
    private Integer getCurrentAccountId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("account_id");
        }
        throw new IllegalStateException("Unable to extract account_id from token");
    }

    /**
     * Convert PatientTreatmentPlan entity to TreatmentPlanSummaryDTO.
     *
     * @param plan Treatment plan entity
     * @return Summary DTO
     */
    private TreatmentPlanSummaryDTO convertToSummaryDTO(PatientTreatmentPlan plan) {
        // Build doctor info
        DoctorInfoDTO doctorInfo = null;
        if (plan.getCreatedBy() != null) {
            doctorInfo = DoctorInfoDTO.builder()
                    .employeeCode(plan.getCreatedBy().getEmployeeCode())
                    .fullName(plan.getCreatedBy().getFullName())
                    .build();
        }

        return TreatmentPlanSummaryDTO.builder()
                .patientPlanId(plan.getPlanId())
                .planName(plan.getPlanName())
                .status(plan.getStatus())
                .doctor(doctorInfo)
                .startDate(plan.getStartDate())
                .expectedEndDate(plan.getExpectedEndDate())
                .totalCost(plan.getTotalPrice())
                .discountAmount(plan.getDiscountAmount())
                .finalCost(plan.getFinalCost())
                .paymentType(plan.getPaymentType())
                .build();
    }
}
