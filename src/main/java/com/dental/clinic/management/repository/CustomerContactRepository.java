package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContact, String> {
    Optional<CustomerContact> findOneByContactId(String contactId);
}
