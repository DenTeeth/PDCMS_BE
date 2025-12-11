package com.dental.clinic.management.listener;

import com.dental.clinic.management.service.domain.DentalService;
import com.dental.clinic.management.service.service.ServiceRedisService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceEntityListener {

    private static ServiceRedisService serviceRedisService;

    @Autowired
    public void setServiceRedisService(ServiceRedisService service) {
        ServiceEntityListener.serviceRedisService = service;
    }

    @PostPersist
    public void onPostPersist(DentalService service) {
        if (serviceRedisService != null && service.getServiceId() != null) {
            serviceRedisService.evictService(service.getServiceId());
        }
    }

    @PostUpdate
    public void onPostUpdate(DentalService service) {
        if (serviceRedisService != null && service.getServiceId() != null) {
            serviceRedisService.evictService(service.getServiceId());
        }
    }

    @PostRemove
    public void onPostRemove(DentalService service) {
        if (serviceRedisService != null && service.getServiceId() != null) {
            serviceRedisService.evictService(service.getServiceId());
        }
    }
}
