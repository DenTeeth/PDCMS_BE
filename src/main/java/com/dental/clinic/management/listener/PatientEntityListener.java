package com.dental.clinic.management.listener;

import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.service.PatientRedisService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatientEntityListener {

    private static PatientRedisService patientRedisService;

    @Autowired
    public void setPatientRedisService(PatientRedisService service) {
        PatientEntityListener.patientRedisService = service;
    }

    @PostPersist
    public void onPostPersist(Patient patient) {
        if (patientRedisService != null && patient.getPatientId() != null) {
            patientRedisService.evictPatient(patient.getPatientId().longValue());
        }
    }

    @PostUpdate
    public void onPostUpdate(Patient patient) {
        if (patientRedisService != null && patient.getPatientId() != null) {
            patientRedisService.evictPatient(patient.getPatientId().longValue());
        }
    }

    @PostRemove
    public void onPostRemove(Patient patient) {
        if (patientRedisService != null && patient.getPatientId() != null) {
            patientRedisService.evictPatient(patient.getPatientId().longValue());
        }
    }
}
