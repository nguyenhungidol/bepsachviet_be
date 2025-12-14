# 🚀 Soft Delete Implementation - Deployment Checklist

## ✅ Pre-Deployment Checklist

### 1. Code Review ✓
- [x] ProductEntity has `isActive` and `deletedAt` fields
- [x] OrderItemEntity has `productName` and `productImage` fields
- [x] ProductRepository has active-only query methods
- [x] ProductService implements soft delete (UPDATE not DELETE)
- [x] CartService validates product `isActive` status
- [x] OrderService validates product `isActive` before order creation
- [x] OrderService stores snapshot data in order items
- [x] Response DTOs include new fields
- [x] No compilation errors

### 2. Documentation ✓
- [x] SOFT_DELETE_MIGRATION.sql created
- [x] SOFT_DELETE_IMPLEMENTATION_GUIDE.md created
- [x] SOFT_DELETE_FRONTEND_GUIDE.md created
- [x] SOFT_DELETE_FLOW_DIAGRAMS.md created
- [x] SOFT_DELETE_COMPLETE_SUMMARY.md created

---

## 📋 Deployment Steps

### Step 1: Backup Database ⚠️ CRITICAL
```bash
# Create backup before running migration
mysqldump -u username -p database_name > backup_before_soft_delete_$(date +%Y%m%d_%H%M%S).sql
```
**Status:** [ ] Done

---

### Step 2: Run Database Migration
```bash
# Option A: Using MySQL CLI
mysql -u username -p database_name < Documents/SOFT_DELETE_MIGRATION.sql

# Option B: Using MySQL Workbench
# 1. Open SOFT_DELETE_MIGRATION.sql
# 2. Execute the script
# 3. Verify no errors
```
**Status:** [ ] Done

**Expected Results:**
- ✅ `products` table has `is_active` column (BOOLEAN, default TRUE)
- ✅ `products` table has `deleted_at` column (TIMESTAMP, nullable)
- ✅ `order_items` table has `product_name` column (VARCHAR)
- ✅ `order_items` table has `product_image` column (VARCHAR)
- ✅ Indexes created on `is_active`, `deleted_at`, `category_id + is_active`

---

### Step 3: Verify Database Schema
```sql
-- Check products table
DESCRIBE products;
-- Look for: is_active, deleted_at

-- Check order_items table
DESCRIBE order_items;
-- Look for: product_name, product_image

-- Check indexes
SHOW INDEX FROM products;
-- Look for: idx_products_is_active, idx_products_category_active, idx_products_deleted_at

-- Verify all products are active
SELECT 
    COUNT(*) as total_products,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_products,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as deleted_products
FROM products;
-- Expected: all products should be active
```
**Status:** [ ] Done

**Expected Output:**
```
total_products: XX
active_products: XX (should equal total)
deleted_products: 0
```

---

### Step 4: Update Existing Order Items (if needed)
```sql
-- Only run if you have existing orders without snapshot data
UPDATE order_items oi
JOIN products p ON oi.product_id = p.id
SET oi.product_name = p.name,
    oi.product_image = p.image_src
WHERE oi.product_name IS NULL OR oi.product_name = '';

-- Verify
SELECT COUNT(*) as items_with_snapshot
FROM order_items
WHERE product_name IS NOT NULL AND product_name != '';
```
**Status:** [ ] Done (or N/A if no existing orders)

---

### Step 5: Build Backend
```bash
cd F:\bepsachviet_be
mvn clean compile
```
**Status:** [ ] Done

**Expected:** No compilation errors

---

### Step 6: Run Tests (Optional but Recommended)
```bash
mvn test
```
**Status:** [ ] Done (or Skipped)

---

### Step 7: Start Application
```bash
mvn spring-boot:run
```
**Status:** [ ] Done

**Expected:** Application starts on port 8080 without errors

---

## 🧪 Post-Deployment Testing

### Test 1: Product Listing (Customer View)
```bash
GET http://localhost:8080/api/v1.0/products
```
**Expected:** 
- ✅ Returns list of products
- ✅ All products have `isActive: true`
- ✅ All products have `deletedAt: null`

**Status:** [ ] Pass [ ] Fail

