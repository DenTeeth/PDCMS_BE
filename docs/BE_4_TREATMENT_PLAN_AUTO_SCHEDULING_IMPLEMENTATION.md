# BE_4: Treatment Plan Auto-Scheduling with Holiday Detection and Service Constraints

## Overview
This implementation adds automatic appointment scheduling for treatment plans with:
1. **Holiday Detection**: Automatically skips holidays when calculating appointment dates
2. **Service Constraints**: Respects service-specific timing requirements (preparation, recovery, spacing, daily limits)

## Feature Requirements

### User Story (Vietnamese)
```
god damn it BE gave me more tasks - nếu có lộ trình phải tạo sẵn thì phải dựa vào 
thời gian dự kiến và nếu trong lộ trình có lịch nghỉ lễ thì phải tính vào

BE_4 NÊN CÓ Ngày tối thiểu, ngày hồi phục, giãn cách để bắt đầu 1 ca appointment mới
```

### Translation
- Treatment plans with pre-created schedules must be based on estimated duration
- If the schedule includes holidays, they must be counted/skipped
- Services should have: minimum preparation days, recovery days, spacing between appointments, max appointments per day

---

## Database Changes

### New Columns in `services` Table

```sql
ALTER TABLE services 
ADD COLUMN minimum_preparation_days INTEGER DEFAULT 0,
ADD COLUMN recovery_days INTEGER DEFAULT 0,
ADD COLUMN spacing_days INTEGER DEFAULT 0,
ADD COLUMN max_appointments_per_day INTEGER;
```

| Column | Type | Description | Example |
|--------|------|-------------|---------|
| `minimum_preparation_days` | INTEGER | Minimum days before this service can be performed | Dental implant: 7 days |
| `recovery_days` | INTEGER | Recovery days needed after this service | Tooth extraction: 7 days |
| `spacing_days` | INTEGER | Spacing between consecutive appointments for this service | Orthodontic adjustment: 30 days |
| `max_appointments_per_day` | INTEGER (nullable) | Maximum appointments per day for this service | Complex surgery: 2 per day |

### Migration Script
Location: `src/main/resources/db/V26_add_service_appointment_constraints.sql`

Apply migration:
```sql
-- For existing database
psql -U postgres -d dental_clinic -f src/main/resources/db/V26_add_service_appointment_constraints.sql

-- Or drop/recreate (development only)
docker-compose down -v
docker-compose up -d
```

---

## Implementation Components

### 1. HolidayDateService (Extended)
**Location**: `com.dental.clinic.management.working_schedule.service.HolidayDateService`

#### New Methods Added

```java
// Get all holidays in a date range
public List<LocalDate> getHolidaysInRange(LocalDate startDate, LocalDate endDate)

// Get next working day (skip holidays)
public LocalDate getNextWorkingDay(LocalDate date)

// Count working days between dates (excluding holidays)
public long countWorkingDaysBetween(LocalDate startDate, LocalDate endDate)

// Add working days to a date (skipping holidays)
public LocalDate addWorkingDays(LocalDate startDate, int workingDays)
```

**Usage Example**:
```java
@Autowired
private HolidayDateService holidayDateService;

// Check if date is holiday
boolean isHoliday = holidayDateService.isHoliday(LocalDate.of(2025, 1, 1)); // true (New Year)

// Get next working day
LocalDate workingDay = holidayDateService.getNextWorkingDay(LocalDate.of(2025, 1, 1)); 
// Returns 2025-01-02 (or later if that's also a holiday)

// Add 5 working days from today
LocalDate futureDate = holidayDateService.addWorkingDays(LocalDate.now(), 5);
```

---

### 2. TreatmentPlanSchedulingService (New)
**Location**: `com.dental.clinic.management.treatment_plans.service.TreatmentPlanSchedulingService`

Core service for calculating appointment schedules.

#### Main Methods

##### calculateAppointmentDates
Calculates all appointment dates for a treatment plan.

```java
public List<LocalDate> calculateAppointmentDates(
    LocalDate startDate,
    Integer estimatedDurationDays,
    List<DentalService> services)
```

**Parameters**:
- `startDate`: Treatment plan start date
- `estimatedDurationDays`: Total estimated duration (from template)
- `services`: Ordered list of services in the treatment plan

**Returns**: List of calculated appointment dates (all working days)

