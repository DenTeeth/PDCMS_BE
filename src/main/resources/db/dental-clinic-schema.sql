-- =================================================================
-- I. CORE AUTH & USER TABLES
-- Nền tảng cho tài khoản, vai trò và quyền hạn
-- =================================================================

CREATE TABLE IF NOT EXISTS accounts (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  account_code VARCHAR(20) UNIQUE,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  status VARCHAR(50), -- e.g., 'ACTIVE', 'LOCKED', 'PENDING_VERIFICATION'
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_accounts_username (username)
);

CREATE TABLE IF NOT EXISTS roles (
  role_id VARCHAR(50) PRIMARY KEY, -- e.g., 'ADMIN', 'MANAGER', 'DENTIST', 'NURSE', 'RECEPTIONIST'
  role_name VARCHAR(50) NOT NULL,
  description TEXT,
  requires_specialization BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
  permission_id VARCHAR(50) PRIMARY KEY, -- e.g., 'VIEW_SHIFTS_ALL', 'CREATE_APPOINTMENT'
  permission_name VARCHAR(100) NOT NULL,
  `module` VARCHAR(50) NOT NULL, -- e.g., 'SCHEDULING', 'PATIENTS', 'BILLING'
  description TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_roles (
  account_id INT NOT NULL,
  role_id VARCHAR(50) NOT NULL,
  PRIMARY KEY (account_id, role_id),
  CONSTRAINT fk_ar_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
  CONSTRAINT fk_ar_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id VARCHAR(50) NOT NULL,
  permission_id VARCHAR(50) NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id VARCHAR(255) PRIMARY KEY,
  account_id INT NOT NULL,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  CONSTRAINT fk_rt_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- =================================================================
-- II. EMPLOYEE & PATIENT MANAGEMENT
-- Quản lý thông tin nhân viên, chuyên môn và bệnh nhân
-- =================================================================

CREATE TABLE IF NOT EXISTS specializations (
  specialization_id INT AUTO_INCREMENT PRIMARY KEY,
  specialization_code VARCHAR(10) NOT NULL UNIQUE,
  specialization_name VARCHAR(100) NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
  employee_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT UNIQUE NOT NULL,
  role_id VARCHAR(50) NOT NULL,
  employee_code VARCHAR(20) UNIQUE,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  phone VARCHAR(15),
  date_of_birth DATE,
  address TEXT,
  employment_type VARCHAR(50), -- e.g., 'FULL_TIME', 'PART_TIME'
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_employee_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
  CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT,
  INDEX idx_employees_code (employee_code)
);

CREATE TABLE IF NOT EXISTS employee_specializations (
  employee_id INT NOT NULL,
  specialization_id INT NOT NULL,
  PRIMARY KEY (employee_id, specialization_id),
  CONSTRAINT fk_es_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_es_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(specialization_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients (
  patient_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT UNIQUE,
  patient_code VARCHAR(20) UNIQUE,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(15),
  date_of_birth DATE,
  address TEXT,
  gender VARCHAR(10),
  medical_history TEXT,
  allergies TEXT,
  emergency_contact_name VARCHAR(100),
  emergency_contact_phone VARCHAR(15),
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_patient_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL,
  INDEX idx_patient_phone (phone)
);

-- =================================================================
-- III. CRM - CUSTOMER CONTACT MANAGEMENT
-- Quản lý các liên hệ tiềm năng trước khi trở thành bệnh nhân
-- =================================================================

CREATE TABLE IF NOT EXISTS customer_contacts (
  contact_id VARCHAR(20) PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(15) NOT NULL,
  email VARCHAR(100),
  source VARCHAR(20), -- e.g., 'WEBSITE', 'FACEBOOK', 'WALK_IN'
  status VARCHAR(20), -- e.g., 'NEW', 'CONTACTED', 'CONVERTED', 'FAILED'
  service_interested VARCHAR(100),
  message TEXT,
  assigned_to INT,
  notes TEXT,
  converted_patient_id INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_cc_employee FOREIGN KEY (assigned_to) REFERENCES employees(employee_id) ON DELETE SET NULL,
  CONSTRAINT fk_cc_patient FOREIGN KEY (converted_patient_id) REFERENCES patients(patient_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS contact_history (
  history_id VARCHAR(20) PRIMARY KEY,
  contact_id VARCHAR(20) NOT NULL,
  employee_id INT,
  `action` VARCHAR(20) NOT NULL, -- e.g., 'CALL', 'EMAIL', 'NOTE'
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ch_contact FOREIGN KEY (contact_id) REFERENCES customer_contacts(contact_id) ON DELETE CASCADE,
  CONSTRAINT fk_ch_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- =================================================================
-- IV. SHIFT & SCHEDULE MANAGEMENT (PHIÊN BẢN ĐÃ CẬP NHẬT)
-- Quản lý ca làm việc, lịch làm, OT, nghỉ phép
-- =================================================================

CREATE TABLE work_slots (
  slot_id VARCHAR(20) PRIMARY KEY,
  slot_code VARCHAR(20) UNIQUE NOT NULL,
  slot_name VARCHAR(100) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  category ENUM('NORMAL', 'NIGHT') NOT NULL DEFAULT 'NORMAL',
  is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE employee_shifts (
  shift_id VARCHAR(20) PRIMARY KEY,
  employee_id INT NOT NULL,
  work_date DATE NOT NULL,
  slot_id VARCHAR(20) NOT NULL,
  is_overtime BOOLEAN DEFAULT FALSE,
  status ENUM('SCHEDULED', 'ON_LEAVE', 'COMPLETED', 'ABSENT', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
  source ENUM('BATCH_JOB', 'REGISTRATION_JOB', 'OT_APPROVAL', 'MANUAL_ENTRY') NOT NULL,
  source_ot_request_id VARCHAR(20),
  source_off_request_id VARCHAR(20),
  check_in_time TIME NULL,
  check_out_time TIME NULL,
  created_by INT,
  notes TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY unique_shift (employee_id, work_date, slot_id),
  CONSTRAINT fk_emps_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_emps_slot FOREIGN KEY (slot_id) REFERENCES work_slots(slot_id) ON DELETE RESTRICT
);

CREATE TABLE employee_shift_registrations (
  registration_id VARCHAR(20) PRIMARY KEY,
  employee_id INT NOT NULL,
  slot_id VARCHAR(20) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE,
  is_active BOOLEAN DEFAULT TRUE,
  CONSTRAINT fk_empsr_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_empsr_slot FOREIGN KEY (slot_id) REFERENCES work_slots(slot_id) ON DELETE CASCADE
);

CREATE TABLE registration_days (
  registration_id VARCHAR(20) NOT NULL,
  day_of_week VARCHAR(10) NOT NULL, -- 'MONDAY', 'TUESDAY', ...
  PRIMARY KEY (registration_id, day_of_week),
  CONSTRAINT fk_rd_registration FOREIGN KEY (registration_id) REFERENCES employee_shift_registrations(registration_id) ON DELETE CASCADE
);

CREATE TABLE overtime_requests (
  request_id VARCHAR(20) PRIMARY KEY,
  request_code VARCHAR(20) UNIQUE NOT NULL,
  employee_id INT NOT NULL,
  requested_by INT NOT NULL,
  work_date DATE NOT NULL,
  slot_id VARCHAR(20) NOT NULL,
  reason TEXT NOT NULL,
  status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
  approved_by INT,
  approved_at DATETIME,
  rejected_reason TEXT,
  cancellation_reason TEXT,
  CONSTRAINT fk_otr_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_otr_requester FOREIGN KEY (requested_by) REFERENCES employees(employee_id) ON DELETE RESTRICT,
  CONSTRAINT fk_otr_approver FOREIGN KEY (approved_by) REFERENCES employees(employee_id) ON DELETE RESTRICT,
  CONSTRAINT fk_otr_slot FOREIGN KEY (slot_id) REFERENCES work_slots(slot_id) ON DELETE RESTRICT
);

CREATE TABLE time_off_types (
  type_id VARCHAR(20) PRIMARY KEY,
  type_code VARCHAR(20) UNIQUE NOT NULL,
  type_name VARCHAR(100) NOT NULL,
  is_paid BOOLEAN NOT NULL DEFAULT TRUE,
  is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE time_off_requests (
  request_id VARCHAR(20) PRIMARY KEY,
  request_code VARCHAR(20) UNIQUE NOT NULL,
  employee_id INT NOT NULL,
  requested_by INT NOT NULL,
  time_off_type_id VARCHAR(20) NOT NULL,
  slot_id VARCHAR(20),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  reason TEXT,
  status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
  approved_by INT,
  approved_at DATETIME,
  rejected_reason TEXT,
  cancellation_reason TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tor_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_tor_requester FOREIGN KEY (requested_by) REFERENCES employees(employee_id) ON DELETE RESTRICT,
  CONSTRAINT fk_tor_approver FOREIGN KEY (approved_by) REFERENCES employees(employee_id) ON DELETE RESTRICT,
  CONSTRAINT fk_tor_type FOREIGN KEY (time_off_type_id) REFERENCES time_off_types(type_id) ON DELETE RESTRICT,
  CONSTRAINT fk_tor_slot FOREIGN KEY (slot_id) REFERENCES work_slots(slot_id) ON DELETE RESTRICT
);

CREATE TABLE holiday_definitions (
  definition_id VARCHAR(20) PRIMARY KEY,
  holiday_name VARCHAR(100) UNIQUE NOT NULL,
  holiday_type ENUM('NATIONAL', 'COMPANY') NOT NULL
);

CREATE TABLE holiday_dates (
  holiday_date DATE PRIMARY KEY,
  definition_id VARCHAR(20) NOT NULL,
  CONSTRAINT fk_hd_definition FOREIGN KEY (definition_id) REFERENCES holiday_definitions(definition_id) ON DELETE CASCADE
);

CREATE TABLE shift_renewal_requests (
  renewal_id VARCHAR(20) PRIMARY KEY,
  employee_id INT NOT NULL,
  expiring_registration_id VARCHAR(20) NOT NULL,
  status ENUM('PENDING_ACTION', 'CONFIRMED', 'DECLINED', 'EXPIRED') NOT NULL DEFAULT 'PENDING_ACTION',
  expires_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  confirmed_at DATETIME,
  CONSTRAINT fk_srr_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
  CONSTRAINT fk_srr_registration FOREIGN KEY (expiring_registration_id) REFERENCES employee_shift_registrations(registration_id) ON DELETE CASCADE
);
