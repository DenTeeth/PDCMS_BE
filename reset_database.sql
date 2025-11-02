-- ============================================
-- SCRIPT: Reset Database (Dev Environment Only)
-- Purpose: Drop and recreate database to fix schema issues
-- ============================================

-- Disconnect all connections first
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'dental_clinic_db'
  AND pid <> pg_backend_pid();

-- Drop database
DROP DATABASE IF EXISTS dental_clinic_db;

-- Create new database
CREATE DATABASE dental_clinic_db
  WITH OWNER = root
  ENCODING = 'UTF8'
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  TEMPLATE = template0;

-- ============================================
-- HOW TO RUN:
-- 
-- Option 1: DBeaver/pgAdmin
--   1. Connect to PostgreSQL server (NOT dental_clinic_db)
--   2. Open this file
--   3. Execute all statements
--
-- Option 2: Command line (if psql is in PATH)
--   psql -U root -d postgres -f reset_database.sql
-- ============================================
