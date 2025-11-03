-- ============================================-- ============================================

-- DENTAL CLINIC MANAGEMENT SYSTEM - DATABASE SCHEMA - V14 (HYBRID)-- DENTAL CLINIC MANAGEMENT SYSTEM - DATABASE SCHEMA - V14 (HYBRID)

-- Date: 2025-10-30-- Date: 2025-10-30

-- PostgreSQL Database Schema Definition-- PostgreSQL Database Schema Definition

-- ============================================-- ============================================

-- NOTE: -- NOTE:

-- - TÁCH BIỆT 2 luồng lịch làm việc:-- - TÁCH BIỆT 2 luồng lịch làm việc:

--   1. Lịch CỐ ĐỊNH (Full-time): Dùng `fixed_shift_registrations` + `fixed_registration_days`.--   1. Lịch CỐ ĐỊNH (Full-time): Dùng `fixed_shift_registrations` + `fixed_registration_days`.

--   2. Lịch LINH HOẠT (Part-time): Dùng `part_time_slots` + `part_time_registrations`.--   2. Lịch LINH HOẠT (Part-time): Dùng `part_time_slots` + `part_time_registrations`.

-- ============================================-- ============================================



-- ============================================-- =============================================

-- ENUMS - Các giá trị enum trong hệ thống-- Table: holiday_dates

-- ============================================-- Purpose: Store public holidays and special non-working days

-- =============================================

-- Base Role TypeCREATE TABLE holiday_dates (

CREATE TYPE base_role_type AS ENUM ('admin', 'employee', 'patient');    holiday_id BIGSERIAL PRIMARY KEY,

    holiday_date DATE NOT NULL UNIQUE,

-- Account Status    holiday_name VARCHAR(255) NOT NULL,

CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING_VERIFICATION');    year INTEGER NOT NULL,

    description TEXT,

-- Employment Type (CẬP NHẬT V14)    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

CREATE TYPE employment_type AS ENUM (    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    'FULL_TIME',        -- Dùng Luồng Cố định (fixed_shift_registrations));

    'PART_TIME_FIXED',  -- Dùng Luồng Cố định (fixed_shift_registrations)

    'PART_TIME_FLEX'    -- Dùng Luồng Linh hoạt (part_time_registrations)-- Indexes for holiday_dates

);CREATE INDEX idx_holiday_dates_date ON holiday_dates(holiday_date);

CREATE INDEX idx_holiday_dates_year ON holiday_dates(year);

-- Gender Type

CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE', 'OTHER');COMMENT ON TABLE holiday_dates IS 'Stores public holidays and special non-working days for scheduling';

COMMENT ON COLUMN holiday_dates.holiday_date IS 'The actual date of the holiday (must be unique)';

-- Contact Management EnumsCOMMENT ON COLUMN holiday_dates.year IS 'Year of the holiday for quick filtering';

CREATE TYPE contact_source AS ENUM ('FACEBOOK', 'WEBSITE', 'REFERRAL', 'WALK_IN', 'ADVERTISEMENT', 'OTHER');

CREATE TYPE contact_status AS ENUM ('NEW', 'CONTACTED', 'CONVERTED', 'LOST');-- =============================================

CREATE TYPE contact_type AS ENUM ('PHONE', 'EMAIL', 'SMS', 'IN_PERSON', 'ZALO', 'FACEBOOK_MESSENGER');-- Table: shift_renewal_requests

CREATE TYPE contact_result AS ENUM ('SUCCESS', 'NO_ANSWER', 'BUSY', 'SCHEDULED', 'NOT_INTERESTED');-- Purpose: Manage shift registration renewal requests for part-time employees

-- =============================================

-- Work Schedule EnumsCREATE TABLE shift_renewal_requests (

CREATE TYPE work_slots_category_enum AS ENUM ('NORMAL', 'NIGHT');    renewal_id VARCHAR(12) PRIMARY KEY,

CREATE TYPE employee_shifts_status_enum AS ENUM ('SCHEDULED', 'ON_LEAVE', 'COMPLETED', 'ABSENT', 'CANCELLED');    expiring_registration_id VARCHAR(12) NOT NULL,

CREATE TYPE employee_shifts_source_enum AS ENUM ('BATCH_JOB', 'REGISTRATION_JOB', 'OT_APPROVAL', 'MANUAL_ENTRY');    employee_id INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTION',

-- Request Status Enums    expires_at TIMESTAMP NOT NULL,

CREATE TYPE overtime_requests_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');    confirmed_at TIMESTAMP,

CREATE TYPE time_off_requests_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');    message TEXT,

CREATE TYPE shift_renewal_requests_status_enum AS ENUM ('PENDING_ACTION', 'CONFIRMED', 'FINALIZED', 'DECLINED', 'EXPIRED');    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- Holiday Enums

CREATE TYPE holiday_definitions_holiday_type_enum AS ENUM ('NATIONAL', 'COMPANY');    -- Foreign Keys

    CONSTRAINT fk_renewal_registration FOREIGN KEY (expiring_registration_id)

-- Appointment Enums        REFERENCES working_schedule(registration_id) ON DELETE CASCADE,

CREATE TYPE appointment_status_enum AS ENUM ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');    CONSTRAINT fk_renewal_employee FOREIGN KEY (employee_id)

CREATE TYPE appointment_action_type AS ENUM ('CREATE', 'DELAY', 'RESCHEDULE_SOURCE', 'RESCHEDULE_TARGET', 'CANCEL', 'STATUS_CHANGE');        REFERENCES employees(employee_id) ON DELETE CASCADE,

CREATE TYPE appointment_reason_code AS ENUM ('PREVIOUS_CASE_OVERRUN', 'DOCTOR_UNAVAILABLE', 'EQUIPMENT_FAILURE', 'PATIENT_REQUEST', 'OPERATIONAL_REDIRECT', 'OTHER');

CREATE TYPE appointment_participant_role_enum AS ENUM ('ASSISTANT', 'SECONDARY_DOCTOR', 'OBSERVER');

    -- Check Constraints

-- Treatment Plan Enums    CONSTRAINT chk_renewal_status CHECK (status IN ('PENDING_ACTION', 'CONFIRMED', 'FINALIZED', 'DECLINED', 'EXPIRED')),

CREATE TYPE treatment_plan_status AS ENUM ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED');    CONSTRAINT chk_renewal_id_format CHECK (renewal_id ~ '^SRR[0-9]{9}$')

CREATE TYPE treatment_plan_payment_type AS ENUM ('FULL', 'PHASED', 'INSTALLMENT'););

CREATE TYPE plan_phase_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED');

