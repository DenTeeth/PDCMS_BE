#  Hướng Dẫn Fix Lỗi Sequence Desync (PERMANENT SOLUTION)

##  Tổng Quan

**Vấn đề**: API POST `/api/v1/fixed-registrations` (và các API khác) bị lỗi:

```
ERROR: duplicate key value violates unique constraint "fixed_shift_registrations_pkey"
Detail: Key (registration_id)=(2) already exists.
```

**Nguyên nhân gốc rễ**:

- File seed data `dental-clinic-seed-data_postgres_v2.sql` INSERT records với hard-coded IDs
- Sau khi INSERT, **THIẾU câu lệnh reset PostgreSQL sequence**
- Khi Hibernate tạo record mới, nó lấy ID từ sequence (bắt đầu từ 1) → conflict với IDs đã tồn tại

**Ảnh hưởng**:

-  Bất kỳ developer/teammate nào chạy seed data đều gặp lỗi này
-  Ảnh hưởng đến nhiều tables: `fixed_shift_registrations`, `holiday_dates`, và các tables khác
-  Lỗi chỉ xuất hiện khi INSERT record MỚI (không phải khi test với data có sẵn)

---

##  Giải Pháp Đã Thực Hiện

### 1️⃣ **Fixed Tables Thiếu Sequence Reset**

Đã thêm sequence reset vào seed data file cho các tables sau:

#### **fixed_shift_registrations** (7 records, IDs 1-7)

```sql
-- Sau INSERT cuối cùng (registration_id=7), thêm:
SELECT setval('fixed_shift_registrations_registration_id_seq',
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1,
    false);
```

#### **holiday_dates** (3 records, IDs 1-3)

```sql
-- Sau INSERT cuối cùng (holiday_id=3), thêm:
SELECT setval('holiday_dates_holiday_id_seq',
    COALESCE((SELECT MAX(holiday_id) FROM holiday_dates), 0) + 1,
    false);
```

### 2️⃣ **Tables Đã Có Sequence Reset** 

Các tables sau **ĐÃ ĐÚNG** trong seed data:

- `base_roles` (line 599)
- `accounts` (line 600)
- `employees` (line 601)
- `patients` (line 602)
- `part_time_slots` (line 940+)

### 3️⃣ **Tables KHÔNG CẦN Sequence Reset**

Các tables sau dùng **VARCHAR primary key** (không dùng SERIAL/IDENTITY):

- `employee_shift_registrations` (ID format: `ESR251101001`)
- `time_off_requests` (ID format: custom String)
- `specializations` (manual Integer ID)
- `work_shifts` (VARCHAR work_shift_id: `WKS_MORNING_01`)
- `time_off_types` (VARCHAR type_id: `TTF_ANNUAL_LEAVE`)

---

##  Validation & Testing

### **Script 1: Validate All Sequences**

File: `validate_all_sequences.sql`

Kiểm tra và fix tất cả sequences:

```bash
docker exec -i postgres-dental psql -U root -d dental_clinic_db < validate_all_sequences.sql
```

**Output mong đợi**:

```
1. base_roles:                      OK
2. accounts:                        OK
3. employees:                       OK
4. patients:                        OK
5. fixed_shift_registrations:       OK
6. part_time_slots:                 OK
7. holiday_dates:                   OK
```

### **Script 2: Fix Sequence Desync (Nếu Cần)**

File: `fix_sequence_issue.sql`

Dùng khi gặp lỗi duplicate key:

```bash
docker exec -i postgres-dental psql -U root -d dental_clinic_db < fix_sequence_issue.sql
```

---

##  Áp Dụng Fix Cho Team

### **Option 1: Fresh Database Setup** (RECOMMENDED)

Nếu teammate setup database mới:

1.  Pull code mới nhất (đã có seed data được fix)
2.  Run Docker Compose: `docker-compose up -d`
3.  Seed data sẽ tự động chạy với sequence resets
4.  KHÔNG GẶP LỖI

### **Option 2: Existing Database**

Nếu đã có database cũ:

**Cách 1 - DROP & RECREATE** (clean slate):

```bash
# 1. Stop application
# 2. Drop database
docker exec -i postgres-dental psql -U root -d postgres -c "DROP DATABASE dental_clinic_db;"

# 3. Recreate database
docker exec -i postgres-dental psql -U root -d postgres -c "CREATE DATABASE dental_clinic_db;"

# 4. Restart application (seed data sẽ tự chạy)
# 5. Hoặc chạy seed data thủ công:
docker exec -i postgres-dental psql -U root -d dental_clinic_db < src/main/resources/db/dental-clinic-seed-data_postgres_v2.sql
```

**Cách 2 - QUICK FIX** (giữ data hiện tại):

```bash
# Chỉ fix sequences mà không mất data
docker exec -i postgres-dental psql -U root -d dental_clinic_db < validate_all_sequences.sql
```

