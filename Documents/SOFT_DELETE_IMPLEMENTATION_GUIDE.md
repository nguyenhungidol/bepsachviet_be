# Soft Delete Implementation - Complete Documentation

## 📋 Overview

This document outlines the complete soft delete strategy implementation for the product management system. Instead of physically deleting products from the database, we mark them as "inactive" to preserve data integrity and maintain historical order records.

---

## 🎯 Implementation Goals

1. **Preserve Data Integrity**: Keep product data for historical orders
2. **Maintain Order History**: Past orders show correct product details even after deletion
3. **Improve User Experience**: Clear messaging when products become unavailable
4. **Prevent Cart Issues**: Validate product availability during checkout
5. **Admin Control**: Allow admins to "delete" products without data loss

---

## 🗄️ Database Changes

### 1. Products Table - New Columns

```sql
is_active BOOLEAN NOT NULL DEFAULT TRUE
deleted_at TIMESTAMP NULL
```

**Purpose**:
- `is_active`: Flag to indicate if product is available (TRUE) or soft-deleted (FALSE)
- `deleted_at`: Timestamp when product was marked as deleted (for audit trail)

### 2. Order Items Table - Snapshot Columns

```sql
product_name VARCHAR(255) NOT NULL
product_image VARCHAR(500) NULL
```

**Purpose**:
- Store product name and image at the time of order creation
- Preserve historical data even if product is later modified or deleted
- Existing `price` field already serves as price snapshot

---

## 🔧 Backend Implementation

### 1. Entity Updates

#### ProductEntity.java
```java
@Column(nullable = false)
private Boolean isActive = true;

@Column
private Timestamp deletedAt;
```

#### OrderItemEntity.java
```java
@Column(nullable = false)
private String productName; // Snapshot

@Column
private String productImage; // Snapshot
```

### 2. Repository Updates

#### ProductRepository.java
New query methods to filter by active status:
- `findByProductIdAndIsActive(productId, true)` - Get active product by ID
- `findAllByIsActive(true)` - Get all active products
- `findAllByCategory_CategoryIdAndIsActive(categoryId, true)` - Get active products by category

### 3. Service Layer Changes

#### ProductServiceImpl.java

**List Products** (Lines ~78-82):
```java
public List<ProductResponse> listProducts() {
  return productRepository.findAllByIsActive(true)
      .stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
}
```
- Only returns active products to customers

**Get Product** (Lines ~84-89):
```java
public ProductResponse getProduct(String productId) {
  return productRepository.findByProductIdAndIsActive(productId, true)
      .map(this::convertToResponse)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
}
```
- Only returns product if it's active

**Delete Product** (Lines ~98-107):
```java
public void deleteProduct(String productId) {
  ProductEntity entity = productRepository.findByProductId(productId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  
  // Soft delete: mark as inactive
  entity.setIsActive(false);
  entity.setDeletedAt(new java.sql.Timestamp(System.currentTimeMillis()));
  productRepository.save(entity);
  
  // Images are NOT deleted to preserve order history
}
```
- **Key Change**: Uses UPDATE instead of DELETE
- Marks product as inactive
- Records deletion timestamp
- **Does NOT delete images from S3** - preserves for historical orders

#### CartServiceImpl.java

**Add to Cart** (Lines ~52-56):
```java
// Check if product is active (soft delete check)
if (product.getIsActive() == null || !product.getIsActive()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is no longer available");
}
```

**Update Cart Item** (Lines ~110-114):
```java
// Check if product is still active
if (product.getIsActive() == null || !product.getIsActive()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is no longer available");
}
```

**Sync Cart** (Lines ~208-211):
```java
// Skip inactive (soft deleted) products
if (product.getIsActive() == null || !product.getIsActive()) {
  continue;
}
```

**Cart Item Response** (Lines ~289-303):
```java
return CartItemResponse.builder()
    // ...existing fields...
    .isProductActive(product.getIsActive() != null && product.getIsActive())
    .availableStock(product.getStockQuantity())
    // ...
    .build();
```
- Includes `isProductActive` flag for frontend validation
- Includes `availableStock` for stock checking

#### OrderServiceImpl.java

**Create Order Validation** (Lines ~66-77):
```java
// Check if product is active (soft delete check)
if (product.getIsActive() == null || !product.getIsActive()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
      "Product is no longer available: " + product.getName());
}
```

