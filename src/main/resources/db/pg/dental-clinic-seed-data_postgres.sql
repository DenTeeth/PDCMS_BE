-- PostgreSQL-compatible seed for Dental Clinic (converted from MySQL seed)
-- Creates missing tables (if needed) and inserts seed data using ON CONFLICT

-- Do not run this file if your schema already contains these tables with
-- different definitions. It is intended for fresh dev DB initialization.

/* Remove MySQL-specific SETs */

-- Create core tables if they don't exist (columns kept minimal to match seed)
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
  specialization_id VARCHAR(64) PRIMARY KEY,
  specialization_code VARCHAR(50),
  specialization_name VARCHAR(200),
  description TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
  account_id VARCHAR(64) PRIMARY KEY,
  username VARCHAR(150) UNIQUE,
  email VARCHAR(200),
  password VARCHAR(255),
  status VARCHAR(50),
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_roles (
  account_id VARCHAR(64) REFERENCES accounts(account_id),
  role_id VARCHAR(64) REFERENCES roles(role_id),
  PRIMARY KEY (account_id, role_id)
);

CREATE TABLE IF NOT EXISTS employees (
  employee_id VARCHAR(64) PRIMARY KEY,
  account_id VARCHAR(64) REFERENCES accounts(account_id),
  role_id VARCHAR(64) REFERENCES roles(role_id),
  employee_code VARCHAR(64),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(50),
  date_of_birth DATE,
  address TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee_specializations (
  employee_id VARCHAR(64) REFERENCES employees(employee_id),
  specialization_id VARCHAR(64) REFERENCES specializations(specialization_id),
  PRIMARY KEY (employee_id, specialization_id)
);

CREATE TABLE IF NOT EXISTS patients (
  patient_id VARCHAR(64) PRIMARY KEY,
  patient_code VARCHAR(64),
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

-- Indexes similar to what app expects
CREATE INDEX IF NOT EXISTS idx_employee_code ON employees (employee_code);
CREATE INDEX IF NOT EXISTS idx_account_username ON accounts (username);

-- ==========================
-- INSERTS (converted)
-- ==========================

-- Roles
INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
VALUES
('ROLE_ADMIN', 'ROLE_ADMIN', 'Quản trị viên hệ thống - Toàn quyền', false, true, NOW()),
('ROLE_DOCTOR', 'ROLE_DOCTOR', 'Bác sĩ nha khoa - Khám và điều trị', true, true, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 'Y tá hỗ trợ điều trị', true, true, NOW()),
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 'Tiếp đón và quản lý lịch hẹn', false, true, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 'Quản lý tài chính và thanh toán', false, true, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 'Quản lý vật tư và thuốc', false, true, NOW()),
('ROLE_PATIENT', 'ROLE_PATIENT', 'Người bệnh - Xem hồ sơ cá nhân', false, true, NOW())
ON CONFLICT (role_id) DO UPDATE
  SET role_name = EXCLUDED.role_name,
      requires_specialization = EXCLUDED.requires_specialization;

-- Permissions
INSERT INTO permissions (permission_id, permission_name, module, description, created_at)
VALUES
('CREATE_ACCOUNT', 'CREATE_ACCOUNT', 'ACCOUNT', 'Tạo tài khoản mới', NOW()),
('VIEW_ACCOUNT', 'VIEW_ACCOUNT', 'ACCOUNT', 'Xem danh sách tài khoản', NOW()),
('UPDATE_ACCOUNT', 'UPDATE_ACCOUNT', 'ACCOUNT', 'Cập nhật tài khoản', NOW()),
('DELETE_ACCOUNT', 'DELETE_ACCOUNT', 'ACCOUNT', 'Xóa tài khoản', NOW()),
('CREATE_EMPLOYEE', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'Tạo nhân viên mới', NOW()),
('VIEW_EMPLOYEE', 'VIEW_EMPLOYEE', 'EMPLOYEE', 'Xem danh sách nhân viên', NOW()),
('UPDATE_EMPLOYEE', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'Cập nhật nhân viên', NOW()),
('DELETE_EMPLOYEE', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'Xóa nhân viên', NOW()),
('CREATE_PATIENT', 'CREATE_PATIENT', 'PATIENT', 'Tạo hồ sơ bệnh nhân', NOW()),
('VIEW_PATIENT', 'VIEW_PATIENT', 'PATIENT', 'Xem hồ sơ bệnh nhân', NOW()),
('UPDATE_PATIENT', 'UPDATE_PATIENT', 'PATIENT', 'Cập nhật hồ sơ bệnh nhân', NOW()),
('DELETE_PATIENT', 'DELETE_PATIENT', 'PATIENT', 'Xóa hồ sơ bệnh nhân', NOW()),
('CREATE_TREATMENT', 'CREATE_TREATMENT', 'TREATMENT', 'Tạo phác đồ điều trị', NOW()),
('VIEW_TREATMENT', 'VIEW_TREATMENT', 'TREATMENT', 'Xem phác đồ điều trị', NOW()),
('UPDATE_TREATMENT', 'UPDATE_TREATMENT', 'TREATMENT', 'Cập nhật phác đồ điều trị', NOW()),
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn', NOW()),
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem lịch hẹn', NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', NOW())
ON CONFLICT (permission_id) DO UPDATE
  SET description = EXCLUDED.description;

