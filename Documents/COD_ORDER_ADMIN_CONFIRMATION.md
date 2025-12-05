# COD Order Admin Confirmation System

## Overview
Implemented a manual confirmation system for Cash on Delivery (COD) orders where only administrators can change order status and confirmation emails are sent only after admin approval.

## Key Features

### 1. **COD Orders Require Manual Admin Confirmation**
- COD orders are created with `PENDING` status
- No automatic confirmation email is sent when order is created
- Admin must manually confirm the order by changing status to `CONFIRMED`
- Confirmation email is sent only after admin approval

### 2. **Admin-Only Status Management**
- Only users with `ROLE_ADMIN` can update order status
- Endpoint: `PATCH /api/v1.0/admin/orders/{orderId}/status`
- Protected by Spring Security configuration

### 3. **Email Sending Strategy**

| Payment Method | Order Creation | Email Sent When |
|---------------|----------------|-----------------|
| **CASH_ON_DELIVERY** | Status: `PENDING` | Admin changes status to `CONFIRMED` |
| **MOMO** | Status: `PENDING` | Payment successfully completed (via IPN) |

## Implementation Details

### OrderServiceImpl Changes

#### 1. Order Creation (`createOrder` method)
```java
// Email sending strategy:
// - MOMO: Email sent after successful payment in IPN handler
// - CASH_ON_DELIVERY: Email sent after admin confirms the order (status changed to CONFIRMED)
// No automatic email is sent during order creation
```

**Behavior:**
- Creates order with `PENDING` status
- Decrements product stock immediately
- **Does NOT send confirmation email**
- Returns order response to customer

#### 2. Status Update (`updateOrderStatus` method)
```java
// Send order confirmation email when admin confirms COD order
if (status == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING &&
    order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
  try {
    emailService.sendOrderConfirmationEmail(updatedOrder);
    System.out.println("✅ Order confirmation email sent for COD order: " + orderId);
  } catch (Exception e) {
    System.err.println("❌ Failed to send order confirmation email for COD order " + orderId + ": " + e.getMessage());
  }
}
```

**Behavior:**
- Only triggers when:
  - New status is `CONFIRMED`
  - Old status was `PENDING`
  - Payment method is `CASH_ON_DELIVERY`
- Sends confirmation email to customer
- Logs success/failure for monitoring

## Order Flow Diagrams

### COD Order Flow

```
1. Customer creates order
   ↓
2. Order saved (status: PENDING)
   ↓
3. Stock decremented
   ↓
4. Customer receives order ID
   ↓
5. ❌ NO EMAIL SENT YET
   ↓
6. Admin reviews order in admin panel
   ↓
7. Admin confirms order (PENDING → CONFIRMED)
   ↓
8. ✅ Confirmation email sent to customer
   ↓
9. Order proceeds to SHIPPING → DELIVERED
```

### MoMo Order Flow

```
1. Customer creates order
   ↓
2. Order saved (status: PENDING)
   ↓
3. Stock decremented
   ↓
4. Customer redirected to MoMo payment
   ↓
5. Payment completed
   ↓
6. MoMo IPN callback received
   ↓
7. Order status: PENDING → CONFIRMED
   ↓
8. ✅ Confirmation email sent to customer
```

## Security Configuration

### Admin Endpoints (SecurityConfig.java)
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

**Protected Endpoints:**
- `GET /api/v1.0/admin/orders` - View all orders
- `GET /api/v1.0/admin/orders/{orderId}` - View specific order
- `PATCH /api/v1.0/admin/orders/{orderId}/status` - Update order status ⭐

### Customer Endpoints
```java
.requestMatchers(HttpMethod.POST, "/orders").authenticated()
.requestMatchers("/orders/**").authenticated()
```

**Customer Access:**
- `POST /api/v1.0/orders` - Create new order
- `GET /api/v1.0/orders/my-orders` - View own orders
- `GET /api/v1.0/orders/{orderId}` - View specific order details

## API Usage

### Customer Creates COD Order
```http
POST /api/v1.0/orders
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "deliveryName": "John Doe",
  "deliveryPhone": "0912345678",
  "deliveryAddress": "123 Main St, Hanoi",
  "paymentMethod": "CASH_ON_DELIVERY",
  "notes": "Please call before delivery",
  "items": [
    {
      "productId": "prod-123",
      "quantity": 2
    }
  ]
}
```

