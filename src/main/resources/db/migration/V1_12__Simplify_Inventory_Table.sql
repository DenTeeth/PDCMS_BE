-- V1_12: Simplify Inventory Table
-- Drop accounting and certification columns (handled by accounting dept)
-- Aligns with simplified Inventory entity (removed pricing, unit measure, certification)

-- Drop columns that are no longer needed
ALTER TABLE inventory DROP COLUMN IF EXISTS unit_price;
ALTER TABLE inventory DROP COLUMN IF EXISTS unit_of_measure;
ALTER TABLE inventory DROP COLUMN IF EXISTS is_certified;
ALTER TABLE inventory DROP COLUMN IF EXISTS certification_date;

-- Add comment to document the change
COMMENT ON TABLE inventory IS 'Simplified inventory table - pricing and certification managed by accounting department';
