# 📅 Hướng Dẫn: Hệ Thống Cron Jobs Mới (P8 Architecture)

## 🎯 Tổng Quan

Dự án đã được **CẢI TIẾN HOÀN TOÀN** hệ thống cron jobs:

- ❌ **CŨ**: 2 jobs riêng biệt cho Fixed (Job 1) và Flex (Job 2)
- ✅ **MỚI**: 1 job duy nhất đồng bộ cả 2 luồng (Job P8)

---

## 📊 Kiến Trúc Mới

### **Job P8: UnifiedScheduleSyncJob** ⭐ (QUAN TRỌNG NHẤT)

**File**: `UnifiedScheduleSyncJob.java`
**Status**: ✅ **ENABLED** (đang chạy)
**Cron**: `0 1 0 * * ?` (00:01 AM hàng ngày)

#### Mục Đích:

Đọc lịch từ **CẢ 2 NGUỒN** và đồng bộ sang `employee_shifts`:

1. **Luồng 1 (Fixed)**: `fixed_shift_registrations` + `fixed_registration_days`
2. **Luồng 2 (Flex)**: `employee_shift_registrations` + `part_time_slots`

#### Tại Sao Chạy Hàng Ngày?

**Self-Healing Architecture** - Tự động sửa lỗi trong vòng 24 giờ:

- Admin thay đổi lịch cố định (P5) → Hệ thống tự cập nhật `employee_shifts` trong 1 ngày
- Part-time đăng ký thêm ca → Lịch thực tế được sync ngay ngày hôm sau
- **KHÔNG CẦN** restart service hay chạy script thủ công

#### Business Logic (14-Day Window):

```
1. DEFINE WINDOW
   ├── Start: Hôm nay
   └── End: Hôm nay + 13 ngày (14 ngày total)

2. CLEAN OLD SHIFTS (Phòng trường hợp admin đổi lịch)
   DELETE FROM employee_shifts
   WHERE work_date >= [Today] AND work_date <= [Today + 13]
     AND status = 'SCHEDULED'
     AND source IN ('BATCH_JOB', 'REGISTRATION_JOB')

3. LOOP 14 DAYS (Day 0 → Day 13)
   FOR EACH target_date IN window:
       ├── Skip if target_date is HOLIDAY
       ├── Get day_of_week (e.g., MONDAY, SATURDAY)
       │
       ├── QUERY 1: Fixed Schedules
       │   SELECT FROM fixed_shift_registrations fsr
       │   JOIN fixed_registration_days frd
       │   WHERE frd.day_of_week = [day_of_week]
       │     AND fsr.effective_from <= [target_date]
       │     AND (fsr.effective_to IS NULL OR fsr.effective_to >= [target_date])
       │     AND fsr.is_active = true
       │   → Result: (employee_id 1, WKS_MORNING_01), (employee_id 2, WKS_AFTERNOON_01)
       │
       ├── QUERY 2: Flex Schedules
       │   SELECT FROM employee_shift_registrations esr
       │   JOIN part_time_slots pts
       │   WHERE pts.day_of_week = [day_of_week]
       │     AND esr.effective_from <= [target_date]
       │     AND esr.effective_to >= [target_date]
       │     AND esr.is_active = true
       │     AND pts.is_active = true
       │   → Result: (employee_id 10, WKS_MORNING_02)
       │
       └── MERGE & INSERT
           INSERT INTO employee_shifts (
               employee_shift_id, employee_id, work_shift_id, work_date,
               status, source, is_overtime, created_at
           )
           VALUES
               ('EMS...', 1, 'WKS_MORNING_01', [target_date], 'SCHEDULED', 'BATCH_JOB', false, NOW()),
               ('EMS...', 2, 'WKS_AFTERNOON_01', [target_date], 'SCHEDULED', 'BATCH_JOB', false, NOW()),
               ('EMS...', 10, 'WKS_MORNING_02', [target_date], 'SCHEDULED', 'REGISTRATION_JOB', false, NOW())

4. LOG SUMMARY
   ├── Total shifts created: 126
   ├── Days skipped (holidays): 2
   └── Sync window: 14 days
```

