# Chat System API Testing Guide

## Prerequisites

1. ✅ Application running on `http://localhost:8080`
2. ✅ Database migration executed (`CHAT_MIGRATION.sql`)
3. ✅ Admin user created in database

---

## 1. Customer Chat Flow

### Step 1: Start a New Conversation (Guest User)

```bash
curl -X POST http://localhost:8080/api/v1.0/chat/conversations \
  -H "Content-Type: application/json" \
  -d '{
    "guestName": "John Doe",
    "guestEmail": "john@example.com",
    "guestPhone": "0123456789",
    "initialMessage": "I need help with my order"
  }'
```

**Expected Response:**
```json
{
  "conversationId": "uuid-here",
  "status": "PENDING",
  "guestName": "John Doe",
  "guestEmail": "john@example.com",
  "hasUnreadMessages": true,
  "messages": [
    {
      "sender": "CUSTOMER",
      "content": "I need help with my order",
      "createdAt": "2025-12-08T00:00:00"
    },
    {
      "sender": "ADMIN",
      "content": "Cảm ơn bạn đã liên hệ! Nhân viên hỗ trợ sẽ phản hồi trong giây lát.",
      "createdAt": "2025-12-08T00:00:01"
    }
  ]
}
```

**Save the `conversationId` for next steps!**

---

### Step 2: Get Current Active Conversation

```bash
curl -X GET "http://localhost:8080/api/v1.0/chat/conversations/me?guestEmail=john@example.com"
```

**Expected Response:**
```json
{
  "conversationId": "uuid-here",
  "status": "PENDING",
  "messages": [...],
  ...
}
```

---

### Step 3: Send a Message

```bash
curl -X POST http://localhost:8080/api/v1.0/chat/conversations/{conversationId}/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "My order number is ORD-251207-0001"
  }'
```

**Expected Response:**
```json
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "sender": "CUSTOMER",
  "content": "My order number is ORD-251207-0001",
  "createdAt": "2025-12-08T00:00:00"
}
```

---

### Step 4: Get All Messages

```bash
curl -X GET http://localhost:8080/api/v1.0/chat/conversations/{conversationId}/messages
```

**Expected Response:**
```json
[
  {
    "messageId": "uuid",
    "sender": "CUSTOMER",
    "content": "I need help with my order"
  },
  {
    "messageId": "uuid",
    "sender": "ADMIN",
    "content": "Cảm ơn bạn đã liên hệ..."
  },
  {
    "messageId": "uuid",
    "sender": "CUSTOMER",
    "content": "My order number is ORD-251207-0001"
  }
]
```

---

### Step 5: Close Conversation

```bash
curl -X POST http://localhost:8080/api/v1.0/chat/conversations/{conversationId}/close
```

**Expected Response:**
```json
{
  "conversationId": "uuid",
  "status": "COMPLETED",
  ...
}
```

---

## 2. Admin Chat Flow

### Step 1: Login as Admin

First, get an admin token:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin_password"
  }'
