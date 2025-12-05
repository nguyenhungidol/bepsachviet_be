-- ============================================
-- Inventory Management Database Migration
-- Date: 2025-12-02
-- Description: Adds stock_quantity field to products table
-- ============================================

-- Step 1: Add stock_quantity column to products table
ALTER TABLE products
ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;

-- Step 2: Set initial stock for existing products (optional)
-- Option A: Set all products to have 100 units in stock
UPDATE products SET stock_quantity = 100;

-- Option B: Set different stock levels by category
-- UPDATE products p
-- JOIN categories c ON p.category_id = c.id
-- SET p.stock_quantity = CASE
--     WHEN c.name LIKE '%vịt%' THEN 50
--     WHEN c.name LIKE '%gà%' THEN 75
--     WHEN c.name LIKE '%heo%' THEN 60
--     WHEN c.name LIKE '%hải sản%' THEN 30
--     ELSE 100
-- END;

-- Step 3: Add index for better performance on stock queries
CREATE INDEX idx_products_stock_quantity ON products(stock_quantity);

-- Step 4: Verify the migration
SELECT
    p.product_id,
    p.name,
    p.stock_quantity,
    c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id
ORDER BY p.stock_quantity ASC
LIMIT 10;

-- ============================================
-- Rollback Script (if needed)
-- ============================================
-- To rollback this migration, uncomment and run:
-- DROP INDEX idx_products_stock_quantity ON products;
-- ALTER TABLE products DROP COLUMN stock_quantity;

