# Real-Time Customer Support Chat System

## Overview
A complete real-time chat system allowing customers to contact support and admins to respond instantly via WebSocket connection.

## Features

### Customer Side
- ✅ Floating chat button always visible
- ✅ Guest mode (no login required) with email/phone collection
- ✅ Logged-in user automatic info fetch
- ✅ Auto-response when sending first message
- ✅ Real-time message delivery
- ✅ Message history persistence

### Admin Side
- ✅ Real-time notifications (sound + red badge)
- ✅ Pending conversations tab
- ✅ Conversation assignment (prevent multiple admin replies)
- ✅ Customer order history sidebar
- ✅ Mark conversation as completed
- ✅ Archived chat history

## Architecture

### Database Tables
1. **chat_conversations** - Stores conversation metadata
2. **chat_messages** - Stores individual messages

### Backend Components
1. **Entities**: ChatConversationEntity, ChatMessageEntity
2. **Repositories**: ChatConversationRepository, ChatMessageRepository  
3. **Services**: ChatService, ChatServiceImpl
4. **Controllers**: ChatController (customer), AdminChatController (admin)
5. **DTOs**: Request & Response objects
6. **WebSocket**: Real-time bidirectional communication

## API Endpoints

### Customer Endpoints

#### Start Chat
```
POST /api/v1.0/chat/start
Content-Type: application/json
Authorization: Bearer <token> (optional for logged-in users)

Request:
{
  "guestName": "John Doe",           // Required if not logged in
  "guestEmail": "john@example.com",  // Required if not logged in (email OR phone)
  "guestPhone": "0123456789",        // Required if not logged in (email OR phone)
  "initialMessage": "I need help with my order"
}

Response:
{
  "conversationId": "uuid",
  "status": "PENDING",
  "messages": [
    {
      "messageId": "uuid",
      "sender": "CUSTOMER",
      "content": "I need help with my order",
      "createdAt": "2025-12-07T10:00:00"
    },
    {
      "messageId": "uuid",
      "sender": "ADMIN",
      "content": "Cảm ơn bạn đã liên hệ! Nhân viên hỗ trợ sẽ phản hồi trong giây lát.",
      "createdAt": "2025-12-07T10:00:01"
    }
  ]
}
```

#### Get Active Conversation
```
GET /api/v1.0/chat/active
Authorization: Bearer <token> (optional)

Query Params (for guest):
  - guestEmail (optional)
  - guestPhone (optional)

Response: Same as start chat
```

#### Send Message (HTTP)
```
POST /api/v1.0/chat/messages
Content-Type: application/json
Authorization: Bearer <token> (optional)

Request:
{
  "conversationId": "uuid",
  "content": "Can you check order ORD-251207-0001?"
}

Response:
{
  "messageId": "uuid",
  "conversationId": "uuid",
  "sender": "CUSTOMER",
  "content": "Can you check order ORD-251207-0001?",
  "isRead": false,
  "createdAt": "2025-12-07T10:05:00"
}
```

#### Get Conversation Messages
```
GET /api/v1.0/chat/conversations/{conversationId}/messages

Response:
[
  {
    "messageId": "uuid",
    "sender": "CUSTOMER",
    "content": "Message content",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

### Admin Endpoints (Require ADMIN role)

#### Get All Conversations
```
GET /api/v1.0/admin/chat/conversations
Authorization: Bearer <admin-token>

Query Params:
  - status (optional): PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, ARCHIVED
  - page (default: 0)
  - size (default: 20)

