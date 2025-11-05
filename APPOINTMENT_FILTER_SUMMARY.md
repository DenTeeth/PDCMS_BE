# Appointment Medical Staff Filter - Final Summary

## ✅ Logic Chính Xác 100%

### Quy tắc đơn giản

```
CÓ STANDARD (ID 8) = Medical Staff = CÓ THỂ tham gia appointment ✅
KHÔNG có STANDARD (ID 8) = Admin/Receptionist = KHÔNG THỂ tham gia ❌
```

### Phân loại nhân viên

**1. Medical Staff (CÓ ID 8)**:

```sql
-- Bác sĩ với chuyên khoa
Employee: EMP002
Specializations: [1 (Chỉnh nha), 7 (Răng thẩm mỹ), 8 (STANDARD)]
→ ✅ Có ID 8 → CÓ THỂ làm doctor/participant

-- Y tá cơ bản
Employee: EMP004
Specializations: [8 (STANDARD)]
→ ✅ Có ID 8 → CÓ THỂ làm doctor/participant

-- Thực tập sinh Y TẾ
Employee: EMP010 (giả định)
Specializations: [8 (STANDARD), 9 (INTERN)]
→ ✅ Có ID 8 → CÓ THỂ làm doctor/participant
```

**2. Admin/Receptionist (KHÔNG có ID 8)**:

```sql
-- Admin/Receptionist
Employee: EMP001
Specializations: [] (empty - không có specialization nào)
→ ❌ KHÔNG có ID 8 → KHÔNG THỂ tham gia appointment

-- Thực tập sinh Lễ tân (giả định)
Employee: EMP999
Specializations: [9 (INTERN)]
→ ❌ KHÔNG có ID 8 → KHÔNG THỂ tham gia appointment
```

### Role của ID 9 (INTERN)

```
ID 9 (INTERN) chỉ là MARKER để đánh dấu "đây là thực tập sinh"
KHÔNG liên quan đến appointment validation

Thực tập sinh Y TẾ = có ID 8 + ID 9 → ✅ Có thể tham gia appointment
Thực tập sinh Lễ tân = chỉ có ID 9 → ❌ Không thể tham gia appointment
```

## Code Implementation - Chỉ Check ID 8

### 1. EmployeeRepository - WHERE specializationId = 8

```java
@Query("SELECT DISTINCT e FROM Employee e " +
       "LEFT JOIN FETCH e.specializations s " +
       "WHERE e.isActive = true " +
       "AND EXISTS (SELECT 1 FROM e.specializations es WHERE es.specializationId = 8) " +
       "ORDER BY e.employeeCode ASC")
List<Employee> findActiveEmployeesWithSpecializations();
```

**Giải thích**:

- `WHERE es.specializationId = 8` → Chỉ check STANDARD
- KHÔNG check ID 9 (INTERN)
- Employee có ID 1-7 nhưng KHÔNG có ID 8 → KHÔNG được trả về

### 2. AppointmentCreationService - anyMatch ID 8

```java
private Employee validateDoctor(String employeeCode) {
    Employee doctor = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(...);

    // CRITICAL: Chỉ check STANDARD (ID 8)
    boolean hasStandardSpecialization = doctor.getSpecializations() != null &&
            doctor.getSpecializations().stream()
                    .anyMatch(spec -> spec.getSpecializationId() == 8);

    if (!hasStandardSpecialization) {
        throw new BadRequestAlertException(
                "Employee must have STANDARD specialization (ID 8) to be assigned as doctor",
                ENTITY_NAME,
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return doctor;
}
```

**Giải thích**:

- `spec.getSpecializationId() == 8` → Hardcoded check ID 8
- KHÔNG check `isEmpty()` - quá generic
- KHÔNG check ID 9

### 3. AppointmentAvailabilityService - anyMatch ID 8

```java
private Employee validateEmployee(String employeeCode) {
    Employee employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(employeeCode)
            .orElseThrow(...);

    // CRITICAL: Chỉ check STANDARD (ID 8)
    boolean hasStandardSpecialization = employee.getSpecializations() != null &&
            employee.getSpecializations().stream()
                    .anyMatch(spec -> spec.getSpecializationId() == 8);

    if (!hasStandardSpecialization) {
        throw new BadRequestAlertException(
                "Employee must have STANDARD specialization (ID 8) to be assigned to appointments",
                "appointment",
                "EMPLOYEE_NOT_MEDICAL_STAFF");
    }

    return employee;
}
```

