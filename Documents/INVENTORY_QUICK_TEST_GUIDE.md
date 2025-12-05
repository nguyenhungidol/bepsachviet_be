# Inventory Management - Quick Testing Guide

## 🚀 Quick Start

### 1. Run Database Migration
```bash
# Connect to your database and run:
mysql -u your_username -p your_database < Documents/INVENTORY_MIGRATION.sql

# Or if using application.properties with H2/PostgreSQL, 
# the schema will auto-update on next application start
```

### 2. Restart Application
```bash
# Stop current application (Ctrl+C)
# Then restart
./mvnw spring-boot:run

# Or if using jar
java -jar target/bepsachviet_be-0.0.1-SNAPSHOT.jar
```

## 📝 Testing Checklist

### ✅ Test 1: Create Product with Stock
**Endpoint:** `POST /api/products`

**Request:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Vịt quay Bắc Kinh",
    "description": "Vịt quay thơm ngon",
    "imageSrc": "https://example.com/vit-quay.jpg",
    "price": 250000,
    "ocUrl": "https://example.com/ocop-4-star.png",
    "stockQuantity": 50,
    "categoryId": "your-category-id"
  }'
```

**Expected Response:**
```json
{
  "productId": "generated-uuid",
  "name": "Vịt quay Bắc Kinh",
  "stockQuantity": 50,
  ...
}
```

### ✅ Test 2: Add to Cart - Success Case
**Endpoint:** `POST /api/cart/items`

**Request:**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "productId": "your-product-id",
    "quantity": 5
  }'
```

**Expected:** ✅ Success (if stock >= 5)

### ✅ Test 3: Add to Cart - Out of Stock Error
**Precondition:** Product has stock = 10

**Request:**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "productId": "your-product-id",
    "quantity": 15
  }'
```

**Expected Response:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock. Available: 10, requested: 15"
}
```

### ✅ Test 4: Update Cart Item
**Endpoint:** `PUT /api/cart/items/{itemId}`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/cart/items/123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "quantity": 8
  }'
```

**Expected:** ✅ Success (if stock >= 8) or ❌ Error (if stock < 8)

### ✅ Test 5: Create Order - Stock Decrement
**Precondition:** Product stock = 50

**Endpoint:** `POST /api/orders`

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "deliveryName": "Nguyễn Văn A",
    "deliveryPhone": "0901234567",
    "deliveryAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "notes": "Giao hàng buổi sáng",
    "items": [
      {
        "productId": "your-product-id",
        "quantity": 10
      }
    ]
  }'
```

**Expected:**
- ✅ Order created with status PENDING
- ✅ Product stock decreased from 50 to 40

**Verify:**
```bash
# Get product details to check stock
curl -X GET http://localhost:8080/api/products/your-product-id \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### ✅ Test 6: Cancel Order - Stock Restoration
**Precondition:** Order exists with status PENDING, product stock = 40

**Endpoint:** `PUT /api/orders/{orderId}/status`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/orders/your-order-id/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "status": "CANCELED"
  }'
```

**Expected:**
- ✅ Order status changed to CANCELED
- ✅ Product stock restored from 40 to 50

### ✅ Test 7: Multiple Items Order - Atomic Transaction
**Precondition:**
- Product A stock = 10
- Product B stock = 5

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "deliveryName": "Nguyễn Văn A",
    "deliveryPhone": "0901234567",
    "deliveryAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "items": [
      {
        "productId": "product-a-id",
        "quantity": 5
      },
      {
        "productId": "product-b-id",
        "quantity": 10
      }
    ]
  }'
```

**Expected:**
- ❌ Order creation fails
- ✅ Error: "Insufficient stock for product: [Product B Name]. Available: 5, requested: 10"
- ✅ Product A stock remains 10 (not decremented)
- ✅ Product B stock remains 5 (not decremented)

### ✅ Test 8: Update Product Stock
**Endpoint:** `PUT /api/products/{productId}`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/products/your-product-id \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "stockQuantity": 100
  }'
```

**Expected:** ✅ Product stock updated to 100

## 🧪 Postman Collection

### Import this collection for easier testing:

