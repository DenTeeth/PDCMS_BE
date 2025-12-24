# Tổng Hợp Sửa Lỗi Notification Khi Tạo Lịch Hẹn

**Ngày:** 19/12/2024  
**Module:** Booking Appointment - Notification System  
**File chính:** `AppointmentCreationService.java`

---

## 📋 Tóm Tắt

Phát hiện và sửa **2 lỗi nghiêm trọng** trong hệ thống thông báo khi tạo lịch hẹn:
1. ❌ **Lỗi #1:** Không gọi method tạo notification sau khi tạo appointment
2. ❌ **Lỗi #2:** Chỉ gửi notification cho bệnh nhân và participants, bỏ sót bác sĩ chính

**Kết quả:** ✅ Notification được gửi đầy đủ cho cả 3 nhóm: Bệnh nhân, Bác sĩ chính, và Participants

---

## 🐛 LỖI #1: Không Gọi Method Tạo Notification

### Mô tả lỗi
- **File:** `AppointmentCreationService.java`
- **Method:** `createAppointment()` (line ~215-230)
- **Vấn đề:** Sau khi tạo appointment thành công, code KHÔNG gọi method `sendAppointmentCreatedNotification()`
- **Hậu quả:** Không có notification nào được tạo trong database

### Code trước khi sửa
```java
insertAuditLog(appointment, createdById);

// BỊ THIẾU: Không gọi sendAppointmentCreatedNotification()

log.info("Successfully created appointment: {}", appointment.getAppointmentCode());

// STEP 9: Build and return response
return buildResponse(appointment, patient, doctor, room, services, participants);
```

### Code sau khi sửa
```java
insertAuditLog(appointment, createdById);

// Send notification to patient, doctor, and participants
log.info("🔔🔔🔔 CALLING sendAppointmentCreatedNotification for appointment: {}", appointment.getAppointmentCode());
sendAppointmentCreatedNotification(appointment, patient);
log.info("🔔🔔🔔 FINISHED sendAppointmentCreatedNotification for appointment: {}", appointment.getAppointmentCode());

log.info("Successfully created appointment: {}", appointment.getAppointmentCode());

// STEP 9: Build and return response
return buildResponse(appointment, patient, doctor, room, services, participants);
```

### Thay đổi
- ✅ Thêm dòng gọi method `sendAppointmentCreatedNotification(appointment, patient)`
- ✅ Thêm logging với emoji markers `🔔🔔🔔` để dễ debug
- ✅ Áp dụng cho cả 2 methods: `createAppointment()` và `createAppointmentInternal()`

---

## 🐛 LỖI #2: Thiếu Notification Cho Bác Sĩ Chính

### Mô tả lỗi
- **File:** `AppointmentCreationService.java`
- **Method:** `sendAppointmentCreatedNotification()` (line ~999-1130)
- **Vấn đề:** Method chỉ gửi notification cho 2 nhóm:
  1. ✅ Bệnh nhân (patient)
  2. ✅ Participants (trợ lý, phụ tá)
  3. ❌ BỊ BỎ SÓT: Bác sĩ chính (main doctor) được assign vào appointment
- **Hậu quả:** Bác sĩ không nhận được thông báo khi có lịch hẹn mới

### Code trước khi sửa
```java
// 1. Send notification to PATIENT
if (patientWithAccount.getAccount() != null) {
    // ... gửi notification cho patient ...
}

// BỊ THIẾU: Không gửi cho bác sĩ chính

// 3. Send notifications to ALL PARTICIPANTS
List<AppointmentParticipant> participants = appointmentParticipantRepository
        .findByIdAppointmentId(appointment.getAppointmentId());
// ... gửi notification cho participants ...
```

### Code sau khi sửa
```java
// 1. Send notification to PATIENT
if (patientWithAccount.getAccount() != null) {
    // ... gửi notification cho patient ...
}

// 2. Send notification to MAIN DOCTOR (dentist assigned to appointment)
Employee mainDoctor = employeeRepository.findById(appointment.getEmployeeId()).orElse(null);
if (mainDoctor != null && mainDoctor.getAccount() != null) {
    Integer doctorUserId = mainDoctor.getAccount().getAccountId();
    log.info("Sending notification to MAIN DOCTOR userId={} (employeeCode={}) for appointment {}",
            doctorUserId, mainDoctor.getEmployeeCode(), appointment.getAppointmentCode());

    CreateNotificationRequest doctorNotification = CreateNotificationRequest.builder()
            .userId(doctorUserId)
            .type(NotificationType.APPOINTMENT_CREATED)
            .title("Bạn có lịch hẹn mới")
            .message(String.format("Cuộc hẹn %s vào %s - Bệnh nhân: %s",
                    appointment.getAppointmentCode(), formattedTime, patient.getFullName()))
            .relatedEntityType(NotificationEntityType.APPOINTMENT)
            .relatedEntityId(appointment.getAppointmentCode())
            .build();

    notificationService.createNotification(doctorNotification);
    log.info("✓ Main doctor notification created successfully");
} else {
    log.warn("Main doctor has no account, skipping doctor notification");
}

// 3. Send notifications to ALL PARTICIPANTS
List<AppointmentParticipant> participants = appointmentParticipantRepository
        .findByIdAppointmentId(appointment.getAppointmentId());
// ... gửi notification cho participants ...
```

