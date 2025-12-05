# Payment System Analysis - MoMo & Cash on Delivery

## Date: December 4, 2025

---

## Current Implementation Status

### ✅ **IMPLEMENTED FEATURES**

#### 1. **Payment Methods**
- ✅ `CASH_ON_DELIVERY` - Cash on delivery payment method
- ✅ `MOMO` - MoMo e-wallet payment integration

#### 2. **Payment Status**
- ✅ `PENDING` - Initial payment status
- ✅ `COMPLETED` - Payment successful
- ✅ `FAILED` - Payment failed
- ✅ `REFUNDED` - Payment refunded

#### 3. **Order Entity**
- ✅ Has `paymentMethod` field (CASH_ON_DELIVERY, MOMO)
- ✅ Has `paymentStatus` field (PENDING, COMPLETED, FAILED, REFUNDED)
- ✅ Has `transactionId` field for storing MoMo transaction ID
- ✅ Default payment method is `CASH_ON_DELIVERY`
- ✅ Default payment status is `PENDING`

#### 4. **Order Creation Flow**
- ✅ User provides payment method in CreateOrderRequest
- ✅ Order is created with specified payment method
- ✅ Stock validation before order creation
- ✅ Stock decrement after order creation
- ✅ Order total calculation

#### 5. **MoMo Integration**
- ✅ MoMoConfig with test credentials
- ✅ MoMoService for creating payment and verifying signature
- ✅ MoMoController with endpoints:
  - POST `/payment/momo/create` - Create MoMo payment
  - GET `/payment/momo/return` - MoMo redirect URL handler
  - POST `/payment/momo/ipn-handler` - MoMo IPN callback handler

---

## 🔍 **IDENTIFIED ISSUES**

### 1. **SecurityConfig - MoMo Endpoints Not Configured**
**Problem**: The MoMo payment endpoints are not whitelisted in SecurityConfig.

**Current State**:
```java
.requestMatchers(HttpMethod.POST, "/orders").authenticated()
.requestMatchers("/orders/**").authenticated()
.anyRequest().authenticated())
```

**Issue**: 
- `/payment/momo/create` requires authentication (should be authenticated)
- `/payment/momo/ipn-handler` requires authentication (should be permitAll for MoMo callback)
- `/payment/momo/return` requires authentication (should be permitAll for redirect)

**Impact**: MoMo cannot callback to IPN handler, payment status won't update automatically.

---

### 2. **Missing Payment Flow Integration**
**Problem**: Order creation and MoMo payment creation are separate processes.

**Current Flow**:
```
1. User creates order → POST /orders (with paymentMethod: MOMO)
2. Order is created with status PENDING, paymentStatus: PENDING
3. User needs to separately call → POST /payment/momo/create
4. User gets payUrl and redirects to MoMo
5. After payment, MoMo calls IPN handler
6. Payment status is updated
```

**Issue**: 
- No automatic transition from order creation to payment
- Frontend needs to handle 2 separate API calls
- No validation that payment is initiated for MOMO orders

---

### 3. **OrderService Missing Payment Logic**
**Problem**: OrderService doesn't handle payment method logic.

**Current Behavior**:
- All orders are created the same way regardless of payment method
- Stock is decremented immediately for both CASH_ON_DELIVERY and MOMO
- Order status is PENDING for both methods

**Expected Behavior**:
- **CASH_ON_DELIVERY**: 
  - Order created with status PENDING
  - Payment status PENDING (will be COMPLETED when delivered)
  - Stock decremented immediately
  
- **MOMO**:
  - Order created with status PENDING
  - Payment status PENDING
  - Should NOT decrement stock until payment is COMPLETED
  - OR: Decrement stock but restore if payment fails within timeout

---

### 4. **MoMoController Issues**

#### a. Fake Order Object
```java
OrderEntity fakeOrder = new OrderEntity();
fakeOrder.setOrderId(orderId);
fakeOrder.setTotalAmount(BigDecimal.valueOf(grandTotal));
```
**Issue**: Creates a fake order instead of fetching the real order.

#### b. No Transaction ID Storage
**Issue**: MoMo response contains `transId` but it's not stored in the order.

#### c. No Order Status Update
**Issue**: When payment is completed, only `paymentStatus` is updated. Order status should also change from PENDING to CONFIRMED or PROCESSING.

---

### 5. **Missing Stock Management for Failed Payments**
**Problem**: If MOMO payment fails, stock is not restored.

**Scenario**:
1. User creates order with MOMO → Stock decremented
2. User doesn't pay or payment fails → Stock remains decremented
3. Order is stuck in PENDING state with FAILED payment status

**Solution Needed**:
- Restore stock when payment fails
- OR: Don't decrement stock until payment is completed

---

### 6. **No Payment Timeout Mechanism**
**Problem**: Orders with MOMO payment method can stay in PENDING state indefinitely.

**Solution Needed**:
- Add scheduled job to auto-cancel unpaid MOMO orders after X minutes
- Restore stock when auto-cancelled

