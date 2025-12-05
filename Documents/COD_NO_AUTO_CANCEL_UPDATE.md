# COD Order Management - Final Update

## Date: December 6, 2025

## Critical Update: COD Orders Are NOT Auto-Canceled

### Summary
COD (Cash on Delivery) orders are **NOT** subject to automatic cancellation. Only MoMo payment orders are auto-canceled after timeout.

---

## Order Auto-Cancellation Policy

### ❌ COD Orders - NO Auto-Cancel
- **Status**: `PENDING` (indefinitely until admin action)
- **Auto-Cancel**: **DISABLED**
- **Must be canceled by**: Admin manually via admin panel
- **Reason**: COD orders need human verification and approval

### ✅ MoMo Orders - Auto-Cancel Enabled
- **Status**: `PENDING` (waiting for payment)
- **Auto-Cancel**: **ENABLED** after 15 minutes
- **Reason**: Payment timeout, customer didn't complete payment
- **Stock**: Automatically refunded

---

## Implementation Details

### OrderScheduler.java Changes

```java
@Scheduled(fixedRate = 60000)  // Every 1 minute
@Transactional
public void cancelUnpaidOrders() {
  Timestamp fifteenMinutesAgo = new Timestamp(System.currentTimeMillis() - 15 * 60 * 1000);
  
  // Find all PENDING orders older than 15 minutes
  List<OrderEntity> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
      OrderStatus.PENDING, fifteenMinutesAgo);

  for (OrderEntity order : expiredOrders) {
    // ⭐ KEY CHANGE: Skip COD orders, only auto-cancel MoMo orders
    if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
      log.debug("Bỏ qua đơn COD (phải admin hủy thủ công): {}", order.getOrderId());
      continue;  // Skip this order
    }

    // Only MoMo orders reach here
    order.setStatus(OrderStatus.CANCELED);
    order.setNotes("Hủy tự động do quá hạn thanh toán 15 phút");
    // ... refund stock
  }
}
```

**Key Logic**:
1. Find all PENDING orders > 15 minutes old
2. **Check payment method**
3. If COD → Skip (continue loop)
4. If MoMo → Cancel + Refund stock

---

## Complete Order Lifecycle

### COD Order Lifecycle

```
[Customer creates COD order]
           ↓
    Status: PENDING
           ↓
    [Waits indefinitely]  ← ⭐ NO AUTO-CANCEL
           ↓
    ┌──────┴──────┐
    │             │
[Admin confirms] [Admin cancels]
    │             │
CONFIRMED      CANCELED
    │          (manual only)
    ↓
[Order processing...]
    ↓
SHIPPING → DELIVERED
```

### MoMo Order Lifecycle

```
[Customer creates MoMo order]
           ↓
    Status: PENDING
           ↓
    [Customer has 15 minutes to pay]
           ↓
    ┌──────┴──────────────┐
    │                     │
[Pays in time]    [Doesn't pay (>15 min)]
    │                     │
Payment Success      ⚠️ AUTO-CANCEL
    │                (by scheduler)
CONFIRMED                 │
    ↓                CANCELED
[Order processing...]  (stock refunded)
```

---

## Why This Design?

### COD Orders - Manual Only
✅ **Human Verification Needed**
- Admin needs to verify customer information
- May need to call customer for confirmation
- Assess delivery feasibility

✅ **No Payment Timeout**
- No online payment involved
- Customer will pay on delivery
- No rush to confirm

✅ **Flexible Timeline**
- Admin can review during business hours
- Can handle large order volumes
- Can batch process orders

### MoMo Orders - Auto-Cancel
✅ **Payment Timeout is Real**
- Customer must complete payment online
- MoMo session has timeout
- Prevents inventory lock-up

✅ **Automated Process**
- No manual intervention needed
- Stock automatically freed
- System self-healing

✅ **Better Inventory Management**
- Don't hold stock forever for unpaid orders
- Reduce abandoned cart impact
- Keep inventory available

---

## Admin Responsibilities

### For COD Orders
1. **Review regularly** - Check PENDING COD orders in admin panel
2. **Contact customers** - Verify orders via phone if needed
3. **Confirm or Cancel** - Make decision and update status
4. **Process confirmed orders** - Ship and deliver

### For MoMo Orders
1. **Monitor only** - System auto-handles unpaid orders
2. **Process confirmed** - Focus on paid orders only
3. **Handle exceptions** - Manual intervention if needed

---

## API Endpoints

### Admin Order Management

#### View COD Orders (PENDING)
```http
GET /api/v1.0/admin/orders?status=PENDING
Authorization: Bearer <admin_token>

Response: List of all PENDING orders (both COD and MoMo)
```

