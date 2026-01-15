# NOTIFICATION SYSTEM - FE & BE INTEGRATION GUIDE (Updated Dec 18, 2025)

## 📢 **THÔNG BÁO QUAN TRỌNG: BUG FIX & UPDATES**

### 🐛 **Bug đã được sửa**

**Vấn đề:** Notification KHÔNG được tạo ra sau khi tạo appointment thành công

**Nguyên nhân:** 
1. Method `createAppointment()` trong `AppointmentCreationService.java` không gọi notification service
2. LazyInitializationException khi load patient account

**Các thay đổi đã thực hiện:**

#### **File 1: AppointmentCreationService.java**

**Location:** Line 233 (sau `insertAuditLog()`)

**Thay đổi:**
```java
insertAuditLog(appointment, createdById);

// ✅ NEW: Send notification to patient, doctor, and participants
sendAppointmentCreatedNotification(appointment, patient);

log.info("Successfully created appointment: {}", appointment.getAppointmentCode());
```

**Location:** Line 1003 (trong method `sendAppointmentCreatedNotification()`)

**Thay đổi:**
```java
// OLD: Regular findById (LAZY load issue)
Patient patientWithAccount = patientRepository.findById(patient.getPatientId())
                .orElse(patient);

// ✅ NEW: Use JOIN FETCH to eagerly load account
Patient patientWithAccount = patientRepository.findOneByPatientCodeWithAccount(patient.getPatientCode())
                .orElse(patient);
```

---

## 🎯 **LUỒNG NOTIFICATION HOẠT ĐỘNG NHƯ THẾ NÀO**

### **Backend Flow**

```
1. Client POST /api/v1/appointments
   ↓
2. AppointmentController.createAppointment()
   ↓
3. AppointmentCreationService.createAppointment()
   ├─ Validate patient, doctor, room, services
   ├─ Check conflicts, shifts, holidays
   ├─ Insert appointment to database
   ├─ Insert appointment services
   ├─ Insert appointment participants
   ├─ Insert audit log
   └─ ✅ sendAppointmentCreatedNotification()  ← NEW!
       ├─ Load patient with account (JOIN FETCH)
       ├─ Create notification for PATIENT
       │   └─ notificationService.createNotification()
       │       ├─ Save to database (notifications table)
       │       └─ Push via WebSocket to /topic/notifications/{account_id}
       └─ Create notifications for PARTICIPANTS (if any)
           └─ For each participant (assistant, secondary doctor, observer)
               └─ notificationService.createNotification()
```

### **Database Flow**

```sql
-- Notification được lưu vào table
INSERT INTO notifications (
    user_id,                    -- Account ID của người nhận
    type,                       -- 'APPOINTMENT_CREATED'
    title,                      -- 'Đặt lịch thành công'
    message,                    -- 'Cuộc hẹn APT-XXX đã được đặt...'
    related_entity_type,        -- 'APPOINTMENT'
    related_entity_id,          -- 'APT-20260108-001'
    is_read,                    -- FALSE (chưa đọc)
    created_at                  -- Current timestamp
) VALUES (...);
```

---

## 📋 **THÔNG TIN CHO FRONTEND TEAM**

### **1. API Endpoints** 

#### **Base URL:** `http://localhost:8080/api/v1`

#### **Authentication:** 
Tất cả requests cần header: `Authorization: Bearer {JWT_TOKEN}`

### **2. Notification REST APIs**

