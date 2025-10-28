-- ============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM
-- COMPLETE TEST DATA WITH RBAC - UPDATED FOR NEW ID STRUCTURE
-- ============================================
-- New ID Structure:
-- - Account, Employee, Patient: INTEGER AUTO_INCREMENT with CODE fields (ACC001, EMP001, PAT001)
-- - CustomerContact, ContactHistory: DATE-BASED VARCHAR(20) (CTC-YYMMDD-001, CTH-YYMMDD-001)
-- - Permission, Role: Name-based VARCHAR IDs
-- - Specialization: INTEGER (1-7)
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;


-- ============================================
-- STEP 1: CREATE ROLES (Dynamic RBAC)
-- ============================================

INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
VALUES
-- Admin role
('ROLE_ADMIN', 'ROLE_ADMIN', 'Quản trị viên hệ thống - Toàn quyền', FALSE, TRUE, NOW()),

-- Clinical roles (REQUIRE SPECIALIZATION)
('ROLE_DOCTOR', 'ROLE_DOCTOR', 'Bác sĩ nha khoa - Khám và điều trị', TRUE, TRUE, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 'Y tá hỗ trợ điều trị', TRUE, TRUE, NOW()),

-- Administrative roles (NO SPECIALIZATION)
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 'Tiếp đón và quản lý lịch hẹn', FALSE, TRUE, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 'Quản lý tài chính và thanh toán', FALSE, TRUE, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 'Quản lý vật tư và thuốc', FALSE, TRUE, NOW()),

-- Patient role
('ROLE_PATIENT', 'ROLE_PATIENT', 'Người bệnh - Xem hồ sơ cá nhân', FALSE, TRUE, NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), requires_specialization = VALUES(requires_specialization);


-- ============================================
-- STEP 2: CREATE PERMISSIONS (Granular Access Control)
-- ============================================

INSERT INTO permissions (permission_id, permission_name, module, description, created_at)
VALUES
-- Account Management
('CREATE_ACCOUNT', 'CREATE_ACCOUNT', 'ACCOUNT', 'Tạo tài khoản mới', NOW()),
('VIEW_ACCOUNT', 'VIEW_ACCOUNT', 'ACCOUNT', 'Xem danh sách tài khoản', NOW()),
('UPDATE_ACCOUNT', 'UPDATE_ACCOUNT', 'ACCOUNT', 'Cập nhật tài khoản', NOW()),
('DELETE_ACCOUNT', 'DELETE_ACCOUNT', 'ACCOUNT', 'Xóa tài khoản', NOW()),

-- Employee Management
('CREATE_EMPLOYEE', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'Tạo nhân viên mới', NOW()),
('VIEW_EMPLOYEE', 'VIEW_EMPLOYEE', 'EMPLOYEE', 'Xem danh sách nhân viên', NOW()),
('UPDATE_EMPLOYEE', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'Cập nhật nhân viên', NOW()),
('DELETE_EMPLOYEE', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'Xóa nhân viên', NOW()),

-- Patient Management
('CREATE_PATIENT', 'CREATE_PATIENT', 'PATIENT', 'Tạo hồ sơ bệnh nhân', NOW()),
('VIEW_PATIENT', 'VIEW_PATIENT', 'PATIENT', 'Xem hồ sơ bệnh nhân', NOW()),
('UPDATE_PATIENT', 'UPDATE_PATIENT', 'PATIENT', 'Cập nhật hồ sơ bệnh nhân', NOW()),
('DELETE_PATIENT', 'DELETE_PATIENT', 'PATIENT', 'Xóa hồ sơ bệnh nhân', NOW()),

-- Treatment Management
('CREATE_TREATMENT', 'CREATE_TREATMENT', 'TREATMENT', 'Tạo phác đồ điều trị', NOW()),
('VIEW_TREATMENT', 'VIEW_TREATMENT', 'TREATMENT', 'Xem phác đồ điều trị', NOW()),
('UPDATE_TREATMENT', 'UPDATE_TREATMENT', 'TREATMENT', 'Cập nhật phác đồ điều trị', NOW()),

