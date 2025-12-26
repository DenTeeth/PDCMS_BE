# 📚 Warehouse-Service Integration Documentation

## 🎯 Giới Thiệu

Hệ thống tích hợp giữa **Clinical Records** (hồ sơ bệnh án), **Services** (dịch vụ nha khoa), và **Warehouse** (kho vật tư) để tự động tracking và trừ vật tư khi điều trị.

---

## 📖 Tài Liệu

### 🚀 Bắt đầu nhanh
**[00_QUICK_START_WAREHOUSE_SERVICE_INTEGRATION.md](00_QUICK_START_WAREHOUSE_SERVICE_INTEGRATION.md)**
- ✅ Tổng quan tính năng
- ✅ Luồng 3 bước đơn giản
- ✅ API chính
- ✅ FAQ

**👉 ĐỌC FILE NÀY TRƯỚC!**

---

### 📝 Dữ liệu test
**[01_TEST_DATA_SETUP.md](01_TEST_DATA_SETUP.md)**
- ✅ Dữ liệu có sẵn trong database seed
- ✅ Services đã có BOM (Bill of Materials)
- ✅ Vật tư trong kho (batches)
- ✅ Users & permissions
- ✅ Scenarios test mẫu
- ✅ SQL scripts để thêm dữ liệu

**👉 ĐỌC KHI CẦN BIẾT CÓ DATA GÌ ĐỂ TEST**

---

### 🔄 Luồng dữ liệu
**[02_DATA_FLOW_EXPLAINED.md](02_DATA_FLOW_EXPLAINED.md)**
- ✅ Sơ đồ chi tiết từng bước
- ✅ Code flow trong backend
- ✅ Database changes mỗi bước
- ✅ FEFO algorithm giải thích
- ✅ Transaction safety
- ✅ Performance considerations

**👉 ĐỌC KHI MUỐN HIỂU SÂU CÁCH HỆ THỐNG HOẠT ĐỘNG**

---

### 🧪 Hướng dẫn test API
**[03_API_TESTING_GUIDE.md](03_API_TESTING_GUIDE.md)**
- ✅ Postman collection ready
- ✅ Step-by-step test scenarios
- ✅ Request/response examples
- ✅ Verification queries
- ✅ Common issues & solutions

**👉 ĐỌC KHI BẮT ĐẦU TEST API**

---

### 🔐 Phân quyền
**[04_PERMISSIONS_GUIDE.md](04_PERMISSIONS_GUIDE.md)**
- ✅ Permissions chi tiết
- ✅ Role-based capabilities
- ✅ Permission matrix
- ✅ Testing permissions
- ✅ Security best practices

**👉 ĐỌC KHI CẦN HIỂU AI LÀM ĐƯỢC GÌ**

---

### 📋 Các tình huống mẫu
**[05_SAMPLE_SCENARIOS.md](05_SAMPLE_SCENARIOS.md)**
- ✅ Happy path: Trám răng
- ✅ Edge case: Thiếu vật tư
- ✅ Multi-procedure appointment
- ✅ FEFO depletion
- ✅ Negative variance

**👉 ĐỌC KHI MUỐN XEM EXAMPLES THỰC TẾ**

---

## 🔑 Key Concepts

### 1. BOM (Bill of Materials)
Định nghĩa vật tư cần thiết cho từng dịch vụ.

**Example:**
```
Dịch vụ "Trám răng Composite" cần:
├─ 1 đôi găng tay
├─ 8g Composite
└─ 5 giọt Bonding Agent
```

**Table:** `service_consumables`

---

### 2. FEFO (First Expired First Out)
Thuật toán tự động dùng lô hàng hết hạn sớm nhất trước.

**Example:**
```
Có 2 batches găng tay:
├─ Batch A: expires in 20 days
└─ Batch B: expires in 90 days

→ Dùng Batch A trước ✅
```

---

### 3. Material Deduction Trigger
Vật tư chỉ được trừ khi appointment status → **COMPLETED**.

```
SCHEDULED → CHECKED_IN → IN_PROGRESS → COMPLETED ⚡
                                           ↑
                                      Trigger point!
```

---

### 4. Variance Tracking
So sánh planned vs actual usage.

**Example:**
```
Planned: 8g composite
Actual: 10g composite
Variance: +2g (ADDITIONAL_USAGE)
```

---

## 🗂️ Database Tables

