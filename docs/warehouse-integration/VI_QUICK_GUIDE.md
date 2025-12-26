# 🇻🇳 Hướng Dẫn Nhanh - Tích Hợp Kho & Dịch Vụ

## 📌 TL;DR (Quá Dài Không Đọc)

**Tính năng gì?** Tự động trừ vật tư từ kho khi hoàn thành dịch vụ.

**Ai dùng?** Bác sĩ, Y tá, Kế toán, Admin.

**Khi nào trừ kho?** Khi appointment status → **COMPLETED**.

**Test nhanh:** Xem file [03_API_TESTING_GUIDE.md](03_API_TESTING_GUIDE.md)

---

## 🎯 Vấn Đề Cần Giải Quyết

### Trước Đây (Manual)
```
1. Bác sĩ điều trị xong
2. Y tá phải nhớ đã dùng vật tư gì
3. Kế toán phải tự nhập vào Excel
4. Quản kho phải trừ thủ công
5. Dễ quên, sai số liệu ❌
```

### Bây Giờ (Automatic)
```
1. Bác sĩ click "Hoàn thành" ⚡
2. Hệ thống tự động:
   ├─ Lấy BOM của dịch vụ
   ├─ Trừ vật tư từ kho (FEFO)
   ├─ Ghi nhận vào hồ sơ
   └─ Y tá chỉ cần xem & điều chỉnh (nếu cần)
3. Chính xác 100% ✅
```

---

## 🔑 Khái Niệm Cơ Bản

### 1. BOM (Bill of Materials)
**Định nghĩa:** Danh sách vật tư cần cho từng dịch vụ.

**Ví dụ:**
```
Dịch vụ: "Trám răng Composite"
Cần:
├─ 1 đôi găng tay
├─ 1 cái khẩu trang
├─ 8 gram Composite
├─ 3 ml Etching Gel
└─ 5 giọt Bonding Agent
```

**Lưu ở đâu?** Bảng `service_consumables`

**Ai quản lý?** Admin, Warehouse Manager

---

### 2. FEFO (First Expired First Out)
**Định nghĩa:** Dùng lô hàng hết hạn sớm nhất trước.

**Tại sao?** Tránh lãng phí vật tư hết hạn.

**Ví dụ:**
```
Kho có 2 lô găng tay:
├─ Lô A: 30 đôi, hết hạn 15/01/2026 (còn 20 ngày)
└─ Lô B: 150 đôi, hết hạn 25/03/2026 (còn 90 ngày)

Dùng 1 đôi → Lấy từ Lô A ✅
```

---

### 3. Variance (Chênh Lệch)
**Định nghĩa:** Chênh lệch giữa dự kiến và thực tế.

**Ví dụ:**
```
Dự kiến: 8g composite
Thực tế: 10g composite
Chênh lệch: +2g (DÙNG THÊM)

Lý do: Sâu răng sâu hơn dự kiến
```

---

## 🔄 Luồng Hoạt Động (Đơn Giản)

```
┌─────────────────────────────────────────────────────────┐
│                   LUỒNG ĐIỀU TRỊ                        │
└─────────────────────────────────────────────────────────┘

1. Lễ tân tạo appointment
   ↓
2. Bệnh nhân check-in
   ↓
3. Bác sĩ bắt đầu điều trị (IN_PROGRESS)
   ├─ Tạo hồ sơ bệnh án (clinical record)
   ├─ Thêm procedure (trám răng số 46)
   └─ Kho: CHƯA TRỪ ⚠️
   ↓
4. Bác sĩ hoàn thành (COMPLETED) ⚡
   └─ Hệ thống TỰ ĐỘNG:
       ├─ Lấy BOM của dịch vụ
       ├─ Trừ vật tư từ kho (theo FEFO)
       ├─ Ghi nhận vào procedure_material_usage
       └─ Kho: ĐÃ TRỪ ✅
   ↓
5. Y tá kiểm tra & điều chỉnh (optional)
   └─ Nếu thực tế khác dự kiến:
       ├─ Cập nhật actual_quantity
       └─ Kho tự động điều chỉnh
```

---

## 📊 Bảng Database Liên Quan

| Bảng | Công Dụng | Ví Dụ |
|------|-----------|-------|
| `service_consumables` | Định nghĩa BOM | "Trám răng cần 8g composite" |
| `item_batches` | Tồn kho theo lô | "Lô ABC còn 35g, hết hạn 2026-06-15" |
| `procedure_material_usage` | Ghi nhận đã dùng | "Procedure 123 dùng 10g composite" |
| `clinical_record_procedures` | Thông tin procedure | "Trám răng số 46, đã trừ kho lúc 10:30" |

---

## 🧪 Test Nhanh (5 Phút)