-- Appointment Management
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn', NOW()),
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem lịch hẹn', NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', NOW()),

-- Contact (Customer Contacts) Permissions
('VIEW_CONTACT', 'VIEW_CONTACT', 'CONTACT', 'Xem danh sách liên hệ khách hàng', NOW()),
('CREATE_CONTACT', 'CREATE_CONTACT', 'CONTACT', 'Tạo liên hệ khách hàng mới', NOW()),
('UPDATE_CONTACT', 'UPDATE_CONTACT', 'CONTACT', 'Cập nhật liên hệ khách hàng', NOW()),
('DELETE_CONTACT', 'DELETE_CONTACT', 'CONTACT', 'Xóa liên hệ khách hàng', NOW()),

-- Contact History Permissions
('VIEW_CONTACT_HISTORY', 'VIEW_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xem lịch sử liên hệ', NOW()),
('CREATE_CONTACT_HISTORY', 'CREATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Tạo lịch sử liên hệ', NOW()),
('UPDATE_CONTACT_HISTORY', 'UPDATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Cập nhật lịch sử liên hệ', NOW()),
('DELETE_CONTACT_HISTORY', 'DELETE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xóa lịch sử liên hệ', NOW()),

-- Work Shift Management Permissions
('CREATE_WORK_SHIFTS', 'CREATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Tạo mẫu ca làm việc mới', NOW()),
('VIEW_WORK_SHIFTS', 'VIEW_WORK_SHIFTS', 'WORK_SHIFTS', 'Xem danh sách mẫu ca làm việc', NOW()),
('UPDATE_WORK_SHIFTS', 'UPDATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Cập nhật mẫu ca làm việc', NOW()),
('DELETE_WORK_SHIFTS', 'DELETE_WORK_SHIFTS', 'WORK_SHIFTS', 'Xóa/vô hiệu hóa mẫu ca làm việc', NOW())

