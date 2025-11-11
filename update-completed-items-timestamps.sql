-- Quick fix: Update completed_at for COMPLETED items in seed data
-- Run this after seed data has been loaded

-- Update PLAN-20251001-001 completed items
UPDATE patient_plan_items
SET completed_at = '2025-10-02 09:00:00'
WHERE item_id = 1 AND status = 'COMPLETED';

UPDATE patient_plan_items
SET completed_at = '2025-10-03 10:30:00'
WHERE item_id = 2 AND status = 'COMPLETED';

UPDATE patient_plan_items
SET completed_at = '2025-10-05 14:00:00'
WHERE item_id = 3 AND status = 'COMPLETED';

UPDATE patient_plan_items
SET completed_at = '2025-10-16 09:00:00'
WHERE item_id = 4 AND status = 'COMPLETED';

UPDATE patient_plan_items
SET completed_at = '2025-10-17 10:00:00'
WHERE item_id = 5 AND status = 'COMPLETED';

-- Verify updates
SELECT
    item_id,
    item_name,
    status,
    completed_at,
    CASE
        WHEN status = 'COMPLETED' AND completed_at IS NOT NULL THEN '✅ OK'
        WHEN status = 'COMPLETED' AND completed_at IS NULL THEN '❌ MISSING'
        ELSE '⏸️ PENDING'
    END as validation
FROM patient_plan_items
WHERE phase_id IN (1, 2)
ORDER BY item_id;
