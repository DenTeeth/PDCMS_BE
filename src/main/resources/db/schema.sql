-- ============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM - SCHEMA V2 CLEAN
-- Date: 2025-11-04
-- PostgreSQL Database Schema
-- ============================================
-- NOTE: Hibernate auto-creates tables from @Entity classes
-- This file is for reference and manual database setup only
-- ============================================

-- ============================================
-- ENUM TYPES (Must be created before tables)
-- ============================================

CREATE TYPE gender AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE employment_type AS ENUM ('FULL_TIME', 'PART_TIME_FIXED', 'PART_TIME_FLEX');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED', 'PENDING_VERIFICATION');
CREATE TYPE appointment_status_enum AS ENUM ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE appointment_participant_role_enum AS ENUM ('ASSISTANT', 'SECONDARY_DOCTOR', 'OBSERVER');
CREATE TYPE shift_status AS ENUM ('SCHEDULED', 'ON_LEAVE', 'COMPLETED', 'ABSENT', 'CANCELLED');
CREATE TYPE request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');
CREATE TYPE work_shift_category AS ENUM ('NORMAL', 'NIGHT');
CREATE TYPE shift_source AS ENUM ('BATCH_JOB', 'REGISTRATION_JOB', 'OT_APPROVAL', 'MANUAL_ENTRY');
CREATE TYPE day_of_week AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');
CREATE TYPE holiday_type AS ENUM ('NATIONAL', 'COMPANY');
CREATE TYPE time_off_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');

-- Warehouse Management ENUMs
CREATE TYPE warehouse_type AS ENUM ('COLD', 'NORMAL');
CREATE TYPE transaction_type AS ENUM ('IMPORT', 'EXPORT', 'ADJUST', 'DESTROY');
CREATE TYPE unit_of_measure AS ENUM ('UNIT', 'BOX', 'BOTTLE', 'VIAL', 'TUBE', 'STRIP', 'PACK');

-- ============================================
-- CORE TABLES
-- ============================================

