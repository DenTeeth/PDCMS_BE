package com.dental.clinic.management.booking_appointment.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing a service that a room can perform.
 * <p>
 * Used in GET /api/v1/rooms/{roomCode}/services response
 * to show compatible services for a room.
 * </p>
 *
 * <p>
 * <b>Example JSON:</b>
 * </p>
 * 
 * <pre>
 * {
 *   "serviceId": 35,
 *   "serviceCode": "IMPL_SURGERY_KR",
 *   "serviceName": "Phẫu thuật đặt trụ Implant Hàn Quốc",
 *   "price": 15000000
 * }
 * </pre>
 *
 * @since V16
 */
public class CompatibleServiceDTO {

    /**
     * Service ID (database primary key).
     */
    private Long serviceId;

    /**
     * Service code (business key, e.g., "IMPL_SURGERY_KR").
     */
    private String serviceCode;

    /**
     * Service name (Vietnamese display name).
     */
    private String serviceName;

    /**
     * Service price in VND.
     */
    private BigDecimal price;

    public CompatibleServiceDTO() {
    }

    public CompatibleServiceDTO(Long serviceId, String serviceCode, String serviceName, BigDecimal price) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.price = price;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
