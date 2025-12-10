package com.dental.clinic.management.patient.repository;

import com.dental.clinic.management.patient.domain.PatientUnbanAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for patient unban audit logs.
 */
@Repository
public interface PatientUnbanAuditLogRepository extends JpaRepository<PatientUnbanAuditLog, Long> {

    /**
     * Find all audit logs for a specific patient.
     */
    List<PatientUnbanAuditLog> findByPatientIdOrderByTimestampDesc(Integer patientId);

    /**
     * Find audit logs by performer.
     */
    Page<PatientUnbanAuditLog> findByPerformedByOrderByTimestampDesc(String performedBy, Pageable pageable);

    /**
     * Find audit logs within date range.
     */
    @Query("SELECT a FROM PatientUnbanAuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<PatientUnbanAuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find audit logs by role (for manager review).
     */
    Page<PatientUnbanAuditLog> findByPerformedByRoleOrderByTimestampDesc(String role, Pageable pageable);
}
