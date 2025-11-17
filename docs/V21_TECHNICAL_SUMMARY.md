# V21 Clinical Rules Engine - Technical Summary

## 📊 Overview

**Feature:** Clinical Rules Validation System
**Version:** V21
**Branch:** feat/BE-501-manage-treatment-plans
**Database Migration:** V21 schema + seed data
**Spring Boot Version:** 3.2.10

---

## 🗄️ Database Changes

### New ENUM Type

```sql
CREATE TYPE dependency_rule_type AS ENUM (
    'REQUIRES_PREREQUISITE',  -- Hard rule: Must complete service A before B
    'REQUIRES_MIN_DAYS',      -- Hard rule: Min days between services
    'EXCLUDES_SAME_DAY',      -- Hard rule: Cannot book together same day
    'BUNDLES_WITH'            -- Soft rule: Suggestion only
);
```

### New Table

```sql
CREATE TABLE service_dependencies (
    dependency_id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES dental_service(service_id),
    dependent_service_id BIGINT NOT NULL REFERENCES dental_service(service_id),
    rule_type dependency_rule_type NOT NULL,
    min_days_apart INTEGER CHECK (min_days_apart >= 0),
    receptionist_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT no_self_dependency CHECK (service_id != dependent_service_id),
    UNIQUE (service_id, dependent_service_id, rule_type)
);

-- Indexes for performance
CREATE INDEX idx_service_dep_service ON service_dependencies(service_id);
CREATE INDEX idx_service_dep_dependent ON service_dependencies(dependent_service_id);
CREATE INDEX idx_service_dep_rule_type ON service_dependencies(rule_type);
```

### Seed Data (6 Rules)

```sql
-- 1. REQUIRES_PREREQUISITE: GEN_EXAM → FILLING_COMP
-- 2. REQUIRES_MIN_DAYS: EXTRACT_WISDOM_L2 → SURG_CHECKUP (7 days)
-- 3. EXCLUDES_SAME_DAY: EXTRACT_WISDOM_L2 ↔ BLEACH_INOFFICE (bidirectional)
-- 4. BUNDLES_WITH: GEN_EXAM ↔ SCALING_L1 (bidirectional suggestions)
```

---

## 🏗️ Architecture

### Layer Structure

```
Controller Layer
    ↓
Service Layer (Business Logic)
    ↓ validates via
ClinicalRulesValidationService (NEW)
    ↓ queries
ServiceDependencyRepository (NEW)
    ↓
Database (service_dependencies table)
```

### Component Dependencies

```
AppointmentCreationService
    → ClinicalRulesValidationService
        → ServiceDependencyRepository
        → AppointmentRepository
        → AppointmentServiceRepository

TreatmentPlanApprovalService
    → ClinicalRulesValidationService
        → ServiceDependencyRepository

TreatmentPlanItemService
    → ClinicalRulesValidationService
        → ServiceDependencyRepository

DentalServiceService
    → ClinicalRulesValidationService
        → ServiceDependencyRepository
```

---

## 📦 New Components

### 1. Domain Layer

#### DependencyRuleType.java

```java
package com.dental.clinic.management.service.domain;

public enum DependencyRuleType {
    REQUIRES_PREREQUISITE,    // Must complete A before B
    REQUIRES_MIN_DAYS,        // Minimum days between A and B
    EXCLUDES_SAME_DAY,        // Cannot book A and B same day
    BUNDLES_WITH              // Soft suggestion: A works well with B
}
```

#### ServiceDependency.java

```java
@Entity
@Table(name = "service_dependencies")
public class ServiceDependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dependencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private DentalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_service_id")
    private DentalService dependentService;

    @Enumerated(EnumType.STRING)
    private DependencyRuleType ruleType;

    private Integer minDaysApart;
    private String receptionistNote;

    // Helper methods
    public boolean isHardRule() {
        return ruleType != DependencyRuleType.BUNDLES_WITH;
    }
}
```

#### PlanItemStatus.java (MODIFIED)

```java
public enum PlanItemStatus {
    PENDING,
    READY_FOR_BOOKING,
    WAITING_FOR_PREREQUISITE,  // ← NEW
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    SKIPPED
}
```

---

### 2. Repository Layer

#### ServiceDependencyRepository.java

