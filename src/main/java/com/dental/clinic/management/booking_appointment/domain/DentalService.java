package com.dental.clinic.management.booking_appointment.domain;

import com.dental.clinic.management.specialization.domain.Specialization;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DentalService entity - Represents dental services (treatments)
 * Used for appointment scheduling and treatment planning
 */
@Entity
@Table(name = "services")
public class DentalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @NotBlank(message = "Service code cannot be blank")
    @Column(name = "service_code", unique = true, nullable = false, length = 50)
    private String serviceCode;

    @NotBlank(message = "Service name cannot be blank")
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Default duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(name = "default_duration_minutes", nullable = false)
    private Integer defaultDurationMinutes;

    @Min(value = 0, message = "Buffer time cannot be negative")
    @Column(name = "default_buffer_minutes", nullable = false)
    private Integer defaultBufferMinutes = 0;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", foreignKey = @ForeignKey(name = "fk_service_specialization"))
    private Specialization specialization;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DentalService() {
    }

    public DentalService(Integer serviceId, String serviceCode, String serviceName, String description,
            Integer defaultDurationMinutes, Integer defaultBufferMinutes, BigDecimal price,
            Specialization specialization, Boolean isActive, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.defaultBufferMinutes = defaultBufferMinutes;
        this.price = price;
        this.specialization = specialization;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    public Integer getDefaultBufferMinutes() {
        return defaultBufferMinutes;
    }

    public void setDefaultBufferMinutes(Integer defaultBufferMinutes) {
        this.defaultBufferMinutes = defaultBufferMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.defaultBufferMinutes == null) {
            this.defaultBufferMinutes = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DentalService))
            return false;
        DentalService that = (DentalService) o;
        return serviceId != null && serviceId.equals(that.getServiceId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DentalService{" +
                "serviceId=" + serviceId +
                ", serviceCode='" + serviceCode + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", price=" + price +
                ", duration=" + defaultDurationMinutes +
                ", isActive=" + isActive +
                '}';
    }
}
