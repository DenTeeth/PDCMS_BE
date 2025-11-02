-- ============================================
-- Migration V1.7: Add HolidayDefinition Support
-- Purpose: Add definition_id and created_at to holiday_dates
-- Date: 2025-11-02
-- ============================================

-- Step 1: Add definition_id column as NULLABLE first
ALTER TABLE holiday_dates
ADD COLUMN IF NOT EXISTS definition_id VARCHAR(20);

-- Step 2: Add created_at and updated_at columns as NULLABLE
ALTER TABLE holiday_dates
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6);

ALTER TABLE holiday_dates
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6);

-- Step 3: Update existing records with default values
UPDATE holiday_dates
SET definition_id = 'LEGACY_HOLIDAY',
    created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW())
WHERE definition_id IS NULL;

-- Step 4: Create default holiday definition for legacy data
INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, is_recurring, created_at, updated_at)
VALUES ('LEGACY_HOLIDAY', 'Legacy Holiday Data', 'NATIONAL', true, NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

-- Step 5: Now make definition_id NOT NULL
ALTER TABLE holiday_dates
ALTER COLUMN definition_id SET NOT NULL;

ALTER TABLE holiday_dates
ALTER COLUMN created_at SET NOT NULL;

-- Step 6: Add foreign key constraint
ALTER TABLE holiday_dates
ADD CONSTRAINT fk_holiday_date_definition
FOREIGN KEY (definition_id)
REFERENCES holiday_definitions(definition_id);

-- Step 7: Create composite unique constraint
ALTER TABLE holiday_dates
DROP CONSTRAINT IF EXISTS uk_holiday_date;

ALTER TABLE holiday_dates
ADD CONSTRAINT uk_holiday_date_definition
UNIQUE (holiday_date, definition_id);

-- ============================================
-- RESULT:
-- - All existing holiday_dates now have definition_id = 'LEGACY_HOLIDAY'
-- - Schema is compatible with new Entity structure
-- - Foreign key constraint is in place
-- ============================================
