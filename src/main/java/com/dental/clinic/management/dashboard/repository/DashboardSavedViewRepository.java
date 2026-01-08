package com.dental.clinic.management.dashboard.repository;

import com.dental.clinic.management.dashboard.domain.DashboardSavedView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardSavedViewRepository extends JpaRepository<DashboardSavedView, Integer> {
    
    List<DashboardSavedView> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<DashboardSavedView> findByIsPublicTrueOrderByCreatedAtDesc();
    
    Optional<DashboardSavedView> findByUserIdAndIsDefaultTrue(Integer userId);
    
    @Modifying
    @Query("UPDATE DashboardSavedView v SET v.isDefault = false WHERE v.userId = :userId")
    void clearDefaultForUser(@Param("userId") Integer userId);
}