**Store Snapshot Data** (Lines ~110-118):
```java
OrderItemEntity orderItem = OrderItemEntity.builder()
    .order(order)
    .product(product)
    .productName(product.getName()) // Snapshot
    .productImage(product.getImageSrc()) // Snapshot
    .quantity(itemRequest.getQuantity())
    .price(product.getPrice()) // Snapshot
    .subtotal(subtotal)
    .build();
```
- Stores product name, image, and price at time of purchase
- Ensures order history remains accurate

---

## 📡 API Response Updates

### ProductResponse.java
```java
private Boolean isActive;
private Timestamp deletedAt;
```

### CartItemResponse.java
```java
private Boolean isProductActive; // For frontend validation
private Integer availableStock;  // Current stock level
```

---

## 🎨 Frontend Integration Guide

### 1. Product Listing Page
**No changes needed** - Backend only returns active products

### 2. Cart Page

#### Check Product Availability
```javascript
// In your cart component
const unavailableItems = cartItems.filter(item => !item.isProductActive);

if (unavailableItems.length > 0) {
  // Show warning message
  console.warn("Some items in your cart are no longer available");
}
```

#### Display Unavailable Products
```jsx
{cartItems.map(item => (
  <CartItem key={item.itemId}>
    <ProductImage src={item.productImageSrc} />
    <ProductName>{item.productName}</ProductName>
    {!item.isProductActive && (
      <UnavailableTag>❌ No Longer Available</UnavailableTag>
    )}
    {item.isProductActive && item.availableStock <= 0 && (
      <OutOfStockTag>⚠️ Out of Stock</OutOfStockTag>
    )}
    <Price>{item.productPrice}</Price>
  </CartItem>
))}
```

#### Disable Checkout Button
```javascript
const hasUnavailableItems = cartItems.some(item => 
  !item.isProductActive || item.availableStock < item.quantity
);

<CheckoutButton 
  disabled={hasUnavailableItems}
  onClick={handleCheckout}
>
  {hasUnavailableItems 
    ? "Remove unavailable items to checkout" 
    : "Proceed to Checkout"}
</CheckoutButton>
```

### 3. Checkout Validation

#### Client-Side Validation
```javascript
const validateCart = () => {
  const errors = [];
  
  cartItems.forEach(item => {
    if (!item.isProductActive) {
      errors.push(`${item.productName} is no longer available`);
    }
    if (item.quantity > item.availableStock) {
      errors.push(`${item.productName}: Only ${item.availableStock} in stock`);
    }
  });
  
  return errors;
};
```

#### Server-Side Validation
The backend automatically validates:
- ✅ Product is active
- ✅ Sufficient stock available
- ✅ Product still exists

**Error Handling**:
```javascript
try {
  const response = await createOrder(orderData);
} catch (error) {
  if (error.response?.status === 400) {
    // Product no longer available or out of stock
    alert(error.response.data.message);
    // Refresh cart to show updated status
    await refreshCart();
  }
}
```

### 4. Order History Page
**No changes needed** - Orders display snapshot data (name, price, image stored at time of purchase)

---

## ✅ Testing Checklist