CREATE TYPE plan_item_status AS ENUM ('PENDING_APPROVAL', 'READY_FOR_BOOKING', 'SCHEDULED', 'COMPLETED', 'CANCELLED');-- Indexes for shift_renewal_requests

CREATE INDEX idx_shift_renewal_employee ON shift_renewal_requests(employee_id);

-- Patient Record TypeCREATE INDEX idx_shift_renewal_status ON shift_renewal_requests(status);

CREATE TYPE patient_record_type AS ENUM ('XRAY', 'CT_SCAN', 'TEST_RESULT', 'CONTRACT', 'IMAGE_BEFORE', 'IMAGE_AFTER', 'OTHER');CREATE INDEX idx_shift_renewal_expires_at ON shift_renewal_requests(expires_at);

CREATE INDEX idx_shift_renewal_registration ON shift_renewal_requests(expiring_registration_id);

-- ============================================CREATE INDEX idx_shift_renewal_employee_status ON shift_renewal_requests(employee_id, status);

-- CORE TABLES - Quản lý tài khoản và phân quyền

-- ============================================COMMENT ON TABLE shift_renewal_requests IS 'Tracks renewal requests for expiring shift registrations';

COMMENT ON COLUMN shift_renewal_requests.renewal_id IS 'Format: SRRYYMMDDSSS (SRR + date + sequence)';

-- Table: base_rolesCOMMENT ON COLUMN shift_renewal_requests.status IS 'PENDING_ACTION: awaiting response, CONFIRMED: accepted, DECLINED: rejected, EXPIRED: no response';

CREATE TABLE base_roles (COMMENT ON COLUMN shift_renewal_requests.expires_at IS 'Deadline for employee response (typically 7 days from creation)';

    base_role_id SERIAL PRIMARY KEY,

    base_role_name VARCHAR(50) UNIQUE NOT NULL,-- =============================================

    default_home_path VARCHAR(255) NOT NULL,-- Table: employee_shifts

    description TEXT,-- Purpose: Store actual scheduled shifts for employees (final schedule)

    is_active BOOLEAN DEFAULT TRUE,-- =============================================

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,CREATE TABLE employee_shifts (

    updated_at TIMESTAMP    shift_id BIGSERIAL PRIMARY KEY,

);    employee_id INTEGER NOT NULL,

    work_date DATE NOT NULL,

COMMENT ON TABLE base_roles IS 'Base Roles - 3 loại cố định xác định layout FE';    work_shift_id VARCHAR(50) NOT NULL,

COMMENT ON COLUMN base_roles.base_role_id IS 'ID base role (1=admin, 2=employee, 3=patient)';    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

COMMENT ON COLUMN base_roles.base_role_name IS 'Tên: admin, employee, patient';    registration_id VARCHAR(12),

    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

-- Table: roles    notes TEXT,

CREATE TABLE roles (    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    role_id VARCHAR(50) PRIMARY KEY,    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    role_name VARCHAR(50) UNIQUE NOT NULL,

    base_role_id INTEGER NOT NULL,    -- Foreign Keys

    home_path_override VARCHAR(255),    CONSTRAINT fk_employee_shift_employee FOREIGN KEY (employee_id)

    description TEXT,        REFERENCES employees(employee_id) ON DELETE CASCADE,

    requires_specialization BOOLEAN DEFAULT FALSE,    CONSTRAINT fk_employee_shift_work_shift FOREIGN KEY (work_shift_id)

    is_active BOOLEAN DEFAULT TRUE,        REFERENCES work_shifts(shift_id) ON DELETE RESTRICT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    CONSTRAINT fk_employee_shift_registration FOREIGN KEY (registration_id)

    updated_at TIMESTAMP,        REFERENCES working_schedule(registration_id) ON DELETE SET NULL,



    CONSTRAINT fk_roles_base_role FOREIGN KEY (base_role_id)     -- Check Constraints

        REFERENCES base_roles(base_role_id) ON UPDATE CASCADE ON DELETE RESTRICT    -- Updated to match ShiftSource enum: BATCH_JOB, REGISTRATION_JOB, OT_APPROVAL, MANUAL_ENTRY

);    CONSTRAINT chk_employee_shift_source CHECK (source IN ('BATCH_JOB', 'REGISTRATION_JOB', 'OT_APPROVAL', 'MANUAL_ENTRY')),

    CONSTRAINT chk_employee_shift_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'ABSENT')),

COMMENT ON TABLE roles IS 'Vai trò (ROLE_DOCTOR, ROLE_ADMIN,...)';

COMMENT ON COLUMN roles.role_id IS 'ID vai trò';    -- Unique constraint: one employee cannot have duplicate shift on same date

COMMENT ON COLUMN roles.base_role_id IS 'FK → base_roles.base_role_id';    CONSTRAINT uk_employee_shift_date_shift UNIQUE (employee_id, work_date, work_shift_id)

COMMENT ON COLUMN roles.home_path_override IS 'Override path (nullable)';);



CREATE INDEX idx_roles_base_role ON roles(base_role_id);-- Indexes for employee_shifts

CREATE INDEX idx_employee_shifts_employee ON employee_shifts(employee_id);

-- Table: accountsCREATE INDEX idx_employee_shifts_date ON employee_shifts(work_date);

CREATE TABLE accounts (CREATE INDEX idx_employee_shifts_status ON employee_shifts(status);

    account_id SERIAL PRIMARY KEY,CREATE INDEX idx_employee_shifts_source ON employee_shifts(source);

    account_code VARCHAR(20) UNIQUE NOT NULL,CREATE INDEX idx_employee_shifts_registration ON employee_shifts(registration_id);

    username VARCHAR(50) UNIQUE NOT NULL,CREATE INDEX idx_employee_shifts_employee_date ON employee_shifts(employee_id, work_date);

    email VARCHAR(100) UNIQUE NOT NULL,CREATE INDEX idx_employee_shifts_date_shift ON employee_shifts(work_date, work_shift_id);

    password VARCHAR(255) NOT NULL,

    role_id VARCHAR(50) NOT NULL,COMMENT ON TABLE employee_shifts IS 'Final scheduled shifts for all employees (generated by batch jobs or manual entry)';

    status account_status DEFAULT 'ACTIVE',COMMENT ON COLUMN employee_shifts.source IS 'BATCH_JOB: monthly full-time job, REGISTRATION_JOB: weekly part-time job, MANUAL: manually created, OVERTIME: from overtime request';

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,COMMENT ON COLUMN employee_shifts.registration_id IS 'Links to registration for part-time employees (NULL for full-time)';

    updated_at TIMESTAMP,COMMENT ON COLUMN employee_shifts.status IS 'SCHEDULED: future shift, COMPLETED: worked, CANCELLED: removed, ABSENT: no-show';



    CONSTRAINT fk_accounts_role FOREIGN KEY (role_id) -- =============================================

        REFERENCES roles(role_id) ON UPDATE CASCADE ON DELETE RESTRICT-- Sample Views (Optional)

);-- =============================================



