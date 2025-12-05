# MoMo Payment - Order Confirmation Email Update

## Overview
Updated the order confirmation email logic to ensure that for MoMo payments, the email is only sent **after** the payment is successfully completed (paymentStatus == COMPLETED).

## Changes Made

### 1. OrderServiceImpl.java
**Location:** `src/main/java/com/doan/bepsachviet_be/service/Impl/OrderServiceImpl.java`

**Change:** Modified the `createOrder()` method to conditionally send order confirmation emails:
- **CASH_ON_DELIVERY**: Email sent immediately when order is created
- **MOMO**: Email is NOT sent during order creation; it will be sent later after successful payment

```java
// Send order confirmation email only for non-MOMO payments
// For MOMO, email will be sent after successful payment in IPN handler
if (savedOrder.getPaymentMethod() != com.doan.bepsachviet_be.constant.PaymentMethod.MOMO) {
  try {
    emailService.sendOrderConfirmationEmail(savedOrder);
  } catch (Exception e) {
    System.err.println("Failed to send order confirmation email for order " + savedOrder.getOrderId() + ": " + e.getMessage());
  }
}
```

### 2. MomoController.java
**Location:** `src/main/java/com/doan/bepsachviet_be/controller/MomoController.java`

**Changes:**
1. Added `EmailService` injection to the controller
2. Modified the `handleIpn()` method to send order confirmation email when payment is successful

```java
if ("0".equals(resultCode)) {
  // Payment successful
  order.setPaymentStatus(PaymentStatus.COMPLETED);

  // Update order status from PENDING to CONFIRMED
  if (order.getStatus() == OrderStatus.PENDING) {
    order.setStatus(OrderStatus.CONFIRMED);
  }

  orderRepository.save(order);
  log.info("✅ Payment successful for order: {}, transactionId: {}", orderId, transactionId);

  // Send order confirmation email after successful payment
  try {
    emailService.sendOrderConfirmationEmail(order);
    log.info("📧 Order confirmation email sent for order: {}", orderId);
  } catch (Exception emailEx) {
    log.error("❌ Failed to send order confirmation email for order {}: {}", orderId, emailEx.getMessage());
  }
}
```

## Payment Flow

### CASH_ON_DELIVERY Payment Flow
1. Customer creates order with CASH_ON_DELIVERY payment method
2. Order is saved to database with status = PENDING
3. ❌ **Order confirmation email is NOT sent**
4. Order waits for admin review
5. Admin reviews and confirms order via admin panel
6. Admin changes order status from PENDING to CONFIRMED
7. ✅ **Order confirmation email is sent to customer**
8. Order proceeds with fulfillment (SHIPPING → DELIVERED)

### MOMO Payment Flow
1. Customer creates order with MOMO payment method
2. Order is saved to database with status = PENDING, paymentStatus = PENDING
3. ❌ **Order confirmation email is NOT sent yet**
4. Customer is redirected to MoMo payment gateway
5. Customer completes payment on MoMo
6. MoMo sends IPN (Instant Payment Notification) callback to our server
7. Server verifies signature and updates order:
   - paymentStatus = COMPLETED
   - status = CONFIRMED (if previously PENDING)
8. ✅ **Order confirmation email is sent ONLY after successful payment**
9. Customer receives email confirmation

## Benefits
- **Better user experience**: Customers only receive order confirmation emails for successfully paid orders
- **Prevents confusion**: Customers won't receive confirmation emails for orders they didn't complete payment for
- **Accurate order tracking**: Email confirmations reflect the actual order status
- **Failed payment handling**: No email is sent if MoMo payment fails or is canceled

## Testing Checklist
- [ ] Test CASH_ON_DELIVERY order - email should be sent immediately
- [ ] Test MOMO successful payment - email should be sent after payment completion
- [ ] Test MOMO failed payment - no email should be sent
- [ ] Test MOMO canceled payment - no email should be sent
- [ ] Verify email content is correct for both payment methods
- [ ] Check logs for proper email sending confirmations

## Date
December 5, 2025

