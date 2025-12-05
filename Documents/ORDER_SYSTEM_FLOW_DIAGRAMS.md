# Order System Flow Diagrams

## Complete Order & Email System - Visual Guide

---

## 1. COD Order Flow (Cash on Delivery)

```
┌─────────────────────────────────────────────────────────────────┐
│                    COD ORDER LIFECYCLE                           │
└─────────────────────────────────────────────────────────────────┘

CUSTOMER ACTIONS                 SYSTEM ACTIONS                ADMIN ACTIONS

[Customer places order]
        │
        ├──────────────→ [Create Order]
                               │
                               ├─ Status: PENDING
                               ├─ Payment: PENDING
                               ├─ Decrement stock
                               │
                               ↓
                        [Order Created]
                               │
                               ├─ Return order ID
                               ├─ ❌ NO EMAIL SENT
                               │
        ↓                      ↓                           ↓
                                                    [Review Order]
[Wait for confirmation]                                    │
                                                    [Verify details]
                                                           │
                                                    [Confirm Order]
                                                           │
                               ↓                           │
                        [Update Status] ←───────────────────┘
                               │
                               ├─ PENDING → CONFIRMED
                               ├─ 📧 SEND EMAIL
                               │
                               ↓
        ↓                      ↓
[Receive email] ←─────── [Email Sent]
        │
[Track order]
        │
        ↓
                                                    [Process Order]
                                                           │
                                                    [Update: SHIPPING]
                                                           │
                                                    [Deliver Order]
                                                           │
                                                    [Update: DELIVERED]
                                                           │
                               ↓                           │
                        [Auto Update] ←──────────────────────┘
                               │
                               ├─ Payment: COMPLETED
                               │
                               ↓
[Order Complete]        [Process Complete]         [Order Fulfilled]
```

---

## 2. MoMo Order Flow (Online Payment)

```
┌─────────────────────────────────────────────────────────────────┐
│                    MOMO ORDER LIFECYCLE                          │
└─────────────────────────────────────────────────────────────────┘

CUSTOMER ACTIONS              SYSTEM ACTIONS              MOMO GATEWAY

[Customer places order]
        │
        ├──────────────→ [Create Order]
                               │
                               ├─ Status: PENDING
                               ├─ Payment: PENDING
                               ├─ Decrement stock
                               │
                               ↓
                        [Order Created]
                               │
                               ├─ Return order ID
                               ├─ ❌ NO EMAIL SENT
                               │
        ↓                      ↓
[Request payment]
        │
        ├──────────────→ [Create Payment Request]
                               │
                               ├─ Generate signature
                               ├─ Build payment URL
                               │
                               ├──────────────→ [Payment Request]
                               │                      │
        ↓                      ↓                      ↓
[Redirected] ←───────── [Return Payment URL]   [Generate QR/Form]
        │                                             │
        ↓                                             │
[Pay via MoMo] ───────────────────────────────────────┘
        │                                             │
        │                      ↓                      ↓
        │               [IPN Callback] ←─────── [Payment Success]
        │                      │
        │                      ├─ Verify signature
        │                      ├─ resultCode = 0
        │                      ├─ Update order
        │                      ├─ Status: CONFIRMED
        │                      ├─ Payment: COMPLETED
        │                      ├─ 📧 SEND EMAIL
        │                      │
        ↓                      ↓
[Receive email] ←─────── [Email Sent]
        │
[Order confirmed]
        │
        ↓
                                                ADMIN PROCESSES ORDER
                                                (Same as COD from here)
```

---

## 3. Order Cancellation Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                   ORDER CANCELLATION FLOW                        │
└─────────────────────────────────────────────────────────────────┘

TRIGGER                      SYSTEM ACTIONS                RESULT