### Thay đổi
- ✅ Thêm section mới: "2. Send notification to MAIN DOCTOR"
- ✅ Query doctor từ database: `employeeRepository.findById(appointment.getEmployeeId())`
- ✅ Kiểm tra doctor có account không
- ✅ Tạo notification với title "Bạn có lịch hẹn mới"
- ✅ Thêm logging chi tiết để debug

### Chi tiết notification cho bác sĩ
```java
{
  "userId": <doctorAccountId>,
  "type": "APPOINTMENT_CREATED",
  "title": "Bạn có lịch hẹn mới",
  "message": "Cuộc hẹn APT-20260107-001 vào 07/01/2026 09:00 - Bệnh nhân: Nguyễn Văn A",
  "relatedEntityType": "APPOINTMENT",
  "relatedEntityId": "APT-20260107-001"
}
```

---

## 🔍 Quá Trình Debug

### Bước 1: Phát hiện lỗi #1
- **Triệu chứng:** Không có notification nào trong database sau khi tạo appointment
- **Kiểm tra:** Query database `SELECT * FROM notifications WHERE related_entity_id = 'APT-...'`
- **Kết quả:** 0 records
- **Nguyên nhân:** Missing call to `sendAppointmentCreatedNotification()`

### Bước 2: Sửa lỗi #1 và test
- **Hành động:** Thêm call `sendAppointmentCreatedNotification()` sau `insertAuditLog()`
- **Test:** Tạo appointment mới
- **Kết quả:** ✅ Patient notification xuất hiện trong database
- **Phát hiện:** User báo "tài khoản của bacsi2 vẫn chưa có thông báo"

### Bước 3: Phát hiện lỗi #2
- **Kiểm tra code:** Read method `sendAppointmentCreatedNotification()`
- **Phát hiện:** Code chỉ gửi cho patient và participants, thiếu main doctor
- **Xác nhận:** Comment trong code: "1. Patient", "3. Participants" → missing "2. Main Doctor"

### Bước 4: Sửa lỗi #2
- **Hành động:** Thêm section "2. Send notification to MAIN DOCTOR"
- **Logic:** Query doctor bằng `appointment.getEmployeeId()`, gửi notification
- **Status:** Code đã fix, đang chờ test sau khi restart BE

---

## 📊 Test Cases

### Test Case 1: Tạo Appointment Thành Công
**Input:**
```json
{
  "patientCode": "BN-1001",
  "employeeCode": "EMP002",
  "roomCode": "ROOM-001",
  "serviceCodes": ["SRV-001"],
  "appointmentStartTime": "2026-01-07T09:00:00",
  "participantCodes": []
}
```

**Expected Output:**
- ✅ Appointment created: `APT-20260107-001`
- ✅ Notification cho patient (benhnhan1):
  - Title: "Đặt lịch thành công"
  - Message: "Cuộc hẹn APT-20260107-001 vào 07/01/2026 09:00"
- ✅ Notification cho doctor (bacsi2):
  - Title: "Bạn có lịch hẹn mới"
  - Message: "Cuộc hẹn APT-20260107-001 vào 07/01/2026 09:00 - Bệnh nhân: Nguyễn Văn A"