#### **GET /notifications** - Lấy danh sách notification
```bash
GET /api/v1/notifications?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách thông báo thành công",
  "data": {
    "content": [
      {
        "notificationId": 1,
        "userId": 12,
        "type": "APPOINTMENT_CREATED",
        "title": "Đặt lịch thành công",
        "message": "Cuộc hẹn APT-20260108-001 đã được đặt thành công vào 08/01/2026 10:00",
        "relatedEntityType": "APPOINTMENT",
        "relatedEntityId": "APT-20260108-001",
        "isRead": false,
        "createdAt": "2025-12-18T15:30:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

#### **GET /notifications/unread-count** - Đếm số notification chưa đọc
```bash
GET /api/v1/notifications/unread-count
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy số lượng thông báo chưa đọc thành công",
  "data": 5
}
```

#### **PATCH /notifications/{id}/read** - Đánh dấu đã đọc
```bash
PATCH /api/v1/notifications/1/read
Authorization: Bearer {token}
```

**Response:** `204 No Content`

#### **PATCH /notifications/read-all** - Đánh dấu tất cả đã đọc
```bash
PATCH /api/v1/notifications/read-all
Authorization: Bearer {token}
```

**Response:** `204 No Content`

#### **DELETE /notifications/{id}** - Xóa notification
```bash
DELETE /api/v1/notifications/1
Authorization: Bearer {token}
```

**Response:** `204 No Content`

---

### **3. WebSocket Real-time Push**

#### **Connection URL:**
```
ws://localhost:8080/ws
```

#### **JavaScript Example (SockJS + STOMP):**

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// 1. Get JWT token from login
const token = localStorage.getItem('jwt_token');

// 2. Extract account_id from JWT
function getAccountIdFromToken(token) {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.account_id; // Integer
}

// 3. Create WebSocket connection
const socket = new SockJS('http://localhost:8080/ws');

const stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
        Authorization: `Bearer ${token}` // IMPORTANT: JWT in CONNECT frame
    },
    debug: (str) => console.log('STOMP:', str),
    
    onConnect: (frame) => {
        console.log('✅ WebSocket Connected');
        
        // 4. Subscribe to user's notification topic
        const accountId = getAccountIdFromToken(token);
        stompClient.subscribe(`/topic/notifications/${accountId}`, (message) => {
            const notification = JSON.parse(message.body);
            console.log('🔔 New Notification:', notification);
            
            // Update UI: Show toast, update badge, play sound
            handleNewNotification(notification);
        });
    },
    
    onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame);
    }
});

// 5. Connect
stompClient.activate();

// 6. Disconnect on logout
function disconnectWebSocket() {
    if (stompClient) {
        stompClient.deactivate();
    }
}
```

---

### **4. Notification Types**

```typescript
enum NotificationType {
    APPOINTMENT_CREATED = 'APPOINTMENT_CREATED',
    APPOINTMENT_UPDATED = 'APPOINTMENT_UPDATED',
    APPOINTMENT_CANCELLED = 'APPOINTMENT_CANCELLED',
    APPOINTMENT_COMPLETED = 'APPOINTMENT_COMPLETED',
    // ... other types
}
```

---

### **5. Khi nào notification được tạo?**

#### **APPOINTMENT_CREATED** - Tạo lịch hẹn thành công

**Người nhận notification:**
1. **Patient** (Bệnh nhân):
   - Title: "Đặt lịch thành công"
   - Message: "Cuộc hẹn {appointmentCode} đã được đặt thành công vào {time}"

2. **Participants** (Bác sĩ phụ, trợ lý, quan sát viên - nếu có):
   - Title: "Bạn đã được phân công làm {role}"
   - Message: "Cuộc hẹn {appointmentCode} vào {time} - Bệnh nhân: {patientName}"

**Lưu ý:** 
- Bác sĩ chính (primary doctor) KHÔNG nhận notification (vì họ là người tạo hoặc được assign trực tiếp)
- Chỉ participants (ASSISTANT, SECONDARY_DOCTOR, OBSERVER) mới nhận

---

## 🔍 **DEBUGGING GUIDE FOR FE**

### **Test Notification Flow**

#### **Step 1: Login as Patient**
```bash
POST /api/v1/auth/login
Body: {"username": "benhnhan1", "password": "123456"}
# Lưu token
```

#### **Step 2: Create Appointment (as Receptionist/Admin)**
```bash
POST /api/v1/appointments
Authorization: Bearer {receptionist_token}
Body: {
    "patientCode": "BN-1001",
    "employeeCode": "EMP002",
    "roomCode": "P-01",
    "serviceCodes": ["OTHER_DIAMOND"],
    "appointmentStartTime": "2026-01-10T09:00:00",
    "notes": "Test notification",
    "participantCodes": []
}
```

#### **Step 3: Check Notifications (as Patient)**
```bash
GET /api/v1/notifications
Authorization: Bearer {patient_token}
# Should return 1 notification with type APPOINTMENT_CREATED
```

