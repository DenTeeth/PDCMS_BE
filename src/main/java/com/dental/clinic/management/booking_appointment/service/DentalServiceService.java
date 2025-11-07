package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.DentalService;
import com.dental.clinic.management.booking_appointment.dto.request.CreateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.response.ServiceResponse;
import com.dental.clinic.management.booking_appointment.mapper.ServiceMapper;
import com.dental.clinic.management.booking_appointment.repository.DentalServiceRepository;
import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.specialization.domain.Specialization;
import com.dental.clinic.management.specialization.repository.SpecializationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing dental services
 */
@Service
public class DentalServiceService {

    private static final Logger log = LoggerFactory.getLogger(DentalServiceService.class);

    private final DentalServiceRepository serviceRepository;
    private final SpecializationRepository specializationRepository;
    private final ServiceMapper serviceMapper;

    public DentalServiceService(DentalServiceRepository serviceRepository,
            SpecializationRepository specializationRepository,
            ServiceMapper serviceMapper) {
        this.serviceRepository = serviceRepository;
        this.specializationRepository = specializationRepository;
        this.serviceMapper = serviceMapper;
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Get all services with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Boolean isActive,
            Integer specializationId,
            String keyword) {

        log.debug(
                "Request to get all services - page: {}, size: {}, sortBy: {}, sortDirection: {}, isActive: {}, specializationId: {}, keyword: {}",
                page, size, sortBy, sortDirection, isActive, specializationId, keyword);

        // Validate and adjust page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }

        // Create sort
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Query with filters
        Page<DentalService> servicesPage = serviceRepository.findWithFilters(
                isActive,
                specializationId,
                keyword,
                pageable);

        log.debug("Found {} services", servicesPage.getTotalElements());

        return servicesPage.map(serviceMapper::toResponse);
    }

    /**
     * Get service by ID
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Integer serviceId) {
        log.debug("Request to get service by ID: {}", serviceId);

        DentalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with ID: " + serviceId,
                        "service",
                        "notfound"));

        return serviceMapper.toResponse(service);
    }

    /**
     * Get service by code
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceByCode(String serviceCode) {
        log.debug("Request to get service by code: {}", serviceCode);

        DentalService service = serviceRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with code: " + serviceCode,
                        "service",
                        "notfound"));

        return serviceMapper.toResponse(service);
    }

    /**
     * Create a new service
     */
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        log.debug("Request to create new service: {}", request.getServiceCode());

        // Validate unique service code
        if (serviceRepository.existsByServiceCode(request.getServiceCode())) {
            throw new BadRequestAlertException(
                    "Service code already exists: " + request.getServiceCode(),
                    "service",
                    "SERVICE_CODE_EXISTS");
        }

        // Validate specialization if provided
        Specialization specialization = null;
        if (request.getSpecializationId() != null) {
            specialization = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Specialization not found with ID: " + request.getSpecializationId(),
                            "specialization",
                            "SPECIALIZATION_NOT_FOUND"));
        }

        // Create service
        DentalService service = serviceMapper.toEntity(request);
        service.setSpecialization(specialization);

        DentalService savedService = serviceRepository.save(service);

        log.info("Created service with ID: {} and code: {}", savedService.getServiceId(),
                savedService.getServiceCode());

        return serviceMapper.toResponse(savedService);
    }

    /**
     * Update service by service code
     */
    @Transactional
    public ServiceResponse updateService(String serviceCode, UpdateServiceRequest request) {
        log.debug("Request to update service code: {}", serviceCode);

        // Find existing service by code
        DentalService service = serviceRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with code: " + serviceCode,
                        "service",
                        "notfound"));

        // Validate unique service code (if changed)
        if (request.getServiceCode() != null &&
                !request.getServiceCode().equals(service.getServiceCode()) &&
                serviceRepository.existsByServiceCodeAndServiceIdNot(request.getServiceCode(),
                        service.getServiceId())) {
            throw new BadRequestAlertException(
                    "Service code already exists: " + request.getServiceCode(),
                    "service",
                    "SERVICE_CODE_EXISTS");
        }

        // Update fields
        if (request.getServiceCode() != null) {
            service.setServiceCode(request.getServiceCode());
        }
        if (request.getServiceName() != null) {
            service.setServiceName(request.getServiceName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getDefaultDurationMinutes() != null) {
            service.setDefaultDurationMinutes(request.getDefaultDurationMinutes());
        }
        if (request.getDefaultBufferMinutes() != null) {
            service.setDefaultBufferMinutes(request.getDefaultBufferMinutes());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        // Update specialization if provided
        if (request.getSpecializationId() != null) {
            Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Specialization not found with ID: " + request.getSpecializationId(),
                            "specialization",
                            "SPECIALIZATION_NOT_FOUND"));
            service.setSpecialization(specialization);
        }

        DentalService updatedService = serviceRepository.save(service);

        log.info("Updated service code: {}", serviceCode);

        return serviceMapper.toResponse(updatedService);
    }

    /**
     * Soft delete service by service ID (set isActive = false)
     * RESTful DELETE endpoint using serviceId
     */
    @Transactional
    public void deleteServiceById(Integer serviceId) {
        log.debug("Request to soft delete service ID: {}", serviceId);

        DentalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with ID: " + serviceId,
                        "service",
                        "notfound"));

        service.setIsActive(false);
        serviceRepository.save(service);

        log.info("Soft deleted service ID: {}", serviceId);
    }

    /**
     * Soft delete service by service code (set isActive = false)
     * Legacy endpoint for backward compatibility
     */
    @Transactional
    public void deleteServiceByCode(String serviceCode) {
        log.debug("Request to soft delete service code: {}", serviceCode);

        DentalService service = serviceRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with code: " + serviceCode,
                        "service",
                        "notfound"));

        service.setIsActive(false);
        serviceRepository.save(service);

        log.info("Soft deleted service code: {}", serviceCode);
    }

    /**
     * Toggle service active status (activate Ã¢â€ â€ deactivate)
     * RESTful PATCH endpoint - returns updated service
     */
    @Transactional
    public ServiceResponse toggleServiceStatus(Integer serviceId) {
        log.debug("Request to toggle service status for ID: {}", serviceId);

        DentalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with ID: " + serviceId,
                        "service",
                        "notfound"));

        // Toggle: if active Ã¢â€ â€™ inactive, if inactive Ã¢â€ â€™ active
        boolean newStatus = !service.getIsActive();
        service.setIsActive(newStatus);
        DentalService savedService = serviceRepository.save(service);

        log.info("Toggled service ID {} status to: {}", serviceId, newStatus);
        return serviceMapper.toResponse(savedService);
    }

    /**
     * Activate service (set isActive = true)
     */
    @Transactional
    public void activateService(Integer serviceId) {
        log.debug("Request to activate service ID: {}", serviceId);

        DentalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Service not found with ID: " + serviceId,
                        "service",
                        "notfound"));

        service.setIsActive(true);
        serviceRepository.save(service);

        log.info("Activated service ID: {}", serviceId);
    }
}
