# Order API - Quick Reference

## Endpoints at a Glance

### User Endpoints
```
POST   /orders                    - Create new order
GET    /orders/my-orders          - Get my order history (paginated)
GET    /orders/{orderId}          - Get order details
```

### Admin Endpoints
```
GET    /admin/orders              - Get all orders (paginated, filterable)
GET    /admin/orders/{orderId}    - Get order details
PATCH  /admin/orders/{orderId}/status - Update order status
```

---

## Quick Start

### 1. Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryName": "John Doe",
    "deliveryPhone": "0123456789",
    "deliveryAddress": "123 Main St",
    "items": [
      {"productId": "uuid-123", "quantity": 2}
    ]
  }'
```

### 2. Get My Orders
```bash
curl -X GET "http://localhost:8080/orders/my-orders?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Update Order Status (Admin)
```bash
curl -X PATCH http://localhost:8080/admin/orders/{orderId}/status \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPING"}'
```

---

## Order Status Flow

```
PENDING → SHIPPING → DELIVERED
   ↓          ↓
CANCELED   CANCELED
```

**Status Values:**
- `PENDING` - Order placed, awaiting processing
- `SHIPPING` - Order is being delivered
- `DELIVERED` - Order completed successfully
- `CANCELED` - Order canceled

---

## Request/Response Examples

### Create Order Request
```json
{
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi",
  "notes": "Call before delivery",
  "items": [
    {"productId": "uuid-product-1", "quantity": 2},
    {"productId": "uuid-product-2", "quantity": 1}
  ]
}
```

### Order Response
```json
{
  "id": 1,
  "orderId": "uuid-order-123",
  "userId": "uuid-user-456",
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi",
  "status": "PENDING",
  "totalAmount": 500000,
  "orderItems": [
    {
      "id": 1,
      "productId": "uuid-product-1",
      "productName": "Gà Ủ Muối",
      "productImage": "https://...",
      "quantity": 2,
      "price": 150000,
      "subtotal": 300000
    }
  ],
  "createdAt": "2025-12-02T10:30:00",
  "updatedAt": "2025-12-02T10:30:00"
}
```

---

## Frontend Integration

### React - Create Order
```javascript
const createOrder = async (orderData) => {
  const response = await fetch('http://localhost:8080/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(orderData)
  });
  return await response.json();
};
```

### React - Get Orders
```javascript
const getMyOrders = async (page = 0) => {
  const response = await fetch(
    `http://localhost:8080/orders/my-orders?page=${page}&size=10`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  return await response.json();
};
```

### React - Update Status (Admin)
```javascript
const updateStatus = async (orderId, status) => {
  const response = await fetch(
    `http://localhost:8080/admin/orders/${orderId}/status`,
    {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminToken}`
      },
      body: JSON.stringify({ status })
    }
  );
  return await response.json();
};
```

---

## Common Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-based) |
| size | int | 10 | Items per page |
| status | OrderStatus | null | Filter by status (admin only) |

**Example:**
```
GET /admin/orders?status=PENDING&page=0&size=20
```

---

## Security

**Authentication:** Required for all endpoints (JWT Bearer token)

**Authorization:**
- `/orders/**` - Authenticated users
- `/admin/orders/**` - ADMIN role required

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## Error Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Success |
| 201 | Created - Order created |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Invalid/missing token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Order/Product not found |

---

## Files Structure

```
entity/
├── OrderEntity.java
└── OrderItemEntity.java

constant/
└── OrderStatus.java

repository/
├── OrderRepository.java
└── OrderItemRepository.java

service/
├── OrderService.java
└── Impl/OrderServiceImpl.java

controller/
└── OrderController.java

io/
├── Request/
│   ├── CreateOrderRequest.java
│   ├── OrderItemRequest.java
│   └── UpdateOrderStatusRequest.java
└── Response/
    ├── OrderResponse.java
    └── OrderItemResponse.java
```

---

## Testing Checklist

- [ ] Create order as user
- [ ] View my orders as user
- [ ] View all orders as admin
- [ ] Filter orders by status as admin
- [ ] Update order status as admin
- [ ] Verify pagination works
- [ ] Test authentication (401 error)
- [ ] Test authorization (403 error)
- [ ] Test with invalid product ID (400 error)
- [ ] Test with non-existent order (404 error)

---

## Related Documentation

- **ORDER_API_DOCUMENTATION.md** - Full API reference
- **ORDER_IMPLEMENTATION_SUMMARY.md** - Technical details
- **CART_API_DOCUMENTATION.md** - Cart integration
- **POST_API_DOCUMENTATION.md** - Similar pattern example

---

**Quick Links:**
- Base URL: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Swagger (if enabled): `http://localhost:8080/swagger-ui.html`

---

**Last Updated:** December 2, 2025

