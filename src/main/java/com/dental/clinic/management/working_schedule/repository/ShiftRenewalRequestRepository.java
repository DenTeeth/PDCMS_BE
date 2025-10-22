package com.dental.clinic.management.working_schedule.repository;

import com.dental.clinic.management.working_schedule.domain.ShiftRenewalRequest;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ShiftRenewalRequest entity.
 */
@Repository
public interface ShiftRenewalRequestRepository extends JpaRepository<ShiftRenewalRequest, String> {

    /**
     * Find all pending renewal requests for a specific employee.
     * Only returns non-expired requests.
     *
     * @param employeeId the employee ID
     * @param now        current timestamp
     * @return list of pending renewals
     */
    @Query("SELECT srr FROM ShiftRenewalRequest srr " +
            "WHERE srr.employee.employeeId = :employeeId " +
            "AND srr.status = 'PENDING_ACTION' " +
            "AND srr.expiresAt > :now " +
            "ORDER BY srr.expiresAt ASC")
    List<ShiftRenewalRequest> findPendingByEmployeeId(
            @Param("employeeId") Integer employeeId,
            @Param("now") LocalDateTime now);

    /**
     * Find a renewal request by ID and employee ID.
     * Used to verify ownership before allowing response.
     *
     * @param renewalId  the renewal ID
     * @param employeeId the employee ID
     * @return optional renewal request
     */
    @Query("SELECT srr FROM ShiftRenewalRequest srr " +
            "WHERE srr.renewalId = :renewalId " +
            "AND srr.employee.employeeId = :employeeId")
    Optional<ShiftRenewalRequest> findByIdAndEmployeeId(
            @Param("renewalId") String renewalId,
            @Param("employeeId") Integer employeeId);

    /**
     * Check if a renewal request already exists for a specific registration.
     * Prevents duplicate renewals.
     *
     * @param registrationId the expiring registration ID (String format
     *                       ESRyymmddSSS)
     * @param status         the status to check
     * @return true if exists
     */
    @Query("SELECT COUNT(srr) > 0 FROM ShiftRenewalRequest srr " +
            "WHERE srr.expiringRegistration.registrationId = :registrationId " +
            "AND srr.status = :status")
    boolean existsByRegistrationIdAndStatus(
            @Param("registrationId") String registrationId,
            @Param("status") RenewalStatus status);

    /**
     * Find all expired renewal requests that need status update.
     * Used by cron job to mark expired renewals.
     *
     * @param now current timestamp
     * @return list of expired renewals still marked as PENDING_ACTION
     */
    @Query("SELECT srr FROM ShiftRenewalRequest srr " +
            "WHERE srr.status = 'PENDING_ACTION' " +
            "AND srr.expiresAt <= :now")
    List<ShiftRenewalRequest> findExpiredPendingRenewals(@Param("now") LocalDateTime now);

    /**
     * Find all renewal requests for a specific employee.
     *
     * @param employeeId the employee ID
     * @return list of all renewals
     */
    List<ShiftRenewalRequest> findByEmployeeEmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    /**
     * Find renewal request by expiring registration ID.
     *
     * @param registrationId the registration ID (String format)
     * @return list of renewals for this registration
     */
    List<ShiftRenewalRequest> findByExpiringRegistrationRegistrationId(String registrationId);
}