#### **Step 4: Connect WebSocket (as Patient)**
```javascript
// Subscribe to /topic/notifications/12 (patient account_id = 12)
// Should receive real-time push when new notification created
```

---

## 🚨 **TROUBLESHOOTING**

### **Problem: Không nhận được notification**

**Checklist:**
1. ✅ BE đã restart sau khi fix?
2. ✅ Patient có account? (check database: `SELECT * FROM patients WHERE patient_code = 'BN-1001'`)
3. ✅ Token đúng user? (decode JWT để xem `account_id`)
4. ✅ WebSocket connected? (check browser console)
5. ✅ Subscribe đúng topic? (`/topic/notifications/{account_id}`)
6. ✅ Check BE logs xem có error không?

### **BE Logs để check:**

```
=== Starting notification creation for appointment APT-XXX ===
Sending notification to PATIENT userId=12 for appointment APT-XXX
✓ Patient notification created successfully
=== Notification creation completed for appointment APT-XXX ===
```

**Nếu thấy log:**
```
Patient {id} has no account, skipping patient notification
```
→ Patient không có account, không thể gửi notification

---

## 📊 **DATABASE SCHEMA**

### **Table: notifications**

```sql
CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,                    -- FK to accounts.account_id
    type VARCHAR(50) NOT NULL,                   -- APPOINTMENT_CREATED, etc.
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),             -- APPOINTMENT, TREATMENT_PLAN, etc.
    related_entity_id VARCHAR(50),               -- APT-XXX, PLAN-XXX, etc.
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);
```

---

## 🎨 **UI/UX RECOMMENDATIONS**

### **1. Notification Bell Icon**

```tsx
<NotificationBell 
    unreadCount={5}
    onClick={() => openNotificationDropdown()}
/>
```

### **2. Notification Dropdown**

- Hiển thị 5-10 notifications gần nhất
- Highlight notifications chưa đọc (bold text, different background)
- Click vào notification:
  - Đánh dấu đã đọc (PATCH /notifications/{id}/read)
  - Navigate đến appointment detail (nếu relatedEntityType = APPOINTMENT)

### **3. Real-time Toast**

Khi nhận WebSocket message:
```javascript
toast.success({
    title: notification.title,
    message: notification.message,
    duration: 5000,
    onClick: () => navigateToAppointment(notification.relatedEntityId)
});
```

### **4. Sound Effect**

```javascript
const notificationSound = new Audio('/sounds/notification.mp3');
notificationSound.play();
```

---

## 📝 **CHANGELOG**

### **Version 1.1 - December 18, 2025**

**Fixes:**
- ✅ Fixed notification not being created after appointment creation
- ✅ Fixed LazyInitializationException when loading patient account
- ✅ Added proper JOIN FETCH for patient account relationship

**Changes:**
- Added `sendAppointmentCreatedNotification()` call in `createAppointment()` method
- Updated patient loading to use `findOneByPatientCodeWithAccount()` instead of `findById()`

**Testing:**
- ✅ Appointment creation works
- ⚠️ Notification creation: STILL INVESTIGATING
  - Fixed: Added sendAppointmentCreatedNotification() call
  - Fixed: Changed to use JOIN FETCH query for patient account
  - Issue: Notifications still not appearing in database
  - Next: Need to check BE logs for exceptions in try-catch block

---

## 🔗 **RELATED DOCUMENTS**

- [NOTIFICATION_SYSTEM_FE_READY.md](./NOTIFICATION_SYSTEM_FE_READY.md) - Full API documentation
- [NOTIFICATION_NOT_CREATED_ROOT_CAUSE_ANALYSIS.md](./NOTIFICATION_NOT_CREATED_ROOT_CAUSE_ANALYSIS.md) - Bug analysis
- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Complete API reference

---

## 👥 **CONTACTS**

**Backend Team:**
- Issues: Check BE logs in terminal "Run: DentalClinicManagementApplication"
- Debug: Add breakpoint in `AppointmentCreationService.sendAppointmentCreatedNotification()`

**Frontend Team:**
- WebSocket issues: Check browser console for STOMP errors
- API issues: Check Network tab for request/response

---

*Last Updated: December 18, 2025*  
*Status: 🟡 IN TESTING - Notification creation being debugged*  
*Next Steps: Resolve LazyInitializationException and verify end-to-end flow*
