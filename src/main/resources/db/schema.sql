-- =============================================
-- Dental Clinic Management System - Database Schema
-- PostgreSQL Database Schema Definition
-- =============================================

-- Drop existing tables (in correct order to avoid FK violations)
DROP TABLE IF EXISTS employee_shifts CASCADE;
DROP TABLE IF EXISTS shift_renewal_requests CASCADE;
DROP TABLE IF EXISTS holiday_dates CASCADE;

-- =============================================
-- Table: holiday_dates
-- Purpose: Store public holidays and special non-working days
-- =============================================
CREATE TABLE holiday_dates (
    holiday_id BIGSERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    holiday_name VARCHAR(255) NOT NULL,
    year INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for holiday_dates
CREATE INDEX idx_holiday_dates_date ON holiday_dates(holiday_date);
CREATE INDEX idx_holiday_dates_year ON holiday_dates(year);

COMMENT ON TABLE holiday_dates IS 'Stores public holidays and special non-working days for scheduling';
COMMENT ON COLUMN holiday_dates.holiday_date IS 'The actual date of the holiday (must be unique)';
COMMENT ON COLUMN holiday_dates.year IS 'Year of the holiday for quick filtering';

-- =============================================
-- Table: shift_renewal_requests
-- Purpose: Manage shift registration renewal requests for part-time employees
-- =============================================
CREATE TABLE shift_renewal_requests (
    renewal_id VARCHAR(12) PRIMARY KEY,
    expiring_registration_id VARCHAR(12) NOT NULL,
    employee_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTION',
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_renewal_registration FOREIGN KEY (expiring_registration_id)
        REFERENCES working_schedule(registration_id) ON DELETE CASCADE,
    CONSTRAINT fk_renewal_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,

    -- Check Constraints
    CONSTRAINT chk_renewal_status CHECK (status IN ('PENDING_ACTION', 'CONFIRMED', 'DECLINED', 'EXPIRED')),
    CONSTRAINT chk_renewal_id_format CHECK (renewal_id ~ '^SRR[0-9]{9}$')
);

-- Indexes for shift_renewal_requests
CREATE INDEX idx_shift_renewal_employee ON shift_renewal_requests(employee_id);
CREATE INDEX idx_shift_renewal_status ON shift_renewal_requests(status);
CREATE INDEX idx_shift_renewal_expires_at ON shift_renewal_requests(expires_at);
CREATE INDEX idx_shift_renewal_registration ON shift_renewal_requests(expiring_registration_id);
CREATE INDEX idx_shift_renewal_employee_status ON shift_renewal_requests(employee_id, status);

COMMENT ON TABLE shift_renewal_requests IS 'Tracks renewal requests for expiring shift registrations';
COMMENT ON COLUMN shift_renewal_requests.renewal_id IS 'Format: SRRYYMMDDSSS (SRR + date + sequence)';
COMMENT ON COLUMN shift_renewal_requests.status IS 'PENDING_ACTION: awaiting response, CONFIRMED: accepted, DECLINED: rejected, EXPIRED: no response';
COMMENT ON COLUMN shift_renewal_requests.expires_at IS 'Deadline for employee response (typically 7 days from creation)';

-- =============================================
-- Table: employee_shifts
-- Purpose: Store actual scheduled shifts for employees (final schedule)
-- =============================================
CREATE TABLE employee_shifts (
    shift_id BIGSERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    work_date DATE NOT NULL,
    work_shift_id VARCHAR(50) NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    registration_id VARCHAR(12),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_employee_shift_employee FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_shift_work_shift FOREIGN KEY (work_shift_id)
        REFERENCES work_shifts(shift_id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_shift_registration FOREIGN KEY (registration_id)
        REFERENCES working_schedule(registration_id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_employee_shift_source CHECK (source IN ('BATCH_JOB', 'REGISTRATION_JOB', 'MANUAL', 'OVERTIME')),
    CONSTRAINT chk_employee_shift_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'ABSENT')),

    -- Unique constraint: one employee cannot have duplicate shift on same date
    CONSTRAINT uk_employee_shift_date_shift UNIQUE (employee_id, work_date, work_shift_id)
);

-- Indexes for employee_shifts
CREATE INDEX idx_employee_shifts_employee ON employee_shifts(employee_id);
CREATE INDEX idx_employee_shifts_date ON employee_shifts(work_date);
CREATE INDEX idx_employee_shifts_status ON employee_shifts(status);
CREATE INDEX idx_employee_shifts_source ON employee_shifts(source);
CREATE INDEX idx_employee_shifts_registration ON employee_shifts(registration_id);
CREATE INDEX idx_employee_shifts_employee_date ON employee_shifts(employee_id, work_date);
CREATE INDEX idx_employee_shifts_date_shift ON employee_shifts(work_date, work_shift_id);

COMMENT ON TABLE employee_shifts IS 'Final scheduled shifts for all employees (generated by batch jobs or manual entry)';
COMMENT ON COLUMN employee_shifts.source IS 'BATCH_JOB: monthly full-time job, REGISTRATION_JOB: weekly part-time job, MANUAL: manually created, OVERTIME: from overtime request';
COMMENT ON COLUMN employee_shifts.registration_id IS 'Links to registration for part-time employees (NULL for full-time)';
COMMENT ON COLUMN employee_shifts.status IS 'SCHEDULED: future shift, COMPLETED: worked, CANCELLED: removed, ABSENT: no-show';

-- =============================================
-- Sample Views (Optional)
-- =============================================

-- View: Upcoming shifts for next 7 days
CREATE OR REPLACE VIEW v_upcoming_shifts AS
SELECT
    es.shift_id,
    es.employee_id,
    e.employee_name,
    es.work_date,
    ws.shift_name,
    ws.start_time,
    ws.end_time,
    es.source,
    es.status
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.shift_id
WHERE es.work_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
    AND es.status = 'SCHEDULED'
ORDER BY es.work_date, ws.start_time;

-- View: Pending renewal requests
CREATE OR REPLACE VIEW v_pending_renewals AS
SELECT
    srr.renewal_id,
    srr.employee_id,
    e.employee_name,
    esr.registration_id,
    esr.effective_from,
    esr.effective_to,
    srr.expires_at,
    srr.created_at
FROM shift_renewal_requests srr
JOIN employees e ON srr.employee_id = e.employee_id
JOIN working_schedule esr ON srr.expiring_registration_id = esr.registration_id
WHERE srr.status = 'PENDING_ACTION'
    AND srr.expires_at > CURRENT_TIMESTAMP
ORDER BY srr.expires_at;

-- View: Holiday calendar
CREATE OR REPLACE VIEW v_holiday_calendar AS
SELECT
    holiday_id,
    holiday_date,
    holiday_name,
    EXTRACT(DOW FROM holiday_date) as day_of_week,
    TO_CHAR(holiday_date, 'Day') as day_name,
    description
FROM holiday_dates
WHERE holiday_date >= CURRENT_DATE
ORDER BY holiday_date;

-- =============================================
-- Triggers for updated_at columns
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tables
CREATE TRIGGER update_holiday_dates_updated_at BEFORE UPDATE ON holiday_dates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shift_renewal_requests_updated_at BEFORE UPDATE ON shift_renewal_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employee_shifts_updated_at BEFORE UPDATE ON employee_shifts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Indexes for Performance Optimization
-- =============================================

-- Additional composite indexes for common queries
CREATE INDEX idx_employee_shifts_employee_status_date
    ON employee_shifts(employee_id, status, work_date);

CREATE INDEX idx_shift_renewal_employee_pending
    ON shift_renewal_requests(employee_id, status, expires_at)
    WHERE status = 'PENDING_ACTION';

-- =============================================
-- End of Schema Definition
-- =============================================
