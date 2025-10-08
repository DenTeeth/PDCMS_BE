-- ============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM
-- COMPLETE TEST DATA WITH RBAC
-- ============================================
-- UUID Format: VARCHAR(36)
-- Auto-generate Account for every Employee/Patient
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- STEP 1: CREATE ROLES (Dynamic RBAC)
-- ============================================

INSERT INTO roles (role_id, name, description, is_active, created_at)
VALUES
-- Admin role
('550e8400-e29b-41d4-a716-446655440001', 'Admin', 'Quản trị viên hệ thống - Toàn quyền', TRUE, NOW()),

-- Clinical roles
('550e8400-e29b-41d4-a716-446655440002', 'Bác sĩ', 'Bác sĩ nha khoa - Khám và điều trị', TRUE, NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'Y tá', 'Y tá hỗ trợ điều trị', TRUE, NOW()),

-- Administrative roles
('550e8400-e29b-41d4-a716-446655440004', 'Lễ tân', 'Tiếp đón và quản lý lịch hẹn', TRUE, NOW()),
('550e8400-e29b-41d4-a716-446655440005', 'Kế toán', 'Quản lý tài chính và thanh toán', TRUE, NOW()),
('550e8400-e29b-41d4-a716-446655440006', 'Quản lý kho', 'Quản lý vật tư và thuốc', TRUE, NOW()),

-- Patient role
('550e8400-e29b-41d4-a716-446655440007', 'Bệnh nhân', 'Người bệnh - Xem hồ sơ cá nhân', TRUE, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);


-- ============================================
-- STEP 2: CREATE PERMISSIONS (Granular Access Control)
-- ============================================

INSERT INTO permissions (permission_id, resource, action, description, created_at)
VALUES
-- Account Management
('660e8400-e29b-41d4-a716-446655440001', 'ACCOUNT', 'CREATE', 'Tạo tài khoản mới', NOW()),
('660e8400-e29b-41d4-a716-446655440002', 'ACCOUNT', 'VIEW', 'Xem danh sách tài khoản', NOW()),
('660e8400-e29b-41d4-a716-446655440003', 'ACCOUNT', 'UPDATE', 'Cập nhật tài khoản', NOW()),
('660e8400-e29b-41d4-a716-446655440004', 'ACCOUNT', 'DELETE', 'Xóa tài khoản', NOW()),

-- Employee Management
('660e8400-e29b-41d4-a716-446655440011', 'EMPLOYEE', 'CREATE', 'Tạo nhân viên mới', NOW()),
('660e8400-e29b-41d4-a716-446655440012', 'EMPLOYEE', 'VIEW', 'Xem danh sách nhân viên', NOW()),
('660e8400-e29b-41d4-a716-446655440013', 'EMPLOYEE', 'UPDATE', 'Cập nhật nhân viên', NOW()),
('660e8400-e29b-41d4-a716-446655440014', 'EMPLOYEE', 'DELETE', 'Xóa nhân viên', NOW()),

-- Patient Management
('660e8400-e29b-41d4-a716-446655440021', 'PATIENT', 'CREATE', 'Tạo hồ sơ bệnh nhân', NOW()),
('660e8400-e29b-41d4-a716-446655440022', 'PATIENT', 'VIEW', 'Xem hồ sơ bệnh nhân', NOW()),
('660e8400-e29b-41d4-a716-446655440023', 'PATIENT', 'UPDATE', 'Cập nhật hồ sơ bệnh nhân', NOW()),
('660e8400-e29b-41d4-a716-446655440024', 'PATIENT', 'DELETE', 'Xóa hồ sơ bệnh nhân', NOW()),

-- Treatment Management
('660e8400-e29b-41d4-a716-446655440031', 'TREATMENT', 'CREATE', 'Tạo phác đồ điều trị', NOW()),
('660e8400-e29b-41d4-a716-446655440032', 'TREATMENT', 'VIEW', 'Xem phác đồ điều trị', NOW()),
('660e8400-e29b-41d4-a716-446655440033', 'TREATMENT', 'UPDATE', 'Cập nhật phác đồ điều trị', NOW()),

