# Specialization Strategy - Giải pháp phân biệt nhân viên y tế không cần thay đổi schema

## Vấn đề ban đầu

1. **POST /api/v1/services** trả về 500 error - thiếu endpoint
2. **GET /api/v1/services/100** - không có error handling khi service không tồn tại
3. **Phân biệt nhân viên**: Cần phân biệt nhân viên y tế (doctor, nurse) vs nhân viên hành chính (admin, receptionist) mà KHÔNG thay đổi schema database

## Giải pháp đã áp dụng

### 1. Sửa lỗi Service API ✅

**File**: `ServiceController.java`

**Changes**:

- ✅ Added `POST /api/v1/services` endpoint
- ✅ Returns HTTP 201 CREATED status
- ✅ Validates request with `@Valid CreateServiceRequest`
- ✅ Already has error handling trong `DentalServiceService.getServiceById()` (throws BadRequestAlertException)

```java
@PostMapping
@PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_SERVICE + "')")
@Operation(summary = "Create new service")
@ApiMessage("Tạo dịch vụ mới thành công")
public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
    ServiceResponse response = serviceService.createService(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 2. Thêm 2 Specialization mới ✅

**File**: `dental-clinic-seed-data_postgres_v2.sql`

**Specializations mới**:

```sql
(8, 'SPEC-STANDARD', 'Y tế cơ bản', 'General Healthcare - Nhân viên y tế có chuyên môn cơ bản', TRUE, NOW()),
(9, 'SPEC-INTERN', 'Thực tập sinh', 'Intern/Trainee - Nhân viên đang đào tạo, học việc', TRUE, NOW())
```

**Ý nghĩa**:

- **ID 8 - STANDARD**: Đại diện cho nhân viên y tế có chuyên môn cơ bản

  - Bác sĩ, y tá, phụ tá đều có STANDARD
  - Dùng để filter trong appointment queries
  - Employees WITHOUT specialization = Admin/Receptionist (không xuất hiện trong danh sách chọn bác sĩ)

- **ID 9 - INTERN**: Cho thực tập sinh/học việc
  - FE tick "có chuyên môn" nhưng chưa có specialized field → chọn INTERN
  - Tránh trường hợp `isSpecialization=true` nhưng `specializationIds=[]`

### 3. Gán STANDARD cho tất cả nhân viên y tế ✅

**File**: `dental-clinic-seed-data_postgres_v2.sql`

**employee_specializations**:

```sql
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
-- Doctors với chuyên khoa cụ thể
(2, 1), (2, 7),  -- Bác sĩ Tâm: Chỉnh nha + Răng thẩm mỹ
(3, 2), (3, 4),  -- Bác sĩ Dũng: Nội nha + Phục hồi răng
(6, 6),          -- Bác sĩ Hạnh: Nha khoa trẻ em

-- Add STANDARD cho TẤT CẢ nhân viên y tế
(2, 8), -- Bác sĩ Tâm
(3, 8), -- Bác sĩ Dũng
(4, 8), -- Y tá Mai
(5, 8), -- Y tá Hương
(6, 8), -- Bác sĩ Hạnh
(7, 8), -- Y tá Thảo
(8, 8), -- Linh (Part-time)
(9, 8)  -- Trang (Part-time)
```

**Logic**:

- Nhân viên y tế: ALWAYS có ít nhất specialization ID 8 (STANDARD)
- Admin/Receptionist: KHÔNG có bất kỳ specialization nào
- Filter query: `WHERE employee.specializations.size() > 0` → chỉ lấy người có chuyên môn

### 4. Cập nhật Services với Specialization đúng ✅

**File**: `dental-clinic-seed-data_postgres_v2.sql`

**Mapping chuyên khoa**:

```sql
-- 8: Y tế cơ bản (STANDARD) - Dịch vụ tổng quát
-- 1: Chỉnh nha
-- 2: Nội nha
-- 3: Nha chu
-- 4: Phục hồi răng
-- 5: Phẫu thuật hàm mặt
-- 6: Nha khoa trẻ em
-- 7: Răng thẩm mỹ
```

**Examples**:

```sql
-- General services → STANDARD (ID 8)
('GEN_EXAM', '...', ..., 8, true, NOW()),
('GEN_XRAY_PERI', '...', ..., 8, true, NOW()),
('EMERG_PAIN', 'Khám cấp cứu', ..., 8, true, NOW()),

