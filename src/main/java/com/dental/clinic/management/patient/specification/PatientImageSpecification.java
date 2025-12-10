package com.dental.clinic.management.patient.specification;

import com.dental.clinic.management.patient.domain.PatientImage;
import com.dental.clinic.management.patient.enums.ImageType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientImageSpecification {

    public static Specification<PatientImage> filterImages(
            Long patientId,
            ImageType imageType,
            Long clinicalRecordId,
            LocalDate fromDate,
            LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patient").get("patientId"), patientId));
            }

            if (imageType != null) {
                predicates.add(criteriaBuilder.equal(root.get("imageType"), imageType));
            }

            if (clinicalRecordId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("clinicalRecord").get("clinicalRecordId"), clinicalRecordId));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("capturedDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("capturedDate"), toDate));
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