-- Employee Shift Assignment Permissions (ADD THESE)
('CREATE_EMPLOYEE_SHIFTS', 'CREATE_EMPLOYEE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Phân ca làm việc cho nhân viên', NOW()),
('VIEW_EMPLOYEE_SHIFTS', 'VIEW_EMPLOYEE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Xem ca làm việc đã phân', NOW()),
('UPDATE_SHIFTS', 'UPDATE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Cập nhật trạng thái/ghi chú ca làm (VD: báo vắng)', NOW()),
('DELETE_EMPLOYEE_SHIFTS', 'DELETE_EMPLOYEE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Xóa/hủy ca làm việc đã phân', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ============================================
-- STEP 3: ASSIGN PERMISSIONS TO ROLES (RBAC)
-- ============================================

-- Admin: Full permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Bác sĩ: Clinical permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'),
('ROLE_DOCTOR', 'UPDATE_PATIENT'),
('ROLE_DOCTOR', 'CREATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'),
('ROLE_DOCTOR', 'UPDATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_APPOINTMENT')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Lễ tân: Patient + Appointment management
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'),
('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'),
-- Customer Contact Management
('ROLE_RECEPTIONIST', 'VIEW_CONTACT'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT'),
('ROLE_RECEPTIONIST', 'DELETE_CONTACT'),
-- Contact History Management
('ROLE_RECEPTIONIST', 'VIEW_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'DELETE_CONTACT_HISTORY')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Bệnh nhân: Own records only
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'),
('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'CREATE_APPOINTMENT'),
('ROLE_PATIENT', 'VIEW_APPOINTMENT')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Lễ tân: Patient + Appointment management
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'),
-- ... (các quyền cũ của lễ tân) ...
('ROLE_RECEPTIONIST', 'DELETE_CONTACT_HISTORY'),

-- Employee Shift Management (ADD THESE)
('ROLE_RECEPTIONIST', 'CREATE_EMPLOYEE_SHIFTS'),
('ROLE_RECEPTIONIST', 'VIEW_EMPLOYEE_SHIFTS'),
('ROLE_RECEPTIONIST', 'UPDATE_SHIFTS'),
('ROLE_RECEPTIONIST', 'DELETE_EMPLOYEE_SHIFTS')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
-- ============================================
-- STEP 4: CREATE SPECIALIZATIONS (Integer IDs: 1-7)
-- ============================================

INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
(1, 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
(2, 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
(3, 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
(4, 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
(5, 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
(6, 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
(7, 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW())
ON DUPLICATE KEY UPDATE specialization_name = VALUES(specialization_name);


-- ============================================
-- STEP 5: CREATE ACCOUNTS FOR EMPLOYEES (Auto-increment IDs + Generated Codes)
-- ============================================
-- Password: 123456 (BCrypt hashed)
-- $2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2

-- Admin Account
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('admin', 'admin@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @admin_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@admin_account_id, 3, '0')) WHERE account_id = @admin_account_id;

-- Doctor Account 1
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('nhasi1', 'nhasi1@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @doctor1_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@doctor1_account_id, 3, '0')) WHERE account_id = @doctor1_account_id;

-- Doctor Account 2
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('nhasi2', 'nhasi2@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @doctor2_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@doctor2_account_id, 3, '0')) WHERE account_id = @doctor2_account_id;

-- Receptionist Account
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('letan', 'letan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @receptionist_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@receptionist_account_id, 3, '0')) WHERE account_id = @receptionist_account_id;

-- Accountant Account
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('ketoan', 'ketoan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @accountant_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@accountant_account_id, 3, '0')) WHERE account_id = @accountant_account_id;

-- Nurse Account
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('yta', 'yta@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @nurse_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@nurse_account_id, 3, '0')) WHERE account_id = @nurse_account_id;


-- ============================================
-- STEP 6: ASSIGN ROLES TO ACCOUNTS
-- ============================================

INSERT INTO account_roles (account_id, role_id)
VALUES
-- Admin
(@admin_account_id, 'ROLE_ADMIN'),

-- Doctors
(@doctor1_account_id, 'ROLE_DOCTOR'),
(@doctor2_account_id, 'ROLE_DOCTOR'),

-- Receptionist
(@receptionist_account_id, 'ROLE_RECEPTIONIST'),

-- Accountant
(@accountant_account_id, 'ROLE_ACCOUNTANT'),

-- Nurse
(@nurse_account_id, 'ROLE_NURSE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 7: CREATE EMPLOYEES (Auto-increment IDs + Generated Codes)
-- ============================================

-- Admin Employee
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@admin_account_id, 'ROLE_ADMIN', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', TRUE, NOW());
SET @admin_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@admin_employee_id, 3, '0')) WHERE employee_id = @admin_employee_id;

-- Doctor 1: Chuyên Chỉnh nha + Răng thẩm mỹ
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@doctor1_account_id, 'ROLE_DOCTOR', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW());
SET @doctor1_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@doctor1_employee_id, 3, '0')) WHERE employee_id = @doctor1_employee_id;

-- Doctor 2: Chuyên Nội nha + Phục hồi răng
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@doctor2_account_id, 'ROLE_DOCTOR', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW());
SET @doctor2_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@doctor2_employee_id, 3, '0')) WHERE employee_id = @doctor2_employee_id;

-- Receptionist
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@receptionist_account_id, 'ROLE_RECEPTIONIST', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW());
SET @receptionist_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@receptionist_employee_id, 3, '0')) WHERE employee_id = @receptionist_employee_id;

-- Accountant
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@accountant_account_id, 'ROLE_ACCOUNTANT', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW());
SET @accountant_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@accountant_employee_id, 3, '0')) WHERE employee_id = @accountant_employee_id;

-- Nurse
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@nurse_account_id, 'ROLE_NURSE', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', TRUE, NOW());
SET @nurse_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@nurse_employee_id, 3, '0')) WHERE employee_id = @nurse_employee_id;


-- ============================================
-- STEP 8: ASSIGN SPECIALIZATIONS TO DOCTORS
-- ============================================

-- Doctor 1: Chỉnh nha + Răng thẩm mỹ
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@doctor1_employee_id, 1),
(@doctor1_employee_id, 7)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);

