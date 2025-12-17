# Notification System API Documentation

## Overview

Hệ thống thông báo real-time sử dụng WebSocket + REST API để gửi và quản lý thông báo cho người dùng.

## Architecture

- **Database**: PostgreSQL với bảng `notifications`
- **Real-time**: Spring WebSocket với STOMP protocol
- **Persistence**: Lưu trữ thông báo trong database (offline users vẫn nhận được khi login)
- **Security**: JWT authentication cho cả REST API và WebSocket

## Database Schema

### Notifications Table

```sql
CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type notification_entity_type,
    related_entity_id VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    read_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### Enum Types

```sql
-- notification_type
'APPOINTMENT_CREATED', 'APPOINTMENT_UPDATED', 'APPOINTMENT_CANCELLED',
'APPOINTMENT_REMINDER', 'APPOINTMENT_COMPLETED', 'TREATMENT_PLAN_APPROVED',
'TREATMENT_PLAN_UPDATED', 'PAYMENT_RECEIVED', 'SYSTEM_ANNOUNCEMENT'

-- notification_entity_type
'APPOINTMENT', 'TREATMENT_PLAN', 'PAYMENT', 'SYSTEM'
```

---

## REST API Endpoints

### Base URL

```
/api/v1/notifications
```

### 1. Lấy danh sách thông báo (Paginated)

**GET** `/api/v1/notifications`

**Query Parameters:**

- `page` (optional): Số trang (default: 0)
- `size` (optional): Số lượng mỗi trang (default: 20)

**Response:**

```json
{
  "success": true,
  "message": "Lấy danh sách thông báo thành công",
  "data": {
    "content": [
      {
        "notificationId": 1,
        "userId": 101,
        "type": "APPOINTMENT_CREATED",
        "title": "Đặt lịch thành công",
        "message": "Cuộc hẹn APP00123 đã được đặt thành công vào 18/12/2024 10:00",
        "relatedEntityType": "APPOINTMENT",
        "relatedEntityId": "APP00123",
        "isRead": false,
        "createdAt": "2024-12-18T09:30:00",
        "readAt": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalPages": 1,
    "totalElements": 5
  }
}
```

**Permissions:** `VIEW_NOTIFICATION`, `MANAGE_NOTIFICATION`

---

### 2. Lấy số lượng thông báo chưa đọc

**GET** `/api/v1/notifications/unread-count`

**Response:**

```json
{
  "success": true,
  "message": "Lấy số lượng thông báo chưa đọc thành công",
  "data": 3
}
```

**Permissions:** `VIEW_NOTIFICATION`, `MANAGE_NOTIFICATION`

**Use Case:** Hiển thị badge số lượng thông báo chưa đọc trên icon

---

### 3. Đánh dấu một thông báo là đã đọc

**PATCH** `/api/v1/notifications/{notificationId}/read`

**Response:**

```json
{
  "success": true,
  "message": "Đánh dấu đã đọc thành công",
  "data": null
}
```

**Permissions:** `VIEW_NOTIFICATION`, `MANAGE_NOTIFICATION`

**Notes:** Chỉ user sở hữu thông báo mới có thể đánh dấu đã đọc

---

### 4. Đánh dấu TẤT CẢ thông báo là đã đọc

**PATCH** `/api/v1/notifications/read-all`

**Response:**

```json
{
  "success": true,
  "message": "Đánh dấu tất cả đã đọc thành công",
  "data": null
}
```

**Permissions:** `VIEW_NOTIFICATION`, `MANAGE_NOTIFICATION`

---

### 5. Xóa một thông báo

**DELETE** `/api/v1/notifications/{notificationId}`

**Response:**

```json
{
  "success": true,
  "message": "Xóa thông báo thành công",
  "data": null
}
```

**Permissions:** `DELETE_NOTIFICATION`, `MANAGE_NOTIFICATION`

---

### 6. Tạo thông báo (Admin/System Only)

**POST** `/api/v1/notifications`

**Request Body:**

```json
{
  "userId": 101,
  "type": "APPOINTMENT_CREATED",
  "title": "Đặt lịch thành công",
  "message": "Cuộc hẹn APP00123 đã được đặt thành công vào 18/12/2024 10:00",
  "relatedEntityType": "APPOINTMENT",
  "relatedEntityId": "APP00123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Tạo thông báo thành công",
  "data": {
    "notificationId": 1,
    "userId": 101,
    "type": "APPOINTMENT_CREATED",
    "title": "Đặt lịch thành công",
    "message": "Cuộc hẹn APP00123 đã được đặt thành công vào 18/12/2024 10:00",
    "relatedEntityType": "APPOINTMENT",
    "relatedEntityId": "APP00123",
    "isRead": false,
    "createdAt": "2024-12-18T09:30:00",
    "readAt": null
  }
}
```

**Permissions:** `MANAGE_NOTIFICATION`

**Notes:** Endpoint này được sử dụng bởi internal services hoặc admin panel

---

## WebSocket Real-Time Notifications

### Connection URL

```
ws://localhost:8080/ws (Development)
wss://your-production-domain.com/ws (Production)
```

### Protocol

- **STOMP** (Simple Text Oriented Messaging Protocol)
- **Fallback**: SockJS (cho browsers không hỗ trợ WebSocket)

### Subscribe to Notifications

```javascript
// Subscribe to personal notifications
stompClient.subscribe("/topic/notifications/{userId}", function (message) {
  const notification = JSON.parse(message.body);
  console.log("New notification:", notification);

  // Update UI với notification mới
  displayNotification(notification);
  updateUnreadCount();
});
```

### Message Format

Khi có thông báo mới, server sẽ push message với format:

```json
{
  "notificationId": 1,
  "userId": 101,
  "type": "APPOINTMENT_CREATED",
  "title": "Đặt lịch thành công",
  "message": "Cuộc hẹn APP00123 đã được đặt thành công vào 18/12/2024 10:00",
  "relatedEntityType": "APPOINTMENT",
  "relatedEntityId": "APP00123",
  "isRead": false,
  "createdAt": "2024-12-18T09:30:00",
  "readAt": null
}
```

---

## Event Triggers

### 1. Appointment Created

**Trigger:** Khi appointment được tạo thành công
**Event:** `AppointmentCreationService.createAppointmentInternal()`
**Notification Type:** `APPOINTMENT_CREATED`
**Recipients:** Patient của appointment
**Message Example:** "Cuộc hẹn APP00123 đã được đặt thành công vào 18/12/2024 10:00"

### Future Events (Planned)

- Appointment updated/cancelled
- Appointment reminder (1 day before, 1 hour before)
- Treatment plan approved/updated
- Payment received
- System announcements

---

## Permissions

### VIEW_NOTIFICATION

- **Module:** NOTIFICATION
- **Description:** Xem thông báo của bản thân
- **Display Order:** 300
- **Roles:** ROLE_PATIENT, ROLE_RECEPTIONIST, ROLE_DENTIST, ROLE_MANAGER

### DELETE_NOTIFICATION

- **Module:** NOTIFICATION
- **Description:** Xóa thông báo của bản thân
- **Display Order:** 301
- **Roles:** ROLE_PATIENT, ROLE_RECEPTIONIST, ROLE_DENTIST, ROLE_MANAGER

### MANAGE_NOTIFICATION

- **Module:** NOTIFICATION
- **Description:** Toàn quyền quản lý thông báo (Admin/System)
- **Display Order:** 302
- **Roles:** ROLE_ADMIN

---

## Security Notes

1. **User Isolation**: Mỗi user CHỈ xem được thông báo của mình
2. **WebSocket Authentication**: Cần JWT token để kết nối WebSocket
3. **CORS**: WebSocket endpoint được config với `app.cors.allowed-origins`
4. **Authorization**: Tất cả endpoints đều yêu cầu authentication

---

## Error Handling

### Common Error Responses

**404 Not Found**

```json
{
  "success": false,
  "message": "Notification not found with notificationId: 999",
  "data": null
}
```

**403 Forbidden**

```json
{
  "success": false,
  "message": "User không có quyền đánh dấu thông báo này",
  "data": null
}
```

**401 Unauthorized**

```json
{
  "success": false,
  "message": "User not authenticated",
  "data": null
}
```

---

## Testing

### Test Scenarios

1. **Create Appointment → Notification Sent**

   - POST `/api/v1/appointments` (create appointment)
   - Verify notification được tạo trong database
   - Verify WebSocket push (nếu user đang online)
   - GET `/api/v1/notifications` (verify notification xuất hiện)

2. **Mark as Read**

   - PATCH `/api/v1/notifications/{id}/read`
   - Verify `isRead = true` và `readAt` được set

3. **Unread Count**

   - GET `/api/v1/notifications/unread-count`
   - Verify số lượng chính xác

4. **WebSocket Connection**
   - Connect to `/ws` với JWT token
   - Subscribe to `/topic/notifications/{userId}`
   - Trigger notification event
   - Verify message received

---

## Performance Considerations

1. **Pagination**: Luôn sử dụng pagination khi lấy danh sách thông báo
2. **Indexing**: Database có index trên `user_id` và `is_read`
3. **Cleanup**: Nên có cron job xóa notifications cũ (> 30 days)
4. **Caching**: Consider caching unread count nếu traffic cao

---

## Future Enhancements

1. **Notification Preferences**: User tự config loại thông báo muốn nhận
2. **Email Fallback**: Gửi email nếu user offline quá 24h
3. **Push Notifications**: Mobile push notifications (FCM)
4. **Notification Templates**: Template engine cho messages
5. **Batch Notifications**: Gửi thông báo cho nhiều users cùng lúc
