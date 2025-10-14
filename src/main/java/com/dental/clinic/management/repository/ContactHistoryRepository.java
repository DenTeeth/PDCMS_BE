package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.ContactHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactHistoryRepository extends JpaRepository<ContactHistory, String> {
    List<ContactHistory> findByContactIdOrderByCreatedAtDesc(String contactId);

    // for daily sequence generation
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
