-- ============================================
-- Migration V1.8: Add Warehouse Management Tables
-- Description: Creates tables for warehouse management system
-- Author: BE-601
-- Date: 2024
-- ============================================

-- 1. SUPPLIERS TABLE
-- Medical supplier verification and certification tracking for legal compliance
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id SERIAL PRIMARY KEY,
    
    -- Basic Information
    supplier_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT NOT NULL,
    
    -- Medical Certification Tracking (REQUIRED for legal compliance)
    certification_number VARCHAR(100) NOT NULL, -- GPNK, GPKD, GMP certificates
    registration_date DATE,                      -- Ngày cấp giấy phép
    expiry_date DATE,                            -- Ngày hết hạn
    
    -- Admin Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date DATE,
    verification_by VARCHAR(255),                -- Admin who verified
    
    -- Performance Metrics
    rating DECIMAL(3,1) DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
    total_transactions INTEGER DEFAULT 0,
    last_transaction_date DATE,
    
    -- Status Management
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    notes TEXT,                                  -- GMP compliance notes, warnings, etc.
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Unique Constraints
    CONSTRAINT uk_supplier_name UNIQUE (supplier_name),
    CONSTRAINT uk_supplier_phone UNIQUE (phone_number),
    CONSTRAINT uk_certification_number UNIQUE (certification_number)
);

-- Create indexes for suppliers
CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(supplier_name);
CREATE INDEX IF NOT EXISTS idx_suppliers_phone ON suppliers(phone_number);
CREATE INDEX IF NOT EXISTS idx_suppliers_certification ON suppliers(certification_number);
CREATE INDEX IF NOT EXISTS idx_suppliers_expiry ON suppliers(expiry_date); -- For expiry alerts
CREATE INDEX IF NOT EXISTS idx_suppliers_verified ON suppliers(is_verified, status);
CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
CREATE INDEX IF NOT EXISTS idx_suppliers_created_at ON suppliers(created_at);

-- 2. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_name VARCHAR(255) NOT NULL,
    warehouse_type VARCHAR(50) NOT NULL CHECK (warehouse_type IN ('COLD', 'NORMAL')),
    description TEXT,
    parent_category_id UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uk_category_name_type UNIQUE (category_name, warehouse_type),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) 
        REFERENCES categories(category_id) ON DELETE SET NULL
);

-- Create indexes for categories
CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(category_name);
CREATE INDEX IF NOT EXISTS idx_categories_type ON categories(warehouse_type);
CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_categories_created_at ON categories(created_at);

-- 3. INVENTORY TABLE
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    category_id UUID NOT NULL,
    supplier_id UUID,
    warehouse_type VARCHAR(50) NOT NULL CHECK (warehouse_type IN ('COLD', 'NORMAL')),
    unit_of_measure VARCHAR(50) NOT NULL CHECK (unit_of_measure IN ('PIECE', 'BOX', 'BOTTLE', 'KILOGRAM', 'LITER')),
    quantity_in_stock INTEGER NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER,
    unit_price DECIMAL(15,2),
    expiration_date DATE,
    batch_number VARCHAR(100),
    storage_location VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_inventory_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_supplier FOREIGN KEY (supplier_id) 
        REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    CONSTRAINT uk_inventory_product_batch UNIQUE (product_name, batch_number, supplier_id)
);

