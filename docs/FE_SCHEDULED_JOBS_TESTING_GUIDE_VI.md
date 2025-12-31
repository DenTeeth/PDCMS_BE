# 🔧 Thông Báo Sửa Lỗi: Scheduled Jobs (Cron Jobs) - Dành Cho FE Team

**Ngày:** 31/12/2025  
**Trạng thái:** ✅ Đã hoàn thành - Sẵn sàng production  

---

## 📋 Tóm Tắt

### Vấn Đề Đã Fix

Các **scheduled jobs (cron jobs/bots)** không chạy trên production environment vì:
- ❌ Timezone không được cấu hình → jobs chạy sai giờ hoặc không chạy
- ❌ Docker container dùng UTC thay vì giờ Việt Nam
- ❌ Thiếu thread pool → jobs có thể bị chặn lẫn nhau
- ❌ Không có logging → không thể debug

### Đã Sửa Xong

✅ **11 scheduled jobs** bây giờ đã hoạt động bình thường:

1. **Auto tạo ca làm việc** (00:01 AM hàng ngày)
2. **Bot nhắc gia hạn hợp đồng** (00:05 AM hàng ngày) ⭐
3. **Bot nhắc manager phê duyệt requests** (09:00 AM hàng ngày) ⭐
4. **Email cảnh báo hàng sắp hết hạn** (08:00 AM hàng ngày)
5. Và 7 jobs cleanup/maintenance khác...

---

## 🎯 Điều FE Team Cần Biết

### 1. **Không Cần Thay Đổi Gì Ở Frontend**

✅ Tất cả thay đổi chỉ ở backend  
✅ API endpoints vẫn giữ nguyên  
✅ Response format không đổi  
✅ Frontend code **KHÔNG** cần update  

### 2. **Điều Gì Sẽ Hoạt Động Tự Động**

Sau khi deploy backend mới:

- ✅ **Ca làm việc tự động được tạo** mỗi ngày lúc 00:01 AM
  - Tạo cho 14 ngày tiếp theo
  - Dựa trên Fixed và Flex registrations
  
- ✅ **Thông báo gia hạn hợp đồng** tự động gửi (00:05 AM)
  - Cho nhân viên có hợp đồng sắp hết hạn (14-28 ngày)
  - Tạo renewal requests trong bảng `shift_renewal_requests`
  
- ✅ **Notifications nhắc manager** tự động gửi (09:00 AM)
  - Nhắc approve overtime requests
  - Nhắc approve time-off requests  
  - Nhắc approve registration requests

- ✅ **Email cảnh báo warehouse** tự động gửi (08:00 AM)
  - Hàng sắp hết hạn trong 5/15/30 ngày

### 3. **Features Cần Test Trên Production**

#### A) **Dashboard/Schedule Page**
```
Mở trang lịch làm việc:
✅ Kiểm tra có ca làm việc cho 14 ngày tới không
✅ Các ca phải hiển thị với status = "SCHEDULED"
✅ Nguồn (source) phải là "BATCH_JOB" hoặc "REGISTRATION_JOB"
```

#### B) **Notifications Page**  
```
Mở trang notifications:
✅ Managers sẽ nhận notifications lúc 9h sáng
✅ Thông báo nhắc về pending requests (overtime, time-off, registration)
✅ Type: REQUEST_OVERTIME_PENDING, REQUEST_TIME_OFF_PENDING, etc.
```

#### C) **Employee Profile/Contract Renewal**
```
Kiểm tra nhân viên có hợp đồng sắp hết hạn:
✅ Renewal request tự động được tạo
✅ Hiển thị trong danh sách renewal requests
✅ Trạng thái = PENDING
```

#### D) **Warehouse/Inventory Page**
```
Nếu có quyền xem warehouse:
✅ Users sẽ nhận email cảnh báo hàng sắp hết hạn
✅ Email gửi lúc 8h sáng
✅ Phân loại theo mức độ: CRITICAL (5 ngày), WARNING (15 ngày), INFO (30 ngày)
```

---

## 🧪 Hướng Dẫn Test Trên Production

### **Bước 1: Kiểm Tra Backend Đã Deploy**

Hỏi backend team confirm đã deploy code mới chưa. Hoặc check logs:

```bash
# Backend team sẽ chạy:
docker logs dentalclinic-app | grep "TaskScheduler initialized"

# Phải thấy: "✅ TaskScheduler initialized successfully"
```

### **Bước 2: Test Thông Qua Frontend**

#### **Test 1: Ca Làm Việc Tự Động**

1. Login với tài khoản **Admin** hoặc **Manager**
2. Vào trang **Schedule/Lịch Làm Việc**
3. Chọn xem lịch cho **14 ngày tới**
4. ✅ **Kỳ vọng:** Thấy ca làm việc đã được tạo sẵn

