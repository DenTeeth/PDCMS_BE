package com.dental.clinic.management.feedback.specification;

import com.dental.clinic.management.feedback.domain.AppointmentFeedback;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for AppointmentFeedback filtering
 * Solves PostgreSQL type inference issue with NULL date parameters
 * 
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-11469">HHH-11469</a>
 */
public class AppointmentFeedbackSpecification {

    /**
     * Build specification for feedback filters
     * Only adds predicates when values are NOT NULL (eliminates NULL parameter issue)
     * 
     * @param rating Optional rating filter
     * @param patientId Optional patient ID filter
     * @param fromDate Optional from date filter (inclusive)
     * @param toDate Optional to date filter (inclusive)
     * @return Specification for filtering feedbacks
     */
    public static Specification<AppointmentFeedback> withFilters(
            Integer rating,
            Integer patientId,
            LocalDate fromDate,
            LocalDate toDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only add predicates when values are NOT NULL
            if (rating != null) {
                predicates.add(criteriaBuilder.equal(root.get("rating"), rating));
            }
            
            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patientId"), patientId));
            }
            
            // Date filtering with LocalDate comparison
            if (fromDate != null) {
                LocalDateTime fromDateTime = fromDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }
            
            if (toDate != null) {
                LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build specification for date range filtering only
     * Used by statistics queries
     * 
     * @param fromDate Optional from date filter (inclusive)
     * @param toDate Optional to date filter (inclusive)
     * @return Specification for date range filtering
     */
    public static Specification<AppointmentFeedback> withDateRange(
            LocalDate fromDate,
            LocalDate toDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (fromDate != null) {
                LocalDateTime fromDateTime = fromDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }
            
            if (toDate != null) {
                LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