#### Source Tags (Quan Trọng):

| Source             | Ý Nghĩa                        | Từ Nguồn Nào                             |
| ------------------ | ------------------------------ | ---------------------------------------- |
| `BATCH_JOB`        | Từ lịch cố định                | `fixed_shift_registrations` (Luồng 1)    |
| `REGISTRATION_JOB` | Từ lịch linh hoạt              | `employee_shift_registrations` (Luồng 2) |
| `OT_APPROVAL`      | Từ overtime request được duyệt | Admin/Manager approve                    |
| `MANUAL_ENTRY`     | Tạo thủ công                   | Admin tạo trực tiếp                      |

---

## 🚫 Jobs Đã DEPRECATED

### **Job 1: MonthlyFullTimeScheduleJob** ❌

**File**: `MonthlyFullTimeScheduleJob.java`
**Status**: ⛔ **DISABLED** (`// @Component`)
**Lý do**: Thay thế bởi `UnifiedScheduleSyncJob`

**Cũ**:

- Chạy tháng 1 lần (ngày 20 hàng tháng, 02:00 AM)
- Tạo lịch cho Full-Time employees cho 1 tháng tiếp theo
- **Vấn đề**: Nếu admin đổi lịch giữa tháng → Phải đợi đến tháng sau mới sync

**Mới** (P8):

- Chạy **HÀNG NGÀY** với window 14 ngày
- Tự động phát hiện thay đổi và cập nhật trong 24h
- **Không cần chờ đến cuối tháng**

---

### **Job 2: WeeklyPartTimeScheduleJob** ❌

**File**: `WeeklyPartTimeScheduleJob.java`
**Status**: ⛔ **DISABLED** (`// @Component`)
**Lý do**: Thay thế bởi `UnifiedScheduleSyncJob`

**Cũ**:

- Chạy tuần 1 lần (Chủ Nhật, 01:00 AM)
- Tạo lịch cho Part-Time Flex employees cho tuần tiếp theo
- **Vấn đề**: Nếu part-time đăng ký ca mới giữa tuần → Phải đợi Chủ Nhật mới có lịch

**Mới** (P8):

- Chạy **HÀNG NGÀY** với window 14 ngày
- Part-time đăng ký ca hôm nay → Lịch xuất hiện ngày mai
- **Không cần chờ đến Chủ Nhật**

---

## ✅ Jobs VẪN HOẠT ĐỘNG

### **Job 3: DailyRenewalDetectionJob** ✅

**File**: `DailyRenewalDetectionJob.java`
**Status**: ✅ **ENABLED**
**Cron**: `0 0 1 * * ?` (01:00 AM hàng ngày)

**Mục đích**:

- Phát hiện `fixed_shift_registrations` sắp hết hạn (7 ngày trước `effective_to`)
- Tạo `shift_renewal_requests` để mời nhân viên gia hạn
- **Chỉ áp dụng cho**: FULL_TIME và PART_TIME_FIXED (KHÔNG áp dụng cho PART_TIME_FLEX)

**Business Logic**:

```sql
-- Find registrations expiring in 7 days
SELECT * FROM fixed_shift_registrations
WHERE effective_to = (CURRENT_DATE + INTERVAL '7 days')
  AND is_active = true
  AND NOT EXISTS (
      SELECT 1 FROM shift_renewal_requests
      WHERE expiring_registration_id = registration_id
        AND status = 'PENDING_ACTION'
  )
```

**Ví dụ**:

- Hôm nay: 2025-11-08
- Job phát hiện registration có `effective_to = 2025-11-15` (7 ngày nữa)
- Tạo renewal request với `expires_at = 2025-11-13` (còn 2 ngày để nhân viên phản hồi)

---

### **Job 4: ExpirePendingRenewalsJob** ✅

**File**: `ExpirePendingRenewalsJob.java`
**Status**: ✅ **ENABLED**
**Cron**: `0 30 1 * * ?` (01:30 AM hàng ngày)

**Mục đích**:

- Đánh dấu các renewal requests đã quá hạn
- Chuyển `status` từ `PENDING_ACTION` → `EXPIRED`

