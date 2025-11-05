# HƯỚNG DẪN TEST API - HỆ THỐNG QUOTA ĐỘNG CHO PART-TIME FLEX

## 📋 TỔNG QUAN

Tài liệu này hướng dẫn Frontend Developer test các API liên quan đến hệ thống quota động cho nhân viên PART_TIME_FLEX.

**Tính năng chính:**
- Manager tạo slot part-time với date range linh hoạt
- Nhân viên PART_TIME_FLEX đăng ký làm việc (trạng thái PENDING)
- Manager duyệt/từ chối đăng ký
- Hệ thống tính quota theo ngày (chỉ đếm đăng ký APPROVED)

---

## 🔐 THÔNG TIN ĐĂNG NHẬP

### Manager Account
```
Username: quan.vnm
Password: 123456
Role: ROLE_MANAGER
Permissions: MANAGE_WORK_SLOTS, MANAGE_PART_TIME_REGISTRATIONS, VIEW_AVAILABLE_SLOTS
```

### Part-Time Flex Employees
```
Employee 1:
  Username: jimmy.d
  Password: 123456
  Employment Type: PART_TIME_FLEX
  
Employee 2:
  Username: chinh.nd
  Password: 123456
  Employment Type: PART_TIME_FLEX
  
Employee 3:
  Username: linh.nk
  Password: 123456
  Employment Type: PART_TIME_FLEX
```

---

## 📝 API ENDPOINTS

### 1. ĐĂNG NHẬP (LOGIN)

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "username": "quan.vnm",
  "password": "123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "quan.vnm",
  "email": "quan.vnm@dentalclinic.com",
  "roles": ["ROLE_MANAGER"],
  "permissions": [
    "MANAGE_WORK_SLOTS",
    "MANAGE_PART_TIME_REGISTRATIONS",
    "VIEW_AVAILABLE_SLOTS",
    ...
  ],
  "employmentType": "FULL_TIME",
  "baseRole": "employee",
  "tokenExpiresAt": 1762340425
}
```

**Lưu ý:** Lưu `token` để dùng cho các request tiếp theo trong header `Authorization: Bearer {token}`

---

## 👨‍💼 QUẢN LÝ SLOT (MANAGER)

### 2. TẠO SLOT PART-TIME MỚI

**Endpoint:** `POST /api/v1/work-slots`

**Permission:** `MANAGE_WORK_SLOTS`

**Đăng nhập:** Manager (quan.vnm)

**Request Headers:**
```
Authorization: Bearer {manager_token}
Content-Type: application/json
```

**Request Body - Slot đơn giản (1 ngày):**
```json
{
  "workShiftId": "WKS_MORNING_02",
  "dayOfWeek": "FRIDAY",
  "quota": 3,
  "effectiveFrom": "2025-11-09",
  "effectiveTo": "2026-02-09"
}
```

**Request Body - Slot nhiều ngày:**
```json
{
  "workShiftId": "WKS_AFTERNOON_01",
  "dayOfWeek": "MONDAY,TUESDAY,THURSDAY,FRIDAY",
  "quota": 2,
  "effectiveFrom": "2025-11-09",
  "effectiveTo": "2026-02-09"
}
```

**Response (201 Created):**
```json
{
  "slotId": 16,
  "workShiftId": "WKS_MORNING_02",
  "workShiftName": "Ca Part-time Sáng (8h-12h)",
  "dayOfWeek": "FRIDAY",
  "quota": 3,
  "registered": 0,
  "isActive": true,
  "effectiveFrom": "2025-11-09",
  "effectiveTo": "2026-02-09"
}
```

**Giải thích:**
- `quota`: Số người cần MỖI NGÀY (ví dụ: quota=3 nghĩa là cần 3 người mỗi thứ 6)
- `registered`: Số người đã đăng ký ĐƯỢC DUYỆT (chỉ đếm APPROVED)
- `dayOfWeek`: Có thể là 1 ngày ("FRIDAY") hoặc nhiều ngày ngăn cách bởi dấu phẩy ("MONDAY,TUESDAY")
- `effectiveFrom/effectiveTo`: Khoảng thời gian slot có hiệu lực

---

### 3. XEM TẤT CẢ SLOT

**Endpoint:** `GET /api/v1/work-slots`

**Permission:** `VIEW_WORK_SHIFTS` hoặc `MANAGE_WORK_SLOTS`

**Đăng nhập:** Manager hoặc Employee

**Request Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "slotId": 16,
    "workShiftId": "WKS_MORNING_02",
    "workShiftName": "Ca Part-time Sáng (8h-12h)",
    "dayOfWeek": "FRIDAY",
    "quota": 3,
    "registered": 2,
    "isActive": true,
    "effectiveFrom": "2025-11-09",
    "effectiveTo": "2026-02-09"
  },
  {
    "slotId": 17,
    "workShiftId": "WKS_AFTERNOON_02",
    "workShiftName": "Ca Part-time Chiều (13h-17h)",
    "dayOfWeek": "MONDAY,WEDNESDAY",
    "quota": 2,
    "registered": 0,
    "isActive": true,
    "effectiveFrom": "2025-11-10",
    "effectiveTo": "2026-02-10"
  }
]
```

