# 🚀 Quick Start: Warehouse-Service Integration

## Tổng Quan Nhanh (Quick Overview)

**Tính năng gì?** Tự động trừ vật tư từ kho khi hoàn thành dịch vụ nha khoa.

**Ai dùng?**
- 👨‍⚕️ **Bác sĩ**: Xem vật tư đã dùng
- 👩‍⚕️ **Y tá/Phụ tá**: Cập nhật số lượng thực tế
- 💰 **Kế toán**: Xem chi phí vật tư
- 👔 **Admin**: Quản lý toàn bộ

---

## ⚡ Luồng Hoạt Động 3 Bước

### Bước 1: Định nghĩa BOM (Bill of Materials)
```
Dịch vụ "Trám răng" cần gì?
├─ 8g Composite
├─ 3ml Etching Gel  
├─ 5 giọt Bonding Agent
└─ 2 gói Gạc
```

### Bước 2: Hoàn thành lịch hẹn
```
Appointment Status: COMPLETED ⚡
  ↓
Tự động trừ vật tư từ kho (FEFO)
  ↓
Ghi nhận vào procedure_material_usage
```

### Bước 3: Điều chỉnh (nếu cần)
```
Y tá: "Thực tế dùng 10g Composite, không phải 8g"
  ↓
PUT /api/v1/clinical-records/procedures/{id}/materials
  ↓
Kho tự động điều chỉnh (+2g)
```

---

## 📊 Bảng Liên Quan

| Bảng | Mục đích |
|------|----------|
| `service_consumables` | Định nghĩa BOM cho từng dịch vụ |
| `item_batches` | Tồn kho thực tế (theo lô hàng) |
| `procedure_material_usage` | Ghi nhận vật tư đã dùng |
| `storage_transactions` | Lịch sử xuất/nhập kho |

---

## 🔑 API Chính

### 1. Xem BOM của dịch vụ
```http
GET /api/v1/warehouse/service-consumables/{serviceId}
```

### 2. Xem vật tư đã dùng cho procedure
```http
GET /api/v1/clinical-records/procedures/{procedureId}/materials
```

### 3. Cập nhật số lượng thực tế
```http
PUT /api/v1/clinical-records/procedures/{procedureId}/materials
```

---

## 🧪 Test Nhanh

### Dữ liệu mẫu có sẵn:
```sql
-- Service đã có BOM
service_id = 5 (Trám răng Composite - FILLING_COMP)

-- Vật tư có sẵn trong kho
- CON-GLOVE-01: Găng tay (150 đôi)
- MAT-COMP-01: Composite (35g)
- MAT-BOND-01: Bonding Agent (45ml)
```

### Test Flow:
1. Tạo appointment với `service_id = 5`
2. Thêm procedure vào clinical record
3. Hoàn thành appointment (status → COMPLETED)
4. Kiểm tra kho đã trừ vật tư chưa

📖 **Chi tiết**: Xem file `01_TEST_DATA_SETUP.md`

---

## 🔐 Permissions Cần Thiết

| Hành động | Permission |
|-----------|-----------|
| Xem vật tư (không có giá) | `VIEW_CLINICAL_RECORD` |
| Xem giá vật tư | `VIEW_WAREHOUSE_COST` |
| Cập nhật số lượng | `WRITE_CLINICAL_RECORD` |
| Quản lý BOM | `MANAGE_WAREHOUSE` |

📖 **Chi tiết**: Xem file `04_PERMISSIONS_GUIDE.md`

---

## ❓ Câu Hỏi Thường Gặp

**Q: Kho trừ vật tư khi nào?**
A: Tự động khi appointment status → COMPLETED

**Q: Có thể sửa số lượng sau không?**
A: Có! Dùng API 8.8 để cập nhật actual_quantity

**Q: Nếu thiếu vật tư trong kho?**
A: Hệ thống báo lỗi "Insufficient stock", không block hoàn thành appointment

**Q: FEFO là gì?**
A: First Expired First Out - dùng lô hết hạn sớm nhất trước

---

## 📚 Các File Hướng Dẫn Khác

1. ✅ **00_QUICK_START** (bạn đang đọc)
2. 📝 **01_TEST_DATA_SETUP** - Dữ liệu test chi tiết
3. 🔄 **02_DATA_FLOW_EXPLAINED** - Luồng dữ liệu
4. 🧪 **03_API_TESTING_GUIDE** - Test API từng bước
5. 🔐 **04_PERMISSIONS_GUIDE** - Phân quyền chi tiết
6. 📋 **05_SAMPLE_SCENARIOS** - Các tình huống mẫu

---

## 🆘 Cần Giúp Đỡ?

- Slack: #backend-support
- Email: backend-team@dental.com
- Doc chính: `PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md`