COMMENT ON TABLE accounts IS 'Bảng tài khoản - Mỗi account có DUY NHẤT 1 role';-- View: Upcoming shifts for next 7 days

COMMENT ON COLUMN accounts.password IS 'Mật khẩu đã mã hóa BCrypt';CREATE OR REPLACE VIEW v_upcoming_shifts AS

COMMENT ON COLUMN accounts.role_id IS 'FK → roles.role_id (SINGLE ROLE)';SELECT

    es.shift_id,

CREATE INDEX idx_accounts_username ON accounts(username);    es.employee_id,

CREATE INDEX idx_accounts_email ON accounts(email);    e.employee_name,

CREATE INDEX idx_accounts_role ON accounts(role_id);    es.work_date,

    ws.shift_name,

-- Table: permissions    ws.start_time,

CREATE TABLE permissions (    ws.end_time,

    permission_id VARCHAR(100) PRIMARY KEY,    es.source,

    permission_name VARCHAR(100) UNIQUE NOT NULL,    es.status

    module VARCHAR(50),FROM employee_shifts es

    description TEXT,JOIN employees e ON es.employee_id = e.employee_id

    path VARCHAR(255),JOIN work_shifts ws ON es.work_shift_id = ws.shift_id

    icon VARCHAR(50),WHERE es.work_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'

    display_order INTEGER,    AND es.status = 'SCHEDULED'

    parent_permission_id VARCHAR(100),ORDER BY es.work_date, ws.start_time;

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,-- View: Pending renewal requests

    updated_at TIMESTAMP,CREATE OR REPLACE VIEW v_pending_renewals AS

    SELECT

    CONSTRAINT fk_permissions_parent FOREIGN KEY (parent_permission_id)     srr.renewal_id,

        REFERENCES permissions(permission_id) ON DELETE SET NULL    srr.employee_id,

);    e.employee_name,

    esr.registration_id,

COMMENT ON TABLE permissions IS 'Bảng quyền - Hỗ trợ parent-child hierarchy';    esr.effective_from,

COMMENT ON COLUMN permissions.permission_id IS 'ID quyền (VIEW_PATIENT,...)';    esr.effective_to,

COMMENT ON COLUMN permissions.module IS 'Module chức năng (APPOINTMENT, PATIENT,...)';    srr.expires_at,

COMMENT ON COLUMN permissions.path IS 'Đường dẫn sidebar (NULL = quyền hành động)';    srr.created_at

FROM shift_renewal_requests srr

CREATE INDEX idx_permissions_module ON permissions(module);JOIN employees e ON srr.employee_id = e.employee_id

CREATE INDEX idx_permissions_parent ON permissions(parent_permission_id);JOIN working_schedule esr ON srr.expiring_registration_id = esr.registration_id

WHERE srr.status = 'PENDING_ACTION'

-- Table: role_permissions    AND srr.expires_at > CURRENT_TIMESTAMP

CREATE TABLE role_permissions (ORDER BY srr.expires_at;

    role_id VARCHAR(50),

    permission_id VARCHAR(100),-- View: Holiday calendar

    CREATE OR REPLACE VIEW v_holiday_calendar AS

    PRIMARY KEY (role_id, permission_id),SELECT

        holiday_id,

    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)     holiday_date,

        REFERENCES roles(role_id) ON UPDATE CASCADE ON DELETE CASCADE,    holiday_name,

    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)     EXTRACT(DOW FROM holiday_date) as day_of_week,

        REFERENCES permissions(permission_id) ON UPDATE CASCADE ON DELETE CASCADE    TO_CHAR(holiday_date, 'Day') as day_name,

);    description

FROM holiday_dates

COMMENT ON TABLE role_permissions IS 'Junction - Ánh xạ quyền cho từng role';WHERE holiday_date >= CURRENT_DATE

ORDER BY holiday_date;

-- ============================================

-- EMPLOYEE & SPECIALIZATION TABLES-- =============================================

-- ============================================-- Triggers for updated_at columns

-- =============================================

-- Table: employees

CREATE TABLE employees (-- Function to update updated_at timestamp

    employee_id SERIAL PRIMARY KEY,CREATE OR REPLACE FUNCTION update_updated_at_column()

    account_id INTEGER UNIQUE NOT NULL,RETURNS TRIGGER AS $$

    employee_code VARCHAR(20) UNIQUE NOT NULL,BEGIN

    first_name VARCHAR(50) NOT NULL,    NEW.updated_at = CURRENT_TIMESTAMP;

    last_name VARCHAR(50) NOT NULL,    RETURN NEW;

    phone VARCHAR(15),END;

    date_of_birth DATE,$$ language 'plpgsql';

    address TEXT,

    employment_type employment_type,-- Apply triggers to tables

    is_active BOOLEAN DEFAULT TRUE,CREATE TRIGGER update_holiday_dates_updated_at BEFORE UPDATE ON holiday_dates

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

    updated_at TIMESTAMP,

    CREATE TRIGGER update_shift_renewal_requests_updated_at BEFORE UPDATE ON shift_renewal_requests

    CONSTRAINT fk_employees_account FOREIGN KEY (account_id)     FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

        REFERENCES accounts(account_id) ON UPDATE CASCADE ON DELETE CASCADE

);CREATE TRIGGER update_employee_shifts_updated_at BEFORE UPDATE ON employee_shifts

    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_employees_account ON employees(account_id);

CREATE INDEX idx_employees_code ON employees(employee_code);-- =============================================

-- Indexes for Performance Optimization

-- Table: specializations-- =============================================

CREATE TABLE specializations (

    specialization_id SERIAL PRIMARY KEY,-- Additional composite indexes for common queries

    specialization_code VARCHAR(20) UNIQUE NOT NULL,CREATE INDEX idx_employee_shifts_employee_status_date

    specialization_name VARCHAR(100) UNIQUE NOT NULL,    ON employee_shifts(employee_id, status, work_date);

    description TEXT,

    is_active BOOLEAN DEFAULT TRUE,CREATE INDEX idx_shift_renewal_employee_pending

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    ON shift_renewal_requests(employee_id, status, expires_at)

    updated_at TIMESTAMP    WHERE status = 'PENDING_ACTION';

);

-- =============================================

