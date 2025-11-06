package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.StorageTransaction;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StorageTransactionRepository extends JpaRepository<StorageTransaction, UUID> {

    /**
     * Find all transactions for a batch.
     */
    List<StorageTransaction> findByItemBatch_BatchIdOrderByTransactionDateDesc(UUID batchId);

    /**
     * Find transactions by type.
     */
    List<StorageTransaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionType);

    /**
     * Find loss report (ADJUSTMENT and DESTROY transactions).
     */
    @Query("SELECT st FROM StorageTransaction st WHERE st.transactionType IN ('ADJUSTMENT', 'DESTROY') " +
            "ORDER BY st.transactionDate DESC")
    List<StorageTransaction> findLossReport();

    /**
     * Find transactions by date range.
     */
    @Query("SELECT st FROM StorageTransaction st WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY st.transactionDate DESC")
    List<StorageTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions by performer.
     */
    List<StorageTransaction> findByPerformedByOrderByTransactionDateDesc(UUID performedBy);
}