**Giải thích:**
- Slot 16: Còn trống 1 chỗ (3 quota - 2 registered = 1 available)
- Slot 17: Còn trống 2 chỗ (2 quota - 0 registered = 2 available)

---

## 👥 ĐĂNG KÝ CA LÀM (EMPLOYEE)

### 4. TẠO ĐĂNG KÝ MỚI

**Endpoint:** `POST /api/v1/registrations/part-time`

**Permission:** `CREATE_REGISTRATION`

**Đăng nhập:** Part-Time Flex Employee (jimmy.d, chinh.nd, hoặc linh.nk)

**Request Headers:**
```
Authorization: Bearer {employee_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "partTimeSlotId": 16,
  "effectiveFrom": "2025-11-14",
  "effectiveTo": "2025-12-31",
  "dayOfWeek": ["FRIDAY"]
}
```

**Request Body - Đăng ký nhiều ngày:**
```json
{
  "partTimeSlotId": 17,
  "effectiveFrom": "2025-11-11",
  "effectiveTo": "2025-12-31",
  "dayOfWeek": ["MONDAY", "WEDNESDAY"]
}
```

**Response (201 Created):**
```json
{
  "registrationId": 1,
  "employeeId": 3,
  "partTimeSlotId": 16,
  "workShiftId": "WKS_MORNING_02",
  "shiftName": "Ca Part-time Sáng (8h-12h)",
  "dayOfWeek": "FRIDAY",
  "effectiveFrom": "2025-11-14",
  "effectiveTo": "2025-12-31",
  "status": "PENDING",
  "dates": [
    "2025-11-14",
    "2025-11-21",
    "2025-11-28",
    "2025-12-05",
    "2025-12-12",
    "2025-12-19",
    "2025-12-26"
  ],
  "reason": null,
  "processedBy": null,
  "processedAt": null,
  "createdAt": "2025-11-05T15:57:07.451949"
}
```

**Giải thích:**
- `status`: "PENDING" - Đang chờ manager duyệt
- `dates`: Hệ thống tự động tính tất cả các ngày phù hợp (ví dụ: tất cả thứ 6 từ 14/11 đến 31/12)
- `dayOfWeek`: Nhân viên chỉ định ngày nào trong tuần họ có thể làm
- Đăng ký chỉ được tạo nếu còn chỗ trống (quota chưa đầy)