## Test Cases với Logic ID 8

### ✅ SUCCESS - Có ID 8

**Case 1: Bác sĩ với nhiều specializations**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP002",
  "serviceCodes": ["GEN_EXAM"]
}

Employee EMP002 specializations: [1, 7, 8]
→ Has ID 8 ✅
→ 201 CREATED
```

**Case 2: Y tá chỉ có STANDARD**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP004",
  "serviceCodes": ["GEN_EXAM"]
}

Employee EMP004 specializations: [8]
→ Has ID 8 ✅
→ 201 CREATED
```

**Case 3: Thực tập sinh Y TẾ (có cả 8 và 9)**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP010",
  "serviceCodes": ["GEN_EXAM"]
}

Employee EMP010 specializations: [8, 9]
→ Has ID 8 ✅
→ 201 CREATED
```

### ❌ FAILURE - Không có ID 8

**Case 1: Admin (không có specialization)**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP001",
  "serviceCodes": ["GEN_EXAM"]
}

Employee EMP001 specializations: []
→ Does NOT have ID 8 ❌
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

**Case 2: Thực tập sinh Lễ tân (chỉ có ID 9)**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP999",
  "serviceCodes": ["GEN_EXAM"]
}

Employee EMP999 specializations: [9]
→ Does NOT have ID 8 ❌
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

**Case 3: Giả định - Employee chỉ có ORTHO (ID 1)**

```json
POST /api/v1/appointments
{
  "employeeCode": "EMP888",
  "serviceCodes": ["ORTHO_BRACES_ON"]
}

Employee EMP888 specializations: [1]
→ Does NOT have ID 8 ❌
→ 400 BAD REQUEST: "Employee must have STANDARD specialization (ID 8)"
```

## Database Seed Data

### Specializations Table

```sql
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
-- ID 8 là REQUIRED cho TẤT CẢ medical staff
(8, 'SPEC-STANDARD', 'Y tế cơ bản', 'General Healthcare - Baseline for ALL medical staff', TRUE, NOW()),

-- ID 9 chỉ là marker cho interns, KHÔNG bắt buộc cho appointment
(9, 'SPEC-INTERN', 'Thực tập sinh', 'Intern/Trainee - Optional marker for trainees', TRUE, NOW());
```

### Employee Specializations - TẤT CẢ medical staff có ID 8

```sql
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
-- Bác sĩ Tâm: Chuyên khoa + STANDARD
(2, 1), (2, 7), (2, 8),

-- Bác sĩ Dũng: Chuyên khoa + STANDARD
(3, 2), (3, 4), (3, 8),

-- Y tá Mai: CHỈ STANDARD (đủ để tham gia appointment)
(4, 8),

-- Y tá Hương: CHỈ STANDARD
(5, 8),

-- Bác sĩ Hạnh: Chuyên khoa + STANDARD
(6, 6), (6, 8),

-- Y tá Thảo: CHỈ STANDARD
(7, 8),

-- Part-time Linh: CHỈ STANDARD
(8, 8),

-- Part-time Trang: CHỈ STANDARD
(9, 8);