**Response:**
```json
{
  "orderId": "abc-123-xyz",
  "status": "PENDING",
  "paymentStatus": "PENDING",
  "paymentMethod": "CASH_ON_DELIVERY",
  "totalAmount": 500000,
  ...
}
```

**Customer Experience:**
- ✅ Order created successfully
- ✅ Order ID provided
- ❌ No confirmation email yet
- ⏳ Waiting for admin confirmation

### Admin Confirms COD Order
```http
PATCH /api/v1.0/admin/orders/{orderId}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**Response:**
```json
{
  "orderId": "abc-123-xyz",
  "status": "CONFIRMED",
  "paymentStatus": "PENDING",
  "paymentMethod": "CASH_ON_DELIVERY",
  ...
}
```

**System Actions:**
- ✅ Order status updated to CONFIRMED
- ✅ Confirmation email sent to customer
- ✅ Admin can proceed with order fulfillment

## Additional Features

### Automatic Stock Refund on Cancellation
When an order is canceled, stock is automatically restored:
```java
if (status == OrderStatus.CANCELED &&
    (oldStatus == OrderStatus.PENDING || oldStatus == OrderStatus.CONFIRMED || oldStatus == OrderStatus.SHIPPING)) {
  for (OrderItemEntity orderItem : order.getOrderItems()) {
    ProductEntity product = orderItem.getProduct();
    product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
    productRepository.save(product);
  }
}
```

### Auto-Complete Payment on Delivery
When COD order is marked as DELIVERED, payment status is automatically updated:
```java
if (status == OrderStatus.DELIVERED &&
    order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY &&
    order.getPaymentStatus() == PaymentStatus.PENDING) {
  order.setPaymentStatus(PaymentStatus.COMPLETED);
}
```

## Benefits

### For Business
✅ **Fraud Prevention**: Admin can verify orders before confirmation  
✅ **Quality Control**: Review order details before processing  
✅ **Inventory Management**: Prevent spam orders affecting stock  
✅ **Customer Service**: Opportunity to contact customer for clarification  

### For Customers
✅ **Clear Communication**: Email only sent after order is confirmed  
✅ **Reliable Status**: No false confirmations  
✅ **Professional Service**: Human review of orders  

### For Administrators
✅ **Full Control**: Can approve/reject orders  
✅ **Easy Management**: Simple status update triggers email  
✅ **Audit Trail**: Clear logs of confirmations  

## Testing Checklist

### COD Order Tests
- [ ] Create COD order as customer → Order created with PENDING status
- [ ] Verify no email sent immediately after order creation
- [ ] Admin updates status to CONFIRMED → Email sent to customer
- [ ] Admin updates status to SHIPPING → No additional email
- [ ] Admin updates status to DELIVERED → Payment status auto-completed
- [ ] Admin cancels PENDING order → Stock refunded
- [ ] Admin cancels CONFIRMED order → Stock refunded

### MoMo Order Tests
- [ ] Create MoMo order → Order created with PENDING status
- [ ] Complete MoMo payment → Order status changed to CONFIRMED
- [ ] Verify email sent after successful payment
- [ ] Cancel MoMo payment → Order remains PENDING
- [ ] Admin manually updates MoMo order status → Works normally

### Security Tests
- [ ] Customer tries to access /admin/orders → 403 Forbidden
- [ ] Customer tries to update order status → 403 Forbidden
- [ ] Admin accesses /admin/orders → Success
- [ ] Admin updates order status → Success

### Email Tests
- [ ] COD order confirmed by admin → Customer receives email
- [ ] MoMo payment successful → Customer receives email
- [ ] Email contains correct order details
- [ ] Email template renders properly

## Related Files
- `src/main/java/com/doan/bepsachviet_be/service/Impl/OrderServiceImpl.java`
- `src/main/java/com/doan/bepsachviet_be/controller/OrderController.java`
- `src/main/java/com/doan/bepsachviet_be/config/SecurityConfig.java`
- `src/main/java/com/doan/bepsachviet_be/controller/MomoController.java`

## Date
December 5, 2025