-- Role permissions: admin gets everything, others get specific ones
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'),
('ROLE_DOCTOR', 'UPDATE_PATIENT'),
('ROLE_DOCTOR', 'CREATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'),
('ROLE_DOCTOR', 'UPDATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_APPOINTMENT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'),
('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'),
('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'CREATE_APPOINTMENT'),
('ROLE_PATIENT', 'VIEW_APPOINTMENT')
ON CONFLICT DO NOTHING;

-- Specializations
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
('770e8400-e29b-41d4-a716-446655440001', 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', true, NOW()),
('770e8400-e29b-41d4-a716-446655440002', 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', true, NOW()),
('770e8400-e29b-41d4-a716-446655440003', 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', true, NOW()),
('770e8400-e29b-41d4-a716-446655440004', 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', true, NOW()),
('770e8400-e29b-41d4-a716-446655440005', 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', true, NOW()),
('770e8400-e29b-41d4-a716-446655440006', 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', true, NOW()),
('770e8400-e29b-41d4-a716-446655440007', 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', true, NOW())
ON CONFLICT (specialization_id) DO UPDATE
  SET specialization_name = EXCLUDED.specialization_name;

-- Accounts (employees + patients)
INSERT INTO accounts (account_id, username, email, password, status, created_at)
VALUES
('880e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440002', 'nhasi1', 'nhasi1@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440003', 'nhasi2', 'nhasi2@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440006', 'yta', 'yta@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440004', 'letan', 'letan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440005', 'ketoan', 'ketoan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440101', 'benhnhan1', 'benhnhan1@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440102', 'benhnhan2', 'benhnhan2@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
('880e8400-e29b-41d4-a716-446655440103', 'benhnhan3', 'benhnhan3@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
ON CONFLICT (account_id) DO UPDATE
  SET username = EXCLUDED.username;

-- Account roles
INSERT INTO account_roles (account_id, role_id)
VALUES
('880e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN'),
('880e8400-e29b-41d4-a716-446655440002', 'ROLE_DOCTOR'),
('880e8400-e29b-41d4-a716-446655440003', 'ROLE_DOCTOR'),
('880e8400-e29b-41d4-a716-446655440006', 'ROLE_NURSE'),
('880e8400-e29b-41d4-a716-446655440004', 'ROLE_RECEPTIONIST'),
('880e8400-e29b-41d4-a716-446655440005', 'ROLE_ACCOUNTANT'),
('880e8400-e29b-41d4-a716-446655440101', 'ROLE_PATIENT'),
('880e8400-e29b-41d4-a716-446655440102', 'ROLE_PATIENT'),
('880e8400-e29b-41d4-a716-446655440103', 'ROLE_PATIENT')
ON CONFLICT DO NOTHING;

-- Employees
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
('990e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN', 'EMP001', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', true, NOW()),
('990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440002', 'ROLE_DOCTOR', 'EMP002', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', true, NOW()),
('990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440003', 'ROLE_DOCTOR', 'EMP003', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', true, NOW()),
('990e8400-e29b-41d4-a716-446655440006', '880e8400-e29b-41d4-a716-446655440006', 'ROLE_NURSE', 'EMP006', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', true, NOW()),
('990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440004', 'ROLE_RECEPTIONIST', 'EMP004', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', true, NOW()),
('990e8400-e29b-41d4-a716-446655440005', '880e8400-e29b-41d4-a716-446655440005', 'ROLE_ACCOUNTANT', 'EMP005', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', true, NOW())
ON CONFLICT (employee_id) DO UPDATE
  SET employee_code = EXCLUDED.employee_code;

-- Employee specializations
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001'),
('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440007'),
('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002'),
('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440004')
ON CONFLICT DO NOTHING;

-- Patients
INSERT INTO patients (patient_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES
('aa0e8400-e29b-41d4-a716-446655440101', 'PT001', 'Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', true, NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440102', 'PT002', 'Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', true, NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440103', 'PT003', 'Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', true, NOW(), NOW())
ON CONFLICT (patient_id) DO UPDATE
  SET patient_code = EXCLUDED.patient_code;

-- Verification queries (use string_agg instead of GROUP_CONCAT)
-- Employees with roles and accounts
-- Note: run these manually if you want interactive verification
-- SELECT
--   e.employee_code,
--   e.first_name,
--   e.last_name,
--   r.role_name AS role_name,
--   a.username,
--   a.email,
--   string_agg(s.specialization_name, ', ') AS specializations
-- FROM employees e
-- LEFT JOIN roles r ON e.role_id = r.role_id
-- LEFT JOIN accounts a ON e.account_id = a.account_id
-- LEFT JOIN employee_specializations es ON e.employee_id = es.employee_id
-- LEFT JOIN specializations s ON es.specialization_id = s.specialization_id
-- GROUP BY e.employee_id
-- ORDER BY e.employee_code;

-- Permissions by role (example)
-- SELECT
--   r.role_name AS role_name,
--   COUNT(DISTINCT p.permission_id) AS permission_count,
--   string_agg(p.module || ':' || p.permission_name, ', ') AS permissions
-- FROM roles r
-- LEFT JOIN role_permissions rp ON r.role_id = rp.role_id
-- LEFT JOIN permissions p ON rp.permission_id = p.permission_id
-- GROUP BY r.role_id
-- ORDER BY r.role_name;
