# Soft Delete - Frontend Quick Reference

## 🚀 Quick Start for Frontend Developers

### What Changed?

**Cart API Response** now includes:
```json
{
  "itemId": 123,
  "productId": "P001",
  "productName": "Gà Ủ Muối",
  "productPrice": 150000,
  "quantity": 2,
  "subtotal": 300000,
  "isProductActive": true,      // ✨ NEW - Check this!
  "availableStock": 10,         // ✨ NEW - Current stock
  "productImageSrc": "https://...",
  "createdAt": "2025-12-06T10:00:00"
}
```

---

## 📝 Implementation Tasks

### 1. Cart Component - Display Unavailable Items

```jsx
// Cart.jsx
const CartItem = ({ item }) => {
  const isUnavailable = !item.isProductActive;
  const isOutOfStock = item.availableStock < item.quantity;
  
  return (
    <div className={`cart-item ${isUnavailable ? 'unavailable' : ''}`}>
      <img src={item.productImageSrc} alt={item.productName} />
      
      <div className="item-details">
        <h3>{item.productName}</h3>
        
        {/* Show warning badges */}
        {isUnavailable && (
          <span className="badge badge-danger">
            ❌ No Longer Available
          </span>
        )}
        
        {!isUnavailable && isOutOfStock && (
          <span className="badge badge-warning">
            ⚠️ Only {item.availableStock} in stock
          </span>
        )}
        
        <p>Price: {formatCurrency(item.productPrice)}</p>
        <p>Quantity: {item.quantity}</p>
        
        {!isUnavailable && (
          <button onClick={() => updateQuantity(item.itemId, item.quantity + 1)}>
            +
          </button>
        )}
      </div>
      
      <button 
        className="btn-remove" 
        onClick={() => removeItem(item.itemId)}
      >
        {isUnavailable ? 'Remove Unavailable Item' : 'Remove'}
      </button>
    </div>
  );
};
```

### 2. Cart Component - Disable Checkout

```jsx
// Cart.jsx
const Cart = ({ cartItems }) => {
  // Check if any items are unavailable or out of stock
  const hasUnavailableItems = cartItems.some(item => 
    !item.isProductActive || item.availableStock < item.quantity
  );
  
  const getCheckoutButtonText = () => {
    if (hasUnavailableItems) {
      return "Remove unavailable items to checkout";
    }
    return `Checkout (${cartItems.length} items)`;
  };
  
  return (
    <div className="cart">
      <h2>Shopping Cart</h2>
      
      {/* Show warning if there are unavailable items */}
      {hasUnavailableItems && (
        <div className="alert alert-warning">
          ⚠️ Some items in your cart are no longer available or out of stock.
          Please remove them before checkout.
        </div>
      )}
      
      {/* Cart Items */}
      {cartItems.map(item => (
        <CartItem key={item.itemId} item={item} />
      ))}
      
      {/* Checkout Button */}
      <button 
        className="btn-checkout"
        disabled={hasUnavailableItems || cartItems.length === 0}
        onClick={handleCheckout}
      >
        {getCheckoutButtonText()}
      </button>
    </div>
  );
};
```

### 3. Checkout Error Handling

```javascript
// checkoutService.js
export const createOrder = async (orderData) => {
  try {
    const response = await apiClient.post('/api/v1.0/orders', orderData);
    return response.data;
  } catch (error) {
    if (error.response?.status === 400) {
      const message = error.response.data.message || 'Order creation failed';
      
      // Check if it's a product availability error
      if (message.includes('no longer available') || 
          message.includes('Insufficient stock')) {
        // Show error and refresh cart to get updated status
        alert(`⚠️ ${message}\n\nYour cart will be refreshed.`);
        
        // Refresh cart to show updated availability
        window.location.reload(); // or use your state management
        return null;
      }
      
      throw new Error(message);
    }
    throw error;
  }
};
```

### 4. Add to Cart Error Handling

```javascript
// productService.js
export const addToCart = async (productId, quantity) => {
  try {
    const response = await apiClient.post('/api/v1.0/cart/items', {
      productId,
      quantity
    });
    return response.data;
  } catch (error) {
    if (error.response?.status === 400) {
      const message = error.response.data.message;
      
      if (message.includes('no longer available')) {
        alert('❌ This product is no longer available');
      } else if (message.includes('out of stock')) {
        alert('⚠️ This product is currently out of stock');
      } else if (message.includes('Insufficient stock')) {
        alert(`⚠️ ${message}`);
      } else {
        alert(`Error: ${message}`);
      }
      
      return null;
    }
    throw error;
  }
};
```

