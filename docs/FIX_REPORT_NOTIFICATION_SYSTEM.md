# BÁO CÁO SỬA LỖI: NOTIFICATION SYSTEM (Dec 18, 2025)

## 📌 TÓM TẮT

**Lỗi:** Notification không được tạo ra sau khi tạo appointment thành công  
**Ngày phát hiện:** December 18, 2025  
**Người fix:** Backend Team  
**Status:** 🟡 ĐANG KIỂM TRA (85% complete)

---

## 🐛 VẤN ĐỀ

### Mô tả lỗi
Khi tạo appointment qua API `POST /api/v1/appointments`, appointment được tạo thành công nhưng KHÔNG có notification nào được gửi đến patient hoặc participants.

### Tác động
- ❌ Patient không nhận được thông báo đặt lịch thành công
- ❌ Participants (trợ lý, bác sĩ phụ) không biết họ được phân công
- ❌ Real-time notification qua WebSocket không hoạt động

---

## 🔍 NGUYÊN NHÂN

### 1. **Logic Flow bị thiếu (ROOT CAUSE)**

**File:** `AppointmentCreationService.java`

**Vấn đề:**  
Method `createAppointment()` (public method được gọi từ Controller) KHÔNG gọi `sendAppointmentCreatedNotification()`.

**Code cũ:**
```java
public CreateAppointmentResponse createAppointment(CreateAppointmentRequest request) {
    // ... validation logic ...
    
    insertAppointment(...);
    insertAppointmentServices(...);
    insertAppointmentParticipants(...);
    insertAuditLog(appointment, createdById);
    
    // ❌ THIẾU: sendAppointmentCreatedNotification()
    
    return buildResponse(appointment, ...);
}
```

**Tại sao lại có lỗi này?**
- Có 2 methods tạo appointment: `createAppointment()` và `createAppointmentInternal()`
- Chỉ có `createAppointmentInternal()` gọi notification (dùng cho reschedule)
- Controller gọi `createAppointment()` → không có notification

### 2. **LazyInitializationException**

**File:** `AppointmentCreationService.java` - Method `sendAppointmentCreatedNotification()`

**Vấn đề:**  
Patient entity có relationship `@OneToOne(fetch = FetchType.LAZY)` với Account. Khi gọi `patientRepository.findById()`, account không được load → exception khi access `patient.getAccount()`.

**Code cũ:**
```java
// ❌ LAZY load - account không được fetch
Patient patientWithAccount = patientRepository.findById(patient.getPatientId())
                .orElse(patient);

if (patientWithAccount.getAccount() != null) {  // LazyInitializationException!
    // ...
}
```

---

## ✅ GIẢI PHÁP ĐÃ ÁP DỤNG

### **Fix 1: Thêm notification call**

**File:** `AppointmentCreationService.java` (Line 233)

**Thay đổi:**
```java
insertAuditLog(appointment, createdById);

// ✅ THÊM MỚI: Gửi notification cho patient và participants
sendAppointmentCreatedNotification(appointment, patient);

log.info("Successfully created appointment: {}", appointment.getAppointmentCode());
```

**Giải thích:**
- Thêm dòng gọi `sendAppointmentCreatedNotification()` sau khi insert audit log
- Đảm bảo notification được gửi mỗi khi appointment được tạo thành công

### **Fix 2: Sử dụng JOIN FETCH**

**File:** `AppointmentCreationService.java` (Line 1003)

**Thay đổi:**
```java
// ❌ CŨ: Regular findById (LAZY load)
Patient patientWithAccount = patientRepository.findById(patient.getPatientId())
                .orElse(patient);

// ✅ MỚI: Dùng JOIN FETCH để load account ngay
Patient patientWithAccount = patientRepository.findOneByPatientCodeWithAccount(patient.getPatientCode())
                .orElse(patient);
```

**Giải thích:**
- `findOneByPatientCodeWithAccount()` đã có sẵn trong repository
- Dùng `LEFT JOIN FETCH p.account` để eagerly load account relationship
- Tránh LazyInitializationException khi access `patient.getAccount()`

---

## 📋 FILES ĐÃ THAY ĐỔI

### 1. **AppointmentCreationService.java**
**Location:** `src/main/java/com/dental/clinic/management/booking_appointment/service/AppointmentCreationService.java`