**Error Response (409 Conflict) - QUOTA ĐÃ ĐẦY:**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Tất cả các ngày bạn yêu cầu đã đầy quota. Vui lòng chọn ngày khác.",
  "instance": "/api/v1/registrations/part-time"
}
```

**Giải thích lỗi:** Xảy ra khi TẤT CẢ các ngày nhân viên muốn đăng ký đã có đủ người (quota exceeded)

---

### 5. XEM ĐĂNG KÝ CỦA BẢN THÂN

**Endpoint:** `GET /api/v1/registrations/part-time`

**Permission:** `VIEW_REGISTRATION_OWN`

**Đăng nhập:** Part-Time Flex Employee

**Request Headers:**
```
Authorization: Bearer {employee_token}
```

**Response (200 OK):**
```json
[
  {
    "registrationId": 1,
    "employeeId": 3,
    "partTimeSlotId": 16,
    "workShiftId": "WKS_MORNING_02",
    "shiftName": "Ca Part-time Sáng (8h-12h)",
    "dayOfWeek": "FRIDAY",
    "effectiveFrom": "2025-11-14",
    "effectiveTo": "2025-12-31",
    "status": "APPROVED",
    "dates": [
      "2025-11-14",
      "2025-11-21",
      "2025-11-28",
      "2025-12-05",
      "2025-12-12",
      "2025-12-19",
      "2025-12-26"
    ],
    "reason": null,
    "processedBy": "Võ Nguyễn Minh Quân",
    "processedAt": "2025-11-05T16:04:50.454145",
    "createdAt": "2025-11-05T15:57:07.451949"
  }
]
```

---

## ✅ DUYỆT/TỪ CHỐI ĐĂNG KÝ (MANAGER)

### 6. XEM TẤT CẢ ĐĂNG KÝ CHỜ DUYỆT

**Endpoint:** `GET /api/v1/admin/registrations/part-time?status=PENDING`

**Permission:** `MANAGE_PART_TIME_REGISTRATIONS`

**Đăng nhập:** Manager (quan.vnm)

**Request Headers:**
```
Authorization: Bearer {manager_token}
```

**Query Parameters:**
- `status`: PENDING (mặc định), APPROVED, REJECTED, ALL
- `employeeId`: (optional) Filter theo ID nhân viên

**Response (200 OK):**
```json
[
  {
    "registrationId": 2,
    "employeeId": 10,
    "partTimeSlotId": 16,
    "workShiftId": "WKS_MORNING_02",
    "shiftName": "Ca Part-time Sáng (8h-12h)",
    "dayOfWeek": "FRIDAY",
    "effectiveFrom": "2025-11-14",
    "effectiveTo": "2025-12-31",
    "status": "PENDING",
    "dates": [
      "2025-11-14",
      "2025-11-21",
      "2025-11-28",
      "2025-12-05",
      "2025-12-12",
      "2025-12-19",
      "2025-12-26"
    ],
    "reason": null,
    "processedBy": null,
    "processedAt": null,
    "createdAt": "2025-11-05T16:00:15.123456"
  }
]
```

---

### 7. DUYỆT ĐĂNG KÝ

**Endpoint:** `PATCH /api/v1/admin/registrations/part-time/{registrationId}/status`

**Permission:** `MANAGE_PART_TIME_REGISTRATIONS`

**Đăng nhập:** Manager (quan.vnm)

**Request Headers:**
```
Authorization: Bearer {manager_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "APPROVED"
}
```

**Response (200 OK):**
```json
{
  "registrationId": 2,
  "employeeId": 10,
  "partTimeSlotId": 16,
  "workShiftId": "WKS_MORNING_02",
  "shiftName": "Ca Part-time Sáng (8h-12h)",
  "dayOfWeek": "FRIDAY",
  "effectiveFrom": "2025-11-14",
  "effectiveTo": "2025-12-31",
  "status": "APPROVED",
  "dates": [
    "2025-11-14",
    "2025-11-21",
    "2025-11-28",
    "2025-12-05",
    "2025-12-12",
    "2025-12-19",
    "2025-12-26"
  ],
  "reason": null,
  "processedBy": "Võ Nguyễn Minh Quân",
  "processedAt": "2025-11-05T16:04:50.454145",
  "createdAt": "2025-11-05T16:00:15.123456"
}
```

**Giải thích:**
- `status`: Chuyển từ "PENDING" → "APPROVED"
- `processedBy`: Tên manager đã duyệt
- `processedAt`: Thời gian duyệt
- Sau khi APPROVED, `registered` count của slot sẽ tăng lên

**Error Response (409 Conflict) - QUOTA VƯỢT QUÁ:**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Không thể duyệt: Một số ngày đã vượt quá quota. Ngày 2025-11-14: 3/3 đã đầy, Ngày 2025-11-21: 3/3 đã đầy",
  "instance": "/api/v1/admin/registrations/part-time/4/status"
}
```

