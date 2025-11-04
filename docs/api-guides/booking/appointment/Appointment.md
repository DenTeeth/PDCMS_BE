# BE-403 Appointment Management API

Base URL: /api/v1/appointments
Auth: Bearer Token
Permissions: CREATE_APPOINTMENT, VIEW_APPOINTMENT_ALL, VIEW_APPOINTMENT_OWN

## 📋 API SUMMARY

| Endpoint | Method | Permission | Description |
|----------|--------|------------|-------------|
| `/available-times` | GET | CREATE_APPOINTMENT | Tìm slot trống cho lịch hẹn |
| `/` | POST | CREATE_APPOINTMENT | Tạo lịch hẹn mới |
| `/` | GET | VIEW_APPOINTMENT_ALL hoặc VIEW_APPOINTMENT_OWN | Dashboard - Danh sách lịch hẹn |

## ⚠️ IMPLEMENTATION STATUS

| Feature | Status | Notes |
|---------|--------|-------|
| ✅ Permission-based RBAC | DONE | Check "VIEW_APPOINTMENT_ALL" in authorities |
| ✅ Search by Patient Name | DONE | JOIN patients, LIKE search |
| ✅ Search by Patient Phone | DONE | LIKE phone search |
| ✅ Filter by Service | DONE | JOIN appointment_services |
| ✅ OBSERVER Role Security | DONE | Only see appointments they participate in |
| ✅ DTO Mapping (Patient, Doctor, Room) | DONE | Basic version with N+1 warning |
| ✅ DatePreset Enum | DONE | TODAY, THIS_WEEK, NEXT_7_DAYS, THIS_MONTH |
| ✅ Computed Fields | DONE | computedStatus, minutesLate in response |
| ⚠️ Patient RBAC Mapping | TODO | Need Patient.account relationship |
| ⚠️ N+1 Query Optimization | TODO | Need batch loading |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
GET AVAILABLE TIMES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Endpoint:
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP001&serviceCodes=GEN_EXAM

Query Params:

- date (String Required) YYYY-MM-DD
- employeeCode (String Required) Mã bác sĩ
- serviceCodes (Array Required) Repeat: serviceCodes=A&serviceCodes=B
- participantCodes (Array Optional) Mã phụ tá

Response 200:

```json
{
  "totalDurationNeeded": 40,
  "availableSlots": [
    {
      "startTime": "2025-11-15T08:00:00",
      "availableCompatibleRoomCodes": ["P-01", "P-02"]
    }
  ]
}
```

Errors:

```json
{"message":"EMPLOYEE_NOT_QUALIFIED"}
{"message":"Doctor has no shifts on 2025-12-25"}
{"message":"Employee not found"}
```

Test Cases:

✅ 1 Service
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP001&serviceCodes=GEN_EXAM

✅ Multiple Services
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP002&serviceCodes=GEN_EXAM&serviceCodes=SCALING_L1

✅ With Participant
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP001&serviceCodes=GEN_EXAM&participantCodes=EMP007

✅ Part-time Dentist (Ca Sáng)
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP003&serviceCodes=EXTRACT_MILK

✅ Part-time Dentist (Ca Chiều)
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP004&serviceCodes=EXTRACT_NORM

❌ Not Qualified (EMP001 không có Nội nha)
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=EMP001&serviceCodes=FILLING_COMP

❌ No Shifts (Chủ nhật không làm việc)
GET /api/v1/appointments/available-times?date=2025-11-16&employeeCode=EMP001&serviceCodes=GEN_EXAM

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
POST CREATE APPOINTMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Endpoint:
POST /api/v1/appointments

Request Body:

```json
{
  "patientCode": "BN-1001",
  "employeeCode": "EMP001",
  "roomCode": "P-01",
  "serviceCodes": ["GEN_EXAM"],
  "appointmentStartTime": "2025-11-15T10:00:00",
  "participantCodes": ["EMP007"],
  "notes": "Khám tổng quát"
}
```

