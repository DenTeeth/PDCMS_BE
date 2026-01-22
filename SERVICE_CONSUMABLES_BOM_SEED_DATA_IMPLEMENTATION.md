# Yêu Cầu Thêm Seed Data Cho Service Consumables (BOM)

## 📋 Tổng Quan

Hiện tại trong seed data chỉ có **1 service** (`SCALING_L1` - Cạo vôi răng) có đầy đủ vật tư tiêu hao (BOM) để test tính năng quản lý vật tư thủ thuật. Để demo và test đầy đủ tính năng này, cần thêm BOM cho nhiều service khác.

## 🎯 Mục Tiêu

Thêm Bill of Materials (BOM) cho các service khác trong seed data, sử dụng các vật tư đã có sẵn trong `item_masters` và `item_units`.

## 📊 Hiện Trạng

### Services Đã Có BOM:
1. **GEN_EXAM** - Khám tổng quát
   - Găng tay (CON-GLOVE-01): 1 đôi
   - Khẩu trang (CON-MASK-01): 1 cái

2. **SCALING_L1** - Cạo vôi răng
   - Găng tay (CON-GLOVE-01): 2 đôi
   - Khẩu trang (CON-MASK-01): 1 cái
   - Gạc (CON-GAUZE-01): 3 gói
   - Bột đánh bóng (MAT-POL-01): 15g

3. **FILLING_COMP** - Trám composite
   - Găng tay (CON-GLOVE-01): 1 đôi
   - Khẩu trang (CON-MASK-01): 1 cái
   - Gạc (CON-GAUZE-01): 2 gói
   - Composite (MAT-COMP-01): 8g
   - Etch gel (MAT-ETCH-01): 3ml
   - Bonding (MAT-BOND-01): 5 drop

4. **EXTRACT_MILK** - Nhổ răng sữa
   - Găng tay (CON-GLOVE-01): 1 đôi
   - Gạc (CON-GAUZE-01): 5 gói
   - Gel tê (MED-GEL-01): 1g

### Vật Tư Có Sẵn Trong Seed Data:
- **CON-GLOVE-01** - Găng tay (Đơn vị: Đôi)
- **CON-MASK-01** - Khẩu trang (Đơn vị: Cái)
- **CON-GAUZE-01** - Gạc (Đơn vị: Gói)
- **MAT-POL-01** - Bột đánh bóng (Đơn vị: g)
- **MAT-COMP-01** - Composite (Đơn vị: g)
- **MAT-ETCH-01** - Etch gel (Đơn vị: ml)
- **MAT-BOND-01** - Bonding (Đơn vị: drop)
- **MED-GEL-01** - Gel tê (Đơn vị: g)

## ⚠️ KIỂM TRA DATABASE - ĐÃ GIẢI QUYẾT

**BE đã kiểm tra database và phát hiện lỗi trong seed data SQL:**

### ❌ Vấn Đề Gốc (Root Cause):
- **Lỗi:** Tất cả INSERT statements cho `item_units` có `ON CONFLICT (item_master_id, unit_name) DO NOTHING`
- **Nguyên nhân:** Table `item_units` KHÔNG có unique constraint trên `(item_master_id, unit_name)`
- **Hậu quả:** Tất cả INSERT statements thất bại âm thầm → Chỉ có 4/17 units được tạo
- **Ảnh hưởng:** Service_consumables JOIN thất bại → Chỉ có Gauze, Bonding, Etch được insert

### ✅ Giải Pháp Đã Thực Hiện:
1. **Xóa tất cả** `ON CONFLICT (item_master_id, unit_name) DO NOTHING` clauses
2. **Thêm units** cho MAT-POL-01 (Bột đánh bóng) và MED-GEL-01 (Gel tê)
3. **Restart application** → Seed data tải lại đầy đủ

---

## 📊 KẾT QUẢ SAU KHI FIX

### Database Statistics:
- **14 services** có BOM (tăng từ 3)
- **57 consumable entries** (tăng từ 5)
- **17 item units** (tăng từ 4)

