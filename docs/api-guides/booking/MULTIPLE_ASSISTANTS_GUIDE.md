# Multiple Assistants Support in Appointment Booking

## Tong Quan (Overview)

He thong da ho tro tinh nang them nhieu phu ta (assistants) cho mot cuoc hen kham tu phien ban V18. Tinh nang nay co san trong API 3.1 (Create Appointment) va khong can thay doi code them.

---

## Thong Tin Chung (General Information)

| Thuoc tinh          | Gia tri                                         |
| ------------------- | ----------------------------------------------- |
| **Feature Status**  | AVAILABLE (da co san tu V18)                    |
| **API Endpoint**    | POST `/api/v1/appointments`                     |
| **Affected Tables** | `appointments`, `appointment_participants`      |
| **Data Type**       | `participantCodes` la `List<String>` trong Java |
| **Database Type**   | Many-to-Many relationship via junction table    |

---

## Database Schema

### Tables Involved

**1. appointments**:

```sql
CREATE TABLE appointments (
  appointment_id SERIAL PRIMARY KEY,
  code VARCHAR(50) UNIQUE NOT NULL,
  patient_id INTEGER REFERENCES patients(patient_id),
  room_id INTEGER REFERENCES rooms(room_id),
  scheduled_date TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(50),
  ...
);
```

**2. appointment_participants** (junction table):

```sql
CREATE TABLE appointment_participants (
  appointment_id INTEGER REFERENCES appointments(appointment_id),
  participant_code VARCHAR(20) NOT NULL,
  PRIMARY KEY (appointment_id, participant_code)
);

-- Purpose: Store multiple assistants for one appointment
-- Example data:
--   (APT-001, 'EMP001')  -- Bac si chinh
--   (APT-001, 'EMP002')  -- Phu ta 1
--   (APT-001, 'EMP003')  -- Phu ta 2
```

**3. employees**:

```sql
-- participant_code references employees.employee_code
-- System validates all codes exist and employees are active
```

---

## API Usage (Huong Dan Su Dung API)

### Endpoint Information

**Create Appointment with Multiple Assistants**:

```
POST /api/v1/appointments
Content-Type: application/json
Authorization: Bearer <token>
```

### Request Body Example

**Single Assistant (Old Style - Still Works)**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-20T10:00:00",
  "participantCodes": ["EMP001"],
  "planItemIds": [],
  "notes": "Kham dinh ky"
}
```

**Multiple Assistants (V18+ Supported)**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-20T10:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP003", "EMP004"],
  "planItemIds": [],
  "notes": "Ca phau thuat phuc tap can nhieu phu ta"
}
```

### Field Description

| Field              | Type       | Required | Description                                  |
| ------------------ | ---------- | -------- | -------------------------------------------- |
| `participantCodes` | `string[]` | Yes      | Danh sach ma nhan vien (bac si + cac phu ta) |
|                    |            |          | - Element 1: Thuong la bac si chinh          |
|                    |            |          | - Elements 2+: Cac phu ta                    |
|                    |            |          | - Toi thieu 1 nguoi, khong gioi han toi da   |

---

## Business Rules (Quy Tac Nghiep Vu)

### 1. Participant Validation

**Active Status Check**:

- Tat ca employee codes trong `participantCodes` phai ton tai trong bang `employees`
- Tat ca employees phai dang hoat dong (`isActive = true`)
- He thong tu dong reject neu co employee khong hop le

**Duplicate Check**:

- He thong tu dong loai bo cac employee codes trung lap trong request
- Vi du: `["EMP001", "EMP001", "EMP002"]` → System stores `["EMP001", "EMP002"]`

### 2. Assignment Logic

**Database Storage**:

- Moi participant code duoc luu thanh 1 row trong bang `appointment_participants`
- Appointment co 3 participants → 3 rows trong junction table

**No Ordering Guarantee**:

- He thong khong bao dam thu tu cua participants trong database
- Frontend nen hien thi theo thu tu alphabet hoac employee_id

### 3. Appointment Lifecycle

**Status Changes**:

- Khi appointment status thay doi, participants khong bi anh huong
- Participants co the duoc cap nhat bang API rieng (future enhancement)

**Deletion**:

- Khi xoa appointment, tat ca rows tuong ung trong `appointment_participants` bi xoa (CASCADE)

---

## Response Structure

### Success Response (201 Created)

```json
{
  "appointmentId": 501,
  "code": "APT-20250120-001",
  "patient": {
    "patientId": 1,
    "fullName": "Nguyen Van A",
    "phoneNumber": "0901234567"
  },
  "room": {
    "roomId": 101,
    "roomName": "Phong kham 1"
  },
  "scheduledDate": "2025-01-20T10:00:00",
  "status": "SCHEDULED",
  "participants": [
    {
      "employeeId": 1,
      "employeeCode": "EMP001",
      "fullName": "Bac si An Khoa",
      "specializations": ["Nha chu"]
    },
    {
      "employeeId": 2,
      "employeeCode": "EMP002",
      "fullName": "Y ta Nguyen Van B",
      "specializations": []
    },
    {
      "employeeId": 3,
      "employeeCode": "EMP003",
      "fullName": "Y ta Tran Thi C",
      "specializations": []
    },
    {
      "employeeId": 4,
      "employeeCode": "EMP004",
      "fullName": "Ky thuat vien Le Van D",
      "specializations": []
    }
  ],
  "linkedItems": [],
  "notes": "Ca phau thuat phuc tap can nhieu phu ta",
  "createdAt": "2025-01-15T14:00:00",
  "createdBy": "EMP001"
}
```

### Response Field Explanation

**participants array**:

- Chua thong tin day du cua tat ca nhan vien tham gia
- Moi element gom:
  - `employeeId`: ID trong database
  - `employeeCode`: Ma nhan vien
  - `fullName`: Ho ten day du
  - `specializations`: Danh sach chuyen mon (neu co)

---

## Error Scenarios

### 1. Invalid Participant Code (404)

**Request**:

```json
{
  "participantCodes": ["EMP001", "EMP999"],
  ...
}
```

**Response**:

```json
{
  "timestamp": "2025-01-15T14:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found with code: EMP999",
  "path": "/api/v1/appointments"
}
```

**Nguyen nhan**:

- Employee code `EMP999` khong ton tai hoac da bi vo hieu hoa

**Giai phap**:

- Kiem tra lai danh sach employee codes tu API Employee List
- Dam bao tat ca employees dang hoat dong

### 2. Empty Participants Array (400)

**Request**:

```json
{
  "participantCodes": [],
  ...
}
```

**Response**:

```json
{
  "timestamp": "2025-01-15T14:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "participantCodes: must not be empty",
  "path": "/api/v1/appointments"
}
```

**Nguyen nhan**:

- Moi appointment phai co it nhat 1 participant (bac si chinh)

**Giai phap**:

- Them it nhat 1 employee code vao `participantCodes`

### 3. Inactive Employee (400)

**Request**:

```json
{
  "participantCodes": ["EMP001", "EMP005"],
  ...
}
```

**Response**:

```json
{
  "timestamp": "2025-01-15T14:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Employee EMP005 is inactive and cannot be assigned",
  "path": "/api/v1/appointments"
}
```

**Nguyen nhan**:

- Employee `EMP005` co `isActive = false` (da nghi viec hoac bi tam ngung)

**Giai phap**:

- Chon employee khac dang hoat dong
- Hoac kich hoat lai employee (quan tri vien)

---

## Test Guide (Huong Dan Test)

### Test Case 1: Create Appointment with 1 Assistant

**Description**: Tao cuoc hen voi 1 phu ta (use case co ban)

**Request**:

```bash
POST /api/v1/appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-20T10:00:00",
  "participantCodes": ["EMP001"],
  "planItemIds": [],
  "notes": "Kham dinh ky"
}
```