Request Fields:

- patientCode (String Required)
- employeeCode (String Required)
- roomCode (String Required)
- serviceCodes (Array Required)
- appointmentStartTime (String Required)
- participantCodes (Array Optional)
- notes (String Optional)

Response 201:

```json
{
  "appointmentCode": "APT-20251115-001",
  "status": "SCHEDULED",
  "appointmentStartTime": "2025-11-15T10:00:00",
  "appointmentEndTime": "2025-11-15T10:40:00",
  "expectedDurationMinutes": 40,
  "patient": { "patientCode": "BN-1001", "fullName": "Đoàn Thanh Phong" },
  "doctor": { "employeeCode": "EMP001", "fullName": "Lê Anh Khoa" },
  "room": { "roomCode": "P-01", "roomName": "Phòng thường 1" },
  "services": [{ "serviceCode": "GEN_EXAM", "serviceName": "Khám tổng quát & Tư vấn" }],
  "participants": [
    { "employeeCode": "EMP007", "fullName": "Đoàn Nguyễn Khôi Nguyên", "role": "ASSISTANT" }
  ]
}
```

Errors:

```json
{"message":"Patient code is required"}
{"message":"DOCTOR_NOT_AVAILABLE"}
{"message":"Patient not found"}
```

Test Cases:

✅ Valid
```json
{
  "patientCode": "BN-1001",
  "employeeCode": "EMP001",
  "roomCode": "P-01",
  "serviceCodes": ["GEN_EXAM"],
  "appointmentStartTime": "2025-11-15T10:00:00"
}
```

✅ Multiple Services + Participant
```json
{
  "patientCode": "BN-1002",
  "employeeCode": "EMP002",
  "roomCode": "P-02",
  "serviceCodes": ["GEN_EXAM", "SCALING_L1"],
  "appointmentStartTime": "2025-11-15T09:00:00",
  "participantCodes": ["EMP007"],
  "notes": "Khám và cạo vôi"
}
```

✅ Part-time Dentist (Chiều)
```json
{
  "patientCode": "BN-1003",
  "employeeCode": "EMP004",
  "roomCode": "P-01",
  "serviceCodes": ["EXTRACT_NORM"],
  "appointmentStartTime": "2025-11-15T14:00:00"
}
```

❌ Double Booking - Tạo 2 lần → 400 DOCTOR_NOT_AVAILABLE

❌ Wrong Shift Time - Ca Sáng (8-12h) nhưng book 14:00 → 400 DOCTOR_NOT_AVAILABLE

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
GET APPOINTMENT LIST (DASHBOARD)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Endpoint:
GET /api/v1/appointments

Authorization (PERMISSION-BASED, NOT ROLE-BASED):
- VIEW_APPOINTMENT_ALL: Lễ tân/Quản lý - Xem tất cả, dùng filters tự do
- VIEW_APPOINTMENT_OWN: Bác sĩ/Y tá/OBSERVER/Bệnh nhân - Filters bị GHI ĐÈ

⚠️ CRITICAL: Logic kiểm tra PERMISSION_ID, KHÔNG kiểm tra role_id

Query Params (All Optional):

- page (Number) Default: 0
- size (Number) Default: 10
- sortBy (String) Default: "appointmentStartTime"
- sortDirection (String) Default: "ASC" (ASC|DESC)
- datePreset (String) ✅ NEW - Quick date filter: TODAY | THIS_WEEK | NEXT_7_DAYS | THIS_MONTH
- dateFrom (String) YYYY-MM-DD - Từ ngày (inclusive)
- dateTo (String) YYYY-MM-DD - Đến ngày (inclusive)
- today (Boolean) DEPRECATED - Dùng datePreset=TODAY thay thế
- status (Array) Repeat: status=SCHEDULED&status=CHECKED_IN
- patientCode (String) Mã bệnh nhân (VIEW_ALL only)
- patientName (String) ✅ NEW - Search tên bệnh nhân LIKE (VIEW_ALL only)
- patientPhone (String) ✅ NEW - Search SĐT bệnh nhân LIKE (VIEW_ALL only)
- employeeCode (String) Mã bác sĩ chính (VIEW_ALL only)
- roomCode (String) Mã phòng
- serviceCode (String) ✅ NEW - Mã dịch vụ (JOIN appointment_services)