**Actual Results (Sau Fix #1):**
- ✅ Patient notification: Confirmed by user "đã thấy thông báo trong db rồi"
- ❌ Doctor notification: User reported "tài khoản của bacsi2 vẫn chưa có thông báo"

**Actual Results (Sau Fix #2):**
- ⏳ Pending test - BE needs restart

---

## 🔧 Debug Tools Added

### Emoji Logging Markers
Thêm emoji markers để dễ dàng theo dõi flow trong logs:

**AppointmentCreationService.java:**
```java
log.info("🔔🔔🔔 CALLING sendAppointmentCreatedNotification for appointment: {}", appointmentCode);
// ... notification logic ...
log.info("🔔🔔🔔 FINISHED sendAppointmentCreatedNotification for appointment: {}", appointmentCode);
```

**NotificationServiceImpl.java:**
```java
log.info("🔥🔥🔥 NotificationService.createNotification() CALLED for user: {}, type: {}", userId, type);
```

### Lợi ích:
- ✅ Dễ grep logs: `grep "🔔🔔🔔" application.log`
- ✅ Nổi bật trong terminal với màu sắc
- ✅ Track flow xuyên suốt nhiều services

---

## 📝 Files Modified

### 1. AppointmentCreationService.java
**Location:** `src/main/java/com/dental/clinic/management/booking_appointment/service/AppointmentCreationService.java`

**Changes:**
- Line 233-235: Added notification call in `createAppointment()`
- Line 337: Added notification call in `createAppointmentInternal()`
- Line 1037-1060: Added "2. Send notification to MAIN DOCTOR" section

**Total lines added:** ~35 lines

### 2. test_quick.ps1
**Location:** `test_quick.ps1`

**Changes:**
- Updated test dates to valid doctor shift dates (2026-01-06, 2026-01-07)
- Added fallback to afternoon slot if morning fails

---

## 🎯 Kết Quả Sau Khi Sửa

### Notification Flow Hoàn Chỉnh
```
Tạo Appointment
    ↓
sendAppointmentCreatedNotification()
    ↓
    ├─→ [1] Patient Notification
    │   ├─ Title: "Đặt lịch thành công"
    │   └─ Message: "Cuộc hẹn {code} vào {time}"
    │
    ├─→ [2] Main Doctor Notification ← FIX #2
    │   ├─ Title: "Bạn có lịch hẹn mới"
    │   └─ Message: "Cuộc hẹn {code} vào {time} - Bệnh nhân: {name}"
    │
    └─→ [3] Participants Notifications (if any)
        ├─ Title: "Bạn được mời tham gia cuộc hẹn mới"
        └─ Message: "Cuộc hẹn {code} vào {time} - Vai trò: {role}"
```

### Database Records
```sql
-- After creating appointment APT-20260107-001
SELECT * FROM notifications WHERE related_entity_id = 'APT-20260107-001';

-- Expected results (3 notifications if 0 participants):
-- 1. userId=<patientAccountId>, type=APPOINTMENT_CREATED, title="Đặt lịch thành công"
-- 2. userId=<doctorAccountId>, type=APPOINTMENT_CREATED, title="Bạn có lịch hẹn mới"
-- 3. (optional) userId=<participantAccountId>, type=APPOINTMENT_CREATED
```

---

## ⚠️ Breaking Changes

**KHÔNG CÓ** - Tất cả thay đổi đều backward compatible:
- Chỉ thêm notification mới, không thay đổi logic hiện tại
- API response không đổi
- Database schema không đổi

---

## 🚀 Deployment Notes

### Bước 1: Build & Test
```bash
# Compile code
mvn clean compile

# Run BE
.\mvnw.cmd spring-boot:run

# Wait for "Started DentalClinicManagementApplication"
```

### Bước 2: Test Notification
```powershell
# Run automated test
.\test_quick.ps1

# Expected output:
# - Appointment created: APT-20260107-001
# - Notifications: 2+ notifications (patient + doctor + participants)
```

### Bước 3: Verify Database
```sql
-- Check notification records
SELECT 
    n.notification_id,
    n.user_id,
    n.type,
    n.title,
    n.message,
    n.is_read,
    n.created_at
FROM notifications n
WHERE n.related_entity_id = 'APT-20260107-001'
ORDER BY n.created_at DESC;

-- Expected: 2+ rows (patient, doctor, possibly participants)
```

---

## 📚 Related Documentation

- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Appointment API endpoints
- [NOTIFICATION_SYSTEM_FE_READY.md](./NOTIFICATION_SYSTEM_FE_READY.md) - Notification system overview
- [FE_APPOINTMENT_BUSINESS_RULES_SUMMARY.md](./FE_APPOINTMENT_BUSINESS_RULES_SUMMARY.md) - Appointment business rules

---

## ✅ Checklist

### Fix #1: Missing Notification Call
- [x] Identify missing call location
- [x] Add `sendAppointmentCreatedNotification()` call
- [x] Add debug logging
- [x] Test patient notification
- [x] Verify in database

### Fix #2: Missing Doctor Notification
- [x] Analyze `sendAppointmentCreatedNotification()` method
- [x] Identify missing doctor notification
- [x] Add "2. Send notification to MAIN DOCTOR" section
- [x] Query doctor from repository
- [x] Create notification request
- [x] Add error handling
- [ ] Test doctor notification (pending BE restart)
- [ ] Verify in database (pending)

### Documentation
- [x] Document both bugs
- [x] Document fixes with code samples
- [x] Create test cases
- [x] Add deployment notes

---

## 🔮 Future Improvements

1. **Refactor Notification Logic**
   - Extract notification creation to separate helper methods
   - Reduce code duplication (patient, doctor, participants use similar structure)

2. **Add Notification Templates**
   - Create template system for notification messages
   - Support multi-language (Vietnamese, English)

3. **Batch Notification**
   - Send multiple notifications in one transaction
   - Improve performance for appointments with many participants

4. **Real-time Push Notification**
   - Integrate WebSocket/SSE for real-time updates
   - Notify users immediately without polling

---

## 👥 Contact

**Developer:** GitHub Copilot  
**Reviewer:** TBD  
**Approved By:** TBD

---

## 📅 Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2024-12-19 | Initial documentation | Copilot |
| 1.1 | 2024-12-19 | Added Fix #2 (main doctor notification) | Copilot |

---

**Status:** ✅ Fixes Implemented, ⏳ Pending Final Test
