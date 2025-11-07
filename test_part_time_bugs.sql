-- =============================================
-- Part-Time Registration Bug Testing Script
-- Execute these commands in order to test all 3 bugs
-- =============================================

-- Connect to database: docker exec -it postgres-dental psql -U root -d dental_clinic_db

-- =============================================
-- STEP 1: Setup - Verify test data exists
-- =============================================

-- Check manager account (quanli1)
SELECT a.account_id, a.username, a.email, e.employee_id, e.first_name, e.last_name, r.role_name
FROM accounts a
JOIN employees e ON a.account_id = e.account_id
JOIN roles r ON a.role_id = r.role_id
WHERE a.username = 'quanli1';
-- Expected: Should return manager account with employee_id

-- Check test employees for registration
SELECT employee_id, employee_code, first_name, last_name, is_active
FROM employees
WHERE is_active = true
ORDER BY employee_id
LIMIT 5;

-- Check active part-time slots
SELECT slot_id, work_shift_id, day_of_week, quota, effective_from, effective_to, is_active
FROM part_time_slots
WHERE is_active = true
ORDER BY slot_id
LIMIT 5;

-- Check work shifts
SELECT work_shift_id, shift_name, start_time, end_time, category, is_active
FROM work_shifts
WHERE is_active = true
ORDER BY work_shift_id;

-- =============================================
-- STEP 2: Clean up any existing test data
-- =============================================

-- Delete test registrations (if any exist from previous tests)
DELETE FROM part_time_registration_dates 
WHERE registration_id IN (
    SELECT registration_id 
    FROM part_time_registrations 
    WHERE employee_id IN (
        SELECT employee_id FROM employees WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003')
    )
);

DELETE FROM part_time_registrations 
WHERE employee_id IN (
    SELECT employee_id FROM employees WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003')
);

-- Delete test employee shifts (if any)
DELETE FROM employee_shifts 
WHERE employee_id IN (
    SELECT employee_id FROM employees WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003')
)
AND work_date >= '2025-11-01';

-- =============================================
-- STEP 3: Create test part-time slot (if needed)
-- =============================================

-- Insert a test slot for MONDAY, MORNING shift
INSERT INTO part_time_slots (
    work_shift_id, 
    day_of_week, 
    quota, 
    effective_from, 
    effective_to, 
    is_active, 
    created_at
)
VALUES (
    (SELECT work_shift_id FROM work_shifts WHERE category = 'NORMAL' AND start_time = '08:00:00' LIMIT 1),
    'MONDAY',
    5,  -- Quota of 5
    '2025-11-01',
    '2026-01-31',
    true,
    NOW()
)
ON CONFLICT DO NOTHING
RETURNING slot_id, work_shift_id, day_of_week, quota;

-- Get the slot_id for testing
SELECT slot_id, work_shift_id, day_of_week, quota, effective_from, effective_to
FROM part_time_slots
WHERE day_of_week = 'MONDAY' 
  AND is_active = true
  AND effective_from <= CURRENT_DATE
  AND effective_to >= CURRENT_DATE
ORDER BY slot_id DESC
LIMIT 1;
-- NOTE: Remember this slot_id for API calls