#### Confirm COD Order
```http
PATCH /api/v1.0/admin/orders/{orderId}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "CONFIRMED"
}

Result: 
- Order status → CONFIRMED
- Email sent to customer ✅
```

#### Cancel COD Order
```http
PATCH /api/v1.0/admin/orders/{orderId}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "CANCELED"
}

Result:
- Order status → CANCELED
- Stock refunded automatically ✅
```

---

## Testing Scenarios

### Test 1: COD Order NOT Auto-Canceled
```
1. Create COD order
2. Wait 16 minutes (longer than timeout)
3. ✅ Verify order still PENDING
4. ✅ Verify scheduler logs show "Bỏ qua đơn COD"
5. ✅ Verify stock NOT refunded
```

### Test 2: MoMo Order IS Auto-Canceled
```
1. Create MoMo order
2. Don't complete payment
3. Wait 16 minutes
4. ✅ Verify order status → CANCELED
5. ✅ Verify note: "Hủy tự động do quá hạn thanh toán 15 phút"
6. ✅ Verify stock refunded
```

### Test 3: Admin Manually Cancels COD
```
1. Create COD order
2. Admin cancels via admin panel
3. ✅ Verify order → CANCELED
4. ✅ Verify stock refunded
```

---

## Monitoring & Logs

### Expected Scheduler Logs

**When COD order is skipped:**
```
DEBUG: Bỏ qua đơn COD (phải admin hủy thủ công): {orderId}
```

**When MoMo order is canceled:**
```
INFO: 🚫 Đã hủy đơn hàng MoMo quá hạn: {orderId}
INFO: Hoàn lại {quantity} cho product id={productId}, mới: {newQuantity}
```

**No action needed:**
```
(No logs - no expired MoMo orders found)
```

---

## Database State

### COD Order After 1 Hour (No Payment)
```sql
SELECT * FROM orders WHERE order_id = 'abc-123';

id  | order_id | status  | payment_method      | created_at
----|----------|---------|--------------------|-----------------
1   | abc-123  | PENDING | CASH_ON_DELIVERY   | 2025-12-06 00:00

-- ✅ Still PENDING after 1 hour
-- ✅ Waiting for admin action
```

### MoMo Order After 16 Minutes (No Payment)
```sql
SELECT * FROM orders WHERE order_id = 'xyz-789';

id  | order_id | status   | payment_method | notes
----|----------|----------|----------------|---------------------------
2   | xyz-789  | CANCELED | MOMO          | Hủy tự động do quá hạn...

-- ✅ Automatically CANCELED
-- ✅ Stock refunded
```

---

## Configuration

### Scheduler Settings
```java
@Scheduled(fixedRate = 60000)  // Run every 60 seconds
```

### Timeout Duration
```java
Timestamp fifteenMinutesAgo = new Timestamp(
  System.currentTimeMillis() - 15 * 60 * 1000
);  // 15 minutes = 15 * 60 * 1000 ms
```

---

## Comparison Table

| Feature | COD Orders | MoMo Orders |
|---------|-----------|-------------|
| Initial Status | PENDING | PENDING |
| Auto-Cancel After Timeout | ❌ NO | ✅ YES (15 min) |
| Manual Cancel by Admin | ✅ YES | ✅ YES |
| Email on Creation | ❌ NO | ❌ NO |
| Email on Confirm | ✅ YES (admin confirms) | ✅ YES (payment success) |
| Stock Deducted | ✅ Immediately | ✅ Immediately |
| Stock Refund on Cancel | ✅ Auto | ✅ Auto |
| Requires Admin Action | ✅ YES (to confirm) | ❌ NO (auto-confirm) |
| Payment Collected | On delivery | Online (before confirm) |

---

## Summary

### ✅ What Changed
- **OrderScheduler** now filters orders by payment method
- **COD orders** are skipped during auto-cancellation
- **Only MoMo orders** are auto-canceled after timeout
- Added clear logging to distinguish behavior

### ✅ What Stayed the Same
- Admin can still manually cancel any order (COD or MoMo)
- Stock refund logic works for both payment methods
- Email sending strategy unchanged
- Security and access control unchanged

### ⚠️ Important Notes
1. COD orders can stay PENDING forever until admin acts
2. Admin must regularly check and process COD orders
3. MoMo orders self-manage (auto-cancel if unpaid)
4. Stock is always refunded on cancellation (manual or auto)

---

## Related Documentation
- [COD Order Admin Confirmation](./COD_ORDER_ADMIN_CONFIRMATION.md)
- [Order System Quick Reference](./ORDER_SYSTEM_QUICK_REFERENCE.md)
- [Order Flow Diagrams](./ORDER_SYSTEM_FLOW_DIAGRAMS.md)

---

**Status**: ✅ IMPLEMENTED & TESTED  
**Build**: SUCCESS  
**Date**: December 6, 2025

