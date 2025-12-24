package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.clinical_records.domain.VitalSignsReference;
import com.dental.clinic.management.clinical_records.dto.VitalSignAssessment;
import com.dental.clinic.management.clinical_records.dto.VitalSignsReferenceResponse;
import com.dental.clinic.management.clinical_records.repository.VitalSignsReferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VitalSignsReferenceService {

    private final VitalSignsReferenceRepository vitalSignsReferenceRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(readOnly = true)
    public List<VitalSignsReferenceResponse> getAllActiveReferences() {
        return vitalSignsReferenceRepository.findByIsActiveOrderByVitalTypeAscAgeMinAsc(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VitalSignsReferenceResponse> getReferencesByAge(Integer age) {
        return vitalSignsReferenceRepository.findAllByAge(age)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VitalSignAssessment assessVitalSign(String vitalType, BigDecimal value, Integer patientAge) {
        var reference = vitalSignsReferenceRepository.findByVitalTypeAndAge(vitalType, patientAge);

        if (reference.isEmpty()) {
            return VitalSignAssessment.builder()
                    .vitalType(vitalType)
                    .value(value)
                    .status("UNKNOWN")
                    .message("Khong tim thay tham chieu cho do tuoi nay")
                    .build();
        }

        VitalSignsReference ref = reference.get();
        String status;
        String message;

        // Only use normalMin/normalMax for assessment (lowThreshold/highThreshold are deprecated)
        if (value.compareTo(ref.getNormalMin()) < 0) {
            status = "BELOW_NORMAL";
            message = String.format("Duoi muc binh thuong (binh thuong: %s-%s %s)",
                    ref.getNormalMin(), ref.getNormalMax(), ref.getUnit());
        } else if (value.compareTo(ref.getNormalMax()) > 0) {
            status = "ABOVE_NORMAL";
            message = String.format("Tren muc binh thuong (binh thuong: %s-%s %s)",
                    ref.getNormalMin(), ref.getNormalMax(), ref.getUnit());
        } else {
            status = "NORMAL";
            message = String.format("Binh thuong (%s-%s %s)",
                    ref.getNormalMin(), ref.getNormalMax(), ref.getUnit());
        }

        return VitalSignAssessment.builder()
                .vitalType(vitalType)
                .value(value)
                .unit(ref.getUnit())
                .status(status)
                .normalMin(ref.getNormalMin())
                .normalMax(ref.getNormalMax())
                .message(message)
                .build();
    }

    private VitalSignsReferenceResponse mapToResponse(VitalSignsReference entity) {
        return VitalSignsReferenceResponse.builder()
                .referenceId(entity.getReferenceId())
                .vitalType(entity.getVitalType())
                .ageMin(entity.getAgeMin())
                .ageMax(entity.getAgeMax())
                .normalMin(entity.getNormalMin())
                .normalMax(entity.getNormalMax())
                // lowThreshold and highThreshold are deprecated - not included in response
                .unit(entity.getUnit())
                .description(entity.getDescription())
                .effectiveDate(entity.getEffectiveDate().format(DATE_FORMATTER))
                .isActive(entity.getIsActive())
                .build();
    }
}