RBAC Logic (Permission-based):

1. VIEW_APPOINTMENT_ALL (Lễ tân/Quản lý):
   → Kiểm tra: auth.authorities contains "VIEW_APPOINTMENT_ALL"
   → Xem TẤT CẢ appointments
   → Filters hoạt động bình thường
   → ✅ Có thể search by patient name/phone

2. VIEW_APPOINTMENT_OWN + Employee (Bác sĩ/Y tá/OBSERVER):
   → Kiểm tra: auth.authorities contains "VIEW_APPOINTMENT_OWN"
   → OVERRIDE: WHERE (appointments.employee_id = [my_employee_id] 
                   OR EXISTS (participant where employee_id = [my_employee_id]))
   → PHỚT LỜI employeeCode từ client
   → ⚠️ OBSERVER (Thực tập sinh):
      • Có quyền VIEW_APPOINTMENT_OWN
      • Thấy appointments MÀ HỌ THAM GIA (role = OBSERVER trong participants)
      • KHÔNG thấy toàn bộ appointments (security)
      • Frontend cần thêm permission để xem medical history

3. VIEW_APPOINTMENT_OWN + Patient (Bệnh nhân):
   → Kiểm tra: auth.authorities contains "VIEW_APPOINTMENT_OWN"
   → OVERRIDE: WHERE appointments.patient_id = [my_patient_id]
   → PHỚT LỜI patientCode từ client

Response 200:

```json
{
  "content": [
    {
      "appointmentCode": "APT-20251115-001",
      "status": "SCHEDULED",
      "computedStatus": "LATE",
      "minutesLate": 15,
      "appointmentStartTime": "2025-11-15T10:00:00",
      "appointmentEndTime": "2025-11-15T10:40:00",
      "expectedDurationMinutes": 40,
      "patient": { 
        "patientCode": "BN-1001", 
        "fullName": "Đoàn Thanh Phong" 
      },
      "doctor": { 
        "employeeCode": "EMP001", 
        "fullName": "Lê Anh Khoa" 
      },
      "room": { 
        "roomCode": "P-01", 
        "roomName": "Phòng thường 1" 
      },
      "services": [
        { 
          "serviceCode": "GEN_EXAM", 
          "serviceName": "Khám tổng quát & Tư vấn" 
        }
      ],
      "participants": [
        { 
          "employeeCode": "EMP007", 
          "fullName": "Đoàn Nguyễn Khôi Nguyên", 
          "role": "ASSISTANT" 
        }
      ],
      "notes": "Khám tổng quát"
    }
  ],
  "page": 0,
  "size": 10,
  "totalPages": 5,
  "totalElements": 50
}
```

Computed Fields Explanation:

- computedStatus: Tính dựa trên status + appointmentStartTime vs NOW()
  • CANCELLED: status == CANCELLED
  • COMPLETED: status == COMPLETED
  • NO_SHOW: status == NO_SHOW
  • CHECKED_IN: status == CHECKED_IN
  • IN_PROGRESS: status == IN_PROGRESS
  • LATE: status == SCHEDULED && NOW() > appointmentStartTime (Bệnh nhân chưa check-in)
  • UPCOMING: status == SCHEDULED && NOW() <= appointmentStartTime

- minutesLate: Số phút trễ (chỉ có khi computedStatus = LATE)
  • Tính: Duration.between(appointmentStartTime, NOW()).toMinutes()
  • Use case: Dashboard hiển thị "Trễ 15 phút" với màu đỏ

Test Cases:

✅ Lễ tân - ⭐ Xem tất cả lịch hôm nay (DatePreset)
GET /api/v1/appointments?datePreset=TODAY
Token: Lễ tân (username: thuan.dk) với permission VIEW_APPOINTMENT_ALL
→ Backend: Auto tính dateFrom=2025-11-04, dateTo=2025-11-04

✅ Lễ tân - ⭐ Xem lịch tuần này (DatePreset)
GET /api/v1/appointments?datePreset=THIS_WEEK
→ Backend: Auto tính dateFrom=Monday, dateTo=Sunday của tuần hiện tại

✅ Lễ tân - ⭐ Xem lịch 7 ngày tới (DatePreset)
GET /api/v1/appointments?datePreset=NEXT_7_DAYS
→ Backend: Auto tính dateFrom=2025-11-04, dateTo=2025-11-10

✅ Lễ tân - ⭐ Xem lịch tháng này (DatePreset)
GET /api/v1/appointments?datePreset=THIS_MONTH
→ Backend: Auto tính dateFrom=2025-11-01, dateTo=2025-11-30

✅ Lễ tân - ⭐ TÌM THEO TÊN BỆNH NHÂN (CRITICAL Feature)
GET /api/v1/appointments?patientName=Phong
→ Backend: LOWER(CONCAT(first_name, ' ', last_name)) LIKE '%phong%'
→ Trả về appointments của "Đoàn Thanh Phong" + "Phạm Văn Phong"

✅ Lễ tân - ⭐ TÌM THEO SỐ ĐIỆN THOẠI
GET /api/v1/appointments?patientPhone=0912
→ Backend: phone LIKE '%0912%'
→ Trả về appointments có SĐT chứa "0912"

✅ Lễ tân - Lọc theo ngày + status + bác sĩ
GET /api/v1/appointments?dateFrom=2025-11-15&dateTo=2025-11-15&status=SCHEDULED&status=CHECKED_IN&employeeCode=EMP001

✅ Lễ tân - ⭐ LỌC THEO DỊCH VỤ (NEW)
GET /api/v1/appointments?serviceCode=IMPL_SURGERY_KR&dateFrom=2025-11-15&dateTo=2025-11-15
→ Backend: JOIN appointment_services WHERE service_code = 'IMPL_SURGERY_KR'
→ Use case: "Tháng này có bao nhiêu ca Implant?"

✅ Lễ tân - Lọc theo phòng
GET /api/v1/appointments?roomCode=P-01

✅ Bác sĩ - Xem lịch của mình (Auto-filter)
GET /api/v1/appointments?today=true
Token: Bác sĩ Lê Anh Khoa (username: khoa.la) với permission VIEW_APPOINTMENT_OWN
→ Backend: findByAccount_Username("khoa.la") → employeeId = EMP001
→ Backend tự động: WHERE (employee_id=EMP001 OR EXISTS participant)
→ PHỚT LỜI nếu client cố gửi employeeCode=EMP002

✅ Bác sĩ - Xem lịch tuần tới
GET /api/v1/appointments?dateFrom=2025-11-11&dateTo=2025-11-17&sortBy=appointmentStartTime&sortDirection=ASC
Token: Bác sĩ (VIEW_APPOINTMENT_OWN)

✅ Y tá/Phụ tá - Xem lịch tham gia
GET /api/v1/appointments?today=true
Token: Y tá Đoàn Nguyễn Khôi Nguyên (username: nguyen.dnk) với VIEW_APPOINTMENT_OWN
→ Backend: Trả về appointments WHERE participant.employee_id = EMP007

✅ ⭐ OBSERVER (Thực tập sinh) - Xem lịch được mời quan sát
GET /api/v1/appointments?today=true
Token: Thực tập sinh Nguyễn Khánh Linh (username: linh.nk) với permission VIEW_APPOINTMENT_OWN
→ Backend: findByAccount_Username("linh.nk") → employeeId = 12 (EMP012)
→ Backend: WHERE EXISTS (participant WHERE employee_id = 12 AND role = 'OBSERVER')
→ ⚠️ CHỈ THẤY appointments mà họ được thêm vào danh sách participants
→ Security: KHÔNG leak thông tin bệnh nhân của appointments khác
→ Test Data: EMP012 - Nguyễn Khánh Linh - Thực tập sinh (ROLE_DENTIST_INTERN)
→ Expected: Trống ban đầu, sau khi add vào participant list mới thấy

