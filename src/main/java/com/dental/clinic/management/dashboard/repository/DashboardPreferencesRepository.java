package com.dental.clinic.management.dashboard.repository;

import com.dental.clinic.management.dashboard.domain.DashboardPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardPreferencesRepository extends JpaRepository<DashboardPreferences, Integer> {
    
    Optional<DashboardPreferences> findByUserId(Integer userId);
    
    void deleteByUserId(Integer userId);
}
