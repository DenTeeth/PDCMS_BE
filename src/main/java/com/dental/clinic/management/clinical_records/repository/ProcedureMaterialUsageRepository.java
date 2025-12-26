package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.ProcedureMaterialUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcedureMaterialUsageRepository extends JpaRepository<ProcedureMaterialUsage, Long> {

    /**
     * Find all material usage records for a procedure
     */
    List<ProcedureMaterialUsage> findByProcedure_ProcedureId(Integer procedureId);

    /**
     * Find all material usage for an item (for reporting)
     */
    @Query("SELECT u FROM ProcedureMaterialUsage u " +
           "WHERE u.itemMaster.itemMasterId = :itemMasterId " +
           "AND u.recordedAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY u.recordedAt DESC")
    List<ProcedureMaterialUsage> findByItemAndDateRange(
            @Param("itemMasterId") Long itemMasterId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Find all material usage within date range (for reporting)
     */
    @Query("SELECT u FROM ProcedureMaterialUsage u " +
           "WHERE u.recordedAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY u.recordedAt DESC")
    List<ProcedureMaterialUsage> findByDateRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
