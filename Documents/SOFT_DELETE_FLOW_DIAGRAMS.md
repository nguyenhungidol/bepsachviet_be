# Soft Delete System Flow Diagrams

## 📊 Overview Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         SOFT DELETE SYSTEM                       │
│                                                                  │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐ │
│  │   Frontend   │◄────►│   Backend    │◄────►│   Database   │ │
│  │              │      │              │      │              │ │
│  │ • Cart UI    │      │ • Validation │      │ • is_active  │ │
│  │ • Badges     │      │ • Soft Delete│      │ • deleted_at │ │
│  │ • Errors     │      │ • Snapshots  │      │ • snapshots  │ │
│  └──────────────┘      └──────────────┘      └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flow 1: Admin Deletes Product

```
┌──────────┐                                    ┌──────────┐
│  Admin   │                                    │ Database │
└────┬─────┘                                    └────┬─────┘
     │                                               │
     │ 1. DELETE /api/products/P001                 │
     ├──────────────────────────────►               │
     │         (Admin Token)                        │
     │                                               │
     │            Backend Service                    │
     │       ┌────────────────────┐                 │
     │       │ Find Product P001  │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │       ┌─────────▼──────────┐                 │
     │       │ Set is_active=FALSE│                 │
     │       │ Set deleted_at=NOW │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │                 │ UPDATE products             │
     │                 │ SET is_active=0             │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ ✅ Updated                  │
     │                 │◄────────────────────────────┤
     │                                               │
     │ ✅ 200 OK (Product soft deleted)             │
     │◄──────────────────────────────                │
     │                                               │
     │ ⚠️ Image remains on S3                        │
     │    (preserved for order history)              │
     │                                               │
```

**Key Points:**
- ✅ No physical DELETE query
- ✅ Product remains in database
- ✅ Image not deleted from S3
- ✅ Can be restored later if needed

---

## 🛒 Flow 2: Customer Views Cart (with deleted product)

```
┌──────────┐                                    ┌──────────┐
│ Customer │                                    │ Database │
└────┬─────┘                                    └────┬─────┘
     │                                               │
     │ 1. GET /api/cart                             │
     ├──────────────────────────────►               │
     │                                               │
     │            Backend Service                    │
     │       ┌────────────────────┐                 │
     │       │ Get User's Cart    │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │                 │ SELECT cart_items           │
     │                 │ JOIN products               │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ Returns:                    │
     │                 │ - Product P001              │
     │                 │   is_active = FALSE ⚠️      │
     │                 │◄────────────────────────────┤
     │                 │                             │
     │       ┌─────────▼──────────┐                 │
     │       │ Build Response     │                 │
     │       │ Add flags:         │                 │
     │       │ • isProductActive  │                 │
     │       │ • availableStock   │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │ Response:                                     │
     │ {                                             │
     │   items: [{                                   │
     │     productId: "P001",                        │
     │     productName: "Gà Ủ Muối",                │
     │     isProductActive: false, ⚠️                │
     │     availableStock: 0                         │
     │   }]                                          │
     │ }                                             │
     │◄──────────────────────────────                │
     │                                               │
     │ Frontend renders:                             │
     │ ┌─────────────────────────┐                  │
     │ │ ❌ Gà Ủ Muối           │                  │
     │ │ No Longer Available     │                  │
     │ │ [Remove Item]           │                  │
     │ └─────────────────────────┘                  │
     │ [Checkout] ← DISABLED                         │
     │                                               │
```

**Key Points:**
- ✅ Cart still shows the item
- ✅ Marked as unavailable
- ✅ Checkout button disabled
- ✅ Clear visual feedback

---

## 🚫 Flow 3: Customer Tries to Checkout (with deleted product)

```
┌──────────┐                                    ┌──────────┐
│ Customer │                                    │ Database │
└────┬─────┘                                    └────┬─────┘
     │                                               │
     │ 1. POST /api/orders                          │
     │    { items: [{ productId: "P001", qty: 2 }] }│
     ├──────────────────────────────►               │
     │                                               │
     │            Backend Service                    │
     │       ┌────────────────────┐                 │
     │       │ Validate Products  │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │                 │ SELECT * FROM products      │
     │                 │ WHERE product_id = 'P001'   │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ Returns:                    │
     │                 │ - P001, is_active = FALSE ⚠️│
     │                 │◄────────────────────────────┤
     │                 │                             │
     │       ┌─────────▼──────────┐                 │
     │       │ Validation Failed! │                 │
     │       │ Product inactive   │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │ ❌ 400 Bad Request                            │
     │ {                                             │
     │   status: 400,                                │
     │   message: "Product is no longer available:  │
     │             Gà Ủ Muối"                       │
     │ }                                             │
     │◄──────────────────────────────                │
     │                                               │
     │ Frontend shows error:                         │
     │ ┌─────────────────────────┐                  │
     │ │ ⚠️ Order Failed         │                  │
     │ │                         │                  │
     │ │ Product is no longer    │                  │
     │ │ available: Gà Ủ Muối   │                  │
     │ │                         │                  │
     │ │ [Refresh Cart]          │                  │
     │ └─────────────────────────┘                  │
     │                                               │
```

