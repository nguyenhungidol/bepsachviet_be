-- ============================================================
-- SOFT DELETE IMPLEMENTATION - DATABASE MIGRATION
-- Date: 2025-12-06
-- Purpose: Add soft delete support to products table and
--          snapshot fields to order_items table
-- ============================================================

-- Step 1: Add soft delete columns to products table
ALTER TABLE products
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE products
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Step 2: Add snapshot columns to order_items table to preserve historical data
ALTER TABLE order_items
ADD COLUMN product_name VARCHAR(255) NOT NULL;

ALTER TABLE order_items
ADD COLUMN product_image VARCHAR(500) NULL;

-- Step 3: Create indexes for better query performance
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_category_active ON products(category_id, is_active);
CREATE INDEX idx_products_deleted_at ON products(deleted_at);

-- ============================================================
-- DATA MIGRATION (if needed)
-- ============================================================

-- If you have existing order_items without snapshot data,
-- you can populate them from the current product data:
UPDATE order_items oi
JOIN products p ON oi.product_id = p.id
SET oi.product_name = p.name,
    oi.product_image = p.image_src
WHERE oi.product_name IS NULL OR oi.product_name = '';

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

-- Check that all products are marked as active
SELECT COUNT(*) as total_products,
       SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_products,
       SUM(CASE WHEN is_active = FALSE THEN 1 ELSE 0 END) as deleted_products
FROM products;

-- Check order_items have snapshot data
SELECT COUNT(*) as total_order_items,
       SUM(CASE WHEN product_name IS NOT NULL AND product_name != '' THEN 1 ELSE 0 END) as with_snapshot
FROM order_items;

-- ============================================================
-- ROLLBACK (if needed - use with caution)
-- ============================================================

-- DROP INDEX idx_products_deleted_at;
-- DROP INDEX idx_products_category_active;
-- DROP INDEX idx_products_is_active;
-- ALTER TABLE order_items DROP COLUMN product_image;
-- ALTER TABLE order_items DROP COLUMN product_name;
-- ALTER TABLE products DROP COLUMN deleted_at;
-- ALTER TABLE products DROP COLUMN is_active;