-- KHÔNG có entry cho EMP001 (Admin) → Không có ID 8 → Không thể tham gia appointment
```

### Khi tạo Thực tập sinh Y TẾ

```sql
-- Thực tập sinh Y TẾ PHẢI có cả ID 8 và ID 9
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(10, 8),  -- STANDARD (bắt buộc để tham gia appointment)
(10, 9);  -- INTERN (marker để đánh dấu là thực tập sinh)
```

### Khi tạo Thực tập sinh Lễ tân

```sql
-- Thực tập sinh Lễ tân CHỈ có ID 9 (không có ID 8)
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(11, 9);  -- CHỈ INTERN, KHÔNG có STANDARD → Không thể tham gia appointment
```

## API Response

### GET /api/v1/employees/medical-staff

**Chỉ trả về employees CÓ ID 8**:

```json
[
  {
    "employeeId": 2,
    "employeeCode": "EMP002",
    "fullName": "Tâm Nguyễn Thị",
    "specializations": [
      { "specializationId": 1, "specializationName": "Chỉnh nha" },
      { "specializationId": 7, "specializationName": "Răng thẩm mỹ" },
      { "specializationId": 8, "specializationName": "Y tế cơ bản" } ← ✅ Có ID 8
    ]
  },
  {
    "employeeId": 4,
    "employeeCode": "EMP004",
    "fullName": "Mai Lê Thị",
    "specializations": [
      { "specializationId": 8, "specializationName": "Y tế cơ bản" } ← ✅ Có ID 8
    ]
  },
  {
    "employeeId": 10,
    "employeeCode": "EMP010",
    "fullName": "Intern Y Tế",
    "specializations": [
      { "specializationId": 8, "specializationName": "Y tế cơ bản" }, ← ✅ Có ID 8
      { "specializationId": 9, "specializationName": "Thực tập sinh" }
    ]
  }
  // EMP001 (Admin) KHÔNG xuất hiện - không có ID 8
  // EMP999 (Intern Lễ tân) KHÔNG xuất hiện - chỉ có ID 9, không có ID 8
]
```

## Summary - Key Points

### ✅ Quy tắc DUY NHẤT

```
Appointment validation CHỈ check:
- Có specialization_id = 8 (STANDARD) → ✅ Pass
- Không có specialization_id = 8 → ❌ Reject

KHÔNG check:
- ID 9 (INTERN) - chỉ là marker, không liên quan validation
- IDs 1-7 (Chuyên khoa) - optional, không thay thế ID 8
- SIZE(specializations) > 0 - quá generic, sai logic
```

### 🎯 Use Cases

**Medical Staff (có ID 8)**:

- ✅ Bác sĩ có chuyên khoa: IDs [1, 8] hoặc [2, 4, 8], etc.
- ✅ Y tá cơ bản: ID [8]
- ✅ Thực tập sinh Y TẾ: IDs [8, 9]

**Non-Medical Staff (không có ID 8)**:

- ❌ Admin: IDs [] (empty)
- ❌ Receptionist: IDs [] (empty)
- ❌ Thực tập sinh Lễ tân: IDs [9] (chỉ có INTERN)

### 📝 Khi tạo Employee mới

```
Tạo BỆNH VIÊN Y TẾ (bất kỳ role):
→ PHẢI add specialization_id = 8 (STANDARD)
→ Có thể add thêm IDs 1-7 (chuyên khoa) hoặc ID 9 (intern)

Tạo ADMIN/RECEPTIONIST:
→ KHÔNG add bất kỳ specialization nào
→ Hoặc chỉ add ID 9 nếu là intern lễ tân

Logic đơn giản:
- Cần tham gia appointment → Có ID 8
- Không tham gia appointment → Không có ID 8
```

## Files Changed

| File                                | Change                           | Status  |
| ----------------------------------- | -------------------------------- | ------- |
| EmployeeRepository.java             | `WHERE es.specializationId = 8`  | ✅ Done |
| EmployeeService.java                | Returns employees with ID 8 only | ✅ Done |
| EmployeeController.java             | GET /medical-staff endpoint      | ✅ Done |
| AppointmentCreationService.java     | `anyMatch(spec.id == 8)`         | ✅ Done |
| AppointmentAvailabilityService.java | `anyMatch(spec.id == 8)`         | ✅ Done |
| dental-clinic-seed-data_v2.sql      | All medical staff have ID 8      | ✅ Done |

**Result**:

- ✅ Code CHỈ check ID 8 (STANDARD) - không check ID 9 (INTERN)
- ✅ Medical staff CÓ ID 8 = có thể tham gia appointment
- ✅ Admin/Receptionist KHÔNG có ID 8 = không thể tham gia
- ✅ Thực tập sinh Y TẾ (có 8+9) = có thể tham gia
- ✅ Thực tập sinh Lễ tân (chỉ có 9) = không thể tham gia