[Admin Cancel]
        │
        ├──────────────→ [Update Order Status]
                               │
                               ├─ Status: CANCELED
                               ├─ Refund Stock
                               │
                               ↓
                        [For each item]
                               │
                               ├─ Get product
                               ├─ quantity += ordered
                               ├─ Save product
                               │
                               ↓
                        [Stock Restored] ──→ [Inventory Updated]


[Scheduler Cancel]
        │
        ├──────────────→ [Find Expired Orders]
                               │
                               ├─ PENDING orders
                               ├─ Created > 15 min ago
                               │
                               ↓
                        [Cancel Each Order]
                               │
                               ├─ Status: CANCELED
                               ├─ Notes: "Auto cancel..."
                               ├─ Refund Stock
                               │
                               ↓
                        [Stock Restored] ──→ [Inventory Updated]
```

---

## 4. Admin Status Update Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                   ADMIN STATUS UPDATE FLOW                       │
└─────────────────────────────────────────────────────────────────┘

ADMIN REQUEST                SECURITY CHECK              SYSTEM ACTION

[PATCH /admin/orders/{id}/status]
        │
        ├──────────────→ [Check Authentication]
                               │
                               ├─ JWT valid?
                               ├─ Role = ADMIN?
                               │
                         ┌─────┴─────┐
                         │           │
                     [YES]         [NO]
                         │           │
                         │           └──→ [403 Forbidden]
                         │
                         ↓
                  [Process Update]
                         │
                  ┌──────┴──────┐
                  │             │
          [Status Change]   [Special Actions]
                  │             │
                  │             ├─ CONFIRMED & COD?
                  │             │  └→ Send email
                  │             │
                  │             ├─ DELIVERED & COD?
                  │             │  └→ Payment: COMPLETED
                  │             │
                  │             ├─ CANCELED?
                  │             │  └→ Refund stock
                  │             │
                  ↓             ↓
           [Save Order]  [Execute Actions]
                  │             │
                  └──────┬──────┘
                         │
                         ↓
                  [Return Updated Order]
```

---

## 5. Email Decision Tree

```
┌─────────────────────────────────────────────────────────────────┐
│                    EMAIL SENDING LOGIC                           │
└─────────────────────────────────────────────────────────────────┘

                        [Order Event]
                              │
                    ┌─────────┴─────────┐
                    │                   │
            [Order Created]      [Status Updated]
                    │                   │
                    ↓                   ↓
            ❌ NO EMAIL           [Check Payment Method]
                                        │
                              ┌─────────┴─────────┐
                              │                   │
                        [COD Order]         [MoMo Order]
                              │                   │
                              ↓                   ↓
                    [Status Changed?]     [Payment Success?]
                              │                   │
                    ┌─────────┴─────┐             │
                    │               │             │
          [PENDING→CONFIRMED]  [Other]      [resultCode=0?]
                    │               │             │
                    ↓               ↓       ┌─────┴─────┐
            ✅ SEND EMAIL    ❌ NO EMAIL   [YES]      [NO]
                                            │          │
                                            ↓          ↓
                                    ✅ SEND EMAIL  ❌ NO EMAIL
```

---

## 6. Stock Management Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    STOCK MANAGEMENT FLOW                         │
└─────────────────────────────────────────────────────────────────┘

ORDER CREATION                          ORDER CANCELLATION

[Create Order]                          [Cancel Order]
       │                                       │
       ↓                                       ↓
[For each item]                          [Check old status]
       │                                       │
       ├─ Get product                    ┌─────┴──────────────┐
       ├─ Check stock                    │                    │
       │                        [PENDING/CONFIRMED/SHIPPING] [Other]
       │                                 │                    │
       ↓                                 ↓                    ↓
[Stock - quantity]                [For each item]      [No refund]
       │                                 │
       ↓                                 ├─ Get product
[Save product]                           ├─ Add quantity
       │                                 ├─ Save product
       ↓                                 │
[Stock decremented]                      ↓
                                  [Stock refunded]


VISUAL EXAMPLE:
Product: "Kitchen Knife"
Initial stock: 100