| Table | Purpose |
|-------|---------|
| `service_consumables` | BOM definition |
| `item_batches` | Warehouse stock |
| `procedure_material_usage` | Actual usage tracking |
| `clinical_record_procedures` | Procedure info + deduction status |

---

## 🔗 API Endpoints Summary

### 1. View Service BOM
```http
GET /api/v1/warehouse/service-consumables/{serviceId}
```

**Response:** Danh sách vật tư cần cho dịch vụ

---

### 2. View Procedure Materials
```http
GET /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Response:** Vật tư đã dùng (planned vs actual)

---

### 3. Update Actual Quantities
```http
PUT /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Request:** New actual quantities
**Response:** Stock adjustments

---

## 🎭 Roles & Permissions

| Role | View Materials | View Costs | Update Quantities |
|------|----------------|------------|-------------------|
| Admin | ✅ All | ✅ Yes | ✅ Yes |
| Doctor | ✅ Own | ❌ No | ✅ Own |
| Nurse | ✅ All | ❌ No | ✅ Yes |
| Accountant | ✅ All | ✅ Yes | ❌ No |

---

## 🧪 Quick Test

### Minimal Test (5 phút)

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Create appointment (service_id=5: Trám răng)
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "serviceId": 5,
    "employeeId": 1,
    "roomId": 1,
    "appointmentStartTime": "2025-12-27T10:00:00"
  }'

# 3. Complete appointment
curl -X PUT http://localhost:8080/api/v1/appointments/{id}/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"newStatus":"COMPLETED"}'

# 4. Verify materials deducted
curl -X GET http://localhost:8080/api/v1/clinical-records/procedures/{id}/materials \
  -H "Authorization: Bearer <token>"
```

**Expected:** `materialsDeducted: true`, warehouse stock decreased

---

## 🐛 Troubleshooting

### Materials không tự động trừ?

**Check:**
1. ✅ Appointment status = COMPLETED?
2. ✅ Service có BOM?
3. ✅ Clinical record được tạo?
4. ✅ Procedure được thêm?
5. ✅ Check logs có lỗi không?

**Debug Query:**
```sql
SELECT 
  a.status,
  p.procedure_id,
  p.materials_deducted_at,
  s.service_code
FROM appointments a
LEFT JOIN clinical_records cr ON cr.appointment_id = a.appointment_id
LEFT JOIN clinical_record_procedures p ON p.clinical_record_id = cr.clinical_record_id
LEFT JOIN services s ON s.service_id = p.service_id
WHERE a.appointment_id = ?;
```

---

### Costs hiện null?

**Cause:** User không có `VIEW_WAREHOUSE_COST` permission

**Solution:** 
- Login as Admin hoặc Accountant
- Hoặc grant permission cho user

---

## 📞 Support

- **Slack:** #backend-support
- **Email:** backend-team@dental.com
- **Documentation:** Full API spec tại `PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md`

---

## 🗺️ Reading Path

### For FE Developer (First Time)
```
1. 00_QUICK_START (10 phút)
2. 01_TEST_DATA_SETUP (15 phút)
3. 03_API_TESTING_GUIDE (30 phút)
4. Start testing! 🚀
```

### For Backend Understanding
```
1. 00_QUICK_START
2. 02_DATA_FLOW_EXPLAINED (Chi tiết!)
3. 04_PERMISSIONS_GUIDE
4. 05_SAMPLE_SCENARIOS
```

### For QA Testing
```
1. 00_QUICK_START
2. 01_TEST_DATA_SETUP
3. 03_API_TESTING_GUIDE
4. 05_SAMPLE_SCENARIOS (Test cases!)
```

---

## 📅 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-26 | Initial documentation split into 5 files |
| - | - | Covers: Quick start, test data, flow, API, permissions, scenarios |

---

## ✅ Checklist

Trước khi bắt đầu test:
- [ ] Database đã chạy seed script
- [ ] Backend đang chạy (port 8080)
- [ ] Đã đọc `00_QUICK_START.md`
- [ ] Đã đọc `01_TEST_DATA_SETUP.md`
- [ ] Có Postman hoặc cURL ready
- [ ] Có token JWT valid

**Ready to test!** 🎉

---

## 📖 Related Docs

- **Main API Guide:** `../PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md`
- **Warehouse Module:** `../WAREHOUSE_MODULE_API_REFERENCE.md`
- **Architecture:** `../architecture/CLINICAL_RECORDS_MODULE_ANALYSIS.md`

---

**Last updated:** December 26, 2025
