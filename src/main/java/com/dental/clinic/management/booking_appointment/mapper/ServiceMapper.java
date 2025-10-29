package com.dental.clinic.management.booking_appointment.mapper;

import com.dental.clinic.management.booking_appointment.domain.DentalService;
import com.dental.clinic.management.booking_appointment.dto.request.CreateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.response.ServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for DentalService entity and DTOs
 */
@Component
public class ServiceMapper {

    /**
     * Convert DentalService entity to ServiceResponse DTO
     */
    public ServiceResponse toResponse(DentalService service) {
        if (service == null) {
            return null;
        }

        ServiceResponse response = new ServiceResponse();
        response.setServiceId(service.getServiceId());
        response.setServiceCode(service.getServiceCode());
        response.setServiceName(service.getServiceName());
        response.setDescription(service.getDescription());
        response.setDefaultDurationMinutes(service.getDefaultDurationMinutes());
        response.setDefaultBufferMinutes(service.getDefaultBufferMinutes());
        response.setPrice(service.getPrice());
        response.setIsActive(service.getIsActive());
        response.setCreatedAt(service.getCreatedAt());
        response.setUpdatedAt(service.getUpdatedAt());

        // Map specialization if exists
        if (service.getSpecialization() != null) {
            response.setSpecializationId(service.getSpecialization().getSpecializationId());
            response.setSpecializationName(service.getSpecialization().getSpecializationName());
        }

        return response;
    }

    /**
     * Convert CreateServiceRequest to DentalService entity
     */
    public DentalService toEntity(CreateServiceRequest request) {
        if (request == null) {
            return null;
        }

        DentalService service = new DentalService();
        service.setServiceCode(request.getServiceCode());
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setDefaultDurationMinutes(request.getDefaultDurationMinutes());
        service.setDefaultBufferMinutes(request.getDefaultBufferMinutes());
        service.setPrice(request.getPrice());
        service.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Specialization will be set in service layer
        return service;
    }
}