---

### Test 2: Soft Delete Product (Admin)
```bash
# 1. Pick a test product ID (e.g., P001)
# 2. Delete it
DELETE http://localhost:8080/api/v1.0/products/P001
Authorization: Bearer {admin-token}

# 3. Verify response
# Expected: 200 OK
```
**Status:** [ ] Pass [ ] Fail

---

### Test 3: Verify Soft Delete in Database
```sql
-- Check product is soft deleted
SELECT product_id, name, is_active, deleted_at
FROM products
WHERE product_id = 'P001';
-- Expected: is_active = 0 (FALSE), deleted_at has timestamp
```
**Status:** [ ] Pass [ ] Fail

**Expected Result:**
```
product_id | name       | is_active | deleted_at
P001       | Gà Ủ Muối  | 0         | 2025-12-06 10:30:00
```

---

### Test 4: Product No Longer in Listings
```bash
GET http://localhost:8080/api/v1.0/products

# Check that P001 is NOT in the response
```
**Status:** [ ] Pass [ ] Fail

**Expected:** P001 should NOT appear in the list

---

### Test 5: Cannot Add Deleted Product to Cart
```bash
POST http://localhost:8080/api/v1.0/cart/items
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "productId": "P001",
  "quantity": 1
}
```
**Status:** [ ] Pass [ ] Fail

**Expected:** 
- ❌ 400 Bad Request
- Message: "Product is no longer available"

---

### Test 6: Cart Shows Unavailable Product
```bash
# Assuming P001 was already in cart before deletion

GET http://localhost:8080/api/v1.0/cart
Authorization: Bearer {user-token}
```
**Status:** [ ] Pass [ ] Fail

**Expected Response:**
```json
{
  "items": [
    {
      "productId": "P001",
      "productName": "Gà Ủ Muối",
      "isProductActive": false,  ← Should be false
      "availableStock": 0,       ← Should be 0 or null
      ...
    }
  ]
}
```

---

### Test 7: Cannot Checkout with Deleted Product
```bash
POST http://localhost:8080/api/v1.0/orders
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "deliveryName": "Test User",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "Test Address",
  "paymentMethod": "CASH_ON_DELIVERY",
  "items": [
    {
      "productId": "P001",
      "quantity": 1
    }
  ]
}
```
**Status:** [ ] Pass [ ] Fail

**Expected:**
- ❌ 400 Bad Request
- Message: "Product is no longer available: Gà Ủ Muối"

---

### Test 8: Stock Management Still Works
```bash
# 1. Find an active product with stock
GET http://localhost:8080/api/v1.0/products

# 2. Note the stock quantity (e.g., stock = 10)

# 3. Create an order with that product
POST http://localhost:8080/api/v1.0/orders
Authorization: Bearer {user-token}
{
  "items": [
    {
      "productId": "P002",
      "quantity": 2
    }
  ],
  ...
}

# 4. Check stock decreased
SELECT stock_quantity FROM products WHERE product_id = 'P002';
-- Expected: 10 - 2 = 8
```
**Status:** [ ] Pass [ ] Fail

---

### Test 9: Order History Preserved
```bash
# 1. Create an order with product P002
# 2. Admin deletes product P002
# 3. View order history

GET http://localhost:8080/api/v1.0/orders/user
Authorization: Bearer {user-token}
```
**Status:** [ ] Pass [ ] Fail

**Expected:**
- ✅ Order still shows product P002
- ✅ Product name is displayed correctly
- ✅ Product image URL still works
- ✅ Price is correct

---

### Test 10: Image Still on S3
```bash
# 1. Note the image URL of deleted product P001
# 2. Open the URL in browser

# Expected: Image still loads ✅
```
**Status:** [ ] Pass [ ] Fail

**Expected:** Image is still accessible (not deleted from S3)

---

## 🎨 Frontend Integration Testing

### Frontend Test 1: Cart Badge Display
**Test:** View cart with deleted product
**Expected:**
- [ ] "No Longer Available" badge displayed
- [ ] Product image is grayed out or semi-transparent
- [ ] Remove button is prominently displayed

**Status:** [ ] Pass [ ] Fail

---

