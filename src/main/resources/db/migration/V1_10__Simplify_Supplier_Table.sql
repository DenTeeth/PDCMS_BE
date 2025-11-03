-- Migration: Simplify Supplier table by removing complex tracking fields
-- Author: System
-- Date: 2025
-- Description: Remove certification, verification, and metrics fields from suppliers table

-- Drop unused columns
ALTER TABLE suppliers
    DROP COLUMN IF EXISTS certification_number,
    DROP COLUMN IF EXISTS registration_date,
    DROP COLUMN IF EXISTS expiry_date,
    DROP COLUMN IF EXISTS is_verified,
    DROP COLUMN IF EXISTS verification_date,
    DROP COLUMN IF EXISTS verification_by,
    DROP COLUMN IF EXISTS rating,
    DROP COLUMN IF EXISTS total_transactions,
    DROP COLUMN IF EXISTS last_transaction_date;

-- Drop unique constraint on certification_number
ALTER TABLE suppliers
    DROP CONSTRAINT IF EXISTS uk_certification_number;

-- Verify simplified structure
-- Remaining columns: supplier_id, supplier_name, phone_number, email, address, status, notes, created_at, updated_at
COMMENT ON TABLE suppliers IS 'Simplified supplier table with basic contact information only';