**Key Points:**
- ✅ Order creation fails gracefully
- ✅ Clear error message
- ✅ No order created in database
- ✅ Stock not decremented

---

## ✅ Flow 4: Successful Order (saves snapshot data)

```
┌──────────┐                                    ┌──────────┐
│ Customer │                                    │ Database │
└────┬─────┘                                    └────┬─────┘
     │                                               │
     │ 1. POST /api/orders                          │
     │    { items: [{ productId: "P002", qty: 2 }] }│
     ├──────────────────────────────►               │
     │                                               │
     │            Backend Service                    │
     │       ┌────────────────────┐                 │
     │       │ Validate Product   │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │                 │ SELECT * FROM products      │
     │                 │ WHERE product_id = 'P002'   │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ Returns:                    │
     │                 │ - P002                      │
     │                 │   is_active = TRUE ✅       │
     │                 │   name = "Vịt Quay"         │
     │                 │   price = 200000            │
     │                 │   image = "s3://..."        │
     │                 │◄────────────────────────────┤
     │                 │                             │
     │       ┌─────────▼──────────┐                 │
     │       │ Create Order       │                 │
     │       │ Store Snapshot:    │                 │
     │       │ • product_name     │                 │
     │       │ • product_image    │                 │
     │       │ • price            │                 │
     │       └─────────┬──────────┘                 │
     │                 │                             │
     │                 │ INSERT INTO orders          │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ INSERT INTO order_items     │
     │                 │   product_id = 2            │
     │                 │   product_name = "Vịt Quay"│
     │                 │   product_image = "s3://..." │
     │                 │   price = 200000            │
     │                 │   quantity = 2              │
     │                 ├────────────────────────────►│
     │                 │                             │
     │                 │ UPDATE products             │
     │                 │ SET stock = stock - 2       │
     │                 │ WHERE id = 2                │
     │                 ├────────────────────────────►│
     │                 │                             │
     │ ✅ 201 Created                                │
     │ { orderId: "ORD123", ... }                    │
     │◄──────────────────────────────                │
     │                                               │
     │                                               │
     │ LATER: Admin deletes Product P002             │
     │        ↓                                      │
     │ UPDATE products SET is_active=FALSE           │
     │        WHERE id = 2                           │
     │                                               │
     │ Customer views order history:                 │
     │ ┌─────────────────────────┐                  │
     │ │ Order #ORD123           │                  │
     │ │ ┌─────────────────────┐ │                  │
     │ │ │ 🖼️ [Image]          │ │ ← From snapshot  │
     │ │ │ Vịt Quay           │ │ ← From snapshot  │
     │ │ │ 200,000đ           │ │ ← From snapshot  │
     │ │ │ Qty: 2              │ │                  │
     │ │ └─────────────────────┘ │                  │
     │ └─────────────────────────┘                  │
     │ ✅ Still shows correct info!                  │
     │                                               │
```

**Key Points:**
- ✅ Snapshot data stored in order_items
- ✅ Product name, image, price preserved
- ✅ Order history accurate forever
- ✅ Works even after product deletion/modification

---

## 🔍 Database State Comparison

### Before Soft Delete (Hard Delete)
```
[Admin clicks DELETE]
     ↓
┌─────────────────────────────────────────────────────────┐
│ products table                                          │
│ ┌────┬────────────┬───────────┬─────────┐             │
│ │ id │ product_id │ name      │ price   │             │
│ ├────┼────────────┼───────────┼─────────┤             │
│ │ 1  │ P001       │ Gà Ủ Muối │ 150000  │ ← DELETED!  │
│ │ 2  │ P002       │ Vịt Quay  │ 200000  │             │
│ └────┴────────────┴───────────┴─────────┘             │
└─────────────────────────────────────────────────────────┘

Result:
❌ Product 1 is GONE
❌ Foreign key issues in order_items
❌ Order history broken
❌ Can't see what customer bought
```

