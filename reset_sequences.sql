-- Reset sequence for fixed_shift_registrations
SELECT setval(
    'fixed_shift_registrations_registration_id_seq', 
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1, 
    false
);

-- Verify current sequence value
SELECT currval('fixed_shift_registrations_registration_id_seq') as current_sequence_value;

-- Show existing records
SELECT registration_id, employee_id, work_shift_id, effective_from 
FROM fixed_shift_registrations 
ORDER BY registration_id;