COMMENT ON COLUMN specializations.specialization_name IS 'Tên chuyên khoa (Implant, Chỉnh nha,...)';-- Table: refresh_tokens

-- Purpose: Store refresh tokens for JWT authentication with token rotation

-- Table: employee_specializations-- =============================================

CREATE TABLE employee_specializations (CREATE TABLE refresh_tokens (

    employee_id INTEGER,    id VARCHAR(36) PRIMARY KEY,

    specialization_id INTEGER,    account_id INTEGER NOT NULL,

        token_hash VARCHAR(512) NOT NULL UNIQUE,

    PRIMARY KEY (employee_id, specialization_id),    expires_at TIMESTAMP NOT NULL,

        is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_emp_spec_employee FOREIGN KEY (employee_id)     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        REFERENCES employees(employee_id) ON UPDATE CASCADE ON DELETE CASCADE,    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_emp_spec_specialization FOREIGN KEY (specialization_id)

        REFERENCES specializations(specialization_id) ON UPDATE CASCADE ON DELETE CASCADE    -- Foreign Key to accounts table

);    CONSTRAINT fk_refresh_token_account FOREIGN KEY (account_id)

        REFERENCES accounts(account_id) ON DELETE CASCADE

COMMENT ON TABLE employee_specializations IS 'Junction - Nhân viên có thể có nhiều chuyên khoa';);



-- ============================================-- Indexes for refresh_tokens

-- WORK SHIFT TABLES (SHARED)CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens(account_id);

-- ============================================CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Table: work_shiftCREATE INDEX idx_refresh_tokens_active ON refresh_tokens(is_active, expires_at)

CREATE TABLE work_shift (    WHERE is_active = TRUE;

    work_shift_id VARCHAR(20) PRIMARY KEY,

    work_shift_code VARCHAR(20) UNIQUE NOT NULL,COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens with hash for security and token rotation support';

    work_shift_name VARCHAR(100) NOT NULL,COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-512 hash of the actual refresh token for security';

    start_time TIME NOT NULL,COMMENT ON COLUMN refresh_tokens.is_active IS 'FALSE when token is rotated or revoked';

    end_time TIME NOT NULL,

    category work_slots_category_enum NOT NULL DEFAULT 'NORMAL',-- =============================================

    is_active BOOLEAN DEFAULT TRUE-- Table: blacklisted_tokens

);-- Purpose: Store blacklisted access tokens (for logout before expiry)

-- =============================================

COMMENT ON TABLE work_shift IS 'Bảng mẫu ca làm việc (Sáng, Chiều, Tối) - DÙNG CHUNG';CREATE TABLE blacklisted_tokens (

    token_hash VARCHAR(512) PRIMARY KEY,

-- ============================================    account_id INTEGER,

-- LUỒNG 1: LỊCH CỐ ĐỊNH (FULL-TIME & PT CỐ ĐỊNH)    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

-- ============================================    expires_at TIMESTAMP NOT NULL,

    reason VARCHAR(50), -- 'LOGOUT', 'PASSWORD_CHANGED', 'ADMIN_REVOKED'

-- Table: fixed_shift_registrations

CREATE TABLE fixed_shift_registrations (    -- Foreign Key to accounts table (nullable for cases where account is deleted)

    registration_id SERIAL PRIMARY KEY,    CONSTRAINT fk_blacklisted_token_account FOREIGN KEY (account_id)

    employee_id INTEGER NOT NULL,        REFERENCES accounts(account_id) ON DELETE SET NULL

    work_shift_id VARCHAR(20) NOT NULL,);

    effective_from DATE NOT NULL,

    effective_to DATE,-- Indexes for blacklisted_tokens

    is_active BOOLEAN DEFAULT TRUE,CREATE INDEX idx_blacklisted_tokens_account_id ON blacklisted_tokens(account_id);

    CREATE INDEX idx_blacklisted_tokens_expires_at ON blacklisted_tokens(expires_at);

    CONSTRAINT fk_fixed_shift_employee FOREIGN KEY (employee_id)

        REFERENCES employees(employee_id) ON DELETE CASCADE,COMMENT ON TABLE blacklisted_tokens IS 'Stores blacklisted access tokens to prevent reuse after logout';

    CONSTRAINT fk_fixed_shift_work_shift FOREIGN KEY (work_shift_id) COMMENT ON COLUMN blacklisted_tokens.reason IS 'Reason for blacklisting: LOGOUT, PASSWORD_CHANGED, ADMIN_REVOKED';

        REFERENCES work_shift(work_shift_id) ON DELETE RESTRICT

);-- =============================================

-- End of Schema Definition

COMMENT ON TABLE fixed_shift_registrations IS 'Lịch cố định do Admin gán (V11)';-- =============================================

COMMENT ON COLUMN fixed_shift_registrations.effective_from IS 'Bắt đầu áp dụng từ';
COMMENT ON COLUMN fixed_shift_registrations.effective_to IS 'Kết thúc khi nào? (NULL = vĩnh viễn)';

CREATE INDEX idx_fixed_shift_employee ON fixed_shift_registrations(employee_id, work_shift_id, is_active);

-- Table: fixed_registration_days
CREATE TABLE fixed_registration_days (
    registration_id INTEGER,
    day_of_week VARCHAR(10),

    PRIMARY KEY (registration_id, day_of_week),

    CONSTRAINT fk_fixed_days_registration FOREIGN KEY (registration_id)
        REFERENCES fixed_shift_registrations(registration_id) ON DELETE CASCADE
);

COMMENT ON TABLE fixed_registration_days IS 'Các ngày làm việc của Lịch cố định (V11)';
COMMENT ON COLUMN fixed_registration_days.day_of_week IS 'Ngày nào? (MONDAY, TUESDAY...)';

-- ============================================
-- LUỒNG 2: LỊCH LINH HOẠT (PART-TIME FLEX)
-- ============================================

-- Table: part_time_slots
CREATE TABLE part_time_slots (
    slot_id SERIAL PRIMARY KEY,
    work_shift_id VARCHAR(20) NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    quota INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_pt_slots_work_shift FOREIGN KEY (work_shift_id)
        REFERENCES work_shift(work_shift_id) ON DELETE RESTRICT,
    CONSTRAINT unique_shift_day UNIQUE (work_shift_id, day_of_week)
);

COMMENT ON TABLE part_time_slots IS 'Admin định nghĩa nhu cầu (VD: Cần 2 người Sáng T3)';
COMMENT ON COLUMN part_time_slots.quota IS 'Cần bao nhiêu người? (Quota)';
COMMENT ON COLUMN part_time_slots.is_active IS 'Admin có cho phép đăng ký suất này không';

