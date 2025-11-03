package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Inventory entity.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory items by warehouse type with pagination.
     */
    Page<Inventory> findByWarehouseType(Inventory.WarehouseType warehouseType, Pageable pageable);

    /**
     * Find inventory items by item name (partial match, case-insensitive) with
     * pagination.
     */
    Page<Inventory> findByItemNameContainingIgnoreCase(String itemName, Pageable pageable);

    /**
     * Find inventory items by warehouse type AND item name with pagination.
     */
    Page<Inventory> findByWarehouseTypeAndItemNameContainingIgnoreCase(
            Inventory.WarehouseType warehouseType, String itemName, Pageable pageable);

    /**
     * Check if item name already exists.
     */
    boolean existsByItemName(String itemName);

    /**
     * Check if item name exists excluding current inventory ID.
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i " +
            "WHERE i.itemName = :itemName AND i.inventoryId != :inventoryId")
    boolean existsByItemNameAndInventoryIdNot(@Param("itemName") String itemName,
            @Param("inventoryId") Long inventoryId);

    /**
     * Find by item name exactly.
     */
    Optional<Inventory> findByItemName(String itemName);
}