-- Specialized services
('SCALING_L1', 'Cạo vôi', ..., 3, true, NOW()),     -- Nha chu
('FILLING_COMP', 'Trám răng', ..., 2, true, NOW()), -- Nội nha
('ORTHO_BRACES_ON', 'Niềng răng', ..., 1, true, NOW()),  -- Chỉnh nha
('CROWN_EMAX', 'Mão sứ Emax', ..., 4, true, NOW()),     -- Phục hồi răng
('EXTRACT_WISDOM_L1', 'Nhổ răng khôn', ..., 5, true, NOW()), -- Phẫu thuật
('EXTRACT_MILK', 'Nhổ răng sữa', ..., 6, true, NOW()),  -- Nha khoa trẻ em
('VENEER_EMAX', 'Mặt dán sứ', ..., 7, true, NOW()),     -- Răng thẩm mỹ
```

### 5. Filter Logic trong Appointment Queries ✅

**Hiện tại đã tự động hoạt động** do:

```java
// AppointmentAvailabilityService.java
private void validateDoctorSpecialization(Employee employee, List<DentalService> services) {
    Set<Integer> requiredSpecializations = services.stream()
        .map(DentalService::getSpecialization)
        .filter(Objects::nonNull)
        .map(spec -> spec.getSpecializationId())
        .collect(Collectors.toSet());

    Set<Integer> employeeSpecializations = employee.getSpecializations().stream()
        .map(s -> s.getSpecializationId())
        .collect(Collectors.toSet());

    if (!employeeSpecializations.containsAll(requiredSpecializations)) {
        throw new BadRequestAlertException(...);
    }
}
```

**Flow**:

1. Service `GEN_EXAM` requires specialization ID 8 (STANDARD)
2. Employee EMP002 (Bác sĩ Tâm) has specializations: [1, 7, 8]
3. Check: [8] ⊆ [1, 7, 8] → ✅ PASS
4. Admin (no specializations) → ❌ FAIL → không xuất hiện trong danh sách

## Ưu điểm của giải pháp

✅ **Không cần thay đổi schema**: Sử dụng lại bảng `employee_specializations` hiện có
✅ **Backward compatible**: Code hiện tại vẫn hoạt động bình thường
✅ **Flexible**: FE có thể chọn STANDARD hoặc INTERN khi tạo employee mới
✅ **Clear separation**:

- `specializations.isEmpty()` → Admin/Receptionist
- `specializations.contains(8)` → Medical staff
- `specializations.contains(9)` → Intern
- `specializations.contains(1-7)` → Specialized doctor
  ✅ **Filter tự động**: Appointment queries chỉ trả về employees có specialization

## Hướng dẫn sử dụng cho FE

### Tạo Employee mới

**Case 1: Bác sĩ có chuyên khoa cụ thể**

```json
{
  "roleId": "ROLE_DOCTOR",
  "specializationIds": [1, 8] // Chỉnh nha + STANDARD
}
```

**Case 2: Y tá (chỉ cần STANDARD)**

```json
{
  "roleId": "ROLE_NURSE",
  "specializationIds": [8] // STANDARD only
}
```

**Case 3: Thực tập sinh**

```json
{
  "roleId": "ROLE_DOCTOR",
  "specializationIds": [9] // INTERN
}
```

**Case 4: Admin/Receptionist**

```json
{
  "roleId": "ROLE_ADMIN",
  "specializationIds": [] // Empty - không có chuyên môn y tế
}
```

### GET Available Employees cho Appointment

**Request**:

```
GET /api/v1/appointments/available-times?
  date=2025-11-04&
  employeeCode=EMP002&
  serviceCodes=GEN_EXAM,SCALING_L1
```

**Response** sẽ CHỈ bao gồm:

- ✅ Employees có specialization (STANDARD hoặc specialized)
- ❌ KHÔNG bao gồm Admin, Receptionist (no specialization)

## Testing

### Test Cases

**1. POST Service với STANDARD specialization**

```bash
POST http://localhost:8080/api/v1/services
{
  "serviceCode": "SV-TEST-001",
  "serviceName": "Test Service",
  "description": "For testing",
  "defaultDurationMinutes": 30,
  "defaultBufferMinutes": 10,
  "price": 100000,
  "specializationId": 8  # STANDARD
}
```

**Expected**: ✅ 201 CREATED

**2. GET Service không tồn tại**

```bash
GET http://localhost:8080/api/v1/services/9999
```

**Expected**: ❌ 400 BAD REQUEST với message "Service not found with ID: 9999"

**3. Appointment với Admin (no specialization)**

```bash
# Giả sử EMP001 (Admin) không có specialization
GET /api/v1/appointments/available-times?
  employeeCode=EMP001&serviceCodes=GEN_EXAM
```

**Expected**: ❌ 400 BAD REQUEST "Employee does not have required specializations"

**4. Appointment với Doctor có STANDARD**

```bash
GET /api/v1/appointments/available-times?
  employeeCode=EMP002&serviceCodes=GEN_EXAM
```

**Expected**: ✅ 200 OK với available time slots

## Migration Guide

### Bước 1: Drop database hiện tại

```sql
DROP DATABASE dental_clinic;
CREATE DATABASE dental_clinic;
```

### Bước 2: Chạy schema

```bash
psql -U postgres -d dental_clinic -f src/main/resources/db/schema.sql
```

### Bước 3: Load seed data mới

```bash
psql -U postgres -d dental_clinic -f src/main/resources/db/dental-clinic-seed-data_postgres_v2.sql
```

### Bước 4: Verify specializations

```sql
-- Kiểm tra 2 specialization mới
SELECT * FROM specializations WHERE specialization_id IN (8, 9);

-- Kiểm tra tất cả medical staff đều có STANDARD
SELECT e.employee_code, e.first_name, e.last_name,
       array_agg(s.specialization_id) as spec_ids,
       array_agg(s.specialization_name) as spec_names
FROM employees e
JOIN employee_specializations es ON e.employee_id = es.employee_id
JOIN specializations s ON es.specialization_id = s.specialization_id
GROUP BY e.employee_id;

-- Verify services có specialization
SELECT service_code, service_name, specialization_id
FROM services
ORDER BY specialization_id;
```

### Bước 5: Restart application

```bash
mvn spring-boot:run
```

## Summary

| Issue                                    | Solution                                | Status         |
| ---------------------------------------- | --------------------------------------- | -------------- |
| POST /api/v1/services 500 error          | Added @PostMapping endpoint             | ✅ Fixed       |
| No error handling for invalid service ID | Already handled by DentalServiceService | ✅ Working     |
| Filter medical vs admin staff            | Added STANDARD specialization (ID 8)    | ✅ Implemented |
| Handle interns without specialty         | Added INTERN specialization (ID 9)      | ✅ Implemented |
| Auto-assign STANDARD to medical staff    | Updated employee_specializations seed   | ✅ Done        |
| Services without specialization          | Changed NULL → STANDARD or specific ID  | ✅ Done        |

**Kết quả**: Hệ thống giờ có thể phân biệt rõ ràng nhân viên y tế vs hành chính mà KHÔNG cần thay đổi schema database!
