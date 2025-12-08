# API 5.15: Assign Doctor to Treatment Plan Item

## Tong Quan (Overview)

API nay cho phep gan hoac thay doi bac si thuc hien cho mot hang muc dieu tri (treatment plan item) trong giai doan (phase). Tinh nang nay ho tro cho viec to chuc ke hoach dieu tri va chuan bi truoc khi dat lich kham.

---

## Thong Tin API (API Information)

| Thuoc tinh              | Gia tri                                             |
| ----------------------- | --------------------------------------------------- |
| **HTTP Method**         | `PUT`                                               |
| **Endpoint**            | `/api/v1/patient-plan-items/{itemId}/assign-doctor` |
| **Content-Type**        | `application/json`                                  |
| **Authorization**       | Bearer Token (JWT)                                  |
| **Required Permission** | `ASSIGN_DOCTOR_TO_ITEM`                             |
| **Allowed Roles**       | ROLE_ADMIN, ROLE_MANAGER, ROLE_DENTIST              |

---

## Business Rules (Quy Tac Nghiep Vu)

### 1. Authorization Rules (Quyen Truy Cap)

**EMPLOYEE (ROLE_DENTIST)**:

- Chi duoc gan bac si cho cac hang muc trong ke hoach do chinh ho tao (`createdBy`)
- Dam bao RBAC: Employee chi thao tac tren ke hoach cua minh

**MANAGER (ROLE_MANAGER)**:

- Co the gan bac si cho bat ky ke hoach nao trong he thong
- Khong can kiem tra `createdBy`

**ADMIN (ROLE_ADMIN)**:

- Quyen tuyet doi, co the gan bac si cho moi ke hoach

**PATIENT**:

- Khong duoc phep thuc hien thao tac nay (read-only access)

### 2. Doctor Validation Rules

**Active Status**:

- Bac si phai dang hoat dong (`isActive = true`)
- Khong duoc gan bac si da nghi viec hoac bi vo hieu hoa

**Specialization Validation**:

- Neu dich vu yeu cau chuyen mon (`service.specialization != null`):
  - Bac si phai co chuyen mon tuong ung trong `employee_specializations`
  - He thong tu dong kiem tra `specialization_id` khop voi `service.specialization_id`
- Neu dich vu khong yeu cau chuyen mon:
  - Bat ky bac si nao dang hoat dong cung co the duoc gan

### 3. Item State Requirements

- Item phai ton tai trong he thong
- Item phai thuoc mot phase hop le
- Phase phai thuoc mot treatment plan hop le
- Khong co rang buoc ve trang thai cua item (co the gan bac si o bat ky trang thai nao)

### 4. Reassignment Rules

- Co the thay doi bac si nhieu lan, khong gioi han
- Moi lan gan se ghi de thong tin bac si truoc do
- He thong ghi log thay doi trong audit trail (future enhancement)

---

## Request Body

### Request Schema

```json
{
  "doctorCode": "EMP001",
  "notes": "Bac si An Khoa co kinh nghiem ve dieu tri phuc hinh"
}
```

### Field Descriptions

| Field        | Type     | Required | Validation    | Description                                       |
| ------------ | -------- | -------- | ------------- | ------------------------------------------------- |
| `doctorCode` | `string` | Yes      | @NotBlank     | Ma nhan vien cua bac si (phai la active employee) |
| `notes`      | `string` | No       | Max 500 chars | Ly do hoac ghi chu ve viec gan bac si             |

### Validation Rules

1. **doctorCode**:

   - Bat buoc phai co
   - Phai la ma nhan vien ton tai trong bang `employees`
   - Employee phai dang hoat dong (`isActive = true`)
   - Employee phai co chuyen mon khop voi dich vu (neu dich vu yeu cau chuyen mon)

2. **notes**:
   - Tuy chon
   - Toi da 500 ky tu
   - Dung de ghi lai ly do gan bac si (vi du: chuyen mon khop, lich trong, yeu cau cua benh nhan...)

---

## Response Body

### Success Response (200 OK)