-- Create indexes for inventory
CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory(product_name);
CREATE INDEX IF NOT EXISTS idx_inventory_category ON inventory(category_id);
CREATE INDEX IF NOT EXISTS idx_inventory_supplier ON inventory(supplier_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_type ON inventory(warehouse_type);
CREATE INDEX IF NOT EXISTS idx_inventory_expiration ON inventory(expiration_date);
CREATE INDEX IF NOT EXISTS idx_inventory_batch ON inventory(batch_number);
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(quantity_in_stock) 
    WHERE quantity_in_stock <= minimum_stock_level;
CREATE INDEX IF NOT EXISTS idx_inventory_created_at ON inventory(created_at);

-- 4. STORAGE_IN_OUT TABLE (Transaction History)
CREATE TABLE IF NOT EXISTS storage_in_out (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('IN', 'OUT')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    transaction_date TIMESTAMP DEFAULT NOW(),
    reference_number VARCHAR(100),
    performed_by VARCHAR(255),
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_storage_inventory FOREIGN KEY (inventory_id) 
        REFERENCES inventory(inventory_id) ON DELETE CASCADE
);

-- Create indexes for storage_in_out
CREATE INDEX IF NOT EXISTS idx_storage_inventory ON storage_in_out(inventory_id);
CREATE INDEX IF NOT EXISTS idx_storage_type ON storage_in_out(transaction_type);
CREATE INDEX IF NOT EXISTS idx_storage_date ON storage_in_out(transaction_date);
CREATE INDEX IF NOT EXISTS idx_storage_reference ON storage_in_out(reference_number);
CREATE INDEX IF NOT EXISTS idx_storage_created_at ON storage_in_out(created_at);

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================

COMMENT ON TABLE suppliers IS 'Nhà cung cấp vật tư y tế với xác minh chứng chỉ (GPNK, GMP) - tuân thủ pháp luật';
COMMENT ON COLUMN suppliers.supplier_id IS 'UUID khóa chính';
COMMENT ON COLUMN suppliers.certification_number IS 'GPNK (Giấy phép nhập khẩu) hoặc GPKD (Giấy phép kinh doanh) - BẮT BUỘC';
COMMENT ON COLUMN suppliers.registration_date IS 'Ngày cấp giấy phép';
COMMENT ON COLUMN suppliers.expiry_date IS 'Ngày hết hạn giấy phép - hệ thống cảnh báo khi sắp hết hạn';
COMMENT ON COLUMN suppliers.is_verified IS 'Admin đã xác minh chứng chỉ và uy tín nhà cung cấp';
COMMENT ON COLUMN suppliers.verification_by IS 'Tên admin thực hiện xác minh';
COMMENT ON COLUMN suppliers.rating IS 'Đánh giá chất lượng nhà cung cấp 0.0-5.0 dựa trên giao dịch';
COMMENT ON COLUMN suppliers.status IS 'ACTIVE (hoạt động), INACTIVE (tạm ngưng), SUSPENDED (đình chỉ)';
COMMENT ON COLUMN suppliers.notes IS 'Ghi chú về GMP, cảnh báo, kết quả thanh tra, v.v.';

COMMENT ON TABLE categories IS 'Danh mục phân loại hàng hóa theo kho lạnh/thường';
COMMENT ON COLUMN categories.warehouse_type IS 'Loại kho: COLD (kho lạnh), NORMAL (kho thường)';
COMMENT ON COLUMN categories.parent_category_id IS 'Danh mục cha (hỗ trợ phân cấp)';

COMMENT ON TABLE inventory IS 'Tồn kho vật tư y tế';
COMMENT ON COLUMN inventory.warehouse_type IS 'Loại kho lưu trữ: COLD hoặc NORMAL';
COMMENT ON COLUMN inventory.unit_of_measure IS 'Đơn vị tính: PIECE, BOX, BOTTLE, KILOGRAM, LITER';
COMMENT ON COLUMN inventory.quantity_in_stock IS 'Số lượng hiện có trong kho';
COMMENT ON COLUMN inventory.minimum_stock_level IS 'Mức tồn kho tối thiểu (cảnh báo)';
COMMENT ON COLUMN inventory.batch_number IS 'Số lô sản xuất';

COMMENT ON TABLE storage_in_out IS 'Lịch sử nhập/xuất kho';
COMMENT ON COLUMN storage_in_out.transaction_type IS 'Loại giao dịch: IN (nhập), OUT (xuất)';
COMMENT ON COLUMN storage_in_out.reference_number IS 'Mã tham chiếu phiếu nhập/xuất';
COMMENT ON COLUMN storage_in_out.performed_by IS 'Người thực hiện giao dịch';
