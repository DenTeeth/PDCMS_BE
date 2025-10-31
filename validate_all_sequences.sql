-- ============================================
-- VALIDATE ALL POSTGRESQL SEQUENCES
-- ============================================
-- This script checks if all sequences are properly synchronized
-- with the maximum ID values in their respective tables

\echo '=========================================='
\echo 'CHECKING ALL SEQUENCES'
\echo '=========================================='

-- Check base_roles sequence
\echo '\n1. base_roles:'
SELECT
    'base_roles' as table_name,
    COALESCE(MAX(base_role_id), 0) as max_id,
    nextval('base_roles_base_role_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('base_roles_base_role_id_seq') - 1 > COALESCE(MAX(base_role_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM base_roles;
SELECT setval('base_roles_base_role_id_seq', (SELECT COALESCE(MAX(base_role_id), 0) + 1 FROM base_roles), false);

-- Check accounts sequence
\echo '\n2. accounts:'
SELECT
    'accounts' as table_name,
    COALESCE(MAX(account_id), 0) as max_id,
    nextval('accounts_account_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('accounts_account_id_seq') - 1 > COALESCE(MAX(account_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM accounts;
SELECT setval('accounts_account_id_seq', (SELECT COALESCE(MAX(account_id), 0) + 1 FROM accounts), false);

-- Check employees sequence
\echo '\n3. employees:'
SELECT
    'employees' as table_name,
    COALESCE(MAX(employee_id), 0) as max_id,
    nextval('employees_employee_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('employees_employee_id_seq') - 1 > COALESCE(MAX(employee_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM employees;
SELECT setval('employees_employee_id_seq', (SELECT COALESCE(MAX(employee_id), 0) + 1 FROM employees), false);

-- Check patients sequence
\echo '\n4. patients:'
SELECT
    'patients' as table_name,
    COALESCE(MAX(patient_id), 0) as max_id,
    nextval('patients_patient_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('patients_patient_id_seq') - 1 > COALESCE(MAX(patient_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM patients;
SELECT setval('patients_patient_id_seq', (SELECT COALESCE(MAX(patient_id), 0) + 1 FROM patients), false);

-- NOTE: specializations uses manual ID (no SERIAL/IDENTITY), so no sequence to check

-- Check fixed_shift_registrations sequence
\echo '\n5. fixed_shift_registrations:'
SELECT
    'fixed_shift_registrations' as table_name,
    COALESCE(MAX(registration_id), 0) as max_id,
    nextval('fixed_shift_registrations_registration_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('fixed_shift_registrations_registration_id_seq') - 1 > COALESCE(MAX(registration_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM fixed_shift_registrations;
SELECT setval('fixed_shift_registrations_registration_id_seq', (SELECT COALESCE(MAX(registration_id), 0) + 1 FROM fixed_shift_registrations), false);

-- Check part_time_slots sequence
\echo '\n6. part_time_slots:'
SELECT
    'part_time_slots' as table_name,
    COALESCE(MAX(slot_id), 0) as max_id,
    nextval('part_time_slots_slot_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('part_time_slots_slot_id_seq') - 1 > COALESCE(MAX(slot_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM part_time_slots;
SELECT setval('part_time_slots_slot_id_seq', (SELECT COALESCE(MAX(slot_id), 0) + 1 FROM part_time_slots), false);

-- Check holiday_dates sequence
\echo '\n7. holiday_dates:'
SELECT
    'holiday_dates' as table_name,
    COALESCE(MAX(holiday_id), 0) as max_id,
    nextval('holiday_dates_holiday_id_seq') - 1 as current_seq,
    CASE
        WHEN nextval('holiday_dates_holiday_id_seq') - 1 > COALESCE(MAX(holiday_id), 0)
        THEN '✓ OK'
        ELSE '✗ NEEDS FIX'
    END as status
FROM holiday_dates;
SELECT setval('holiday_dates_holiday_id_seq', (SELECT COALESCE(MAX(holiday_id), 0) + 1 FROM holiday_dates), false);

\echo '\n=========================================='
\echo 'ALL SEQUENCES VALIDATED AND FIXED!'
\echo '=========================================='
