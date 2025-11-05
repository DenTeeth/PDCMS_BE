# Appointment Medical Staff Filter - CORRECT Implementation (V2)

## ⚠️ CRITICAL CHANGE: Specific ID Validation

**Vấn đề cũ**: Check `SIZE(specializations) > 0` - quá generic, không đảm bảo STANDARD
**Giải pháp mới**: Check `specializationId == 8` - chính xác STANDARD only

## Logic đúng 100%

### Medical Staff Definition

```
Medical Staff = Employee có STANDARD specialization (ID 8)

✅ ID 8 (SPEC-STANDARD) = Y tế cơ bản - REQUIRED for ALL medical staff
✅ ID 9 (SPEC-INTERN) = Thực tập sinh - Optional, can be combined with ID 8
✅ ID 1-7 = Specific specializations - Optional, can be combined with ID 8

❌ NO ID 8 = Admin/Receptionist - CANNOT be assigned to appointments
```

### Validation Rules

**Rule 1: Doctor Validation**

```java
// MUST have STANDARD (ID 8)
doctor.specializations.stream().anyMatch(spec -> spec.specializationId == 8)
→ ✅ Can be doctor

doctor.specializations.stream().noneMatch(spec -> spec.specializationId == 8)
→ ❌ REJECT with "EMPLOYEE_NOT_MEDICAL_STAFF"
```

**Rule 2: Participant Validation**

```java
// ALL participants MUST have STANDARD (ID 8)
ALL participants.specializations.contains(ID 8)
→ ✅ Proceed

ANY participant.specializations.NOT contains(ID 8)
→ ❌ REJECT with "PARTICIPANT_NOT_MEDICAL_STAFF"
```

**Rule 3: Available Times Validation**

```java
// Employee MUST have STANDARD (ID 8) to check availability
employee.specializations.contains(ID 8)
→ ✅ Check available slots

employee.specializations.NOT contains(ID 8)
→ ❌ REJECT with "EMPLOYEE_NOT_MEDICAL_STAFF"
```

## Code Implementation

### 1. EmployeeRepository - Specific ID 8 Query ✅

**File**: `EmployeeRepository.java`

```java
/**
 * Find ACTIVE employees who have STANDARD specialization (ID 8) - medical staff only
 * STANDARD (ID 8) = General healthcare workers baseline
 */
@Query("SELECT DISTINCT e FROM Employee e " +
       "LEFT JOIN FETCH e.specializations s " +
       "WHERE e.isActive = true " +
       "AND EXISTS (SELECT 1 FROM e.specializations es WHERE es.specializationId = 8) " +
       "ORDER BY e.employeeCode ASC")
List<Employee> findActiveEmployeesWithSpecializations();

/**
 * Check if employee has STANDARD specialization (ID 8) - is medical staff
 */
@Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END " +
       "FROM Employee e JOIN e.specializations es " +
       "WHERE e.employeeId = :employeeId " +
       "AND es.specializationId = 8")
boolean hasSpecializations(@Param("employeeId") Integer employeeId);
```

**Key Points**:

- ✅ `EXISTS (... WHERE es.specializationId = 8)` - specifically checks for ID 8
- ✅ `AND es.specializationId = 8` - counts only STANDARD
- ❌ NO `SIZE(e.specializations) > 0` - too generic, would include employees with only IDs 1-7

### 2. EmployeeService - Returns ID 8 Only ✅

**File**: `EmployeeService.java`

```java
/**
 * Get active medical staff only (employees with STANDARD specialization ID 8)
 */
@Transactional(readOnly = true)
public List<EmployeeInfoResponse> getActiveMedicalStaff() {
    // Repository query ensures only employees with ID 8 are returned
    List<Employee> employees = employeeRepository.findActiveEmployeesWithSpecializations();
    return employees.stream()
            .map(employeeMapper::toEmployeeInfoResponse)
            .collect(Collectors.toList());
}

/**
 * Check if employee is medical staff (has STANDARD specialization ID 8)
 */
@Transactional(readOnly = true)
public boolean isMedicalStaff(Integer employeeId) {
    // Returns true ONLY if employee has ID 8
    return employeeRepository.hasSpecializations(employeeId);
}
```

### 3. EmployeeController - Medical Staff Endpoint ✅

**File**: `EmployeeController.java`

```java
/**
 * GET /api/v1/employees/medical-staff
 * Returns only employees with STANDARD specialization (ID 8)
 */
@GetMapping("/medical-staff")
@Operation(summary = "Get medical staff for appointments",
           description = "Get active employees with STANDARD specialization (ID 8)")
@ApiMessage("Get medical staff successfully")
public ResponseEntity<List<EmployeeInfoResponse>> getActiveMedicalStaff() {
    List<EmployeeInfoResponse> medicalStaff = employeeService.getActiveMedicalStaff();
    return ResponseEntity.ok(medicalStaff);
}
```