**Giải thích lỗi:** Xảy ra khi manager cố duyệt nhưng một số ngày trong đăng ký đã đầy quota

---

### 8. TỪ CHỐI ĐĂNG KÝ

**Endpoint:** `PATCH /api/v1/admin/registrations/part-time/{registrationId}/status`

**Permission:** `MANAGE_PART_TIME_REGISTRATIONS`

**Đăng nhập:** Manager (quan.vnm)

**Request Headers:**
```
Authorization: Bearer {manager_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "REJECTED",
  "reason": "Not enough experienced staff this month"
}
```

**Lưu ý:** `reason` là BẮT BUỘC khi từ chối

**Response (200 OK):**
```json
{
  "registrationId": 4,
  "employeeId": 3,
  "partTimeSlotId": 17,
  "workShiftId": "WKS_AFTERNOON_02",
  "shiftName": "Ca Part-time Chiều (13h-17h)",
  "dayOfWeek": "MONDAY,WEDNESDAY",
  "effectiveFrom": "2025-11-11",
  "effectiveTo": "2025-12-31",
  "status": "REJECTED",
  "dates": [
    "2025-11-11",
    "2025-11-13",
    "2025-11-18",
    "2025-11-20",
    ...
  ],
  "reason": "Not enough experienced staff this month",
  "processedBy": "Võ Nguyễn Minh Quân",
  "processedAt": "2025-11-05T16:10:23.789012",
  "createdAt": "2025-11-05T16:05:30.654321"
}
```

**Giải thích:**
- `status`: Chuyển từ "PENDING" → "REJECTED"
- `reason`: Lý do từ chối (hiển thị cho nhân viên)
- Đăng ký REJECTED KHÔNG đếm vào quota

---

## 🔢 LOGIC TÍNH QUOTA

### Cách Hệ Thống Tính Quota

1. **Quota theo ngày:** Mỗi ngày làm việc có quota riêng
   - Ví dụ: Slot FRIDAY có quota=3 → Cần 3 người mỗi thứ 6

2. **Chỉ đếm APPROVED:** 
   - PENDING: Không đếm (chờ duyệt)
   - APPROVED: Đếm vào quota ✅
   - REJECTED: Không đếm

3. **Kiểm tra khi tạo đăng ký:**
   - Hệ thống kiểm tra TẤT CẢ ngày nhân viên muốn đăng ký
   - Nếu TẤT CẢ ngày đều đầy → Lỗi 409 Conflict
   - Nếu còn ít nhất 1 ngày trống → Cho phép tạo (PENDING)

4. **Kiểm tra khi duyệt:**
   - Manager duyệt → Hệ thống kiểm tra lại quota của TỪNG ngày
   - Nếu có ngày nào đầy → Lỗi 409 Conflict
   - Nếu tất cả ngày đều còn chỗ → Duyệt thành công

### Ví Dụ Thực Tế

**Slot:** FRIDAY, quota=3, date range: 01/11/2025 - 30/11/2025

**Các thứ 6 trong tháng 11:**
- 01/11/2025
- 08/11/2025
- 15/11/2025
- 22/11/2025
- 29/11/2025

**Tình huống:**
1. Employee A đăng ký TẤT CẢ thứ 6 → APPROVED → Mỗi thứ 6 có 1/3
2. Employee B đăng ký TẤT CẢ thứ 6 → APPROVED → Mỗi thứ 6 có 2/3
3. Employee C đăng ký TẤT CẢ thứ 6 → APPROVED → Mỗi thứ 6 có 3/3 (ĐẦY)
4. Employee D đăng ký TẤT CẢ thứ 6 → ❌ Lỗi 409 (tất cả ngày đều đầy)
5. Employee E đăng ký CHỈ 15/11 và 22/11 → ❌ Lỗi 409 (2 ngày này đều đầy)
6. Employee F đăng ký CHỈ 08/12 (tháng sau) → ✅ OK (tháng sau còn trống)