**Business Logic**:

```sql
-- Find expired renewals
UPDATE shift_renewal_requests
SET status = 'EXPIRED',
    confirmed_at = NOW()
WHERE status = 'PENDING_ACTION'
  AND expires_at <= NOW()
```

**Audit Trail**:

- Status: `EXPIRED`
- `confirmed_at`: Timestamp khi job chạy
- HR/Admin có thể xem báo cáo nhân viên nào không phản hồi

---

### **Job 5: AnnualLeaveBalanceResetJob** ✅

**File**: `AnnualLeaveBalanceResetJob.java`
**Status**: ✅ **ENABLED**
**Cron**: `0 0 0 1 1 ?` (00:00 AM, ngày 1/1 hàng năm)

**Mục đích**:

- Reset số ngày phép năm cho tất cả nhân viên
- Chạy vào đầu năm mới

---

## 📈 So Sánh Kiến Trúc

| Tiêu Chí           | Cũ (Job 1 & 2)                                    | Mới (Job P8)                  |
| ------------------ | ------------------------------------------------- | ----------------------------- |
| **Tần suất**       | Tháng 1 lần (Full-Time)<br>Tuần 1 lần (Part-Time) | **Hàng ngày**                 |
| **Sync window**    | 30 ngày (Full-Time)<br>7 ngày (Part-Time)         | **14 ngày** (cả 2 loại)       |
| **Self-healing**   | ❌ Không                                          | ✅ **Có** (24h auto-correct)  |
| **Admin đổi lịch** | Phải đợi job tiếp theo                            | **Tự động sync trong 1 ngày** |
| **Độ phức tạp**    | 2 jobs riêng biệt                                 | **1 job duy nhất**            |
| **Trùng lặp code** | Cao (copy logic)                                  | Thấp (reuse logic)            |
| **Maintenance**    | Khó (2 nơi fix bug)                               | **Dễ** (1 nơi fix)            |

---

## 🔧 Cấu Hình Cron Timing

### Thứ Tự Chạy Hàng Ngày:

```
00:01 AM → UnifiedScheduleSyncJob      (P8 - Sync cả 2 luồng)
01:00 AM → DailyRenewalDetectionJob    (P3 - Phát hiện renewal)
01:30 AM → ExpirePendingRenewalsJob    (P4 - Expire renewals)
02:00 AM → (MonthlyFullTimeScheduleJob - DISABLED)
```

**Lý do thứ tự**:

1. **00:01 AM**: Sync lịch trước để có dữ liệu mới nhất
2. **01:00 AM**: Phát hiện renewal sau khi đã sync
3. **01:30 AM**: Expire renewals sau cùng

---

## 🧪 Testing & Validation

### Test Case 1: Self-Healing After Admin Change

**Scenario**: Admin đổi lịch cố định của nhân viên

```sql
-- Day 0: Admin changes registration
UPDATE fixed_shift_registrations
SET work_shift_id = 'WKS_AFTERNOON_01'  -- Changed from MORNING to AFTERNOON
WHERE registration_id = 5;

-- Day 0 (23:59): Old shifts still exist in employee_shifts
SELECT * FROM employee_shifts
WHERE employee_id = 5 AND work_date = '2025-11-10'
-- Result: WKS_MORNING_01 (OLD)

-- Day 1 (00:01): UnifiedScheduleSyncJob runs
-- Cleanup: DELETE old SCHEDULED shifts
-- Sync: INSERT new shifts with WKS_AFTERNOON_01

-- Day 1 (00:05): New shifts created
SELECT * FROM employee_shifts
WHERE employee_id = 5 AND work_date = '2025-11-10'
-- Result: WKS_AFTERNOON_01 (NEW) ✅
```

---

### Test Case 2: 14-Day Window Coverage

**Scenario**: Verify job creates shifts for exactly 14 days