-- Base Roles (3 loại: admin, employee, patient)
CREATE TABLE base_roles (
    base_role_id INTEGER PRIMARY KEY,
    base_role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles (Vai trò cụ thể trong hệ thống)
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    base_role_id INTEGER NOT NULL REFERENCES base_roles(base_role_id),
    description TEXT,
    requires_specialization BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts (Tài khoản đăng nhập)
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(role_id),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions (Quyền hạn)
CREATE TABLE permissions (
    permission_id VARCHAR(100) PRIMARY KEY,
    permission_name VARCHAR(255) NOT NULL,
    description TEXT,
    resource VARCHAR(100),
    action VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions (Quyền của từng vai trò)
CREATE TABLE role_permissions (
    role_id VARCHAR(50) NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id VARCHAR(100) NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Specializations (Chuyên môn nha khoa)
CREATE TABLE specializations (
    specialization_id INTEGER PRIMARY KEY,
    specialization_code VARCHAR(50) UNIQUE NOT NULL,
    specialization_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employees (Nhân viên)
CREATE TABLE employees (
    employee_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    employee_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    employment_type VARCHAR(50) DEFAULT 'FULL_TIME',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employee Specializations (Chuyên môn của nhân viên)
CREATE TABLE employee_specializations (
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    specialization_id INTEGER NOT NULL REFERENCES specializations(specialization_id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, specialization_id)
);

-- Patients (Bệnh nhân)
CREATE TABLE patients (
    patient_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES accounts(account_id) ON DELETE SET NULL,
    patient_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    gender VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Services (Dịch vụ nha khoa)
CREATE TABLE services (
    service_id BIGSERIAL PRIMARY KEY,
    service_code VARCHAR(50) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    default_duration_minutes INTEGER NOT NULL,
    default_buffer_minutes INTEGER DEFAULT 0,
    price DECIMAL(15,2),
    specialization_id INTEGER REFERENCES specializations(specialization_id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms (Phòng khám)
CREATE TABLE rooms (
    room_id VARCHAR(50) PRIMARY KEY,
    room_code VARCHAR(50) UNIQUE NOT NULL,
    room_name VARCHAR(255) NOT NULL,
    room_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Room Services (Dịch vụ tương thích với phòng)
CREATE TABLE room_services (
    room_id VARCHAR(50) NOT NULL REFERENCES rooms(room_id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(service_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, service_id)
);

-- Work Shifts (Ca làm việc)
CREATE TABLE work_shifts (
    work_shift_id VARCHAR(50) PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    category VARCHAR(50) DEFAULT 'NORMAL',
    is_active BOOLEAN DEFAULT TRUE
);

-- Employee Shifts (Lịch làm việc của nhân viên)
CREATE TABLE employee_shifts (
    employee_shift_id VARCHAR(50) PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    work_shift_id VARCHAR(50) NOT NULL REFERENCES work_shifts(work_shift_id),
    source VARCHAR(50) DEFAULT 'MANUAL_ENTRY',
    is_overtime BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, work_date, work_shift_id)
);

-- Appointments (Lịch hẹn)
CREATE TABLE appointments (
    appointment_id BIGSERIAL PRIMARY KEY,
    appointment_code VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL REFERENCES patients(patient_id),
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id),
    room_id VARCHAR(50) NOT NULL REFERENCES rooms(room_id),
    appointment_start_time TIMESTAMP NOT NULL,
    appointment_end_time TIMESTAMP NOT NULL,
    expected_duration_minutes INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Appointment Services (Dịch vụ trong lịch hẹn)
CREATE TABLE appointment_services (
    appointment_service_id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(service_id),
    service_duration_minutes INTEGER,
    service_buffer_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Appointment Participants (Phụ tá/Bác sĩ phụ trong lịch hẹn)
CREATE TABLE appointment_participants (
    participant_id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id),
    participant_role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (appointment_id, employee_id)
);

-- Appointment Audit Logs (Lịch sử thay đổi lịch hẹn)
CREATE TABLE appointment_audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    changed_by_employee_id BIGINT REFERENCES employees(employee_id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL,
    reason_code VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    old_start_time TIMESTAMP,
    new_start_time TIMESTAMP,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    notes TEXT,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_appointment ON appointment_audit_logs(appointment_id);
CREATE INDEX idx_audit_action_type ON appointment_audit_logs(action_type);
CREATE INDEX idx_audit_timestamp ON appointment_audit_logs(action_timestamp);

-- ============================================
-- TIME OFF & LEAVE MANAGEMENT
-- ============================================

-- Time Off Types (Loại nghỉ phép)
CREATE TABLE time_off_types (
    type_id VARCHAR(50) PRIMARY KEY,
    type_code VARCHAR(50) UNIQUE NOT NULL,
    type_name VARCHAR(255) NOT NULL,
    is_paid BOOLEAN DEFAULT TRUE,
    requires_approval BOOLEAN DEFAULT TRUE,
    requires_balance BOOLEAN DEFAULT TRUE,
    default_days_per_year DECIMAL(5,2),
    is_active BOOLEAN DEFAULT TRUE
);

-- Employee Leave Balances (Số ngày phép còn lại)
CREATE TABLE employee_leave_balances (
    balance_id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    time_off_type_id VARCHAR(50) NOT NULL REFERENCES time_off_types(type_id),
    year INTEGER NOT NULL,
    total_days DECIMAL(5,2) NOT NULL DEFAULT 0,
    used_days DECIMAL(5,2) NOT NULL DEFAULT 0,
    remaining_days DECIMAL(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, time_off_type_id, year)
);

-- Time Off Requests (Đơn xin nghỉ phép)
CREATE TABLE time_off_requests (
    request_id VARCHAR(50) PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(employee_id),
    time_off_type_id VARCHAR(50) NOT NULL REFERENCES time_off_types(type_id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(5,2) NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    approved_by BIGINT REFERENCES employees(employee_id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- HOLIDAY MANAGEMENT
-- ============================================

-- Holiday Definitions (Định nghĩa ngày lễ)
CREATE TABLE holiday_definitions (
    holiday_id BIGSERIAL PRIMARY KEY,
    holiday_code VARCHAR(50) UNIQUE NOT NULL,
    holiday_name VARCHAR(255) NOT NULL,
    holiday_type VARCHAR(50) NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    day INTEGER NOT NULL CHECK (day BETWEEN 1 AND 31),
    is_recurring BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Holiday Dates (Ngày lễ cụ thể theo năm)
CREATE TABLE holiday_dates (
    holiday_date_id BIGSERIAL PRIMARY KEY,
    holiday_id BIGINT NOT NULL REFERENCES holiday_definitions(holiday_id) ON DELETE CASCADE,
    holiday_date DATE NOT NULL UNIQUE,
    year INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- WAREHOUSE MANAGEMENT (Quản lý kho vật tư y tế)
-- ============================================

-- Categories (Danh mục vật tư - cấu trúc cây)
CREATE TABLE categories (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_code VARCHAR(50) UNIQUE NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    parent_category_id UUID REFERENCES categories(category_id) ON DELETE SET NULL,
    warehouse_type VARCHAR(20) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_warehouse_type CHECK (warehouse_type IN ('COLD', 'NORMAL'))
);

-- Suppliers (Nhà cung cấp)
CREATE TABLE suppliers (
    supplier_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_code VARCHAR(50) UNIQUE NOT NULL,
    supplier_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Item Masters (Vật tư chính - master data)
CREATE TABLE item_masters (
    item_master_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_code VARCHAR(50) UNIQUE NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(category_id),
    unit_of_measure VARCHAR(20) NOT NULL,
    description TEXT,
    min_stock_level INTEGER DEFAULT 0,
    max_stock_level INTEGER DEFAULT 1000,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_unit_of_measure CHECK (unit_of_measure IN ('UNIT', 'BOX', 'BOTTLE', 'VIAL', 'TUBE', 'STRIP', 'PACK')),
    CONSTRAINT chk_stock_levels CHECK (min_stock_level >= 0 AND max_stock_level >= min_stock_level)
);

-- Item-Supplier Mapping (N-N relationship)
CREATE TABLE item_suppliers (
    item_master_id UUID NOT NULL REFERENCES item_masters(item_master_id) ON DELETE CASCADE,
    supplier_id UUID NOT NULL REFERENCES suppliers(supplier_id) ON DELETE CASCADE,
    PRIMARY KEY (item_master_id, supplier_id)
);

-- Item Batches (Lô hàng - FEFO inventory tracking)
CREATE TABLE item_batches (
    batch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_master_id UUID NOT NULL REFERENCES item_masters(item_master_id),
    lot_number VARCHAR(100) NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity CHECK (quantity_in_stock >= 0),
    CONSTRAINT uq_item_lot UNIQUE (item_master_id, lot_number)
);

-- Storage Transactions (Giao dịch kho - audit trail)
CREATE TABLE storage_transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL REFERENCES item_batches(batch_id),
    transaction_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by UUID NOT NULL,
    reference_code VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('IMPORT', 'EXPORT', 'ADJUST', 'DESTROY')),
    CONSTRAINT chk_transaction_quantity CHECK (quantity <> 0)
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

CREATE INDEX idx_employees_account ON employees(account_id);
CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_patients_code ON patients(patient_code);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_employee ON appointments(employee_id);
CREATE INDEX idx_appointments_room ON appointments(room_id);
CREATE INDEX idx_appointments_start_time ON appointments(appointment_start_time);
CREATE INDEX idx_appointment_services_appointment ON appointment_services(appointment_id);
CREATE INDEX idx_appointment_participants_appointment ON appointment_participants(appointment_id);
CREATE INDEX idx_employee_shifts_employee ON employee_shifts(employee_id);
CREATE INDEX idx_employee_shifts_date ON employee_shifts(work_date);
CREATE INDEX idx_time_off_requests_employee ON time_off_requests(employee_id);
CREATE INDEX idx_time_off_requests_status ON time_off_requests(status);

-- Warehouse indexes
CREATE INDEX idx_categories_parent ON categories(parent_category_id);
CREATE INDEX idx_categories_warehouse_type ON categories(warehouse_type);
CREATE INDEX idx_item_masters_category ON item_masters(category_id);
CREATE INDEX idx_item_batches_item_master ON item_batches(item_master_id);
CREATE INDEX idx_item_batches_expiry ON item_batches(expiry_date);
CREATE INDEX idx_storage_transactions_batch ON storage_transactions(batch_id);
CREATE INDEX idx_storage_transactions_type ON storage_transactions(transaction_type);
CREATE INDEX idx_storage_transactions_date ON storage_transactions(transaction_date);

-- ============================================
-- END OF SCHEMA
-- ============================================
