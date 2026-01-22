# Yêu cầu điều chỉnh format mã bệnh nhân trong seed data

## 📋 Mô tả vấn đề

Hiện tại có sự **không nhất quán** về format mã bệnh nhân (`patient_code`) giữa:
- **Seed data**: Sử dụng format `PAT-xxx` (có dấu gạch ngang, ví dụ: `PAT-001`, `PAT-002`, `PAT-003`)
- **Khi tạo mới**: Hệ thống tự động tạo mã với format `PATxxx` (không có dấu gạch ngang, ví dụ: `PAT010`, `PAT011`)

Điều này gây ra:
- ❌ **Không nhất quán** trong dữ liệu
- ❌ **Khó khăn** trong việc tìm kiếm và lọc dữ liệu
- ❌ **Nhầm lẫn** cho người dùng khi thấy 2 format khác nhau

---

## 🎯 Yêu cầu

**Yêu cầu BE team điều chỉnh seed data để format mã bệnh nhân nhất quán với format khi tạo mới: `PATxxx` (không có dấu gạch ngang).**

### Format mong muốn:
- ✅ **Format đúng**: `PAT001`, `PAT002`, `PAT003`, ..., `PAT010`, `PAT011`, ...
- ❌ **Format cũ (cần sửa)**: `PAT-001`, `PAT-002`, `PAT-003`, ...

---

## 📊 Danh sách mã bệnh nhân cần cập nhật trong seed data

Dựa trên file `dental-clinic-seed-data.sql`, các mã bệnh nhân sau cần được cập nhật:

### 1. **PAT-001** → **PAT001**
- **Tên bệnh nhân**: Đoàn Thanh Phong
- **Vị trí trong file**: Dòng 1347
- **Thay đổi**: `'PAT-001'` → `'PAT001'`

### 2. **PAT-002** → **PAT002**
- **Tên bệnh nhân**: Phạm Văn Phong
- **Vị trí trong file**: Dòng 1350
- **Thay đổi**: `'PAT-002'` → `'PAT002'`

### 3. **PAT-003** → **PAT003**
- **Tên bệnh nhân**: Nguyễn Tuấn Anh
- **Vị trí trong file**: Dòng 1353
- **Thay đổi**: `'PAT-003'` → `'PAT003'`

### 4. **PAT-004** → **PAT004**
- **Tên bệnh nhân**: Trần Văn Nam
- **Vị trí trong file**: Dòng 1356
- **Thay đổi**: `'PAT-004'` → `'PAT004'`

### 5. **PAT-005** → **PAT005**
- **Tên bệnh nhân**: Lê Thị Hoa
- **Vị trí trong file**: Dòng 1377
- **Thay đổi**: `'PAT-005'` → `'PAT005'`

### 6. **PAT-006** → **PAT006**
- **Tên bệnh nhân**: Võ Văn Khánh
- **Vị trí trong file**: Dòng 1380
- **Thay đổi**: `'PAT-006'` → `'PAT006'`

### 7. **PAT-007** → **PAT007**
- **Tên bệnh nhân**: Trần Thị Mai
- **Vị trí trong file**: Dòng 1383
- **Thay đổi**: `'PAT-007'` → `'PAT007'`

### 8. **PAT-008** → **PAT008**
- **Tên bệnh nhân**: Phan Văn Tú
- **Vị trí trong file**: Dòng 1386
- **Thay đổi**: `'PAT-008'` → `'PAT008'`

### 9. **PAT-009** → **PAT009**
- **Tên bệnh nhân**: Nguyễn Thị Lan
- **Vị trí trong file**: Dòng 1389
- **Thay đổi**: `'PAT-009'` → `'PAT009'`

---

## 🔍 Cách thực hiện

### Option 1: Sửa trực tiếp trong file seed data
Tìm và thay thế tất cả các mã bệnh nhân trong file `dental-clinic-seed-data.sql`:

```sql
-- Tìm tất cả các dòng có 'PAT-xxx'
-- Thay thế:
'PAT-001' → 'PAT001'
'PAT-002' → 'PAT002'
'PAT-003' → 'PAT003'
'PAT-004' → 'PAT004'
'PAT-005' → 'PAT005'
'PAT-006' → 'PAT006'
'PAT-007' → 'PAT007'
'PAT-008' → 'PAT008'
'PAT-009' → 'PAT009'
```

### Option 2: Tạo script SQL để update
Nếu đã có dữ liệu trong database, có thể chạy script SQL:

```sql
UPDATE patients 
SET patient_code = REPLACE(patient_code, 'PAT-', 'PAT')
WHERE patient_code LIKE 'PAT-%';
```

