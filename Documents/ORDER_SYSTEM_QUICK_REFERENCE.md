# Order Management System - Quick Reference

## Email Confirmation Strategy

| Payment Method | Order Status on Creation | Confirmation Email Sent When | Who Controls |
|---------------|-------------------------|------------------------------|--------------|
| **CASH_ON_DELIVERY** | `PENDING` | Admin changes status to `CONFIRMED` | Admin Only |
| **MOMO** | `PENDING` | Payment successfully completed | Automatic (MoMo IPN) |

## Order Status Flow

### Cash on Delivery (COD)
```
PENDING (waiting for admin confirmation)
   ↓ (Admin confirms) → 📧 Email sent
CONFIRMED
   ↓ (Admin processes)
SHIPPING
   ↓ (Delivered + Payment collected)
DELIVERED + Payment: COMPLETED
```

### MoMo Online Payment
```
PENDING (waiting for payment)
   ↓ (Customer pays)
Payment successful → Status: CONFIRMED + 📧 Email sent
   ↓ (Admin processes)
SHIPPING
   ↓ (Delivered)
DELIVERED
```

## Order Cancellation
```
PENDING/CONFIRMED/SHIPPING
   ↓ (Admin/System cancels)
CANCELED
   ↓ (Automatic)
Stock refunded to inventory
```

## API Endpoints

### Customer Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1.0/orders` | Create new order | ✅ Customer |
| GET | `/api/v1.0/orders/my-orders` | View my orders | ✅ Customer |
| GET | `/api/v1.0/orders/{orderId}` | View order details | ✅ Customer |

### Admin Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1.0/admin/orders` | List all orders | ✅ Admin Only |
| GET | `/api/v1.0/admin/orders?status=PENDING` | Filter by status | ✅ Admin Only |
| GET | `/api/v1.0/admin/orders/{orderId}` | View order details | ✅ Admin Only |
| PATCH | `/api/v1.0/admin/orders/{orderId}/status` | Update order status | ✅ Admin Only |

### Payment Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1.0/payment/momo/create` | Create MoMo payment | ✅ Customer |
| GET | `/api/v1.0/payment/momo/return` | MoMo return URL | Public |
| POST | `/api/v1.0/payment/momo/ipn-handler` | MoMo IPN callback | Public |

## Order Status Definitions

| Status | Description | Payment Status | Actions Available |
|--------|-------------|----------------|-------------------|
| **PENDING** | Order created, awaiting confirmation | PENDING | Admin: Confirm/Cancel |
| **CONFIRMED** | Order confirmed, ready for processing | PENDING/COMPLETED | Admin: Ship/Cancel |
| **SHIPPING** | Order in transit | PENDING/COMPLETED | Admin: Deliver/Cancel |
| **DELIVERED** | Order delivered to customer | COMPLETED (auto for COD) | None |
| **CANCELED** | Order canceled | PENDING/FAILED | None |

## Payment Status Definitions

| Status | Description | When Set |
|--------|-------------|----------|
| **PENDING** | Payment not yet received | Order creation (default) |
| **COMPLETED** | Payment received | MoMo: After successful payment<br>COD: Auto when DELIVERED |
| **FAILED** | Payment failed | MoMo: After failed payment |

## Admin Workflows

### Confirming COD Order
1. Admin logs into admin panel
2. Views orders with status = PENDING
3. Reviews order details
4. Calls/contacts customer if needed
5. Updates status to CONFIRMED
6. ✅ System sends confirmation email to customer
7. Proceed with order fulfillment

### Processing Confirmed Order
1. View CONFIRMED orders
2. Prepare products for shipment
3. Update status to SHIPPING
4. Customer receives tracking/shipping notification
5. Deliver order
6. Update status to DELIVERED
7. For COD: Payment status auto-updates to COMPLETED

### Canceling Order
1. View order details
2. Update status to CANCELED
3. ✅ System automatically refunds stock to inventory
4. Customer notified (if notification system implemented)

## Stock Management

### Stock Deduction
- **When**: Immediately when order is created
- **Amount**: Quantity ordered by customer
- **Status**: Works for both PENDING and confirmed orders

### Stock Refund
- **When**: Order status changed to CANCELED
- **From Status**: PENDING, CONFIRMED, or SHIPPING
- **Amount**: Full quantity restored
- **Automatic**: Yes, handled by system

## Email Triggers

| Trigger | Condition | Recipient | Template |
|---------|-----------|-----------|----------|
| COD Order Confirmed | Admin changes PENDING → CONFIRMED | Customer | Order Confirmation |
| MoMo Payment Success | IPN resultCode = 0 | Customer | Order Confirmation |
| Password Reset | User requests reset | User | Password Reset Link |

## Security Features

✅ **Role-Based Access Control (RBAC)**
- Admin endpoints require `ROLE_ADMIN`
- Customer endpoints require authentication
- Public endpoints: Login, Register, Product viewing, MoMo callbacks

✅ **JWT Authentication**
- All authenticated requests require valid JWT token
- Token passed in `Authorization: Bearer <token>` header

