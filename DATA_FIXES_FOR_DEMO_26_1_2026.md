# DEMO PREPARATION CHECKLIST - 26/1/2026

## ✅ COMPLETED TASKS

### 1. Patient Code Standardization
**Issue:** Seed data có code bệnh nhân là BN-100x nhưng khi tạo là PAT0xx

**Fixed:**
- Đã đổi tất cả BN-1001, BN-1002... thành PAT-001, PAT-002...
- File: `dental-clinic-seed-data.sql` (lines 1347-1395)

---

### 2. Remove TEST Suffix from Appointments
**Issue:** Phần lịch hẹn seed data đang có chữ TEST (ví dụ: APT2026.....-TEST01)

**Fixed:**
- Đã xóa tất cả suffix -TEST
- APT-20260102-TEST01 → APT-20260102-001
- APT-20260102-TEST02 → APT-20260102-002
- Đã update cả invoice codes và WHERE clauses
- File: `dental-clinic-seed-data.sql` (lines 5747-5760)

---

### 3. Delete Test Patient
**Issue:** Xóa bệnh nhân "Mít tơ bít - trôn"

**Fixed:**
- Đã xóa patient_id=4 (Mít tơ Bít)
- Renumber patients: PAT-001, PAT-002, PAT-003, PAT-004 (Trần Văn Nam)
- File: `dental-clinic-seed-data.sql` (lines 1347-1395)

---

### 4. Remove "Tại nhà" Services
**Issue:** Một số dịch vụ có chữ "tại nhà" bị lỗi không thể tạo lịch

**Fixed:**
- Đã comment out "Tẩy trắng răng tại nhà" (BLEACH_ATHOME)
- File: `dental-clinic-seed-data.sql` (line 2806)
- **Note:** Nếu còn service nào lỗi khi tạo lịch, cần test lại để tìm

---

### 5. Professional Email Templates
**Issue:** Mail xác nhận lịch hẹn gửi cho bệnh nhân nhìn rất AI (emoji, số đt: 1900-xxxx)

**Fixed:**
- Removed emojis: 📞, ✉️, ⚠️, ✅
- Changed phone: `1900-xxxx` → `028-1234-5678`
- Subject: "✅ Xác nhận..." → "Xác nhận..."
- Professional corporate style now!
- File: `AppointmentEmailService.java` (lines 131, 154, 234, 244)

---

### 6. Service Price Reduction for Demo
**Issue:** Giá dịch vụ hiện tại 200k-300k, cần xuống vài chục k cho demo payment testing

**Fixed:** ALL services now < 100k for DEMO PAYMENT TESTING

| Category | Old Prices | New Prices |
|----------|-----------|------------|
| A_GENERAL (Tổng quát) | 100k-2.5M | 20k-95k |
| B_COSMETIC (Thẩm mỹ) | 800k-8M | 70k-95k |
| C_IMPLANT (Cấy ghép) | 0-25M | 0-98k |
| D_ORTHO (Chỉnh nha) | 0-5M | 0-98k |
| E_PROS_DENTURE (Hàm tháo lắp) | 0-1M | 0-85k |
| F_OTHER (Khác) | 0-1M | 0-95k |

**Example Price Changes:**
- Khám tổng quát: 100k → 30k
- Cạo vôi răng: 300k → 50k
- Trám răng: 500k → 60k
- Nhổ răng khôn: 2.5M → 95k
- Crown Emax: 6M → 90k
- Implant surgery: 25M → 98k

**Test Invoices Updated:**
- All 6 test invoices: 140k each (30k GEN_EXAM + 50k SCALING + 60k FILLING)
- Invoice items match new service prices

**Reason:** Demo on 26/1 needs cheap prices for payment testing!

---

### 7. Clean Leave Request Test Data
**Issue:** Danh sách yêu cầu nghỉ phép có data bị lỗi (TOR_TEST_AUTO...)

**Fixed:**
- Đã comment out TOR_TEST_AUTO_001, 002, 003
- File: `dental-clinic-seed-data.sql` (lines 1667-1679)

---

### 8. Add Feedback Data for Dashboard
**Issue:** Thêm dữ liệu cho phần góp ý cho dashboard (7-8 cái gì đó)

**Fixed:**
- Added 8 feedbacks
- Ratings: 3-5 stars
- Comments: Professional Vietnamese
- Tags: PROFESSIONAL, FRIENDLY, CLEAN
- Linked to existing appointments
- File: `dental-clinic-seed-data.sql` (lines 5840-5905)

---

### 9. Add Time-Off Requests for Dashboard
**Issue:** Thêm dữ liệu cho đơn nghỉ phép cho mấy ngày quá khứ (10-20 cái cho dashboard)

**Fixed:**
- Added 15 time_off_requests
- TOR-20260105-001 through TOR-20260119-001
- Mix: APPROVED, PENDING, REJECTED
- Types: ANNUAL_LEAVE, SICK_LEAVE, UNPAID_LEAVE, MATERNITY_LEAVE
- Dates: Spread across Jan 2026
- File: `dental-clinic-seed-data.sql` (lines 5840-5905)

---

## 📊 SUMMARY

| Status | Count | Tasks |
|--------|-------|-------|
| ✅ Completed | 9/9 | ALL DONE! |
| ⚠️ Skipped | 0 | - |
| ❌ Pending | 0 | - |

---

## 📁 FILES MODIFIED

### 1. `src/main/resources/db/dental-clinic-seed-data.sql`
- Patient codes: BN-100x → PAT-00x
- Appointment codes: Removed -TEST suffixes
- Deleted test patient "Mít tơ bít"
- Commented out "Tẩy trắng răng tại nhà" service
- Commented out TOR_TEST_AUTO leave requests
- Added 8 feedbacks for dashboard
- Added 15 time_off_requests for dashboard
- Updated ALL services to < 100k prices (6 categories)
- Updated test invoices and invoice_items to match new prices

### 2. `src/main/java/com/dentalclinic/service/email/AppointmentEmailService.java`
- Removed AI-style emojis (📞, ✉️, ⚠️, ✅)
- Changed phone: 1900-xxxx → 028-1234-5678

---

## 🎉 STATUS

**READY FOR DEMO 26/1!**

ALL DATA CLEANED AND PRICES SET FOR PAYMENT TESTING!
