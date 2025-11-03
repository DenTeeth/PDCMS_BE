-- ============================================
-- Migration V1.9: Update Inventory Table Structure
-- Description: Convert inventory to Long ID, remove Category FK, add new fields
-- Author: BE-601  
-- Date: 2024-11-02
-- ============================================

-- Drop old inventory table (if exists from V1.8)
DROP TABLE IF EXISTS storage_in_out CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;

-- Create new inventory table with Long ID and updated structure
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id SERIAL PRIMARY KEY,
    
    -- Supplier Relationship
    supplier_id INTEGER NOT NULL,
    
    -- Basic Information
    item_name VARCHAR(255) NOT NULL,
    warehouse_type VARCHAR(20) NOT NULL CHECK (warehouse_type IN ('COLD', 'NORMAL')),
    category VARCHAR(100),                       -- Simple string category (no FK)
    
    -- Pricing & Measurement
    unit_price DECIMAL(10,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL CHECK (unit_of_measure IN ('CAI', 'HOP', 'LO', 'GOI', 'CHAI', 'THUNG')),
    
    -- Stock Management
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    min_stock_level INTEGER,
    max_stock_level INTEGER,
    
    -- Expiry & Certification (required for COLD warehouse)
    expiry_date DATE,                            -- BẮT BUỘC nếu warehouse_type = 'COLD'
    is_certified BOOLEAN DEFAULT FALSE,
    certification_date DATE,
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK')),
    notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Foreign Key to Suppliers
    CONSTRAINT fk_inventory_supplier FOREIGN KEY (supplier_id) 
        REFERENCES suppliers(supplier_id) ON DELETE RESTRICT,
    
    -- Unique item name
    CONSTRAINT uk_item_name UNIQUE (item_name)
);

-- Create indexes for inventory
CREATE INDEX IF NOT EXISTS idx_inventory_item_name ON inventory(item_name);
CREATE INDEX IF NOT EXISTS idx_inventory_supplier_id ON inventory(supplier_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_type ON inventory(warehouse_type);
CREATE INDEX IF NOT EXISTS idx_inventory_category ON inventory(category);
CREATE INDEX IF NOT EXISTS idx_inventory_expiry_date ON inventory(expiry_date);
CREATE INDEX IF NOT EXISTS idx_inventory_status ON inventory(status);
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(stock_quantity) 
    WHERE stock_quantity <= min_stock_level;
CREATE INDEX IF NOT EXISTS idx_inventory_created_at ON inventory(created_at);

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================

COMMENT ON TABLE inventory IS 'Tồn kho vật tư y tế - ID tự tăng từ 1';
COMMENT ON COLUMN inventory.inventory_id IS 'ID tự tăng (SERIAL PRIMARY KEY)';
COMMENT ON COLUMN inventory.supplier_id IS 'FK đến suppliers table';
COMMENT ON COLUMN inventory.item_name IS 'Tên vật tư (duy nhất)';
COMMENT ON COLUMN inventory.warehouse_type IS 'Loại kho: COLD (kho lạnh, bắt buộc expiry_date), NORMAL (kho thường)';
COMMENT ON COLUMN inventory.category IS 'Nhóm vật tư (string đơn giản, không dùng FK)';
COMMENT ON COLUMN inventory.unit_price IS 'Đơn giá';
COMMENT ON COLUMN inventory.unit_of_measure IS 'Đơn vị đo: CAI, HOP, LO, GOI, CHAI, THUNG';
COMMENT ON COLUMN inventory.stock_quantity IS 'Số lượng tồn kho hiện tại (>= 0)';
COMMENT ON COLUMN inventory.min_stock_level IS 'Mức tồn kho tối thiểu (cảnh báo)';
COMMENT ON COLUMN inventory.max_stock_level IS 'Mức tồn kho tối đa';
COMMENT ON COLUMN inventory.expiry_date IS 'Ngày hết hạn (BẮT BUỘC nếu COLD warehouse)';
COMMENT ON COLUMN inventory.is_certified IS 'Đã chứng nhận chất lượng';
COMMENT ON COLUMN inventory.certification_date IS 'Ngày chứng nhận';
COMMENT ON COLUMN inventory.status IS 'Trạng thái: ACTIVE, INACTIVE, OUT_OF_STOCK';
COMMENT ON COLUMN inventory.notes IS 'Ghi chú';
