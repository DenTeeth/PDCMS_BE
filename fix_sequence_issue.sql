-- ============================================
-- FIX: PostgreSQL Sequence Desync Issue
-- Problem: duplicate key value violates unique constraint "fixed_shift_registrations_pkey"
-- Cause: Sequence is out of sync with actual max ID in table
-- ============================================

-- 1. Check current sequence value
SELECT
    'Current Sequence Value' as description,
    last_value as value
FROM fixed_shift_registrations_registration_id_seq;

-- 2. Check max ID in table
SELECT
    'Max Registration ID in Table' as description,
    COALESCE(MAX(registration_id), 0) as value
FROM fixed_shift_registrations;

-- 3. Reset sequence to correct value
-- This sets the next value to be max(registration_id) + 1
SELECT setval(
    'fixed_shift_registrations_registration_id_seq',
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1,
    false
);

-- 4. Verify the fix
SELECT
    'New Sequence Value (Next ID will be)' as description,
    last_value as value
FROM fixed_shift_registrations_registration_id_seq;

-- 5. Show all existing registrations
SELECT
    registration_id,
    employee_id,
    work_shift_id,
    effective_from,
    effective_to,
    is_active,
    created_at
FROM fixed_shift_registrations
ORDER BY registration_id;

-- ============================================
-- HOW TO RUN THIS SCRIPT:
-- ============================================
-- Option 1: Using pgAdmin
--   1. Open pgAdmin
--   2. Connect to dental_clinic_db
--   3. Tools → Query Tool
--   4. Paste this script
--   5. Click Execute (F5)
--
-- Option 2: Using DBeaver
--   1. Open DBeaver
--   2. Connect to dental_clinic_db
--   3. SQL Editor → New SQL Script
--   4. Paste this script
--   5. Execute (Ctrl+Enter)
--
-- Option 3: Using psql command line
--   psql -U postgres -d dental_clinic_db -f fix_sequence_issue.sql
-- ============================================
