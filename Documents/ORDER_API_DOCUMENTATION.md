# Order API Documentation

## Overview
The Order API provides comprehensive order management functionality for both users and administrators. Users can create and view their orders, while administrators have full access to manage all orders including status updates.

## Features
- ✅ Create orders from cart items
- ✅ View order history for users
- ✅ Admin dashboard to view all orders
- ✅ Filter orders by status (PENDING, SHIPPING, DELIVERED, CANCELED)
- ✅ Update order status (Admin only)
- ✅ Pagination support
- ✅ Complete order details with customer and product information

---

## Entities

### OrderEntity
- **id**: Long (Primary Key)
- **orderId**: String (Unique identifier - UUID)
- **user**: UserEntity (Customer who placed the order)
- **orderItems**: List<OrderItemEntity> (Products in the order)
- **status**: OrderStatus (PENDING, SHIPPING, DELIVERED, CANCELED)
- **totalAmount**: BigDecimal (Total order value)
- **deliveryAddress**: String (Shipping address)
- **deliveryPhone**: String (Contact number)
- **deliveryName**: String (Recipient name)
- **notes**: String (Optional order notes)
- **createdAt**: Timestamp (Order creation date)
- **updatedAt**: Timestamp (Last update date)

### OrderItemEntity
- **id**: Long (Primary Key)
- **order**: OrderEntity (Parent order)
- **product**: ProductEntity (Ordered product)
- **quantity**: Integer (Number of items)
- **price**: BigDecimal (Product price at time of order)
- **subtotal**: BigDecimal (quantity × price)
- **createdAt**: Timestamp

### OrderStatus Enum
```java
public enum OrderStatus {
  PENDING,    // Order placed, awaiting processing
  SHIPPING,   // Order is being delivered
  DELIVERED,  // Order successfully delivered
  CANCELED    // Order canceled
}
```

---

## API Endpoints

### 1. Create Order (User)
**POST** `/orders`

Creates a new order for the authenticated user.

**Authentication Required**: Yes (User)

**Request Body**:
```json
{
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi, Vietnam",
  "notes": "Please call before delivery",
  "items": [
    {
      "productId": "uuid-product-1",
      "quantity": 2
    },
    {
      "productId": "uuid-product-2",
      "quantity": 1
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "orderId": "uuid-order-123",
  "userId": "uuid-user-456",
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi, Vietnam",
  "notes": "Please call before delivery",
  "status": "PENDING",
  "totalAmount": 500000,
  "orderItems": [
    {
      "id": 1,
      "productId": "uuid-product-1",
      "productName": "Gà Ủ Muối",
      "productImage": "https://s3.amazonaws.com/...",
      "quantity": 2,
      "price": 150000,
      "subtotal": 300000
    },
    {
      "id": 2,
      "productId": "uuid-product-2",
      "productName": "Vịt Quay",
      "productImage": "https://s3.amazonaws.com/...",
      "quantity": 1,
      "price": 200000,
      "subtotal": 200000
    }
  ],
  "createdAt": "2025-12-02T10:30:00",
  "updatedAt": "2025-12-02T10:30:00"
}
```

**Validation**:
- `deliveryName`: Required, not blank
- `deliveryPhone`: Required, not blank
- `deliveryAddress`: Required, not blank
- `items`: Required, not empty
- `items[].productId`: Required, not blank
- `items[].quantity`: Required, minimum 1

---

### 2. Get My Orders (User)
**GET** `/orders/my-orders?page=0&size=10`

Retrieves all orders for the authenticated user with pagination.

**Authentication Required**: Yes (User)

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Items per page (default: 10)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "orderId": "uuid-order-123",
      "userId": "uuid-user-456",
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "deliveryName": "John Doe",
      "deliveryPhone": "0123456789",
      "deliveryAddress": "123 Main St, Hanoi, Vietnam",
      "notes": "Please call before delivery",
      "status": "SHIPPING",
      "totalAmount": 500000,
      "orderItems": [...],
      "createdAt": "2025-12-02T10:30:00",
      "updatedAt": "2025-12-02T11:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

---

### 3. Get Order by ID (User)
**GET** `/orders/{orderId}`

Retrieves detailed information about a specific order.

**Authentication Required**: Yes (User)

