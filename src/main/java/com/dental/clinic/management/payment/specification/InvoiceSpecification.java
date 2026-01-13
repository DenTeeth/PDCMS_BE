package com.dental.clinic.management.payment.specification;

// import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import org.springframework.data.jpa.domain.Specification;

// import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for Invoice filtering
 * Solves PostgreSQL type inference issue with NULL date parameters
 * 
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-11469">HHH-11469</a>
 */
public class InvoiceSpecification {

    /**
     * Build specification with optional filters
     * Only adds predicates when values are NOT NULL (eliminates NULL parameter issue)
     * 
     * @param status Optional payment status filter
     * @param type Optional invoice type filter
     * @param patientId Optional patient ID filter
     * @param startDate Optional start date filter (inclusive)
     * @param endDate Optional end date filter (inclusive)
     * @return Specification for filtering invoices
     */
    public static Specification<Invoice> withFilters(
            InvoicePaymentStatus status,
            InvoiceType type,
            Integer patientId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only add predicates when values are NOT NULL
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), status));
            }
            
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("invoiceType"), type));
            }
            
            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patientId"), patientId));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build specification with filters for patient payment history
     * Includes patientCode lookup via join to Patient entity
     * 
     * @param patientCode Patient business code (required)
     * @param status Optional payment status filter
     * @param startDate Optional start date filter (inclusive)
     * @param endDate Optional end date filter (inclusive)
     * @return Specification for filtering invoices by patient code
     */
    public static Specification<Invoice> withPatientCodeFilters(
            String patientCode,
            InvoicePaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Join to Patient entity to filter by patientCode
            // Note: We're using patientId FK, so we need to join Patient table
            if (patientCode != null) {
                // Add subquery to find patientId from patientCode
                predicates.add(root.get("patientId").in(
                    criteriaBuilder.createQuery(Integer.class)
                        .select(root.get("patientId"))
                        .where(criteriaBuilder.equal(
                            criteriaBuilder.function("lower", String.class, 
                                criteriaBuilder.literal(patientCode)), 
                            criteriaBuilder.literal(patientCode.toLowerCase())
                        ))
                ));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), status));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
