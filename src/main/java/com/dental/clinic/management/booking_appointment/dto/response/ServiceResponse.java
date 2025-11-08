package com.dental.clinic.management.booking_appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for dental service
 */
@Schema(description = "Dental service response")
public class ServiceResponse {

    @Schema(description = "Service ID", example = "1")
    private Integer serviceId;

    @Schema(description = "Service code", example = "SV-CAOVOI")
    private String serviceCode;

    @Schema(description = "Service name", example = "Cạo vôi răng và Đánh bóng")
    private String serviceName;

    @Schema(description = "Service description", example = "Lấy sách vôi răng...")
    private String description;

    @Schema(description = "Default duration in minutes", example = "30")
    private Integer defaultDurationMinutes;

    @Schema(description = "Default buffer time in minutes", example = "10")
    private Integer defaultBufferMinutes;

    @Schema(description = "Service price (VND)", example = "300000")
    private BigDecimal price;

    @Schema(description = "Specialization ID", example = "1")
    private Integer specializationId;

    @Schema(description = "Specialization name", example = "Chuyên khoa răng hàm mặt")
    private String specializationName;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Created at", example = "2025-10-29T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at", example = "2025-10-29T15:45:00")
    private LocalDateTime updatedAt;

    public ServiceResponse() {
    }

    public ServiceResponse(Integer serviceId, String serviceCode, String serviceName, String description,
            Integer defaultDurationMinutes, Integer defaultBufferMinutes, BigDecimal price,
            Integer specializationId, String specializationName, Boolean isActive,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.defaultBufferMinutes = defaultBufferMinutes;
        this.price = price;
        this.specializationId = specializationId;
        this.specializationName = specializationName;
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

    public Integer getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Integer specializationId) {
        this.specializationId = specializationId;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
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
}