```

**Save the `token` from response!**

---

### Step 2: Get Pending Conversations Count

```bash
curl -X GET http://localhost:8080/api/v1.0/admin/chat/conversations/pending-count \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "pendingCount": 5
}
```

---

### Step 3: Get All Pending Conversations

```bash
curl -X GET "http://localhost:8080/api/v1.0/admin/chat/conversations?status=PENDING&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "content": [
    {
      "conversationId": "uuid",
      "guestName": "John Doe",
      "guestEmail": "john@example.com",
      "status": "PENDING",
      "lastMessage": "My order number is ORD-251207-0001",
      "hasUnreadMessages": true,
      "unreadCount": 2
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

### Step 4: Assign Conversation to Self

```bash
curl -X POST http://localhost:8080/api/v1.0/admin/chat/conversations/{conversationId}/assign \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "conversationId": "uuid",
  "status": "ASSIGNED",
  "assignedAdminName": "Admin Name",
  ...
}
```

---

### Step 5: View Customer Order History

```bash
curl -X GET http://localhost:8080/api/v1.0/admin/chat/conversations/{conversationId}/customer-orders \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
[
  {
    "orderId": "ORD-251207-0001",
    "status": "DELIVERED",
    "totalAmount": 500000,
    "paymentMethod": "CASH_ON_DELIVERY",
    "createdAt": "2025-12-07T10:00:00"
  },
  {
    "orderId": "ORD-251205-0042",
    "status": "PENDING",
    "totalAmount": 250000,
    "paymentMethod": "MOMO",
    "createdAt": "2025-12-05T15:30:00"
  }
]
```

---

### Step 6: Send Admin Reply

```bash
curl -X POST http://localhost:8080/api/v1.0/admin/chat/conversations/{conversationId}/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "I found your order ORD-251207-0001. It was delivered on Dec 7th. Is there any issue?"
  }'
```

**Expected Response:**
```json
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "sender": "ADMIN",
  "senderName": "Admin Name",
  "content": "I found your order...",
  "createdAt": "2025-12-08T00:00:00"
}
```

---

### Step 7: Mark as Finished

```bash
curl -X POST http://localhost:8080/api/v1.0/admin/chat/conversations/{conversationId}/finish \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "conversationId": "uuid",
  "status": "COMPLETED",
  "assignedAdminName": "Admin Name",
  ...
}
```

---

## 3. WebSocket Testing

### Using JavaScript (Browser Console or Node.js)

```javascript
// Install dependencies
// npm install sockjs-client stompjs

const SockJS = require('sockjs-client');
const Stomp = require('stompjs');

// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to conversation messages
  stompClient.subscribe('/topic/conversation/' + conversationId, function(message) {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
  });
  
  // Admin subscribes to notifications
  stompClient.subscribe('/topic/admin/notifications', function(notification) {
    const notif = JSON.parse(notification.body);
    console.log('Admin notification:', notif);
    // Play sound, show badge, etc.
  });
  
  // Send message via WebSocket
  stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
    conversationId: conversationId,
    content: 'Hello via WebSocket!'
  }));
});
```

---

## 4. React Frontend Example

### Customer Chat Component

```jsx
import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function ChatWidget() {
  const [conversationId, setConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    // Connect WebSocket
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const client = Stomp.over(socket);
    
    client.connect({}, () => {
      console.log('WebSocket connected');
      setStompClient(client);
    });

    return () => {
      if (client) client.disconnect();
    };
  }, []);

  useEffect(() => {
    if (stompClient && conversationId) {
      // Subscribe to messages
      const subscription = stompClient.subscribe(
        `/topic/conversation/${conversationId}`,
        (message) => {
          const chatMessage = JSON.parse(message.body);
          setMessages(prev => [...prev, chatMessage]);
        }
      );

      return () => subscription.unsubscribe();
    }
  }, [stompClient, conversationId]);

  const startChat = async (initialMessage) => {
    const response = await fetch('http://localhost:8080/api/v1.0/chat/conversations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        guestName: 'Guest User',
        guestEmail: 'guest@example.com',
        initialMessage
      })
    });
    
    const data = await response.json();
    setConversationId(data.conversationId);
    setMessages(data.messages);
  };

  const sendMessage = async (content) => {
    await fetch(`http://localhost:8080/api/v1.0/chat/conversations/${conversationId}/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content })
    });
  };

  return (
    <div className={`chat-widget ${isOpen ? 'open' : ''}`}>
      {/* Your chat UI */}
    </div>
  );
}
```

---

## 5. Admin Dashboard Example

```jsx
function AdminDashboard() {
  const [conversations, setConversations] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    // Connect WebSocket
    const socket = new SockJS('http://localhost:8080/ws/admin/notifications');
    const client = Stomp.over(socket);
    
    client.connect({}, () => {
      // Subscribe to admin notifications
      client.subscribe('/topic/admin/notifications', (notification) => {
        const notif = JSON.parse(notification.body);
        setUnreadCount(notif.unreadCount);
        playNotificationSound();
        showToast(notif.message);
      });
      
      setStompClient(client);
    });

    return () => client.disconnect();
  }, []);

  const fetchConversations = async () => {
    const response = await fetch(
      'http://localhost:8080/api/v1.0/admin/chat/conversations?status=PENDING',
      {
        headers: { 'Authorization': `Bearer ${token}` }
      }
    );
    const data = await response.json();
    setConversations(data.content);
  };

  return (
    <div className="admin-dashboard">
      <div className="notification-badge">{unreadCount}</div>
      {/* Conversation list */}
    </div>
  );
}
```

---

## 6. Common Test Scenarios

### Scenario 1: Guest User Gets Help
1. Guest starts conversation
2. Guest sends order number
3. Admin gets notification
4. Admin assigns conversation
5. Admin views customer orders
6. Admin replies
7. Guest receives reply
8. Admin marks as finished

### Scenario 2: Logged-in User Gets Help
1. User logs in
2. User starts conversation (auto-filled info)
3. Same flow as guest but with user details

### Scenario 3: Multiple Admins
1. Admin A assigns conversation
2. Admin B sees it's assigned to Admin A
3. Admin B cannot interfere

---

## 7. Error Scenarios to Test

### Test 1: Guest Without Email/Phone
```bash
curl -X POST http://localhost:8080/api/v1.0/chat/conversations \
  -H "Content-Type: application/json" \
  -d '{"guestName": "John", "initialMessage": "Hello"}'
```
**Expected:** 400 Bad Request - "Guest users must provide email or phone number"

### Test 2: Non-Admin Accessing Admin Endpoints
```bash
curl -X GET http://localhost:8080/api/v1.0/admin/chat/conversations \
  -H "Authorization: Bearer USER_TOKEN"
```
**Expected:** 403 Forbidden

### Test 3: Invalid Conversation ID
```bash
curl -X GET http://localhost:8080/api/v1.0/chat/conversations/invalid-id/messages
```
**Expected:** 404 Not Found

---

## 8. Performance Testing

### Load Test with Apache Bench
```bash
# Test conversation creation
ab -n 100 -c 10 -p conversation.json -T application/json \
  http://localhost:8080/api/v1.0/chat/conversations

# Where conversation.json contains:
# {"guestName":"Test","guestEmail":"test@test.com","initialMessage":"Hello"}
```

---

## Date: December 8, 2025
## Status: Ready for Testing