✅ ⭐ OBSERVER - Thêm vào participant, verify thấy appointment
1. Admin adds EMP012 to APT-20251115-001 as OBSERVER
2. Login as linh.nk
3. GET /api/v1/appointments?datePreset=TODAY
4. Should return APT-20251115-001 in response

✅ ⭐ OBSERVER - Xóa khỏi participant, verify không còn thấy
1. Admin removes EMP012 from APT-20251115-001
2. Login as linh.nk
3. GET /api/v1/appointments?datePreset=TODAY
4. Should return empty list []

✅ Bệnh nhân - Xem lịch của mình
GET /api/v1/appointments
Token: Bệnh nhân Đoàn Thanh Phong (username: phong.dt) với VIEW_APPOINTMENT_OWN
→ Backend: TODO - Cần mapping Patient.account
→ Backend tự động: WHERE patient_id = BN-1001

✅ Bệnh nhân - Xem lịch sắp tới
GET /api/v1/appointments?dateFrom=2025-11-15&status=SCHEDULED&sortBy=appointmentStartTime&sortDirection=ASC
Token: Bệnh nhân (VIEW_APPOINTMENT_OWN)

❌ Bệnh nhân cố xem lịch người khác - PHỚT LỜI filter (SECURITY)
GET /api/v1/appointments?patientCode=BN-1002
Token: Bệnh nhân BN-1001 với VIEW_APPOINTMENT_OWN
→ Backend OVERRIDE: Vẫn chỉ trả về appointments của BN-1001
→ Security: Prevent privilege escalation

❌ Bác sĩ cố xem lịch bác sĩ khác - PHỚT LỜI filter (SECURITY)
GET /api/v1/appointments?employeeCode=EMP002
Token: Bác sĩ EMP001 với VIEW_APPOINTMENT_OWN
→ Backend OVERRIDE: Vẫn chỉ trả về appointments của EMP001
→ Security: Prevent data leak

❌ OBSERVER cố xem tất cả lịch - BỊ GIỚI HẠN (SECURITY)
GET /api/v1/appointments?dateFrom=2025-11-01&dateTo=2025-11-30
Token: OBSERVER với VIEW_APPOINTMENT_OWN
→ Backend: CHỈ trả về appointments mà OBSERVER THAM GIA
→ Không có permission VIEW_APPOINTMENT_ALL → Không thấy toàn bộ

❌ Unauthorized - Không có quyền VIEW
GET /api/v1/appointments
Token: Không có VIEW_APPOINTMENT_ALL hoặc VIEW_APPOINTMENT_OWN
→ 403 Forbidden

Implementation Notes:

⚠️ CRITICAL IMPROVEMENTS (vs Initial Design):

1. ✅ Search by Patient Name/Phone (FIXED)
   - JOIN patients table
   - LIKE search: LOWER(CONCAT(first_name, ' ', last_name)) LIKE '%search%'
   - Real-world use case: Lễ tân gõ "Lan" thay vì nhớ "BN-1234"

2. ✅ Filter by Service Code (ADDED)
   - JOIN appointment_services + services
   - Use case: "Tháng này có bao nhiêu ca Implant?"

3. ✅ Permission-based Auth (FIXED)
   - Check "VIEW_APPOINTMENT_ALL" in authorities
   - NOT check role_id
   - Data-driven: Easy to add new roles via database

4. ✅ OBSERVER Role Security (CLARIFIED)
   - OBSERVER có permission VIEW_APPOINTMENT_OWN
   - CHỈ thấy appointments họ được mời tham gia
   - Principle of Least Privilege
   - Medical data privacy protection
   - Test user: EMP012 - Nguyễn Khánh Linh (linh.nk)

