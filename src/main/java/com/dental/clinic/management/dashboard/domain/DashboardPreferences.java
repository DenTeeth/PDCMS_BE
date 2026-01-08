package com.dental.clinic.management.dashboard.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Dashboard user preferences entity
 * Stores user-specific dashboard settings (widget layout, visibility, etc.)
 */
@Entity
@Table(name = "dashboard_preferences")
public class DashboardPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "layout", columnDefinition = "TEXT")
    private String layout; // JSON string for widget positions

    @Column(name = "visible_widgets", columnDefinition = "TEXT")
    private String visibleWidgets; // JSON array of visible widget IDs

    @Column(name = "default_date_range")
    private String defaultDateRange; // TODAY, THIS_WEEK, THIS_MONTH, etc.

    @Column(name = "auto_refresh")
    private Boolean autoRefresh;

    @Column(name = "refresh_interval")
    private Integer refreshInterval; // in seconds

    @Column(name = "chart_type_preference")
    private String chartTypePreference; // CHART, TABLE, BOTH

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getVisibleWidgets() {
        return visibleWidgets;
    }

    public void setVisibleWidgets(String visibleWidgets) {
        this.visibleWidgets = visibleWidgets;
    }

    public String getDefaultDateRange() {
        return defaultDateRange;
    }

    public void setDefaultDateRange(String defaultDateRange) {
        this.defaultDateRange = defaultDateRange;
    }

    public Boolean getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public Integer getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getChartTypePreference() {
        return chartTypePreference;
    }

    public void setChartTypePreference(String chartTypePreference) {
        this.chartTypePreference = chartTypePreference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
