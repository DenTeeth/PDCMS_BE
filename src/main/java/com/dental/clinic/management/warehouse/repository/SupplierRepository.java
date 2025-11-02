package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Supplier entity.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Check if supplier exists by name.
     */
    boolean existsBySupplierName(String supplierName);

    /**
     * Check if supplier exists by phone number.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find supplier by name.
     */
    Optional<Supplier> findBySupplierName(String supplierName);

    /**
     * Find supplier by phone number.
     */
    Optional<Supplier> findByPhoneNumber(String phoneNumber);
}
