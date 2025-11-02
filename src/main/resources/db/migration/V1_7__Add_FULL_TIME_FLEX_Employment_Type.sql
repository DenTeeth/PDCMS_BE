-- Migration V1.7: Add FULL_TIME_FLEX to employment_type check constraint
-- Author: System
-- Date: 2025-11-02
-- Description: Extend employment_type to support FULL_TIME_FLEX for flexible full-time employees

-- Drop existing check constraint
ALTER TABLE employees
    DROP CONSTRAINT IF EXISTS employees_employment_type_check;

-- Add new check constraint with FULL_TIME_FLEX
ALTER TABLE employees
    ADD CONSTRAINT employees_employment_type_check
    CHECK (employment_type::text = ANY (ARRAY[
        'FULL_TIME'::character varying,
        'FULL_TIME_FLEX'::character varying,
        'PART_TIME_FIXED'::character varying,
        'PART_TIME_FLEX'::character varying,
        'PART_TIME'::character varying  -- Legacy support
    ]::text[]));

COMMENT ON CONSTRAINT employees_employment_type_check ON employees IS 
'Valid employment types: FULL_TIME (fixed schedule), FULL_TIME_FLEX (flexible schedule), PART_TIME_FIXED (fixed schedule), PART_TIME_FLEX (flexible schedule), PART_TIME (legacy)';