-- Table: part_time_registrations
CREATE TABLE part_time_registrations (
    registration_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    part_time_slot_id INTEGER NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_pt_reg_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_reg_slot FOREIGN KEY (part_time_slot_id)
        REFERENCES part_time_slots(slot_id) ON DELETE RESTRICT
);

COMMENT ON TABLE part_time_registrations IS 'Lưu việc nhân viên "claim" 1 suất linh hoạt (V13)';
COMMENT ON COLUMN part_time_registrations.effective_from IS 'Bắt đầu áp dụng từ';
COMMENT ON COLUMN part_time_registrations.effective_to IS 'Kết thúc khi nào? (Thường có hạn)';

CREATE INDEX active_employee_slot_idx ON part_time_registrations(employee_id, part_time_slot_id, is_active);

-- Table: shift_renewal_requests
-- Table shift_renewal_requests (P7/BE-307)
-- Purpose: Manage renewal requests for FIXED shift registrations (FULL_TIME & PART_TIME_FIXED)
-- Job P8 creates requests when effective_to is approaching
-- Employees respond via API P7
CREATE TABLE shift_renewal_requests (
    renewal_id VARCHAR(20) PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    expiring_registration_id INTEGER NOT NULL, -- FK to fixed_shift_registrations
    status shift_renewal_requests_status_enum NOT NULL DEFAULT 'PENDING_ACTION',
    expires_at TIMESTAMP NOT NULL, -- Deadline to respond
    decline_reason TEXT, -- ⭐ V15 NEW: Required when DECLINED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,

    CONSTRAINT fk_renewal_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_renewal_registration FOREIGN KEY (expiring_registration_id)
        REFERENCES fixed_shift_registrations(registration_id) ON DELETE CASCADE -- ⭐ V15: Changed from part_time_registrations
);

-- Indexes for shift_renewal_requests
CREATE INDEX idx_renewal_employee_status ON shift_renewal_requests(employee_id, status);
CREATE INDEX idx_renewal_expires_at ON shift_renewal_requests(expires_at);

COMMENT ON TABLE shift_renewal_requests IS '⭐ V15: Dành cho Luồng 1 (Fixed shift registrations) - Gia hạn hợp đồng/lịch cố định';
COMMENT ON COLUMN shift_renewal_requests.decline_reason IS 'V15: Lý do nhân viên từ chối gia hạn (required khi DECLINED)';
COMMENT ON COLUMN shift_renewal_requests.expires_at IS 'Deadline phản hồi (thường 14 ngày trước khi hết hạn)';

-- ============================================
-- BẢNG KẾT QUẢ (DÙNG CHUNG)
-- ============================================

