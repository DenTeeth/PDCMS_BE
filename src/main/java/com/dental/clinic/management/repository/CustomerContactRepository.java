package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.CustomerContact;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContact, String> {
    Optional<CustomerContact> findOneByContactId(String contactId);

    boolean existsByPhone(String phone);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // new helper used for auto-assign + simple metrics
    long countByAssignedToAndStatus(String assignedTo, CustomerContactStatus status);
}
