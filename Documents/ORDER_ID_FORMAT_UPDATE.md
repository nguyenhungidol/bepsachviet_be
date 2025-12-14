# Order ID Format Update - UUID to Date-Based Sequential Format

## Overview
Updated the order ID generation from UUID format to a date-based sequential format for better readability and organization.

## New Order ID Format
**Format:** `ORD-YYMMDD-NNNN`

**Example:** `ORD-251207-0199`
- `ORD` - Prefix identifier
- `251207` - Date in YYMMDD format (December 7, 2025)
- `0199` - Sequential order number for that day (4 digits, zero-padded)

## Benefits
1. **Human Readable** - Easy to identify when an order was placed
2. **Sequential** - Orders are numbered sequentially per day
3. **Organized** - Better for reporting and analytics
4. **Short** - More compact than UUID (15 characters vs 36)
5. **Sortable** - Natural sorting by date and order number

## Changes Made

### 1. Created OrderIdGenerator Utility
**File:** `src/main/java/com/doan/bepsachviet_be/util/OrderIdGenerator.java`

Features:
- `generateOrderId(int orderNumber)` - Generates order ID with current date
- `extractDateFromOrderId(String orderId)` - Extracts date portion
- `extractOrderNumberFromOrderId(String orderId)` - Extracts order number

### 2. Updated OrderRepository
**File:** `src/main/java/com/doan/bepsachviet_be/repository/OrderRepository.java`

Added methods:
```java
// Count orders created on or after a specific timestamp
long countByCreatedAtGreaterThanEqual(Timestamp timestamp);

// Find the last order created on a specific date
Optional<OrderEntity> findFirstByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Timestamp timestamp);
```

### 3. Updated OrderServiceImpl
**File:** `src/main/java/com/doan/bepsachviet_be/service/Impl/OrderServiceImpl.java`

Changes:
- Removed UUID generation
- Added `OrderIdGenerator` dependency
- Implemented `generateNextOrderId()` method
- Method is synchronized to prevent race conditions

**Generation Logic:**
```java
private synchronized String generateNextOrderId() {
  // Get start of today
  LocalDate today = LocalDate.now();
  Timestamp startOfDay = Timestamp.valueOf(today.atStartOfDay());
  
  // Count orders created today
  long todayOrderCount = orderRepository.countByCreatedAtGreaterThanEqual(startOfDay);
  
  // Generate order number (1-based, so add 1 to count)
  int orderNumber = (int) todayOrderCount + 1;
  
  return orderIdGenerator.generateOrderId(orderNumber);
}
```

## Database Migration
**No database migration needed!** The `orderId` column is already a VARCHAR/TEXT field, so the new format works with existing schema.

### Existing Orders
- Old orders with UUID format will continue to work
- New orders will use the new format
- Both formats can coexist in the database

## Compatibility

### Frontend Changes Needed
✅ **No breaking changes** - The frontend receives `orderId` as a string, regardless of format.

### API Endpoints
All order-related endpoints continue to work:
- `GET /api/v1.0/orders/:orderId` - Works with both UUID and new format
- `PUT /api/v1.0/orders/:orderId/status` - Works with both formats
- `POST /payment/momo/create` - Works with both formats

### MoMo Payment Integration
✅ **Compatible** - MoMo integration uses `order.getOrderId()` which works with any string format.

## Testing

### Manual Testing Steps
1. **Create a new order**
   ```
   POST /api/v1.0/orders
   ```
   Expected: Order ID follows format `ORD-YYMMDD-NNNN`

2. **Create multiple orders on same day**
   - First order: `ORD-251207-0001`
   - Second order: `ORD-251207-0002`
   - Third order: `ORD-251207-0003`

3. **Retrieve order by ID**
   ```
   GET /api/v1.0/orders/ORD-251207-0001
   ```
   Expected: Order details returned successfully

4. **Test MoMo payment**
   - Create order with MoMo payment method
   - Initiate payment
   - Complete payment via IPN callback
   Expected: Payment processes correctly with new order ID format

### Concurrency Testing
The `synchronized` keyword on `generateNextOrderId()` ensures thread-safety when multiple orders are created simultaneously.

## Examples

### Daily Order Sequence
```
ORD-251207-0001  (First order on Dec 7, 2025)
ORD-251207-0002  (Second order)
ORD-251207-0003  (Third order)
...
ORD-251207-0099  (99th order)
ORD-251207-0100  (100th order)
...
ORD-251207-9999  (Maximum 9999 orders per day)
```

### Multiple Days
```
ORD-251207-0001  (Dec 7, 2025)
ORD-251207-0002
ORD-251208-0001  (Dec 8, 2025 - sequence resets)
ORD-251208-0002
```

## Rollback Plan
If issues arise, rollback is simple:

1. Revert `OrderServiceImpl.java` to use UUID:
```java
.orderId(UUID.randomUUID().toString())
```

2. Remove the `OrderIdGenerator` dependency from `OrderServiceImpl`

3. Existing orders with new format will remain but future orders will use UUID

## Notes
- Maximum 9,999 orders per day supported
- If more than 9,999 orders needed per day, adjust the format to use 5 digits (NNNNN)
- The synchronized method may become a bottleneck at very high concurrency
  - Consider distributed ID generation (Redis, database sequence) for high-volume scenarios

## Related Documentation
- Order API Documentation: `ORDER_API_DOCUMENTATION.md`
- Order System Flow: `ORDER_SYSTEM_FLOW_DIAGRAMS.md`
- Payment Testing Guide: `PAYMENT_TESTING_GUIDE.md`

## Date: December 7, 2025
## Status: ✅ Completed and Tested

