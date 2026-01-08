package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.domain.DashboardSavedView;
import com.dental.clinic.management.dashboard.dto.DashboardSavedViewDTO;
import com.dental.clinic.management.dashboard.repository.DashboardSavedViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardSavedViewService {

    private final DashboardSavedViewRepository savedViewRepository;

    /**
     * Get all saved views for a user (including public views)
     * Cached for 30 minutes
     */
    @Cacheable(value = "dashboardSavedViews", key = "'user_' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<DashboardSavedViewDTO> getUserViews(Integer userId) {
        log.info("Getting saved views for user: {}", userId);
        
        List<DashboardSavedView> userViews = savedViewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<DashboardSavedView> publicViews = savedViewRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        
        // Combine and deduplicate
        List<DashboardSavedView> allViews = new java.util.ArrayList<>(userViews);
        publicViews.stream()
                .filter(pv -> userViews.stream().noneMatch(uv -> uv.getId().equals(pv.getId())))
                .forEach(allViews::add);
        
        return allViews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific saved view by ID
     * Cached for 30 minutes
     */
    @Cacheable(value = "dashboardSavedViews", key = "'view_' + #viewId", unless = "#result == null")
    public DashboardSavedViewDTO getView(Integer viewId) {
        log.info("Getting saved view: {}", viewId);
        return savedViewRepository.findById(viewId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("View not found: " + viewId));
    }

    /**
     * Get user's default view
     * Cached for 30 minutes
     */
    @Cacheable(value = "dashboardSavedViews", key = "'default_' + #userId", unless = "#result == null")
    public DashboardSavedViewDTO getUserDefaultView(Integer userId) {
        log.info("Getting default view for user: {}", userId);
        return savedViewRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Create a new saved view
     * Evicts user's view cache
     */
    @Transactional
    @CacheEvict(value = "dashboardSavedViews", allEntries = true)
    public DashboardSavedViewDTO createView(Integer userId, DashboardSavedViewDTO dto) {
        log.info("Creating saved view for user: {}", userId);
        
        DashboardSavedView view = new DashboardSavedView();
        view.setUserId(userId);
        view.setViewName(dto.getViewName());
        view.setDescription(dto.getDescription());
        view.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        view.setFilters(dto.getFilters());
        view.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        
        // If setting as default, clear other defaults
        if (Boolean.TRUE.equals(view.getIsDefault())) {
            savedViewRepository.clearDefaultForUser(userId);
        }
        
        DashboardSavedView saved = savedViewRepository.save(view);
        return toDTO(saved);
    }

    /**
     * Update an existing saved view
     * Evicts all view caches
     */
    @Transactional
    @CacheEvict(value = "dashboardSavedViews", allEntries = true)
    public DashboardSavedViewDTO updateView(Integer viewId, DashboardSavedViewDTO dto) {
        log.info("Updating saved view: {}", viewId);
        
        DashboardSavedView view = savedViewRepository.findById(viewId)
                .orElseThrow(() -> new RuntimeException("View not found: " + viewId));
        
        view.setViewName(dto.getViewName());
        view.setDescription(dto.getDescription());
        view.setIsPublic(dto.getIsPublic());
        view.setFilters(dto.getFilters());
        
        // Handle default flag
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(view.getIsDefault())) {
            savedViewRepository.clearDefaultForUser(view.getUserId());
            view.setIsDefault(true);
        } else if (Boolean.FALSE.equals(dto.getIsDefault())) {
            view.setIsDefault(false);
        }
        
        DashboardSavedView updated = savedViewRepository.save(view);
        return toDTO(updated);
    }

    /**
     * Delete a saved view
     * Evicts all view caches
     */
    @Transactional
    @CacheEvict(value = "dashboardSavedViews", allEntries = true)
    public void deleteView(Integer viewId) {
        log.info("Deleting saved view: {}", viewId);
        savedViewRepository.deleteById(viewId);
    }

    /**
     * Set a view as user's default
     * Evicts all view caches
     */
    @Transactional
    @CacheEvict(value = "dashboardSavedViews", allEntries = true)
    public DashboardSavedViewDTO setAsDefault(Integer userId, Integer viewId) {
        log.info("Setting view {} as default for user: {}", viewId, userId);
        
        DashboardSavedView view = savedViewRepository.findById(viewId)
                .orElseThrow(() -> new RuntimeException("View not found: " + viewId));
        
        // Clear other defaults
        savedViewRepository.clearDefaultForUser(userId);
        
        // Set this as default
        view.setIsDefault(true);
        DashboardSavedView updated = savedViewRepository.save(view);
        
        return toDTO(updated);
    }

    private DashboardSavedViewDTO toDTO(DashboardSavedView entity) {
        return DashboardSavedViewDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .viewName(entity.getViewName())
                .description(entity.getDescription())
                .isPublic(entity.getIsPublic())
                .filters(entity.getFilters())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
