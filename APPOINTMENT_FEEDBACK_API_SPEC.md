# Appointment Feedback API Specification

## Overview
API cho tính năng đánh giá lịch hẹn (Appointment Feedback). Bệnh nhân có thể đánh giá sau khi lịch hẹn hoàn thành.

## Database Schema

### Table: `appointment_feedbacks`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `feedback_id` | BIGINT | PK, AUTO_INCREMENT | ID đánh giá |
| `appointment_code` | VARCHAR(50) | FK, UNIQUE, NOT NULL | Mã lịch hẹn |
| `patient_id` | BIGINT | FK, NOT NULL | ID bệnh nhân |
| `rating` | TINYINT | NOT NULL, CHECK(1-5) | Số sao (1-5) |
| `comment` | TEXT | NULL | Nội dung đánh giá |
| `tags` | JSON | NULL | Danh sách tags ["Sạch sẽ", "Thân thiện"] |
| `created_by` | BIGINT | FK, NOT NULL | Người tạo (patient_id hoặc employee_id) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |

**Indexes:**
- `idx_feedback_appointment` ON `appointment_code`
- `idx_feedback_patient` ON `patient_id`
- `idx_feedback_rating` ON `rating`

---

## API Endpoints

### 1. Create Feedback
Tạo đánh giá cho lịch hẹn đã hoàn thành.

```
POST /api/v1/feedbacks
```

**Request Body:**
```json
{
  "appointmentCode": "APT-20260107-001",
  "rating": 5,
  "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
  "tags": ["Thân thiện", "Chuyên nghiệp", "Tư vấn kỹ"]
}
```

**Validation:**
| Field | Rules |
|-------|-------|
| `appointmentCode` | Required, must exist, status = COMPLETED |
| `rating` | Required, integer 1-5 |
| `comment` | Optional, max 1000 characters |
| `tags` | Optional, array of strings, max 10 tags |

**Response Success (201):**
```json
{
  "success": true,
  "data": {
    "feedbackId": 123,
    "appointmentCode": "APT-20260107-001",
    "rating": 5,
    "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
    "tags": ["Thân thiện", "Chuyên nghiệp", "Tư vấn kỹ"],
    "createdAt": "2026-01-07T10:30:00Z"
  },
  "message": "Đánh giá đã được gửi thành công"
}
```

**Error Responses:**

| Code | Error Code | Message |
|------|------------|---------|
| 400 | `INVALID_RATING` | Rating phải từ 1 đến 5 |
| 400 | `FEEDBACK_ALREADY_EXISTS` | Lịch hẹn này đã được đánh giá |
| 403 | `APPOINTMENT_NOT_COMPLETED` | Chỉ có thể đánh giá lịch hẹn đã hoàn thành |
| 403 | `NOT_AUTHORIZED` | Bạn không có quyền đánh giá lịch hẹn này |
| 404 | `APPOINTMENT_NOT_FOUND` | Không tìm thấy lịch hẹn |

---

### 2. Get Feedback by Appointment
Lấy đánh giá của một lịch hẹn.

```
GET /api/v1/feedbacks/appointment/{appointmentCode}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "feedbackId": 123,
    "appointmentCode": "APT-20260107-001",
    "patientName": "Đoàn Thanh Phong",
    "rating": 5,
    "comment": "Bác sĩ làm nhẹ nhàng, tư vấn kỹ",
    "tags": ["Thân thiện", "Chuyên nghiệp"],
    "createdAt": "2026-01-07T10:30:00Z"
  }
}
```

**Response Not Found (404):**
```json
{
  "success": false,
  "error": {
    "code": "FEEDBACK_NOT_FOUND",
    "message": "Lịch hẹn này chưa có đánh giá"
  }
}
```

---

### 3. Get Feedbacks List (Admin/Employee)
Lấy danh sách đánh giá với filter và pagination.

```
GET /api/v1/feedbacks
```