Order created (qty: 5)
  └→ Stock: 100 - 5 = 95 ✅

Order canceled
  └→ Stock: 95 + 5 = 100 ✅
```

---

## 7. Security & Access Control

```
┌─────────────────────────────────────────────────────────────────┐
│                    API SECURITY LAYERS                           │
└─────────────────────────────────────────────────────────────────┘

                        [HTTP Request]
                              │
                              ↓
                      [CORS Filter]
                              │
                    ┌─────────┴─────────┐
                    │                   │
              [Allowed Origin]    [Other Origin]
                    │                   │
                    ↓                   ↓
            [Continue]           [403 Forbidden]
                    │
                    ↓
              [Security Filter Chain]
                    │
              ┌─────┴─────────────────────────┐
              │                               │
    [Public Endpoints]              [Protected Endpoints]
              │                               │
              ├─ /login                       ↓
              ├─ /registers            [JWT Filter]
              ├─ /products (GET)              │
              ├─ /momo/ipn          ┌─────────┴─────────┐
              │                     │                   │
              ↓              [Valid JWT]         [Invalid JWT]
        [Allow Access]              │                   │
                                    ↓                   ↓
                          [Extract Role]        [401 Unauthorized]
                                    │
                          ┌─────────┴─────────┐
                          │                   │
                    [ROLE_ADMIN]        [ROLE_CUSTOMER]
                          │                   │
                          ↓                   ↓
                  [Admin Endpoints]   [Customer Endpoints]
                          │                   │
                          ├─ /admin/**        ├─ /orders (POST)
                          │  └→ All CRUD      ├─ /orders/my-orders
                          │                   ├─ /cart/**
                          │                   └─ /payment/momo/create
                          │                   │
                          ↓                   ↓
                    [Allow Access]      [Allow Access]
```

---

## 8. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        SYSTEM ARCHITECTURE                               │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────┐                                    ┌──────────────┐
│   Frontend   │                                    │   External   │
│  (React.js)  │                                    │   Services   │
│              │                                    │              │
│ - Customer   │                                    │ - Gmail SMTP │
│ - Admin      │                                    │ - MoMo API   │
└──────┬───────┘                                    │ - AWS S3     │
       │                                            └──────┬───────┘
       │ HTTP/REST                                         │
       │                                                   │
       ↓                                                   ↓
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Backend                          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Controllers                          │    │
│  │  - OrderController  - MomoController  - CartController │    │
│  └──────────────────────┬──────────────────────────────────┘    │
│                         ↓                                       │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                Security Layer (JWT)                     │    │
│  │  - Authentication  - Authorization  - CORS             │    │
│  └──────────────────────┬──────────────────────────────────┘    │
│                         ↓                                       │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Services                             │    │
│  │  - OrderService  - EmailService  - PaymentService      │    │
│  └──────────────────────┬──────────────────────────────────┘    │
│                         ↓                                       │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                  Repositories                           │    │
│  │  - OrderRepo  - ProductRepo  - UserRepo                │    │
│  └──────────────────────┬──────────────────────────────────┘    │
│                         ↓                                       │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                  Scheduled Tasks                        │    │
│  │  - OrderScheduler (Auto-cancel expired orders)         │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                  │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           │ JDBC
                           ↓
                  ┌─────────────────┐
                  │  MySQL Database │
                  │                 │
                  │ - orders        │
                  │ - order_items   │
                  │ - products      │
                  │ - users         │
                  │ - carts         │
                  └─────────────────┘
```

---

## 9. Data Flow: Complete Order Processing

```
┌─────────────────────────────────────────────────────────────────────────┐
│               COMPLETE ORDER PROCESSING DATA FLOW                        │
└─────────────────────────────────────────────────────────────────────────┘

TIME    CUSTOMER              FRONTEND           BACKEND              DATABASE
  │
  ↓
[T0]  [Browse products]
         │
         ├──────────→ [Add to cart]
         │                            ├──────→ [Update cart]
         │                                         ├──────→ [cart_items]
  ↓      ↓
[T1]  [Checkout]
         │
         ├──────────→ [Create order]
                                      ├──────→ [Validate stock]
                                                  ├──────→ [products]
                                      ├──────→ [Create order]
                                                  ├──────→ [orders]
                                      ├──────→ [Create items]
                                                  ├──────→ [order_items]
                                      ├──────→ [Update stock]
                                                  ├──────→ [products]
                       ←─────────── [Return order ID]
  ↓      ↓
[T2]  ╔════════════════════════════════════════════════════════════╗
      ║         IF COD: Wait for admin                             ║
      ║         IF MOMO: Go to payment                            ║
      ╚════════════════════════════════════════════════════════════╝

      ┌─────────────────────────┬─────────────────────────────┐
      │     COD PATH            │      MOMO PATH              │
      │                         │                             │
[T3]  │ [Wait...]              │ [Pay on MoMo]              │
      │                         │      │                     │
      │                         │      ├────→ [MoMo Gateway]│
      │                         │              ↓             │
      │                         │      [Process payment]     │
      │                         │              ↓             │
      │                         │      [Send IPN callback]   │
      │                         │              ↓             │
      │                         │              ├──────→ [Backend]
      │                         │                      ├──→ [Verify]
      │                         │                      ├──→ [Update]
      │                         │                           ├→ [orders]
      │                         │                      ├──→ [Send email]
      │                         │                           ├→ [SMTP]
      │                         │                             │
[T4]  │ ADMIN:                 │      ↓                     │
      │ [Review order]         │ [Receive email] ←─────────────┘
      │      ↓                 │
      │ [Confirm order]        │
      │      ├──────→ [Update status]
      │                  ├──────→ [Update order]
      │                              ├──────→ [orders]
      │                  ├──────→ [Send email]
      │                              ├──────→ [SMTP]
      │      ↓                       │
[T5]  │ [Receive email] ←───────────┘
      │                             │
      └─────────────────────────────┘
                    │
                    ↓
[T6]         [Order processing continues...]
```

---

## 10. Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING FLOW                           │
└─────────────────────────────────────────────────────────────────┘

[API Request]
      │
      ↓
[Try Execute]
      │
      ├──────────────────────────────────────┐
      │                                      │
 [SUCCESS]                               [ERROR]
      │                                      │
      ↓                             ┌────────┴────────┐
[200 OK]                            │                 │
[Return Data]              [Validation Error]  [System Error]
                                    │                 │
                                    ↓                 ↓
                            [400 Bad Request]  [500 Server Error]
                                    │                 │
                                    ├─ Log error      ├─ Log stack trace
                                    ├─ Return message ├─ Return generic message
                                    │                 ├─ Email NOT sent
                                    ↓                 ↓
                            [User informed]    [Admin alerted]


SPECIFIC SCENARIOS:

[Email Sending Fails]
      │
      ├─ Log error
      ├─ ❌ Email NOT sent
      ├─ ✅ Order STILL created
      ├─ Continue processing
      │
      └─ Admin can retry from admin panel

[Stock Insufficient]
      │
      ├─ Before order creation
      ├─ 400 Bad Request
      ├─ Clear error message
      ├─ ❌ Order NOT created
      │
      └─ Customer tries different quantity

[Payment Failed (MoMo)]
      │
      ├─ IPN resultCode != 0
      ├─ Payment status: FAILED
      ├─ Order status: PENDING
      ├─ ❌ Email NOT sent
      │
      └─ Customer can retry payment
```

---

**End of Visual Guide**

For detailed implementation, see:
- ORDER_EMAIL_SYSTEM_COMPLETE_SUMMARY.md
- COD_ORDER_ADMIN_CONFIRMATION.md
- ORDER_SYSTEM_QUICK_REFERENCE.md