### Bước 1: Login
```bash
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### Bước 2: Tạo appointment (trám răng)
```bash
POST /api/v1/appointments
{
  "patientId": 1,
  "serviceId": 5,  # Trám răng Composite
  "employeeId": 1,
  "roomId": 1,
  "appointmentStartTime": "2025-12-27T10:00:00"
}
```

### Bước 3: Hoàn thành
```bash
PUT /api/v1/appointments/{id}/status
{ "newStatus": "COMPLETED" }
```

### Bước 4: Kiểm tra kho đã trừ
```bash
GET /api/v1/clinical-records/procedures/{id}/materials
```

**Kết quả mong đợi:**
- `materialsDeducted: true`
- Danh sách vật tư đã dùng
- Kho đã giảm số lượng

---

## 🔐 Phân Quyền (Ai Làm Được Gì?)

| Người Dùng | Xem Vật Tư | Xem Giá | Cập Nhật Số Lượng |
|------------|------------|---------|-------------------|
| 👔 Admin | ✅ Tất cả | ✅ Có | ✅ Có |
| 👨‍⚕️ Bác sĩ | ✅ Của mình | ❌ Không | ✅ Của mình |
| 👩‍⚕️ Y tá | ✅ Tất cả | ❌ Không | ✅ Có |
| 💰 Kế toán | ✅ Tất cả | ✅ Có | ❌ Không |
| 📝 Lễ tân | ✅ Hạn chế | ❌ Không | ❌ Không |

**Lưu ý:** Chỉ Admin & Kế toán xem được giá!

---

## 📝 Dữ Liệu Test Có Sẵn

### Dịch vụ có BOM (dùng test)
```
1. Khám tổng quát (service_id = 1)
   └─ Cần: 1 găng tay, 1 khẩu trang

3. Lấy cao răng (service_id = 3)
   └─ Cần: 2 găng tay, 1 khẩu trang, 3 gạc

5. Trám răng Composite (service_id = 5) ⭐ HAY DÙNG
   └─ Cần: 1 găng tay, 8g composite, 5 giọt keo...
```

### Vật tư trong kho
```
Găng tay (CON-GLOVE-01):
├─ Lô 1: 30 đôi (hết hạn 20 ngày nữa)
└─ Lô 2: 150 đôi (hết hạn 90 ngày nữa)

Composite (MAT-COMP-01):
└─ Lô 1: 35 gram (hết hạn 200 ngày nữa)
```

---

## ❓ Câu Hỏi Thường Gặp

### Q1: Khi nào kho tự động trừ?
**A:** Khi appointment status chuyển sang **COMPLETED**.

Trước đó (SCHEDULED, CHECKED_IN, IN_PROGRESS): Kho không đổi.

---

### Q2: Có thể sửa số lượng sau không?
**A:** Có! Y tá/bác sĩ dùng API này:
```
PUT /api/v1/clinical-records/procedures/{id}/materials
```

Kho sẽ tự động điều chỉnh.

---

### Q3: Nếu kho hết vật tư thì sao?
**A:** 
- ❌ Vật tư **KHÔNG** được trừ
- ✅ Appointment vẫn COMPLETED
- 📋 Log lỗi: "Insufficient stock"
- 👉 Cần nhập hàng và trừ thủ công sau

---

### Q4: FEFO là gì?
**A:** First Expired First Out = Dùng lô hết hạn sớm nhất trước.

Tránh vật tư bị hết hạn nằm kho.

---

### Q5: Tại sao tôi không thấy giá vật tư?
**A:** Bạn không có permission `VIEW_WAREHOUSE_COST`.

Chỉ Admin & Kế toán mới xem được giá.

---

### Q6: Data test ở đâu?
**A:** 
1. File seed: `src/main/resources/db/dental-clinic-seed-data.sql`
2. Đọc: [01_TEST_DATA_SETUP.md](01_TEST_DATA_SETUP.md)

---

## 🐛 Lỗi Thường Gặp

### Lỗi 1: Materials không tự động trừ

**Nguyên nhân:**
1. Service chưa có BOM
2. Clinical record chưa được tạo
3. Procedure chưa được thêm

**Cách fix:**
```sql
-- Kiểm tra service có BOM không
SELECT COUNT(*) FROM service_consumables WHERE service_id = 5;
-- Nếu = 0 → Chưa có BOM, cần thêm
```

---

### Lỗi 2: 403 Forbidden

**Nguyên nhân:** Không có permission

**Cách fix:**
1. Login bằng user khác (có permission)
2. Hoặc grant permission cho user hiện tại

---

### Lỗi 3: Giá hiện null

**Nguyên nhân:** User không có `VIEW_WAREHOUSE_COST`

**Cách fix:** Login bằng admin hoặc accountant

---

## 📚 Tài Liệu Đầy Đủ

### Đọc theo thứ tự (cho FE)
1. ✅ **README.md** (bạn đang đọc)
2. ✅ **[00_QUICK_START](00_QUICK_START_WAREHOUSE_SERVICE_INTEGRATION.md)** - Tổng quan
3. ✅ **[01_TEST_DATA_SETUP](01_TEST_DATA_SETUP.md)** - Dữ liệu test
4. ✅ **[03_API_TESTING_GUIDE](03_API_TESTING_GUIDE.md)** - Hướng dẫn test API
5. ⭐ **Bắt đầu test!**

### Đọc thêm (nếu cần)
- **[02_DATA_FLOW_EXPLAINED](02_DATA_FLOW_EXPLAINED.md)** - Luồng chi tiết
- **[04_PERMISSIONS_GUIDE](04_PERMISSIONS_GUIDE.md)** - Phân quyền
- **[05_SAMPLE_SCENARIOS](05_SAMPLE_SCENARIOS.md)** - Các tình huống mẫu

---

## 🎯 Checklist Trước Khi Test

- [ ] Backend đang chạy (localhost:8080)
- [ ] Database đã có seed data
- [ ] Đã đọc Quick Start
- [ ] Đã đọc Test Data Setup
- [ ] Có Postman/Insomnia ready
- [ ] Có token JWT

**Sẵn sàng!** 🚀

---

## 📞 Hỗ Trợ

- **Slack:** #backend-support
- **Email:** backend-team@dental.com
- **Bug Report:** Tạo issue trên Jira/GitLab

---

**Chúc test vui vẻ!** 😊
