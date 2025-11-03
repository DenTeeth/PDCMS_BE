-- ============================================
-- Migration V1.11: Enable unaccent extension for accent-insensitive search
-- Description: Enable PostgreSQL unaccent extension for Vietnamese search
-- Author: BE-601
-- Date: 2025-11-03
-- ============================================

-- Enable unaccent extension (for removing Vietnamese accents in search)
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Test unaccent function
-- unaccent('Quận Phú Nhuận') = 'Quan Phu Nhuan'
-- unaccent('Công ty TNHH') = 'Cong ty TNHH'

COMMENT ON EXTENSION unaccent IS 'Extension for accent-insensitive text search (Vietnamese support)';