-- Appointment Management
('660e8400-e29b-41d4-a716-446655440041', 'APPOINTMENT', 'CREATE', 'Đặt lịch hẹn', NOW()),
('660e8400-e29b-41d4-a716-446655440042', 'APPOINTMENT', 'VIEW', 'Xem lịch hẹn', NOW()),
('660e8400-e29b-41d4-a716-446655440043', 'APPOINTMENT', 'UPDATE', 'Cập nhật lịch hẹn', NOW()),
('660e8400-e29b-41d4-a716-446655440044', 'APPOINTMENT', 'DELETE', 'Hủy lịch hẹn', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);


-- ============================================
-- STEP 3: ASSIGN PERMISSIONS TO ROLES (RBAC)
-- ============================================

-- Admin: Full permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440001', permission_id FROM permissions
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Bác sĩ: Clinical permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440022'), -- VIEW_PATIENT
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440023'), -- UPDATE_PATIENT
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440031'), -- CREATE_TREATMENT
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440032'), -- VIEW_TREATMENT
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440033'), -- UPDATE_TREATMENT
('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440042')  -- VIEW_APPOINTMENT
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Lễ tân: Patient + Appointment management
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440021'), -- CREATE_PATIENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440022'), -- VIEW_PATIENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440023'), -- UPDATE_PATIENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440041'), -- CREATE_APPOINTMENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440042'), -- VIEW_APPOINTMENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440043'), -- UPDATE_APPOINTMENT
('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440044')  -- DELETE_APPOINTMENT
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Bệnh nhân: Own records only
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('550e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440022'), -- VIEW_PATIENT (own)
('550e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440032'), -- VIEW_TREATMENT (own)
('550e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440041'), -- CREATE_APPOINTMENT (own)
('550e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440042')  -- VIEW_APPOINTMENT (own)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 4: CREATE SPECIALIZATIONS (Optional for Doctors)
-- ============================================

INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
('770e8400-e29b-41d4-a716-446655440001', 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440002', 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440003', 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440004', 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440005', 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440006', 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
('770e8400-e29b-41d4-a716-446655440007', 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW())
ON DUPLICATE KEY UPDATE specialization_name = VALUES(specialization_name);


-- ============================================
-- STEP 5: CREATE ACCOUNTS FOR EMPLOYEES
-- ============================================
-- Password: DentalClinic@2025 (BCrypt hashed)
-- $2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6

-- Admin Account
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@dentalclinic.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Doctor Accounts
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440002', 'bs.nguyen.van.a', 'nguyen.van.a@dentalclinic.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440003', 'bs.tran.thi.b', 'tran.thi.b@dentalclinic.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Receptionist Account
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440004', 'le.thi.c', 'le.thi.c@dentalclinic.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Accountant Account
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440005', 'pham.van.d', 'pham.van.d@dentalclinic.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);


-- ============================================
-- STEP 6: ASSIGN ROLES TO ACCOUNTS
-- ============================================

INSERT INTO account_roles (account_id, role_id)
VALUES
-- Admin
('880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001'),

-- Doctors
('880e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002'),
('880e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002'),

-- Receptionist
('880e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004'),

-- Accountant
('880e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 7: CREATE EMPLOYEES
-- ============================================

-- Admin Employee
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'EMP001', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', TRUE, NOW())
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);

-- Doctor 1: Chuyên Chỉnh nha + Răng thẩm mỹ
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'EMP002', 'Văn A', 'Nguyễn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW())
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);

-- Doctor 2: Chuyên Nội nha + Phục hồi răng
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 'EMP003', 'Thị B', 'Trần', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW())
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);

-- Receptionist
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'EMP004', 'Thị C', 'Lê', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW())
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);

-- Accountant
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440005', '880e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'EMP005', 'Văn D', 'Phạm', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW())
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);


-- ============================================
-- STEP 8: ASSIGN SPECIALIZATIONS TO DOCTORS
-- ============================================