✅ **CORS Configuration**
- Allowed origin: `http://localhost:5173`
- Allowed methods: POST, GET, DELETE, PUT, PATCH, OPTIONS
- Credentials allowed: Yes

## Automatic Tasks

### Order Auto-Cancellation (Scheduler)
- **Frequency**: Every 1 minute
- **Target**: PENDING orders created > 15 minutes ago
- **Action**: 
  - Status → CANCELED
  - Stock refunded
  - Notes: "Hủy tự động do quá hạn thanh toán 15 phút"

### Payment Auto-Completion
- **Trigger**: COD order status → DELIVERED
- **Condition**: Payment status = PENDING
- **Action**: Payment status → COMPLETED

## Configuration

### Email Service
- **Provider**: Gmail SMTP
- **Port**: 587
- **TLS**: Enabled
- **From**: nguyencuongaq1@gmail.com

### MoMo Configuration
- **Environment**: Test
- **Partner Code**: MOMOLRJZ20181206
- **Endpoint**: https://test-payment.momo.vn/v2/gateway/api/create
- **IPN URL**: https://unincidental-eneida-unspun.ngrok-free.dev/api/v1.0/payment/momo/ipn-handler

### Database
- **Type**: MySQL
- **Host**: localhost:3306
- **Database**: bepsachviet
- **JPA**: Auto-update DDL

## Common Use Cases

### Use Case 1: Customer Places COD Order
```
1. Customer: POST /orders (payment method: COD)
2. System: Creates order, decrements stock, returns order ID
3. Customer: Sees order in "my orders" with PENDING status
4. ❌ Customer does not receive email yet
5. Admin: Reviews order in admin panel
6. Admin: PATCH /admin/orders/{id}/status → CONFIRMED
7. ✅ Customer receives confirmation email
8. Admin: Ships order → PATCH status → SHIPPING
9. Admin: Delivers order → PATCH status → DELIVERED
10. System: Auto-updates payment status → COMPLETED
```

### Use Case 2: Customer Places MoMo Order
```
1. Customer: POST /orders (payment method: MOMO)
2. System: Creates order, decrements stock, returns order ID
3. Customer: POST /payment/momo/create
4. System: Returns MoMo payment URL
5. Customer: Redirected to MoMo, completes payment
6. MoMo: Sends IPN to server
7. System: Verifies payment, updates status → CONFIRMED
8. ✅ Customer receives confirmation email
9. Admin: Processes order normally
```

### Use Case 3: Admin Cancels Order
```
1. Admin: Views order with PENDING/CONFIRMED status
2. Admin: PATCH /admin/orders/{id}/status → CANCELED
3. System: Refunds stock automatically
4. Customer: Can see order is canceled in their order history
```

## Troubleshooting

### Issue: Customer didn't receive confirmation email (COD)
- **Check**: Has admin confirmed the order?
- **Status**: Should be CONFIRMED, not PENDING
- **Action**: Admin needs to update status to CONFIRMED

### Issue: Customer didn't receive confirmation email (MoMo)
- **Check**: Was payment successful?
- **Check**: Payment status should be COMPLETED
- **Check**: IPN callback received and processed?
- **Logs**: Check server logs for IPN processing

### Issue: Stock not refunded after cancellation
- **Check**: Was order status PENDING, CONFIRMED, or SHIPPING before cancellation?
- **Check**: Server logs for errors during status update
- **Action**: Manually verify product stock quantities

### Issue: Cannot update order status
- **Check**: Are you using admin account?
- **Check**: JWT token valid and contains ROLE_ADMIN?
- **Check**: Endpoint is /admin/orders/{id}/status

## Best Practices

✅ **For Admins**
- Review and confirm COD orders promptly
- Contact customer if order details unclear
- Update status progressively (CONFIRMED → SHIPPING → DELIVERED)
- Monitor PENDING orders older than 15 minutes
- Check stock levels before confirming orders

✅ **For Developers**
- Always test email sending in development
- Monitor MoMo IPN callback logs
- Keep ngrok URL updated in configuration
- Verify stock refund logic after cancellations
- Check JWT token expiration times

✅ **For System Maintenance**
- Regular database backups
- Monitor email service quota
- Keep MoMo credentials secure
- Review order scheduler logs
- Clean up old CANCELED orders

## Quick Commands

### Build Project
```bash
.\mvnw.cmd clean compile -DskipTests
```

### Run Application
```bash
.\mvnw.cmd spring-boot:run
```

### Run Tests
```bash
.\mvnw.cmd test
```

### Build JAR
```bash
.\mvnw.cmd clean package
```

## Related Documentation
- [COD Order Admin Confirmation](./COD_ORDER_ADMIN_CONFIRMATION.md)
- [MoMo Email Confirmation Update](./MOMO_EMAIL_CONFIRMATION_UPDATE.md)
- [Order API Documentation](./ORDER_API_DOCUMENTATION.md)
- [Payment API Quick Reference](./PAYMENT_API_QUICK_REFERENCE.md)

---
**Last Updated**: December 5, 2025

