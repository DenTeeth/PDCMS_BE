# Hướng Dẫn Update Seed Data Cho Production

## 🎯 Vấn Đề
Khi có thêm permissions, roles, hoặc data mới trong `dental-clinic-seed-data.sql`, DB production cần được cập nhật để FE hoạt động đúng.

## ✅ Giải Pháp - Tự Động Load Seed Data

### 1. **Lần Đầu Deploy (Fresh Install)**
Seed data sẽ tự động được load khi container PostgreSQL khởi động lần đầu.

```bash
docker-compose up -d
```

### 2. **Update Seed Data (Có Thay Đổi Mới)**

#### Option A: Xóa Volume và Tạo Lại (CẢNH BÁO: MẤT DỮ LIỆU)
```bash
# ⚠️ CẢNH BÁO: Lệnh này sẽ XÓA TOÀN BỘ dữ liệu trong DB
docker-compose down -v  # Stop và xóa volumes
docker-compose up -d    # Khởi động lại, seed data sẽ tự động load
```

#### Option B: Force Reseed (GIỮ NGUYÊN DỮ LIỆU, CHỈ THÊM MỚI)
```bash
# Bước 1: Set environment variable
export FORCE_RESEED=true

# Bước 2: Recreate postgres container
docker-compose up -d --force-recreate postgres

# Bước 3: Kiểm tra logs
docker-compose logs -f postgres

# Bước 4: Reset lại biến (sau khi xong)
export FORCE_RESEED=false
```

#### Option C: Chạy SQL Trực Tiếp (An Toàn Nhất)
```bash
# Bước 1: Copy file SQL vào container
docker cp ./src/main/resources/db/dental-clinic-seed-data.sql dentalclinic-postgres:/tmp/

# Bước 2: Exec vào container và chạy SQL
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -f /tmp/dental-clinic-seed-data.sql

# Hoặc chạy trực tiếp từ host
docker exec -i dentalclinic-postgres psql -U root -d dental_clinic_db < ./src/main/resources/db/dental-clinic-seed-data.sql
```

#### Option D: Chỉ Update Permissions (Nhanh Nhất)
```bash
# Chạy SQL query trực tiếp cho permissions mới
docker exec -i dentalclinic-postgres psql -U root -d dental_clinic_db <<EOF
-- Thêm permission mới nếu chưa có
INSERT INTO permissions (permission_code, permission_name, group_code, description, sort_order, parent_permission_code, is_active, created_at)
VALUES ('VIEW_NOTIFICATION', 'VIEW_NOTIFICATION', 'NOTIFICATION', 'Xem thông báo của bản thân', 300, NULL, TRUE, NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- Gán permission cho tất cả roles
INSERT INTO role_permissions (role_code, permission_code)
VALUES 
    ('ROLE_DENTIST', 'VIEW_NOTIFICATION'),
    ('ROLE_NURSE', 'VIEW_NOTIFICATION'),
    ('ROLE_DENTIST_INTERN', 'VIEW_NOTIFICATION'),
    ('ROLE_RECEPTIONIST', 'VIEW_NOTIFICATION'),
    ('ROLE_MANAGER', 'VIEW_NOTIFICATION'),
    ('ROLE_ACCOUNTANT', 'VIEW_NOTIFICATION'),
    ('ROLE_INVENTORY_MANAGER', 'VIEW_NOTIFICATION'),
    ('ROLE_PATIENT', 'VIEW_NOTIFICATION')
ON CONFLICT DO NOTHING;

-- Verify
SELECT * FROM permissions WHERE permission_code = 'VIEW_NOTIFICATION';
SELECT * FROM role_permissions WHERE permission_code = 'VIEW_NOTIFICATION';
EOF
```

## 🔄 Workflow Khi Có Update Seed Data

### Step 1: Update File SQL
Chỉnh sửa `src/main/resources/db/dental-clinic-seed-data.sql`

### Step 2: Commit Code
```bash
git add src/main/resources/db/dental-clinic-seed-data.sql
git commit -m "feat: Add new permissions/data to seed file"
git push origin main
```

