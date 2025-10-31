# 🔧 HƯỚNG DẪN FIX LỖI ANNUAL RESET VÀ SEED DATA

## ⚠️ LỖI HIỆN TẠI

```
ERROR: Transaction silently rolled back because it has been marked as rollback-only
```

**Nguyên nhân:** `TimeOffTypeNotFoundException` - Không tìm thấy `type_id = 'ANNUAL_LEAVE'` trong database.

## 🔍 Vấn đề cốt lõi

1. ❌ **API POST /api/v1/admin/leave-balances/annual-reset** bị lỗi 500
2. ⚠️ **Nguyên nhân:** Database chưa có `time_off_types` với `type_id = 'ANNUAL_LEAVE'`
3. 📊 **API GET /api/v1/admin/leave-balances** hoạt động nhưng trả về `balances: []` (chưa có data)

## ✅ GIẢI PHÁP NHANH (5 phút)

### Bước 1: Kết nối PostgreSQL

```bash
# Windows (Git Bash hoặc CMD)
psql -U postgres -d dental_clinic

# Hoặc nếu dùng Docker
docker exec -it <postgres_container_name> psql -U postgres -d dental_clinic
```

### Bước 2: Kiểm tra time_off_types hiện tại

```sql
-- Check xem có ANNUAL_LEAVE chưa
SELECT type_id, type_code, type_name 
FROM time_off_types 
WHERE type_code = 'ANNUAL_LEAVE';
```

**Kết quả có thể:**
- ❌ **Empty** → Chưa có, cần INSERT
- ⚠️ **type_id = 'TOT001'** → Có nhưng sai format, cần fix

### Bước 3: Chạy script fix (COPY & PASTE toàn bộ)

```sql
-- ==========================================
-- FIX TIME_OFF_TYPES: type_id = type_code
-- ==========================================

-- Xóa old data (nếu có)
DELETE FROM leave_balance_history WHERE balance_id IN (
    SELECT balance_id FROM employee_leave_balances WHERE time_off_type_id LIKE 'TOT%'
);
DELETE FROM employee_leave_balances WHERE time_off_type_id LIKE 'TOT%';
DELETE FROM time_off_types WHERE type_id LIKE 'TOT%';

-- Insert correct data với type_id = type_code
INSERT INTO time_off_types (type_id, type_code, type_name, is_paid, requires_approval, requires_balance, default_days_per_year, is_active)
VALUES 
('ANNUAL_LEAVE', 'ANNUAL_LEAVE', 'Nghỉ phép năm', TRUE, TRUE, TRUE, 12.0, TRUE),
('SICK_LEAVE', 'SICK_LEAVE', 'Nghỉ ốm (BHXH)', TRUE, TRUE, FALSE, NULL, TRUE),
('UNPAID_PERSONAL', 'UNPAID_PERSONAL', 'Nghỉ việc riêng không lương', FALSE, TRUE, FALSE, NULL, TRUE),
('MATERNITY_LEAVE', 'MATERNITY_LEAVE', 'Nghỉ thai sản (6 tháng)', TRUE, TRUE, FALSE, NULL, TRUE),
('PATERNITY_LEAVE', 'PATERNITY_LEAVE', 'Nghỉ chăm con (5-14 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('MARRIAGE_LEAVE', 'MARRIAGE_LEAVE', 'Nghỉ kết hôn (3 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('BEREAVEMENT_LEAVE', 'BEREAVEMENT_LEAVE', 'Nghỉ tang lễ (1-3 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('EMERGENCY_LEAVE', 'EMERGENCY_LEAVE', 'Nghỉ khẩn cấp', FALSE, TRUE, FALSE, NULL, TRUE),
('STUDY_LEAVE', 'STUDY_LEAVE', 'Nghỉ học tập/đào tạo', TRUE, TRUE, FALSE, NULL, TRUE),
('COMPENSATORY_LEAVE', 'COMPENSATORY_LEAVE', 'Nghỉ bù (sau làm thêm giờ)', TRUE, FALSE, FALSE, NULL, TRUE)
ON CONFLICT (type_id) DO UPDATE SET
    type_code = EXCLUDED.type_code,
    type_name = EXCLUDED.type_name,
    is_paid = EXCLUDED.is_paid,
    requires_approval = EXCLUDED.requires_approval,
    requires_balance = EXCLUDED.requires_balance,
    default_days_per_year = EXCLUDED.default_days_per_year,
    is_active = EXCLUDED.is_active;

-- Verify
SELECT type_id, type_code, type_name, requires_balance, default_days_per_year 
FROM time_off_types 
ORDER BY type_code;
```

