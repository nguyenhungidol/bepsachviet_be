# Payment API Quick Reference

## 📦 Order APIs

### Create Order (User)
```http
POST /api/v1.0/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "deliveryName": "string",
  "deliveryPhone": "string",
  "deliveryAddress": "string",
  "notes": "string (optional)",
  "paymentMethod": "CASH_ON_DELIVERY" | "MOMO",
  "items": [
    {
      "productId": "string",
      "quantity": number
    }
  ]
}
```

**Response:**
```json
{
  "orderId": "uuid",
  "status": "PENDING",
  "paymentMethod": "CASH_ON_DELIVERY" | "MOMO",
  "paymentStatus": "PENDING",
  "totalAmount": number,
  "orderItems": [...],
  ...
}
```

---

### Get My Orders (User)
```http
GET /api/v1.0/orders/my-orders?page=0&size=10
Authorization: Bearer <token>
```

---

### Get Order Details (User)
```http
GET /api/v1.0/orders/{orderId}
Authorization: Bearer <token>
```

---

### Get All Orders (Admin)
```http
GET /api/v1.0/admin/orders?page=0&size=10&status=PENDING
Authorization: Bearer <admin-token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `status`: Filter by status (optional): PENDING | CONFIRMED | SHIPPING | DELIVERED | CANCELED

---

### Update Order Status (Admin)
```http
PATCH /api/v1.0/admin/orders/{orderId}/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "status": "PENDING" | "CONFIRMED" | "SHIPPING" | "DELIVERED" | "CANCELED"
}
```

**Status Transitions:**
- PENDING → CONFIRMED (via payment)
- PENDING/CONFIRMED → SHIPPING (admin manually)
- SHIPPING → DELIVERED (admin manually)
- Any status → CANCELED (admin manually)

**Side Effects:**
- Changing to DELIVERED (CASH_ON_DELIVERY) → Payment status → COMPLETED
- Changing to CANCELED → Stock restored (if from PENDING/CONFIRMED/SHIPPING)

---

## 💳 MoMo Payment APIs

### Create MoMo Payment (User)
```http
POST /api/v1.0/payment/momo/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": "uuid-from-order-creation"
}
```

**Response:**
```json
{
  "partnerCode": "MOMOLRJZ20181206",
  "requestId": "1701234567890",
  "orderId": "uuid",
  "amount": "50000",
  "payUrl": "https://test-payment.momo.vn/...",
  "message": "Successful.",
  "resultCode": 0
}
```

**Frontend Action:**
Redirect user to `payUrl`

---

### MoMo Return Handler (Public)
```http
GET /api/v1.0/payment/momo/return?orderId=...&resultCode=...
```

**Purpose:** MoMo redirects user here after payment
**Response:** JSON with payment status

---

### MoMo IPN Handler (Public - Webhook)
```http
POST /api/v1.0/payment/momo/ipn-handler
Content-Type: application/json

{
  "partnerCode": "string",
  "orderId": "string",
  "requestId": "string",
  "amount": "string",
  "resultCode": "string",
  "message": "string",
  "transId": "string",
  "signature": "string",
  ...
}
```

**Purpose:** MoMo calls this automatically after payment
**Note:** Must be publicly accessible (no auth required)

---

## 📊 Payment Status Flow

### CASH_ON_DELIVERY Flow:
```
1. User creates order → paymentStatus: PENDING
2. Admin: PENDING → SHIPPING
3. Admin: SHIPPING → DELIVERED → paymentStatus: COMPLETED ✅
```

### MOMO Flow:
```
1. User creates order → paymentStatus: PENDING, status: PENDING
2. User calls /payment/momo/create → Gets payUrl
3. User completes payment on MoMo
4. MoMo calls IPN handler → paymentStatus: COMPLETED, status: CONFIRMED ✅
5. Admin: CONFIRMED → SHIPPING → DELIVERED
```

---

## 🔐 Authentication Requirements

| Endpoint | Auth Required | Role |
|----------|---------------|------|
| POST /orders | ✅ Yes | USER |
| GET /orders/my-orders | ✅ Yes | USER |
| GET /orders/{orderId} | ✅ Yes | USER (own orders) |
| POST /payment/momo/create | ✅ Yes | USER |
| GET /admin/orders | ✅ Yes | ADMIN |
| PATCH /admin/orders/{orderId}/status | ✅ Yes | ADMIN |
| GET /payment/momo/return | ❌ No | Public |
| POST /payment/momo/ipn-handler | ❌ No | Public (MoMo webhook) |

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "error": "Order payment method is not MOMO"
}
```

### 404 Not Found
```json
{
  "error": "Order not found: order-id"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied"
}
```

---

## 🎯 Payment Method Values

```java
enum PaymentMethod {
  CASH_ON_DELIVERY,  // Pay when delivered
  MOMO              // Pay via MoMo e-wallet
}
```

---

## 📈 Order Status Values

```java
enum OrderStatus {
  PENDING,    // Created, awaiting payment/processing
  CONFIRMED,  // Payment completed (MOMO)
  SHIPPING,   // Being shipped
  DELIVERED,  // Delivered to customer
  CANCELED    // Cancelled by admin
}
```

---

## 💰 Payment Status Values

```java
enum PaymentStatus {
  PENDING,    // Not paid yet
  COMPLETED,  // Paid successfully
  FAILED,     // Payment failed
  REFUNDED    // Money returned (future use)
}
```

---

## 🧪 Test Credentials (MoMo Test Environment)

```properties
momo.partnerCode=MOMOLRJZ20181206
momo.accessKey=mTCKt9W3eU1m39TW
momo.secretKey=SetA5RDnLHvt51AULf51DyauxUo3kDU6
momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
```

**Test Cards:** Use MoMo test environment cards (check MoMo documentation)

---

## 🌐 Frontend Integration Example

```javascript
// 1. Create order
const orderResponse = await fetch('/api/v1.0/orders', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    deliveryName: 'John Doe',
    deliveryPhone: '0123456789',
    deliveryAddress: '123 Main St',
    paymentMethod: 'MOMO',
    items: [{productId: 'PROD-001', quantity: 2}]
  })
});

const order = await orderResponse.json();

// 2. If MOMO, create payment
if (order.paymentMethod === 'MOMO') {
  const paymentResponse = await fetch('/api/v1.0/payment/momo/create', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      orderId: order.orderId
    })
  });
  
  const payment = await paymentResponse.json();
  
  // 3. Redirect to MoMo
  window.location.href = payment.payUrl;
}

// 4. After MoMo redirect back, check order status
const updatedOrder = await fetch(`/api/v1.0/orders/${order.orderId}`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

## 📞 Support

For issues, check:
1. Backend logs
2. Database order state
3. MoMo IPN handler logs
4. Network requests in browser DevTools