### Admin Side
- [ ] Delete a product (verify it's soft-deleted, not physically removed)
- [ ] Check database: `is_active` = FALSE, `deleted_at` has timestamp
- [ ] Verify product image remains on S3
- [ ] Confirm product doesn't appear in customer product listings

### Customer Cart
- [ ] Add product to cart
- [ ] Admin deletes the product
- [ ] Refresh cart page - should show "No Longer Available" message
- [ ] Checkout button should be disabled
- [ ] Try to checkout - should receive error message
- [ ] Remove unavailable item from cart
- [ ] Checkout should work with remaining valid items

### Customer Checkout
- [ ] Try to add deleted product to cart - should fail
- [ ] Try to checkout with deleted product - should fail with clear error
- [ ] Verify error message includes product name

### Order History
- [ ] Place an order with product A
- [ ] Admin deletes product A
- [ ] View past orders - product A should still show:
  - ✅ Correct name (from snapshot)
  - ✅ Correct price (from snapshot)
  - ✅ Correct image (from snapshot)
- [ ] Product image URL should still work

### Stock Management
- [ ] Product with 5 in stock
- [ ] Try to add 10 to cart - should fail
- [ ] Add 3 to cart - should succeed
- [ ] Try to checkout with 3 - should succeed
- [ ] Stock should decrease to 2 after order

---

## 🚨 Important Notes

### AWS S3 Image Management
**⚠️ CRITICAL**: Product images are **NOT deleted** during soft delete!

**Reason**: 
- Order history needs to display product images
- Customers should see what they purchased
- Images are referenced by URL in `order_items.product_image`

**Storage Consideration**:
- Images accumulate over time
- Implement a cleanup job for images from products deleted > 1 year ago (optional)
- Or accept the storage cost as a business requirement

### Database Migration
**Run the SQL migration** before deploying backend changes:
```bash
mysql -u username -p database_name < Documents/SOFT_DELETE_MIGRATION.sql
```

Or use your preferred migration tool (Flyway, Liquibase, etc.)

### Existing Orders
If you have existing `order_items` without snapshot data:
1. Run the data migration query (see SQL file)
2. Populates `product_name` and `product_image` from current product data
3. Future orders automatically include snapshots

---

## 🔄 Workflow Summary

### Admin Deletes Product
1. Admin clicks "Delete" button
2. Backend sets `is_active = FALSE`, `deleted_at = NOW()`
3. Product no longer appears in customer listings
4. Product images remain on S3

### Customer Views Cart
1. Frontend receives cart with `isProductActive` field
2. If `isProductActive = false`, show "Unavailable" badge
3. Disable checkout if any items unavailable
4. Provide option to remove unavailable items

### Customer Attempts Checkout
1. Backend validates all products are active
2. Backend validates stock availability
3. If validation fails, return 400 error with clear message
4. Frontend displays error and refreshes cart

### Order Creation
1. Backend stores snapshot data:
   - `product_name` (from product.name)
   - `product_image` (from product.imageSrc)
   - `price` (from product.price)
2. Order items reference product but include snapshot
3. Historical accuracy preserved

---

## 📊 Database Schema Reference

### Before Soft Delete
```sql
products:
- id (PK)
- product_id (unique)
- name
- price
- image_src
- stock_quantity
- category_id (FK)

order_items:
- id (PK)
- order_id (FK)
- product_id (FK)
- quantity
- price
- subtotal
```

### After Soft Delete
```sql
products:
- id (PK)
- product_id (unique)
- name
- price
- image_src
- stock_quantity
- is_active ✨ NEW
- deleted_at ✨ NEW
- category_id (FK)

order_items:
- id (PK)
- order_id (FK)
- product_id (FK)
- product_name ✨ NEW (snapshot)
- product_image ✨ NEW (snapshot)
- quantity
- price (snapshot)
- subtotal
```

---

## 🎓 Benefits of This Implementation

1. **Data Integrity**: Never lose order history data
2. **Customer Trust**: Past orders always show accurate product info
3. **Admin Flexibility**: Can "delete" products without consequences
4. **Better UX**: Clear messaging when products become unavailable
5. **Audit Trail**: `deleted_at` timestamp for tracking
6. **Reversible**: Could add "restore product" feature later
7. **Legal Compliance**: Maintains transaction records

---

## 🔮 Future Enhancements (Optional)

### 1. Restore Deleted Products
```java
public void restoreProduct(String productId) {
  ProductEntity product = productRepository.findByProductId(productId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  
  product.setIsActive(true);
  product.setDeletedAt(null);
  productRepository.save(product);
}
```

### 2. Admin View Deleted Products
```java
public List<ProductResponse> listDeletedProducts() {
  return productRepository.findAllByIsActive(false)
      .stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
}
```

### 3. Permanent Delete (After 1 Year)
```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void cleanupOldDeletedProducts() {
  Timestamp oneYearAgo = new Timestamp(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000);
  List<ProductEntity> oldDeletedProducts = productRepository
      .findAllByIsActiveFalseAndDeletedAtBefore(oneYearAgo);
  
  for (ProductEntity product : oldDeletedProducts) {
    // Delete image from S3
    fileUploadService.deleteFile(product.getImageSrc());
    // Permanently delete from database
    productRepository.delete(product);
  }
}
```

---

## 📞 Support & Questions

If you encounter any issues:
1. Check database migration ran successfully
2. Verify indexes were created
3. Test with a sample product
4. Check application logs for errors
5. Ensure frontend is using updated API responses

---

**Document Version**: 1.0  
**Last Updated**: 2025-12-06  
**Author**: GitHub Copilot  

