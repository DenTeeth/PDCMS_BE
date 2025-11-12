-- ============================================
-- V20 SEED DATA: Additional Treatment Plans for API 5.5 Testing
-- ============================================
-- Purpose: Add more treatment plans with various statuses and approval states
-- for testing GET /patient-treatment-plans with RBAC filters
--
-- Coverage:
-- - Multiple patients (BN-1001, BN-1002, BN-1003, BN-1004, BN-1005)
-- - Multiple doctors (EMP-1, EMP-2, EMP-3)
-- - Various statuses: PENDING, IN_PROGRESS, ACTIVE, COMPLETED, ON_HOLD
-- - Various approval statuses: DRAFT, APPROVED
-- - Date ranges: Last 6 months to future
--
-- Version: V20
-- Date: 2025-01-12
-- ============================================

-- ============================================
-- Treatment Plan 4: BN-1003 - Nhổ răng khôn + Tẩy trắng (Doctor EMP-2, PENDING, DRAFT)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    created_at
) VALUES (
    4, 'PLAN-20250110-001', 'Nhổ răng khôn và Tẩy trắng', 3, 2,
    'PENDING', 'DRAFT', '2025-01-20', '2025-02-20',
    8500000, 500000, 8000000, 'FULL',
    '2025-01-10 10:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

-- Phase 1: Nhổ răng khôn
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    7, 4, 1, 'Giai đoạn 1: Nhổ răng khôn',
    'PENDING', '2025-01-20', 7, '2025-01-10 10:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, created_at
) VALUES
    (19, 7, 14, 1, 'Nhổ răng khôn hàm dưới bên trái', 'PENDING', 60, 2500000, '2025-01-10 10:00:00'),
    (20, 7, 14, 2, 'Nhổ răng khôn hàm dưới bên phải', 'PENDING', 60, 2500000, '2025-01-10 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- Phase 2: Tẩy trắng
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    8, 4, 2, 'Giai đoạn 2: Tẩy trắng răng',
    'PENDING', '2025-02-05', 14, '2025-01-10 10:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, created_at
) VALUES
    (21, 8, 31, 1, 'Tẩy trắng răng Laser', 'PENDING', 90, 3500000, '2025-01-10 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 5: BN-1004 - Bọc răng sứ 6 răng (Doctor EMP-1, IN_PROGRESS, APPROVED)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    approved_by, approved_at, created_at
) VALUES (
    5, 'PLAN-20241215-001', 'Bọc răng sứ thẩm mỹ 6 răng cửa', 4, 1,
    'IN_PROGRESS', 'APPROVED', '2024-12-15', '2025-02-15',
    42000000, 2000000, 40000000, 'INSTALLMENT',
    3, '2024-12-16 09:00:00', '2024-12-15 14:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

-- Phase 1: Khám và chuẩn bị (COMPLETED)
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, completion_date, estimated_duration_days, created_at
) VALUES (
    9, 5, 1, 'Giai đoạn 1: Khám và chuẩn bị',
    'COMPLETED', '2024-12-15', '2024-12-20', 5, '2024-12-15 14:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (22, 9, 1, 1, 'Khám tổng quát và tư vấn', 'COMPLETED', 30, 500000, '2024-12-15 15:00:00', '2024-12-15 14:00:00'),
    (23, 9, 3, 2, 'Vệ sinh răng miệng', 'COMPLETED', 45, 800000, '2024-12-17 10:00:00', '2024-12-15 14:00:00'),
    (24, 9, 7, 3, 'Mài răng chuẩn bị bọc sứ', 'COMPLETED', 120, 3000000, '2024-12-19 14:00:00', '2024-12-15 14:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- Phase 2: Bọc răng sứ (IN_PROGRESS)
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    10, 5, 2, 'Giai đoạn 2: Lắp răng sứ',
    'IN_PROGRESS', '2025-01-05', 30, '2024-12-15 14:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (25, 10, 22, 1, 'Bọc răng sứ Titan răng 11', 'COMPLETED', 60, 6000000, '2025-01-05 10:00:00', '2024-12-15 14:00:00'),
    (26, 10, 22, 2, 'Bọc răng sứ Titan răng 12', 'COMPLETED', 60, 6000000, '2025-01-05 11:00:00', '2024-12-15 14:00:00'),
    (27, 10, 22, 3, 'Bọc răng sứ Titan răng 21', 'COMPLETED', 60, 6000000, '2025-01-06 10:00:00', '2024-12-15 14:00:00'),
    (28, 10, 22, 4, 'Bọc răng sứ Titan răng 22', 'READY_FOR_BOOKING', 60, 6000000, NULL, '2024-12-15 14:00:00'),
    (29, 10, 22, 5, 'Bọc răng sứ Titan răng 13', 'READY_FOR_BOOKING', 60, 6000000, NULL, '2024-12-15 14:00:00'),
    (30, 10, 22, 6, 'Bọc răng sứ Titan răng 23', 'READY_FOR_BOOKING', 60, 6000000, NULL, '2024-12-15 14:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 6: BN-1005 - Trồng răng Implant (Doctor EMP-3, COMPLETED, APPROVED)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    approved_by, approved_at, created_at
) VALUES (
    6, 'PLAN-20240815-001', 'Trồng răng Implant răng hàm', 5, 3,
    'COMPLETED', 'APPROVED', '2024-08-15', '2024-12-20',
    25000000, 1000000, 24000000, 'FULL',
    7, '2024-08-16 09:00:00', '2024-08-15 10:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

-- All phases completed
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, completion_date, estimated_duration_days, created_at
) VALUES (
    11, 6, 1, 'Giai đoạn 1: Khám và Chụp CT', 'COMPLETED', '2024-08-15', '2024-08-20', 5, '2024-08-15 10:00:00'),
    (12, 6, 2, 'Giai đoạn 2: Cấy trụ Implant', 'COMPLETED', '2024-09-01', '2024-09-10', 10, '2024-08-15 10:00:00'),
    (13, 6, 3, 'Giai đoạn 3: Lắp mão sứ', 'COMPLETED', '2024-12-10', '2024-12-20', 10, '2024-08-15 10:00:00')
ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    -- Phase 1
    (31, 11, 1, 1, 'Khám và chụp CT 3D', 'COMPLETED', 45, 1500000, '2024-08-15 11:00:00', '2024-08-15 10:00:00'),
    (32, 11, 3, 2, 'Vệ sinh răng miệng', 'COMPLETED', 30, 800000, '2024-08-17 10:00:00', '2024-08-15 10:00:00'),
    -- Phase 2
    (33, 12, 29, 1, 'Cấy trụ Implant răng 36', 'COMPLETED', 120, 18000000, '2024-09-01 14:00:00', '2024-08-15 10:00:00'),
    -- Phase 3
    (34, 13, 22, 1, 'Lắp mão sứ Titan răng 36', 'COMPLETED', 60, 6000000, '2024-12-15 10:00:00', '2024-08-15 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 7: BN-1001 - Điều trị nướu răng (Doctor EMP-2, PENDING, DRAFT)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    created_at
) VALUES (
    7, 'PLAN-20250108-001', 'Điều trị viêm nướu và chăm sóc nha chu', 1, 2,
    'PENDING', 'DRAFT', '2025-01-15', '2025-03-15',
    5500000, 0, 5500000, 'FULL',
    '2025-01-08 11:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    14, 7, 1, 'Giai đoạn 1: Vệ sinh và điều trị nướu',
    'PENDING', '2025-01-15', 60, '2025-01-08 11:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, created_at
) VALUES
    (35, 14, 3, 1, 'Vệ sinh răng miệng sâu', 'PENDING', 60, 1200000, '2025-01-08 11:00:00'),
    (36, 14, 4, 2, 'Điều trị viêm nướu (Lần 1)', 'PENDING', 45, 1500000, '2025-01-08 11:00:00'),
    (37, 14, 4, 3, 'Điều trị viêm nướu (Lần 2)', 'PENDING', 45, 1500000, '2025-01-08 11:00:00'),
    (38, 14, 4, 4, 'Kiểm tra và tái khám', 'PENDING', 30, 800000, '2025-01-08 11:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 8: BN-1002 - Niềng răng Invisalign (Doctor EMP-1, IN_PROGRESS, APPROVED)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    approved_by, approved_at, created_at
) VALUES (
    8, 'PLAN-20241101-001', 'Niềng răng trong suốt Invisalign', 2, 1,
    'IN_PROGRESS', 'APPROVED', '2024-11-01', '2025-11-01',
    85000000, 5000000, 80000000, 'INSTALLMENT',
    7, '2024-11-02 09:00:00', '2024-11-01 10:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

-- Phase 1: Chuẩn bị (COMPLETED)
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, completion_date, estimated_duration_days, created_at
) VALUES (
    15, 8, 1, 'Giai đoạn 1: Khám và lập kế hoạch',
    'COMPLETED', '2024-11-01', '2024-11-10', 10, '2024-11-01 10:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (39, 15, 1, 1, 'Khám tổng quát và chụp CT 3D', 'COMPLETED', 45, 2000000, '2024-11-01 11:00:00', '2024-11-01 10:00:00'),
    (40, 15, 3, 2, 'Vệ sinh răng miệng', 'COMPLETED', 45, 800000, '2024-11-05 10:00:00', '2024-11-01 10:00:00'),
    (41, 15, 40, 3, 'Thiết kế khay Invisalign', 'COMPLETED', 60, 10000000, '2024-11-08 14:00:00', '2024-11-01 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- Phase 2: Điều chỉnh (IN_PROGRESS)
INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    16, 8, 2, 'Giai đoạn 2: Đeo khay và điều chỉnh (12 tháng)',
    'IN_PROGRESS', '2024-11-15', 365, '2024-11-01 10:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (42, 16, 40, 1, 'Bộ khay số 1-5', 'COMPLETED', 30, 15000000, '2024-11-15 10:00:00', '2024-11-01 10:00:00'),
    (43, 16, 40, 2, 'Bộ khay số 6-10', 'COMPLETED', 30, 15000000, '2024-12-15 10:00:00', '2024-11-01 10:00:00'),
    (44, 16, 40, 3, 'Bộ khay số 11-15', 'READY_FOR_BOOKING', 30, 15000000, NULL, '2024-11-01 10:00:00'),
    (45, 16, 40, 4, 'Bộ khay số 16-20', 'READY_FOR_BOOKING', 30, 15000000, NULL, '2024-11-01 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 9: BN-1003 - Hàn răng sâu (Doctor EMP-1, COMPLETED, APPROVED)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    approved_by, approved_at, created_at
) VALUES (
    9, 'PLAN-20240920-001', 'Hàn răng sâu và điều trị tủy', 3, 1,
    'COMPLETED', 'APPROVED', '2024-09-20', '2024-10-05',
    7500000, 500000, 7000000, 'FULL',
    3, '2024-09-21 09:00:00', '2024-09-20 14:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, completion_date, estimated_duration_days, created_at
) VALUES (
    17, 9, 1, 'Giai đoạn 1: Điều trị và hàn răng',
    'COMPLETED', '2024-09-20', '2024-10-05', 15, '2024-09-20 14:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (46, 17, 1, 1, 'Khám và chụp X-quang', 'COMPLETED', 30, 500000, '2024-09-20 15:00:00', '2024-09-20 14:00:00'),
    (47, 17, 8, 2, 'Điều trị tủy răng 16', 'COMPLETED', 90, 3500000, '2024-09-25 10:00:00', '2024-09-20 14:00:00'),
    (48, 17, 7, 3, 'Hàn răng composite 16', 'COMPLETED', 60, 1500000, '2024-09-30 14:00:00', '2024-09-20 14:00:00'),
    (49, 17, 7, 4, 'Hàn răng composite 26', 'COMPLETED', 60, 1500000, '2024-10-02 10:00:00', '2024-09-20 14:00:00'),
    (50, 17, 1, 5, 'Tái khám sau điều trị', 'COMPLETED', 30, 500000, '2024-10-05 11:00:00', '2024-09-20 14:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Treatment Plan 10: BN-1004 - Cạo vôi răng định kỳ (Doctor EMP-2, IN_PROGRESS, APPROVED)
-- ============================================
INSERT INTO patient_treatment_plans (
    plan_id, plan_code, plan_name, patient_id, created_by,
    status, approval_status, start_date, expected_end_date,
    total_price, discount_amount, final_cost, payment_type,
    approved_by, approved_at, created_at
) VALUES (
    10, 'PLAN-20250105-001', 'Vệ sinh răng miệng và chăm sóc định kỳ', 4, 2,
    'IN_PROGRESS', 'APPROVED', '2025-01-05', '2025-07-05',
    3600000, 0, 3600000, 'FULL',
    7, '2025-01-06 09:00:00', '2025-01-05 10:00:00'
) ON CONFLICT (plan_id) DO NOTHING;

INSERT INTO patient_plan_phases (
    patient_phase_id, plan_id, phase_number, phase_name,
    status, start_date, estimated_duration_days, created_at
) VALUES (
    18, 10, 1, 'Giai đoạn 1: Vệ sinh 6 tháng',
    'IN_PROGRESS', '2025-01-05', 180, '2025-01-05 10:00:00'
) ON CONFLICT (patient_phase_id) DO NOTHING;

INSERT INTO patient_plan_items (
    item_id, phase_id, service_id, sequence_number, item_name,
    status, estimated_time_minutes, price, completed_at, created_at
) VALUES
    (51, 18, 3, 1, 'Cạo vôi răng lần 1', 'COMPLETED', 45, 800000, '2025-01-05 11:00:00', '2025-01-05 10:00:00'),
    (52, 18, 1, 2, 'Khám tổng quát lần 1', 'COMPLETED', 30, 500000, '2025-01-05 12:00:00', '2025-01-05 10:00:00'),
    (53, 18, 3, 3, 'Cạo vôi răng lần 2 (sau 3 tháng)', 'READY_FOR_BOOKING', 45, 800000, NULL, '2025-01-05 10:00:00'),
    (54, 18, 1, 4, 'Khám tổng quát lần 2', 'READY_FOR_BOOKING', 30, 500000, NULL, '2025-01-05 10:00:00'),
    (55, 18, 3, 5, 'Cạo vôi răng lần 3 (sau 6 tháng)', 'READY_FOR_BOOKING', 45, 800000, NULL, '2025-01-05 10:00:00'),
    (56, 18, 1, 6, 'Khám tổng quát lần 3', 'READY_FOR_BOOKING', 30, 500000, NULL, '2025-01-05 10:00:00')
ON CONFLICT (item_id) DO NOTHING;

-- ============================================
-- Reset Sequences (Important!)
-- ============================================
-- Update sequences to next available values to avoid conflicts with future INSERTs

SELECT setval('patient_treatment_plans_plan_id_seq', (SELECT MAX(plan_id) FROM patient_treatment_plans));
SELECT setval('patient_plan_phases_patient_phase_id_seq', (SELECT MAX(patient_phase_id) FROM patient_plan_phases));
SELECT setval('patient_plan_items_item_id_seq', (SELECT MAX(item_id) FROM patient_plan_items));

-- ============================================
-- END V20 SEED DATA
-- ============================================

-- ============================================
-- SUMMARY STATISTICS (for verification)
-- ============================================
-- Total Treatment Plans: 10 (3 existing + 7 new)
-- 
-- By Status:
-- - PENDING: 2 plans (Plan 4, Plan 7)
-- - IN_PROGRESS: 4 plans (Plan 1, Plan 5, Plan 8, Plan 10)
-- - COMPLETED: 3 plans (Plan 2, Plan 6, Plan 9)
-- - ON_HOLD: 1 plan (Plan 3 - from existing data)
--
-- By Approval Status:
-- - DRAFT: 2 plans (Plan 4, Plan 7)
-- - APPROVED: 7 plans (Plan 1, 2, 5, 6, 8, 9, 10)
--
-- By Doctor:
-- - EMP-1 (Doctor 1): 5 plans (Plan 1, 5, 8, 9)
-- - EMP-2 (Doctor 2): 4 plans (Plan 2, 4, 7, 10)
-- - EMP-3 (Doctor 3): 1 plan (Plan 6)
--
-- By Patient:
-- - BN-1001: 3 plans (Plan 1, 7, and orthodontics)
-- - BN-1002: 3 plans (Plan 2, 8, and implant)
-- - BN-1003: 3 plans (Plan 3, 4, 9)
-- - BN-1004: 2 plans (Plan 5, 10)
-- - BN-1005: 1 plan (Plan 6)
--
-- Date Range Coverage:
-- - 2024-05-15 to 2024-12-20 (historical)
-- - 2025-01-05 to 2025-11-01 (current/future)
-- ============================================