**Path Parameters**:
- `orderId`: Order UUID

**Response** (200 OK):
```json
{
  "id": 1,
  "orderId": "uuid-order-123",
  "userId": "uuid-user-456",
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi, Vietnam",
  "notes": "Please call before delivery",
  "status": "PENDING",
  "totalAmount": 500000,
  "orderItems": [...],
  "createdAt": "2025-12-02T10:30:00",
  "updatedAt": "2025-12-02T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found"
}
```

---

### 4. Get All Orders - Admin
**GET** `/admin/orders?page=0&size=10&status=PENDING`

Retrieves all orders with optional status filtering and pagination.

**Authentication Required**: Yes (Admin only)

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Items per page (default: 10)
- `status`: Optional filter (PENDING, SHIPPING, DELIVERED, CANCELED)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "orderId": "uuid-order-123",
      "userId": "uuid-user-456",
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "deliveryName": "John Doe",
      "deliveryPhone": "0123456789",
      "deliveryAddress": "123 Main St, Hanoi, Vietnam",
      "notes": "Please call before delivery",
      "status": "PENDING",
      "totalAmount": 500000,
      "orderItems": [...],
      "createdAt": "2025-12-02T10:30:00",
      "updatedAt": "2025-12-02T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 15,
  "totalPages": 2,
  "last": false,
  "first": true
}
```

**Example Requests**:
```bash
# Get all orders
GET /admin/orders

# Get pending orders only
GET /admin/orders?status=PENDING

# Get shipping orders with pagination
GET /admin/orders?status=SHIPPING&page=0&size=20
```

---

### 5. Get Order by ID - Admin
**GET** `/admin/orders/{orderId}`

Admin endpoint to view any order details.

**Authentication Required**: Yes (Admin only)

**Path Parameters**:
- `orderId`: Order UUID

**Response**: Same as user endpoint

---

### 6. Update Order Status - Admin
**PATCH** `/admin/orders/{orderId}/status`

Updates the status of an order. This is the primary way admins manage order lifecycle.

**Authentication Required**: Yes (Admin only)

**Path Parameters**:
- `orderId`: Order UUID

**Request Body**:
```json
{
  "status": "SHIPPING"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "orderId": "uuid-order-123",
  "userId": "uuid-user-456",
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "deliveryName": "John Doe",
  "deliveryPhone": "0123456789",
  "deliveryAddress": "123 Main St, Hanoi, Vietnam",
  "notes": "Please call before delivery",
  "status": "SHIPPING",
  "totalAmount": 500000,
  "orderItems": [...],
  "createdAt": "2025-12-02T10:30:00",
  "updatedAt": "2025-12-02T11:30:00"
}
```

**Valid Status Transitions**:
- `PENDING` → `SHIPPING` (Start delivery)
- `PENDING` → `CANCELED` (Cancel before processing)
- `SHIPPING` → `DELIVERED` (Successful delivery)
- `SHIPPING` → `CANCELED` (Delivery failed/canceled)

**Validation**:
- `status`: Required, must be valid OrderStatus enum value

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found"
}
```

---

## Security Configuration

The Order API endpoints are protected as follows:

```java
// User endpoints - require authentication
.requestMatchers("/orders/**").authenticated()

// Admin endpoints - require ADMIN role
.requestMatchers("/admin/**").hasRole("ADMIN")
```

**User Permissions**:
- Create orders
- View own orders
- View order details

**Admin Permissions**:
- View all orders
- Filter orders by status
- Update order status
- View any order details

---

## Usage Examples

### Frontend Integration (React/JavaScript)