**Expected Result**:

- Status Code: `201 Created`
- Response chua 1 participant
- Database: 1 row trong `appointment_participants`

**Validation**:

```sql
SELECT ap.appointment_id, ap.participant_code, e.first_name, e.last_name
FROM appointment_participants ap
JOIN employees e ON ap.participant_code = e.employee_code
WHERE ap.appointment_id = <returned_appointment_id>;

-- Expected: 1 row
```

### Test Case 2: Create Appointment with 3 Assistants

**Description**: Tao cuoc hen voi 3 phu ta (use case nhieu phu ta)

**Request**:

```bash
POST /api/v1/appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-20T14:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP003"],
  "planItemIds": [],
  "notes": "Ca phau thuat can 3 phu ta"
}
```

**Expected Result**:

- Status Code: `201 Created`
- Response chua 3 participants
- Database: 3 rows trong `appointment_participants`

**Validation**:

```sql
SELECT ap.appointment_id, ap.participant_code, e.first_name, e.last_name
FROM appointment_participants ap
JOIN employees e ON ap.participant_code = e.employee_code
WHERE ap.appointment_id = <returned_appointment_id>
ORDER BY ap.participant_code;

-- Expected: 3 rows (EMP001, EMP002, EMP003)
```

### Test Case 3: Create Appointment with 5 Assistants

**Description**: Test so luong lon phu ta (stress test)

**Request**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-21T09:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP003", "EMP004", "EMP005"],
  "planItemIds": [],
  "notes": "Ca dac biet can nhieu nhan vien"
}
```

**Expected Result**:

- Status Code: `201 Created`
- Response chua 5 participants
- He thong khong gioi han so luong participants

### Test Case 4: Create Appointment from Treatment Plan with Multiple Assistants

**Description**: Ket hop treatment plan item voi nhieu phu ta

**Request**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-22T10:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP003"],
  "planItemIds": [1, 2],
  "notes": "Thuc hien 2 hang muc tu treatment plan voi 3 nhan vien"
}
```

**Expected Result**:

- Status Code: `201 Created`
- 3 participants duoc gan cho appointment
- 2 plan items duoc link voi appointment (trang thai READY_FOR_BOOKING → SCHEDULED)

**Validation**:

```sql
-- Check participants
SELECT COUNT(*) FROM appointment_participants WHERE appointment_id = <id>;
-- Expected: 3

-- Check linked items
SELECT COUNT(*) FROM appointment_plan_items WHERE appointment_id = <id>;
-- Expected: 2
```

### Test Case 5: Invalid Employee Code Error

**Description**: Test loi khi co employee code khong hop le

**Request**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-23T10:00:00",
  "participantCodes": ["EMP001", "EMP999"],
  "planItemIds": [],
  "notes": "Test invalid employee"
}
```

**Expected Result**:

- Status Code: `404 Not Found`
- Error message: `Employee not found with code: EMP999`
- Appointment khong duoc tao

### Test Case 6: Duplicate Participant Codes

**Description**: Test xu ly khi co employee codes bi trung

**Request**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-24T10:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP001"],
  "planItemIds": [],
  "notes": "Test duplicate codes"
}
```

**Expected Result**:

- Status Code: `201 Created` (he thong tu dong deduplicate)
- Response chi chua 2 participants (EMP001 va EMP002)
- Database: 2 rows trong `appointment_participants`

### Test Case 7: Empty Participants Array Error

**Description**: Test loi khi khong co participant nao