### 4. AppointmentCreationService - Validate STANDARD ✅

**File**: `AppointmentCreationService.java`

**Validate Doctor**:

```java
private Employee validateDoctor(String employeeCode) {
    Employee doctor = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(() -> new BadRequestAlertException(
                    "Employee not found or inactive: " + employeeCode,
                    ENTITY_NAME,
                    "EMPLOYEE_NOT_FOUND"));

    // CRITICAL: Check for STANDARD specialization (ID 8) specifically
    boolean hasStandardSpecialization = doctor.getSpecializations() != null &&
            doctor.getSpecializations().stream()
                    .anyMatch(spec -> spec.getSpecializationId() == 8);

    if (!hasStandardSpecialization) {
        throw new BadRequestAlertException(
                "Employee must have STANDARD specialization (ID 8) to be assigned as doctor. " +
                "Employee " + employeeCode + " does not have STANDARD specialization",
                ENTITY_NAME,
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return doctor;
}
```

**Validate Participants**:

```java
private List<Employee> validateParticipants(List<String> participantCodes) {
    // ... existing code to fetch participants ...

    // CRITICAL: Check each participant has STANDARD (ID 8)
    List<String> nonMedicalStaff = participants.stream()
            .filter(p -> p.getSpecializations() == null ||
                    p.getSpecializations().stream()
                            .noneMatch(spec -> spec.getSpecializationId() == 8))
            .map(Employee::getEmployeeCode)
            .collect(Collectors.toList());

    if (!nonMedicalStaff.isEmpty()) {
        throw new BadRequestAlertException(
                "Participants must have STANDARD specialization (ID 8). " +
                "The following employees do not have STANDARD: " +
                String.join(", ", nonMedicalStaff),
                ENTITY_NAME,
                "PARTICIPANT_NOT_MEDICAL_STAFF");
    }

    return participants;
}
```

### 5. AppointmentAvailabilityService - Validate STANDARD ✅

**File**: `AppointmentAvailabilityService.java`

**Validate Employee**:

```java
private Employee validateEmployee(String employeeCode) {
    Employee employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(() -> new BadRequestAlertException(
                    "Employee not found or inactive: " + employeeCode,
                    "appointment",
                    "EMPLOYEE_NOT_FOUND"));

    // CRITICAL: Check for STANDARD specialization (ID 8)
    boolean hasStandardSpecialization = employee.getSpecializations() != null &&
            employee.getSpecializations().stream()
                    .anyMatch(spec -> spec.getSpecializationId() == 8);

    if (!hasStandardSpecialization) {
        throw new BadRequestAlertException(
                "Employee must have STANDARD specialization (ID 8) to be assigned to appointments. " +
                "Employee " + employeeCode + " does not have STANDARD specialization",
                "appointment",
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return employee;
}
```

**Validate Participants**:

```java
private List<Employee> validateParticipants(List<String> participantCodes) {
    if (participantCodes == null || participantCodes.isEmpty()) {
        return Collections.emptyList();
    }

    List<Employee> participants = new ArrayList<>();
    List<String> nonMedicalStaff = new ArrayList<>();

    for (String code : participantCodes) {
        var emp = employeeRepository.findByEmployeeCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Participant not found or inactive: " + code,
                        "appointment",
                        "PARTICIPANT_NOT_FOUND"));

        // CRITICAL: Check for STANDARD specialization (ID 8)
        boolean hasStandardSpecialization = emp.getSpecializations() != null &&
                emp.getSpecializations().stream()
                        .anyMatch(spec -> spec.getSpecializationId() == 8);

        if (!hasStandardSpecialization) {
            nonMedicalStaff.add(code);
        } else {
            participants.add(emp);
        }
    }

    if (!nonMedicalStaff.isEmpty()) {
        throw new BadRequestAlertException(
                "Participants must have STANDARD specialization (ID 8). " +
                "The following employees do not have STANDARD: " +
                String.join(", ", nonMedicalStaff),
                "appointment",
                "PARTICIPANT_NOT_MEDICAL_STAFF");
    }

    return participants;
}
```

## Test Scenarios with EXACT ID Checks

### ✅ Success Cases

**1. Medical staff with STANDARD (ID 8)**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP002",  # Has IDs [1, 7, 8] → ✅ Has ID 8
  "serviceCodes": ["GEN_EXAM"]
}
→ 201 CREATED
```

**2. Nurse with only STANDARD**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP004",  # Has ID [8] → ✅ Has ID 8
  "serviceCodes": ["GEN_EXAM"]
}
→ 201 CREATED
```

