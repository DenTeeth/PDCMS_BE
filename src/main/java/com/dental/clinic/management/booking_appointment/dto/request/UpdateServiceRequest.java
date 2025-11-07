package com.dental.clinic.management.booking_appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Request DTO for updating a dental service
 */
@Schema(description = "Request body for updating a service")
public class UpdateServiceRequest {

    @Schema(description = "Unique service code", example = "SV-CAOVOI-UPDATED")
    private String serviceCode;

    @Schema(description = "Service name", example = "CÃ¡ÂºÂ¡o vÃƒÂ´i rÃ„Æ’ng vÃƒÂ  Ã„ÂÃƒÂ¡nh bÃƒÂ³ng (VIP)")
    private String serviceName;

    @Schema(description = "Service description", example = "LÃ¡ÂºÂ¥y sÃ¡ÂºÂ¡ch vÃƒÂ´i rÃ„Æ’ng vÃ¡Â»â€ºi cÃƒÂ´ng nghÃ¡Â»â€¡ mÃ¡Â»â€ºi")
    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Schema(description = "Default duration in minutes", example = "35")
    private Integer defaultDurationMinutes;

    @Min(value = 0, message = "Buffer time cannot be negative")
    @Schema(description = "Default buffer time in minutes", example = "10")
    private Integer defaultBufferMinutes;

    @Min(value = 0, message = "Price cannot be negative")
    @Schema(description = "Service price (VND)", example = "350000")
    private BigDecimal price;

    @Schema(description = "Specialization ID (nullable)", example = "1")
    private Integer specializationId;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    public UpdateServiceRequest() {
    }

    public UpdateServiceRequest(String serviceCode, String serviceName, String description,
            Integer defaultDurationMinutes, Integer defaultBufferMinutes,
            BigDecimal price, Integer specializationId, Boolean isActive) {
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.defaultBufferMinutes = defaultBufferMinutes;
        this.price = price;
        this.specializationId = specializationId;
        this.isActive = isActive;
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

    public Integer getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Integer specializationId) {
        this.specializationId = specializationId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