**Example**:
```java
@Autowired
private TreatmentPlanSchedulingService schedulingService;

LocalDate startDate = LocalDate.of(2025, 12, 15);
Integer duration = 180; // 6 months
List<DentalService> services = /* get from treatment plan */;

List<LocalDate> appointmentDates = schedulingService.calculateAppointmentDates(
    startDate, duration, services);
// Returns: [2025-12-15, 2026-01-20, 2026-03-05, ...] (skips holidays)
```

##### calculateNextAvailableDate
Calculates next available date considering service constraints.

```java
public LocalDate calculateNextAvailableDate(
    LocalDate previousDate,
    DentalService service,
    int defaultInterval)
```

**Logic**:
1. Takes the maximum of: `defaultInterval`, `minimumPreparationDays`, `recoveryDays`, `spacingDays`
2. Adds that many **working days** (skipping holidays)
3. Returns the next valid date

**Example**:
```java
DentalService implantService = // service with minimumPreparationDays = 7
LocalDate previousDate = LocalDate.of(2025, 12, 15);

LocalDate nextDate = schedulingService.calculateNextAvailableDate(
    previousDate, implantService, 3); // defaultInterval = 3
// Returns date at least 7 working days later (because constraint > default)
```

##### isDateValidForAppointment
Validates if a proposed date is valid.

```java
public boolean isDateValidForAppointment(
    LocalDate proposedDate,
    LocalDate previousAppointmentDate,
    DentalService service)
```

**Checks**:
1. Not a holiday
2. Respects `minimumPreparationDays` from previous appointment
3. Respects `recoveryDays` from previous appointment
4. Respects `spacingDays` for same service

---

### 3. AppointmentConstraintValidator (New)
**Location**: `com.dental.clinic.management.booking_appointment.service.validator.AppointmentConstraintValidator`

Validates appointment creation against all constraints.

#### Main Method

```java
public ValidationResult validateAppointmentConstraints(
    LocalDateTime appointmentDate,
    DentalService service,
    Long patientId)
```