**API endpoint FE đang dùng:**
```
GET /api/v1/employee-shifts?startDate={today}&endDate={today+14}
```

**Response mẫu:**
```json
[
  {
    "employeeShiftId": "EMS251231001",
    "employeeId": 1,
    "workShiftId": "WKS_MORNING_01",
    "workDate": "2025-12-31",
    "status": "SCHEDULED",
    "source": "BATCH_JOB",  // ← Từ scheduled job
    "isOvertime": false
  }
]
```

#### **Test 2: Notifications Tự Động**

1. Login với tài khoản **Admin**
2. Vào trang **Notifications** 
3. Sau **9h sáng**, refresh trang
4. ✅ **Kỳ vọng:** Thấy notifications mới về pending requests (nếu có requests pending)

**API endpoint FE đang dùng:**
```
GET /api/v1/notifications?userId={adminId}
```

**Response mẫu:**
```json
[
  {
    "notificationId": "NTF251231001",
    "userId": 1,
    "type": "REQUEST_OVERTIME_PENDING",
    "title": "Nhắc nhở: Phê duyệt yêu cầu tăng ca",
    "message": "Yêu cầu tăng ca của Nguyễn Văn A cho ngày 01/01/2026...",
    "createdAt": "2025-12-31T09:00:15",
    "isRead": false
  }
]
```

#### **Test 3: Contract Renewal**

1. Login với tài khoản có quyền xem renewal requests
2. Vào trang **Shift Renewal Requests** (nếu có)
3. ✅ **Kỳ vọng:** Thấy renewal requests cho nhân viên có hợp đồng sắp hết hạn

**API endpoint FE có thể dùng:**
```
GET /api/v1/admin/shift-renewals
hoặc
GET /api/v1/employee/my-renewals (cho employee)
```

### **Bước 3: Test Manual (Không Cần Đợi Scheduled Time)**

Backend đã cung cấp **test endpoints** để FE team có thể test ngay:

#### **Trigger Auto Shift Creation Ngay**

```bash
curl -X GET "https://your-production-domain.com/api/v1/admin/test/scheduled-jobs/trigger-sync" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**Sau khi chạy:**
- Refresh trang Schedule
- Phải thấy ca mới được tạo cho 14 ngày tới

#### **Trigger Contract Renewal Bot Ngay**

```bash
curl -X GET "https://your-production-domain.com/api/v1/admin/test/scheduled-jobs/trigger-renewal-detection" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**Sau khi chạy:**
- Check trang renewal requests
- Phải thấy renewal requests mới (nếu có hợp đồng sắp hết hạn)

#### **Trigger Request Reminder Bot Ngay**

```bash
curl -X GET "https://your-production-domain.com/api/v1/admin/test/scheduled-jobs/trigger-request-reminders" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

**Sau khi chạy:**
- Refresh trang Notifications
- Phải thấy notifications mới về pending requests

---

## 📱 Test Bằng Postman (Dễ Hơn)

### **Setup:**

1. Import collection này vào Postman
2. Tạo environment variable:
   - `base_url` = `https://your-production-domain.com`
   - `admin_token` = JWT token của admin (lấy từ login response)

### **Collection:**

```json
{
  "name": "Scheduled Jobs Testing",
  "requests": [
    {
      "name": "1. List All Jobs",
      "method": "GET",
      "url": "{{base_url}}/api/v1/admin/test/scheduled-jobs/list",
      "headers": {
        "Authorization": "Bearer {{admin_token}}"
      }
    },
    {
      "name": "2. Trigger Auto Shift Creation",
      "method": "GET",
      "url": "{{base_url}}/api/v1/admin/test/scheduled-jobs/trigger-sync",
      "headers": {
        "Authorization": "Bearer {{admin_token}}"
      }
    },
    {
      "name": "3. Trigger Contract Renewal Bot",
      "method": "GET",
      "url": "{{base_url}}/api/v1/admin/test/scheduled-jobs/trigger-renewal-detection",
      "headers": {
        "Authorization": "Bearer {{admin_token}}"
      }
    },
    {
      "name": "4. Trigger Request Reminders",
      "method": "GET",
      "url": "{{base_url}}/api/v1/admin/test/scheduled-jobs/trigger-request-reminders",
      "headers": {
        "Authorization": "Bearer {{admin_token}}"
      }
    },
    {
      "name": "5. Trigger ALL Jobs",
      "method": "GET",
      "url": "{{base_url}}/api/v1/admin/test/scheduled-jobs/trigger-all",
      "headers": {
        "Authorization": "Bearer {{admin_token}}"
      }
    }
  ]
}
```

---

## ⏰ Lịch Chạy Tự Động (Production)

