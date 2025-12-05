# Inventory Management Documentation

## Overview
The inventory management feature has been implemented to track product stock quantities and prevent overselling. This feature includes stock validation during cart operations and automatic inventory updates when orders are placed or cancelled.

## Database Changes

### ProductEntity
Added a new field to track inventory:
- **stockQuantity** (Integer, nullable=false, default=0): The current available stock quantity for the product

```sql
ALTER TABLE products ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;
```

## API Changes

### Product API

#### ProductRequest (Create/Update)
Added new field:
```json
{
  "name": "Product Name",
  "description": "Description",
  "imageSrc": "image-url",
  "price": 100.00,
  "ocUrl": "ocop-url",
  "stockQuantity": 50,  // NEW: Initial or updated stock quantity
  "categoryId": "category-uuid"
}
```

#### ProductResponse
Added new field:
```json
{
  "productId": "uuid",
  "name": "Product Name",
  "description": "Description",
  "imageSrc": "image-url",
  "price": 100.00,
  "ocUrl": "ocop-url",
  "stockQuantity": 50,  // NEW: Current stock quantity
  "categoryId": "category-uuid",
  "categoryName": "Category Name",
  "createdAt": "2025-12-02T10:00:00",
  "updatedAt": "2025-12-02T10:00:00"
}
```

## Business Logic

### Cart Operations

#### 1. Add to Cart
**Endpoint:** `POST /api/cart/items`

**Validation:**
- Checks if product is out of stock (stockQuantity <= 0)
- Validates that requested quantity doesn't exceed available stock
- If item already exists in cart, validates total quantity (existing + new)

**Error Responses:**
```json
{
  "message": "Product is out of stock",
  "status": 400
}
```
```json
{
  "message": "Insufficient stock. Available: 10, requested: 15",
  "status": 400
}
```

#### 2. Update Cart Item
**Endpoint:** `PUT /api/cart/items/{itemId}`

**Validation:**
- Checks if product is out of stock
- Validates that new quantity doesn't exceed available stock

**Error Responses:**
Same as Add to Cart

#### 3. Sync Cart
**Endpoint:** `POST /api/cart/sync`

**Validation:**
- Skips out of stock products
- Caps quantity at available stock if requested quantity exceeds it
- Continues processing other items even if one fails validation

### Order Operations

#### 1. Create Order
**Endpoint:** `POST /api/orders`

**Process:**
1. **Pre-validation Phase:**
   - Validates all requested products exist
   - Checks stock availability for ALL items before processing any
   - If any item has insufficient stock, entire order is rejected (atomic operation)

2. **Order Creation Phase:**
   - Creates order with PENDING status
   - For each order item:
     - Calculates subtotal
     - **Decrements product stock quantity**
     - Creates order item record

3. **Stock Decrement:**
   ```
   newStockQuantity = currentStockQuantity - orderedQuantity
   ```

**Error Responses:**
```json
{
  "message": "Insufficient stock for product: Product Name. Available: 5, requested: 10",
  "status": 400
}
```

**Important:** Stock is decremented immediately when order is placed (with PENDING status), not when order is confirmed or delivered.

#### 2. Update Order Status
**Endpoint:** `PUT /api/orders/{orderId}/status`

**Stock Restoration:**
When order status changes to CANCELED and previous status was PENDING or SHIPPING:
- Restores stock for all order items
- Restoration formula:
  ```
  newStockQuantity = currentStockQuantity + orderedQuantity
  ```

**Status Flow:**
```
PENDING -> SHIPPING -> DELIVERED (no stock changes)
PENDING -> CANCELED (stock restored)
SHIPPING -> CANCELED (stock restored)
DELIVERED -> CANCELED (NO stock restoration - product already shipped)
```

## Testing Scenarios

### Test Case 1: Normal Purchase Flow
1. Product has stock: 100
2. User adds 10 to cart → Success
3. User updates to 15 in cart → Success
4. User places order for 15 → Stock becomes 85
5. Order delivered → Stock remains 85

### Test Case 2: Out of Stock
1. Product has stock: 5
2. User tries to add 10 to cart → Error: Insufficient stock
3. User adds 5 to cart → Success
4. Another user places order for 3 → Stock becomes 2
5. First user tries to checkout with 5 → Error: Insufficient stock

### Test Case 3: Order Cancellation
1. Product has stock: 50
2. User places order for 20 → Stock becomes 30
3. Admin cancels order (status: PENDING) → Stock restored to 50

### Test Case 4: Multiple Items Order
1. Product A stock: 10, Product B stock: 5
2. User orders: Product A (5), Product B (10)
3. Order rejected → Error: Insufficient stock for Product B
4. Stock unchanged for both products (atomic operation)

## Frontend Integration