#### 1. Create Order
```javascript
const createOrder = async (orderData) => {
  const response = await fetch('http://localhost:8080/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`
    },
    body: JSON.stringify({
      deliveryName: orderData.name,
      deliveryPhone: orderData.phone,
      deliveryAddress: orderData.address,
      notes: orderData.notes,
      items: cartItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    })
  });
  
  if (response.ok) {
    const order = await response.json();
    console.log('Order created:', order);
    return order;
  }
};
```

#### 2. Get User Orders
```javascript
const fetchMyOrders = async (page = 0, size = 10) => {
  const response = await fetch(
    `http://localhost:8080/orders/my-orders?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${userToken}`
      }
    }
  );
  
  const data = await response.json();
  return data;
};
```

#### 3. Admin: Update Order Status
```javascript
const updateOrderStatus = async (orderId, newStatus) => {
  const response = await fetch(
    `http://localhost:8080/admin/orders/${orderId}/status`,
    {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminToken}`
      },
      body: JSON.stringify({ status: newStatus })
    }
  );
  
  if (response.ok) {
    const updatedOrder = await response.json();
    console.log('Order updated:', updatedOrder);
    return updatedOrder;
  }
};
```

#### 4. Admin: Get Orders by Status
```javascript
const fetchOrdersByStatus = async (status, page = 0, size = 10) => {
  const url = status 
    ? `http://localhost:8080/admin/orders?status=${status}&page=${page}&size=${size}`
    : `http://localhost:8080/admin/orders?page=${page}&size=${size}`;
    
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
  
  const data = await response.json();
  return data;
};
```

---

## Order Workflow

### User Flow:
1. **Browse Products** → Add items to cart
2. **Checkout** → Fill delivery information
3. **Create Order** → POST `/orders` (Cart items → Order items)
4. **View Orders** → GET `/orders/my-orders`
5. **Track Order** → GET `/orders/{orderId}` (Check status)

### Admin Flow:
1. **View All Orders** → GET `/admin/orders`
2. **Filter by Status** → GET `/admin/orders?status=PENDING`
3. **View Order Details** → GET `/admin/orders/{orderId}`
4. **Process Order** → PATCH `/admin/orders/{orderId}/status` (PENDING → SHIPPING)
5. **Complete Order** → PATCH `/admin/orders/{orderId}/status` (SHIPPING → DELIVERED)

---

## Database Schema

```sql
-- Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    delivery_phone VARCHAR(50) NOT NULL,
    delivery_name VARCHAR(255) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

---

## Testing

### Postman/cURL Examples

#### Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "deliveryName": "John Doe",
    "deliveryPhone": "0123456789",
    "deliveryAddress": "123 Main St, Hanoi",
    "notes": "Call before delivery",
    "items": [
      {"productId": "product-uuid-1", "quantity": 2},
      {"productId": "product-uuid-2", "quantity": 1}
    ]
  }'
```

#### Get My Orders
```bash
curl -X GET "http://localhost:8080/orders/my-orders?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Update Order Status (Admin)
```bash
curl -X PATCH http://localhost:8080/admin/orders/ORDER_UUID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{"status": "SHIPPING"}'
```

#### Get Orders by Status (Admin)
```bash
curl -X GET "http://localhost:8080/admin/orders?status=PENDING&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Error Handling

### Common Error Responses

**400 Bad Request** - Validation Error
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product not found: product-uuid-123"
}
```

**401 Unauthorized** - Missing/Invalid Token
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

**403 Forbidden** - Insufficient Permissions
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**404 Not Found** - Resource Not Found
```json
{
  "timestamp": "2025-12-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found"
}
```

---

## Notes

1. **Order Creation**: When an order is created, product prices are captured at that moment to preserve historical accuracy.

2. **Status Management**: Order status updates should follow logical transitions. Implement business logic to prevent invalid transitions if needed.

3. **Pagination**: Always use pagination for order lists to improve performance, especially for admins viewing all orders.

4. **Authentication**: All endpoints require authentication. Admin endpoints additionally require ADMIN role.

5. **Order ID**: Uses UUID format for security and uniqueness across distributed systems.

6. **Total Amount**: Automatically calculated from order items during creation.

7. **Delivery Information**: Required fields ensure orders can be properly fulfilled.

---

## Future Enhancements

Potential features to consider:
- Order cancellation by users (before shipping)
- Order history tracking (status change logs)
- Email notifications on status changes
- Payment integration
- Invoice generation
- Return/refund management
- Order search functionality
- Export orders to CSV/Excel
- Real-time order tracking
- Estimated delivery date

---

## Related Documentation

- [Cart API Documentation](CART_API_DOCUMENTATION.md)
- [Product API Documentation](POST_API_DOCUMENTATION.md)
- [User Profile API Documentation](PASSWORD_RESET_AND_USER_PROFILE_API_DOCUMENTATION.md)

---

**Last Updated**: December 2, 2025  
**API Version**: 1.0  
**Base URL**: `http://localhost:8080`