**Kết quả mong đợi:**
```
      type_id      |      type_code       |         type_name              | requires_balance | default_days_per_year
-------------------+----------------------+--------------------------------+------------------+----------------------
 ANNUAL_LEAVE      | ANNUAL_LEAVE         | Nghỉ phép năm                  | t                | 12.0
 BEREAVEMENT_LEAVE | BEREAVEMENT_LEAVE    | Nghỉ tang lễ (1-3 ngày)        | t                | null
 COMPENSATORY_LEAVE| COMPENSATORY_LEAVE   | Nghỉ bù (sau làm thêm giờ)     | f                | null
 EMERGENCY_LEAVE   | EMERGENCY_LEAVE      | Nghỉ khẩn cấp                  | t                | null
 MARRIAGE_LEAVE    | MARRIAGE_LEAVE       | Nghỉ kết hôn (3 ngày)          | t                | null
 MATERNITY_LEAVE   | MATERNITY_LEAVE      | Nghỉ thai sản (6 tháng)        | t                | null
 PATERNITY_LEAVE   | PATERNITY_LEAVE      | Nghỉ chăm con (5-14 ngày)      | t                | null
 SICK_LEAVE        | SICK_LEAVE           | Nghỉ ốm (BHXH)                 | f                | null
 STUDY_LEAVE       | STUDY_LEAVE          | Nghỉ học tập/đào tạo           | t                | null
 UNPAID_PERSONAL   | UNPAID_PERSONAL      | Nghỉ việc riêng không lương    | f                | null
(10 rows)
```

### Bước 4: Test API annual-reset (KHÔNG CẦN restart application)

```bash
POST http://localhost:8080/api/v1/admin/leave-balances/annual-reset
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "cycle_year": 2025,
  "apply_to_type_id": "ANNUAL_LEAVE",
  "default_allowance": 12.0
}
```

**Response thành công:**
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "message": "Annual reset hoàn tất",
    "cycle_year": 2025,
    "time_off_type_id": "ANNUAL_LEAVE",
    "default_allowance": 12.0,
    "total_employees": 9,
    "created_count": 9,
    "skipped_count": 0
  }
}
```

### Bước 5: Verify balances đã được tạo

```bash
GET http://localhost:8080/api/v1/admin/leave-balances?cycle_year=2025&time_off_type_id=ANNUAL_LEAVE
Authorization: Bearer {admin_token}
```

**Response mong đợi:**
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "filter": {
      "cycleYear": 2025,
      "timeOffTypeId": "ANNUAL_LEAVE"
    },
    "data": [
      {
        "employeeId": 1,
        "employeeName": "Admin Hệ thống",
        "balances": [
          {
            "timeOffTypeName": "Nghỉ phép năm",
            "totalDaysAllowed": 12.0,
            "daysTaken": 0.0,
            "daysRemaining": 12.0
          }
        ]
      },
      {
        "employeeId": 2,
        "employeeName": "Minh Nguyễn Văn",
        "balances": [
          {
            "timeOffTypeName": "Nghỉ phép năm",
            "totalDaysAllowed": 12.0,
            "daysTaken": 0.0,
            "daysRemaining": 12.0
          }
        ]
      }
      // ... 7 employees khác (total 9)
    ]
  }
}
```

### Bước 6: Test GET single employee balance

```bash
GET http://localhost:8080/api/v1/admin/employees/1/leave-balances?cycle_year=2025
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "employeeId": 1,
    "cycleYear": 2025,
    "balances": [
      {
        "balanceId": 1,
        "timeOffType": {
          "typeId": "ANNUAL_LEAVE",
          "typeName": "Nghỉ phép năm",
          "isPaid": true
        },
        "totalDaysAllowed": 12.0,
        "daysTaken": 0.0,
        "daysRemaining": 12.0
      }
    ]
  }
}
```

---

## 🎯 Checklist

- [ ] Kết nối PostgreSQL thành công
- [ ] Chạy script fix time_off_types
- [ ] Verify có 10 rows với type_id = type_code
- [ ] ANNUAL_LEAVE có default_days_per_year = 12.0
- [ ] Call POST annual-reset thành công (200 OK)
- [ ] GET all balances trả về 9 employees
- [ ] GET single employee balance trả về data

---

## 🚀 Kết quả cuối cùng

Sau khi hoàn thành, bạn sẽ có:
- ✅ 10 time_off_types với format đúng (type_id = type_code)
- ✅ 9 employees có balance cho ANNUAL_LEAVE năm 2025
- ✅ API annual-reset hoạt động cho cả năm hiện tại và năm tới
- ✅ GET all balances và GET single balance đều trả về data đầy đủ
- ✅ **KHÔNG CẦN restart application!**

---

## 📝 Notes quan trọng

1. **Tại sao cần type_id = type_code?**
   - Dễ sử dụng trong API (không cần nhớ TOT001 vs ANNUAL_LEAVE)
   - Frontend có thể dùng trực tiếp type_code
   - Tránh nhầm lẫn giữa type_id và type_code

2. **Tại sao transaction bị rollback?**
   - `@Transactional` annotation ở Service method
   - `TimeOffTypeNotFoundException` được throw từ trong transaction
   - Spring đánh dấu transaction là rollback-only
   - Không thể commit ngay cả khi catch exception

3. **Làm sao biết có lỗi gì?**
   - Check console log Spring Boot
   - Tìm dòng: `"Transaction silently rolled back"`
   - Xem exception gốc phía trên (TimeOffTypeNotFoundException)

---

## 🔧 Alternative: Dùng SQL file thay vì manual

Nếu muốn chạy từ file:

```bash
# Trong terminal
psql -U postgres -d dental_clinic -f src/main/resources/db/migration/fix_time_off_types.sql
```

File đã được tạo sẵn tại: `src/main/resources/db/migration/fix_time_off_types.sql`