### Display Stock Status
```javascript
const ProductCard = ({
  imageSrc,
  name,
  price,
  ocUrl,
  stockQuantity  // NEW FIELD
}) => (
  <div className="product-card">
    <div className="product-image-container">
      <img src={imageSrc} alt={name} className="product-image" />
    </div>
    <div className="product-info">
      <p className="product-name">{name}</p>
      {ocUrl && (
        <div className="product-rating">
          <img src={ocUrl} alt="OCOP Rating" className="rating-image" />
        </div>
      )}
      <p className="product-price">SELLING PRICE: {price}</p>
      
      {/* NEW: Stock status display */}
      {stockQuantity <= 0 && (
        <p className="out-of-stock">Out of Stock</p>
      )}
      {stockQuantity > 0 && stockQuantity <= 10 && (
        <p className="low-stock">Only {stockQuantity} left!</p>
      )}
      {stockQuantity > 10 && (
        <p className="in-stock">In Stock</p>
      )}
      
      {/* Disable add to cart if out of stock */}
      <button 
        disabled={stockQuantity <= 0}
        onClick={() => addToCart()}
      >
        {stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
      </button>
    </div>
  </div>
);
```

### Handle Stock Errors
```javascript
try {
  await addToCart(productId, quantity);
} catch (error) {
  if (error.response?.status === 400) {
    // Display error message to user
    alert(error.response.data.message);
    // Example: "Insufficient stock. Available: 5, requested: 10"
  }
}
```

### Update Cart Quantity with Validation
```javascript
const handleQuantityChange = async (itemId, newQuantity) => {
  try {
    await updateCartItem(itemId, newQuantity);
  } catch (error) {
    if (error.response?.status === 400) {
      // Show stock limitation message
      const message = error.response.data.message;
      alert(message);
      
      // Optionally, extract available quantity and set max
      const match = message.match(/Available: (\d+)/);
      if (match) {
        const available = parseInt(match[1]);
        // Update UI to show max available
      }
    }
  }
};
```

## Admin Features

### Inventory Management
Admins can:
1. **Set initial stock** when creating a product
2. **Update stock** when receiving new inventory
3. **View current stock** in product listings
4. **Monitor low stock** products
5. **Track stock changes** through order history

### Recommended Admin UI
```javascript
// Product Management Table
<table>
  <thead>
    <tr>
      <th>Product</th>
      <th>Price</th>
      <th>Stock</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    {products.map(product => (
      <tr key={product.productId}>
        <td>{product.name}</td>
        <td>{product.price}</td>
        <td>
          <input 
            type="number" 
            value={product.stockQuantity}
            onChange={(e) => updateStock(product.productId, e.target.value)}
          />
        </td>
        <td>
          {product.stockQuantity <= 0 && <span className="badge-danger">Out of Stock</span>}
          {product.stockQuantity > 0 && product.stockQuantity <= 10 && 
            <span className="badge-warning">Low Stock</span>}
          {product.stockQuantity > 10 && <span className="badge-success">In Stock</span>}
        </td>
        <td>
          <button onClick={() => restockProduct(product.productId)}>
            Restock
          </button>
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

## Database Migration

If you already have existing products in the database, run this SQL to set default stock:

```sql
-- Set default stock to 0 for all existing products
UPDATE products SET stock_quantity = 0 WHERE stock_quantity IS NULL;

-- Or set a default stock value for all products
UPDATE products SET stock_quantity = 100 WHERE stock_quantity IS NULL;
```

## Best Practices

1. **Always validate stock** before allowing add to cart
2. **Use atomic transactions** for order creation to prevent race conditions
3. **Display stock status** prominently on product pages
4. **Notify users** when products become available again
5. **Implement low stock alerts** for admin dashboard
6. **Log stock changes** for audit purposes (future enhancement)
7. **Consider implementing** stock reservation system for checkout process (future enhancement)

## Future Enhancements

1. **Stock Reservation**: Reserve stock during checkout process (15-minute window)
2. **Stock History**: Track all stock changes with timestamps and reasons
3. **Low Stock Notifications**: Email alerts when stock falls below threshold
4. **Automatic Reordering**: Suggest reorder when stock is low
5. **Warehouse Management**: Support multiple warehouse locations
6. **Stock Forecasting**: Predict stock needs based on sales patterns

## API Summary

### Stock Check Flow
```
User Action → Stock Validation → Operation
   ↓              ↓                  ↓
Add to Cart → Check Available → Update Cart or Error
Update Cart → Check Available → Update or Error  
Place Order → Check Available → Decrement Stock or Error
Cancel Order → No Check → Restore Stock (if applicable)
```

### Error Codes
- **400 Bad Request**: Insufficient stock or product out of stock
- **404 Not Found**: Product not found
- **500 Internal Server Error**: Database or system error

## Conclusion

The inventory management system ensures that:
- ✅ Products cannot be oversold
- ✅ Stock is accurately tracked
- ✅ Users receive clear feedback about stock availability
- ✅ Cancelled orders restore inventory
- ✅ Admin can manage stock levels easily
- ✅ Transactions are atomic and consistent