**Validations**:
1. ✅ Date is not a holiday
2. ✅ `max_appointments_per_day` not exceeded (for service)
3. ✅ `minimum_preparation_days` respected (from patient's last appointment)
4. ✅ `recovery_days` respected (from patient's last completed appointment)
5. ✅ `spacing_days` respected (for same service)

**Usage Example**:
```java
@Autowired
private AppointmentConstraintValidator validator;

LocalDateTime proposedDate = LocalDateTime.of(2025, 12, 25, 10, 0); // Christmas
DentalService service = /* service */;
Long patientId = 123L;

ValidationResult result = validator.validateAppointmentConstraints(
    proposedDate, service, patientId);

if (!result.isValid()) {
    throw new BadRequestException(result.getMessage());
    // Message: "Cannot create appointment on 2025-12-25 - it is a holiday"
}
```

**ValidationResult Object**:
```java
public static class ValidationResult {
    private final boolean valid;
    private final String message; // null if valid, error message if invalid
    
    public boolean isValid()
    public String getMessage()
}
```

---

### 4. Repository Updates

#### AppointmentRepository - New Queries

##### countByServiceAndDate
Counts appointments for a service on a specific date (for `max_appointments_per_day` validation).

```java
@Query("SELECT COUNT(a) FROM Appointment a " +
    "WHERE a.service.serviceId = :serviceId " +
    "AND FUNCTION('DATE', a.appointmentDateTime) = :date " +
    "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
long countByServiceAndDate(
    @Param("serviceId") Long serviceId,
    @Param("date") java.time.LocalDate date);
```

##### findRecentCompletedByPatient
Finds recent completed appointments for constraint checking.

```java
@Query("SELECT a FROM Appointment a " +
    "WHERE a.patientId = :patientId " +
    "AND a.status = 'COMPLETED' " +
    "ORDER BY a.appointmentDateTime DESC")
List<Appointment> findRecentCompletedByPatient(
    @Param("patientId") Long patientId,
    @Param("limit") int limit);
```

---

## API Changes

### Service DTOs Updated

All service-related DTOs now include the 4 new constraint fields:

#### DentalServiceDTO
```java
private Integer minimumPreparationDays;
private Integer recoveryDays;
private Integer spacingDays;
private Integer maxAppointmentsPerDay;
```

#### CreateServiceRequest
```java
@Min(value = 0, message = "Minimum preparation days cannot be negative")
@Schema(description = "BE_4: Minimum preparation days before this service (days)", example = "0")
private Integer minimumPreparationDays;

@Min(value = 0, message = "Recovery days cannot be negative")
@Schema(description = "BE_4: Recovery days needed after this service (days)", example = "0")
private Integer recoveryDays;

@Min(value = 0, message = "Spacing days cannot be negative")
@Schema(description = "BE_4: Spacing days between consecutive appointments (days)", example = "0")
private Integer spacingDays;

@Min(value = 1, message = "Max appointments per day must be at least 1")
@Schema(description = "BE_4: Maximum appointments allowed per day (null = no limit)", example = "5")
private Integer maxAppointmentsPerDay;
```

#### UpdateServiceRequest
Same fields as CreateServiceRequest (all optional).

#### ServiceResponse
Same fields as DTO (returned in API responses).

---

## Integration Guide

### For FE: Creating Services with Constraints

#### POST /api/services
```json
{
  "serviceCode": "IMPLANT_FULL",
  "serviceName": "Cấy ghép Implant toàn hàm",
  "description": "Cấy ghép implant cho toàn bộ hàm răng",
  "defaultDurationMinutes": 120,
  "defaultBufferMinutes": 30,
  "price": 50000000,
  "specializationId": 3,
  "displayOrder": 1,
  "minimumPreparationDays": 7,
  "recoveryDays": 14,
  "spacingDays": 30,
  "maxAppointmentsPerDay": 2,
  "isActive": true
}
```

**Response** (200 OK):
```json
{
  "serviceId": 123,
  "serviceCode": "IMPLANT_FULL",
  "serviceName": "Cấy ghép Implant toàn hàm",
  "price": 50000000,
  "minimumPreparationDays": 7,
  "recoveryDays": 14,
  "spacingDays": 30,
  "maxAppointmentsPerDay": 2,
  "isActive": true,
  "createdAt": "2025-12-11T10:30:00",
  "updatedAt": "2025-12-11T10:30:00"
}
```

### For FE: Treatment Plan Auto-Scheduling

#### Backend Integration Point (BE Team)

In the treatment plan creation service, use `TreatmentPlanSchedulingService`:

```java
@Service
@RequiredArgsConstructor
public class PatientTreatmentPlanService {
    
    private final TreatmentPlanSchedulingService schedulingService;
    
    public TreatmentPlanResponse createFromTemplate(
            Long patientId, 
            Long templateId, 
            LocalDate startDate) {
        
        // 1. Load template and services
        TreatmentPlanTemplate template = // load from DB
        List<DentalService> services = // extract services from template phases
        
        // 2. Calculate appointment dates (skips holidays automatically)
        List<LocalDate> appointmentDates = schedulingService.calculateAppointmentDates(
            startDate,
            template.getEstimatedDurationDays(),
            services
        );
        
        // 3. Create patient treatment plan with calculated dates
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
            .patientId(patientId)
            .templateId(templateId)
            .startDate(startDate)
            .expectedEndDate(appointmentDates.get(appointmentDates.size() - 1))
            .build();
        
        // 4. Create phases and appointments with calculated dates
        for (int i = 0; i < services.size(); i++) {
            createAppointment(plan, services.get(i), appointmentDates.get(i));
        }
        
        return /* return DTO */;
    }
}
```

#### FE API Call (Expected Response)

**POST** `/api/treatment-plans/create-from-template`
```json
{
  "patientId": 123,
  "templateId": 5,
  "startDate": "2025-12-15"
}
```

**Response** (200 OK):
```json
{
  "treatmentPlanId": 456,
  "patientId": 123,
  "templateId": 5,
  "templateName": "Niềng răng mắc cài kim loại 2 năm",
  "startDate": "2025-12-15",
  "expectedEndDate": "2027-06-15",
  "phases": [
    {
      "phaseId": 1,
      "phaseName": "Chuẩn bị",
      "appointments": [
        {
          "appointmentId": 789,
          "serviceCode": "CONSULTATION",
          "serviceName": "Tư vấn ban đầu",
          "scheduledDate": "2025-12-15",  // Working day (not holiday)
          "status": "SCHEDULED"
        }
      ]
    },
    {
      "phaseId": 2,
      "phaseName": "Lắp đặt",
      "appointments": [
        {
          "appointmentId": 790,
          "serviceCode": "BRACES_INSTALL",
          "serviceName": "Lắp mắc cài",
          "scheduledDate": "2026-01-20",  // Skipped holidays, respects prep days
          "status": "SCHEDULED"
        }
      ]
    }
  ]
}
```

**Note**: All `scheduledDate` values will be **working days** (holidays are automatically skipped).

---

### For FE: Appointment Validation

When creating appointments manually, BE will validate constraints automatically:

#### POST /api/appointments
```json
{
  "patientId": 123,
  "serviceCode": "IMPLANT_FULL",
  "appointmentDateTime": "2025-12-25T10:00:00",  // Christmas (holiday)
  "doctorId": 5,
  "duration": 120
}
```

**Response** (400 Bad Request):
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Cannot create appointment on 2025-12-25 - it is a holiday"
}
```

#### Constraint Violation Examples

**Max Appointments Per Day**:
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Maximum appointments per day reached for service 'Cấy ghép Implant' on 2025-12-20 (2/2)"
}
```

**Minimum Preparation Days**:
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Service 'Cấy ghép Implant' requires minimum 7 days preparation. Last appointment was on 2025-12-15 (3 days ago)"
}
```

**Recovery Days**:
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Service 'Nhổ răng khôn' requires 7 days recovery period. Last appointment was on 2025-12-15 (4 days ago)"
}
```