-- Doctor 2: Nội nha + Phục hồi răng
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@doctor2_employee_id, 2),
(@doctor2_employee_id, 4)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);

-- Nurse: Nha khoa trẻ em
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@nurse_employee_id, 6)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);


-- ============================================
-- STEP 9: CREATE PATIENT ACCOUNTS (Auto-increment IDs + Generated Codes)
-- ============================================

-- Patient Account 1
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan1', 'benhnhan1@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient1_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient1_account_id, 3, '0')) WHERE account_id = @patient1_account_id;

-- Patient Account 2
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan2', 'benhnhan2@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient2_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient2_account_id, 3, '0')) WHERE account_id = @patient2_account_id;

-- Patient Account 3
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan3', 'benhnhan3@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient3_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient3_account_id, 3, '0')) WHERE account_id = @patient3_account_id;

-- Assign PATIENT role
INSERT INTO account_roles (account_id, role_id)
VALUES
(@patient1_account_id, 'ROLE_PATIENT'),
(@patient2_account_id, 'ROLE_PATIENT'),
(@patient3_account_id, 'ROLE_PATIENT')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 10: CREATE PATIENTS (Auto-increment IDs + Generated Codes)
-- ============================================

-- Patient 1
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW());
SET @patient1_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient1_id, 3, '0')) WHERE patient_id = @patient1_id;

-- Patient 2
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW());
SET @patient2_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient2_id, 3, '0')) WHERE patient_id = @patient2_id;

-- Patient 3
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW());
SET @patient3_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient3_id, 3, '0')) WHERE patient_id = @patient3_id;


-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- View all employees with roles and accounts
SELECT
    e.employee_code,
    e.first_name,
    e.last_name,
    r.role_name AS role_name,
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
    SELECT account_id FROM account_roles WHERE role_id = 'ROLE_PATIENT'
)
AND a.email = p.email
ORDER BY p.patient_code;

-- View permissions by role
SELECT
    r.role_name AS role_name,
    COUNT(DISTINCT p.permission_id) AS permission_count,
    GROUP_CONCAT(CONCAT(p.module, ':', p.permission_name) SEPARATOR ', ') AS permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.role_id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.permission_id
GROUP BY r.role_id
ORDER BY r.role_name;


-- ============================================
-- LOGIN CREDENTIALS
-- ============================================

/*
DEFAULT PASSWORD FOR ALL ACCOUNTS: 123456

ADMIN:
- Username: admin
- Password: 123456

DOCTORS:
- Username: nhasi1 | Password: 123456
- Username: nhasi2 | Password: 123456

STAFF:
- Username: yta (Y tá) | Password: 123456
- Username: letan (Lễ tân) | Password: 123456
- Username: ketoan (Kế toán) | Password: 123456

PATIENTS:
- Username: benhnhan1 | Password: 123456
- Username: benhnhan2 | Password: 123456
- Username: benhnhan3 | Password: 123456
*/


-- ============================================
-- ADDITIONAL TEST DATA (merged from test-data-employee.sql)
-- The following entries are added with INSERT IGNORE to avoid conflicts
-- if the same role/account/employee already exists in the main seed.
-- ============================================

-- Roles (legacy/test file)
INSERT IGNORE INTO roles (role_id, role_name, description, created_at)
VALUES
('ROLE_DOCTOR', 'Bác sĩ', 'Bác sĩ nha khoa', NOW()),
('ROLE_RECEPTIONIST', 'Lễ tân', 'Nhân viên lễ tân tiếp đón', NOW()),
('ROLE_ACCOUNTANT', 'Kế toán', 'Nhân viên kế toán', NOW()),
('ROLE_WAREHOUSE_MANAGER', 'Quản lý kho', 'Quản lý kho vật tư', NOW());

