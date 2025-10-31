-- Fix PostgreSQL sequence desync for fixed_shift_registrations
-- This ensures the sequence starts from the correct value after seed data import

-- Reset sequence to max(registration_id) + 1
SELECT setval(
    'fixed_shift_registrations_registration_id_seq',
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1,
    false
);