| Giờ | Job | FE Có Thể Thấy Gì |
|-----|-----|-------------------|
| 00:01 AM | Auto tạo ca | Ca làm việc mới trong Schedule page |
| 00:05 AM | Contract renewal bot | Renewal requests mới xuất hiện |
| 08:00 AM | Warehouse email | Admin warehouse nhận email (không qua FE) |
| 09:00 AM | Request reminders | Notifications mới trong Notifications page |

**Lưu ý:** Tất cả giờ theo **giờ Việt Nam (GMT+7)**

---

## ✅ Checklist Cho FE Team

### **Ngay Sau Khi Backend Deploy:**

- [ ] Backend team confirm đã deploy code mới
- [ ] Kiểm tra logs thấy "TaskScheduler initialized successfully"
- [ ] Test manual bằng test endpoints (xem phần trên)

### **Trong 24h Đầu:**

- [ ] Sáng hôm sau (sau 00:01 AM), check xem có ca mới được tạo không
- [ ] Lúc 9h sáng, check xem có notifications mới không (nếu có pending requests)
- [ ] Hỏi user có nhận email warehouse không (nếu có hàng sắp hết hạn)

### **Nếu Có Vấn Đề:**

- [ ] Chụp screenshot lỗi/vấn đề
- [ ] Check browser console có error không
- [ ] Check Network tab xem API response
- [ ] Báo cho backend team với thông tin chi tiết

---

## 🚨 Lưu Ý Quan Trọng

### **1. Test Endpoints Chỉ Dùng Để Test**

⚠️ **KHÔNG** gọi test endpoints từ frontend code  
⚠️ **KHÔNG** dùng test endpoints trong production workflow  
⚠️ **CHỈ** dùng để test/debug thủ công  

### **2. Jobs Chạy Tự Động**

✅ Không cần FE trigger  
✅ Không cần user làm gì  
✅ Backend tự động chạy theo lịch  

### **3. Timezone**

✅ Tất cả giờ đều theo giờ Việt Nam (GMT+7)  
✅ Không cần convert timezone ở frontend  
✅ Timestamps từ API vẫn theo định dạng ISO 8601  

---

## 📞 Liên Hệ & Hỗ Trợ

### **Nếu FE Gặp Vấn Đề:**

1. **Check API response trước:**
   - Mở DevTools → Network tab
   - Xem response từ API có đúng format không
   - Copy response gửi cho backend team

2. **Báo cho backend team:**
   - Mô tả vấn đề cụ thể
   - Kèm screenshot
   - Kèm API response (nếu có)
   - Kèm browser console errors (nếu có)

3. **Thông tin cần cung cấp:**
   - User role đang test (Admin/Manager/Employee)
   - Trang đang test (Schedule/Notifications/etc.)
   - Thời gian xảy ra vấn đề
   - Expected vs Actual behavior

### **Backend Team Contact:**

- Slack: #backend-team
- Email: backend@dental.com
- Ticket: Jira PROJECT-XXX

---

## 📚 Tài Liệu Bổ Sung

### **Cho FE Developer:**

- **API Documentation:** `docs/API_DOCUMENTATION.md`
- **Notification System Guide:** `docs/NOTIFICATION_SYSTEM_FE_BE_INTEGRATION_GUIDE.md`
- **Full Scheduled Jobs Guide:** `docs/SCHEDULED_JOBS_COMPLETE_GUIDE.md` (chi tiết kỹ thuật)

### **Testing Resources:**

- **Test Endpoints:** Xem phần "Test Manual" ở trên
- **Postman Collection:** Xem phần "Test Bằng Postman" ở trên
- **API Samples:** `docs/API_ENDPOINTS_WITH_FUNCTION_NAMES_AND_SAMPLES.md`

---

## 🎯 Tóm Tắt Nhanh

### **Điều FE Cần Làm:**

1. ✅ **KHÔNG** cần thay đổi code frontend
2. ✅ Test xem ca làm việc tự động được tạo chưa
3. ✅ Test xem notifications tự động được gửi chưa
4. ✅ Báo backend nếu có vấn đề

### **Điều FE KHÔNG Cần Làm:**

❌ Thay đổi code  
❌ Trigger scheduled jobs từ frontend  
❌ Xử lý timezone conversion  
❌ Lo lắng về backend logic  

### **Timeline:**

- **Hôm nay:** Backend deploy code mới
- **Ngày mai sáng:** Jobs bắt đầu chạy tự động
- **24-48h:** Monitor và confirm mọi thứ hoạt động ổn

---

**Prepared by:** Backend Team  
**Date:** 31/12/2025  
**Status:** ✅ Sẵn sàng cho production testing

**Có câu hỏi? Hỏi ngay trên Slack #backend-team!** 🚀