```java
@Repository
public interface ServiceDependencyRepository extends JpaRepository<ServiceDependency, Long> {

    // 1. Find all rules for a service
    List<ServiceDependency> findByServiceId(Long serviceId);

    // 2. Find services that depend on this service
    List<ServiceDependency> findByDependentServiceId(Long dependentServiceId);

    // 3. Find rules by type
    List<ServiceDependency> findByServiceIdAndRuleType(Long serviceId, DependencyRuleType ruleType);

    // 4. Get bundle suggestions (bidirectional)
    @Query("SELECT DISTINCT CASE " +
           "WHEN sd.service.serviceId = :serviceId THEN sd.dependentService.serviceCode " +
           "ELSE sd.service.serviceCode END " +
           "FROM ServiceDependency sd " +
           "WHERE sd.ruleType = 'BUNDLES_WITH' " +
           "AND (sd.service.serviceId = :serviceId OR sd.dependentService.serviceId = :serviceId)")
    List<String> findBundlesByServiceId(@Param("serviceId") Long serviceId);

    // 5. Check exclusion rules for list of services
    @Query("SELECT sd FROM ServiceDependency sd " +
           "WHERE sd.ruleType = 'EXCLUDES_SAME_DAY' " +
           "AND ((sd.service.serviceId IN :serviceIds AND sd.dependentService.serviceId IN :serviceIds))")
    List<ServiceDependency> findExclusionRulesForServices(@Param("serviceIds") List<Long> serviceIds);

    // 6. Find all hard rules (non-bundle) for a service
    @Query("SELECT sd FROM ServiceDependency sd " +
           "WHERE sd.service.serviceId = :serviceId AND sd.ruleType != 'BUNDLES_WITH'")
    List<ServiceDependency> findHardRulesByServiceId(@Param("serviceId") Long serviceId);

    // 7. Check if specific rule exists
    boolean existsByServiceAndDependentAndRuleType(
        DentalService service,
        DentalService dependent,
        DependencyRuleType ruleType
    );
}
```

#### AppointmentRepository.java (MODIFIED)

```java
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // ... existing methods ...

    // NEW: Find completed appointments for validation
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.patient.patientId = :patientId " +
           "AND a.status = 'COMPLETED' " +
           "ORDER BY a.startTime DESC")
    List<Appointment> findCompletedAppointmentsByPatientId(@Param("patientId") Long patientId);
}
```

---

### 3. Service Layer

#### ClinicalRulesValidationService.java

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicalRulesValidationService {

    private final ServiceDependencyRepository serviceDependencyRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;

    /**
     * MAIN VALIDATION METHOD
     * Validates all hard rules before appointment creation
     *
     * @throws ConflictException if any hard rule is violated
     */
    public void validateAppointmentServices(
            Long patientId,
            List<Long> serviceIds,
            LocalDate appointmentDate
    ) {
        // STEP 1: Validate EXCLUDES_SAME_DAY
        validateNoExclusionConflicts(serviceIds);

        // STEP 2: Validate REQUIRES_PREREQUISITE
        validatePrerequisites(patientId, serviceIds);

        // STEP 3: Validate REQUIRES_MIN_DAYS
        validateMinimumDays(patientId, serviceIds, appointmentDate);
    }

    /**
     * HELPER: Get bundle suggestions (soft rule)
     * Used by API 6.5 to show combo recommendations
     */
    public List<Long> getBundleSuggestions(Long serviceId) {
        // Returns serviceIds that bundle well with this service
    }

    /**
     * HELPER: Check if service has prerequisites
     * Used by API 5.9 to set initial item status
     */
    public boolean hasPrerequisites(Long serviceId) {
        return !serviceDependencyRepository
            .findByDependentServiceIdAndRuleType(serviceId, REQUIRES_PREREQUISITE)
            .isEmpty();
    }

    /**
     * HELPER: Get services unlocked by completing this service
     * Used by API 5.6 for auto-unlock
     */
    public List<Long> getServicesUnlockedBy(Long completedServiceId) {
        return serviceDependencyRepository
            .findByServiceIdAndRuleType(completedServiceId, REQUIRES_PREREQUISITE)
            .stream()
            .map(ServiceDependency::getDependentServiceId)
            .collect(Collectors.toList());
    }
}
```

---

## 🔄 Integration Points

### API 3.2 - AppointmentCreationService

**Location:** `createAppointment()` and `createAppointmentInternal()`

**Change:**

```java
// STEP 7B: NEW - Validate clinical rules
List<Long> serviceIds = services.stream()
    .map(s -> s.getServiceId().longValue())
    .collect(Collectors.toList());

clinicalRulesValidationService.validateAppointmentServices(
    patient.getPatientId(),
    serviceIds,
    startTime.toLocalDate()
);
// If validation fails → throws ConflictException with specific error code
```

**Error Codes:**

- `CLINICAL_RULE_EXCLUSION_VIOLATED`
- `CLINICAL_RULE_PREREQUISITE_NOT_MET`
- `CLINICAL_RULE_MIN_DAYS_NOT_MET`

---

### API 5.9 - TreatmentPlanApprovalService

**Location:** `updatePlanApprovalStatus()`

**Change:**

```java
// STEP 8B: NEW - Set initial item statuses based on prerequisites
if (newStatus == ApprovalStatus.APPROVED) {
    activateItemsWithClinicalRulesCheck(plan);
}