```json
{
  "itemId": 1001,
  "sequenceNumber": 1,
  "itemName": "Lay cao rang va lam sach",
  "serviceId": 102,
  "price": 300000,
  "estimatedTimeMinutes": 45,
  "status": "PENDING",
  "completedAt": null,
  "notes": "Bac si An Khoa co kinh nghiem ve dieu tri phuc hinh",
  "phaseId": 201,
  "phaseName": "Phase 1: Kham va chuan bi",
  "phaseSequenceNumber": 1,
  "linkedAppointments": [],
  "updatedAt": "2025-01-15T10:30:00",
  "updatedBy": "DR_AN_KHOA"
}
```

### Response Field Descriptions

| Field                  | Type       | Description                                     |
| ---------------------- | ---------- | ----------------------------------------------- |
| `itemId`               | `number`   | ID cua item                                     |
| `sequenceNumber`       | `number`   | So thu tu trong phase (1, 2, 3...)              |
| `itemName`             | `string`   | Ten hang muc dieu tri                           |
| `serviceId`            | `number`   | ID dich vu tham chieu                           |
| `price`                | `number`   | Gia (snapshot khi tao plan)                     |
| `estimatedTimeMinutes` | `number`   | Thoi gian uoc tinh (phut)                       |
| `status`               | `string`   | Trang thai hien tai cua item                    |
| `completedAt`          | `datetime` | Thoi diem hoan thanh (null neu chua hoan thanh) |
| `notes`                | `string`   | Ghi chu tu request                              |
| `phaseId`              | `number`   | ID cua phase chua item nay                      |
| `phaseName`            | `string`   | Ten phase                                       |
| `phaseSequenceNumber`  | `number`   | So thu tu cua phase trong plan                  |
| `linkedAppointments`   | `array`    | Danh sach cac cuoc hen lien quan den item nay   |
| `updatedAt`            | `datetime` | Thoi diem cap nhat                              |
| `updatedBy`            | `string`   | Nguoi thuc hien thao tac                        |

---

## Error Responses

### 1. Item Not Found (404)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "ITEM_NOT_FOUND: Plan item not found with id: 9999",
  "path": "/api/v1/patient-plan-items/9999/assign-doctor"
}
```

**Nguyen nhan**:

- Item ID khong ton tai trong bang `patient_plan_items`

**Giai phap**:

- Kiem tra lai itemId tu API 5.1 hoac 5.2 (lay danh sach treatment plans)

### 2. Doctor Not Found (404)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "DOCTOR_NOT_FOUND: Active doctor not found with code: EMP999",
  "path": "/api/v1/patient-plan-items/1001/assign-doctor"
}
```

**Nguyen nhan**:

- doctorCode khong ton tai hoac bac si da nghi viec (`isActive = false`)

**Giai phap**:

- Kiem tra lai ma nhan vien tu API Employee List
- Dam bao bac si dang hoat dong

### 3. Invalid Specialization (400)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Doctor EMP001 does not have required specialization 'Nha chu' for service 'Dieu tri viem loi'",
  "path": "/api/v1/patient-plan-items/1001/assign-doctor"
}
```

**Nguyen nhan**:

- Dich vu yeu cau chuyen mon (vi du: Nha chu, Chinh nha...)
- Bac si khong co chuyen mon tuong ung trong bang `employee_specializations`

**Giai phap**:

- Chon bac si khac co chuyen mon phu hop
- Hoac cap nhat chuyen mon cho bac si (thao tac quan tri vien)

### 4. Access Denied - RBAC Violation (403)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied: EMPLOYEE can only modify plans they created",
  "path": "/api/v1/patient-plan-items/1001/assign-doctor"
}
```

**Nguyen nhan**:

- User co role EMPLOYEE (ROLE_DENTIST)
- Dang co gan bac si cho ke hoach khong phai do ho tao (`plan.createdBy != current employee`)

**Giai phap**:

- Employee chi duoc thao tac tren ke hoach cua minh
- Yeu cau Manager hoac Admin thuc hien thao tac

