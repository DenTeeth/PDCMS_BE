package com.dental.clinic.management.treatment_plans.repository;

import com.dental.clinic.management.treatment_plans.domain.template.TreatmentPlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TreatmentPlanTemplate entity.
 * Provides methods to fetch templates with their phases and services.
 */
@Repository
public interface TreatmentPlanTemplateRepository extends JpaRepository<TreatmentPlanTemplate, Long> {

    /**
     * Find template by code (simple, without eager loading)
     */
    Optional<TreatmentPlanTemplate> findByTemplateCode(String templateCode);

    /**
     * Find template by code WITH phases (NOT services).
     *
     * Used by API 5.3 to create patient plan from template.
     * Fetches template + phases in one query, services loaded lazily.
     * This avoids MultipleBagFetchException (cannot fetch 2 collections
     * simultaneously).
     *
     * @param templateCode The template code (e.g., "TPL_ORTHO_METAL")
     * @return Optional with template+phases loaded, or empty if not found
     */
    @Query("SELECT DISTINCT t FROM TreatmentPlanTemplate t LEFT JOIN FETCH t.templatePhases WHERE t.templateCode = :templateCode")
    Optional<TreatmentPlanTemplate> findByTemplateCodeWithPhasesAndServices(@Param("templateCode") String templateCode);

    /**
     * Check if template exists and is active
     */
    boolean existsByTemplateCodeAndIsActiveTrue(String templateCode);
}
