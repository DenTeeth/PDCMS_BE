#!/bin/bash

# Script to reset database and re-seed data
# Usage: ./reset-and-seed.sh

echo "==================================="
echo "RESET DATABASE AND RE-SEED DATA"
echo "==================================="

# Database connection settings
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="dental_clinic_db"
DB_USER="root"
DB_PASSWORD="123456"

echo ""
echo "Step 1: Resetting database tables..."

# Drop and recreate tables
docker exec -i postgres-dental psql -U $DB_USER -d $DB_NAME << EOF
-- Drop tables in correct order
DROP TABLE IF EXISTS appointment_plan_items CASCADE;
DROP TABLE IF EXISTS patient_plan_items CASCADE;
DROP TABLE IF EXISTS patient_plan_phases CASCADE;
DROP TABLE IF EXISTS patient_treatment_plans CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS service_categories CASCADE;

-- Recreate service_categories table
CREATE TABLE IF NOT EXISTS service_categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Recreate services table with proper structure
CREATE TABLE IF NOT EXISTS services (
    service_id SERIAL PRIMARY KEY,
    service_code VARCHAR(50) NOT NULL UNIQUE,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    default_duration_minutes INTEGER,
    default_buffer_minutes INTEGER,
    price NUMERIC(10,2),
    specialization_id INTEGER NOT NULL,
    category_id BIGINT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES service_categories(category_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_services_category ON services(category_id);
CREATE INDEX IF NOT EXISTS idx_services_active ON services(is_active);
CREATE INDEX IF NOT EXISTS idx_services_specialization ON services(specialization_id);

EOF

echo "✅ Tables reset successfully!"
echo ""
echo "Step 2: Inserting seed data..."

# Insert service categories
docker exec -i postgres-dental psql -U $DB_USER -d $DB_NAME << EOF
-- Insert service categories
INSERT INTO service_categories (category_code, category_name, display_order, is_active, created_at) VALUES
('A_GENERAL', 'A. Nha khoa tổng quát', 1, true, NOW()),
('B_COSMETIC', 'B. Thẩm mỹ & Phục hình', 2, true, NOW()),
('C_IMPLANT', 'C. Cắm ghép Implant', 3, true, NOW()),
('D_ORTHO', 'D. Chỉnh nha', 4, true, NOW()),
('E_PROS_DENTURE', 'E. Phục hình Tháo lắp', 5, true, NOW()),
('F_OTHER', 'F. Dịch vụ khác', 6, true, NOW())
ON CONFLICT (category_code) DO NOTHING;

-- Insert services with category mapping
-- A. Nha khoa tổng quát
INSERT INTO services (service_code, service_name, description, default_duration_minutes, default_buffer_minutes, price, specialization_id, category_id, display_order, is_active, created_at)
SELECT
    vals.service_code,
    vals.service_name,
    vals.description,
    vals.default_duration_minutes,
    vals.default_buffer_minutes,
    vals.price,
    vals.specialization_id,
    sc.category_id,
    vals.display_order,
    vals.is_active,
    NOW()
FROM (VALUES
    -- A. Nha khoa tổng quát (display_order within category)
    ('GEN_EXAM', 'Khám tổng quát & Tư vấn', 'Khám tổng quát, chụp X-quang phim nhỏ nếu cần thiết để chẩn đoán.', 30, 15, 100000, 8, 'A_GENERAL', 1, true),
    ('GEN_XRAY_PERI', 'Chụp X-Quang quanh chóp', 'Chụp phim X-quang nhỏ tại ghế.', 10, 5, 50000, 8, 'A_GENERAL', 2, true),
    ('SCALING_L1', 'Cạo vôi răng & Đánh bóng - Mức 1', 'Làm sạch vôi răng và mảng bám mức độ ít/trung bình.', 45, 15, 300000, 3, 'A_GENERAL', 3, true),
    ('SCALING_L2', 'Cạo vôi răng & Đánh bóng - Mức 2', 'Làm sạch vôi răng và mảng bám mức độ nhiều.', 60, 15, 400000, 3, 'A_GENERAL', 4, true),
    ('SCALING_VIP', 'Cạo vôi VIP không đau', 'Sử dụng máy rung siêu âm ít ê buốt.', 60, 15, 500000, 3, 'A_GENERAL', 5, true),
    ('FILLING_COMP', 'Trám răng Composite', 'Trám răng sâu, mẻ bằng vật liệu composite thẩm mỹ.', 45, 15, 400000, 2, 'A_GENERAL', 6, true),
    ('FILLING_GAP', 'Đắp kẽ răng thưa Composite', 'Đóng kẽ răng thưa nhỏ bằng composite.', 60, 15, 500000, 7, 'A_GENERAL', 7, true),
    ('EXTRACT_MILK', 'Nhổ răng sữa', 'Nhổ răng sữa cho trẻ em.', 15, 15, 50000, 6, 'A_GENERAL', 8, true),
    ('EXTRACT_NORM', 'Nhổ răng thường', 'Nhổ răng vĩnh viễn đơn giản (không phải răng khôn).', 45, 15, 500000, 5, 'A_GENERAL', 9, true),
    ('EXTRACT_WISDOM_L1', 'Nhổ răng khôn mức 1 (Dễ)', 'Tiểu phẫu nhổ răng khôn mọc thẳng, ít phức tạp.', 60, 30, 1500000, 5, 'A_GENERAL', 10, true),
    ('EXTRACT_WISDOM_L2', 'Nhổ răng khôn mức 2 (Khó)', 'Tiểu phẫu nhổ răng khôn mọc lệch, ngầm.', 90, 30, 2500000, 5, 'A_GENERAL', 11, true),
    ('ENDO_TREAT_ANT', 'Điều trị tủy răng trước', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng cửa/răng nanh.', 60, 15, 1500000, 2, 'A_GENERAL', 12, true),
    ('ENDO_TREAT_POST', 'Điều trị tủy răng sau', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng tiền cối/răng cối.', 75, 15, 2000000, 2, 'A_GENERAL', 13, true),
    ('ENDO_POST_CORE', 'Đóng chốt tái tạo cùi răng', 'Đặt chốt vào ống tủy đã chữa để tăng cường lưu giữ cho mão sứ.', 45, 15, 500000, 4, 'A_GENERAL', 14, true),

    -- B. Thẩm mỹ & Phục hình (display_order resets to 1 within this category)
    ('BLEACH_ATHOME', 'Tẩy trắng răng tại nhà', 'Cung cấp máng và thuốc tẩy trắng tại nhà.', 30, 15, 800000, 7, 'B_COSMETIC', 1, true),
    ('BLEACH_INOFFICE', 'Tẩy trắng răng tại phòng (Laser)', 'Tẩy trắng bằng đèn chiếu hoặc laser.', 90, 15, 1200000, 7, 'B_COSMETIC', 2, true),
    ('CROWN_PFM', 'Mão răng sứ Kim loại thường', 'Mão sứ sườn kim loại Cr-Co hoặc Ni-Cr.', 60, 15, 1000000, 4, 'B_COSMETIC', 3, true),
    ('CROWN_TITAN', 'Mão răng sứ Titan', 'Mão sứ sườn hợp kim Titan.', 60, 15, 2500000, 4, 'B_COSMETIC', 4, true),
    ('CROWN_ZIR_KATANA', 'Mão răng toàn sứ Katana/Zir HT', 'Mão sứ 100% Zirconia phổ thông.', 60, 15, 3500000, 4, 'B_COSMETIC', 5, true),
    ('CROWN_ZIR_CERCON', 'Mão răng toàn sứ Cercon HT', 'Mão sứ 100% Zirconia cao cấp (Đức).', 60, 15, 5000000, 4, 'B_COSMETIC', 6, true),
    ('CROWN_EMAX', 'Mão răng sứ thủy tinh Emax', 'Mão sứ Lithium Disilicate thẩm mỹ cao.', 60, 15, 6000000, 4, 'B_COSMETIC', 7, true),
    ('CROWN_ZIR_LAVA', 'Mão răng toàn sứ Lava Plus', 'Mão sứ Zirconia đa lớp (Mỹ).', 60, 15, 8000000, 4, 'B_COSMETIC', 8, true),
    ('VENEER_EMAX', 'Mặt dán sứ Veneer Emax', 'Mặt dán sứ Lithium Disilicate mài răng tối thiểu.', 75, 15, 6000000, 7, 'B_COSMETIC', 9, true),
    ('VENEER_LISI', 'Mặt dán sứ Veneer Lisi Ultra', 'Mặt dán sứ Lithium Disilicate (Mỹ).', 75, 15, 8000000, 7, 'B_COSMETIC', 10, true),
    ('INLAY_ONLAY_ZIR', 'Trám sứ Inlay/Onlay Zirconia', 'Miếng trám gián tiếp bằng sứ Zirconia CAD/CAM.', 60, 15, 2000000, 4, 'B_COSMETIC', 11, true),
    ('INLAY_ONLAY_EMAX', 'Trám sứ Inlay/Onlay Emax', 'Miếng trám gián tiếp bằng sứ Emax Press.', 60, 15, 3000000, 4, 'B_COSMETIC', 12, true),

    -- C. Cắm ghép Implant
    ('IMPL_CONSULT', 'Khám & Tư vấn Implant', 'Khám, đánh giá tình trạng xương, tư vấn kế hoạch.', 45, 15, 0, 4, 'C_IMPLANT', 1, true),
    ('IMPL_CT_SCAN', 'Chụp CT Cone Beam (Implant)', 'Chụp phim 3D phục vụ cắm ghép Implant.', 30, 15, 500000, 4, 'C_IMPLANT', 2, true),
    ('IMPL_SURGERY_KR', 'Phẫu thuật đặt trụ Implant Hàn Quốc', 'Phẫu thuật cắm trụ Implant (VD: Osstem, Biotem).', 90, 30, 15000000, 4, 'C_IMPLANT', 3, true),
    ('IMPL_SURGERY_EUUS', 'Phẫu thuật đặt trụ Implant Thụy Sĩ/Mỹ', 'Phẫu thuật cắm trụ Implant (VD: Straumann, Nobel).', 90, 30, 25000000, 4, 'C_IMPLANT', 4, true),
    ('IMPL_BONE_GRAFT', 'Ghép xương ổ răng', 'Phẫu thuật bổ sung xương cho vị trí cắm Implant.', 60, 30, 5000000, 5, 'C_IMPLANT', 5, true),
    ('IMPL_SINUS_LIFT', 'Nâng xoang hàm (Hở/Kín)', 'Phẫu thuật nâng xoang để cắm Implant hàm trên.', 75, 30, 8000000, 5, 'C_IMPLANT', 6, true),
    ('IMPL_HEALING', 'Gắn trụ lành thương (Healing Abutment)', 'Gắn trụ giúp nướu lành thương đúng hình dạng.', 20, 10, 500000, 4, 'C_IMPLANT', 7, true),
    ('IMPL_IMPRESSION', 'Lấy dấu Implant', 'Lấy dấu để làm răng sứ trên Implant.', 30, 15, 0, 4, 'C_IMPLANT', 8, true),
    ('IMPL_CROWN_TITAN', 'Mão sứ Titan trên Implant', 'Làm và gắn mão sứ Titan trên Abutment.', 45, 15, 3000000, 4, 'C_IMPLANT', 9, true),
    ('IMPL_CROWN_ZIR', 'Mão sứ Zirconia trên Implant', 'Làm và gắn mão sứ Zirconia trên Abutment.', 45, 15, 5000000, 4, 'C_IMPLANT', 10, true),

    -- D. Chỉnh nha
    ('ORTHO_CONSULT', 'Khám & Tư vấn Chỉnh nha', 'Khám, phân tích phim, tư vấn kế hoạch niềng.', 45, 15, 0, 1, 'D_ORTHO', 1, true),
    ('ORTHO_FILMS', 'Chụp Phim Chỉnh nha (Pano, Ceph)', 'Chụp phim X-quang Toàn cảnh và Sọ nghiêng.', 30, 15, 500000, 1, 'D_ORTHO', 2, true),
    ('ORTHO_BRACES_ON', 'Gắn mắc cài kim loại/sứ', 'Gắn bộ mắc cài lên răng.', 90, 30, 5000000, 1, 'D_ORTHO', 3, true),
    ('ORTHO_ADJUST', 'Tái khám Chỉnh nha / Siết niềng', 'Điều chỉnh dây cung, thay thun định kỳ.', 30, 15, 500000, 1, 'D_ORTHO', 4, true),
    ('ORTHO_INVIS_SCAN', 'Scan mẫu hàm Invisalign', 'Scan 3D mẫu hàm để gửi làm khay Invisalign.', 45, 15, 1000000, 1, 'D_ORTHO', 5, true),
    ('ORTHO_INVIS_ATTACH', 'Gắn Attachment Invisalign', 'Gắn các điểm tạo lực trên răng cho Invisalign.', 60, 15, 2000000, 1, 'D_ORTHO', 6, true),
    ('ORTHO_MINIVIS', 'Cắm Mini-vis Chỉnh nha', 'Phẫu thuật nhỏ cắm vít hỗ trợ niềng răng.', 45, 15, 1500000, 1, 'D_ORTHO', 7, true),
    ('ORTHO_BRACES_OFF', 'Tháo mắc cài & Vệ sinh', 'Tháo bỏ mắc cài sau khi kết thúc niềng.', 60, 15, 1000000, 1, 'D_ORTHO', 8, true),
    ('ORTHO_RETAINER_FIXED', 'Gắn hàm duy trì cố định', 'Dán dây duy trì mặt trong răng.', 30, 15, 1000000, 1, 'D_ORTHO', 9, true),
    ('ORTHO_RETAINER_REMOV', 'Làm hàm duy trì tháo lắp', 'Lấy dấu và giao hàm duy trì (máng trong/Hawley).', 30, 15, 1000000, 1, 'D_ORTHO', 10, true),

    -- E. Phục hình Tháo lắp
    ('PROS_CEMENT', 'Gắn sứ / Thử sứ (Lần 2)', 'Hẹn lần 2 để thử và gắn vĩnh viễn mão sứ, cầu răng, veneer.', 30, 15, 0, 4, 'E_PROS_DENTURE', 1, true),
    ('DENTURE_CONSULT', 'Khám & Lấy dấu Hàm Tháo Lắp', 'Lấy dấu lần đầu để làm hàm giả tháo lắp.', 45, 15, 1000000, 4, 'E_PROS_DENTURE', 2, true),
    ('DENTURE_TRYIN', 'Thử sườn/Thử răng Hàm Tháo Lắp', 'Hẹn thử khung kim loại hoặc thử răng sáp.', 30, 15, 0, 4, 'E_PROS_DENTURE', 3, true),
    ('DENTURE_DELIVERY', 'Giao hàm & Chỉnh khớp cắn', 'Giao hàm hoàn thiện, chỉnh sửa các điểm vướng cộm.', 30, 15, 0, 4, 'E_PROS_DENTURE', 4, true),

    -- F. Dịch vụ khác
    ('OTHER_DIAMOND', 'Đính đá/kim cương lên răng', 'Gắn đá thẩm mỹ lên răng.', 30, 15, 300000, 7, 'F_OTHER', 1, true),
    ('OTHER_GINGIVECTOMY', 'Phẫu thuật cắt nướu (thẩm mỹ)', 'Làm dài thân răng, điều trị cười hở lợi.', 60, 30, 1000000, 5, 'F_OTHER', 2, true),
    ('EMERG_PAIN', 'Khám cấp cứu / Giảm đau', 'Khám và xử lý khẩn cấp các trường hợp đau nhức, sưng, chấn thương.', 30, 15, 150000, 8, 'F_OTHER', 3, true),
    ('SURG_CHECKUP', 'Tái khám sau phẫu thuật / Cắt chỉ', 'Kiểm tra vết thương sau nhổ răng khôn, cắm Implant, cắt nướu.', 15, 10, 0, 5, 'F_OTHER', 4, true)
) AS vals(service_code, service_name, description, default_duration_minutes, default_buffer_minutes, price, specialization_id, category_code_ref, display_order, is_active)
LEFT JOIN service_categories sc ON sc.category_code = vals.category_code_ref
ON CONFLICT (service_code) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    display_order = EXCLUDED.display_order,
    updated_at = NOW();

EOF

echo "✅ Seed data inserted successfully!"
echo ""
echo "Step 3: Verifying data..."

docker exec -i postgres-dental psql -U $DB_USER -d $DB_NAME << EOF
-- Show service categories
SELECT category_id, category_code, category_name, display_order
FROM service_categories
ORDER BY display_order;

-- Show service count by category
SELECT
    sc.category_name,
    COUNT(s.service_id) as service_count
FROM service_categories sc
LEFT JOIN services s ON s.category_id = sc.category_id
GROUP BY sc.category_id, sc.category_name
ORDER BY sc.display_order;
EOF

echo ""
echo "==================================="
echo "✅ RESET AND SEED COMPLETED!"
echo "==================================="
echo ""
echo "You can now restart your Spring Boot application."