**3. Intern with STANDARD + INTERN**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP010",  # Has IDs [8, 9] → ✅ Has ID 8
  "serviceCodes": ["GEN_EXAM"]
}
→ 201 CREATED
```

### ❌ Failure Cases

**1. Admin without any specialization**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP001",  # Has NO specializations → ❌ No ID 8
  "serviceCodes": ["GEN_EXAM"]
}
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

**2. Hypothetical: Employee with only ORTHO (ID 1), no STANDARD**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP999",  # Has ONLY ID [1] → ❌ No ID 8
  "serviceCodes": ["ORTHO_BRACES_ON"]
}
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

**3. Intern with only ID 9, no STANDARD**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP888",  # Has ONLY ID [9] → ❌ No ID 8
  "serviceCodes": ["GEN_EXAM"]
}
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

## Database Seed Data

### Specializations

```sql
-- ID 8 is REQUIRED for all medical staff
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
(8, 'SPEC-STANDARD', 'Y tế cơ bản', 'General Healthcare - Baseline for ALL medical staff', TRUE, NOW()),
(9, 'SPEC-INTERN', 'Thực tập sinh', 'Intern/Trainee - Optional additional marker', TRUE, NOW());
```

### Employee Specializations Assignment

```sql
-- ALL medical staff MUST have ID 8 (STANDARD)
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
-- Bác sĩ Tâm: Chỉnh nha + Răng thẩm mỹ + STANDARD
(2, 1), (2, 7), (2, 8),

-- Bác sĩ Dũng: Nội nha + Phục hồi + STANDARD
(3, 2), (3, 4), (3, 8),

-- Y tá Mai: ONLY STANDARD
(4, 8),

-- Y tá Hương: ONLY STANDARD
(5, 8),

-- Bác sĩ Hạnh: Nha khoa trẻ em + STANDARD
(6, 6), (6, 8),

-- Y tá Thảo: ONLY STANDARD
(7, 8),

-- Part-time Linh: ONLY STANDARD
(8, 8),

-- Part-time Trang: ONLY STANDARD
(9, 8);

-- EMP001 (Admin) has NO entries → No ID 8 → Cannot be assigned to appointments
```

## API Response Examples

### GET /api/v1/employees/medical-staff

**Response** (only employees with ID 8):

```json
[
  {
    "employeeId": 2,
    "employeeCode": "EMP002",
    "fullName": "Tâm Nguyễn Thị",
    "specializations": [
      { "specializationId": 1, "specializationName": "Chỉnh nha" },
      { "specializationId": 7, "specializationName": "Răng thẩm mỹ" },
      { "specializationId": 8, "specializationName": "Y tế cơ bản" } ← HAS ID 8 ✅
    ]
  },
  {
    "employeeId": 4,
    "employeeCode": "EMP004",
    "fullName": "Mai Lê Thị",
    "specializations": [
      { "specializationId": 8, "specializationName": "Y tế cơ bản" } ← HAS ID 8 ✅
    ]
  }
  // ... EMP001 NOT included - no ID 8 ❌
]
```

### Error Response for Non-Medical Staff

```json
{
  "statusCode": 400,
  "error": "EMPLOYEE_NOT_MEDICAL_STAFF",
  "message": "Employee must have STANDARD specialization (ID 8) to be assigned as doctor. Employee EMP001 does not have STANDARD specialization"
}
```

## Summary: OLD vs NEW Logic

### ❌ OLD (Generic - Wrong)

```java
// TOO GENERIC - accepts ANY specialization
if (employee.getSpecializations().isEmpty()) {
    throw Exception;
}

// Problem: Employee with ONLY ID 1 (ORTHO) would pass
// Problem: Doesn't enforce STANDARD (ID 8) requirement
```

### ✅ NEW (Specific - Correct)

```java
// SPECIFIC - checks for ID 8 (STANDARD) only
boolean hasStandardSpecialization = employee.getSpecializations().stream()
        .anyMatch(spec -> spec.getSpecializationId() == 8);

if (!hasStandardSpecialization) {
    throw Exception;
}

// Correct: Employee MUST have ID 8 to pass
// Correct: Enforces STANDARD as baseline requirement
```

## Migration Checklist

- [x] EmployeeRepository - Use `WHERE es.specializationId = 8` (not SIZE > 0)
- [x] EmployeeService - Use repository with ID 8 filter
- [x] EmployeeController - Endpoint returns only ID 8 employees
- [x] AppointmentCreationService - Check `spec.getSpecializationId() == 8`
- [x] AppointmentAvailabilityService - Check `spec.getSpecializationId() == 8`
- [x] Seed data - All medical staff have ID 8
- [ ] Frontend - Use GET /api/v1/employees/medical-staff
- [ ] Frontend - Handle error messages with ID 8 mention
- [ ] Test all scenarios with ID validation

## Key Takeaway

**Medical Staff = Has STANDARD (ID 8)**

```
NO ID 8 = NOT medical staff = CANNOT be assigned to appointments

This is HARDCODED by design:
- ID 8 is the BASELINE for all medical workers
- Specific specializations (1-7) are ADDITIONAL, not REPLACEMENT
- Intern (ID 9) can be COMBINED with ID 8
- Admin/Receptionist have NO specializations at all
```
