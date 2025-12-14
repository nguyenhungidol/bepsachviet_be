# Quick Test Guide - New Order ID Format

## Test Scenario 1: Create Single Order
**Endpoint:** `POST /api/v1.0/orders`

**Request Body:**
```json
{
  "items": [
    {
      "productId": "your-product-id",
      "quantity": 2
    }
  ],
  "deliveryName": "Test User",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Test Street",
  "paymentMethod": "CASH_ON_DELIVERY",
  "notes": "Test order"
}
```

**Expected Response:**
```json
{
  "orderId": "ORD-251207-0001",
  "status": "PENDING",
  ...
}
```

✅ **Verify:** Order ID follows format `ORD-YYMMDD-NNNN`

---

## Test Scenario 2: Multiple Orders Same Day
**Action:** Create 3 orders consecutively

**Expected Order IDs:**
1. `ORD-251207-0001`
2. `ORD-251207-0002`
3. `ORD-251207-0003`

✅ **Verify:** Sequential numbering increments correctly

---

## Test Scenario 3: Retrieve Order by New ID
**Endpoint:** `GET /api/v1.0/orders/ORD-251207-0001`

**Expected:** Order details returned successfully

✅ **Verify:** Order retrieval works with new format

---

## Test Scenario 4: MoMo Payment with New ID
**Step 1:** Create order with MoMo payment
```json
{
  "paymentMethod": "MOMO",
  ...
}
```

**Step 2:** Get order ID (e.g., `ORD-251207-0004`)

**Step 3:** Create MoMo payment
```
POST /payment/momo/create
{
  "orderId": "ORD-251207-0004"
}
```

**Expected:** MoMo payment URL returned successfully

✅ **Verify:** MoMo integration works with new order ID format

---

## Test Scenario 5: Admin Update Order Status
**Endpoint:** `PUT /api/v1.0/orders/ORD-251207-0001/status`

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Expected:** Order status updated, confirmation email sent (for COD)

✅ **Verify:** Status updates work with new format

---

## Test Scenario 6: User View Their Orders
**Endpoint:** `GET /api/v1.0/users/me/orders`

**Expected Response:**
```json
{
  "content": [
    {
      "orderId": "ORD-251207-0003",
      "status": "PENDING",
      ...
    },
    {
      "orderId": "ORD-251207-0002",
      "status": "CONFIRMED",
      ...
    },
    {
      "orderId": "ORD-251207-0001",
      "status": "DELIVERED",
      ...
    }
  ]
}
```

✅ **Verify:** All orders display with new ID format

---

## Test Scenario 7: Old UUID Orders Still Work
**Action:** If you have existing orders with UUID format

**Endpoint:** `GET /api/v1.0/orders/{old-uuid-order-id}`

**Expected:** Order retrieved successfully

✅ **Verify:** Backward compatibility maintained

---

## Order ID Format Validation

### Valid Formats:
- `ORD-251207-0001` ✅
- `ORD-251207-0099` ✅
- `ORD-251207-9999` ✅
- `ORD-260101-0001` ✅ (Jan 1, 2026)

### Format Breakdown:
```
ORD - 25 12 07 - 0001
 │    │  │  │     │
 │    │  │  │     └─ Order number (0001-9999)
 │    │  │  └─────── Day (01-31)
 │    │  └─────────── Month (01-12)
 │    └─────────────── Year (2-digit)
 └──────────────────── Prefix
```

---

## Console Logs to Monitor

When creating an order, watch for:
```
✅ Order created with ID: ORD-251207-0001
```

When sending confirmation emails:
```
✅ Order confirmation email sent for COD order: ORD-251207-0001
📧 Order confirmation email sent for order: ORD-251207-0001
```

When MoMo payment succeeds:
```
✅ Payment successful for order: ORD-251207-0001, transactionId: 123456789
📧 Order confirmation email sent for order: ORD-251207-0001
```

---

## Troubleshooting

### Issue: Order ID is still UUID format
**Cause:** Code not recompiled or old compiled classes still in use

**Solution:**
```bash
mvn clean compile
mvn spring-boot:run
```

### Issue: Duplicate order numbers
**Cause:** Race condition (unlikely due to synchronized method)

**Solution:** Check database for duplicate `createdAt` timestamps

### Issue: Order ID number doesn't increment
**Cause:** Database counting issue

**Debug:**
```sql
SELECT COUNT(*) FROM orders 
WHERE created_at >= CURRENT_DATE;
```

---

## Date: December 7, 2025
## Status: Ready for Testing

