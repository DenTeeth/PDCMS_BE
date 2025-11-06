package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, UUID> {

    Optional<ItemMaster> findByItemName(String itemName);

    boolean existsByItemName(String itemName);

    List<ItemMaster> findByCategory_CategoryId(UUID categoryId);

    List<ItemMaster> findByItemNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT im FROM ItemMaster im JOIN im.compatibleSuppliers s WHERE s.supplierId = :supplierId")
    List<ItemMaster> findByCompatibleSupplierId(UUID supplierId);
}
