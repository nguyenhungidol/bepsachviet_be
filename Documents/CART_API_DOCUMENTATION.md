# Shopping Cart API Documentation

## Overview
This document describes the Shopping Cart REST API endpoints for the Bep Sach Viet application.

## Base URL
```
/api/v1.0/cart
```

## Authentication
All cart endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## API Endpoints

### 1. Get Cart
Get all items in the current user's cart.

**Endpoint:** `GET /api/v1.0/cart`

**Response:**
```json
{
  "cartId": 1,
  "userId": "user-uuid",
  "items": [
    {
      "itemId": 1,
      "productId": "product-uuid",
      "productName": "Product Name",
      "productDescription": "Product description",
      "productImageSrc": "https://example.com/image.jpg",
      "productPrice": 100000.00,
      "quantity": 2,
      "subtotal": 200000.00,
      "createdAt": "2025-11-26T10:00:00.000+00:00",
      "updatedAt": "2025-11-26T10:00:00.000+00:00"
    }
  ],
  "totalItems": 2,
  "totalPrice": 200000.00,
  "createdAt": "2025-11-26T10:00:00.000+00:00",
  "updatedAt": "2025-11-26T10:00:00.000+00:00"
}
```

---

### 2. Add Item to Cart
Add a product to the cart. If the product already exists in the cart, the quantity will be increased.

**Endpoint:** `POST /api/v1.0/cart/items`

**Request Body:**
```json
{
  "productId": "product-uuid",
  "quantity": 2
}
```

**Response:** (HTTP 201 Created)
```json
{
  "itemId": 1,
  "productId": "product-uuid",
  "productName": "Product Name",
  "productDescription": "Product description",
  "productImageSrc": "https://example.com/image.jpg",
  "productPrice": 100000.00,
  "quantity": 2,
  "subtotal": 200000.00,
  "createdAt": "2025-11-26T10:00:00.000+00:00",
  "updatedAt": "2025-11-26T10:00:00.000+00:00"
}
```

---

### 3. Update Cart Item Quantity
Update the quantity of a specific cart item.

**Endpoint:** `PUT /api/v1.0/cart/items/{itemId}`

**Path Parameters:**
- `itemId` (Long) - The ID of the cart item to update

**Request Body:**
```json
{
  "quantity": 5
}
```

**Response:**
```json
{
  "itemId": 1,
  "productId": "product-uuid",
  "productName": "Product Name",
  "productDescription": "Product description",
  "productImageSrc": "https://example.com/image.jpg",
  "productPrice": 100000.00,
  "quantity": 5,
  "subtotal": 500000.00,
  "createdAt": "2025-11-26T10:00:00.000+00:00",
  "updatedAt": "2025-11-26T10:30:00.000+00:00"
}
```

---

### 4. Remove Item from Cart
Remove a specific item from the cart.

**Endpoint:** `DELETE /api/v1.0/cart/items/{itemId}`

**Path Parameters:**
- `itemId` (Long) - The ID of the cart item to remove

**Response:** HTTP 204 No Content

---

### 5. Clear Cart
Remove all items from the cart (typically used after a successful order).

**Endpoint:** `DELETE /api/v1.0/cart`

**Response:** HTTP 204 No Content

---

### 6. Get Cart Count
Get the total number of items in the cart (useful for displaying on cart icon).

**Endpoint:** `GET /api/v1.0/cart/count`

**Response:**
```json
{
  "count": 5
}
```

---

### 7. Sync Cart from localStorage
Sync cart items from localStorage to the database when a user logs in. This merges localStorage cart with existing database cart.

**Endpoint:** `POST /api/v1.0/cart/sync`

**Request Body:**
```json
{
  "items": [
    {
      "productId": "product-uuid-1",
      "quantity": 2
    },
    {
      "productId": "product-uuid-2",
      "quantity": 1
    }
  ]
}
```

**Response:**
```json
{
  "cartId": 1,
  "userId": "user-uuid",
  "items": [
    {
      "itemId": 1,
      "productId": "product-uuid-1",
      "productName": "Product 1",
      "productDescription": "Description 1",
      "productImageSrc": "https://example.com/image1.jpg",
      "productPrice": 100000.00,
      "quantity": 2,
      "subtotal": 200000.00,
      "createdAt": "2025-11-26T10:00:00.000+00:00",
      "updatedAt": "2025-11-26T10:00:00.000+00:00"
    },
    {
      "itemId": 2,
      "productId": "product-uuid-2",
      "productName": "Product 2",
      "productDescription": "Description 2",
      "productImageSrc": "https://example.com/image2.jpg",
      "productPrice": 150000.00,
      "quantity": 1,
      "subtotal": 150000.00,
      "createdAt": "2025-11-26T10:00:00.000+00:00",
      "updatedAt": "2025-11-26T10:00:00.000+00:00"
    }
  ],
  "totalItems": 3,
  "totalPrice": 350000.00,
  "createdAt": "2025-11-26T10:00:00.000+00:00",
  "updatedAt": "2025-11-26T10:00:00.000+00:00"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-26T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product ID is required",
  "path": "/api/v1.0/cart/items"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-26T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "User not authenticated",
  "path": "/api/v1.0/cart"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-26T10:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "This cart item does not belong to you",
  "path": "/api/v1.0/cart/items/1"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-26T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/api/v1.0/cart/items"
}
```

