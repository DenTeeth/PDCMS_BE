-- ==================================================
-- DROP VÀ TẠO LẠI DATABASE
-- ==================================================
-- Chạy script này trong DBeaver/pgAdmin
-- Kết nối đến database: postgres (NOT dental_clinic_db)
-- ==================================================

-- Ngắt tất cả connections hiện tại
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'dental_clinic_db'
  AND pid <> pg_backend_pid();

-- Drop database
DROP DATABASE IF EXISTS dental_clinic_db;

-- Tạo lại database mới
CREATE DATABASE dental_clinic_db
    WITH 
    OWNER = root
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Thông báo
SELECT 'Database dental_clinic_db đã được tạo lại thành công!' AS message;
