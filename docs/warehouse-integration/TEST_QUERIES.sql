-- =============================================
-- WAREHOUSE INTEGRATION - QUICK TEST SCRIPTS
-- =============================================
-- Purpose: SQL scripts để test và verify warehouse integration
-- Date: 2025-12-26
-- =============================================

-- =============================================
-- 1. KIỂM TRA DỮ LIỆU CƠ BẢN
-- =============================================

-- 1.1. Xem tất cả services có BOM
SELECT 
  s.service_id,
  s.service_code,
  s.service_name,
  COUNT(sc.link_id) as material_count
FROM services s
LEFT JOIN service_consumables sc ON s.service_id = sc.service_id
WHERE sc.link_id IS NOT NULL
GROUP BY s.service_id, s.service_code, s.service_name
ORDER BY s.service_id;

-- Expected: Ít nhất 3-4 services có material_count > 0

-- =============================================

-- 1.2. Xem chi tiết BOM của service "Trám răng Composite"
SELECT 
  s.service_code,
  s.service_name,
  im.item_code,
  im.item_name,
  sc.quantity_per_service,
  u.unit_name,
  sc.notes
FROM service_consumables sc
JOIN services s ON sc.service_id = s.service_id
JOIN item_masters im ON sc.item_master_id = im.item_master_id
JOIN item_units u ON sc.unit_id = u.unit_id
WHERE s.service_code = 'FILLING_COMP'
ORDER BY im.item_code;

-- Expected: 6 rows (găng tay, khẩu trang, gạc, composite, etching, bonding)

-- =============================================

-- 1.3. Xem tồn kho vật tư (theo lô hàng + FEFO order)
SELECT 
  im.item_code,
  im.item_name,
  ib.lot_number,
  ib.quantity_on_hand,
  ib.expiry_date,
  CASE 
    WHEN ib.expiry_date IS NULL THEN 'NO_EXPIRY'
    WHEN ib.expiry_date < CURRENT_DATE THEN '❌ EXPIRED'
    WHEN ib.expiry_date < CURRENT_DATE + INTERVAL '30 days' THEN '⚠️ EXPIRING_SOON'
    ELSE '✅ OK'
  END as expiry_status,
  ib.bin_location
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
WHERE im.item_code IN ('CON-GLOVE-01', 'MAT-COMP-01', 'CON-MASK-01', 'CON-GAUZE-01')
ORDER BY 
  im.item_code,
  CASE WHEN ib.expiry_date IS NULL THEN 1 ELSE 0 END,
  ib.expiry_date ASC NULLS LAST;

-- Expected: Mỗi item có ít nhất 1 batch, FEFO order (expiry sớm nhất trước)

-- =============================================

-- 1.4. Xem users có permissions cần thiết
SELECT 
  u.username,
  r.role_name,
  STRING_AGG(p.permission_name, ', ' ORDER BY p.permission_name) as permissions
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
LEFT JOIN role_permissions rp ON r.role_id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.permission_id
WHERE p.permission_name IN ('VIEW_CLINICAL_RECORD', 'WRITE_CLINICAL_RECORD', 'VIEW_WAREHOUSE_COST')
GROUP BY u.user_id, u.username, r.role_name
ORDER BY u.username;

-- Expected: admin có tất cả 3 permissions, doctor/nurse có VIEW+WRITE, accountant có VIEW+VIEW_COST

-- =============================================
-- 2. TEST PROCEDURES (Sau khi complete appointment)
-- =============================================

-- 2.1. Xem procedures đã deduct materials
SELECT 
  p.procedure_id,
  p.tooth_number,
  s.service_name,
  p.materials_deducted_at,
  p.materials_deducted_by,
  p.storage_transaction_id,
  COUNT(pmu.usage_id) as material_count
FROM clinical_record_procedures p
JOIN services s ON p.service_id = s.service_id
LEFT JOIN procedure_material_usage pmu ON p.procedure_id = pmu.procedure_id
WHERE p.materials_deducted_at IS NOT NULL
GROUP BY p.procedure_id, s.service_name, p.materials_deducted_at, p.materials_deducted_by, p.storage_transaction_id
ORDER BY p.materials_deducted_at DESC
LIMIT 10;