-- =============================================
-- TEST SCENARIO 1: Overlapping Date Range (Bug #1)
-- =============================================

-- Create first registration (we'll approve this via API)
-- Employee: EMP001, Slot: (from above), Dates: 2025-11-09 to 2025-12-21

-- After API approval of first registration, create second overlapping one:
-- Employee: Same EMP001, Same Slot, Dates: 2025-11-07 to 2026-01-07
-- Try to approve via API - should be REJECTED with overlap error

-- Query to verify after test:
SELECT 
    r.registration_id,
    e.employee_code,
    r.effective_from,
    r.effective_to,
    r.status,
    COUNT(rd.registered_date) as working_days
FROM part_time_registrations r
JOIN employees e ON r.employee_id = e.employee_id
LEFT JOIN part_time_registration_dates rd ON r.registration_id = rd.registration_id
WHERE e.employee_code = 'EMP001'
GROUP BY r.registration_id, e.employee_code, r.effective_from, r.effective_to, r.status
ORDER BY r.created_at;

-- Check for overlaps (should be NONE after fix)
SELECT 
    r1.registration_id as reg1_id,
    r2.registration_id as reg2_id,
    r1.employee_id,
    r1.effective_from as r1_from,
    r1.effective_to as r1_to,
    r2.effective_from as r2_from,
    r2.effective_to as r2_to,
    r1.status as r1_status,
    r2.status as r2_status
FROM part_time_registrations r1
JOIN part_time_registrations r2 
    ON r1.employee_id = r2.employee_id 
    AND r1.part_time_slot_id = r2.part_time_slot_id
    AND r1.registration_id < r2.registration_id
WHERE (r1.status IN ('APPROVED', 'PENDING') AND r2.status IN ('APPROVED', 'PENDING'))
  AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to);
-- Expected: 0 rows (no overlaps)

-- =============================================
-- TEST SCENARIO 2: Clear Error Messages (Bug #2)
-- =============================================

-- This is tested with same scenario as Bug #1
-- The error message should be clear and show:
-- - Number of overlapping dates
-- - First 5 dates
-- - Existing registration ID
-- - Status (APPROVED/PENDING)

-- =============================================
-- TEST SCENARIO 3: Pre-existing Employee Shift (Bug #3)
-- =============================================

-- Setup: Create employee shift manually (simulate existing shift)
INSERT INTO employee_shifts (
    employee_id,
    work_date,
    work_shift_id,
    status,
    notes,
    created_at
)
VALUES (
    (SELECT employee_id FROM employees WHERE employee_code = 'EMP002' LIMIT 1),
    '2025-11-17',  -- A Monday in the future
    (SELECT work_shift_id FROM work_shifts WHERE category = 'NORMAL' AND start_time = '08:00:00' LIMIT 1),
    'SCHEDULED',
    'Pre-existing shift for Bug #3 testing',
    NOW()
)
ON CONFLICT (employee_id, work_date, work_shift_id) DO NOTHING;

-- Verify the shift was created
SELECT 
    es.shift_id,
    e.employee_code,
    es.work_date,
    ws.shift_name,
    ws.start_time,
    ws.end_time,
    es.status
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_code = 'EMP002'
  AND es.work_date = '2025-11-17';

-- Now create a part-time registration via API that includes 2025-11-17
-- Employee: EMP002, Dates: 2025-11-10 to 2025-11-24
-- Try to approve via API - should be REJECTED with pre-existing shift error

-- Query to verify no duplicate shifts after test
SELECT 
    e.employee_code,
    es.work_date,
    ws.shift_name,
    COUNT(*) as shift_count
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_code = 'EMP002'
  AND es.work_date >= '2025-11-10'
  AND es.work_date <= '2025-11-24'
GROUP BY e.employee_code, es.work_date, ws.shift_name
HAVING COUNT(*) > 1;
-- Expected: 0 rows (no duplicates)

-- =============================================
-- CLEANUP QUERIES (run after all tests)
-- =============================================

-- View all test registrations
SELECT 
    r.registration_id,
    e.employee_code,
    s.day_of_week,
    r.effective_from,
    r.effective_to,
    r.status,
    r.processed_at,
    r.processed_by
FROM part_time_registrations r
JOIN employees e ON r.employee_id = e.employee_id
JOIN part_time_slots s ON r.part_time_slot_id = s.slot_id
WHERE e.employee_code IN ('EMP001', 'EMP002', 'EMP003')
ORDER BY r.created_at DESC;

-- View all test employee shifts
SELECT 
    e.employee_code,
    es.work_date,
    ws.shift_name,
    es.status,
    es.notes
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_code IN ('EMP001', 'EMP002', 'EMP003')
  AND es.work_date >= '2025-11-01'
ORDER BY e.employee_code, es.work_date;

-- =============================================
-- SUMMARY VERIFICATION
-- =============================================

-- Final check: Should have NO overlapping APPROVED registrations
SELECT 'Overlapping Approved Registrations' as check_name, COUNT(*) as violations
FROM (
    SELECT r1.registration_id, r2.registration_id
    FROM part_time_registrations r1
    JOIN part_time_registrations r2 
        ON r1.employee_id = r2.employee_id 
        AND r1.part_time_slot_id = r2.part_time_slot_id
        AND r1.registration_id < r2.registration_id
    WHERE (r1.status IN ('APPROVED', 'PENDING') AND r2.status IN ('APPROVED', 'PENDING'))
      AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to)
) overlaps
UNION ALL
-- Final check: Should have NO duplicate employee shifts
SELECT 'Duplicate Employee Shifts' as check_name, COUNT(*) as violations
FROM (
    SELECT employee_id, work_date, work_shift_id, COUNT(*) as cnt
    FROM employee_shifts
    WHERE work_date >= '2025-11-01'
    GROUP BY employee_id, work_date, work_shift_id
    HAVING COUNT(*) > 1
) duplicates;

-- Expected: Both violations should be 0
