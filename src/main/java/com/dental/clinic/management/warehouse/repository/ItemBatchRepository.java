package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.ItemBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemBatchRepository extends JpaRepository<ItemBatch, UUID> {

    /**
     * Find batch by item master ID and lot number (UNIQUE constraint).
     */
    Optional<ItemBatch> findByItemMaster_ItemMasterIdAndLotNumber(UUID itemMasterId, String lotNumber);

    /**
     * Find all batches for an item master with stock available, sorted by FEFO.
     */
    @Query("SELECT ib FROM ItemBatch ib WHERE ib.itemMaster.itemMasterId = :itemMasterId " +
            "AND ib.quantityOnHand > 0 ORDER BY ib.expiryDate ASC NULLS LAST")
    List<ItemBatch> findAvailableBatchesByItemMasterIdOrderByExpiryAsc(@Param("itemMasterId") UUID itemMasterId);

    /**
     * Find expiring batches (within days threshold).
     */
    @Query("SELECT ib FROM ItemBatch ib WHERE ib.expiryDate IS NOT NULL " +
            "AND ib.expiryDate <= :thresholdDate AND ib.quantityOnHand > 0 " +
            "ORDER BY ib.expiryDate ASC")
    List<ItemBatch> findExpiringBatches(@Param("thresholdDate") LocalDate thresholdDate);

    /**
     * Find expired batches.
     */
    @Query("SELECT ib FROM ItemBatch ib WHERE ib.expiryDate IS NOT NULL " +
            "AND ib.expiryDate < :today AND ib.quantityOnHand > 0")
    List<ItemBatch> findExpiredBatches(@Param("today") LocalDate today);

    /**
     * Find all batches for an item master.
     */
    List<ItemBatch> findByItemMaster_ItemMasterId(UUID itemMasterId);
}