Response:
{
  "content": [
    {
      "conversationId": "uuid",
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "status": "PENDING",
      "lastMessage": "I need help",
      "lastMessageAt": "2025-12-07T10:00:00",
      "hasUnreadMessages": true,
      "unreadCount": 3
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

#### Assign Conversation to Self
```
POST /api/v1.0/admin/chat/conversations/{conversationId}/assign
Authorization: Bearer <admin-token>

Response: Updated conversation object
```

#### Send Admin Message
```
POST /api/v1.0/admin/chat/messages
Content-Type: application/json
Authorization: Bearer <admin-token>

Request:
{
  "conversationId": "uuid",
  "content": "Your order ORD-251207-0001 is being processed"
}

Response: Message object
```

#### Get Customer Order History
```
GET /api/v1.0/admin/chat/conversations/{conversationId}/customer-orders
Authorization: Bearer <admin-token>

Response:
[
  {
    "orderId": "ORD-251207-0001",
    "status": "DELIVERED",
    "totalAmount": 500000,
    "createdAt": "2025-12-05T10:00:00"
  }
]
```

#### Mark Conversation as Completed
```
POST /api/v1.0/admin/chat/conversations/{conversationId}/complete
Authorization: Bearer <admin-token>

Response: Updated conversation with status COMPLETED
```

#### Get Unread Count
```
GET /api/v1.0/admin/chat/unread-count
Authorization: Bearer <admin-token>

Response:
{
  "unreadCount": 5
}
```

## WebSocket Integration

### Connection
```javascript
// Client connects to WebSocket
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to conversation messages
  stompClient.subscribe('/topic/conversation/' + conversationId, function(message) {
    const chatMessage = JSON.parse(message.body);
    displayMessage(chatMessage);
  });
  
  // Admin subscribes to notifications
  stompClient.subscribe('/topic/admin/notifications', function(notification) {
    const notif = JSON.parse(notification.body);
    showNotification(notif);
    playSound();
  });
});
```

### Send Message via WebSocket
```javascript
// Customer sends message
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  conversationId: 'uuid',
  content: 'Hello, I need help'
}));

// Admin sends message
stompClient.send('/app/admin.sendMessage', {}, JSON.stringify({
  conversationId: 'uuid',
  content: 'How can I help you?'
}));
```

### Message Format
```javascript
{
  messageId: "uuid",
  conversationId: "uuid",
  sender: "CUSTOMER" | "ADMIN",
  senderUserId: "uuid",
  senderName: "John Doe",
  content: "Message text",
  isRead: false,
  createdAt: "2025-12-07T10:00:00"
}
```

### Notification Format (Admin only)
```javascript
{
  conversationId: "uuid",
  message: "Last message preview",
  senderName: "John Doe",
  unreadCount: 5,
  timestamp: "2025-12-07T10:00:00"
}
```

## Frontend Implementation Guide

### React Customer Chat Component
```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function CustomerChat() {
  const [messages, setMessages] = useState([]);
  const [conversationId, setConversationId] = useState(null);
  const [stompClient, setStompClient] = useState(null);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    // Connect WebSocket
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const client = Stomp.over(socket);
    
    client.connect({}, () => {
      setStompClient(client);
    });

    return () => client.disconnect();
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
    const response = await fetch('/api/v1.0/chat/start', {
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

  const sendMessage = (content) => {
    if (stompClient && conversationId) {
      stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        conversationId,
        content
      }));
    }
  };

  return (
    <div className={`chat-widget ${isOpen ? 'open' : ''}`}>
      {/* Chat UI */}
    </div>
  );
}
```

### React Admin Dashboard Component
```jsx
function AdminChatDashboard() {
  const [conversations, setConversations] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    // Connect WebSocket
    const socket = new SockJS('http://localhost:8080/ws/chat');
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

  const fetchConversations = async (status = 'PENDING') => {
    const response = await fetch(
      `/api/v1.0/admin/chat/conversations?status=${status}`,
      {
        headers: { 'Authorization': `Bearer ${token}` }
      }
    );
    const data = await response.json();
    setConversations(data.content);
  };

  const assignToMe = async (conversationId) => {
    await fetch(`/api/v1.0/admin/chat/conversations/${conversationId}/assign`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    fetchConversations();
  };

  return (
    <div className="admin-dashboard">
      {/* Notification badge */}
      <div className="notification-badge">{unreadCount}</div>
      
      {/* Conversation list */}
      {conversations.map(conv => (
        <ConversationItem key={conv.conversationId} {...conv} />
      ))}
    </div>
  );
}
```

## Database Migration

Run the SQL script: `Documents/CHAT_MIGRATION.sql`

```bash
mysql -u username -p database_name < Documents/CHAT_MIGRATION.sql
```

## Security Configuration

WebSocket and chat endpoints are configured in `SecurityConfig.java`:
- `/ws/**` - Public (WebSocket endpoint)
- `/api/v1.0/chat/start` - Public (allow guest chat)
- `/api/v1.0/chat/**` - Public (authentication optional)
- `/api/v1.0/admin/chat/**` - Requires ADMIN role

## Testing

### Manual Testing Steps

1. **Start Backend**
   ```bash
   mvn spring-boot:run
   ```

2. **Test Guest Chat**
   ```bash
   curl -X POST http://localhost:8080/api/v1.0/chat/start \
     -H "Content-Type: application/json" \
     -d '{
       "guestName": "Test User",
       "guestEmail": "test@example.com",
       "initialMessage": "Hello"
     }'
   ```

3. **Test Admin Assignment**
   ```bash
   curl -X POST http://localhost:8080/api/v1.0/admin/chat/conversations/{id}/assign \
     -H "Authorization: Bearer {admin-token}"
   ```

4. **Test WebSocket** - Use browser console or Postman

## Deployment Checklist

- [ ] Run database migration
- [ ] Update CORS configuration for production domain
- [ ] Configure WebSocket endpoint URL in frontend
- [ ] Test notification sound in production
- [ ] Set up monitoring for WebSocket connections
- [ ] Configure rate limiting for chat messages

## Troubleshooting

### WebSocket Connection Fails
- Check CORS configuration in `WebSocketConfig.java`
- Verify firewall allows WebSocket connections
- Check SockJS fallback is working

### Messages Not Delivered
- Verify conversation ID is correct
- Check WebSocket subscription is active
- Ensure user is authenticated for private conversations

### Admin Not Receiving Notifications
- Verify admin is subscribed to `/topic/admin/notifications`
- Check browser notification permissions
- Verify sound file is accessible

## Future Enhancements

- [ ] File/image attachments in chat
- [ ] Chat message search
- [ ] Conversation tags/categories
- [ ] Canned responses for admins
- [ ] Chat analytics dashboard
- [ ] Multi-language support
- [ ] Mobile app notifications

## Date: December 7, 2025
## Status: Implementation Ready