### 5. Missing Permission (403)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access is denied",
  "path": "/api/v1/patient-plan-items/1001/assign-doctor"
}
```

**Nguyen nhan**:

- User khong co permission `ASSIGN_DOCTOR_TO_ITEM`
- Hoac role PATIENT dang co truy cap (patients khong duoc phep modify)

**Giai phap**:

- Kiem tra role va permission cua user
- Dam bao user la ROLE_DENTIST, ROLE_MANAGER, hoac ROLE_ADMIN

### 6. Invalid Request Body (400)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "doctorCode: must not be blank",
  "path": "/api/v1/patient-plan-items/1001/assign-doctor"
}
```

**Nguyen nhan**:

- Request body khong hop le (doctorCode trong hoac null)

**Giai phap**:

- Dam bao request body co du truong bat buoc
- Validate theo schema truoc khi gui request

---

## Test Guide (Huong Dan Test)

### Prerequisites (Yeu Cau Truoc Khi Test)

1. **Database Setup**:

   - He thong da chay seed data (`dental-clinic-seed-data.sql`)
   - Co it nhat 1 treatment plan voi cac hang muc dieu tri

2. **Test Accounts**:

   - User: `EMP001` (ROLE_DENTIST), Password: `123456`
   - User: `EMP002` (ROLE_MANAGER), Password: `123456`

3. **Test Data**:
   - Treatment plan ID: Lay tu API 5.1 hoac 5.2
   - Item ID: Lay tu response cua treatment plan detail
   - Doctor codes: `EMP001`, `EMP002`, `EMP003` (active employees)

### Test Case 1: Assign Doctor Successfully

**Description**: Gan bac si thanh cong cho mot hang muc dieu tri

**Request**:

```bash
PUT /api/v1/patient-plan-items/1/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP002",
  "notes": "Bac si Nguyen Van B co kinh nghiem ve dieu tri nha chu"
}
```

**Expected Result**:

- Status Code: `200 OK`
- Response chua thong tin item da cap nhat
- Field `updatedAt` va `updatedBy` duoc cap nhat
- Kiem tra database: `patient_plan_items.assigned_doctor_id` = employee_id cua EMP002

**Validation**:

```sql
SELECT item_id, item_name, assigned_doctor_id, status
FROM patient_plan_items
WHERE item_id = 1;
```

### Test Case 2: Reassign Doctor (Change Existing Doctor)

**Description**: Thay doi bac si da gan truoc do

**Precondition**:

- Item 1 da duoc gan bac si EMP002 (tu test case 1)

**Request**:

```bash
PUT /api/v1/patient-plan-items/1/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP003",
  "notes": "Thay doi bac si do lich cua bac si Nguyen Van B khong phu hop"
}
```

**Expected Result**:

- Status Code: `200 OK`
- assigned_doctor_id duoc cap nhat thanh employee_id cua EMP003
- Ghi de thong tin bac si cu

**Validation**:

```sql
SELECT item_id, item_name, assigned_doctor_id, status
FROM patient_plan_items
WHERE item_id = 1;

-- Kiem tra employee_id cua EMP003
SELECT employee_id, employee_code, first_name, last_name
FROM employees
WHERE employee_code = 'EMP003';
```

### Test Case 3: Assign Without Notes (Optional Field)

**Description**: Gan bac si khong co ghi chu (notes la optional)

**Request**:

```bash
PUT /api/v1/patient-plan-items/2/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP001"
}
```

**Expected Result**:

- Status Code: `200 OK`
- Field `notes` trong response se la empty string hoac null
- Thao tac thanh cong du khong co notes

### Test Case 4: Doctor Not Found Error

**Description**: Test loi khi ma bac si khong ton tai

**Request**:

```bash
PUT /api/v1/patient-plan-items/1/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP999",
  "notes": "Test invalid doctor code"
}
```

**Expected Result**:

- Status Code: `404 Not Found`
- Error message: `DOCTOR_NOT_FOUND: Active doctor not found with code: EMP999`

### Test Case 5: Item Not Found Error

**Description**: Test loi khi item ID khong ton tai

**Request**:

```bash
PUT /api/v1/patient-plan-items/99999/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP001",
  "notes": "Test invalid item ID"
}
```

**Expected Result**:

- Status Code: `404 Not Found`
- Error message: `ITEM_NOT_FOUND: Plan item not found with id: 99999`

### Test Case 6: Specialization Validation