-- Doctor 1: Chỉnh nha + Răng thẩm mỹ
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001'),
('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440007')
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);

-- Doctor 2: Nội nha + Phục hồi răng
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002'),
('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440004')
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);


-- ============================================
-- STEP 9: CREATE PATIENT ACCOUNTS
-- ============================================

INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440101', 'patient.nguyen.khang', 'khang.nguyen@email.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440102', 'patient.tran.lan', 'lan.tran@email.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440103', 'patient.le.duc', 'duc.le@email.com', '$2a$10$N.zmdr9k7uOCQQVbMhOHOe6LwXJm5k4h9wJQCn1lSaGEQXrNTbxG6', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Assign PATIENT role
INSERT INTO account_roles (account_id, role_id)
VALUES
('880e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440007'),
('880e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440007'),
('880e8400-e29b-41d4-a716-446655440103', '550e8400-e29b-41d4-a716-446655440007')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 10: CREATE PATIENTS
-- ============================================

INSERT INTO patients (patient_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES
('aa0e8400-e29b-41d4-a716-446655440101', 'PT001', 'Văn Khang', 'Nguyễn', 'khang.nguyen@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440102', 'PT002', 'Thị Lan', 'Trần', 'lan.tran@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440103', 'PT003', 'Minh Đức', 'Lê', 'duc.le@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_code = VALUES(patient_code);


-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- View all employees with roles and accounts
SELECT
    e.employee_code,
    e.first_name,
    e.last_name,
    r.name AS role_name,
    a.username,
    a.email,
    GROUP_CONCAT(s.specialization_name SEPARATOR ', ') AS specializations
FROM employees e
LEFT JOIN roles r ON e.role_id = r.role_id
LEFT JOIN accounts a ON e.account_id = a.account_id
LEFT JOIN employee_specializations es ON e.employee_id = es.employee_id
LEFT JOIN specializations s ON es.specialization_id = s.specialization_id
GROUP BY e.employee_id
ORDER BY e.employee_code;

-- View all patients with accounts
SELECT
    p.patient_code,
    p.first_name,
    p.last_name,
    p.email,
    p.phone,
    a.username,
    CASE WHEN a.account_id IS NOT NULL THEN 'Yes' ELSE 'No' END AS has_account
FROM patients p
LEFT JOIN accounts a ON a.account_id IN (
    SELECT account_id FROM account_roles WHERE role_id = '550e8400-e29b-41d4-a716-446655440007'
)
AND a.email = p.email
ORDER BY p.patient_code;

-- View permissions by role
SELECT
    r.name AS role_name,
    COUNT(DISTINCT p.permission_id) AS permission_count,
    GROUP_CONCAT(CONCAT(p.resource, ':', p.action) SEPARATOR ', ') AS permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.role_id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.permission_id
GROUP BY r.role_id
ORDER BY r.name;


-- ============================================
-- LOGIN CREDENTIALS
-- ============================================

/*
DEFAULT PASSWORD FOR ALL ACCOUNTS: DentalClinic@2025

ADMIN:
- Username: admin
- Password: DentalClinic@2025

DOCTORS:
- Username: bs.nguyen.van.a | Password: DentalClinic@2025
- Username: bs.tran.thi.b | Password: DentalClinic@2025

STAFF:
- Username: le.thi.c (Lễ tân) | Password: DentalClinic@2025
- Username: pham.van.d (Kế toán) | Password: DentalClinic@2025

PATIENTS:
- Username: patient.nguyen.khang | Password: DentalClinic@2025
- Username: patient.tran.lan | Password: DentalClinic@2025
- Username: patient.le.duc | Password: DentalClinic@2025
*/


-- ============================================
-- CLEANUP (if needed)
-- ============================================

/*
-- Uncomment to reset all data

DELETE FROM employee_specializations;
DELETE FROM account_roles;
DELETE FROM role_permissions;
DELETE FROM employees;
DELETE FROM patients;
DELETE FROM accounts;
DELETE FROM specializations;
DELETE FROM permissions;
DELETE FROM roles;
*/
