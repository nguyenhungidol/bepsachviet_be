# Product Code Manual Entry Implementation

## Date: December 4, 2025

---

## ✅ CHANGES IMPLEMENTED

### Overview
Updated the product management system to allow **manual entry of product codes** instead of auto-generating them with UUID.

---

## 📝 FILES MODIFIED

### 1. ProductRequest.java
**Location:** `f:\bepsachviet_be\src\main\java\com\doan\bepsachviet_be\io\Request\ProductRequest.java`

**Changes:**
- ✅ Added `productId` field with `@NotBlank` validation
- ✅ Added `@NotBlank` validation to `name` field
- ✅ Added validation messages for better error handling

**New Structure:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
  @NotBlank(message = "Product ID is required")
  private String productId;  // ⬅️ NEW: Manually entered product code
  
  @NotBlank(message = "Product name is required")
  private String name;
  
  private String description;
  private String imageSrc;
  private BigDecimal price;
  private String ocUrl;
  private Integer stockQuantity;
  private String categoryId;
}
```

---

### 2. ProductServiceImpl.java
**Location:** `f:\bepsachviet_be\src\main\java\com\doan\bepsachviet_be\service\Impl\ProductServiceImpl.java`

**Changes:**

#### a. Removed UUID Import
```java
// REMOVED: import java.util.UUID;
```

#### b. Updated createProduct Method
**Before:**
```java
ProductEntity entity = ProductEntity.builder()
    .productId(UUID.randomUUID().toString())  // Auto-generated
    .name(request.getName())
    // ...
```

**After:**
```java
// Check if product code already exists
if (productRepository.findByProductId(request.getProductId()).isPresent()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
      "Product code already exists: " + request.getProductId());
}

ProductEntity entity = ProductEntity.builder()
    .productId(request.getProductId())  // ⬅️ Use provided product code
    .name(request.getName())
    // ...
```

#### c. Updated validateRequest Method
Added validation for product code:
```java
private void validateRequest(ProductRequest request) {
  if (request.getProductId() == null || request.getProductId().isBlank()) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product code is required");
  }
  // ... other validations
}
```

---

## 🎯 NEW BEHAVIOR

### Creating a Product

**Before (UUID Auto-Generated):**
```http
POST /api/v1.0/admin/products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Vịt quay",
  "description": "Vịt quay ngon",
  "price": 250000,
  "stockQuantity": 50,
  "categoryId": "CAT-001"
}

Response:
{
  "productId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",  // ⬅️ Auto-generated UUID
  "name": "Vịt quay",
  ...
}
```

**After (Manual Entry):**
```http
POST /api/v1.0/admin/products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "productId": "PROD-VIT-001",  // ⬅️ REQUIRED: Admin provides product code
  "name": "Vịt quay",
  "description": "Vịt quay ngon",
  "price": 250000,
  "stockQuantity": 50,
  "categoryId": "CAT-001"
}

Response:
{
  "productId": "PROD-VIT-001",  // ⬅️ Uses the provided code
  "name": "Vịt quay",
  ...
}
```

---

## ✅ VALIDATIONS ADDED

### 1. Product Code Required
```http
POST /api/v1.0/admin/products
{
  "name": "Vịt quay",
  "price": 250000
}

Response: 400 Bad Request
{
  "error": "Product code is required"
}
```

### 2. Product Code Must Be Unique
```http
POST /api/v1.0/admin/products
{
  "productId": "PROD-VIT-001",  // Already exists
  "name": "Gà ta",
  "price": 200000
}

Response: 400 Bad Request
{
  "error": "Product code already exists: PROD-VIT-001"
}
```

### 3. Product Code Cannot Be Blank
```http
POST /api/v1.0/admin/products
{
  "productId": "   ",  // Blank spaces
  "name": "Vịt quay",
  "price": 250000
}

Response: 400 Bad Request
{
  "error": "Product code is required"
}
```

---

## 📋 PRODUCT CODE NAMING CONVENTIONS (RECOMMENDED)

To maintain consistency, consider using these naming patterns:

### Pattern 1: Category Prefix + Sequential Number
```
PROD-VIT-001    // Duck products
PROD-VIT-002
PROD-GA-001     // Chicken products
PROD-GA-002
PROD-HEO-001    // Pork products
```

### Pattern 2: Category + Type + Number
```
VIT-QUAY-001    // Roasted duck
VIT-HAM-001     // Steamed duck
GA-UMUOI-001    // Salt-baked chicken
```

### Pattern 3: Simple Sequential
```
PROD-001
PROD-002
PROD-003
```

### Pattern 4: Category Code + Date + Number
```
VIT-20251204-001    // Duck product created on Dec 4, 2025
GA-20251204-001
```

**Best Practice:** Choose one pattern and stick to it for consistency!

---

## 🧪 TESTING SCENARIOS

### Test 1: Create Product with Custom Code
```http
POST /api/v1.0/admin/products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "productId": "PROD-VIT-001",
  "name": "Vịt quay Bắc Kinh",
  "description": "Vịt quay theo phong cách Bắc Kinh",
  "imageSrc": "https://...",
  "price": 350000,
  "ocUrl": "https://...",
  "stockQuantity": 30,
  "categoryId": "CAT-VIT"
}
```

**Expected Result:**
- ✅ Product created successfully
- ✅ Product code is "PROD-VIT-001"
- ✅ Can be retrieved via GET /products/PROD-VIT-001

---

### Test 2: Try to Create Duplicate Product Code
```http
POST /api/v1.0/admin/products
{
  "productId": "PROD-VIT-001",  // Same code as Test 1
  "name": "Another product",
  "price": 100000,
  "categoryId": "CAT-VIT"
}
```

**Expected Result:**
- ❌ Status: 400 Bad Request
- ❌ Error: "Product code already exists: PROD-VIT-001"

---

### Test 3: Try to Create Without Product Code
```http
POST /api/v1.0/admin/products
{
  "name": "Vịt quay",
  "price": 100000,
  "categoryId": "CAT-VIT"
}
```

**Expected Result:**
- ❌ Status: 400 Bad Request
- ❌ Error: "Product ID is required" (from @NotBlank validation)

---

### Test 4: Update Product (Product Code Should Not Change)
```http
PUT /api/v1.0/admin/products/PROD-VIT-001
{
  "productId": "PROD-VIT-999",  // Trying to change code
  "name": "Updated name",
  "price": 400000,
  "categoryId": "CAT-VIT"
}
```

**Expected Result:**
- ⚠️ Product code should NOT change (remains "PROD-VIT-001")
- ⚠️ Only other fields are updated
- ℹ️ The productId in the request body is ignored during updates

---

## 🔍 DATABASE IMPACT

### Before Update
```sql
SELECT * FROM products;