---

## 🧪 KỊCH BẢN TEST

### Test Case 1: Tạo Slot và Đăng Ký Thành Công

**Bước 1:** Login as Manager
```bash
POST /api/v1/auth/login
Body: {"username":"quan.vnm","password":"123456"}
→ Lưu token
```

**Bước 2:** Tạo slot mới
```bash
POST /api/v1/work-slots
Header: Authorization: Bearer {manager_token}
Body: {
  "workShiftId": "WKS_MORNING_02",
  "dayOfWeek": "FRIDAY",
  "quota": 2,
  "effectiveFrom": "2025-11-15",
  "effectiveTo": "2025-12-15"
}
→ Slot ID: 20, quota=2, registered=0
```

**Bước 3:** Login as Employee 1
```bash
POST /api/v1/auth/login
Body: {"username":"jimmy.d","password":"123456"}
→ Lưu token
```

**Bước 4:** Tạo đăng ký
```bash
POST /api/v1/registrations/part-time
Header: Authorization: Bearer {employee_token}
Body: {
  "partTimeSlotId": 20,
  "effectiveFrom": "2025-11-15",
  "effectiveTo": "2025-11-30",
  "dayOfWeek": ["FRIDAY"]
}
→ Registration ID: 10, status: PENDING
```

**Bước 5:** Manager duyệt
```bash
PATCH /api/v1/admin/registrations/part-time/10/status
Header: Authorization: Bearer {manager_token}
Body: {"status":"APPROVED"}
→ Status: APPROVED, processedBy: "Võ Nguyễn Minh Quân"
```

**Bước 6:** Kiểm tra slot
```bash
GET /api/v1/work-slots
→ Slot ID: 20, quota=2, registered=1 (tăng từ 0 lên 1)
```

---

### Test Case 2: Quota Vượt Quá - Lỗi Khi Tạo

**Tiếp tục từ Test Case 1...**

**Bước 7:** Employee 2 tạo đăng ký
```bash
POST /api/v1/registrations/part-time
Body: {
  "partTimeSlotId": 20,
  "effectiveFrom": "2025-11-15",
  "effectiveTo": "2025-11-30",
  "dayOfWeek": ["FRIDAY"]
}
→ Registration ID: 11, status: PENDING
```

**Bước 8:** Manager duyệt Employee 2
```bash
PATCH /api/v1/admin/registrations/part-time/11/status
Body: {"status":"APPROVED"}
→ Slot giờ có registered=2 (ĐẦY)
```

**Bước 9:** Employee 3 cố tạo đăng ký
```bash
POST /api/v1/registrations/part-time
Body: {
  "partTimeSlotId": 20,
  "effectiveFrom": "2025-11-15",
  "effectiveTo": "2025-11-30",
  "dayOfWeek": ["FRIDAY"]
}
→ ❌ Error 409: "Tất cả các ngày bạn yêu cầu đã đầy quota"
```

---

### Test Case 3: Từ Chối Đăng Ký

**Bước 1-4:** Giống Test Case 1 (tạo slot và đăng ký)

**Bước 5:** Manager từ chối
```bash
PATCH /api/v1/admin/registrations/part-time/10/status
Header: Authorization: Bearer {manager_token}
Body: {
  "status":"REJECTED",
  "reason":"Schedule conflict with another project"
}
→ Status: REJECTED, reason hiển thị
```

**Bước 6:** Kiểm tra slot
```bash
GET /api/v1/work-slots
→ Slot ID: 20, registered=0 (KHÔNG tăng vì bị từ chối)
```

---

## 📊 CÁC TRẠNG THÁI (STATUS)