```sql
-- Before job runs
SELECT COUNT(*) FROM employee_shifts
WHERE work_date >= CURRENT_DATE
  AND work_date <= CURRENT_DATE + 13
  AND source IN ('BATCH_JOB', 'REGISTRATION_JOB')
-- Expected: 0 (or old data)

-- After job runs (00:01 AM)
SELECT
    work_date,
    COUNT(*) as shift_count
FROM employee_shifts
WHERE work_date >= CURRENT_DATE
  AND work_date <= CURRENT_DATE + 13
  AND source IN ('BATCH_JOB', 'REGISTRATION_JOB')
GROUP BY work_date
ORDER BY work_date
-- Expected: 14 rows (Day 0 to Day 13)
```

---

### Test Case 3: Holiday Skipping

**Scenario**: Job skips holidays correctly

```sql
-- Setup: Insert holiday
INSERT INTO holiday_dates (holiday_date, holiday_name, year)
VALUES ('2025-12-25', 'Christmas', 2025);

-- After job runs
SELECT * FROM employee_shifts
WHERE work_date = '2025-12-25'
  AND source IN ('BATCH_JOB', 'REGISTRATION_JOB')
-- Expected: 0 shifts (holiday skipped) ✅
```

---

## 🛠️ Troubleshooting

### Vấn Đề 1: Job Không Chạy

**Kiểm tra**:

```java
// UnifiedScheduleSyncJob.java
@Component  // ← Phải có annotation này
@Slf4j
@RequiredArgsConstructor
public class UnifiedScheduleSyncJob {
    @Scheduled(cron = "0 1 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    // ...
}
```

**Log expected**:

```
2025-11-08 00:01:00 INFO  - === Starting Unified Schedule Sync Job (P8) ===
2025-11-08 00:01:00 INFO  - Sync window: 2025-11-08 to 2025-11-21 (14 days)
...
2025-11-08 00:01:05 INFO  - === Unified Schedule Sync Job Completed ===
2025-11-08 00:01:05 INFO  - Total shifts created: 126
```

---

### Vấn Đề 2: Shifts Không Được Tạo

**Debug Checklist**:

```sql
-- 1. Check work shifts exist
SELECT COUNT(*) FROM work_shifts WHERE is_active = true;
-- Expected: > 0

-- 2. Check Fixed registrations
SELECT COUNT(*) FROM fixed_shift_registrations WHERE is_active = true;
-- Expected: > 0

-- 3. Check Flex registrations
SELECT COUNT(*) FROM employee_shift_registrations WHERE is_active = true;
-- Expected: > 0 (if có part-time flex employees)

-- 4. Check effective dates
SELECT registration_id, effective_from, effective_to
FROM fixed_shift_registrations
WHERE is_active = true;
-- effective_from <= CURRENT_DATE
-- effective_to IS NULL OR effective_to >= CURRENT_DATE

-- 5. Check registration days
SELECT fsr.registration_id, frd.day_of_week
FROM fixed_shift_registrations fsr
JOIN fixed_registration_days frd ON fsr.registration_id = frd.registration_id
WHERE fsr.is_active = true;
-- Expected: Có days được định nghĩa
```

---

### Vấn Đề 3: Duplicate Shifts

**Nguyên nhân**: Job chạy 2 lần (lỗi config)

**Kiểm tra**:

```sql
SELECT employee_id, work_date, work_shift_id, COUNT(*)
FROM employee_shifts
WHERE work_date >= CURRENT_DATE
  AND source IN ('BATCH_JOB', 'REGISTRATION_JOB')
GROUP BY employee_id, work_date, work_shift_id
HAVING COUNT(*) > 1
-- Expected: 0 rows (no duplicates)
```

**Fix**: Ensure unique constraint exists

```sql
ALTER TABLE employee_shifts
ADD CONSTRAINT uk_employee_date_shift
UNIQUE (employee_id, work_date, work_shift_id);
```

---

## 📝 Migration Guide (Nếu Bạn Có Data Cũ)

### Bước 1: Backup

```bash
# Backup employee_shifts table
docker exec -i postgres-dental pg_dump -U root -d dental_clinic_db \
  -t employee_shifts > backup_employee_shifts_$(date +%Y%m%d).sql
```

### Bước 2: Clean Old Shifts (Optional)