---

## 🎨 CSS Styling Suggestions

```css
/* Cart Item - Unavailable */
.cart-item.unavailable {
  opacity: 0.6;
  background-color: #f8f9fa;
  border: 2px dashed #dc3545;
}

.cart-item.unavailable img {
  filter: grayscale(100%);
}

/* Badges */
.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  margin: 4px 0;
}

.badge-danger {
  background-color: #dc3545;
  color: white;
}

.badge-warning {
  background-color: #ffc107;
  color: #000;
}

/* Alert */
.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.alert-warning {
  background-color: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
}

/* Checkout Button - Disabled */
.btn-checkout:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}
```

---

## 🧪 Testing Scenarios

### Test 1: Product Deleted While in Cart
1. Add product to cart
2. Admin deletes the product
3. Refresh cart page
4. **Expected**: Item shows "No Longer Available" badge
5. **Expected**: Checkout button is disabled

### Test 2: Product Out of Stock
1. Product has 5 in stock
2. Add 3 to cart
3. Another user buys 3 (stock becomes 2)
4. Try to checkout with your 3 items
5. **Expected**: Error message about insufficient stock
6. **Expected**: Cart refreshes showing available stock

### Test 3: Try to Add Deleted Product
1. Admin deletes a product
2. Try to add it to cart from product page
3. **Expected**: Product not found or 400 error

### Test 4: Checkout Validation
1. Have valid items in cart
2. Admin deletes one product
3. Try to checkout
4. **Expected**: Error message with product name
5. **Expected**: Cart refreshes automatically

---

## 📋 Checklist for Frontend Team

- [ ] Update Cart component to display `isProductActive` badge
- [ ] Add "Remove unavailable item" button styling
- [ ] Implement checkout button disable logic
- [ ] Add error handling for 400 responses during checkout
- [ ] Add error handling for 400 responses when adding to cart
- [ ] Show available stock quantity in cart
- [ ] Test with deleted products
- [ ] Test with out-of-stock products
- [ ] Add CSS for unavailable items (grayed out)
- [ ] Add alert/notification for unavailable items

---

## 🔍 API Endpoints Reference

### Get Cart
```
GET /api/v1.0/cart
Authorization: Bearer <token>

Response includes isProductActive and availableStock
```

### Add to Cart
```
POST /api/v1.0/cart/items
Authorization: Bearer <token>
Body: { "productId": "P001", "quantity": 2 }

Possible Errors:
- 400: "Product is no longer available"
- 400: "Product is out of stock"
- 400: "Insufficient stock. Available: 5, requested: 10"
```

### Checkout
```
POST /api/v1.0/orders
Authorization: Bearer <token>
Body: { ... order details ... }

Possible Errors:
- 400: "Product is no longer available: Gà Ủ Muối"
- 400: "Insufficient stock for product: Gà Ủ Muối. Available: 5, requested: 10"
```

---

## 💡 Pro Tips

1. **Auto-refresh cart**: When checkout fails, automatically refresh cart to show updated status
2. **Visual feedback**: Gray out unavailable items, make them semi-transparent
3. **Clear CTAs**: Make it obvious how to remove unavailable items
4. **Stock indicators**: Show "Only X left!" when stock is low
5. **Error messages**: Use the exact error message from backend (includes product name)

---

## 🆘 Common Issues

### Issue: Checkout button not disabling
**Solution**: Check `item.isProductActive` is being evaluated correctly
```javascript
const hasUnavailableItems = cartItems.some(item => !item.isProductActive);
```

### Issue: Error message not showing
**Solution**: Check error handling catches 400 status
```javascript
if (error.response?.status === 400) {
  alert(error.response.data.message);
}
```

### Issue: Cart not refreshing after error
**Solution**: Reload cart data after failed checkout
```javascript
catch (error) {
  if (error.response?.status === 400) {
    await refreshCart(); // or window.location.reload()
  }
}
```

---

**Need Help?** Check the full documentation: `SOFT_DELETE_IMPLEMENTATION_GUIDE.md`

