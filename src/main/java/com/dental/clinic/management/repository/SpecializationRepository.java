package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link Specialization} entity.
 */
@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, String> {

    Optional<Specialization> findOneBySpecializationCode(String specializationCode);

    Boolean existsBySpecializationCode(String specializationCode);

    Optional<Specialization> findOneBySpecializationName(String specializationName);

    @Query("SELECT s FROM Specialization s WHERE s.isActive = true")
    List<Specialization> findAllActiveSpecializations();
}
