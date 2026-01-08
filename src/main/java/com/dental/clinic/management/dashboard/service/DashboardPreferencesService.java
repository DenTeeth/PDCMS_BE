package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.domain.DashboardPreferences;
import com.dental.clinic.management.dashboard.dto.DashboardPreferencesDTO;
import com.dental.clinic.management.dashboard.repository.DashboardPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPreferencesService {

    private final DashboardPreferencesRepository preferencesRepository;

    /**
     * Get user's dashboard preferences
     * Cached for 1 hour
     */
    @Cacheable(value = "dashboardPreferences", key = "#userId", unless = "#result == null")
    public DashboardPreferencesDTO getUserPreferences(Integer userId) {
        log.info("Getting dashboard preferences for user: {}", userId);
        
        return preferencesRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElse(createDefaultPreferences(userId));
    }

    /**
     * Save or update user's dashboard preferences
     * Evicts cache to ensure fresh data on next read
     */
    @Transactional
    @CacheEvict(value = "dashboardPreferences", key = "#userId")
    public DashboardPreferencesDTO saveUserPreferences(Integer userId, DashboardPreferencesDTO dto) {
        log.info("Saving dashboard preferences for user: {}", userId);
        
        DashboardPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElse(new DashboardPreferences());
        
        preferences.setUserId(userId);
        preferences.setLayout(dto.getLayout());
        preferences.setVisibleWidgets(dto.getVisibleWidgets());
        preferences.setDefaultDateRange(dto.getDefaultDateRange());
        preferences.setAutoRefresh(dto.getAutoRefresh());
        preferences.setRefreshInterval(dto.getRefreshInterval());
        preferences.setChartTypePreference(dto.getChartTypePreference());
        
        DashboardPreferences saved = preferencesRepository.save(preferences);
        return toDTO(saved);
    }

    /**
     * Reset user's dashboard preferences to default
     * Evicts cache
     */
    @Transactional
    @CacheEvict(value = "dashboardPreferences", key = "#userId")
    public void resetUserPreferences(Integer userId) {
        log.info("Resetting dashboard preferences for user: {}", userId);
        preferencesRepository.deleteByUserId(userId);
    }

    private DashboardPreferencesDTO toDTO(DashboardPreferences entity) {
        return DashboardPreferencesDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .layout(entity.getLayout())
                .visibleWidgets(entity.getVisibleWidgets())
                .defaultDateRange(entity.getDefaultDateRange())
                .autoRefresh(entity.getAutoRefresh())
                .refreshInterval(entity.getRefreshInterval())
                .chartTypePreference(entity.getChartTypePreference())
                .build();
    }

    private DashboardPreferencesDTO createDefaultPreferences(Integer userId) {
        return DashboardPreferencesDTO.builder()
                .userId(userId)
                .defaultDateRange("THIS_MONTH")
                .autoRefresh(false)
                .refreshInterval(300) // 5 minutes
                .chartTypePreference("CHART")
                .build();
    }
}