**Request**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-25T10:00:00",
  "participantCodes": [],
  "planItemIds": [],
  "notes": "Test empty array"
}
```

**Expected Result**:

- Status Code: `400 Bad Request`
- Error message: `participantCodes: must not be empty`

### Test Case 8: Get Appointment Detail with Multiple Participants

**Description**: Test lay thong tin appointment da co nhieu participants

**Precondition**:

- Da tao appointment voi 3 participants (tu test case 2)

**Request**:

```bash
GET /api/v1/appointments/<appointment_id>
Authorization: Bearer <token>
```

**Expected Result**:

- Status Code: `200 OK`
- Response chua `participants` array voi 3 elements
- Moi participant co day du thong tin (employeeId, employeeCode, fullName, specializations)

---

## Database Query Examples

### Query 1: Get All Participants for an Appointment

```sql
SELECT
  a.code AS appointment_code,
  e.employee_code,
  e.first_name,
  e.last_name,
  COALESCE(
    STRING_AGG(sp.specialization_name, ', '),
    'No specialization'
  ) AS specializations
FROM appointments a
JOIN appointment_participants ap ON a.appointment_id = ap.appointment_id
JOIN employees e ON ap.participant_code = e.employee_code
LEFT JOIN employee_specializations es ON e.employee_id = es.employee_id
LEFT JOIN specializations sp ON es.specialization_id = sp.specialization_id
WHERE a.code = 'APT-20250120-001'
GROUP BY a.code, e.employee_code, e.first_name, e.last_name
ORDER BY e.employee_code;
```

### Query 2: Get All Appointments for an Employee (as Participant)

```sql
SELECT
  a.code AS appointment_code,
  a.scheduled_date,
  a.status,
  p.full_name AS patient_name,
  r.room_name,
  (SELECT COUNT(*) FROM appointment_participants ap2
   WHERE ap2.appointment_id = a.appointment_id) AS total_participants
FROM appointments a
JOIN appointment_participants ap ON a.appointment_id = ap.appointment_id
JOIN patients p ON a.patient_id = p.patient_id
JOIN rooms r ON a.room_id = r.room_id
WHERE ap.participant_code = 'EMP001'
  AND a.scheduled_date >= CURRENT_DATE
ORDER BY a.scheduled_date;
```

### Query 3: Count Appointments by Number of Participants

```sql
SELECT
  participant_count,
  COUNT(*) AS appointment_count
FROM (
  SELECT
    a.appointment_id,
    COUNT(ap.participant_code) AS participant_count
  FROM appointments a
  LEFT JOIN appointment_participants ap ON a.appointment_id = ap.appointment_id
  GROUP BY a.appointment_id
) subquery
GROUP BY participant_count
ORDER BY participant_count;

-- Example output:
-- participant_count | appointment_count
-- 1                 | 150
-- 2                 | 75
-- 3                 | 30
-- 4                 | 10
-- 5                 | 2
```

---

## Integration with Other Features

### 1. Treatment Plan Integration

**Scenario**: Tao appointment tu treatment plan items voi nhieu phu ta

**Workflow**:

1. User chon cac items tu treatment plan (API 5.2)
2. System hien thi form dat lich
3. User chon bac si chinh + cac phu ta (multi-select)
4. System call API 3.1 voi `planItemIds` + `participantCodes`
5. System tu dong link items voi appointment va cap nhat trang thai

**Example**:

```json
{
  "patientId": 1,
  "roomId": 101,
  "scheduledDate": "2025-01-26T10:00:00",
  "participantCodes": ["EMP001", "EMP002", "EMP003"],
  "planItemIds": [5, 6, 7],
  "notes": "Thuc hien 3 items: Lay cao rang, Lam sach, Va tram rang"
}
```

### 2. Doctor Assignment Integration

**Scenario**: Su dung bac si da gan trong treatment plan item

**Workflow**:

1. Manager gan bac si cho item bang API 5.15
2. Khi tao appointment tu item do, he thong tu dong dien bac si da gan
3. User co the them them phu ta vao `participantCodes`

**Recommendation**:

- Frontend nen hien thi bac si da gan (tu `item.assignedDoctor`) lam mac dinh
- User co the thay doi hoac them phu ta

---

## Frontend UI Recommendations

### Multi-Select Participants Component

```
[ Create Appointment ]