**Description**: Test logic kiem tra chuyen mon (neu dich vu yeu cau)

**Precondition**:

- Item phai lien ket voi dich vu co yeu cau chuyen mon
- Bac si khong co chuyen mon tuong ung

**Steps**:

1. Kiem tra cac dich vu co yeu cau chuyen mon:

```sql
SELECT s.service_id, s.service_name, sp.specialization_name
FROM services s
JOIN specializations sp ON s.specialization_id = sp.specialization_id
WHERE s.specialization_id IS NOT NULL;
```

2. Kiem tra chuyen mon cua bac si:

```sql
SELECT e.employee_code, e.first_name, e.last_name, sp.specialization_name
FROM employees e
LEFT JOIN employee_specializations es ON e.employee_id = es.employee_id
LEFT JOIN specializations sp ON es.specialization_id = sp.specialization_id
WHERE e.employee_code = 'EMP001';
```

3. Gan bac si khong co chuyen mon cho dich vu yeu cau chuyen mon khac:

```bash
PUT /api/v1/patient-plan-items/{itemId}/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP001",
  "notes": "Test specialization validation"
}
```

**Expected Result**:

- Status Code: `400 Bad Request`
- Error message chi ro chuyen mon bi thieu

### Test Case 7: RBAC - Employee Can Only Modify Own Plans

**Description**: Test RBAC: EMPLOYEE chi duoc gan bac si cho ke hoach do ho tao

**Precondition**:

- Treatment plan A duoc tao boi EMP001 (createdBy = EMP001's account_id)
- Treatment plan B duoc tao boi EMP002

**Request** (EMP001 co gan bac si cho plan cua EMP002):

```bash
PUT /api/v1/patient-plan-items/{item_in_plan_B}/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "doctorCode": "EMP003"
}
```

**Expected Result**:

- Status Code: `403 Forbidden`
- Error message: `Access Denied: EMPLOYEE can only modify plans they created`

### Test Case 8: RBAC - Manager Can Modify All Plans

**Description**: Test RBAC: MANAGER co the gan bac si cho bat ky ke hoach nao

**Request** (EMP002 la MANAGER gan bac si cho plan cua EMP001):

```bash
PUT /api/v1/patient-plan-items/{item_in_plan_A}/assign-doctor
Authorization: Bearer <token_EMP002>
Content-Type: application/json

{
  "doctorCode": "EMP003",
  "notes": "Manager assigns doctor for employee's plan"
}
```

**Expected Result**:

- Status Code: `200 OK`
- Thao tac thanh cong, khong bi chan boi RBAC

### Test Case 9: Missing Required Field

**Description**: Test validation khi thieu truong bat buoc

**Request**:

```bash
PUT /api/v1/patient-plan-items/1/assign-doctor
Authorization: Bearer <token_EMP001>
Content-Type: application/json

{
  "notes": "Missing doctorCode field"
}
```

**Expected Result**:

- Status Code: `400 Bad Request`
- Error message: `doctorCode: must not be blank`

### Test Case 10: Assign Doctor at Different Item States

**Description**: Test gan bac si o cac trang thai khac nhau cua item

**Scenarios**:

- Item status = PENDING: Should work
- Item status = READY_FOR_BOOKING: Should work
- Item status = SCHEDULED: Should work
- Item status = COMPLETED: Should work (for record-keeping)
- Item status = SKIPPED: Should work (in case plan is revised)

**Expected Result**:

- Tat ca scenarios deu thanh cong (khong co rang buoc ve trang thai item)

---

## Database Schema Reference

### Affected Tables

**patient_plan_items**:

```sql
-- V32: Added assigned_doctor_id column
ALTER TABLE patient_plan_items
ADD COLUMN assigned_doctor_id INTEGER REFERENCES employees(employee_id);

-- Purpose: Store reference to assigned doctor for each treatment plan item
-- NULL = no doctor assigned yet (can be assigned later during phase organization)
```

**employees**:

```sql
-- Primary key: employee_id
-- Unique constraint: employee_code
-- Fields: first_name, last_name, is_active, etc.
```

**employee_specializations** (junction table):

```sql
-- employee_id (FK to employees)
-- specialization_id (FK to specializations)
-- Purpose: Many-to-many relationship between employees and specializations
```

**services**:

```sql
-- service_id (PK)
-- specialization_id (FK to specializations, nullable)
-- Purpose: Some services require specific specialization, some don't
```

### Query Examples

**Get item with assigned doctor**:

```sql
SELECT
  i.item_id,
  i.item_name,
  i.status,
  e.employee_code,
  e.first_name,
  e.last_name,
  s.service_name,
  sp.specialization_name
FROM patient_plan_items i
LEFT JOIN employees e ON i.assigned_doctor_id = e.employee_id
LEFT JOIN services s ON i.service_id = s.service_id
LEFT JOIN specializations sp ON s.specialization_id = sp.specialization_id
WHERE i.item_id = 1;
```

**Get all items assigned to a doctor**:

```sql
SELECT
  i.item_id,
  i.item_name,
  i.status,
  p.patient_name,
  ph.phase_name
FROM patient_plan_items i
JOIN patient_plan_phases ph ON i.phase_id = ph.patient_phase_id
JOIN patient_treatment_plans pt ON ph.plan_id = pt.plan_id
JOIN patients p ON pt.patient_id = p.patient_id
WHERE i.assigned_doctor_id = (SELECT employee_id FROM employees WHERE employee_code = 'EMP001')
ORDER BY i.item_id;
```

---

## Integration Notes

### Use Cases

**1. Phase Organization Workflow**:

```
1. Manager/Dentist creates treatment plan (API 5.3 or 5.4)
2. System auto-generates phases and items
3. Staff uses API 5.15 to assign doctors to each item
4. Staff creates appointments with assigned doctors (API 3.1)
```

**2. Workload Distribution**:

```
- Manager reviews upcoming treatment plans
- Assigns doctors based on:
  * Specialization requirements
  * Doctor availability
  * Workload balance
- Uses API 5.15 to update assignments
```

**3. Doctor Replacement**:

```
- Doctor A is on leave
- Manager reassigns Doctor A's items to Doctor B
- Uses API 5.15 with doctorCode = "EMP_B"
- Preserves treatment plan structure
```

### Frontend UI Recommendations

**Assign Doctor Dialog**:

```
[ Treatment Plan Item: Lay cao rang ]
[ Current Status: PENDING ]

Select Doctor:
[v] EMP001 - Bac si An Khoa
    Specializations: Nha chu, Dieu tri tuy
    Available: Yes

[ ] EMP002 - Bac si Nguyen Van B
    Specializations: Chinh nha
    Available: No (On leave)

Notes:
[_____________________________________________]
[ Optional: Reason for assignment            ]

[Cancel] [Assign Doctor]
```

**Validation Rules in UI**:

- Show only active doctors
- Highlight doctors with matching specialization
- Disable doctors without required specialization
- Show warning if changing existing assignment

---

## Related APIs

| API         | Description               | Relationship                               |
| ----------- | ------------------------- | ------------------------------------------ |
| **API 5.1** | Get All Treatment Plans   | Get list of plans to find items            |
| **API 5.2** | Get Treatment Plan Detail | Get full plan structure with items         |
| **API 5.6** | Update Item Status        | Complementary: Status vs Doctor assignment |
| **API 3.1** | Create Appointment        | Uses assigned doctor when scheduling       |

---

## Version History

| Version | Date       | Changes                                     | Author |
| ------- | ---------- | ------------------------------------------- | ------ |
| V32     | 2025-01-15 | Initial implementation of assign doctor API | System |

---

## Additional Notes

1. **Audit Trail**: Future enhancement: Log all doctor assignments in `plan_audit_logs` table
2. **Notifications**: Future enhancement: Notify assigned doctor about new assignments
3. **Workload Metrics**: Future enhancement: Show doctor workload before assignment
4. **Bulk Assignment**: Future enhancement: API to assign doctor to multiple items at once
5. **Doctor Availability**: Future enhancement: Check doctor schedule before assignment

---

## Support Contact

Neu gap van de khi su dung API nay, vui long lien he:

- Developer Team: [Support Email]
- Documentation: Check related APIs above
- Database Schema: See `docs/architecture/SCHEMA_V32.md`