---

## Database Schema

### Table: carts
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | Foreign key to users table (unique) |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### Table: cart_items
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| cart_id | BIGINT | Foreign key to carts table |
| product_id | BIGINT | Foreign key to products table |
| quantity | INTEGER | Quantity of the product |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

---

## Frontend Integration Guide

### For Visitors (Not Logged In)
Store cart in localStorage:
```javascript
// Structure
const cart = {
  items: [
    {
      productId: "product-uuid",
      quantity: 2
    }
  ]
};

localStorage.setItem('cart', JSON.stringify(cart));
```

### When User Logs In
1. Get cart from localStorage
2. Call sync endpoint to merge with database
3. Clear localStorage cart
4. Use database cart from now on

```javascript
// Example
async function syncCartOnLogin(jwtToken) {
  const localCart = JSON.parse(localStorage.getItem('cart') || '{"items":[]}');
  
  if (localCart.items.length > 0) {
    const response = await fetch('/api/v1.0/cart/sync', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}`
      },
      body: JSON.stringify(localCart)
    });
    
    if (response.ok) {
      localStorage.removeItem('cart');
      const mergedCart = await response.json();
      return mergedCart;
    }
  }
}
```

### For Logged In Users
Use API calls for all cart operations:

```javascript
// Add to cart
async function addToCart(productId, quantity, jwtToken) {
  const response = await fetch('/api/v1.0/cart/items', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({ productId, quantity })
  });
  return await response.json();
}

// Get cart
async function getCart(jwtToken) {
  const response = await fetch('/api/v1.0/cart', {
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  return await response.json();
}

// Get cart count
async function getCartCount(jwtToken) {
  const response = await fetch('/api/v1.0/cart/count', {
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  const data = await response.json();
  return data.count;
}

// Update item quantity
async function updateCartItem(itemId, quantity, jwtToken) {
  const response = await fetch(`/api/v1.0/cart/items/${itemId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({ quantity })
  });
  return await response.json();
}

// Remove item
async function removeCartItem(itemId, jwtToken) {
  await fetch(`/api/v1.0/cart/items/${itemId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
}

// Clear cart (after successful order)
async function clearCart(jwtToken) {
  await fetch('/api/v1.0/cart', {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
}
```

---

## Implementation Details

### Features
- ✅ Cart persistence per user
- ✅ Automatic cart creation for new users
- ✅ Merge functionality for localStorage sync
- ✅ Automatic quantity increment for existing items
- ✅ Security validation (users can only access their own cart)
- ✅ Transaction support for data consistency
- ✅ Orphan removal for cart items
- ✅ Lazy/Eager loading optimization

### Business Logic
- When adding a product that already exists in cart, quantities are summed
- Sync operation adds localStorage quantities to existing database quantities
- Cart is automatically created when user first adds an item
- All cart operations are transactional
- Users can only modify their own cart items

---

## Testing the API

### Using cURL

```bash
# Get cart
curl -X GET "http://localhost:8080/api/v1.0/cart" \
  -H "Authorization: Bearer <your-jwt-token>"

# Add item to cart
curl -X POST "http://localhost:8080/api/v1.0/cart/items" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"product-uuid","quantity":2}'

# Update cart item
curl -X PUT "http://localhost:8080/api/v1.0/cart/items/1" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"quantity":5}'

# Get cart count
curl -X GET "http://localhost:8080/api/v1.0/cart/count" \
  -H "Authorization: Bearer <your-jwt-token>"

# Sync cart
curl -X POST "http://localhost:8080/api/v1.0/cart/sync" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":"product-uuid","quantity":2}]}'

# Remove item
curl -X DELETE "http://localhost:8080/api/v1.0/cart/items/1" \
  -H "Authorization: Bearer <your-jwt-token>"

# Clear cart
curl -X DELETE "http://localhost:8080/api/v1.0/cart" \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## Notes
- All timestamps are in ISO 8601 format
- Prices use BigDecimal for precision (up to 4 decimal places)
- Cart is automatically linked to the authenticated user via JWT
- Product information is embedded in cart responses to avoid additional lookups