### Step 3: Deploy Lên Production
```bash
# SSH vào server
ssh user@pdcms.duckdns.org

# Pull code mới
cd /path/to/PDCMS_BE
git pull origin main

# Option: Chạy SQL trực tiếp (KHUYẾN NGHỊ)
docker exec -i dentalclinic-postgres psql -U root -d dental_clinic_db < ./src/main/resources/db/dental-clinic-seed-data.sql

# Hoặc: Xóa và tạo lại (nếu development/testing)
docker-compose down -v
docker-compose up -d
```

## 🔍 Kiểm Tra Seed Data Đã Load

```bash
# Kiểm tra permissions
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c "SELECT COUNT(*) FROM permissions;"

# Kiểm tra roles
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c "SELECT COUNT(*) FROM roles;"

# Kiểm tra role_permissions
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c "SELECT COUNT(*) FROM role_permissions;"

# Xem chi tiết VIEW_NOTIFICATION permission
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c "
SELECT p.permission_code, p.permission_name, rp.role_code 
FROM permissions p 
LEFT JOIN role_permissions rp ON p.permission_code = rp.permission_code 
WHERE p.permission_code = 'VIEW_NOTIFICATION';
"
```

## 📝 Files Liên Quan

- **docker-compose.yml**: Config mount seed data files
- **src/main/resources/db/enums.sql**: Enum types
- **src/main/resources/db/dental-clinic-seed-data.sql**: Master data (permissions, roles, sample data)
- **src/main/resources/db/init-seed-data.sh**: Auto-load script

## ⚠️ Lưu Ý Quan Trọng

### 1. **Production Data Safety**
- Không bao giờ dùng `docker-compose down -v` trên production với dữ liệu thật
- Luôn backup trước khi update: `docker exec dentalclinic-postgres pg_dump -U root dental_clinic_db > backup.sql`

### 2. **Idempotent SQL**
Seed data SQL nên dùng `ON CONFLICT DO NOTHING` hoặc `INSERT ... WHERE NOT EXISTS` để có thể chạy nhiều lần an toàn:

```sql
-- ✅ Tốt: Idempotent
INSERT INTO permissions (permission_code, ...) 
VALUES ('VIEW_NOTIFICATION', ...)
ON CONFLICT (permission_code) DO NOTHING;

-- ❌ Tệ: Lỗi nếu chạy lại
INSERT INTO permissions (permission_code, ...) 
VALUES ('VIEW_NOTIFICATION', ...);
```

### 3. **Testing**
Luôn test trên local/staging trước khi apply lên production:

```bash
# Local testing
docker-compose -f docker-compose.yml up -d
docker-compose logs -f postgres

# Staging testing
ssh staging-server
docker-compose up -d
```

## 🚀 Quick Fix Cho Production Hiện Tại

Nếu production đang thiếu `VIEW_NOTIFICATION` permission, chạy ngay:

```bash
# SSH vào server production
ssh user@pdcms.duckdns.org

# Chạy SQL fix
docker exec -i dentalclinic-postgres psql -U root -d dental_clinic_db <<'EOF'
INSERT INTO permissions (permission_code, permission_name, group_code, description, sort_order, parent_permission_code, is_active, created_at)
VALUES ('VIEW_NOTIFICATION', 'VIEW_NOTIFICATION', 'NOTIFICATION', 'Xem thông báo của bản thân', 300, NULL, TRUE, NOW())
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO role_permissions (role_code, permission_code)
SELECT role_code, 'VIEW_NOTIFICATION'
FROM roles
WHERE role_code IN ('ROLE_DENTIST', 'ROLE_NURSE', 'ROLE_DENTIST_INTERN', 'ROLE_RECEPTIONIST', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_INVENTORY_MANAGER', 'ROLE_PATIENT')
ON CONFLICT DO NOTHING;

SELECT 'Permissions fixed!' as result;
EOF

# Verify
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db -c "SELECT COUNT(*) FROM role_permissions WHERE permission_code = 'VIEW_NOTIFICATION';"
```

Sau khi chạy xong, FE sẽ có thể gọi API `/api/v1/notifications` thành công! ✅