```json
{
  "info": {
    "name": "Inventory Management Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Create Product with Stock",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{admin_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Test Product\",\n  \"description\": \"Test Description\",\n  \"imageSrc\": \"https://example.com/image.jpg\",\n  \"price\": 100000,\n  \"stockQuantity\": 50,\n  \"categoryId\": \"{{category_id}}\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/products",
          "host": ["{{base_url}}"],
          "path": ["api", "products"]
        }
      }
    },
    {
      "name": "2. Add to Cart (Success)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{user_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"productId\": \"{{product_id}}\",\n  \"quantity\": 5\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/cart/items",
          "host": ["{{base_url}}"],
          "path": ["api", "cart", "items"]
        }
      }
    },
    {
      "name": "3. Add to Cart (Insufficient Stock)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{user_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"productId\": \"{{product_id}}\",\n  \"quantity\": 1000\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/cart/items",
          "host": ["{{base_url}}"],
          "path": ["api", "cart", "items"]
        }
      }
    },
    {
      "name": "4. Create Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{user_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"deliveryName\": \"Nguyễn Văn A\",\n  \"deliveryPhone\": \"0901234567\",\n  \"deliveryAddress\": \"123 Đường ABC, Quận 1, TP.HCM\",\n  \"items\": [\n    {\n      \"productId\": \"{{product_id}}\",\n      \"quantity\": 10\n    }\n  ]\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/orders",
          "host": ["{{base_url}}"],
          "path": ["api", "orders"]
        }
      }
    },
    {
      "name": "5. Cancel Order (Restore Stock)",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{admin_token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"status\": \"CANCELED\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/orders/{{order_id}}/status",
          "host": ["{{base_url}}"],
          "path": ["api", "orders", "{{order_id}}", "status"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    },
    {
      "key": "admin_token",
      "value": "your-admin-jwt-token"
    },
    {
      "key": "user_token",
      "value": "your-user-jwt-token"
    },
    {
      "key": "product_id",
      "value": "your-product-id"
    },
    {
      "key": "category_id",
      "value": "your-category-id"
    },
    {
      "key": "order_id",
      "value": "your-order-id"
    }
  ]
}
```

## 🔍 Verify Stock Changes

### Query Database Directly
```sql
-- Check product stock
SELECT product_id, name, stock_quantity, price
FROM products
ORDER BY stock_quantity ASC;

-- Check orders and their impact on stock
SELECT 
    o.order_id,
    o.status,
    p.name as product_name,
    oi.quantity,
    p.stock_quantity as current_stock,
    o.created_at
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
ORDER BY o.created_at DESC
LIMIT 10;
```

## 🐛 Common Issues & Solutions

### Issue 1: Column 'stock_quantity' doesn't exist
**Solution:** Run the migration script first
```bash
mysql -u root -p your_database < Documents/INVENTORY_MIGRATION.sql
```

### Issue 2: All products show stock = 0
**Solution:** Update stock values manually or via admin panel
```sql
UPDATE products SET stock_quantity = 100;
```

### Issue 3: Can't add to cart even with stock available
**Solution:** Check if stock_quantity is NULL
```sql
UPDATE products SET stock_quantity = COALESCE(stock_quantity, 0);
```

### Issue 4: Stock not decreasing after order
**Solution:** Check transaction is being committed
- Verify `@Transactional` annotation is present
- Check database supports transactions
- Review application logs for errors

## 📊 Monitor Stock Levels

### Low Stock Products Query
```sql
SELECT 
    product_id,
    name,
    stock_quantity,
    price,
    category_id
FROM products
WHERE stock_quantity <= 10
ORDER BY stock_quantity ASC;
```

### Out of Stock Products Query
```sql
SELECT 
    product_id,
    name,
    stock_quantity,
    price
FROM products
WHERE stock_quantity = 0;
```

### Best Selling Products (Stock Movement)
```sql
SELECT 
    p.name,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_sold,
    p.stock_quantity as current_stock
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
GROUP BY p.id
ORDER BY total_sold DESC
LIMIT 10;
```

## ✨ Success Criteria

Your inventory management is working correctly if:

- ✅ Products can be created with initial stock quantity
- ✅ Stock quantity appears in product API responses
- ✅ Adding to cart validates stock availability
- ✅ Orders cannot be placed with insufficient stock
- ✅ Stock decreases when order is placed
- ✅ Stock is restored when PENDING/SHIPPING order is cancelled
- ✅ Stock is NOT restored when DELIVERED order is cancelled
- ✅ Multiple item orders are atomic (all or nothing)
- ✅ Admin can update stock quantities
- ✅ Frontend shows stock status (In Stock / Low Stock / Out of Stock)

## 🎉 Next Steps

1. **Test all scenarios** in the checklist above
2. **Verify database** changes with SQL queries
3. **Test with frontend** integration
4. **Monitor logs** for any errors
5. **Set up alerts** for low stock products
6. **Document** any custom stock rules for your business

---

**Need Help?** Check the main documentation: `INVENTORY_MANAGEMENT_DOCUMENTATION.md`

