package com.dental.clinic.management.clinical_records.repository;

import com.dental.clinic.management.clinical_records.domain.VitalSignsReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalSignsReferenceRepository extends JpaRepository<VitalSignsReference, Integer> {

       @Query("SELECT v FROM VitalSignsReference v WHERE v.vitalType = :vitalType " +
                     "AND v.isActive = true " +
                     "AND v.ageMin <= :age " +
                     "AND (v.ageMax IS NULL OR v.ageMax >= :age) " +
                     "ORDER BY v.effectiveDate DESC")
       Optional<VitalSignsReference> findByVitalTypeAndAge(
                     @Param("vitalType") String vitalType,
                     @Param("age") Integer age);

       @Query("SELECT v FROM VitalSignsReference v WHERE v.isActive = true " +
                     "AND v.ageMin <= :age " +
                     "AND (v.ageMax IS NULL OR v.ageMax >= :age)")
       List<VitalSignsReference> findAllByAge(@Param("age") Integer age);

       List<VitalSignsReference> findByVitalTypeAndIsActive(String vitalType, Boolean isActive);

       List<VitalSignsReference> findByIsActiveOrderByVitalTypeAscAgeMinAsc(Boolean isActive);
}