5. ✅ DatePreset Enum (IMPLEMENTED)
   - TODAY, THIS_WEEK, NEXT_7_DAYS, THIS_MONTH
   - Backend tự động tính dateFrom/dateTo
   - KHÔNG cần thay đổi DB Schema V16
   - Use case: Dashboard quick filters

6. ✅ Computed Fields (IMPLEMENTED)
   - computedStatus: UPCOMING | LATE | IN_PROGRESS | CHECKED_IN | COMPLETED | CANCELLED
   - minutesLate: Số phút trễ (Duration.between)
   - Real-time calculation based on NOW()
   - Use case: Dashboard color coding (red for LATE)

7. ⚠️ N+1 Query Warning (Noted - TODO)
   - Current: Load patient/employee per appointment (N+1)
   - TODO: Batch loading or @EntityGraph
   - Impact: Performance with 100+ appointments

6. ⚠️ Patient RBAC Mapping (TODO)
   - Employee mapping: ✅ DONE (findByAccount_Username)
   - Patient mapping: ❌ TODO (need Patient.account relationship)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SEED DATA
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Test Accounts:

- thuan.dk (Lễ tân) - ROLE_RECEPTIONIST - Permission: VIEW_APPOINTMENT_ALL
- khoa.la (Bác sĩ) - ROLE_DOCTOR - Permission: VIEW_APPOINTMENT_OWN
- nguyen.dnk (Y tá) - ROLE_NURSE - Permission: VIEW_APPOINTMENT_OWN
- linh.nk (Thực tập sinh) ✅ NEW - ROLE_DENTIST_INTERN - Permission: VIEW_APPOINTMENT_OWN
- phong.dt (Bệnh nhân) - ROLE_PATIENT - Permission: VIEW_APPOINTMENT_OWN (TODO: mapping)

Employees (Ca Sáng 8-12h on 2025-11-15):

- EMP001 - Lê Anh Khoa - Nha sĩ - Chỉnh nha (ID 1), Phục hồi (ID 4), STANDARD (ID 8)
- EMP002 - Trịnh Công Thái - Nha sĩ - Nội nha (ID 2), Răng thẩm mỹ (ID 7), STANDARD (ID 8)
- EMP003 - Jimmy Donaldson - Nha sĩ (Part-time flex) - Nha khoa trẻ em (ID 6), STANDARD (ID 8)
- EMP007 - Đoàn Nguyễn Khôi Nguyên - Y tá - STANDARD (ID 8)
- EMP009 - Huỳnh Tấn Quang Nhật - Y tá (Part-time fixed) - STANDARD (ID 8)

Employees (Ca Chiều 13-17h on 2025-11-15):

- EMP004 - Junya Ota - Nha sĩ (Part-time fixed) - Phẫu thuật (ID 5), STANDARD (ID 8)
- EMP008 - Nguyễn Trần Tuấn Khang - Y tá - STANDARD (ID 8)
- EMP010 - Ngô Đình Chính - Y tá (Part-time flex) - STANDARD (ID 8)

Services:

- GEN_EXAM (30 min + 15 buffer) STANDARD (ID 8)
- SCALING_L1 (45 min + 15 buffer) Nha chu (ID 3)
- ORTHO_BRACES_ON (90 min + 30 buffer) Chỉnh nha (ID 1)
- CROWN_EMAX (60 min + 15 buffer) Phục hồi (ID 4)
- IMPL_SURGERY_KR (90 min + 30 buffer) Phục hồi (ID 4)

Rooms:

- P-01 (STANDARD) - Compatible với tất cả STANDARD services
- P-02 (STANDARD) - Compatible với tất cả STANDARD services
- P-03 (STANDARD) - Compatible với tất cả STANDARD services
- P-04-IMPLANT (IMPLANT) - Compatible với IMPLANT + tất cả STANDARD services