```sql
-- Xóa shifts cũ từ Job 1 & Job 2 (nếu muốn clean slate)
DELETE FROM employee_shifts
WHERE source IN ('BATCH_JOB', 'REGISTRATION_JOB')
  AND status = 'SCHEDULED'
  AND work_date >= CURRENT_DATE;
```

### Bước 3: Enable P8, Disable Job 1 & 2

```java
// MonthlyFullTimeScheduleJob.java
// @Component  ← Comment out

// WeeklyPartTimeScheduleJob.java
// @Component  ← Comment out

// UnifiedScheduleSyncJob.java
@Component  ← Must be enabled
```

### Bước 4: Restart & Verify

```bash
# Restart application
docker-compose restart pdcms_be

# Check logs
docker logs -f pdcms_be | grep "Unified Schedule Sync"

# Verify shifts created
docker exec -i postgres-dental psql -U root -d dental_clinic_db \
  -c "SELECT work_date, COUNT(*) FROM employee_shifts WHERE work_date >= CURRENT_DATE GROUP BY work_date ORDER BY work_date;"
```

---

## 🎯 Best Practices

### 1. Monitor Job Execution

**Setup Logging**:

```yaml
# application.yaml
logging:
  level:
    com.dental.clinic.management.scheduled: DEBUG
```

**Expected Logs**:

```
DEBUG - Processing 2025-11-08 (FRIDAY)
DEBUG - Created 9 shifts for 2025-11-08 (7 Fixed, 2 Flex)
DEBUG - Processing 2025-11-09 (SATURDAY)
DEBUG - Created 5 shifts for 2025-11-09 (3 Fixed, 2 Flex)
```

---

### 2. Định Kỳ Kiểm Tra

**Weekly Check** (mỗi Thứ 2):

```sql
-- Verify 14-day coverage
SELECT
    CASE
        WHEN work_date = CURRENT_DATE THEN 'Today'
        WHEN work_date = CURRENT_DATE + 13 THEN 'Day 13 (Last)'
        ELSE TO_CHAR(work_date, 'Day DD/MM')
    END as day_label,
    COUNT(*) as total_shifts,
    SUM(CASE WHEN source = 'BATCH_JOB' THEN 1 ELSE 0 END) as fixed_shifts,
    SUM(CASE WHEN source = 'REGISTRATION_JOB' THEN 1 ELSE 0 END) as flex_shifts
FROM employee_shifts
WHERE work_date >= CURRENT_DATE
  AND work_date <= CURRENT_DATE + 13
  AND status = 'SCHEDULED'
GROUP BY work_date
ORDER BY work_date;
```

---

### 3. Alert Nếu Job Fail

**Setup Health Check** (Optional):

```java
// Add to UnifiedScheduleSyncJob
private LocalDateTime lastSuccessfulRun;

public LocalDateTime getLastSuccessfulRun() {
    return lastSuccessfulRun;
}

// At end of syncSchedules()
this.lastSuccessfulRun = LocalDateTime.now();
```

**Monitor**:

```bash
# Check if job ran in last 25 hours
SELECT
    NOW() - MAX(created_at) as time_since_last_sync
FROM employee_shifts
WHERE source IN ('BATCH_JOB', 'REGISTRATION_JOB')
  AND created_at >= CURRENT_DATE;
-- Expected: < 25 hours
```

---

## 🚀 Kết Luận

### Những Gì Đã Thay Đổi:

✅ **1 Job thay vì 2**: Giảm complexity
✅ **Daily sync**: Self-healing trong 24h
✅ **14-day window**: Balance giữa performance và coverage
✅ **Clean architecture**: Dễ maintain, dễ extend

### Lợi Ích Cho Team:

👨‍💼 **Admin**: Đổi lịch nhân viên → Tự động sync ngày hôm sau
👨‍⚕️ **Nhân viên**: Đăng ký ca part-time → Thấy lịch ngày hôm sau
👨‍💻 **Developer**: Chỉ cần maintain 1 file duy nhất
📊 **Business**: Dữ liệu luôn chính xác trong vòng 24h

**No more manual fixes!** 🎉