| Status | Ý Nghĩa | Đếm Quota? | Ai Thấy? |
|--------|---------|------------|----------|
| **PENDING** | Chờ manager duyệt | ❌ Không | Employee + Manager |
| **APPROVED** | Đã được duyệt | ✅ Có | Employee + Manager |
| **REJECTED** | Bị từ chối | ❌ Không | Employee + Manager |

---

## ⚠️ CÁC LỖI THƯỜNG GẶP

### 1. 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token expired or invalid"
}
```
**Giải pháp:** Login lại để lấy token mới

### 2. 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```
**Giải pháp:** Kiểm tra user có đủ permission không

### 3. 409 Conflict - Quota Exceeded (Khi tạo)
```json
{
  "status": 409,
  "detail": "Tất cả các ngày bạn yêu cầu đã đầy quota. Vui lòng chọn ngày khác."
}
```
**Giải pháp:** Chọn ngày khác hoặc slot khác

### 4. 409 Conflict - Quota Exceeded (Khi duyệt)
```json
{
  "status": 409,
  "detail": "Không thể duyệt: Một số ngày đã vượt quá quota. Ngày 2025-11-14: 3/3 đã đầy"
}
```
**Giải pháp:** Manager từ chối hoặc liên hệ nhân viên chọn ngày khác

### 5. 400 Bad Request - Thiếu reason khi reject
```json
{
  "status": 400,
  "detail": "Reason is required when rejecting a registration"
}
```
**Giải pháp:** Thêm `reason` vào request body

---

## 🎯 CHECKLIST KIỂM TRA

### Manager UI
- [ ] Tạo slot với 1 ngày được
- [ ] Tạo slot với nhiều ngày (comma-separated) được
- [ ] Xem danh sách slot với quota và registered count chính xác
- [ ] Xem danh sách đăng ký PENDING
- [ ] Duyệt đăng ký → Status chuyển APPROVED
- [ ] Từ chối đăng ký với reason → Status chuyển REJECTED
- [ ] Không thể duyệt khi quota đầy (hiện lỗi 409)

### Employee UI
- [ ] Xem danh sách slot available
- [ ] Tạo đăng ký với 1 ngày được
- [ ] Tạo đăng ký với nhiều ngày được
- [ ] Không thể tạo đăng ký khi tất cả ngày đều đầy (hiện lỗi 409)
- [ ] Xem đăng ký của mình với status PENDING
- [ ] Xem đăng ký APPROVED với processedBy
- [ ] Xem đăng ký REJECTED với reason

### Quota Logic
- [ ] Chỉ đăng ký APPROVED mới đếm vào registered count
- [ ] PENDING không đếm
- [ ] REJECTED không đếm
- [ ] Quota tính theo TỪNG NGÀY, không phải tổng

---

## 💡 LƯU Ý KHI PHÁT TRIỂN

1. **Token Expiration:** Token hết hạn sau một thời gian, cần handle refresh/re-login

2. **Date Format:** Luôn dùng format `YYYY-MM-DD` (ISO 8601)

3. **Day of Week:** 
   - Backend nhận: "MONDAY", "TUESDAY", ... (uppercase)
   - Có thể gửi array hoặc comma-separated string

4. **Quota Display:**
   ```
   Available = quota - registered
   ```

5. **Status Colors:**
   - PENDING: Màu vàng/cam (⏳)
   - APPROVED: Màu xanh lá (✅)
   - REJECTED: Màu đỏ (❌)

6. **Vietnamese Characters:** Đảm bảo encoding UTF-8 cho reason và messages

7. **Error Handling:** 
   - 409 Conflict cần hiển thị message rõ ràng cho user
   - Đề xuất action tiếp theo (chọn ngày khác, chọn slot khác)

---

## 📞 HỖ TRỢ

Nếu có vấn đề khi test API, liên hệ:
- Backend Team Lead
- Tham khảo source code tại: `PDCMS_BE/src/main/java/com/dental/clinic/management/working_schedule/`

**Ngày cập nhật:** 05/11/2025
**Phiên bản:** BE-403 - Dynamic Quota System
