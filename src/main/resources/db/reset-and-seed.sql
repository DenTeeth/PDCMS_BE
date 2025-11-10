-- =============================================
-- RESET DATABASE AND RE-SEED DATA
-- Run this manually when you need to reset all data
-- Usage: Run from command line or database tool
-- =============================================

-- STEP 1: Drop all tables in correct order (respect FK constraints)
DROP TABLE IF EXISTS appointment_plan_items CASCADE;
DROP TABLE IF EXISTS patient_plan_items CASCADE;
DROP TABLE IF EXISTS patient_plan_phases CASCADE;
DROP TABLE IF EXISTS patient_treatment_plans CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS service_categories CASCADE;

-- STEP 2: Let Hibernate recreate tables on next startup
-- (Just restart the application after running this script)

-- STEP 3: After tables are created, run the seed data
-- Execute the dental-clinic-seed-data.sql file
