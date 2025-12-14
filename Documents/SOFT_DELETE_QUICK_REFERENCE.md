# 🎯 Soft Delete - Quick Reference Card

## 🚀 One-Minute Summary

**What is Soft Delete?**
Instead of physically deleting products, we mark them as "inactive" to preserve data integrity and order history.

**Key Change:**
```java
// ❌ OLD WAY (Hard Delete)
productRepository.delete(product);  // Product is GONE forever

// ✅ NEW WAY (Soft Delete)  
product.setIsActive(false);         // Product marked as deleted
product.setDeletedAt(new Timestamp(System.currentTimeMillis()));
productRepository.save(product);    // Product still in DB
```

---

## 📦 What's New?

### Database
- `products.is_active` (BOOLEAN) - TRUE = active, FALSE = deleted
- `products.deleted_at` (TIMESTAMP) - When deleted
- `order_items.product_name` (VARCHAR) - Snapshot
- `order_items.product_image` (VARCHAR) - Snapshot

### API Response
```json
{
  "productId": "P001",
  "isProductActive": false,     // ✨ NEW
  "availableStock": 0,          // ✨ NEW
  "deletedAt": "2025-12-06..."  // ✨ NEW
}
```

---

## 🔧 How It Works

### Admin Deletes Product
```
DELETE /api/products/P001
        ↓
UPDATE products SET is_active=FALSE, deleted_at=NOW()
        ↓
✅ Product hidden from customers
✅ Data preserved in database
✅ Images remain on S3
```

### Customer Views Cart
```
GET /api/cart
        ↓
{
  "items": [{
    "productId": "P001",
    "isProductActive": false,  ← Check this!
    ...
  }]
}
        ↓
Frontend: Show "No Longer Available" badge
Frontend: Disable checkout button
```

### Customer Tries to Checkout
```
POST /api/orders
{ "items": [{ "productId": "P001", ... }] }
        ↓
Backend validates: is_active = FALSE
        ↓
❌ 400 Bad Request
"Product is no longer available: Gà Ủ Muối"
```

---

## 💻 Code Snippets

### Backend - Check if Product is Active
```java
if (product.getIsActive() == null || !product.getIsActive()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
      "Product is no longer available");
}
```

### Backend - Store Snapshot Data
```java
OrderItemEntity orderItem = OrderItemEntity.builder()
    .product(product)
    .productName(product.getName())      // Snapshot
    .productImage(product.getImageSrc()) // Snapshot
    .price(product.getPrice())           // Snapshot
    .quantity(quantity)
    .build();
```

### Frontend - Display Unavailable Badge
```jsx
{!item.isProductActive && (
  <span className="badge-danger">
    ❌ No Longer Available
  </span>
)}
```

### Frontend - Disable Checkout
```javascript
const hasUnavailableItems = cartItems.some(
  item => !item.isProductActive
);

<button disabled={hasUnavailableItems}>
  {hasUnavailableItems 
    ? "Remove unavailable items" 
    : "Checkout"}
</button>
```

---

## 🎯 Quick Validation

### ✅ Product Deleted Successfully
```sql
SELECT product_id, is_active, deleted_at 
FROM products 
WHERE product_id = 'P001';

-- Expected: is_active = 0, deleted_at = [timestamp]
```

### ✅ Product Hidden from Listings
```bash
GET /api/products
# P001 should NOT appear in response
```

### ✅ Image Preserved on S3
```bash
# Open product image URL in browser
# Expected: Image still loads ✅
```

### ✅ Order History Intact
```bash
GET /api/orders/user
# Past orders with P001 should show:
# - Product name ✅
# - Product image ✅
# - Correct price ✅
```

---

## 🚨 Common Scenarios

### Scenario 1: Product in Cart + Admin Deletes
**What happens?**
- Product remains in cart (not removed)
- `isProductActive = false` in response
- Frontend shows "Unavailable" badge
- Checkout is blocked

**User action:** Remove item from cart manually

---

