# COD vs MoMo Order Management - Quick Comparison

## Visual Quick Reference - December 6, 2025

---

## 📋 Side-by-Side Comparison

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    COD vs MOMO ORDER HANDLING                            │
└─────────────────────────────────────────────────────────────────────────┘

Feature                  │  COD (Cash on Delivery)  │  MoMo (Online Payment)
─────────────────────────┼──────────────────────────┼─────────────────────────
Initial Status           │  PENDING                 │  PENDING
─────────────────────────┼──────────────────────────┼─────────────────────────
Email on Creation        │  ❌ NO                    │  ❌ NO
─────────────────────────┼──────────────────────────┼─────────────────────────
Auto-Cancel After 15min  │  ❌ NEVER                 │  ✅ YES
─────────────────────────┼──────────────────────────┼─────────────────────────
Manual Cancel by Admin   │  ✅ YES (required)        │  ✅ YES (optional)
─────────────────────────┼──────────────────────────┼─────────────────────────
Confirmation Method      │  Admin manually confirms │  Auto after payment
─────────────────────────┼──────────────────────────┼─────────────────────────
Email Sent When          │  Admin confirms          │  Payment success
─────────────────────────┼──────────────────────────┼─────────────────────────
Stock Deducted           │  ✅ On order creation     │  ✅ On order creation
─────────────────────────┼──────────────────────────┼─────────────────────────
Stock Refunded           │  ✅ On cancel (manual)    │  ✅ On cancel (auto/manual)
─────────────────────────┼──────────────────────────┼─────────────────────────
Can Stay PENDING         │  ✅ Indefinitely          │  ⏱️ Max 15 minutes
─────────────────────────┼──────────────────────────┼─────────────────────────
Payment Collected        │  On delivery             │  Before confirmation
─────────────────────────┼──────────────────────────┼─────────────────────────
Requires Admin Action    │  ✅ YES (must confirm)    │  ❌ NO (auto-confirm)
─────────────────────────┼──────────────────────────┼─────────────────────────
```

---

## 🔄 Timeline Comparison

### COD Order Timeline
```
T=0     Customer creates order
        └─ Status: PENDING
        └─ Stock: -5 units
        └─ Email: ❌ NOT sent
          
T=5min  [No change] ← Still PENDING
          
T=15min [No change] ← Still PENDING ⚠️ NO AUTO-CANCEL
          
T=30min [No change] ← Still PENDING
          
T=1hr   [No change] ← Still PENDING
          
T=2hr   Admin reviews order
        Admin confirms order
        └─ Status: PENDING → CONFIRMED
        └─ Email: ✅ SENT to customer
          
T=2.5hr Admin ships order
        └─ Status: CONFIRMED → SHIPPING
          
T=3hr   Order delivered
        └─ Status: SHIPPING → DELIVERED
        └─ Payment: PENDING → COMPLETED
```

### MoMo Order Timeline
```
T=0     Customer creates order
        └─ Status: PENDING
        └─ Stock: -5 units
        └─ Email: ❌ NOT sent
          
T=2min  Customer completes MoMo payment
        └─ IPN callback received
        └─ Status: PENDING → CONFIRMED
        └─ Payment: PENDING → COMPLETED
        └─ Email: ✅ SENT to customer
          
T=10min Admin processes order
        └─ Status: CONFIRMED → SHIPPING
          
T=30min Order delivered
        └─ Status: SHIPPING → DELIVERED

─────────────── Alternative Path ───────────────

T=0     Customer creates order
        └─ Status: PENDING
        └─ Stock: -5 units
          
T=5min  Customer hasn't paid yet
          
T=15min Customer still hasn't paid
          
T=16min ⚠️ SCHEDULER AUTO-CANCELS
        └─ Status: PENDING → CANCELED
        └─ Stock: +5 units (refunded)
        └─ Note: "Hủy tự động do quá hạn..."
```

---

## 🤖 Scheduler Behavior

### What the Scheduler Does

```
┌─────────────────────────────────────────────────────────────┐
│          EVERY 1 MINUTE, SCHEDULER RUNS                      │
└─────────────────────────────────────────────────────────────┘

[Find all PENDING orders older than 15 minutes]
                    │
                    ↓
            [Loop through each order]
                    │
                    ↓
        ┌───────────┴───────────┐
        │                       │
