-- ============================================
-- TEST DATA - EMPLOYEE MANAGEMENT
-- ============================================
-- Insert complete test data including employees
-- ============================================

-- Set character set to UTF-8
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- BƯỚC 1: Insert Roles (nếu chưa có)
-- ============================================

INSERT IGNORE INTO roles (role_id, role_name, description, created_at)
VALUES
('ROLE_DOCTOR', 'Bác sĩ', 'Bác sĩ nha khoa', NOW()),
('ROLE_RECEPTIONIST', 'Lễ tân', 'Nhân viên lễ tân tiếp đón', NOW()),
('ROLE_ACCOUNTANT', 'Kế toán', 'Nhân viên kế toán', NOW()),
('ROLE_WAREHOUSE_MANAGER', 'Quản lý kho', 'Quản lý kho vật tư', NOW());


-- BƯỚC 2: Tạo Accounts
-- ============================================

INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
-- Nha sĩ (Doctors)
('ACC_BS01', 'bs.nguyen.chinh.nha', 'nguyen.chinh.nha@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
('ACC_BS02', 'bs.tran.noi.nha', 'tran.noi.nha@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
-- Nhân viên (Staff)
('ACC_NV01', 'nv.le.le.tan', 'le.le.tan@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
('ACC_NV02', 'nv.pham.ke.toan', 'pham.ke.toan@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
('ACC_NV03', 'nv.hoang.kho', 'hoang.kho@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW());


-- BƯỚC 2: Tạo Chuyên khoa Nha khoa
-- ============================================

INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
('1', 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
('2', 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
('3', 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
('4', 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
('5', 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
('6', 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
('7', 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW());


-- BƯỚC 4: Tạo Employees (INSERT TRỰC TIẾP VÀO DB)
-- ============================================

-- NHA SĨ 1: Chuyên Chỉnh nha + Răng thẩm mỹ
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('EMP_ID_001', 'ACC_BS01', 'ROLE_DOCTOR', 'EMP001', 'Văn A', 'Nguyễn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW());

-- NHA SĨ 2: Chuyên Nội nha + Phục hồi răng
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('EMP_ID_002', 'ACC_BS02', 'ROLE_DOCTOR', 'EMP002', 'Thị B', 'Trần', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW());

-- LỄ TÂN (Receptionist)
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('EMP_ID_003', 'ACC_NV01', 'ROLE_RECEPTIONIST', 'EMP003', 'Thị C', 'Lê', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW());

-- KẾ TOÁN (Accountant)
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('EMP_ID_004', 'ACC_NV02', 'ROLE_ACCOUNTANT', 'EMP004', 'Văn D', 'Phạm', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW());

-- QUẢN LÝ KHO (Warehouse Manager)
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('EMP_ID_005', 'ACC_NV03', 'ROLE_WAREHOUSE_MANAGER', 'EMP005', 'Văn E', 'Hoàng', '0905678901', '1990-11-18', '555 Pasteur, Q3, TPHCM', TRUE, NOW());


-- BƯỚC 5: Gán Chuyên khoa cho Nha sĩ
-- ============================================

-- Nha sĩ 1: Chỉnh nha + Răng thẩm mỹ
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
('EMP_ID_001', '1'),
('EMP_ID_001', '7');

-- Nha sĩ 2: Nội nha + Phục hồi răng
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
('EMP_ID_002', '2'),
('EMP_ID_002', '4');


-- BƯỚC 5: Kiểm tra dữ liệu
-- ============================================

-- Xem tất cả employees
SELECT
    e.employee_code,
    e.first_name,
    e.last_name,
    e.role_id,
    e.phone,
    a.username,
    a.email
FROM employees e
JOIN accounts a ON e.account_id = a.account_id
ORDER BY e.employee_code;

-- Xem employees với chuyên khoa
SELECT
    e.employee_code,
    e.first_name,
    e.last_name,
    GROUP_CONCAT(s.specialization_name SEPARATOR ', ') as specializations
FROM employees e
LEFT JOIN employee_specializations es ON e.employee_id = es.employee_id
LEFT JOIN specializations s ON es.specialization_id = s.specialization_id
GROUP BY e.employee_id, e.employee_code, e.first_name, e.last_name
ORDER BY e.employee_code;


-- BƯỚC 6: Dọn dẹp (nếu cần test lại)
-- ============================================

-- Uncomment để xóa tất cả test data
/*
DELETE FROM employee_specializations WHERE employee_id IN (SELECT employee_id FROM employees WHERE employee_code LIKE 'EMP%');
DELETE FROM employees WHERE employee_code LIKE 'EMP%';
DELETE FROM accounts WHERE account_id LIKE 'ACC_%';
DELETE FROM specializations WHERE specialization_id IN ('1','2','3','4','5','6','7');
*/


-- ============================================
-- THÔNG TIN TÓM TẮT
-- ============================================

/*
EMPLOYEES CREATED:
-----------------
1. EMP001 - Nguyễn Văn A (Nha sĩ) - Chuyên: Chỉnh nha, Răng thẩm mỹ
2. EMP002 - Trần Thị B (Nha sĩ) - Chuyên: Nội nha, Phục hồi răng
3. EMP003 - Lê Thị C (Lễ tân) - Không có chuyên khoa
4. EMP004 - Phạm Văn D (Kế toán) - Không có chuyên khoa
5. EMP005 - Hoàng Văn E (Quản lý kho) - Không có chuyên khoa

ROLE IDs:
---------
3 = Nha sĩ / Doctor
4 = Lễ tân / Receptionist
5 = Kế toán / Accountant
6 = Quản lý kho / Warehouse Manager

SPECIALIZATIONS (chỉ cho Nha sĩ):
--------------------------------
1 = Chỉnh nha
2 = Nội nha
3 = Nha chu
4 = Phục hồi răng
5 = Phẫu thuật hàm mặt
6 = Nha khoa trẻ em
7 = Răng thẩm mỹ
*/