### Scenario 2: Customer Bought Product + Admin Deletes
**What happens?**
- Order history still shows product correctly
- Name, price, image from snapshot data
- No impact on past orders

**User experience:** Can still view what they bought ✅

---

### Scenario 3: Try to Add Deleted Product
**What happens?**
- Backend rejects: "Product not found" or "Product is no longer available"
- Cannot add to cart

**User experience:** Product doesn't appear in listings anyway

---

## 📋 Testing Checklist (1 Minute)

- [ ] Delete product → Check DB: `is_active=FALSE` ✅
- [ ] Product not in listings anymore ✅
- [ ] Cannot add to cart ✅
- [ ] Cart shows "unavailable" badge ✅
- [ ] Checkout fails with clear error ✅
- [ ] Image still on S3 ✅
- [ ] Order history correct ✅

---

## 🔍 Troubleshooting (30 Seconds)

### "Column 'is_active' not found"
**Fix:** Run database migration first
```bash
mysql < SOFT_DELETE_MIGRATION.sql
```

### Frontend not showing badge
**Fix:** Check API response includes `isProductActive` field
```bash
curl /api/cart | grep isProductActive
```

### Can still checkout with deleted product
**Fix:** Verify backend validation is active
```java
// Should be in OrderServiceImpl.createOrder()
if (!product.getIsActive()) {
  throw new ResponseStatusException(...);
}
```

---

## 📊 Files Changed Summary

### Entities (4 fields added)
- `ProductEntity.java` → `isActive`, `deletedAt`
- `OrderItemEntity.java` → `productName`, `productImage`

### Services (3 files updated)
- `ProductServiceImpl.java` → Soft delete logic
- `CartServiceImpl.java` → Validation logic
- `OrderServiceImpl.java` → Validation + snapshots

### Repositories (3 methods added)
- `ProductRepository.java` → Filter by `isActive`

### DTOs (3 fields added)
- `ProductResponse.java` → `isActive`, `deletedAt`
- `CartItemResponse.java` → `isProductActive`, `availableStock`

### Database (6 columns + 3 indexes)
- See `SOFT_DELETE_MIGRATION.sql`

---

## 🎓 Key Benefits

1. **Data Integrity** → Never lose order history
2. **Customer Trust** → Past orders always accurate
3. **Reversible** → Can restore deleted products
4. **Audit Trail** → Track when products deleted
5. **Safe** → No cascading delete issues
6. **Legal** → Maintain transaction records

---

## 📚 Full Documentation

- **Complete Guide:** `SOFT_DELETE_IMPLEMENTATION_GUIDE.md`
- **Frontend Guide:** `SOFT_DELETE_FRONTEND_GUIDE.md`
- **Flow Diagrams:** `SOFT_DELETE_FLOW_DIAGRAMS.md`
- **Summary:** `SOFT_DELETE_COMPLETE_SUMMARY.md`
- **Deployment:** `SOFT_DELETE_DEPLOYMENT_CHECKLIST.md`
- **SQL Script:** `SOFT_DELETE_MIGRATION.sql`

---

## ⏱️ Deployment Time

1. **Backup DB** → 2 min
2. **Run migration** → 1 min
3. **Deploy code** → 5 min
4. **Test** → 10 min

**Total:** ~20 minutes

---

## 🆘 Emergency Rollback

```bash
# 1. Stop app
kill <pid>

# 2. Restore DB
mysql < backup.sql

# 3. Revert code
git stash

# 4. Restart
mvn spring-boot:run
```

---

## ✅ Success Indicators

- ✅ No compilation errors
- ✅ Application starts normally
- ✅ Product listings work
- ✅ Cart validation works
- ✅ Checkout validation works
- ✅ Order history accurate
- ✅ Images accessible

---

## 🎉 You're Done!

Print this card and keep it handy during deployment.

**Questions?** Check the full documentation files.

**Issues?** Follow the troubleshooting section above.

**Version:** 1.0 | **Date:** 2025-12-06