private void activateItemsWithClinicalRulesCheck(TreatmentPlan plan) {
    for (Phase phase : plan.getPhases()) {
        for (PatientPlanItem item : phase.getItems()) {
            if (item.getStatus() == PlanItemStatus.PENDING && item.getServiceId() != null) {
                boolean hasPrerequisites = clinicalRulesValidationService
                    .hasPrerequisites(item.getServiceId());

                PlanItemStatus newStatus = hasPrerequisites
                    ? PlanItemStatus.WAITING_FOR_PREREQUISITE
                    : PlanItemStatus.READY_FOR_BOOKING;

                item.setStatus(newStatus);
                patientPlanItemRepository.save(item);
            }
        }
    }
}
```

---

### API 5.6 - TreatmentPlanItemService

**Location:** `updateItemStatus()`

**Change:**

```java
// STEP 6B: NEW - Auto-unlock dependent items when item completed
if (newStatus == PlanItemStatus.COMPLETED && savedItem.getServiceId() != null) {
    unlockDependentItems(plan, savedItem.getServiceId().longValue());
}

private void unlockDependentItems(TreatmentPlan plan, Long completedServiceId) {
    List<Long> unlockedServiceIds = clinicalRulesValidationService
        .getServicesUnlockedBy(completedServiceId);

    if (unlockedServiceIds.isEmpty()) return;

    int unlockedCount = 0;
    for (Phase phase : plan.getPhases()) {
        for (PatientPlanItem item : phase.getItems()) {
            if (item.getStatus() == PlanItemStatus.WAITING_FOR_PREREQUISITE
                && unlockedServiceIds.contains(item.getServiceId())) {

                item.setStatus(PlanItemStatus.READY_FOR_BOOKING);
                patientPlanItemRepository.save(item);
                unlockedCount++;

                log.info("Auto-unlocked item {} (service {})",
                    item.getItemId(), item.getServiceId());
            }
        }
    }
    log.info("Unlocked {} items after completing service {}",
        unlockedCount, completedServiceId);
}
```

**State Transitions Updated:**

```java
private static final Map<PlanItemStatus, List<PlanItemStatus>> STATE_TRANSITIONS = Map.ofEntries(
    entry(PENDING, List.of(READY_FOR_BOOKING, WAITING_FOR_PREREQUISITE, SKIPPED)),
    entry(WAITING_FOR_PREREQUISITE, List.of(READY_FOR_BOOKING, SKIPPED)),  // NEW
    entry(READY_FOR_BOOKING, List.of(SCHEDULED, SKIPPED)),
    // ... rest of transitions
);
```

---

### API 6.5 - DentalServiceService

**Location:** `getInternalGroupedServices()`

**Change:**

```java
// Create map for serviceId → serviceCode conversion
Map<Long, String> serviceIdToCodeMap = services.stream()
    .collect(Collectors.toMap(DentalService::getServiceId, DentalService::getServiceCode));