**Query Parameters:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Trang (0-indexed) |
| `size` | int | 20 | Số item/trang |
| `rating` | int | - | Filter theo số sao (1-5) |
| `employeeCode` | string | - | Filter theo bác sĩ |
| `patientCode` | string | - | Filter theo bệnh nhân |
| `fromDate` | string | - | Filter từ ngày (YYYY-MM-DD) |
| `toDate` | string | - | Filter đến ngày (YYYY-MM-DD) |
| `sort` | string | createdAt,desc | Sắp xếp |

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "feedbackId": 123,
        "appointmentCode": "APT-20260107-001",
        "patientName": "Đoàn Thanh Phong",
        "employeeName": "Trịnh Công Thái",
        "rating": 5,
        "comment": "Bác sĩ làm nhẹ nhàng",
        "tags": ["Thân thiện"],
        "createdAt": "2026-01-07T10:30:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

---

### 4. Get Feedback Statistics (Admin/Employee)
Lấy thống kê đánh giá.

```
GET /api/v1/feedbacks/statistics
```

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `employeeCode` | string | Filter theo bác sĩ (optional) |
| `fromDate` | string | Từ ngày (optional) |
| `toDate` | string | Đến ngày (optional) |

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "totalFeedbacks": 150,
    "averageRating": 4.5,
    "ratingDistribution": {
      "1": 5,
      "2": 10,
      "3": 20,
      "4": 45,
      "5": 70
    },
    "topTags": [
      { "tag": "Thân thiện", "count": 80 },
      { "tag": "Chuyên nghiệp", "count": 65 },
      { "tag": "Sạch sẽ", "count": 50 }
    ]
  }
}
```

---

## Business Rules

| Rule ID | Description |
|---------|-------------|
| BR-20 | Đánh giá đã gửi KHÔNG thể chỉnh sửa hoặc xóa |
| BR-21 | Người được phép đánh giá: Bệnh nhân của lịch hẹn, Admin, Manager |
| BR-22 | Chỉ đánh giá được lịch hẹn có status = `COMPLETED` |
| BR-23 | Mỗi lịch hẹn chỉ được đánh giá 1 lần (UNIQUE constraint) |
| BR-24 | Rating bắt buộc (1-5 sao), comment và tags là tùy chọn |

---

## Authorization

| Endpoint | Patient | Employee | Admin |
|----------|---------|----------|-------|
| POST /feedbacks | ✅ (own appointment) | ❌ | ✅ |
| GET /feedbacks/appointment/{code} | ✅ (own) | ✅ | ✅ |
| GET /feedbacks | ❌ | ✅ | ✅ |
| GET /feedbacks/statistics | ❌ | ✅ | ✅ |

---

## Predefined Tags

```java
public enum FeedbackTag {
    CLEAN("Sạch sẽ"),
    FRIENDLY("Thân thiện"),
    PROFESSIONAL("Chuyên nghiệp"),
    ON_TIME("Đúng giờ"),
    DETAILED_CONSULTATION("Tư vấn kỹ"),
    GENTLE("Nhẹ nhàng"),
    REASONABLE_PRICE("Giá hợp lý"),
    GOOD_FACILITIES("Cơ sở vật chất tốt");
}
```

---

## Integration with Appointment API

Cập nhật response của Appointment API để include feedback info:

```json
// GET /api/v1/appointments/{code}
{
  "appointmentCode": "APT-20260107-001",
  "status": "COMPLETED",
  // ... other fields
  "hasFeedback": true,
  "feedback": {
    "feedbackId": 123,
    "rating": 5,
    "comment": "...",
    "createdAt": "..."
  }
}
```

Hoặc thêm field `hasFeedback` vào list response:

```json
// GET /api/v1/appointments
{
  "content": [
    {
      "appointmentCode": "APT-20260107-001",
      "status": "COMPLETED",
      "hasFeedback": false  // <-- Thêm field này
    }
  ]
}
```

---

## Error Codes Summary

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_RATING` | 400 | Rating không hợp lệ |
| `FEEDBACK_ALREADY_EXISTS` | 400 | Đã đánh giá rồi |
| `APPOINTMENT_NOT_COMPLETED` | 403 | Lịch hẹn chưa hoàn thành |
| `NOT_AUTHORIZED` | 403 | Không có quyền |
| `APPOINTMENT_NOT_FOUND` | 404 | Không tìm thấy lịch hẹn |
| `FEEDBACK_NOT_FOUND` | 404 | Không tìm thấy đánh giá |