[Check payment method]    [Check payment method]
        │                       │
  PaymentMethod = COD    PaymentMethod = MOMO
        │                       │
        ↓                       ↓
[🚫 SKIP - Continue]    [✅ CANCEL ORDER]
        │                       │
        ├─ Log: "Bỏ qua đơn COD"  ├─ Status → CANCELED
        ├─ Do nothing           ├─ Refund stock
        │                       ├─ Save order
        │                       └─ Log: "Đã hủy MoMo"
        │                       │
        └─────────┬─────────────┘
                  │
                  ↓
        [Continue to next order]
```

### Code Logic
```java
for (OrderEntity order : expiredOrders) {
  // Check payment method
  if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
    log.debug("Bỏ qua đơn COD (phải admin hủy thủ công): {}", order.getOrderId());
    continue;  // ← Skip COD orders
  }

  // Only MoMo orders reach this point
  order.setStatus(OrderStatus.CANCELED);
  // ... refund stock
}
```

---

## 📊 Order Status Flow Diagrams

### COD Order Flow
```
        [Customer]
            │
            ↓
    ┌───────────────┐
    │ Create Order  │
    └───────┬───────┘
            │
            ↓
    ┌───────────────┐
    │    PENDING    │ ←─── Can stay here forever
    │               │      (NO auto-cancel)
    └───────┬───────┘
            │
            │ ⏳ Wait for admin...
            │
            ↓
        [Admin]
            │
     ┌──────┴──────┐
     │             │
[Confirms]    [Cancels]
     │             │
     ↓             ↓
┌──────────┐  ┌──────────┐
│CONFIRMED │  │ CANCELED │
└────┬─────┘  └──────────┘
     │
     │ 📧 Email sent
     │
     ↓
┌──────────┐
│ SHIPPING │
└────┬─────┘
     │
     ↓
┌───────────┐
│ DELIVERED │
│(Pay COD)  │
└───────────┘
```

### MoMo Order Flow
```
        [Customer]
            │
            ↓
    ┌───────────────┐
    │ Create Order  │
    └───────┬───────┘
            │
            ↓
    ┌───────────────┐
    │    PENDING    │
    │ (Max 15 min)  │
    └───────┬───────┘
            │
     ┌──────┴───────┐
     │              │
[Customer pays] [15min timeout]
     │              │
     ↓              ↓
┌──────────┐  ┌────────────┐
│CONFIRMED │  │  CANCELED  │
│📧 Email  │  │ (Auto by   │
└────┬─────┘  │ scheduler) │
     │        └────────────┘
     │
     ↓
┌──────────┐
│ SHIPPING │
└────┬─────┘
     │
     ↓
┌───────────┐
│ DELIVERED │
└───────────┘
```

---

## 🎯 Use Case Examples

### Example 1: Successful COD Order
```
09:00 - Customer places COD order for $50
        Order ID: COD-001
        Status: PENDING
        Stock: Laptop (-1 unit)

09:30 - Order still PENDING (scheduler skips it)

10:00 - Admin reviews order
        Admin calls customer to confirm
        Customer confirms delivery address

10:05 - Admin confirms order in system
        Status: PENDING → CONFIRMED
        Email: ✅ Sent to customer

11:00 - Admin prepares and ships order
        Status: CONFIRMED → SHIPPING

14:00 - Order delivered
        Customer pays $50 cash
        Status: SHIPPING → DELIVERED
        Payment: PENDING → COMPLETED
```

### Example 2: Successful MoMo Order
```
09:00 - Customer places MoMo order for $50
        Order ID: MOMO-001
        Status: PENDING
        Stock: Laptop (-1 unit)

09:02 - Customer completes MoMo payment
        MoMo IPN callback received
        Status: PENDING → CONFIRMED
        Payment: PENDING → COMPLETED
        Email: ✅ Sent to customer

10:00 - Admin processes confirmed order
        Status: CONFIRMED → SHIPPING

13:00 - Order delivered
        Status: SHIPPING → DELIVERED
```

### Example 3: Abandoned MoMo Order
```
09:00 - Customer places MoMo order for $50
        Order ID: MOMO-002
        Status: PENDING
        Stock: Laptop (-1 unit)