Patient: Nguyen Van A
Date: 2025-01-20 10:00
Room: Phong kham 1

Select Participants:
[x] EMP001 - Bac si An Khoa (Nha chu)          [Primary Doctor]
[x] EMP002 - Y ta Nguyen Van B                  [Assistant]
[x] EMP003 - Y ta Tran Thi C                    [Assistant]
[ ] EMP004 - Ky thuat vien Le Van D             [Available]
[ ] EMP005 - Bac si Pham Thi E (Chinh nha)      [Available]

Selected: 3 participants

Treatment Plan Items (optional):
[ ] Item 1: Lay cao rang
[ ] Item 2: Lam sach rang

Notes:
[_______________________________________________]

[Cancel] [Create Appointment]
```

**UI Features**:

- Checkbox list for multi-select
- Show specializations in parentheses
- Highlight primary doctor (first selected)
- Show count of selected participants
- Allow drag-and-drop to reorder (optional)

---

## Performance Considerations

### Database Indexing

```sql
-- Index on junction table for faster lookups
CREATE INDEX idx_appointment_participants_code
ON appointment_participants(participant_code);

CREATE INDEX idx_appointment_participants_appointment
ON appointment_participants(appointment_id);

-- Composite index for common queries
CREATE INDEX idx_appointments_date_status
ON appointments(scheduled_date, status);
```

### Query Optimization

- Use JOIN FETCH in JPA to avoid N+1 problem
- Fetch participants in single query when loading appointments
- Example JPA query:

```java
@Query("SELECT a FROM Appointment a " +
       "LEFT JOIN FETCH a.participants p " +
       "WHERE a.appointmentId = :id")
Optional<Appointment> findByIdWithParticipants(@Param("id") Integer id);
```

---

## API Change History

| Version | Date       | Changes                                       |
| ------- | ---------- | --------------------------------------------- |
| V18     | 2024-12-01 | Added support for multiple participants       |
|         |            | Changed participantCodes from String to List  |
|         |            | Added appointment_participants junction table |
| V1-V17  | Before     | Only supported single participant (1 doctor)  |

---

## Frequently Asked Questions (FAQ)

**Q1: Co gioi han so luong participants khong?**
A1: Khong co gioi han cung. He thong ho tro so luong bat ky (tested up to 10 participants).

**Q2: Co the cap nhat danh sach participants sau khi tao appointment khong?**
A2: Hien tai chua co API cap nhat participants. Future enhancement: PUT `/api/v1/appointments/{id}/participants`.

**Q3: Co the xoa mot participant khoi appointment khong?**
A3: Hien tai phai xoa va tao lai appointment. Future enhancement se co API cap nhat participants.

**Q4: Neu khong truyen participantCodes thi sao?**
A4: Request bi reject voi loi `participantCodes: must not be empty`. Bat buoc phai co it nhat 1 participant.

**Q5: Co the dung patient_id lam participant khong?**
A5: Khong. Participants phai la employees (employee_code). Patients khong the tham gia lam phu ta.

**Q6: Lam sao de phan biet bac si chinh va phu ta?**
A6: Hien tai khong co phan biet ro rang trong database. Theo convention, phan tu dau tien trong array la bac si chinh. Future enhancement: Them truong `role` (PRIMARY_DOCTOR, ASSISTANT, etc.)

---

## Related Documentation

- **API 3.1**: Create Appointment (chi tiet day du)
- **API 5.15**: Assign Doctor to Treatment Plan Item
- **Database Schema V18**: Changelog for appointment_participants table

---

## Support

Neu gap van de khi su dung tinh nang nay:

1. Kiem tra seed data: Dam bao co du employees de test
2. Kiem tra employee status: Tat ca employees phai `isActive = true`
3. Xem logs: Check backend logs de xac dinh nguyen nhan loi
4. Test don gian truoc: Thu voi 1 participant truoc khi test nhieu