### After Soft Delete (Soft Delete)
```
[Admin clicks DELETE]
     ↓
┌───────────────────────────────────────────────────────────────────┐
│ products table                                                    │
│ ┌────┬────────────┬───────────┬────────┬───────────┬────────────┐│
│ │ id │ product_id │ name      │ price  │ is_active │ deleted_at ││
│ ├────┼────────────┼───────────┼────────┼───────────┼────────────┤│
│ │ 1  │ P001       │ Gà Ủ Muối │ 150000 │ FALSE ⚠️  │ 2025-12-06││
│ │ 2  │ P002       │ Vịt Quay  │ 200000 │ TRUE ✅   │ NULL      ││
│ └────┴────────────┴───────────┴────────┴───────────┴────────────┘│
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│ order_items table                                                 │
│ ┌────┬──────────┬────────────┬──────────────┬──────────┬────────┐│
│ │ id │ order_id │ product_id │ product_name │ price    │ qty   ││
│ ├────┼──────────┼────────────┼──────────────┼──────────┼────────┤│
│ │ 1  │ ORD001   │ 1          │ Gà Ủ Muối   │ 150000   │ 2     ││
│ │ 2  │ ORD002   │ 2          │ Vịt Quay    │ 200000   │ 1     ││
│ └────┴──────────┴────────────┴──────────────┴──────────┴────────┘│
│        ↑ FK still valid!    ↑ Snapshot preserved!                │
└───────────────────────────────────────────────────────────────────┘

Result:
✅ Product 1 is PRESERVED
✅ Foreign keys intact
✅ Order history perfect
✅ Customer sees what they bought
✅ Can restore product if needed
```

---

## 📈 System Status Indicators

### Product Status Flow
```
┌─────────────┐
│   ACTIVE    │  is_active = TRUE
│             │  deleted_at = NULL
│  Visible to │  ✅ Appears in listings
│  customers  │  ✅ Can be purchased
└──────┬──────┘  ✅ Can add to cart
       │
       │ [Admin clicks DELETE]
       │
       ▼
┌─────────────┐
│  DELETED    │  is_active = FALSE
│             │  deleted_at = 2025-12-06...
│  Hidden     │  ❌ Hidden from listings
│  from       │  ❌ Cannot be purchased
│  customers  │  ❌ Cannot add to cart
└──────┬──────┘  ⚠️  Shows "unavailable" in cart
       │          ✅ Still in database
       │          ✅ Order history intact
       │
       │ [Optional: Admin restores]
       │
       ▼
┌─────────────┐
│  RESTORED   │  is_active = TRUE
│             │  deleted_at = NULL (cleared)
│  Visible    │  ✅ Appears in listings again
│  again      │  ✅ Can be purchased again
└─────────────┘  ✅ Can add to cart
```

---

## 🎯 Validation Checkpoints

### Checkpoint 1: Product Listing
```
GET /api/products
         ↓
┌────────────────────┐
│ ProductRepository  │
│ .findAllByIsActive │
│      (true)        │
└─────────┬──────────┘
          │
          ▼
SELECT * FROM products
WHERE is_active = TRUE  ← Only active products
          │
          ▼
    Return to customer
```

### Checkpoint 2: Add to Cart
```
POST /api/cart/items
         ↓
┌──────────────────────┐
│ Fetch Product        │
│ Check is_active      │  ← Validation #1
└─────────┬────────────┘
          │
          ▼
  if (!product.isActive)
    ❌ Throw Error
          │
          ▼
┌──────────────────────┐
│ Check stock          │  ← Validation #2
│ quantity > 0         │
└─────────┬────────────┘
          │
          ▼
  if (stock <= 0)
    ❌ Throw Error
          │
          ▼
    ✅ Add to cart
```

### Checkpoint 3: Checkout
```
POST /api/orders
         ↓
┌──────────────────────┐
│ Validate ALL items   │
│ in order             │
└─────────┬────────────┘
          │
    ┌─────▼─────┐
    │ Item 1    │
    │ Check:    │
    │ • Active? │  ← Validation
    │ • Stock?  │  ← Validation
    └─────┬─────┘
          │
    ┌─────▼─────┐
    │ Item 2    │
    │ Check:    │
    │ • Active? │
    │ • Stock?  │
    └─────┬─────┘
          │
          ▼
  if (any validation fails)
    ❌ Throw Error
    (with product name)
          │
          ▼
┌──────────────────────┐
│ Create Order         │
│ Save Snapshots       │
│ Decrement Stock      │
└──────────────────────┘
          │
          ▼
    ✅ Order created
```

---

## 📊 Performance Impact

### Query Performance
```
Before: SELECT * FROM products
After:  SELECT * FROM products WHERE is_active = TRUE

Impact: +1 WHERE clause
Mitigation: INDEX on is_active column ✅
Performance: Minimal impact
```

### Storage Impact
```
Before: Physical DELETE removes row
        Database size decreases

After:  Soft DELETE keeps row
        + 2 columns per product (is_active, deleted_at)
        + 2 columns per order_item (product_name, product_image)
        
Impact: Database size increases slightly
Trade-off: Data integrity > storage cost ✅
```

---

**End of Flow Diagrams**

These diagrams illustrate the complete flow of the soft delete system from admin actions to customer interactions and database operations.

