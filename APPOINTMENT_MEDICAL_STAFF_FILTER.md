# Appointment Medical Staff Filter - Implementation Summary

## Vấn đề đã giải quyết

**Yêu cầu**: Chỉ hiển thị và cho phép chọn nhân viên có chuyên môn y tế (doctors, nurses, assistants) cho appointments, loại bỏ Admin/Receptionist.

**Giải pháp**: Sử dụng `employee_specializations` table để phân biệt:

- Medical staff CƠ BẢN = **PHẢI có STANDARD (ID 8)**
- Intern = có thể có INTERN (ID 9)
- Admin/Receptionist = **KHÔNG có specialization ID 8**

## Logic chính xác

### Medical Staff Identification

```
Medical Staff = Employee có specialization_id = 8 (STANDARD)

✅ STANDARD (ID 8) = Y tế cơ bản - Required for ALL medical staff
✅ INTERN (ID 9) = Thực tập sinh - Optional additional marker
❌ NO ID 8 = Admin/Receptionist - CANNOT be assigned to appointments
```

### Validation Rules

**Rule 1**: Doctor MUST have STANDARD (ID 8)

```java
employee.specializations.contains(ID = 8) → ✅ Can be doctor
employee.specializations.NOT contains(ID = 8) → ❌ REJECT
```

**Rule 2**: Participants MUST have STANDARD (ID 8)

```java
ALL participants.specializations.contains(ID = 8) → ✅ Proceed
ANY participant.specializations.NOT contains(ID = 8) → ❌ REJECT
```

**Rule 3**: Available times check MUST have STANDARD (ID 8)

```java
employee.specializations.contains(ID = 8) → ✅ Check availability
employee.specializations.NOT contains(ID = 8) → ❌ REJECT
```

## Changes Made

### 1. EmployeeRepository - Add Specialization Queries ✅

**File**: `EmployeeRepository.java`

**Added Methods**:

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

**Key Changes**:

- ✅ Uses `EXISTS (... WHERE specializationId = 8)` - checks for STANDARD specifically
- ✅ Uses `AND es.specializationId = 8` - only counts STANDARD specialization
- ❌ NO longer uses `SIZE(e.specializations) > 0` - too generic

### 2. EmployeeService - Add Medical Staff Methods ✅

**File**: `EmployeeService.java`

**Added Methods**:

```java
/**
 * Get active medical staff only (employees with STANDARD specialization ID 8)
 * Used for appointment doctor/participant selection
 */
@Transactional(readOnly = true)
public List<EmployeeInfoResponse> getActiveMedicalStaff() {
    // Only returns employees with STANDARD (ID 8)
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
    // Returns true only if employee has STANDARD (ID 8)
    return employeeRepository.hasSpecializations(employeeId);
}
```

**Key Points**:

- ✅ `getActiveMedicalStaff()` returns ONLY employees with ID 8
- ✅ `isMedicalStaff()` returns true ONLY if employee has ID 8
- ❌ Employees with other specializations but NO ID 8 → NOT returned

### 3. EmployeeController - Add Medical Staff Endpoint ✅

**File**: `EmployeeController.java`

**New Endpoint**:

```java
/**
 * GET /api/v1/employees/medical-staff
 * Get active medical staff only (employees with specializations)
 */
@GetMapping("/medical-staff")
@Operation(summary = "Get medical staff for appointments",
           description = "Get active employees with specializations (excludes admin/receptionist)")
@ApiMessage("Get medical staff successfully")
public ResponseEntity<List<EmployeeInfoResponse>> getActiveMedicalStaff() {
    List<EmployeeInfoResponse> medicalStaff = employeeService.getActiveMedicalStaff();
    return ResponseEntity.ok(medicalStaff);
}
```

### 4. AppointmentCreationService - Validate Doctor & Participants ✅

**File**: `AppointmentCreationService.java`

**Updated `validateDoctor()`**:

```java
private Employee validateDoctor(String employeeCode) {
    Employee doctor = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(() -> new BadRequestAlertException(...));

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

**Updated `validateParticipants()`**:

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

**Key Logic**:

- ✅ `anyMatch(spec -> spec.getSpecializationId() == 8)` - finds STANDARD
- ✅ `noneMatch(spec -> spec.getSpecializationId() == 8)` - filters out non-STANDARD
- ❌ Employees with only IDs 1-7, 9 (no ID 8) → REJECTED

**File**: `AppointmentCreationService.java`

**Updated `validateDoctor()`**:

```java
private Employee validateDoctor(String employeeCode) {
    Employee doctor = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(() -> new BadRequestAlertException(...));

    // CRITICAL: Validate employee has specializations (is medical staff)
    if (doctor.getSpecializations() == null || doctor.getSpecializations().isEmpty()) {
        throw new BadRequestAlertException(
                "Employee must have medical specializations to be assigned as doctor. " +
                "Employee " + employeeCode + " has no specializations (Admin/Receptionist cannot be doctors)",
                ENTITY_NAME,
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return doctor;
}
```

**Updated `validateParticipants()`**:

```java
private List<Employee> validateParticipants(List<String> participantCodes) {
    // ... existing code ...

    // CRITICAL: Validate all participants have specializations
    List<String> nonMedicalStaff = participants.stream()
            .filter(p -> p.getSpecializations() == null || p.getSpecializations().isEmpty())
            .map(Employee::getEmployeeCode)
            .collect(Collectors.toList());

    if (!nonMedicalStaff.isEmpty()) {
        throw new BadRequestAlertException(
                "Participants must have medical specializations. " +
                "The following employees have no specializations: " +
                String.join(", ", nonMedicalStaff),
                ENTITY_NAME,
                "PARTICIPANT_NOT_MEDICAL_STAFF");
    }

    return participants;
}
```

### 5. AppointmentAvailabilityService - Validate Employee & Participants ✅

**File**: `AppointmentAvailabilityService.java`

**Updated `validateEmployee()`**:

```java
private Employee validateEmployee(String employeeCode) {
    Employee employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(() -> new BadRequestAlertException(...));

    // CRITICAL: Validate employee has specializations
    if (employee.getSpecializations() == null || employee.getSpecializations().isEmpty()) {
        throw new BadRequestAlertException(
                "Employee must have medical specializations to be assigned to appointments. " +
                "Employee " + employeeCode + " has no specializations",
                "appointment",
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return employee;
}
```

**Updated `validateParticipants()`**:

```java
private List<Employee> validateParticipants(List<String> participantCodes) {
    // ... existing code ...

    List<String> nonMedicalStaff = new ArrayList<>();

    for (String code : participantCodes) {
        var emp = employeeRepository.findByEmployeeCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BadRequestAlertException(...));

        // Validate participant has specializations
        if (emp.getSpecializations() == null || emp.getSpecializations().isEmpty()) {
            nonMedicalStaff.add(code);
        } else {
            participants.add(emp);
        }
    }

    if (!nonMedicalStaff.isEmpty()) {
        throw new BadRequestAlertException(
                "Participants must have medical specializations. " +
                "The following employees have no specializations: " +
                String.join(", ", nonMedicalStaff),
                "appointment",
                "PARTICIPANT_NOT_MEDICAL_STAFF");
    }

    return participants;
}
```

## API Changes

### New Endpoint for Frontend

**GET /api/v1/employees/medical-staff**

**Purpose**: Lấy danh sách nhân viên y tế để hiển thị trong dropdown chọn doctor/participants

**Response**:

```json
[
  {
    "employeeId": 2,
    "employeeCode": "EMP002",
    "firstName": "Tâm",
    "lastName": "Nguyễn Thị",
    "fullName": "Tâm Nguyễn Thị",
    "specializations": [
      {
        "specializationId": 1,
        "specializationCode": "SPEC001",
        "specializationName": "Chỉnh nha"
      },
      {
        "specializationId": 8,
        "specializationCode": "SPEC-STANDARD",
        "specializationName": "Y tế cơ bản"
      }
    ]
  },
  {
    "employeeId": 4,
    "employeeCode": "EMP004",
    "firstName": "Mai",
    "lastName": "Lê Thị",
    "fullName": "Mai Lê Thị",
    "specializations": [
      {
        "specializationId": 8,
        "specializationCode": "SPEC-STANDARD",
        "specializationName": "Y tế cơ bản"
      }
    ]
  }
  // ... more medical staff
]
```

**Will NOT include**:

- ❌ EMP001 (Admin) - no specializations
- ❌ Any receptionist without specializations

## Validation Logic

### At Appointment Creation/Availability Check

**Doctor Validation**:

```
IF employee.specializations.isEmpty() THEN
    ❌ REJECT with "EMPLOYEE_NOT_MEDICAL_STAFF"
ELSE
    ✅ PROCEED to check specialization match with services
```

**Participant Validation**:

```
FOR EACH participant IN participantCodes:
    IF participant.specializations.isEmpty() THEN
        ❌ ADD to nonMedicalStaff list
    ELSE
        ✅ ADD to valid participants

IF nonMedicalStaff.isNotEmpty() THEN
    ❌ REJECT with "PARTICIPANT_NOT_MEDICAL_STAFF"
```

## Error Messages

### 1. Doctor without specialization

```json
{
  "statusCode": 400,
  "error": "EMPLOYEE_NOT_MEDICAL_STAFF",
  "message": "Employee must have medical specializations to be assigned as doctor. Employee EMP001 has no specializations (Admin/Receptionist cannot be doctors)"
}
```

### 2. Participant without specialization

```json
{
  "statusCode": 400,
  "error": "PARTICIPANT_NOT_MEDICAL_STAFF",
  "message": "Participants must have medical specializations. The following employees have no specializations (Admin/Receptionist cannot be participants): EMP001, EMP010"
}
```

## Testing Scenarios

### ✅ Success Cases

**1. Create appointment with medical staff**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP002",  # Doctor with specializations [1, 7, 8]
  "participantCodes": ["EMP004", "EMP005"],  # Nurses with [8]
  "serviceCodes": ["GEN_EXAM"]
}
→ 201 CREATED
```

**2. Get medical staff list**

```bash
GET /api/v1/employees/medical-staff
→ 200 OK with list of employees who have specializations
```

**3. Check available times with medical staff**

```bash
GET /api/v1/appointments/available-times?
  employeeCode=EMP002&
  participantCodes=EMP004,EMP005&
  serviceCodes=GEN_EXAM
→ 200 OK with available slots
```

### ❌ Failure Cases

**1. Try to use Admin as doctor**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP001",  # Admin - no specializations
  "serviceCodes": ["GEN_EXAM"]
}
→ 400 BAD REQUEST: "EMPLOYEE_NOT_MEDICAL_STAFF"
```

**2. Try to use Receptionist as participant**

```bash
POST /api/v1/appointments
{
  "employeeCode": "EMP002",
  "participantCodes": ["EMP010"],  # Receptionist - no specializations
  "serviceCodes": ["GEN_EXAM"]
}
→ 400 BAD REQUEST: "PARTICIPANT_NOT_MEDICAL_STAFF"
```

**3. Available times with non-medical staff**

```bash
GET /api/v1/appointments/available-times?
  employeeCode=EMP001&  # Admin
  serviceCodes=GEN_EXAM
→ 400 BAD REQUEST: "EMPLOYEE_NOT_MEDICAL_STAFF"
```

## Frontend Integration Guide

### 1. Load Medical Staff for Dropdown

**Old way** (shows everyone including admin):

```javascript
GET /api/v1/employees?isActive=true
```

**New way** (only medical staff):

```javascript
GET / api / v1 / employees / medical - staff;
```

### 2. Display Specializations

```javascript
// Employee with specializations
{
  employeeCode: "EMP002",
  fullName: "Tâm Nguyễn Thị",
  specializations: [
    { specializationName: "Chỉnh nha" },
    { specializationName: "Y tế cơ bản" }
  ]
}

// Display in dropdown:
"Tâm Nguyễn Thị (EMP002) - Chỉnh nha, Y tế cơ bản"
```

### 3. Handle Validation Errors

```javascript
try {
  await createAppointment({
    employeeCode: selectedDoctor,
    participantCodes: selectedParticipants,
    serviceCodes: selectedServices,
  });
} catch (error) {
  if (error.error === "EMPLOYEE_NOT_MEDICAL_STAFF") {
    showError(
      "Nhân viên được chọn không có chuyên môn y tế. Vui lòng chọn bác sĩ hoặc y tá."
    );
  } else if (error.error === "PARTICIPANT_NOT_MEDICAL_STAFF") {
    showError(
      "Một số người tham gia không có chuyên môn y tế. Chỉ có thể chọn bác sĩ, y tá."
    );
  }
}
```

## Migration Checklist

- [x] Add specialization queries to EmployeeRepository
- [x] Add medical staff methods to EmployeeService
- [x] Add `/medical-staff` endpoint to EmployeeController
- [x] Add validation in AppointmentCreationService (doctor & participants)
- [x] Add validation in AppointmentAvailabilityService (employee & participants)
- [x] Update seed data with STANDARD (ID 8) and INTERN (ID 9) specializations
- [x] Assign STANDARD to all medical staff in seed data
- [ ] Frontend: Update doctor/participant selection to use new endpoint
- [ ] Frontend: Handle new error codes (EMPLOYEE_NOT_MEDICAL_STAFF, PARTICIPANT_NOT_MEDICAL_STAFF)
- [ ] Test all appointment flows with filtered employees

## Summary

| Component                      | Change                                                         | Status  |
| ------------------------------ | -------------------------------------------------------------- | ------- |
| EmployeeRepository             | Added specialization filter queries                            | ✅ Done |
| EmployeeService                | Added getActiveMedicalStaff() & isMedicalStaff()               | ✅ Done |
| EmployeeController             | Added GET /medical-staff endpoint                              | ✅ Done |
| AppointmentCreationService     | Validate doctor & participants have specializations            | ✅ Done |
| AppointmentAvailabilityService | Validate employee & participants have specializations          | ✅ Done |
| Seed Data                      | Added STANDARD (8) & INTERN (9), assigned to all medical staff | ✅ Done |

**Result**:

- ✅ Admin/Receptionist (no specializations) CANNOT be selected as doctor/participants
- ✅ Only medical staff (with specializations) appear in appointment dropdowns
- ✅ Clear error messages when trying to use non-medical staff
- ✅ NO schema changes required - uses existing employee_specializations table