### Frontend Test 2: Checkout Button Disabled
**Test:** Cart contains unavailable product
**Expected:**
- [ ] Checkout button is disabled
- [ ] Button text shows reason (e.g., "Remove unavailable items")
- [ ] Cannot click checkout

**Status:** [ ] Pass [ ] Fail

---

### Frontend Test 3: Error Handling
**Test:** Try to checkout with unavailable product
**Expected:**
- [ ] Error message displayed
- [ ] Error includes product name
- [ ] Cart auto-refreshes or prompts to refresh

**Status:** [ ] Pass [ ] Fail

---

### Frontend Test 4: Order History
**Test:** View past orders with deleted products
**Expected:**
- [ ] Product name displays correctly
- [ ] Product image loads correctly
- [ ] Price displays correctly

**Status:** [ ] Pass [ ] Fail

---

## 📊 Performance Testing

### Performance Test 1: Product Listing Speed
```bash
# Before and after comparison
time curl http://localhost:8080/api/v1.0/products
```
**Expected:** Response time similar to before (minimal impact from WHERE clause)

**Status:** [ ] Pass [ ] Fail

---

### Performance Test 2: Cart Loading Speed
```bash
time curl -H "Authorization: Bearer {token}" http://localhost:8080/api/v1.0/cart
```
**Expected:** Response time similar to before

**Status:** [ ] Pass [ ] Fail

---

## 🚨 Rollback Plan (if needed)

### If Something Goes Wrong:

#### Step 1: Stop Application
```bash
# Press Ctrl+C or kill the process
```

#### Step 2: Restore Database Backup
```bash
# Restore from backup created in Step 1
mysql -u username -p database_name < backup_before_soft_delete_YYYYMMDD_HHMMSS.sql
```

#### Step 3: Revert Code Changes
```bash
git stash  # or git checkout <previous-commit>
```

#### Step 4: Restart Application
```bash
mvn spring-boot:run
```

---

## ✅ Sign-Off

### Backend Developer
- [ ] Code reviewed and tested
- [ ] Database migration executed successfully
- [ ] All backend tests pass
- [ ] API endpoints tested manually

**Name:** ________________  
**Date:** ________________  
**Signature:** ________________

---

### Frontend Developer
- [ ] Cart UI updated to show unavailable products
- [ ] Checkout validation implemented
- [ ] Error handling implemented
- [ ] All frontend tests pass

**Name:** ________________  
**Date:** ________________  
**Signature:** ________________

---

### QA Tester
- [ ] All test scenarios executed
- [ ] No critical bugs found
- [ ] Performance acceptable
- [ ] Ready for production

**Name:** ________________  
**Date:** ________________  
**Signature:** ________________

---

### Project Manager
- [ ] All stakeholders informed
- [ ] Documentation complete
- [ ] Deployment approved

**Name:** ________________  
**Date:** ________________  
**Signature:** ________________

---

## 📞 Support Contacts

**Backend Issues:**
- Check logs: `tail -f logs/spring.log`
- Check database: Verify migration ran correctly
- Rollback: Follow rollback plan above

**Frontend Issues:**
- Check browser console for errors
- Verify API responses include new fields
- Check network tab for 400 errors

**Database Issues:**
- Verify migration script ran without errors
- Check indexes are created
- Verify data integrity

---

## 📚 Reference Documents

1. `SOFT_DELETE_IMPLEMENTATION_GUIDE.md` - Technical details
2. `SOFT_DELETE_FRONTEND_GUIDE.md` - Frontend code examples
3. `SOFT_DELETE_FLOW_DIAGRAMS.md` - System flow visualization
4. `SOFT_DELETE_COMPLETE_SUMMARY.md` - Complete overview
5. `SOFT_DELETE_MIGRATION.sql` - Database migration script

---

## 🎉 Deployment Complete

Once all checkboxes are checked and all tests pass:

✅ **Soft Delete Implementation is LIVE!**

**Deployed By:** ________________  
**Deployment Date:** ________________  
**Deployment Time:** ________________  
**Version:** 1.0

---

**Notes/Comments:**
_____________________________________________________________
_____________________________________________________________
_____________________________________________________________
_____________________________________________________________


