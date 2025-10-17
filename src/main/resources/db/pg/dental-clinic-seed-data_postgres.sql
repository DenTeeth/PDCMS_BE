-- ============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM - POSTGRESQL VERSION
-- COMPLETE TEST DATA WITH RBAC - UPDATED FOR NEW ID STRUCTURE
-- ============================================
-- New ID Structure:
-- - Account, Employee, Patient: INTEGER AUTO_INCREMENT (SERIAL) with CODE fields (ACC001, EMP001, PAT001)
-- - CustomerContact, ContactHistory: DATE-BASED VARCHAR(20) (CTC-YYMMDD-001, CTH-YYMMDD-001)
-- - Permission, Role: Name-based VARCHAR IDs
-- - Specialization: INTEGER (1-7)
-- ============================================

-- PostgreSQL does not need SET NAMES - encoding is set at database level

-- Create core tables if they don't exist
CREATE TABLE IF NOT EXISTS roles (
  role_id VARCHAR(64) PRIMARY KEY,
  role_name VARCHAR(200) NOT NULL,
  description TEXT,
  requires_specialization BOOLEAN DEFAULT false,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
  permission_id VARCHAR(64) PRIMARY KEY,
  permission_name VARCHAR(200),
  module VARCHAR(100),
  description TEXT,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id VARCHAR(64) REFERENCES roles(role_id),
  permission_id VARCHAR(64) REFERENCES permissions(permission_id),
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS specializations (
  specialization_id SERIAL PRIMARY KEY,
  specialization_code VARCHAR(50),
  specialization_name VARCHAR(200),
  description TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
  account_id SERIAL PRIMARY KEY,
  account_code VARCHAR(50) UNIQUE,
  username VARCHAR(150) UNIQUE,
  email VARCHAR(200),
  password VARCHAR(255),
  status VARCHAR(50),
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_roles (
  account_id INTEGER REFERENCES accounts(account_id),
  role_id VARCHAR(64) REFERENCES roles(role_id),
  PRIMARY KEY (account_id, role_id)
);

CREATE TABLE IF NOT EXISTS employees (
  employee_id SERIAL PRIMARY KEY,
  employee_code VARCHAR(50) UNIQUE,
  account_id INTEGER REFERENCES accounts(account_id),
  role_id VARCHAR(64) REFERENCES roles(role_id),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(50),
  date_of_birth DATE,
  address TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee_specializations (
  employee_id INTEGER REFERENCES employees(employee_id),
  specialization_id INTEGER REFERENCES specializations(specialization_id),
  PRIMARY KEY (employee_id, specialization_id)
);

CREATE TABLE IF NOT EXISTS patients (
  patient_id SERIAL PRIMARY KEY,
  patient_code VARCHAR(50) UNIQUE,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  email VARCHAR(200),
  phone VARCHAR(50),
  date_of_birth DATE,
  address TEXT,
  gender VARCHAR(20),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_employee_code ON employees (employee_code);
CREATE INDEX IF NOT EXISTS idx_account_username ON accounts (username);
CREATE INDEX IF NOT EXISTS idx_patient_code ON patients (patient_code);
CREATE INDEX IF NOT EXISTS idx_account_code ON accounts (account_code);

-- ============================================
-- STEP 1: CREATE ROLES (Dynamic RBAC)
-- ============================================

INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
VALUES
-- Admin role
('ROLE_ADMIN', 'ROLE_ADMIN', 'Quản trị viên hệ thống - Toàn quyền', false, true, NOW()),

-- Clinical roles (REQUIRE SPECIALIZATION)
('ROLE_DOCTOR', 'ROLE_DOCTOR', 'Bác sĩ nha khoa - Khám và điều trị', true, true, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 'Y tá hỗ trợ điều trị', true, true, NOW()),

-- Administrative roles (NO SPECIALIZATION)
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 'Tiếp đón và quản lý lịch hẹn', false, true, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 'Quản lý tài chính và thanh toán', false, true, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 'Quản lý vật tư và thuốc', false, true, NOW()),

-- Patient role
('ROLE_PATIENT', 'ROLE_PATIENT', 'Người bệnh - Xem hồ sơ cá nhân', false, true, NOW())
ON CONFLICT (role_id) DO UPDATE
  SET role_name = EXCLUDED.role_name,
      requires_specialization = EXCLUDED.requires_specialization;


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
ON CONFLICT (permission_id) DO UPDATE
  SET description = EXCLUDED.description;


-- ============================================
-- STEP 3: ASSIGN PERMISSIONS TO ROLES (RBAC)
-- ============================================

-- Admin: Full permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions
ON CONFLICT DO NOTHING;

-- Doctor: Patient and treatment permissions
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'),
('ROLE_DOCTOR', 'UPDATE_PATIENT'),
('ROLE_DOCTOR', 'CREATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'),
('ROLE_DOCTOR', 'UPDATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_APPOINTMENT')
ON CONFLICT DO NOTHING;

-- Receptionist: Patient and appointment management
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'),
('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'VIEW_CONTACT'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT'),
('ROLE_RECEPTIONIST', 'VIEW_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT_HISTORY')
ON CONFLICT DO NOTHING;

-- Patient: View-only access
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'),
('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'CREATE_APPOINTMENT'),
('ROLE_PATIENT', 'VIEW_APPOINTMENT')
ON CONFLICT DO NOTHING;


-- ============================================
-- STEP 4: CREATE SPECIALIZATIONS (Integer IDs 1-7)
-- ============================================

-- Use INSERT ON CONFLICT to handle SERIAL sequence
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
(1, 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', true, NOW()),
(2, 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', true, NOW()),
(3, 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', true, NOW()),
(4, 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', true, NOW()),
(5, 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', true, NOW()),
(6, 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', true, NOW()),
(7, 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', true, NOW())
ON CONFLICT (specialization_id) DO UPDATE
  SET specialization_name = EXCLUDED.specialization_name;

-- Reset sequence to 8 for future inserts
SELECT setval('specializations_specialization_id_seq', 8, false);


-- ============================================
-- STEP 5: CREATE ACCOUNTS FOR EMPLOYEES
-- ============================================
-- Password: 123456 (BCrypt hashed)
-- PostgreSQL uses RETURNING to get the inserted ID

-- Admin Account
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('admin', 'admin@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Doctor Account 1
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('nhasi1', 'nhasi1@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Doctor Account 2
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('nhasi2', 'nhasi2@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Receptionist Account
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('letan', 'letan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Accountant Account
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('ketoan', 'ketoan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Nurse Account
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('yta', 'yta@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);


-- ============================================
-- STEP 6: ASSIGN ROLES TO ACCOUNTS
-- ============================================

INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_ADMIN' FROM accounts a WHERE a.username = 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_DOCTOR' FROM accounts a WHERE a.username IN ('nhasi1', 'nhasi2')
ON CONFLICT DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_RECEPTIONIST' FROM accounts a WHERE a.username = 'letan'
ON CONFLICT DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_ACCOUNTANT' FROM accounts a WHERE a.username = 'ketoan'
ON CONFLICT DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_NURSE' FROM accounts a WHERE a.username = 'yta'
ON CONFLICT DO NOTHING;


-- ============================================
-- STEP 7: CREATE EMPLOYEES
-- ============================================

-- Admin Employee
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_ADMIN', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', true, NOW()
  FROM accounts a WHERE a.username = 'admin'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);

-- Doctor 1
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_DOCTOR', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', true, NOW()
  FROM accounts a WHERE a.username = 'nhasi1'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);

-- Doctor 2
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_DOCTOR', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', true, NOW()
  FROM accounts a WHERE a.username = 'nhasi2'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);

-- Receptionist
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_RECEPTIONIST', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', true, NOW()
  FROM accounts a WHERE a.username = 'letan'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);

-- Accountant
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_ACCOUNTANT', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', true, NOW()
  FROM accounts a WHERE a.username = 'ketoan'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);

-- Nurse
WITH ins AS (
  INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
  SELECT a.account_id, 'ROLE_NURSE', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', true, NOW()
  FROM accounts a WHERE a.username = 'yta'
  ON CONFLICT (account_id) DO NOTHING
  RETURNING employee_id
)
UPDATE employees SET employee_code = 'EMP' || LPAD(employee_id::text, 3, '0')
WHERE employee_id = (SELECT employee_id FROM ins);


-- ============================================
-- STEP 8: ASSIGN SPECIALIZATIONS TO DOCTORS
-- ============================================

-- Doctor 1: Chỉnh nha + Răng thẩm mỹ
INSERT INTO employee_specializations (employee_id, specialization_id)
SELECT e.employee_id, 1 FROM employees e JOIN accounts a ON e.account_id = a.account_id WHERE a.username = 'nhasi1'
UNION ALL
SELECT e.employee_id, 7 FROM employees e JOIN accounts a ON e.account_id = a.account_id WHERE a.username = 'nhasi1'
ON CONFLICT DO NOTHING;

-- Doctor 2: Nội nha + Phục hồi răng
INSERT INTO employee_specializations (employee_id, specialization_id)
SELECT e.employee_id, 2 FROM employees e JOIN accounts a ON e.account_id = a.account_id WHERE a.username = 'nhasi2'
UNION ALL
SELECT e.employee_id, 4 FROM employees e JOIN accounts a ON e.account_id = a.account_id WHERE a.username = 'nhasi2'
ON CONFLICT DO NOTHING;

-- Nurse: Nha khoa trẻ em
INSERT INTO employee_specializations (employee_id, specialization_id)
SELECT e.employee_id, 6 FROM employees e JOIN accounts a ON e.account_id = a.account_id WHERE a.username = 'yta'
ON CONFLICT DO NOTHING;


-- ============================================
-- STEP 9: CREATE PATIENT ACCOUNTS
-- ============================================

-- Patient Account 1
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('benhnhan1', 'benhnhan1@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Patient Account 2
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('benhnhan2', 'benhnhan2@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Patient Account 3
WITH ins AS (
  INSERT INTO accounts (username, email, password, status, created_at)
  VALUES ('benhnhan3', 'benhnhan3@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
  ON CONFLICT (username) DO UPDATE SET email = EXCLUDED.email
  RETURNING account_id
)
UPDATE accounts SET account_code = 'ACC' || LPAD(account_id::text, 3, '0')
WHERE account_id = (SELECT account_id FROM ins);

-- Assign PATIENT role
INSERT INTO account_roles (account_id, role_id)
SELECT a.account_id, 'ROLE_PATIENT' FROM accounts a WHERE a.username IN ('benhnhan1', 'benhnhan2', 'benhnhan3')
ON CONFLICT DO NOTHING;


-- ============================================
-- STEP 10: CREATE PATIENTS
-- ============================================

-- Patient 1
INSERT INTO patients (patient_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES (
  gen_random_uuid()::text, 
  'PAT001', 
  'Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', true, NOW(), NOW()
)
ON CONFLICT (patient_code) DO NOTHING;

-- Patient 2
INSERT INTO patients (patient_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES (
  gen_random_uuid()::text, 
  'PAT002', 
  'Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', true, NOW(), NOW()
)
ON CONFLICT (patient_code) DO NOTHING;

-- Patient 3
INSERT INTO patients (patient_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES (
  gen_random_uuid()::text, 
  'PAT003', 
  'Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', true, NOW(), NOW()
)
ON CONFLICT (patient_code) DO NOTHING;


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

-- ====================================
-- ADDITIONAL TEST DATA (LEGACY COMPATIBILITY)
-- Includes data with old VARCHAR-based IDs for backward compatibility testing
-- ====================================

-- Roles (legacy/test file)
INSERT INTO roles (role_id, role_name, description, created_at)
VALUES
    ('ROLE_DOCTOR', 'Bác sĩ', 'Bác sĩ nha khoa', NOW()),
    ('ROLE_RECEPTIONIST', 'Lễ tân', 'Nhân viên lễ tân tiếp đón', NOW()),
    ('ROLE_ACCOUNTANT', 'Kế toán', 'Nhân viên kế toán', NOW()),
    ('ROLE_WAREHOUSE_MANAGER', 'Quản lý kho', 'Quản lý kho vật tư', NOW())
ON CONFLICT (role_id) DO NOTHING;

-- Accounts from test-data-employee
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
    ('ACC_BS01', 'bs.nguyen.chinh.nha', 'nguyen.chinh.nha@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
    ('ACC_BS02', 'bs.tran.noi.nha', 'tran.noi.nha@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
    ('ACC_NV01', 'nv.le.le.tan', 'le.le.tan@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
    ('ACC_NV02', 'nv.pham.ke.toan', 'pham.ke.toan@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW()),
    ('ACC_NV03', 'nv.hoang.kho', 'hoang.kho@dental.com', '$2a$10$abcdefghijklmnopqrstuvwxyz', 'ACTIVE', NOW())
ON CONFLICT (account_id) DO NOTHING;

-- Specializations (legacy/test file) - using numeric ids, INSERT IGNORE to avoid collision
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
    (1, 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
    (2, 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
    (3, 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
    (4, 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
    (5, 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
    (6, 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
    (7, 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW())
ON CONFLICT (specialization_id) DO NOTHING;

-- Employees from test-data-employee (legacy IDs)
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
    ('EMP_ID_001', 'ACC_BS01', 'ROLE_DOCTOR', 'EMP001', 'Văn A', 'Nguyễn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW()),
    ('EMP_ID_002', 'ACC_BS02', 'ROLE_DOCTOR', 'EMP002', 'Thị B', 'Trần', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW()),
    ('EMP_ID_003', 'ACC_NV01', 'ROLE_RECEPTIONIST', 'EMP003', 'Thị C', 'Lê', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW()),
    ('EMP_ID_004', 'ACC_NV02', 'ROLE_ACCOUNTANT', 'EMP004', 'Văn D', 'Phạm', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW()),
    ('EMP_ID_005', 'ACC_NV03', 'ROLE_WAREHOUSE_MANAGER', 'EMP005', 'Văn E', 'Hoàng', '0905678901', '1990-11-18', '555 Pasteur, Q3, TPHCM', TRUE, NOW())
ON CONFLICT (employee_id) DO NOTHING;

-- Employee specializations (legacy/test file)
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
    ('EMP_ID_001', 1),
    ('EMP_ID_001', 7),
    ('EMP_ID_002', 2),
    ('EMP_ID_002', 4)
ON CONFLICT (employee_id, specialization_id) DO NOTHING;

-- SELECT
--   r.role_name AS role_name,
--   COUNT(DISTINCT p.permission_id) AS permission_count,
--   string_agg(p.module || ':' || p.permission_name, ', ') AS permissions
-- FROM roles r
-- LEFT JOIN role_permissions rp ON r.role_id = rp.role_id
-- LEFT JOIN permissions p ON rp.permission_id = p.permission_id
-- GROUP BY r.role_id
-- ORDER BY r.role_name;