-- Expected: Nếu đã test, sẽ có procedures với materials_deducted_at != NULL

-- =============================================

-- 2.2. Xem chi tiết vật tư đã dùng cho 1 procedure
-- (Thay {PROCEDURE_ID} bằng procedure_id từ query 2.1)
SELECT 
  pmu.usage_id,
  im.item_code,
  im.item_name,
  pmu.planned_quantity,
  pmu.actual_quantity,
  pmu.variance_quantity,
  pmu.variance_reason,
  u.unit_name,
  pmu.recorded_at,
  pmu.recorded_by,
  pmu.notes
FROM procedure_material_usage pmu
JOIN item_masters im ON pmu.item_master_id = im.item_master_id
JOIN item_units u ON pmu.unit_id = u.unit_id
WHERE pmu.procedure_id = {PROCEDURE_ID}  -- Thay bằng ID thực tế
ORDER BY im.item_code;

-- Expected: Danh sách vật tư với planned & actual quantities

-- =============================================

-- 2.3. So sánh tồn kho trước/sau deduction
-- (Dùng để verify FEFO đúng không)
SELECT 
  im.item_code,
  im.item_name,
  ib.lot_number,
  ib.initial_quantity as initial,
  ib.quantity_on_hand as current,
  (ib.initial_quantity - ib.quantity_on_hand) as used,
  ib.expiry_date
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
WHERE im.item_code IN ('CON-GLOVE-01', 'MAT-COMP-01')
  AND (ib.initial_quantity - ib.quantity_on_hand) > 0  -- Chỉ xem batches đã dùng
ORDER BY im.item_code, ib.expiry_date;

-- Expected: Batches hết hạn sớm nhất bị trừ trước

-- =============================================
-- 3. RESET DATA (Để test lại từ đầu)
-- =============================================

-- ⚠️ CẢNH BÁO: Chỉ chạy trong môi trường TEST/DEV!

-- 3.1. Reset tồn kho về trạng thái ban đầu
UPDATE item_batches 
SET quantity_on_hand = initial_quantity
WHERE item_master_id IN (
  SELECT item_master_id FROM item_masters 
  WHERE item_code IN ('CON-GLOVE-01', 'MAT-COMP-01', 'CON-MASK-01', 'CON-GAUZE-01', 'MAT-BOND-01')
);

-- =============================================

-- 3.2. Xóa usage records của procedures test
DELETE FROM procedure_material_usage
WHERE procedure_id IN (
  SELECT procedure_id FROM clinical_record_procedures
  WHERE materials_deducted_at > CURRENT_DATE - INTERVAL '7 days'
);

-- =============================================

-- 3.3. Reset procedures về trạng thái chưa deduct
UPDATE clinical_record_procedures
SET 
  materials_deducted_at = NULL,
  materials_deducted_by = NULL,
  storage_transaction_id = NULL
WHERE materials_deducted_at > CURRENT_DATE - INTERVAL '7 days';

-- =============================================

-- 3.4. Reset appointments về SCHEDULED
UPDATE appointments
SET status = 'SCHEDULED'
WHERE appointment_id IN (
  SELECT a.appointment_id FROM appointments a
  JOIN clinical_records cr ON cr.appointment_id = a.appointment_id
  JOIN clinical_record_procedures p ON p.clinical_record_id = cr.clinical_record_id
  WHERE p.materials_deducted_at > CURRENT_DATE - INTERVAL '7 days'
);

-- =============================================
-- 4. THÊM DỮ LIỆU TEST MỚI
-- =============================================

-- 4.1. Thêm batch mới cho Etching Gel (nếu chưa có)
INSERT INTO item_batches (
  item_master_id, 
  lot_number, 
  quantity_on_hand, 
  initial_quantity, 
  expiry_date, 
  supplier_id, 
  imported_at, 
  bin_location, 
  created_at
)
SELECT 
  im.item_master_id,
  'BATCH-ETCH-2024-001',
  500,
  500,
  CURRENT_DATE + INTERVAL '150 days',
  3,
  NOW() - INTERVAL '10 days',
  'Kệ C-03',
  NOW()
FROM item_masters im 
WHERE im.item_code = 'MAT-ETCH-01'
ON CONFLICT DO NOTHING;