09:05 - Customer closes payment window
        (Doesn't complete payment)

09:16 - Scheduler runs (16 minutes elapsed)
        Detects PENDING MoMo order > 15 min
        Status: PENDING → CANCELED
        Stock: Laptop (+1 unit, refunded)
        Note: "Hủy tự động do quá hạn..."
```

### Example 4: COD Order - Admin Cancels
```
09:00 - Customer places COD order for $50
        Order ID: COD-002
        Status: PENDING
        Stock: Laptop (-1 unit)

09:20 - Admin tries to call customer
        Phone number invalid

09:25 - Admin manually cancels order
        Status: PENDING → CANCELED
        Stock: Laptop (+1 unit, refunded)

Note: Even if admin doesn't cancel, order stays
      PENDING forever (never auto-canceled)
```

---

## 🔍 How to Identify Order Type

### In Database
```sql
-- COD Orders
SELECT * FROM orders 
WHERE payment_method = 'CASH_ON_DELIVERY';

-- MoMo Orders
SELECT * FROM orders 
WHERE payment_method = 'MOMO';
```

### In Admin Panel
```
Order List View:
┌────────────┬─────────┬──────────────────┬──────────┐
│ Order ID   │ Status  │ Payment Method   │ Created  │
├────────────┼─────────┼──────────────────┼──────────┤
│ COD-001    │ PENDING │ COD             │ 2h ago   │ ← Won't auto-cancel
│ MOMO-001   │ PENDING │ MOMO            │ 10min ago│ ← Will auto-cancel
│ MOMO-002   │ CANCELED│ MOMO            │ 1h ago   │ ← Was auto-canceled
└────────────┴─────────┴──────────────────┴──────────┘
```

### In Logs
```
// COD order skipped by scheduler
DEBUG: Bỏ qua đơn COD (phải admin hủy thủ công): COD-001

// MoMo order auto-canceled by scheduler
INFO: 🚫 Đã hủy đơn hàng MoMo quá hạn: MOMO-002
INFO: Hoàn lại 1 cho product id=123, mới: 51
```

---

## ⚠️ Important Warnings

### ❌ DON'T
- ❌ Wait for COD orders to auto-cancel (they never will)
- ❌ Ignore old PENDING COD orders
- ❌ Assume all PENDING orders will be auto-canceled
- ❌ Delete scheduler thinking it's not needed

### ✅ DO
- ✅ Regularly check PENDING COD orders
- ✅ Contact customers for COD verification
- ✅ Manually cancel invalid COD orders
- ✅ Monitor MoMo payment success rate
- ✅ Let scheduler handle MoMo timeouts

---

## 📈 Admin Dashboard Recommendations

### Key Metrics to Track

**COD Orders:**
- Number of PENDING COD orders
- Age of oldest PENDING COD order
- Average time to confirm COD orders
- COD cancellation rate

**MoMo Orders:**
- Number of PENDING MoMo orders (should be low)
- MoMo payment success rate
- Auto-cancel rate
- Average time to payment completion

**Alerts:**
```
⚠️ HIGH PRIORITY
- COD orders PENDING > 24 hours
- More than 50 PENDING COD orders

⚠️ MEDIUM PRIORITY  
- MoMo payment failure rate > 20%
- High auto-cancel rate

✅ LOW PRIORITY
- MoMo orders being auto-canceled (expected)
```

---

## 🛠️ Admin Actions Guide

### For PENDING COD Orders
```
1. View order details
2. Verify customer information
3. Call customer if needed
4. Choose action:
   - Confirm → Customer receives email
   - Cancel → Stock refunded
```

### For PENDING MoMo Orders
```
1. View order details
2. Check payment status
3. If > 10 minutes old:
   - Likely customer abandoned
   - Wait for auto-cancel (saves work)
4. If suspicious:
   - Manually cancel immediately
```

---

## Summary

| Aspect | COD | MoMo |
|--------|-----|------|
| **Who confirms?** | Admin (manual) | System (auto) |
| **Auto-cancel?** | ❌ Never | ✅ After 15 min |
| **Admin workload** | High (must review all) | Low (auto-handled) |
| **Email timing** | After admin confirms | After payment |
| **Best for** | Local customers | Online customers |

---

**Key Takeaway**: COD requires human intervention, MoMo is fully automated.

**Date**: December 6, 2025  
**Status**: ✅ IMPLEMENTED & DOCUMENTED