| Service Code Yêu Cầu | Trạng Thái | Service Code Thực Tế | Số Items | Ghi Chú |
|----------------------|------------|----------------------|----------|---------|
| ❌ ROOT_CANAL | KHÔNG TỒN TẠI | ✅ ENDO_TREAT_ANT | 6 items | Găng tay, khẩu trang, gạc, bonding, composite, etch |
| ❌ ROOT_CANAL | KHÔNG TỒN TẠI | ✅ ENDO_TREAT_POST | 6 items | Găng tay, khẩu trang, gạc, bonding, composite, etch |
| ❌ CROWN_PREP | KHÔNG TỒN TẠI | ⚠️ Không có tương đương | - | Service không tồn tại |
| ✅ ORTHO_CONSULT | TỒN TẠI | ✅ ORTHO_CONSULT | 3 items | Găng tay, khẩu trang, gạc |
| ❌ XRAY_PANORAMIC | KHÔNG TỒN TẠI | ⚠️ GEN_XRAY_PERI | - | Chưa thêm BOM |
| ❌ WHITENING | KHÔNG TỒN TẠI | ✅ BLEACH_INOFFICE | 4 items | Găng tay, khẩu trang, gạc, gel tê |

## ✅ DANH SÁCH SERVICES ĐÃ CÓ BOM (14 Services)

### Các service từ yêu cầu gốc:
1. ✅ **ENDO_TREAT_ANT** - Điều trị tủy răng trước (6 items)
   - CON-GLOVE-01 (2 đôi), CON-MASK-01 (1 cái), CON-GAUZE-01 (4 gói)
   - MAT-COMP-01 (8g), MAT-ETCH-01 (4ml), MAT-BOND-01 (6 drop)

2. ✅ **ENDO_TREAT_POST** - Điều trị tủy răng sau (6 items)
   - CON-GLOVE-01 (2 đôi), CON-MASK-01 (1 cái), CON-GAUZE-01 (5 gói)
   - MAT-COMP-01 (10g), MAT-ETCH-01 (5ml), MAT-BOND-01 (8 drop)

3. ✅ **ORTHO_CONSULT** - Tư vấn chỉnh nha (3 items)
   - CON-GLOVE-01 (1 đôi), CON-MASK-01 (1 cái), CON-GAUZE-01 (1 gói)

4. ✅ **BLEACH_INOFFICE** - Tẩy trắng răng tại phòng (4 items)
   - CON-GLOVE-01 (2 đôi), CON-MASK-01 (1 cái), CON-GAUZE-01 (4 gói)
   - MED-GEL-01 (2g)

### Các service bổ sung (10 services):
5. ✅ **FILLING_COMP** - Trám răng Composite (6 items)
6. ✅ **EXTRACT_MILK** - Nhổ răng sữa (3 items)
7. ✅ **EXTRACT_NORM** - Nhổ răng thường (4 items)
8. ✅ **EXTRACT_WISDOM_L1** - Nhổ răng khôn mức 1 (4 items)
9. ✅ **EXTRACT_WISDOM_L2** - Nhổ răng khôn mức 2 (4 items)
10. ✅ **SCALING_L1** - Cạo vôi răng mức 1 (4 items)
11. ✅ **SCALING_L2** - Cạo vôi răng mức 2 (4 items)
12. ✅ **OTHER_GINGIVECTOMY** - Phẫu thuật cắt nướu (4 items)
13. ✅ **ORTHO_FILMS** - Chụp phim chỉnh nha (3 items)
14. ✅ **GEN_EXAM** - Khám tổng quát (2 items)

---

## 🔍 YÊU CẦU GỐC (CHỈ THAM KHẢO - MỘT SỐ CODE KHÔNG TỒN TẠI)

### 1. Thêm BOM Cho Các Service Sau (Ưu Tiên Cao):

#### A. Các Service Điều Trị Nha Khoa Phổ Biến:

**❌ ROOT_CANAL** - Điều trị tủy răng (KHÔNG TỒN TẠI - Dùng ENDO_TREAT_ANT/POST)
```sql
-- Găng tay: 2 đôi (thay đổi nhiều lần)
-- Khẩu trang: 1 cái
-- Gạc: 5 gói (lau máu và nước bọt)
-- Composite: 10g (trám tạm)
-- Etch gel: 5ml
-- Bonding: 8 drop
```

**❌ CROWN_PREP** - Mài răng làm mão (KHÔNG TỒN TẠI)
```sql
-- Găng tay: 2 đôi
-- Khẩu trang: 1 cái
-- Gạc: 4 gói
-- Composite: 12g (trám tạm)
```

**✅ ORTHO_CONSULT** - Tư vấn chỉnh nha (ĐÃ THÊM BOM)
```sql
-- Găng tay: 1 đôi
-- Khẩu trang: 1 cái
-- Gạc: 1 gói
```

**❌ XRAY_PANORAMIC** - Chụp X-quang toàn cảnh (KHÔNG TỒN TẠI)
```sql
-- Găng tay: 1 đôi
-- Khẩu trang: 1 cái
-- Gạc: 1 gói (lau nước bọt)
```