+----+--------------------------------------+---------------+--------+
| id | product_id                           | name          | price  |
+----+--------------------------------------+---------------+--------+
| 1  | a1b2c3d4-e5f6-7890-1234-567890abcdef | Vịt quay      | 250000 |
| 2  | f9e8d7c6-b5a4-3210-9876-543210fedcba | Gà ta         | 200000 |
+----+--------------------------------------+---------------+--------+
```

### After Update
```sql
SELECT * FROM products;

+----+--------------+---------------+--------+
| id | product_id   | name          | price  |
+----+--------------+---------------+--------+
| 1  | PROD-VIT-001 | Vịt quay      | 250000 |
| 2  | PROD-GA-001  | Gà ta         | 200000 |
+----+--------------+---------------+--------+
```

---

## ⚠️ MIGRATION CONSIDERATIONS

If you have existing products with UUID product codes, you may need to:

### Option 1: Keep Existing UUIDs (No Migration)
- Existing products keep their UUID codes
- New products use custom codes
- Mixed format (works but not ideal for consistency)

### Option 2: Migrate Existing Products
Create a migration script:
```sql
-- Example: Convert existing UUIDs to custom codes
UPDATE products SET product_id = 'PROD-VIT-001' WHERE id = 1;
UPDATE products SET product_id = 'PROD-VIT-002' WHERE id = 2;
-- ... etc
```

### Option 3: Add Transition Period
- Keep UUID generation as fallback temporarily
- Gradually migrate to custom codes
- Remove UUID generation after migration complete

---

## 📊 ADVANTAGES OF MANUAL PRODUCT CODES

### ✅ Human-Readable
- `PROD-VIT-001` is easier to remember than `a1b2c3d4-e5f6-7890-1234-567890abcdef`
- Easier to communicate ("Give me product VIT-001")

### ✅ Meaningful Structure
- Can encode category information (VIT = duck, GA = chicken)
- Can include date or sequence information

### ✅ Easier URLs
- `/products/PROD-VIT-001` vs `/products/a1b2c3d4-e5f6-7890-1234-567890abcdef`
- Better for SEO and user experience

### ✅ Business-Friendly
- Aligns with physical inventory codes
- Can match barcode systems

### ✅ Consistent with Category System
- You already use custom codes for categories (CAT-001, etc.)
- Products now follow the same pattern

---

## 🔐 SECURITY NOTES

### Product Code as Primary Identifier
- Product codes are now publicly visible identifiers
- Don't include sensitive information in codes
- Use predictable patterns (PROD-001, PROD-002) is fine - database ID prevents conflicts

### Validation
- ✅ Uniqueness check prevents duplicates
- ✅ Required validation prevents empty codes
- ✅ Blank check prevents whitespace-only codes

---

## 🚀 DEPLOYMENT CHECKLIST

- [x] Update ProductRequest.java with productId field
- [x] Update ProductServiceImpl.java to use provided codes
- [x] Add uniqueness validation
- [x] Remove UUID generation
- [x] Test create product with custom code
- [x] Test duplicate code rejection
- [x] Test missing code validation
- [ ] Update API documentation with new requirement
- [ ] Update frontend to include productId input field
- [ ] Train admin users on product code conventions
- [ ] Decide on migration strategy for existing products
- [ ] Update product creation forms/UI

---

## 📞 FRONTEND INTEGRATION

Update the product creation form to include product code input:

```jsx
// Example React form
<form onSubmit={handleCreateProduct}>
  <input
    type="text"
    name="productId"
    placeholder="Product Code (e.g., PROD-VIT-001)"
    required
  />
  <input
    type="text"
    name="name"
    placeholder="Product Name"
    required
  />
  {/* ... other fields */}
</form>
```

---

## ✨ SUMMARY

### What Changed:
- ✅ Product codes are now **manually entered** by admins
- ✅ UUID auto-generation **removed**
- ✅ Uniqueness validation **added**
- ✅ Better error messages **implemented**

### What Stays the Same:
- ✅ Product entity structure unchanged
- ✅ API endpoints unchanged
- ✅ Update/Delete functionality unchanged
- ✅ Database schema unchanged (still uses productId column)

### Next Steps:
1. Update frontend to include product code input
2. Decide on product code naming convention
3. Train admin users
4. Consider migrating existing products (optional)

---

**Implementation completed by:** GitHub Copilot  
**Date:** December 4, 2025  
**Status:** ✅ Ready for Use

