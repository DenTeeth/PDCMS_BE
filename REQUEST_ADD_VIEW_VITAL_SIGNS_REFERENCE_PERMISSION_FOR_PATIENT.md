# Yêu cầu thêm quyền VIEW_VITAL_SIGNS_REFERENCE cho ROLE_PATIENT

## 📋 Mô tả vấn đề

Hiện tại, role **ROLE_PATIENT** không có quyền `VIEW_VITAL_SIGNS_REFERENCE`, dẫn đến việc bệnh nhân không thể xem đánh giá chỉ số sức khỏe (vital signs assessment) trong trang chi tiết lịch hẹn của họ.

### 🔍 Chi tiết vấn đề

1. **Vị trí**: Trang `/patient/appointments/[appointmentCode]` - Tab "Bệnh án"
2. **Hiện tượng**: 
   - Chỉ số sức khỏe (Huyết áp, Nhịp tim, Nhiệt độ, SpO2) hiển thị trạng thái "Không xác định" (UNKNOWN)
   - Trong khi đó, trang admin và employee hiển thị đúng trạng thái (Bình thường, Thấp, Cao)
3. **Nguyên nhân**: 
   - Component `ClinicalRecordView` cần gọi API `GET /api/v1/vital-signs-reference/by-age/{age}` để lấy reference ranges
   - API này yêu cầu quyền `VIEW_VITAL_SIGNS_REFERENCE` hoặc `WRITE_CLINICAL_RECORD`
   - Role `ROLE_PATIENT` không có cả hai quyền này

---

## 🎯 Yêu cầu

**Yêu cầu BE team thêm quyền `VIEW_VITAL_SIGNS_REFERENCE` cho role `ROLE_PATIENT`.**

### Lý do

1. **Bệnh nhân cần xem đánh giá chỉ số sức khỏe của chính họ**: 
   - Đây là thông tin y tế của bệnh nhân, họ có quyền được biết
   - Giúp bệnh nhân hiểu rõ hơn về tình trạng sức khỏe của mình

2. **Tính nhất quán với các role khác**:
   - Admin và Employee đều có thể xem đánh giá chỉ số sức khỏe
   - Bệnh nhân cũng nên có quyền xem thông tin của chính mình

3. **Không ảnh hưởng đến bảo mật**:
   - `VIEW_VITAL_SIGNS_REFERENCE` chỉ là quyền xem reference ranges (phạm vi tham chiếu)
   - Không cho phép chỉnh sửa hoặc xem dữ liệu của bệnh nhân khác
   - Bệnh nhân chỉ có thể xem bệnh án của chính họ (đã được kiểm soát bởi `VIEW_APPOINTMENT_OWN`)

---

## 📊 Thông tin kỹ thuật

### API liên quan

**Endpoint**: `GET /api/v1/vital-signs-reference/by-age/{age}`

**Yêu cầu quyền hiện tại**:
- `VIEW_VITAL_SIGNS_REFERENCE` **HOẶC**
- `WRITE_CLINICAL_RECORD`

**Vị trí trong code FE**:
- Service: `src/services/vitalSignsReferenceService.ts`
- Component: `src/components/clinical-records/ClinicalRecordView.tsx`
- Page: `src/app/patient/appointments/[appointmentCode]/page.tsx`

### Permission hiện tại

Theo documentation trong code:
- `VIEW_VITAL_SIGNS_REFERENCE`: Quyền xem vital signs reference ranges
- `WRITE_CLINICAL_RECORD`: Quyền ghi bệnh án (chỉ dành cho bác sĩ/nhân viên y tế)

### Workaround hiện tại (FE)

FE đã implement workaround bằng cách:
- Pass `appointment.patient.dateOfBirth` vào `ClinicalRecordView` component
- Tuy nhiên, vẫn không thể load reference ranges do thiếu quyền

---

## ✅ Kết quả mong đợi

Sau khi BE team thêm quyền `VIEW_VITAL_SIGNS_REFERENCE` cho `ROLE_PATIENT`:

1. ✅ Bệnh nhân có thể xem đánh giá chỉ số sức khỏe trong bệnh án của họ
2. ✅ Chỉ số sức khỏe hiển thị đúng trạng thái (Bình thường, Thấp, Cao) thay vì "Không xác định"
3. ✅ Trải nghiệm người dùng nhất quán giữa các role (Patient, Employee, Admin)

---

## 🔗 Tài liệu tham khảo

- **Permission enum**: `src/types/permission.ts` - Line 54
- **Service**: `src/services/vitalSignsReferenceService.ts`
- **Component**: `src/components/clinical-records/ClinicalRecordView.tsx` - Line 127-143

---

## 📝 Ghi chú

- Quyền này chỉ cho phép **xem** reference ranges, không cho phép chỉnh sửa
- Bệnh nhân chỉ có thể xem bệnh án của chính họ (đã được kiểm soát bởi backend)
- Không ảnh hưởng đến bảo mật vì chỉ là dữ liệu tham chiếu công khai

---

**Ngày tạo**: 2025-01-26  
**Người tạo**: FE Team  
**Priority**: Medium  
**Status**: ✅ RESOLVED (2026-01-24)

---

## ✅ Giải pháp đã triển khai

**File**: `src/main/resources/db/dental-clinic-seed-data.sql` (Line 1135)

**Thay đổi**:
```sql
-- CLINICAL_RECORDS (read-only own records)
('ROLE_PATIENT', 'VIEW_ATTACHMENT'), -- View attachments of own clinical records
('ROLE_PATIENT', 'VIEW_VITAL_SIGNS_REFERENCE'), -- View vital signs reference ranges for assessment
```

**Kết quả triển khai**:
- ✅ Permission được thêm vào seed data file
- ✅ Database đã được verify: ROLE_PATIENT có 12 permissions (bao gồm VIEW_VITAL_SIGNS_REFERENCE)
- ✅ Application tự động load seed data khi khởi động
- ✅ Bệnh nhân có thể gọi API `GET /api/v1/vital-signs-reference/by-age/{age}` 
- ✅ Chỉ số sức khỏe hiển thị đúng trạng thái (Bình thường, Thấp, Cao) thay vì "Không xác định"
- ✅ Trải nghiệm người dùng nhất quán giữa các role (Patient, Employee, Admin)

**Verified**: 2026-01-24 - Permission confirmed in database after application startup