-- NOTE: Legacy accounts with string IDs removed to prevent duplicate entries
-- The accounts 'ACC_BS01', 'ACC_BS02', etc. were causing duplicates because
-- the account_id field is INTEGER AUTO_INCREMENT, not VARCHAR.

-- Specializations (legacy/test file) - using numeric ids, INSERT IGNORE to avoid collision
INSERT IGNORE INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
('1', 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
('2', 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
('3', 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
('4', 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
('5', 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
('6', 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
('7', 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW());

-- NOTE: Legacy employees with string IDs removed to prevent foreign key issues
-- The employees 'EMP_ID_001', 'EMP_ID_002', etc. were referencing non-existent
-- account IDs 'ACC_BS01', 'ACC_BS02', etc.

-- ============================================
-- STEP 7.5: CREATE SHIFT TABLES
-- ============================================

-- Bảng mẫu ca làm việc (Work Shift Templates)
CREATE TABLE IF NOT EXISTS work_shifts (
    work_shift_id VARCHAR(20) NOT NULL,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (work_shift_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng phân ca làm việc cho nhân viên (Employee Shift Assignments)
CREATE TABLE IF NOT EXISTS employee_shifts (
    employee_shift_id VARCHAR(20) NOT NULL,
    employee_id INT NOT NULL,
    work_shift_id VARCHAR(20) NOT NULL,
    work_date DATE NOT NULL,
    status ENUM('PENDING', 'SCHEDULED', 'ABSENT', 'COMPLETED', 'CANCELLED', 'ON_LEAVE') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    source VARCHAR(50) DEFAULT 'MANUAL',
    is_overtime BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_shift_id),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (work_shift_id) REFERENCES work_shifts(work_shift_id),
    -- Thêm index để tối ưu tìm kiếm
    INDEX idx_employee_date (employee_id, work_date),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- ============================================
-- STEP 11: CREATE SHIFT DATA
-- ============================================

-- Insert mẫu ca làm (Work Shift Templates)
INSERT INTO work_shifts (work_shift_id, shift_name, start_time, end_time, is_active)
VALUES
('WS_MORNING', 'Ca Sáng', '08:00:00', '12:00:00', TRUE),
('WS_AFTERNOON', 'Ca Chiều', '13:30:00', '17:30:00', TRUE),
('WS_FULLDAY', 'Ca Cả Ngày', '08:00:00', '17:30:00', TRUE)
ON DUPLICATE KEY UPDATE shift_name = VALUES(shift_name);


-- Insert ca làm chi tiết cho nhân viên (Employee Shift Assignments)
-- Bao gồm cả ca làm EMS251030002 để bạn test PATCH
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_shift_id, work_date, status, notes)
VALUES
-- Ca làm bạn đang test (Bác sĩ 1)
('EMS251030002', @doctor1_employee_id, 'WS_MORNING', '2025-10-30', 'SCHEDULED', 'Ca làm đã được xếp lịch.'),

-- Ca làm đã hoàn thành (Bác sĩ 2) -> Dùng để test lỗi 409
('EMS251030001', @doctor2_employee_id, 'WS_MORNING', '2025-10-30', 'COMPLETED', 'Đã hoàn thành ca.'),

-- Ca làm đã bị hủy (Y tá) -> Dùng để test lỗi 409
('EMS251030003', @nurse_employee_id, 'WS_MORNING', '2025-10-30', 'CANCELLED', 'Đã hủy ca.'),

-- Một số ca khác
('EMS251031001', @doctor1_employee_id, 'WS_MORNING', '2025-10-31', 'SCHEDULED', ''),
('EMS251031002', @doctor2_employee_id, 'WS_AFTERNOON', '2025-10-31', 'SCHEDULED', ''),
('EMS251031003', @receptionist_employee_id, 'WS_FULLDAY', '2025-10-31', 'SCHEDULED', '')
ON DUPLICATE KEY UPDATE status = VALUES(status), notes = VALUES(notes);