package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.Specialization;
import com.dental.clinic.management.repository.SpecializationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecializationService {

    private final SpecializationRepository specializationRepository;

    public SpecializationService(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    /**
     * Get all active specializations
     * 
     * @return List of active specializations
     */
    @Transactional(readOnly = true)
    public List<Specialization> getAllActiveSpecializations() {
        return specializationRepository.findAllActiveSpecializations();
    }
}