**Lưu ý**: Nếu có các bảng khác tham chiếu đến `patient_code` (như `appointments`, `invoices`, `treatment_plans`, v.v.), cần đảm bảo cập nhật đồng bộ hoặc sử dụng foreign key constraints để tự động cập nhật.

---

## ✅ Checklist sau khi cập nhật

Sau khi BE team cập nhật, vui lòng kiểm tra:

- [ ] Tất cả mã bệnh nhân trong seed data đã được cập nhật từ `PAT-xxx` → `PATxxx`
- [ ] Không còn mã nào có dấu gạch ngang trong seed data
- [ ] Format mã bệnh nhân mới tạo vẫn hoạt động đúng (đã đúng rồi: `PATxxx`)
- [ ] Tất cả các bảng liên quan (nếu có) đã được cập nhật đồng bộ
- [ ] Test lại việc tạo bệnh nhân mới để đảm bảo format nhất quán

---

## 📝 Ví dụ sau khi sửa

### Trước (❌ Không nhất quán):
```sql
-- Seed data
INSERT INTO patients (..., patient_code, ...) VALUES
(1, 12, 'PAT-001', ...),  -- ❌ Có dấu gạch ngang
(2, 13, 'PAT-002', ...),  -- ❌ Có dấu gạch ngang
...

-- Khi tạo mới
patient_code = 'PAT010'  -- ✅ Không có dấu gạch ngang
```

### Sau (✅ Nhất quán):
```sql
-- Seed data
INSERT INTO patients (..., patient_code, ...) VALUES
(1, 12, 'PAT001', ...),  -- ✅ Không có dấu gạch ngang
(2, 13, 'PAT002', ...),  -- ✅ Không có dấu gạch ngang
...

-- Khi tạo mới
patient_code = 'PAT010'  -- ✅ Không có dấu gạch ngang
```

---

## 🎯 Kết quả mong đợi

Sau khi cập nhật:
- ✅ Tất cả mã bệnh nhân có format nhất quán: `PATxxx` (không có dấu gạch ngang)
- ✅ Seed data và dữ liệu mới tạo đều sử dụng cùng một format
- ✅ Không còn sự nhầm lẫn cho người dùng
- ✅ Dễ dàng tìm kiếm và lọc dữ liệu hơn

---

## 📅 Ngày tạo
**Ngày**: 22/01/2025

## 👤 Người yêu cầu
**FE Team**

---

## ✅ KẾT QUẢ THỰC HIỆN - 22/01/2026

**Đã cập nhật thành công 9 mã bệnh nhân:**

| STT | Mã cũ | Mã mới | Tên bệnh nhân | Status |
|-----|-------|--------|---------------|--------|
| 1 | PAT-001 | PAT001 | Đoàn Thanh Phong | ✅ Updated |
| 2 | PAT-002 | PAT002 | Phạm Văn Phong | ✅ Updated |
| 3 | PAT-003 | PAT003 | Nguyễn Tuấn Anh | ✅ Updated |
| 4 | PAT-004 | PAT004 | Trần Văn Nam | ✅ Updated |
| 5 | PAT-005 | PAT005 | Lê Thị Hoa | ✅ Updated |
| 6 | PAT-006 | PAT006 | Võ Văn Khánh | ✅ Updated |
| 7 | PAT-007 | PAT007 | Trần Thị Mai | ✅ Updated |
| 8 | PAT-008 | PAT008 | Phan Văn Tú | ✅ Updated |
| 9 | PAT-009 | PAT009 | Nguyễn Thị Lan | ✅ Updated |

**Changes Applied:**
1. ✅ Seed file updated: Lines 1354, 1357, 1360, 1363, 1384, 1387, 1390, 1393, 1396
2. ✅ Database verified after drop & restart: All 9 patient codes correctly loaded with `PATxxx` format
3. ✅ No manual database updates needed - seed file changes work correctly on fresh database
4. ✅ Format consistency: Seed data now matches auto-generated format (`PATxxx`)

**Verification Method:**
- Database was dropped and restarted to ensure seed file changes load correctly
- All patient codes automatically loaded with proper format (no dashes)
- Confirms seed data is the source of truth for format consistency

**Checklist:**
- [x] Tất cả mã bệnh nhân trong seed data đã được cập nhật từ `PAT-xxx` → `PATxxx`
- [x] Không còn mã nào có dấu gạch ngang trong database sau khi restart
- [x] Format mã bệnh nhân mới tạo vẫn hoạt động đúng: `PATxxx`
- [x] Seed file đã được cập nhật và verified qua database drop + restart

---

Cảm ơn BE team! 🙏