---

### 7. **Missing Payment Validation**
**Problem**: No validation that CASH_ON_DELIVERY orders should complete payment upon delivery.

**Current State**:
- CASH_ON_DELIVERY orders have paymentStatus PENDING
- No mechanism to update to COMPLETED when order is DELIVERED

---

## 🔧 **RECOMMENDED FIXES**

### Priority 1: SecurityConfig (CRITICAL)
```java
.requestMatchers("/payment/momo/ipn-handler").permitAll()
.requestMatchers("/payment/momo/return").permitAll()
.requestMatchers("/payment/momo/create").authenticated()
```

### Priority 2: Integrate Payment into Order Creation
Modify OrderController to:
1. Create order
2. If payment method is MOMO, immediately create payment and return payUrl
3. Return OrderResponse with optional `paymentUrl` field

### Priority 3: Fix Stock Management for MOMO
Option A: Don't decrement stock until payment is completed
Option B: Decrement stock but restore if payment fails/timeout

### Priority 4: Store Transaction ID
Update MomoController IPN handler to store `transId` in order.

### Priority 5: Update Order Status on Payment
When payment is COMPLETED, update order status from PENDING to CONFIRMED.

---

## 📝 **TESTING CHECKLIST**

### Cash on Delivery
- [ ] Create order with CASH_ON_DELIVERY
- [ ] Verify order status is PENDING
- [ ] Verify payment status is PENDING
- [ ] Verify stock is decremented
- [ ] Admin updates order status to SHIPPING
- [ ] Admin updates order status to DELIVERED
- [ ] Manually update payment status to COMPLETED (or auto-update when DELIVERED)

### MoMo Payment
- [ ] Create order with MOMO
- [ ] Verify order is created
- [ ] Call /payment/momo/create with orderId
- [ ] Verify payUrl is returned
- [ ] Redirect to MoMo payment page
- [ ] Complete payment on MoMo
- [ ] Verify IPN handler is called by MoMo
- [ ] Verify payment status is updated to COMPLETED
- [ ] Verify order status is updated to CONFIRMED
- [ ] Verify transaction ID is stored

### MoMo Payment Failure
- [ ] Create order with MOMO
- [ ] Call /payment/momo/create
- [ ] Cancel payment on MoMo
- [ ] Verify payment status is FAILED
- [ ] Verify stock is restored (if implementing Option B)

---

## 🚀 **IMPLEMENTATION PLAN**

### Step 1: Fix SecurityConfig
Add MoMo endpoints to permitAll for IPN handler.

### Step 2: Fix MomoController
- Fetch real order instead of creating fake one
- Store transaction ID
- Update order status when payment completes
- Add proper error handling and logging

### Step 3: Enhance OrderService
- Add logic to handle different payment methods
- Implement stock restoration for failed MOMO payments
- Add payment timeout mechanism

### Step 4: Update OrderResponse
- Add optional `paymentUrl` field for MOMO orders

### Step 5: Integrate Payment into Order Creation
- Modify createOrder to automatically initiate MOMO payment
- Return payment URL in response

### Step 6: Add Payment Status Automation
- Auto-update payment status to COMPLETED when CASH_ON_DELIVERY order is DELIVERED
- Add scheduled job for payment timeout

---

## 📌 **CURRENT ENDPOINT SUMMARY**

### Order Endpoints
- `POST /orders` - Create order (authenticated)
- `GET /orders/my-orders` - Get user's orders (authenticated)
- `GET /orders/{orderId}` - Get order details (authenticated)
- `GET /admin/orders` - Get all orders (admin)
- `GET /admin/orders/{orderId}` - Get order details (admin)
- `PATCH /admin/orders/{orderId}/status` - Update order status (admin)

### MoMo Endpoints
- `POST /payment/momo/create` - Create MoMo payment (needs authentication fix)
- `GET /payment/momo/return` - MoMo redirect handler (needs permitAll)
- `POST /payment/momo/ipn-handler` - MoMo IPN callback (needs permitAll)

---

## ⚠️ **SECURITY CONSIDERATIONS**

1. **IPN Handler**: Must verify signature from MoMo to prevent fraud
2. **Order Validation**: Ensure user owns the order before creating payment
3. **Amount Validation**: Verify amount in MoMo response matches order amount
4. **Transaction ID**: Store and validate transaction ID to prevent duplicate processing

---

## 🎯 **CONCLUSION**

The basic payment infrastructure is in place, but there are several critical issues that need to be addressed:

1. **SecurityConfig must be fixed immediately** - IPN handler is blocked
2. **Stock management needs improvement** - Handle failed MOMO payments
3. **Payment flow integration** - Make it seamless for frontend
4. **Transaction tracking** - Store transaction IDs properly
5. **Status automation** - Auto-update statuses based on payment results

The CASH_ON_DELIVERY implementation is functional but basic. The MOMO integration has the core functionality but needs the above fixes to work properly in production.