for (DentalService service : services) {
    // Get bundle suggestions
    List<Long> bundleServiceIds = clinicalRulesValidationService
        .getBundleSuggestions(service.getServiceId());

    List<String> bundlesWith = bundleServiceIds.stream()
        .map(serviceIdToCodeMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    // Add to DTO
    InternalServiceDTO.builder()
        .serviceId(service.getServiceId())
        .serviceCode(service.getServiceCode())
        .bundlesWith(bundlesWith)  // NEW field
        .build();
}
```

**DTO Modified:**

```java
@Data
@Builder
public class InternalServiceDTO {
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private BigDecimal price;
    private Integer durationMinutes;
    private List<String> bundlesWith;  // ← NEW FIELD
}
```

---

## 🧪 Testing Strategy

### Unit Tests Required

```java
@SpringBootTest
class ClinicalRulesValidationServiceTest {

    @Test
    void testExclusionRuleValidation() {
        // Given: Services with EXCLUDES_SAME_DAY rule
        // When: Try to book both on same day
        // Then: Should throw ConflictException with CLINICAL_RULE_EXCLUSION_VIOLATED
    }

    @Test
    void testPrerequisiteValidation_NotCompleted() {
        // Given: Service with REQUIRES_PREREQUISITE rule
        // When: Try to book without completing prerequisite
        // Then: Should throw ConflictException with CLINICAL_RULE_PREREQUISITE_NOT_MET
    }

    @Test
    void testPrerequisiteValidation_Completed() {
        // Given: Prerequisite already completed
        // When: Try to book dependent service
        // Then: Should NOT throw exception
    }

    @Test
    void testMinDaysValidation_TooSoon() {
        // Given: Service with REQUIRES_MIN_DAYS (7 days)
        // When: Try to book 3 days after prerequisite
        // Then: Should throw ConflictException with CLINICAL_RULE_MIN_DAYS_NOT_MET
    }

    @Test
    void testBundleSuggestions() {
        // Given: Service with BUNDLES_WITH rule
        // When: Get bundle suggestions
        // Then: Should return list of related service IDs
    }

    @Test
    void testAutoUnlock() {
        // Given: Treatment plan with locked item
        // When: Complete prerequisite service
        // Then: Locked item should change to READY_FOR_BOOKING
    }
}
```

### Integration Tests Required

```java
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentCreationIntegrationTest {

    @Test
    void testCreateAppointment_WithExclusionViolation() {
        // End-to-end test of API 3.2 with exclusion rule
    }

    @Test
    void testTreatmentPlanApproval_SetsWaitingStatus() {
        // End-to-end test of API 5.9 with prerequisite items
    }

    @Test
    void testCompleteItem_UnlocksDependents() {
        // End-to-end test of API 5.6 auto-unlock
    }
}
```

---

## 📊 Performance Considerations

### Database Query Optimization

1. **Indexes Created:**

   - `idx_service_dep_service` on `service_id`
   - `idx_service_dep_dependent` on `dependent_service_id`
   - `idx_service_dep_rule_type` on `rule_type`

2. **N+1 Query Prevention:**

   - `ServiceDependency` uses `@ManyToOne(fetch = LAZY)`
   - Separate queries for `AppointmentService` to avoid JOIN overhead
   - Map-based lookup for serviceId → serviceCode conversion

3. **Expected Load:**
   - ~20 services with ~10 rules = 200 dependency records
   - Validation queries: 3-5 per appointment creation
   - Bundle suggestion queries: 1 per service in menu (cached)

### Caching Strategy (Future Enhancement)

```java
@Cacheable(value = "clinical-rules", key = "#serviceId")
public List<ServiceDependency> findRulesByService(Long serviceId) {
    // Cache rules per service
}

@Cacheable(value = "bundle-suggestions", key = "#serviceId")
public List<Long> getBundleSuggestions(Long serviceId) {
    // Cache bundle suggestions
}
```

---

## 🔒 Security Considerations

### Authorization

- All validation happens **server-side** (cannot be bypassed)
- Clinical rules enforced at service layer (before DB insert)
- Frontend checks are **convenience only** (not security)

### Data Integrity

- `CHECK` constraint: No self-dependencies
- `UNIQUE` constraint: One rule per service pair per type
- `FOREIGN KEY` constraints: All service references valid
- Bidirectional rules stored separately (explicit)

---

## 🚀 Deployment

### Database Migration

```bash
# 1. Run V21 schema migration
psql -d dental_clinic -f db/migration/V21__add_clinical_rules.sql

# 2. Load seed data
psql -d dental_clinic -f db/seed/dental-clinic-seed-data.sql

# 3. Verify
psql -d dental_clinic -c "SELECT COUNT(*) FROM service_dependencies;"
# Expected: 6 rows
```

### Application Restart

```bash
# No special config needed
./mvnw spring-boot:run
```

### Verification Checklist

- [ ] Schema V21 applied
- [ ] Seed data loaded (6 rules)
- [ ] Application starts without errors
- [ ] Swagger shows updated API responses
- [ ] Test appointment creation with rules

---

## 📈 Monitoring

### Log Messages to Watch

```
INFO  ClinicalRulesValidationService - Validating services [1, 2] for patient 123
INFO  ClinicalRulesValidationService - Found 2 exclusion rules to check
WARN  ClinicalRulesValidationService - EXCLUSION violation: EXTRACT_WISDOM_L2, BLEACH_INOFFICE
ERROR ConflictException - CLINICAL_RULE_EXCLUSION_VIOLATED
```

### Metrics to Track

- `clinical_rule_violations_total{type="exclusion"}`
- `clinical_rule_violations_total{type="prerequisite"}`
- `clinical_rule_violations_total{type="min_days"}`
- `bundle_suggestions_shown_total`
- `auto_unlock_events_total`

---

## 🔧 Configuration

### Application Properties (Optional)

```yaml
# Future configuration options
clinical-rules:
  enabled: true
  cache:
    enabled: false # Not implemented yet
    ttl: 3600
  logging:
    level: INFO
```

Currently uses default behavior (no config needed).

---

## 📚 References

- **Database Schema:** `src/main/resources/db/migration/schema.sql` (V21)
- **Seed Data:** `src/main/resources/db/seed/dental-clinic-seed-data.sql` (Lines 1797-1891)
- **Main Service:** `com.dental.clinic.management.service.service.ClinicalRulesValidationService`
- **Entity:** `com.dental.clinic.management.service.domain.ServiceDependency`
- **Repository:** `com.dental.clinic.management.service.repository.ServiceDependencyRepository`

---

**Version:** V21
**Status:** Implementation Complete
**Testing:** Pending
**Documentation:** Complete
**Last Updated:** November 17, 2024
