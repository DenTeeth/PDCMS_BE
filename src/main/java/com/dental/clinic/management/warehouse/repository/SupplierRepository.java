package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Supplier entity.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

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

    /**
     * Search suppliers by keyword (name, phone, email, address).
     * Case-insensitive, accent-insensitive search.
     * 
     * @param keyword  search keyword
     * @param pageable pagination with sort (default: newest first)
     * @return page of suppliers matching keyword
     */
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(CAST(FUNCTION('unaccent', s.supplierName) AS string)) LIKE LOWER(CAST(FUNCTION('unaccent', CONCAT('%', :keyword, '%')) AS string)) OR "
            +
            "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(FUNCTION('unaccent', COALESCE(s.email, '')) AS string)) LIKE LOWER(CAST(FUNCTION('unaccent', CONCAT('%', :keyword, '%')) AS string)) OR "
            +
            "LOWER(CAST(FUNCTION('unaccent', s.address) AS string)) LIKE LOWER(CAST(FUNCTION('unaccent', CONCAT('%', :keyword, '%')) AS string))")
    Page<Supplier> searchSuppliers(@Param("keyword") String keyword, Pageable pageable);
}
