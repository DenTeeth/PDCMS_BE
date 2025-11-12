-- ============================================
-- MIGRATION V19: Treatment Plan Enhancements
-- Date: 2025-11-12
-- Author: Backend Team
-- ============================================
-- PURPOSE:
-- 1. Add approval workflow for custom treatment plans (Price Override Control)
-- 2. Add patient consent tracking
-- 3. Add phase duration estimation
-- 4. Fix plan_item_status enum to match Java code
-- 5. Add sequence_number to template_phase_services (Bug fix from API 5.3 review)
-- ============================================

-- ============================================
-- STEP 1: Create new ENUM type for ApprovalStatus
-- ============================================
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approval_status') THEN
        CREATE TYPE approval_status AS ENUM (
            'DRAFT',           -- Bác sĩ đang soạn thảo
            'PENDING_REVIEW',  -- Chờ Quản lý duyệt
            'APPROVED',        -- Đã phê duyệt
            'REJECTED'         -- Bị từ chối
        );
    END IF;
END $$;

-- ============================================
-- STEP 2: Add approval columns to patient_treatment_plans
-- ============================================
ALTER TABLE patient_treatment_plans 
    ADD COLUMN IF NOT EXISTS approval_status approval_status NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS patient_consent_date TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS approved_by INTEGER NULL,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT NULL;

-- Add FK for approved_by
ALTER TABLE patient_treatment_plans 
    ADD CONSTRAINT fk_approved_by_employee 
    FOREIGN KEY (approved_by) REFERENCES employees(employee_id) 
    ON DELETE SET NULL;

-- Add comment
COMMENT ON COLUMN patient_treatment_plans.approval_status IS 'Trạng thái phê duyệt (V19): DRAFT, PENDING_REVIEW, APPROVED, REJECTED';
COMMENT ON COLUMN patient_treatment_plans.patient_consent_date IS 'Ngày bệnh nhân ký đồng thuận (V19)';
COMMENT ON COLUMN patient_treatment_plans.approved_by IS 'FK → employees.employee_id - Người duyệt (V19)';
COMMENT ON COLUMN patient_treatment_plans.approved_at IS 'Thời điểm duyệt (V19)';
COMMENT ON COLUMN patient_treatment_plans.rejection_reason IS 'Lý do từ chối (nếu status = REJECTED) (V19)';

-- ============================================
-- STEP 3: Add estimated_duration_days to patient_plan_phases
-- ============================================
ALTER TABLE patient_plan_phases 
    ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER NULL;

COMMENT ON COLUMN patient_plan_phases.estimated_duration_days IS 'Số ngày dự kiến của giai đoạn (V19) - Dùng để tính timeline';

-- ============================================
-- STEP 4: Add sequence_number to template_phase_services
-- (Bug fix: API 5.3 cần thứ tự để expand items đúng)
-- ============================================
ALTER TABLE template_phase_services 
    ADD COLUMN IF NOT EXISTS sequence_number INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN template_phase_services.sequence_number IS 'Thứ tự thực hiện dịch vụ trong giai đoạn (V19) - Fix bug API 5.3';

-- ============================================
-- STEP 5: Update plan_item_status ENUM to match Java
-- (Drop old values, add new ones)
-- ============================================

-- 5.1: Rename existing ENUM (backup)
ALTER TYPE plan_item_status RENAME TO plan_item_status_old;

-- 5.2: Create new ENUM with correct values
CREATE TYPE plan_item_status AS ENUM (
    'PENDING',            -- Đổi từ PENDING_APPROVAL (V19)
    'READY_FOR_BOOKING',  -- Không đổi
    'SCHEDULED',          -- Không đổi
    'IN_PROGRESS',        -- THÊM MỚI (V19)
    'COMPLETED'           -- Không đổi
    -- XÓA: CANCELLED (gom vào SKIPPED nếu cần - nhưng không dùng trong V19)
);

-- 5.3: Migrate data (convert old values to new)
ALTER TABLE patient_plan_items 
    ALTER COLUMN status TYPE plan_item_status 
    USING (
        CASE status::text
            WHEN 'PENDING_APPROVAL' THEN 'PENDING'::plan_item_status
            WHEN 'CANCELLED' THEN 'PENDING'::plan_item_status  -- Fallback
            ELSE status::text::plan_item_status
        END
    );

-- 5.4: Drop old ENUM
DROP TYPE plan_item_status_old;

COMMENT ON TYPE plan_item_status IS 'Trạng thái hạng mục (V19): Đã cập nhật để khớp Java Enum';

-- ============================================
-- STEP 6: Add index for approval_status (performance)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_plans_approval_status 
    ON patient_treatment_plans(approval_status, created_by);

CREATE INDEX IF NOT EXISTS idx_plans_awaiting_approval 
    ON patient_treatment_plans(approval_status, created_at) 
    WHERE approval_status = 'PENDING_REVIEW';

-- ============================================
-- STEP 7: Update seed data (if needed)
-- ============================================
-- Update existing plans to APPROVED (backward compatibility)
UPDATE patient_treatment_plans 
SET approval_status = 'APPROVED', 
    approved_at = created_at
WHERE approval_status = 'DRAFT' 
  AND status IN ('IN_PROGRESS', 'COMPLETED');

-- ============================================
-- STEP 8: Add audit triggers (Optional - for enterprise)
-- ============================================
-- (Bỏ qua trong V19 - Sẽ thêm trong future version nếu cần)

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify migration success:

-- 1. Check new columns exist
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'patient_treatment_plans'
  AND column_name IN ('approval_status', 'patient_consent_date', 'approved_by', 'approved_at', 'rejection_reason');

-- 2. Check new enum values
SELECT enumlabel 
FROM pg_enum 
WHERE enumtypid = 'approval_status'::regtype 
ORDER BY enumsortorder;

-- 3. Check plan_item_status enum
SELECT enumlabel 
FROM pg_enum 
WHERE enumtypid = 'plan_item_status'::regtype 
ORDER BY enumsortorder;

-- 4. Check sequence_number added
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'template_phase_services'
  AND column_name = 'sequence_number';

-- 5. Check estimated_duration_days added
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'patient_plan_phases'
  AND column_name = 'estimated_duration_days';

-- ============================================
-- ROLLBACK SCRIPT (Use with caution!)
-- ============================================
/*
-- Rollback V19 (if something goes wrong)

-- 1. Drop new columns
ALTER TABLE patient_treatment_plans 
    DROP COLUMN IF EXISTS approval_status,
    DROP COLUMN IF EXISTS patient_consent_date,
    DROP COLUMN IF EXISTS approved_by,
    DROP COLUMN IF EXISTS approved_at,
    DROP COLUMN IF EXISTS rejection_reason;

-- 2. Drop approval_status type
DROP TYPE IF EXISTS approval_status CASCADE;

-- 3. Drop sequence_number
ALTER TABLE template_phase_services DROP COLUMN IF EXISTS sequence_number;

-- 4. Drop estimated_duration_days
ALTER TABLE patient_plan_phases DROP COLUMN IF EXISTS estimated_duration_days;

-- 5. Revert plan_item_status enum (manual step - complex!)
-- ...

-- 6. Drop indexes
DROP INDEX IF EXISTS idx_plans_approval_status;
DROP INDEX IF EXISTS idx_plans_awaiting_approval;

*/

-- ============================================
-- END OF V19 MIGRATION
-- ============================================