**Spacing Days**:
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Service 'Điều chỉnh niềng' requires 30 days spacing between appointments. Last appointment with this service was on 2025-11-20 (20 days ago)"
}
```

---

## Testing Guide

### 1. Database Setup

```sql
-- Insert test holidays
INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description)
VALUES ('HD_XMAS_2025', 'Christmas 2025', 'ANNUAL', 'Christmas Day');

INSERT INTO holiday_dates (holiday_date, definition_id)
VALUES ('2025-12-25', 'HD_XMAS_2025');

-- Create test service with constraints
INSERT INTO services (
    service_code, service_name, default_duration_minutes, 
    price, minimum_preparation_days, recovery_days, 
    spacing_days, max_appointments_per_day
) VALUES (
    'TEST_IMPLANT', 'Test Implant Service', 120, 
    10000000, 7, 14, 30, 2
);
```

### 2. Unit Tests

#### Test HolidayDateService
```java
@Test
void testAddWorkingDays_SkipsHolidays() {
    // Given: Dec 24 (working), Dec 25 (holiday), Dec 26 (working)
    LocalDate start = LocalDate.of(2025, 12, 24);
    
    // When: Add 1 working day
    LocalDate result = holidayDateService.addWorkingDays(start, 1);
    
    // Then: Should skip Dec 25 and return Dec 26
    assertEquals(LocalDate.of(2025, 12, 26), result);
}
```

#### Test TreatmentPlanSchedulingService
```java
@Test
void testCalculateAppointmentDates_RespectsServiceConstraints() {
    // Given: Service with 7 days minimum preparation
    DentalService service = createServiceWithConstraints(7, 0, 0, null);
    LocalDate startDate = LocalDate.of(2025, 12, 15);
    
    // When: Calculate next date
    LocalDate nextDate = schedulingService.calculateNextAvailableDate(
        startDate, service, 1);
    
    // Then: Should be at least 7 working days later
    long workingDays = holidayDateService.countWorkingDaysBetween(startDate, nextDate);
    assertTrue(workingDays >= 7);
}
```

#### Test AppointmentConstraintValidator
```java
@Test
void testValidateAppointmentConstraints_RejectsHoliday() {
    // Given: December 25 is a holiday
    LocalDateTime xmas = LocalDateTime.of(2025, 12, 25, 10, 0);
    DentalService service = createService();
    
    // When: Validate appointment on holiday
    ValidationResult result = validator.validateAppointmentConstraints(
        xmas, service, 123L);
    
    // Then: Should be invalid
    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("holiday"));
}
```

### 3. Integration Tests

#### Test End-to-End Treatment Plan Creation
```java
@Test
@Transactional
void testCreateTreatmentPlanFromTemplate_SkipsHolidays() {
    // Given: Template with 180 days duration, 3 services
    // Given: Dec 25, 2025 is a holiday
    
    // When: Create treatment plan starting Dec 20, 2025
    TreatmentPlanResponse plan = treatmentPlanService.createFromTemplate(
        patientId, templateId, LocalDate.of(2025, 12, 20));
    
    // Then: No appointments should be on Dec 25
    plan.getPhases().forEach(phase -> 
        phase.getAppointments().forEach(apt -> 
            assertNotEquals(LocalDate.of(2025, 12, 25), apt.getScheduledDate())
        )
    );
}
```

---

## Performance Considerations

### Database Indexes
```sql
-- Already created in migration script
CREATE INDEX idx_appointments_service_date 
ON appointments(service_id, appointment_start_time) 
WHERE status NOT IN ('CANCELLED', 'NO_SHOW');
```

### Query Optimization
- `countByServiceAndDate`: Uses indexed columns, filters cancelled/no-show
- `findRecentCompletedByPatient`: Limited to 10 results, sorted DESC
- Holiday checks: Cached in service layer (consider adding Redis cache for production)

### Caching Recommendations
```java
@Service
@CacheConfig(cacheNames = "holidays")
public class HolidayDateService {
    