-- =============================================

-- 4.2. Thêm BOM mới cho service (ví dụ: Root Canal)
-- (Uncomment nếu cần)
/*
INSERT INTO service_consumables (
  service_id, 
  item_master_id, 
  quantity_per_service, 
  unit_id, 
  notes
)
SELECT 
  s.service_id,
  im.item_master_id,
  2.0,
  u.unit_id,
  'Gây tê cho điều trị tủy'
FROM services s
CROSS JOIN item_masters im
CROSS JOIN item_units u
WHERE s.service_code = 'ROOT_CANAL'
  AND im.item_code = 'MED-SEPT-01'
  AND u.item_master_id = im.item_master_id
  AND u.unit_name = 'Ống'
ON CONFLICT (service_id, item_master_id) DO NOTHING;
*/

-- =============================================
-- 5. VERIFY FEFO ALGORITHM
-- =============================================

-- 5.1. Xem thứ tự FEFO cho 1 item (giống như backend query)
-- (Thay {ITEM_CODE} bằng item code thực tế)
SELECT 
  ib.batch_id,
  ib.lot_number,
  ib.quantity_on_hand,
  ib.expiry_date,
  CASE 
    WHEN ib.expiry_date IS NULL THEN 'NEVER_EXPIRES'
    ELSE TO_CHAR(ib.expiry_date, 'YYYY-MM-DD')
  END as expiry_formatted
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
WHERE im.item_code = '{ITEM_CODE}'  -- Ví dụ: 'CON-GLOVE-01'
  AND ib.quantity_on_hand > 0
ORDER BY 
  CASE WHEN ib.expiry_date IS NULL THEN 1 ELSE 0 END,
  ib.expiry_date ASC NULLS LAST;

-- Expected: Batch hết hạn sớm nhất ở top

-- =============================================

-- 5.2. Simulate FEFO deduction (xem batch nào sẽ bị trừ)
-- (Ví dụ: Cần 3 đôi găng tay)
WITH fefo_order AS (
  SELECT 
    ib.batch_id,
    ib.lot_number,
    ib.quantity_on_hand,
    ib.expiry_date,
    ROW_NUMBER() OVER (
      ORDER BY 
        CASE WHEN ib.expiry_date IS NULL THEN 1 ELSE 0 END,
        ib.expiry_date ASC NULLS LAST
    ) as fefo_rank
  FROM item_batches ib
  JOIN item_masters im ON ib.item_master_id = im.item_master_id
  WHERE im.item_code = 'CON-GLOVE-01'
    AND ib.quantity_on_hand > 0
),
deduction_simulation AS (
  SELECT 
    lot_number,
    quantity_on_hand,
    expiry_date,
    CASE 
      WHEN fefo_rank = 1 THEN LEAST(quantity_on_hand, 3)
      WHEN fefo_rank = 2 THEN LEAST(quantity_on_hand, GREATEST(0, 3 - (
        SELECT SUM(quantity_on_hand) FROM fefo_order WHERE fefo_rank < 2
      )))
      ELSE 0
    END as will_deduct
  FROM fefo_order
)
SELECT 
  lot_number,
  quantity_on_hand as before,
  will_deduct,
  (quantity_on_hand - will_deduct) as after,
  expiry_date
FROM deduction_simulation
WHERE will_deduct > 0
ORDER BY expiry_date NULLS LAST;

-- Expected: Batch hết hạn sớm nhất bị trừ trước

-- =============================================
-- 6. TROUBLESHOOTING QUERIES
-- =============================================

-- 6.1. Tìm procedures không deduct được (có lỗi)
SELECT 
  p.procedure_id,
  s.service_name,
  p.created_at,
  a.status as appointment_status,
  p.materials_deducted_at
FROM clinical_record_procedures p
JOIN services s ON p.service_id = s.service_id
JOIN clinical_records cr ON p.clinical_record_id = cr.clinical_record_id
JOIN appointments a ON cr.appointment_id = a.appointment_id
WHERE a.status = 'COMPLETED'
  AND p.materials_deducted_at IS NULL
  AND p.created_at > CURRENT_DATE - INTERVAL '7 days'
ORDER BY p.created_at DESC;

