# Yêu cầu cập nhật giá dịch vụ có giá = 0

## 📋 Mô tả

Trong file seed data hiện tại, có một số dịch vụ đang được set giá (price) = 0. Điều này có thể gây ra vấn đề khi tính toán hóa đơn và hiển thị giá dịch vụ cho bệnh nhân.

## 🎯 Yêu cầu

**Yêu cầu BE team cập nhật giá của các dịch vụ có `price = 0` thành `10000` (10,000 VNĐ).**

---

## 📊 Danh sách dịch vụ cần cập nhật

### 1. **IMPL_CONSULT** - Khám & Tư vấn Implant
- **Service Code**: `IMPL_CONSULT`
- **Tên dịch vụ**: Khám & Tư vấn Implant
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: C. Cắm ghép Implant
- **Vị trí trong file**: Dòng 2817

### 2. **IMPL_IMPRESSION** - Lấy dấu Implant
- **Service Code**: `IMPL_IMPRESSION`
- **Tên dịch vụ**: Lấy dấu Implant
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: C. Cắm ghép Implant
- **Vị trí trong file**: Dòng 2824

### 3. **ORTHO_CONSULT** - Khám & Tư vấn Chỉnh nha
- **Service Code**: `ORTHO_CONSULT`
- **Tên dịch vụ**: Khám & Tư vấn Chỉnh nha
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: D. Chỉnh nha
- **Vị trí trong file**: Dòng 2829

### 4. **PROS_CEMENT** - Gắn sứ / Thử sứ (Lần 2)
- **Service Code**: `PROS_CEMENT`
- **Tên dịch vụ**: Gắn sứ / Thử sứ (Lần 2)
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: E. Phục hình Tháo lắp
- **Vị trí trong file**: Dòng 2841

### 5. **DENTURE_TRYIN** - Thử sườn/Thử răng Hàm Tháo Lắp
- **Service Code**: `DENTURE_TRYIN`
- **Tên dịch vụ**: Thử sườn/Thử răng Hàm Tháo Lắp
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: E. Phục hình Tháo lắp
- **Vị trí trong file**: Dòng 2843

### 6. **DENTURE_DELIVERY** - Giao hàm & Chỉnh khớp cắn
- **Service Code**: `DENTURE_DELIVERY`
- **Tên dịch vụ**: Giao hàm & Chỉnh khớp cắn
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: E. Phục hình Tháo lắp
- **Vị trí trong file**: Dòng 2844

### 7. **SURG_CHECKUP** - Tái khám sau phẫu thuật / Cắt chỉ
- **Service Code**: `SURG_CHECKUP`
- **Tên dịch vụ**: Tái khám sau phẫu thuật / Cắt chỉ
- **Giá hiện tại**: `0`
- **Giá đề xuất**: `10000`
- **Danh mục**: F. Dịch vụ khác
- **Vị trí trong file**: Dòng 2850

---

## 📝 Tóm tắt

| STT | Service Code | Tên dịch vụ | Giá hiện tại | Giá đề xuất |
|-----|--------------|-------------|--------------|-------------|
| 1 | `IMPL_CONSULT` | Khám & Tư vấn Implant | 0 | 10000 |
| 2 | `IMPL_IMPRESSION` | Lấy dấu Implant | 0 | 10000 |
| 3 | `ORTHO_CONSULT` | Khám & Tư vấn Chỉnh nha | 0 | 10000 |
| 4 | `PROS_CEMENT` | Gắn sứ / Thử sứ (Lần 2) | 0 | 10000 |
| 5 | `DENTURE_TRYIN` | Thử sườn/Thử răng Hàm Tháo Lắp | 0 | 10000 |
| 6 | `DENTURE_DELIVERY` | Giao hàm & Chỉnh khớp cắn | 0 | 10000 |
| 7 | `SURG_CHECKUP` | Tái khám sau phẫu thuật / Cắt chỉ | 0 | 10000 |

**Tổng số dịch vụ cần cập nhật**: 7 dịch vụ

---

## 🔧 Cách thực hiện

### Option 1: Cập nhật trực tiếp trong file seed data

**File**: `docs/files/dental-clinic-seed-data.sql`

Cập nhật các dòng sau:

```sql
-- Dòng 2817
('IMPL_CONSULT', 'Khám & Tư vấn Implant', 'Khám, đánh giá tình trạng xương, tư vấn kế hoạch.', 45, 15, 10000, 4, 'C_IMPLANT', 1, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2824
('IMPL_IMPRESSION', 'Lấy dấu Implant', 'Lấy dấu để làm răng sứ trên Implant.', 30, 15, 10000, 4, 'C_IMPLANT', 8, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2829
('ORTHO_CONSULT', 'Khám & Tư vấn Chỉnh nha', 'Khám, phân tích phim, tư vấn kế hoạch niềng.', 45, 15, 10000, 1, 'D_ORTHO', 1, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2841
('PROS_CEMENT', 'Gắn sứ / Thử sứ (Lần 2)', 'Hẹn lần 2 để thử và gắn vĩnh viễn mão sứ, cầu răng, veneer.', 30, 15, 10000, 4, 'E_PROS_DENTURE', 1, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2843
('DENTURE_TRYIN', 'Thử sườn/Thử răng Hàm Tháo Lắp', 'Hẹn thử khung kim loại hoặc thử răng sáp.', 30, 15, 10000, 4, 'E_PROS_DENTURE', 3, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2844
('DENTURE_DELIVERY', 'Giao hàm & Chỉnh khớp cắn', 'Giao hàm hoàn thiện, chỉnh sửa các điểm vướng cộm.', 30, 15, 10000, 4, 'E_PROS_DENTURE', 4, 0, 0, 0, NULL, true, NOW()),

-- Dòng 2850
('SURG_CHECKUP', 'Tái khám sau phẫu thuật / Cắt chỉ', 'Kiểm tra vết thương sau nhổ răng khôn, cắm Implant, cắt nướu.', 15, 10, 10000, 5, 'F_OTHER', 4, 0, 0, 0, NULL, true, NOW())
```

### Option 2: Tạo script SQL UPDATE

Nếu đã có dữ liệu trong database, có thể chạy script UPDATE:

```sql
-- Cập nhật giá cho các dịch vụ có price = 0
UPDATE services 
SET price = 10000 
WHERE price = 0 
  AND service_code IN (
    'IMPL_CONSULT',
    'IMPL_IMPRESSION',
    'ORTHO_CONSULT',
    'PROS_CEMENT',
    'DENTURE_TRYIN',
    'DENTURE_DELIVERY',
    'SURG_CHECKUP'
  );
```

---

## ✅ Kiểm tra sau khi cập nhật

Sau khi BE team cập nhật, vui lòng kiểm tra:

1. ✅ Tất cả 7 dịch vụ trên đã có `price = 10000`
2. ✅ Không còn dịch vụ nào có `price = 0` (trừ khi có lý do đặc biệt)
3. ✅ Giá dịch vụ hiển thị đúng trên FE khi tạo lịch hẹn
4. ✅ Giá dịch vụ hiển thị đúng trong hóa đơn

---

## 📌 Lưu ý

- Giá `10000` VNĐ là giá đề xuất tối thiểu. BE team có thể điều chỉnh theo giá thực tế của từng dịch vụ nếu cần.
- Nếu có dịch vụ nào cần giữ giá = 0 (ví dụ: dịch vụ miễn phí), vui lòng thông báo cho FE team để cập nhật logic hiển thị phù hợp.

---

## 📅 Timeline

- **Priority**: 🔴 Medium
- **Created**: 2026-01-22
- **Requested by**: FE Team
- **Status**: ✅ **COMPLETED** - 2026-01-22
- **Updated by**: BE Team

---

## ✅ KẾT QUẢ THỰC HIỆN

**Đã cập nhật thành công 7 dịch vụ:**

| Service Code | Giá cũ | Giá mới | Status |
|--------------|--------|---------|--------|
| IMPL_CONSULT | 0 | 10000 | ✅ Updated |
| IMPL_IMPRESSION | 0 | 10000 | ✅ Updated |
| ORTHO_CONSULT | 0 | 10000 | ✅ Updated |
| PROS_CEMENT | 0 | 10000 | ✅ Updated |
| DENTURE_TRYIN | 0 | 10000 | ✅ Updated |
| DENTURE_DELIVERY | 0 | 10000 | ✅ Updated |
| SURG_CHECKUP | 0 | 10000 | ✅ Updated |

**Changes Applied:**
1. ✅ Database updated: All 7 services now have price = 10,000 VNĐ
2. ✅ Seed file updated: `dental-clinic-seed-data.sql` lines 2824, 2831, 2836, 2848, 2850, 2851, 2857
3. ✅ Verified: No services with price = 0 remaining (total 53 services, all have proper pricing)

**Price Distribution After Update:**
- Lowest price: 10,000 VNĐ (consultation services)
- Highest price: 8,000,000 VNĐ (IMPL_SINUS_LIFT)
- Total services with updated pricing: 53/53

---

## 📞 Liên hệ

Nếu có thắc mắc hoặc cần thảo luận về giá dịch vụ, vui lòng liên hệ FE team.