-- Table: employee_shifts
CREATE TABLE employee_shifts (
    shift_id VARCHAR(20) PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    work_date DATE NOT NULL,
    work_shift_id VARCHAR(20) NOT NULL,
    is_overtime BOOLEAN DEFAULT FALSE,
    status employee_shifts_status_enum NOT NULL DEFAULT 'SCHEDULED',
    source employee_shifts_source_enum NOT NULL,
    source_ot_request_id VARCHAR(20),
    source_off_request_id VARCHAR(20),
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    created_by INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_emp_shift_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_shift_work_shift FOREIGN KEY (work_shift_id)
        REFERENCES work_shift(work_shift_id) ON DELETE RESTRICT,
    CONSTRAINT fk_emp_shift_created_by FOREIGN KEY (created_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL,
    CONSTRAINT unique_shift UNIQUE (employee_id, work_date, work_shift_id)
);

COMMENT ON TABLE employee_shifts IS 'Lịch làm việc thực tế của nhân viên (kết quả từ cả 2 luồng)';
COMMENT ON COLUMN employee_shifts.source IS 'BATCH_JOB (từ Luồng 1), REGISTRATION_JOB (từ Luồng 2), ...';
COMMENT ON COLUMN employee_shifts.created_by IS 'FK -> employees.employee_id';

-- ============================================
-- OVERTIME & TIME OFF TABLES
-- ============================================

-- Table: overtime_requests
CREATE TABLE overtime_requests (
    request_id VARCHAR(20) PRIMARY KEY,
    request_code VARCHAR(20) UNIQUE NOT NULL,
    employee_id INTEGER NOT NULL,
    requested_by INTEGER NOT NULL,
    work_date DATE NOT NULL,
    work_shift_id VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    status overtime_requests_status_enum NOT NULL DEFAULT 'PENDING',
    approved_by INTEGER,
    approved_at TIMESTAMP,
    rejected_reason TEXT,
    cancellation_reason TEXT,

    CONSTRAINT fk_ot_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_ot_work_shift FOREIGN KEY (work_shift_id)
        REFERENCES work_shift(work_shift_id) ON DELETE RESTRICT,
    CONSTRAINT fk_ot_requested_by FOREIGN KEY (requested_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL,
    CONSTRAINT fk_ot_approved_by FOREIGN KEY (approved_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- Table: time_off_types
CREATE TABLE time_off_types (
    type_id VARCHAR(20) PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Table: time_off_requests
CREATE TABLE time_off_requests (
    request_id VARCHAR(20) PRIMARY KEY,
    request_code VARCHAR(20) UNIQUE NOT NULL,
    employee_id INTEGER NOT NULL,
    requested_by INTEGER NOT NULL,
    time_off_type_id VARCHAR(20) NOT NULL,
    work_shift_id VARCHAR(20),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status time_off_requests_status_enum NOT NULL DEFAULT 'PENDING',
    approved_by INTEGER,
    approved_at TIMESTAMP,
    rejected_reason TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_timeoff_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_timeoff_type FOREIGN KEY (time_off_type_id)
        REFERENCES time_off_types(type_id) ON DELETE RESTRICT,
    CONSTRAINT fk_timeoff_work_shift FOREIGN KEY (work_shift_id)
        REFERENCES work_shift(work_shift_id) ON DELETE SET NULL,
    CONSTRAINT fk_timeoff_requested_by FOREIGN KEY (requested_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL,
    CONSTRAINT fk_timeoff_approved_by FOREIGN KEY (approved_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

COMMENT ON COLUMN time_off_requests.work_shift_id IS 'Nullable, vì có thể nghỉ cả ngày';

-- Table: employee_leave_balances
CREATE TABLE employee_leave_balances (
    balance_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    time_off_type_id VARCHAR(20) NOT NULL,
    cycle_year INTEGER NOT NULL,
    total_days_allowed DECIMAL(5, 2) NOT NULL,
    days_taken DECIMAL(5, 2) NOT NULL DEFAULT 0,
    notes TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id),
    CONSTRAINT fk_balance_type FOREIGN KEY (time_off_type_id)
        REFERENCES time_off_types(type_id),
    CONSTRAINT unique_employee_type_year UNIQUE (employee_id, time_off_type_id, cycle_year)
);

-- Table: leave_balance_history
CREATE TABLE leave_balance_history (
    history_id SERIAL PRIMARY KEY,
    balance_id INTEGER NOT NULL,
    changed_by INTEGER,
    change_amount DECIMAL(5, 2) NOT NULL,
    reason VARCHAR(255),
    source_request_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_balance FOREIGN KEY (balance_id)
        REFERENCES employee_leave_balances(balance_id),
    CONSTRAINT fk_history_changed_by FOREIGN KEY (changed_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL,
    CONSTRAINT fk_history_source_request FOREIGN KEY (source_request_id)
        REFERENCES time_off_requests(request_id)
);

COMMENT ON COLUMN leave_balance_history.changed_by IS 'FK -> employees.employee_id';
COMMENT ON COLUMN leave_balance_history.source_request_id IS 'FK -> time_off_requests.request_id';

-- ============================================
-- HOLIDAY TABLES
-- ============================================

-- Table: holiday_definitions
CREATE TABLE holiday_definitions (
    definition_id VARCHAR(20) PRIMARY KEY,
    holiday_name VARCHAR(100) UNIQUE NOT NULL,
    holiday_type holiday_definitions_holiday_type_enum NOT NULL
);

-- Table: holiday_dates
CREATE TABLE holiday_dates (
    holiday_date DATE PRIMARY KEY,
    definition_id VARCHAR(20) NOT NULL,

    CONSTRAINT fk_holiday_date_definition FOREIGN KEY (definition_id)
        REFERENCES holiday_definitions(definition_id) ON DELETE CASCADE
);

-- ============================================
-- PATIENT & CONTACT TABLES
-- ============================================

-- Table: patients
CREATE TABLE patients (
    patient_id SERIAL PRIMARY KEY,
    account_id INTEGER UNIQUE,
    patient_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE,
    address TEXT,
    gender gender_type,
    medical_history TEXT,
    allergies TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_patients_account FOREIGN KEY (account_id)
        REFERENCES accounts(account_id) ON UPDATE CASCADE ON DELETE SET NULL
);

COMMENT ON TABLE patients IS 'Bảng bệnh nhân - account_id nullable (walk-in)';

CREATE INDEX idx_patients_account ON patients(account_id);
CREATE INDEX idx_patients_code ON patients(patient_code);
CREATE INDEX idx_patients_phone ON patients(phone);

-- Table: customer_contacts
CREATE TABLE customer_contacts (
    contact_id SERIAL PRIMARY KEY,
    contact_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    source contact_source,
    status contact_status DEFAULT 'NEW',
    notes TEXT,
    assigned_to INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_contacts_assigned FOREIGN KEY (assigned_to)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

COMMENT ON TABLE customer_contacts IS 'Bảng liên hệ khách hàng - Leads (Luồng 2)';
COMMENT ON COLUMN customer_contacts.notes IS 'Ghi chú';
COMMENT ON COLUMN customer_contacts.assigned_to IS 'FK → employees.employee_id';

CREATE INDEX idx_contacts_phone ON customer_contacts(phone);
CREATE INDEX idx_contacts_status ON customer_contacts(status);
CREATE INDEX idx_contacts_assigned ON customer_contacts(assigned_to);

-- Table: contact_histories
CREATE TABLE contact_histories (
    history_id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    contacted_by INTEGER NOT NULL,
    contact_type contact_type NOT NULL,
    contact_date TIMESTAMP NOT NULL,
    notes TEXT,
    result contact_result,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_contact FOREIGN KEY (contact_id)
        REFERENCES customer_contacts(contact_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_history_contacted_by FOREIGN KEY (contacted_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

COMMENT ON COLUMN contact_histories.contact_id IS 'FK → customer_contacts.contact_id';
COMMENT ON COLUMN contact_histories.contacted_by IS 'FK → employees.employee_id';

CREATE INDEX idx_history_contact ON contact_histories(contact_id);
CREATE INDEX idx_history_contacted_by ON contact_histories(contacted_by);
CREATE INDEX idx_history_date ON contact_histories(contact_date);

-- ============================================
-- AUTHENTICATION TABLES
-- ============================================

-- Table: refresh_tokens
CREATE TABLE refresh_tokens (
    token_id SERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    account_id INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_account FOREIGN KEY (account_id)
        REFERENCES accounts(account_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_account ON refresh_tokens(account_id);

-- Table: jwt_blacklist
CREATE TABLE jwt_blacklist (
    token_id SERIAL PRIMARY KEY,
    token_hash VARCHAR(64) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE jwt_blacklist IS 'Blacklist access tokens';
COMMENT ON COLUMN jwt_blacklist.token_hash IS 'SHA-256 hash của JWT';

CREATE INDEX idx_blacklist_hash ON jwt_blacklist(token_hash);

-- ============================================
-- MODULE: APPOINTMENT & RESOURCE MANAGEMENT
-- ============================================

-- Table: services
CREATE TABLE services (
    service_id SERIAL PRIMARY KEY,
    service_code VARCHAR(20) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    default_duration_minutes INTEGER NOT NULL,
    default_buffer_minutes INTEGER NOT NULL DEFAULT 15,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    specialization_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_service_specialization FOREIGN KEY (specialization_id)
        REFERENCES specializations(specialization_id) ON DELETE SET NULL
);

COMMENT ON TABLE services IS 'Bảng Dịch vụ đơn lẻ (Cạo vôi, Nhổ răng...)';
COMMENT ON COLUMN services.default_duration_minutes IS 'Thời gian thực hiện (phút)';
COMMENT ON COLUMN services.default_buffer_minutes IS 'Thời gian đệm (phút) dọn dẹp';

-- Table: rooms
CREATE TABLE rooms (
    room_id VARCHAR(50) PRIMARY KEY,
    room_code VARCHAR(20) UNIQUE NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    room_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE rooms IS 'Bảng Phòng/Ghế nha khoa - Tài nguyên hữu hình';
COMMENT ON COLUMN rooms.room_code IS 'VD: P1, GHE-01';
COMMENT ON COLUMN rooms.room_name IS 'VD: Phòng tiểu phẫu, Ghế 01';
COMMENT ON COLUMN rooms.room_type IS 'VD: STANDARD, SURGERY, XRAY';

-- ============================================
-- V16: BẢNG ROOM_SERVICES (Junction Table)
-- ============================================
-- Purpose: Define which services can be performed in which rooms
-- Business Rule: A room can support multiple services, a service can be performed in multiple rooms
-- Example: "Phòng Implant" can do "Cắm trụ Implant" and "Nâng xoang"
-- ============================================

CREATE TABLE room_services (
    room_id VARCHAR(50) NOT NULL,
    service_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (room_id, service_id),

    CONSTRAINT fk_room_services_room FOREIGN KEY (room_id)
        REFERENCES rooms(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_room_services_service FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE CASCADE
);

-- Index to quickly find all rooms that can perform a specific service
CREATE INDEX idx_room_services_service_id ON room_services(service_id);

COMMENT ON TABLE room_services IS 'V16: Junction table - Khai báo dịch vụ nào được phép thực hiện ở phòng nào';
COMMENT ON COLUMN room_services.room_id IS 'FK -> rooms.room_id';
COMMENT ON COLUMN room_services.service_id IS 'FK -> services.service_id';
COMMENT ON COLUMN room_services.created_at IS 'Thời điểm gán dịch vụ cho phòng';

-- Table: appointments
CREATE TABLE appointments (
    appointment_id SERIAL PRIMARY KEY,
    appointment_code VARCHAR(20) UNIQUE NOT NULL,
    patient_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    appointment_start_time TIMESTAMP NOT NULL,
    appointment_end_time TIMESTAMP NOT NULL,
    expected_duration_minutes INTEGER NOT NULL,
    status appointment_status_enum NOT NULL DEFAULT 'SCHEDULED',
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    rescheduled_to_appointment_id INTEGER,
    notes TEXT,
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_appt_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE RESTRICT,
    CONSTRAINT fk_appt_room FOREIGN KEY (room_id)
        REFERENCES rooms(room_id) ON DELETE RESTRICT,
    CONSTRAINT fk_appt_created_by FOREIGN KEY (created_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL,
    CONSTRAINT fk_appt_rescheduled_to FOREIGN KEY (rescheduled_to_appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE SET NULL
);

COMMENT ON TABLE appointments IS 'Bảng Lịch hẹn trung tâm - Khóa tài nguyên (Bác sĩ + Ghế)';
COMMENT ON COLUMN appointments.employee_id IS 'Bác sĩ CHÍNH';
COMMENT ON COLUMN appointments.room_id IS 'Ghế/Phòng CHÍNH';
COMMENT ON COLUMN appointments.notes IS 'Ghi chú của Lễ tân';
COMMENT ON COLUMN appointments.created_by IS 'FK -> employees.employee_id (Lễ tân)';

CREATE INDEX idx_appt_patient ON appointments(patient_id);
CREATE INDEX idx_appt_employee_time ON appointments(employee_id, appointment_start_time, appointment_end_time);
CREATE INDEX idx_appt_room_time ON appointments(room_id, appointment_start_time, appointment_end_time);
CREATE INDEX idx_appt_rescheduled ON appointments(rescheduled_to_appointment_id);

-- Table: appointment_participants (V13)
CREATE TABLE appointment_participants (
    appointment_id INTEGER,
    employee_id INTEGER,
    role appointment_participant_role_enum NOT NULL DEFAULT 'ASSISTANT',

    PRIMARY KEY (appointment_id, employee_id),

    CONSTRAINT fk_participants_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE
);

COMMENT ON TABLE appointment_participants IS 'Lưu các nhân viên khác tham gia lịch hẹn (ngoài Bác sĩ chính)';
COMMENT ON COLUMN appointment_participants.employee_id IS 'Nhân viên tham gia, VD: Phụ tá';
COMMENT ON COLUMN appointment_participants.role IS 'Vai trò trong ca: ASSISTANT (phụ tá), SECONDARY_DOCTOR (bác sĩ phụ), OBSERVER (quan sát viên)';

-- Table: appointment_services
CREATE TABLE appointment_services (
    appointment_id INTEGER,
    service_id INTEGER,

    PRIMARY KEY (appointment_id, service_id),

    CONSTRAINT fk_appt_services_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    CONSTRAINT fk_appt_services_service FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE RESTRICT
);

-- Table: appointment_audit_logs
CREATE TABLE appointment_audit_logs (
    log_id SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL,
    changed_by_employee_id INTEGER,
    action_type appointment_action_type NOT NULL,
    reason_code appointment_reason_code,
    old_value TEXT,
    new_value TEXT,
    old_start_time TIMESTAMP,
    new_start_time TIMESTAMP,
    old_status appointment_status_enum,
    new_status appointment_status_enum,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_employee FOREIGN KEY (changed_by_employee_id)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

COMMENT ON TABLE appointment_audit_logs IS 'Bảng nhật ký ghi lại các thay đổi quan trọng trên lịch hẹn';
COMMENT ON COLUMN appointment_audit_logs.notes IS 'Ghi chú thêm của người thực hiện';

CREATE INDEX idx_audit_appointment ON appointment_audit_logs(appointment_id);
CREATE INDEX idx_audit_action_type ON appointment_audit_logs(action_type);
CREATE INDEX idx_audit_reason_code ON appointment_audit_logs(reason_code);

-- ============================================
-- MODULE: TREATMENT PLAN (TEMPLATES)
-- ============================================

-- Table: treatment_plan_templates
CREATE TABLE treatment_plan_templates (
    template_id SERIAL PRIMARY KEY,
    template_code VARCHAR(20) UNIQUE NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    specialization_id INTEGER,
    estimated_total_cost DECIMAL(15, 2),
    estimated_duration_days INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_template_specialization FOREIGN KEY (specialization_id)
        REFERENCES specializations(specialization_id) ON DELETE SET NULL
);

COMMENT ON TABLE treatment_plan_templates IS 'Bảng "Gói" dịch vụ mẫu (Catalog)';
COMMENT ON COLUMN treatment_plan_templates.template_name IS 'VD: Gói Niềng Răng Mắc Cài Kim Loại';

-- Table: template_phases
CREATE TABLE template_phases (
    phase_template_id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL,
    phase_name VARCHAR(255) NOT NULL,
    step_order INTEGER NOT NULL,
    description TEXT,

    CONSTRAINT fk_phase_template FOREIGN KEY (template_id)
        REFERENCES treatment_plan_templates(template_id) ON DELETE CASCADE,
    CONSTRAINT unique_template_step UNIQUE (template_id, step_order)
);

COMMENT ON TABLE template_phases IS 'Các "Giai đoạn" mẫu trong 1 Gói';
COMMENT ON COLUMN template_phases.phase_name IS 'VD: Giai đoạn 1: Chuẩn bị';

-- Table: template_phase_services
CREATE TABLE template_phase_services (
    phase_template_id INTEGER,
    service_id INTEGER,
    quantity INTEGER NOT NULL DEFAULT 1,

    PRIMARY KEY (phase_template_id, service_id),

    CONSTRAINT fk_template_phase_service_phase FOREIGN KEY (phase_template_id)
        REFERENCES template_phases(phase_template_id) ON DELETE CASCADE,
    CONSTRAINT fk_template_phase_service_service FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE RESTRICT
);

COMMENT ON TABLE template_phase_services IS 'Các "Dịch vụ" mẫu trong 1 Giai đoạn';

-- ============================================
-- MODULE: PATIENT TREATMENT PLANS (INSTANCES)
-- ============================================

-- Table: patient_treatment_plans
CREATE TABLE patient_treatment_plans (
    patient_plan_id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    source_template_id INTEGER,
    plan_name VARCHAR(255) NOT NULL,
    status treatment_plan_status NOT NULL DEFAULT 'PENDING',
    total_cost DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    final_cost DECIMAL(15, 2) NOT NULL,
    payment_type treatment_plan_payment_type NOT NULL DEFAULT 'PHASED',
    start_date DATE,
    expected_end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_patient_plan_patient FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_patient_plan_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE RESTRICT,
    CONSTRAINT fk_patient_plan_template FOREIGN KEY (source_template_id)
        REFERENCES treatment_plan_templates(template_id) ON DELETE SET NULL
);

COMMENT ON TABLE patient_treatment_plans IS '"Hợp đồng" điều trị của bệnh nhân';
COMMENT ON COLUMN patient_treatment_plans.employee_id IS 'Bác sĩ phụ trách';
COMMENT ON COLUMN patient_treatment_plans.plan_name IS 'VD: Lộ trình niềng răng của chị B';
COMMENT ON COLUMN patient_treatment_plans.total_cost IS 'Giá niêm yết';
COMMENT ON COLUMN patient_treatment_plans.final_cost IS 'Công nợ thực tế (total - discount)';

CREATE INDEX idx_patient_plan_patient_status ON patient_treatment_plans(patient_id, status);
CREATE INDEX idx_patient_plan_employee_status ON patient_treatment_plans(employee_id, status);

-- Table: patient_plan_phases
CREATE TABLE patient_plan_phases (
    patient_phase_id SERIAL PRIMARY KEY,
    patient_plan_id INTEGER NOT NULL,
    phase_name VARCHAR(255) NOT NULL,
    step_order INTEGER NOT NULL,
    status plan_phase_status NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_patient_phase_plan FOREIGN KEY (patient_plan_id)
        REFERENCES patient_treatment_plans(patient_plan_id) ON DELETE CASCADE,
    CONSTRAINT unique_plan_step UNIQUE (patient_plan_id, step_order)
);

COMMENT ON TABLE patient_plan_phases IS 'Các "Giai đoạn" thực tế của bệnh nhân';

-- Table: patient_plan_items
CREATE TABLE patient_plan_items (
    patient_item_id SERIAL PRIMARY KEY,
    patient_phase_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(15, 2) NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    status plan_item_status NOT NULL DEFAULT 'PENDING_APPROVAL',

    CONSTRAINT fk_patient_item_phase FOREIGN KEY (patient_phase_id)
        REFERENCES patient_plan_phases(patient_phase_id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_item_service FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE RESTRICT
);

COMMENT ON TABLE patient_plan_items IS 'Checklist công việc/hạng mục của Bác sĩ';
COMMENT ON COLUMN patient_plan_items.item_name IS 'Copy từ service_name (cho phép sửa)';
COMMENT ON COLUMN patient_plan_items.unit_price IS 'Đơn giá tại thời điểm ký HĐ';

CREATE INDEX idx_patient_item_phase_status ON patient_plan_items(patient_phase_id, status);

-- Table: appointment_plan_items
CREATE TABLE appointment_plan_items (
    appointment_id INTEGER,
    patient_item_id INTEGER,

    PRIMARY KEY (appointment_id, patient_item_id),

    CONSTRAINT fk_appt_plan_item_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    CONSTRAINT fk_appt_plan_item_item FOREIGN KEY (patient_item_id)
        REFERENCES patient_plan_items(patient_item_id) ON DELETE CASCADE
);

COMMENT ON TABLE appointment_plan_items IS 'Nối 1 buổi hẹn với 1 (hoặc nhiều) hạng mục trong Lộ trình';

-- ============================================
-- MODULE: PATIENT RECORDS (FILES)
-- ============================================

-- Table: patient_records
CREATE TABLE patient_records (
    record_id SERIAL PRIMARY KEY,
    record_code VARCHAR(20) UNIQUE NOT NULL,
    patient_id INTEGER NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url_or_key TEXT NOT NULL,
    file_type VARCHAR(50),
    file_size_kb INTEGER,
    record_type patient_record_type NOT NULL,
    description TEXT,
    appointment_id INTEGER,
    patient_plan_id INTEGER,
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    CONSTRAINT fk_record_plan FOREIGN KEY (patient_plan_id)
        REFERENCES patient_treatment_plans(patient_plan_id) ON DELETE SET NULL,
    CONSTRAINT fk_record_created_by FOREIGN KEY (created_by)
        REFERENCES employees(employee_id) ON DELETE SET NULL
);

COMMENT ON TABLE patient_records IS 'Lưu metadata của file (X-Ray, CT...). File thật nằm trên Cloud Storage.';
COMMENT ON COLUMN patient_records.file_name IS 'Tên file gốc (VD: ct_scan.jpg)';
COMMENT ON COLUMN patient_records.file_url_or_key IS 'Đường dẫn Firebase Storage/S3';
COMMENT ON COLUMN patient_records.file_type IS 'VD: image/jpeg, application/dicom';
COMMENT ON COLUMN patient_records.record_type IS 'Phân loại: XRAY, CT_SCAN, ...';
COMMENT ON COLUMN patient_records.description IS 'Bác sĩ ghi chú về file này';
COMMENT ON COLUMN patient_records.appointment_id IS 'File này được tạo trong buổi hẹn nào?';
COMMENT ON COLUMN patient_records.patient_plan_id IS 'File này thuộc Lộ trình điều trị nào?';
COMMENT ON COLUMN patient_records.created_by IS 'FK -> employees.employee_id (Ai đã upload?)';

CREATE INDEX idx_record_patient ON patient_records(patient_id);
CREATE INDEX idx_record_type ON patient_records(record_type);
CREATE INDEX idx_record_appointment ON patient_records(appointment_id);
CREATE INDEX idx_record_plan ON patient_records(patient_plan_id);

-- ============================================
-- END OF SCHEMA V14
-- ============================================