-- Expected: Nếu có rows → Có procedures không deduct được, cần check logs

-- =============================================

-- 6.2. Tìm items thiếu BOM
SELECT DISTINCT
  s.service_id,
  s.service_code,
  s.service_name
FROM services s
LEFT JOIN service_consumables sc ON s.service_id = sc.service_id
WHERE sc.link_id IS NULL
  AND s.is_active = true
ORDER BY s.service_code;

-- Expected: List các services chưa có BOM

-- =============================================

-- 6.3. Tìm items sắp hết hàng
SELECT 
  im.item_code,
  im.item_name,
  SUM(ib.quantity_on_hand) as total_stock,
  im.min_stock_level,
  CASE 
    WHEN SUM(ib.quantity_on_hand) = 0 THEN '❌ OUT_OF_STOCK'
    WHEN SUM(ib.quantity_on_hand) <= im.min_stock_level THEN '⚠️ LOW_STOCK'
    ELSE '✅ OK'
  END as stock_status
FROM item_masters im
LEFT JOIN item_batches ib ON im.item_master_id = ib.item_master_id
WHERE im.is_active = true
GROUP BY im.item_master_id, im.item_code, im.item_name, im.min_stock_level
HAVING SUM(ib.quantity_on_hand) <= im.min_stock_level
ORDER BY total_stock ASC;

-- Expected: List items cần nhập hàng

-- =============================================

-- 6.4. Tìm batches sắp hết hạn (trong 30 ngày)
SELECT 
  im.item_code,
  im.item_name,
  ib.lot_number,
  ib.quantity_on_hand,
  ib.expiry_date,
  (ib.expiry_date - CURRENT_DATE) as days_to_expiry
FROM item_batches ib
JOIN item_masters im ON ib.item_master_id = im.item_master_id
WHERE ib.expiry_date IS NOT NULL
  AND ib.expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'
  AND ib.quantity_on_hand > 0
ORDER BY ib.expiry_date ASC;

-- Expected: List batches cần dùng/thanh lý gấp

-- =============================================
-- 7. REPORTS (Báo cáo)
-- =============================================

-- 7.1. Báo cáo tổng hợp vật tư đã dùng (7 ngày qua)
SELECT 
  im.item_code,
  im.item_name,
  COUNT(DISTINCT pmu.procedure_id) as procedure_count,
  SUM(pmu.actual_quantity) as total_used,
  u.unit_name,
  SUM(pmu.variance_quantity) as total_variance
FROM procedure_material_usage pmu
JOIN item_masters im ON pmu.item_master_id = im.item_master_id
JOIN item_units u ON pmu.unit_id = u.unit_id
WHERE pmu.recorded_at > CURRENT_DATE - INTERVAL '7 days'
GROUP BY im.item_master_id, im.item_code, im.item_name, u.unit_name
ORDER BY total_used DESC;

-- Expected: Top vật tư sử dụng nhiều nhất

-- =============================================

-- 7.2. Báo cáo variance (chênh lệch) cao
SELECT 
  p.procedure_id,
  s.service_name,
  im.item_name,
  pmu.planned_quantity,
  pmu.actual_quantity,
  pmu.variance_quantity,
  pmu.variance_reason,
  ABS(pmu.variance_quantity) as abs_variance
FROM procedure_material_usage pmu
JOIN clinical_record_procedures p ON pmu.procedure_id = p.procedure_id
JOIN services s ON p.service_id = s.service_id
JOIN item_masters im ON pmu.item_master_id = im.item_master_id
WHERE pmu.recorded_at > CURRENT_DATE - INTERVAL '30 days'
  AND pmu.variance_quantity != 0
ORDER BY abs_variance DESC
LIMIT 20;

-- Expected: Các trường hợp dùng vật tư khác nhiều so với dự kiến

-- =============================================
-- END OF SCRIPTS
-- =============================================
-- 
-- NOTES:
-- - Thay {PROCEDURE_ID}, {ITEM_CODE} bằng giá trị thực tế khi chạy
-- - Scripts reset (section 3) chỉ dùng trong DEV/TEST
-- - Luôn backup database trước khi chạy scripts xóa/update
--
-- Last updated: 2025-12-26
-- =============================================