**❌ WHITENING** - Tẩy trắng răng (KHÔNG TỒN TẠI - Dùng BLEACH_INOFFICE)
```sql
-- Găng tay: 2 đôi
-- Khẩu trang: 1 cái
-- Gạc: 3 gói
-- Gel tê: 2g (nếu cần)
```

#### B. Các Service Khác (Nếu Có Trong Seed Data):

✅ **ĐÃ HOÀN THÀNH** - BE đã kiểm tra tất cả service trong database và thêm BOM cho 10 services bổ sung.

### 2. Format SQL Insert:

Sử dụng format tương tự như seed data hiện tại:

```sql
INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, {QUANTITY}, u.unit_id, '{NOTES}' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = '{SERVICE_CODE}' 
  AND im.item_code = '{ITEM_CODE}' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = '{UNIT_NAME}' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;
```

### 3. Lưu Ý:

- **Số lượng vật tư**: Ước tính dựa trên thực tế sử dụng trong nha khoa
- **Notes**: Mô tả ngắn gọn mục đích sử dụng (tiếng Việt)
- **Unit**: Phải khớp với `unit_name` trong `item_units`
- **Conflict handling**: Dùng `ON CONFLICT DO NOTHING` để tránh lỗi khi chạy lại

## 📝 Ví Dụ SQL Hoàn Chỉnh

```sql
-- ROOT_CANAL - Điều trị tủy răng
INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 2, u.unit_id, 'Thay đổi nhiều lần trong quá trình điều trị' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'CON-GLOVE-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'Đôi' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;

INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 1, u.unit_id, 'Bảo vệ bác sĩ' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'CON-MASK-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'Cái' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;

INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 5, u.unit_id, 'Lau máu và nước bọt' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'CON-GAUZE-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'Gói' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;

INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 10, u.unit_id, 'Trám tạm sau điều trị tủy' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'MAT-COMP-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'g' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;

INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 5, u.unit_id, 'Xoi mòn men răng trước khi trám' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'MAT-ETCH-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'ml' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;

INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes) 
SELECT s.service_id, im.item_master_id, 8, u.unit_id, 'Keo dán trám' 
FROM services s, item_masters im, item_units u 
WHERE s.service_code = 'ROOT_CANAL' 
  AND im.item_code = 'MAT-BOND-01' 
  AND u.item_master_id = im.item_master_id 
  AND u.unit_name = 'drop' 
ON CONFLICT (service_id, item_master_id) DO NOTHING;
```

## 🧪 Testing Checklist

Sau khi BE thêm seed data, FE sẽ test:

- [ ] Tạo thủ thuật với service có BOM → Vật tư tự động được tạo
- [ ] Xem danh sách vật tư trong modal "Chi tiết thủ thuật"
- [ ] Chỉnh sửa số lượng vật tư trước khi trừ kho
- [ ] Cập nhật số lượng thực tế sau khi trừ kho
- [ ] Kiểm tra hiển thị "Vật tư dự kiến" khi chưa có materials
- [ ] Kiểm tra hiển thị "Thủ thuật này không tiêu hao vật tư" cho service không có BOM

## 📌 Priority

**Priority: HIGH** - Cần thiết cho demo và test tính năng quản lý vật tư thủ thuật.

## ⏰ Timeline

Cần hoàn thành trước khi demo sản phẩm.

---

**Created by:** FE Team  
**Date:** 2025-01-21  
**Updated by:** BE Team  
**Updated:** 2025-01-22  
**Status:** ⚠️ HOÀN THÀNH MỘT PHẦN - Cần FE xác nhận service codes không tồn tại

---

## 📋 ACTION REQUIRED - FE Team

**Các service code sau KHÔNG TỒN TẠI trong database. FE vui lòng:**
1. ❌ **ROOT_CANAL** → BE đã dùng `ENDO_TREAT_ANT` và `ENDO_TREAT_POST` thay thế. OK?
2. ❌ **CROWN_PREP** → Không có trong DB. Cần service nào? (Có CROWN_PFM, CROWN_TITAN, CROWN_EMAX, v.v.)
3. ❌ **XRAY_PANORAMIC** → Không có trong DB. Có `GEN_XRAY_PERI` (X-quang quanh chóp). Cần service nào?
4. ❌ **WHITENING** → BE đã dùng `BLEACH_INOFFICE` thay thế. OK?

**Database hiện có 53 services. Vui lòng check lại requirements hoặc xác nhận các service thay thế ở trên.**