**Changes:**
- Line 233: Thêm `sendAppointmentCreatedNotification(appointment, patient);`
- Line 1003: Đổi `findById()` → `findOneByPatientCodeWithAccount()`

**Commit message suggestion:**
```
fix: Add notification call after appointment creation

- Call sendAppointmentCreatedNotification() in createAppointment()
- Use JOIN FETCH query to avoid LazyInitializationException
- Fixes #XXX: Notifications not sent after booking appointment
```

---

## 🧪 TESTING

### **Test Script:** `test_appointment_notification.ps1`

**Cách chạy:**
```powershell
cd D:\PDCMS_BE
.\test_appointment_notification.ps1
```

**Kết quả mong đợi:**
```
✅ Appointment created successfully
✅ Total notifications: 1
✅ Found 1 related notification(s)
  → ID: 1 | Type: APPOINTMENT_CREATED
    Title: Đặt lịch thành công
    Message: Cuộc hẹn APT-XXX đã được đặt thành công vào ...
```

### **Test Cases**

#### ✅ **Test 1: Appointment Creation**
```bash
POST /api/v1/appointments
{
  "patientCode": "BN-1001",
  "employeeCode": "EMP002",
  "roomCode": "P-01",
  "serviceCodes": ["OTHER_DIAMOND"],
  "appointmentStartTime": "2026-01-08T10:00:00",
  "notes": "Test",
  "participantCodes": []
}
```
**Result:** ✅ Appointment created with code `APT-20260108-001`

#### ⚠️ **Test 2: Notification Creation**
```bash
GET /api/v1/notifications (as patient BN-1001)
```
**Expected:** 1 notification with type APPOINTMENT_CREATED  
**Actual:** 0 notifications  
**Status:** 🔴 FAILED - Still investigating

### **Issue: Notifications not appearing**

**Possible causes being investigated:**
1. ✅ Method not called → FIXED
2. ✅ LazyInitializationException → FIXED  
3. ⚠️ Exception caught in try-catch block → CHECKING
4. ⚠️ Transaction rollback → CHECKING
5. ⚠️ NotificationService issue → CHECKING

**Next steps:**
1. Check BE logs for exceptions:
   - Look for log: `"=== Starting notification creation for appointment..."`
   - Look for errors in try-catch block
2. Add debug breakpoint in `sendAppointmentCreatedNotification()`
3. Check database: `SELECT * FROM notifications WHERE user_id = 12`
4. Verify NotificationService.createNotification() is being called

---

## 📚 DOCUMENTS ĐÃ TẠO

### 1. **NOTIFICATION_NOT_CREATED_ROOT_CAUSE_ANALYSIS.md**
**Nội dung:** Phân tích chi tiết nguyên nhân lỗi, kiến trúc code, luồng xử lý

**Location:** `docs/NOTIFICATION_NOT_CREATED_ROOT_CAUSE_ANALYSIS.md`

**Key sections:**
- Nguyên nhân gốc rễ
- Kiến trúc hiện tại vs kiến trúc mong muốn
- Dữ liệu patient BN-1001
- Phân tích code notification
- Giải pháp đề xuất

### 2. **NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md**
**Nội dung:** Hướng dẫn integration cho FE và BE team

**Location:** `docs/NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md`

**Key sections:**
- Thông báo về bug fixes
- Luồng notification hoạt động
- REST API endpoints
- WebSocket real-time push
- UI/UX recommendations
- Troubleshooting guide
- Database schema

---

## 👥 HƯỚNG DẪN CHO TEAM

### **Cho Backend Team**

#### **Để test locally:**
1. Restart BE application
2. Chạy script: `.\test_appointment_notification.ps1`
3. Check BE logs trong terminal "Run: DentalClinicManagementApplication"
4. Tìm log messages:
   ```
   === Starting notification creation for appointment APT-XXX ===
   Sending notification to PATIENT userId=12 for appointment APT-XXX
   ✓ Patient notification created successfully
   ```

#### **Nếu không thấy logs:**
- Method `sendAppointmentCreatedNotification()` không được gọi
- Check lại xem đã sửa đúng file chưa
- Verify BE đã restart

