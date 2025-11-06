-- Fix suppliers table migration issue
-- Run this SQL script in pgAdmin or DBeaver

-- Step 1: Drop foreign key constraints
DROP TABLE IF EXISTS item_suppliers CASCADE;

-- Step 2: Drop and recreate suppliers table with UUID
DROP TABLE IF EXISTS suppliers CASCADE;

-- Step 3: Hibernate will auto-create the table on next run with correct UUID type