    @Cacheable(key = "#date")
    public boolean isHoliday(LocalDate date) {
        // Cache result for 24 hours
    }
    
    @Cacheable(key = "#startDate + '-' + #endDate")
    public List<LocalDate> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        // Cache holiday list for date ranges
    }
}
```

---

## Troubleshooting

### Issue: Appointments scheduled on holidays
**Cause**: Holiday data not loaded in database  
**Solution**: 
```sql
-- Check if holidays exist
SELECT * FROM holiday_dates WHERE holiday_date = '2025-12-25';

-- Insert missing holidays
INSERT INTO holiday_dates (holiday_date, definition_id) 
VALUES ('2025-12-25', 'HD_XMAS_2025');
```

### Issue: Constraint validation fails unexpectedly
**Cause**: Service constraints not set correctly  
**Solution**:
```sql
-- Check service constraints
SELECT service_code, minimum_preparation_days, recovery_days, spacing_days, max_appointments_per_day
FROM services WHERE service_code = 'YOUR_SERVICE_CODE';

-- Update constraints
UPDATE services 
SET minimum_preparation_days = 7, recovery_days = 14
WHERE service_code = 'YOUR_SERVICE_CODE';
```

### Issue: Too many appointments rejected
**Cause**: Constraints too strict or holidays not configured  
**Solution**:
1. Review service constraint values (may be too high)
2. Ensure holiday data is complete and accurate
3. Check patient appointment history for conflicts

---

## Summary

### Files Created
1. ✅ `TreatmentPlanSchedulingService.java` - Auto-scheduling engine
2. ✅ `AppointmentConstraintValidator.java` - Constraint validation
3. ✅ `V26_add_service_appointment_constraints.sql` - Database migration

### Files Modified
1. ✅ `DentalService.java` - Added 4 constraint fields
2. ✅ `DentalServiceDTO.java` - Added constraint fields
3. ✅ `CreateServiceRequest.java` - Added constraint fields
4. ✅ `UpdateServiceRequest.java` - Added constraint fields
5. ✅ `ServiceResponse.java` - Added constraint fields
6. ✅ `ServiceMapper.java` - Map constraint fields
7. ✅ `AppointmentDentalServiceService.java` - Update service logic
8. ✅ `HolidayDateService.java` - Added scheduling methods
9. ✅ `AppointmentRepository.java` - Added validation queries
10. ✅ `schema.sql` - Updated services table definition

### Key Features Delivered
✅ Holiday detection and skipping  
✅ Service minimum preparation days  
✅ Service recovery days  
✅ Service spacing days  
✅ Service max appointments per day  
✅ Automatic appointment date calculation  
✅ Manual appointment validation  
✅ Database migration scripts  
✅ API DTO updates  
✅ Repository query methods  

---

## Next Steps (Optional Enhancements)

1. **Add UI Controls** (FE):
   - Service management form with constraint fields
   - Treatment plan calendar view showing holidays
   - Appointment booking with real-time validation

2. **Add Business Rules** (BE):
   - Weekend detection (Saturday/Sunday blocking)
   - Clinic operating hours validation
   - Doctor-specific working days

3. **Add Notifications** (BE/FE):
   - Email reminder: "Your appointment is on [working day], not a holiday"
   - Admin notification: "Service X has reached max daily appointments"

4. **Add Analytics** (BE):
   - Report: Most constrained services
   - Report: Holiday impact on scheduling
   - Report: Average days between appointments per service type

---

**Implementation Date**: December 11, 2025  
**Version**: BE_4 - Service Appointment Constraints  
**Status**: ✅ Complete and Ready for Testing