#### **Nếu có exception:**
- Check full stack trace
- Verify patient có account (query: `SELECT * FROM patients p JOIN accounts a ON p.account_id = a.account_id WHERE p.patient_code = 'BN-1001'`)
- Check transaction rollback

### **Cho Frontend Team**

#### **API Integration:**
1. Đọc file: `NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md`
2. Implement REST API calls:
   - `GET /api/v1/notifications` - List notifications
   - `GET /api/v1/notifications/unread-count` - Badge count
   - `PATCH /api/v1/notifications/{id}/read` - Mark as read

#### **WebSocket Integration:**
1. Setup SockJS + STOMP client
2. Connect với JWT token trong header
3. Subscribe to `/topic/notifications/{account_id}`
4. Handle incoming messages để update UI real-time

#### **Testing:**
1. Login as patient: `benhnhan1 / 123456`
2. Tạo appointment (as admin/receptionist)
3. Check patient notifications API
4. Verify WebSocket nhận được message

#### **Current Status:**
- ⚠️ BE vẫn đang fix notification creation
- ✅ FE có thể bắt đầu implement UI cho notification bell, dropdown
- ✅ REST APIs đã sẵn sàng để test (có thể tạo manual notification qua API)
- ⏳ WebSocket sẽ hoạt động khi BE fix xong

---

## 🔄 TIMELINE

### **December 18, 2025**

**9:00 AM** - Phát hiện bug: Notification không được tạo  
**9:30 AM** - Phân tích root cause  
**10:00 AM** - Fix #1: Thêm sendAppointmentCreatedNotification() call  
**10:15 AM** - Fix #2: Sửa LazyInitializationException với JOIN FETCH  
**10:30 AM** - Test #1: Appointment creation OK, notifications still 0  
**10:45 AM** - Document: Tạo 2 files guide  
**11:00 AM** - Status: 🟡 INVESTIGATING - Need to check why notifications not saved to DB

---

## ✅ CHECKLIST

### **Completed:**
- [x] Phân tích root cause
- [x] Fix method call trong createAppointment()
- [x] Fix LazyInitializationException
- [x] Tạo test script
- [x] Tạo documentation cho FE/BE
- [x] Update code với JOIN FETCH query

### **In Progress:**
- [ ] Debug: Tại sao notifications không lưu vào DB
- [ ] Verify NotificationService.createNotification() được gọi
- [ ] Check BE logs để tìm exception

### **Todo:**
- [ ] Verify end-to-end flow hoạt động
- [ ] Test WebSocket push
- [ ] Test với multiple participants
- [ ] Performance testing với nhiều notifications
- [ ] Deploy to staging/production

---

## 🎯 KẾT QUẢ MONG ĐỢI (SAU KHI FIX HOÀN TOÀN)

### **User Experience:**

1. **Patient (Bệnh nhân):**
   - Ngay sau khi tạo appointment → Nhận notification trong 1-2 giây
   - Notification bell hiển thị badge count
   - Toast notification xuất hiện (if online)
   - Email notification gửi đến (optional)

2. **Doctor/Staff:**
   - Nhận notification khi được assign vào appointment
   - Bell icon update real-time
   - Sound notification (if enabled)

3. **Admin/Receptionist:**
   - Có thể xem tất cả notifications (if permission)
   - Dashboard hiển thị notification stats

### **Technical:**
- ✅ Notification saved to database
- ✅ WebSocket push to connected clients
- ✅ REST API returns correct data
- ✅ No LazyInitializationException
- ✅ Transaction commits successfully
- ✅ Logs show successful creation

---

## 📞 CONTACT

**Nếu có vấn đề:**
- BE issues: Check terminal logs "Run: DentalClinicManagementApplication"
- FE issues: Check browser console for WebSocket errors
- Database issues: Query `SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10`

**Files liên quan:**
- Code: `AppointmentCreationService.java`
- Service: `NotificationServiceImpl.java`
- Repository: `NotificationRepository.java`
- Controller: `NotificationController.java`
- Test: `test_appointment_notification.ps1`
- Docs: `NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md`

---

*Báo cáo tạo bởi: Backend Team*  
*Ngày: December 18, 2025*  
*Status: 🟡 IN PROGRESS - 85% Complete*  
*File: FIX_REPORT_NOTIFICATION_SYSTEM.md*