---

##  Khi Nào Gặp Lỗi Này?

### Triệu chứng:

```json
{
  "status": 500,
  "message": "Failed to create fixed shift registration",
  "timestamp": "2025-01-31T...",
  "path": "/api/v1/fixed-registrations"
}
```

### Log error:

```
org.springframework.dao.DataIntegrityViolationException:
  could not execute statement [ERROR: duplicate key value violates unique constraint "fixed_shift_registrations_pkey"
  Detail: Key (registration_id)=(2) already exists.]
```

### Nguyên nhân:

- Sequence đang ở giá trị nhỏ (vd: 2)
- Table đã có record với ID=2 từ seed data
- Hibernate cố INSERT với ID=2 → CONFLICT

---

##  Table Summary

| Table                          | Primary Key Type | Seed Data IDs | Sequence Reset | Status       |
| ------------------------------ | ---------------- | ------------- | -------------- | ------------ |
| `base_roles`                   | SERIAL           | 1-3           |  Line 599    |  OK        |
| `accounts`                     | SERIAL           | 1-21          |  Line 600    |  OK        |
| `employees`                    | SERIAL           | 1-11          |  Line 601    |  OK        |
| `patients`                     | SERIAL           | 1-3           |  Line 602    |  OK        |
| `specializations`              | INTEGER (manual) | 1-7           |  N/A         |  OK        |
| `fixed_shift_registrations`    | BIGSERIAL        | 1-7           |  **ADDED**   |  **FIXED** |
| `part_time_slots`              | BIGSERIAL        | 1-14          |  Line 940+   |  OK        |
| `holiday_dates`                | BIGSERIAL        | 1-3           |  **ADDED**   |  **FIXED** |
| `employee_shift_registrations` | VARCHAR          | ESR...        |  N/A         |  OK        |
| `time_off_requests`            | VARCHAR          | TOR...        |  N/A         |  OK        |
| `work_shifts`                  | VARCHAR          | WKS\_...      |  N/A         |  OK        |
| `time_off_types`               | VARCHAR          | TTF\_...      |  N/A         |  OK        |

---

##  Root Cause Analysis

### Before Fix:

```sql
-- Seed data INSERT với hard-coded IDs
INSERT INTO fixed_shift_registrations (registration_id, ...)
VALUES
(1, ...), (2, ...), (3, ...), ..., (7, ...);

--  THIẾU: Sequence reset
-- → Sequence vẫn ở giá trị mặc định = 1
-- → Lần INSERT tiếp theo: Hibernate lấy nextval() = 1 → CONFLICT với ID=1 đã tồn tại
```

### After Fix:

```sql
-- Seed data INSERT với hard-coded IDs
INSERT INTO fixed_shift_registrations (registration_id, ...)
VALUES
(1, ...), (2, ...), (3, ...), ..., (7, ...);

--  ADDED: Reset sequence về max_id + 1
SELECT setval('fixed_shift_registrations_registration_id_seq',
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1,
    false);
-- → Sequence = 8
-- → Lần INSERT tiếp theo: Hibernate lấy nextval() = 8 → NO CONFLICT 
```

---

##  Contact & Support

**Nếu bạn vẫn gặp lỗi sau khi áp dụng fix:**

1.  Verify bạn đã pull code mới nhất
2.  Run validation script: `validate_all_sequences.sql`
3.  Check sequence values:
   ```sql
   SELECT
     MAX(registration_id) as max_id,
     nextval('fixed_shift_registrations_registration_id_seq') as next_seq
   FROM fixed_shift_registrations;
   ```
4.  Nếu `next_seq <= max_id` → Run `fix_sequence_issue.sql`

**Error Documentation**: Xem file `FIXED_REGISTRATION_ERROR_GUIDE.md` để biết tất cả error codes có thể gặp.

---

##  Change Log

| Date       | Author       | Change                                                    |
| ---------- | ------------ | --------------------------------------------------------- |
| 2025-01-31 | Backend Team |  Added sequence reset for `fixed_shift_registrations`   |
| 2025-01-31 | Backend Team |  Added sequence reset for `holiday_dates`               |
| 2025-01-31 | Backend Team |  Created validation script `validate_all_sequences.sql` |
| 2025-01-31 | Backend Team |  Updated `FIXED_REGISTRATION_ERROR_GUIDE.md`            |

---

##  Kết Luận

**Fix này là PERMANENT SOLUTION** - chỉ cần pull code và seed data mới là OK!

 **Teammates không cần làm gì thêm** nếu setup database mới
 **Seed data đã được fix tận gốc** - không còn sequence desync
 **Validation script** có sẵn để check nếu nghi ngờ có vấn đề
 **Documentation đầy đủ** cho Frontend team

**No more "duplicate key value" errors!** 
