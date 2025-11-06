package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Optional<Supplier> findBySupplierName(String supplierName);

    boolean existsBySupplierName(String supplierName);

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> searchByKeyword(@Param("keyword") String keyword);
}
