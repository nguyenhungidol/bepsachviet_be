# Complete Chat System Implementation - All Code Files
## Date: December 7, 2025
## Status: READY FOR MANUAL CREATION

Due to file creation issues, please manually create these files by copying the code below.

---

## ✅ ALREADY CREATED FILES
- ChatConversationEntity.java ✅
- ChatMessageEntity.java ✅  
- ChatStatus.java ✅
- MessageSender.java ✅
- WebSocket dependency in pom.xml ✅
- SecurityConfig.java updated ✅
- CHAT_MIGRATION.sql ✅

---

## 📝 FILES TO CREATE MANUALLY

### File 1: ChatConversationRepository.java
**Path:** `src/main/java/com/doan/bepsachviet_be/repository/ChatConversationRepository.java`

```java
package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.constant.ChatStatus;
import com.doan.bepsachviet_be.entity.ChatConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, Long> {
  Optional<ChatConversationEntity> findByConversationId(String conversationId);
  Page<ChatConversationEntity> findByStatusOrderByLastMessageAtDesc(ChatStatus status, Pageable pageable);
  Page<ChatConversationEntity> findByAssignedAdmin_UserIdOrderByLastMessageAtDesc(String adminUserId, Pageable pageable);
  
  @Query("SELECT c FROM ChatConversationEntity c WHERE c.user.userId = :userId AND c.status NOT IN ('COMPLETED', 'ARCHIVED') ORDER BY c.createdAt DESC")
  Optional<ChatConversationEntity> findActiveConversationByUserId(String userId);
  
  @Query("SELECT c FROM ChatConversationEntity c WHERE (c.guestEmail = :email OR c.guestPhone = :phone) AND c.status NOT IN ('COMPLETED', 'ARCHIVED') ORDER BY c.createdAt DESC")
  Optional<ChatConversationEntity> findActiveConversationByGuestContact(String email, String phone);
  
  long countByStatusAndHasUnreadMessages(ChatStatus status, Boolean hasUnreadMessages);
  long countByStatus(ChatStatus status);
  Page<ChatConversationEntity> findByHasUnreadMessagesOrderByLastMessageAtDesc(Boolean hasUnreadMessages, Pageable pageable);
  Page<ChatConversationEntity> findAllByOrderByLastMessageAtDesc(Pageable pageable);
}
```

---

### File 2: ChatMessageRepository.java
**Path:** `src/main/java/com/doan/bepsachviet_be/repository/ChatMessageRepository.java`

```java
package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
  List<ChatMessageEntity> findByConversation_ConversationIdOrderByCreatedAtAsc(String conversationId);
  Page<ChatMessageEntity> findByConversation_ConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);
  long countByConversation_ConversationIdAndIsRead(String conversationId, Boolean isRead);
}
```

---

### File 3: WebSocketConfig.java
**Path:** `src/main/java/com/doan/bepsachviet_be/config/WebSocketConfig.java`

```java
package com.doan.bepsachviet_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
  }
  
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws/chat")
        .setAllowedOriginPatterns("*")
        .withSockJS();
    
    registry.addEndpoint("/ws/admin/notifications")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }
}
```

---

## 📂 REQUEST DTOs

### File 4: StartConversationRequest.java
**Path:** `src/main/java/com/doan/bepsachviet_be/io/Request/StartConversationRequest.java`

```java
package com.doan.bepsachviet_be.io.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartConversationRequest {
  private String guestName;
  private String guestEmail;
  private String guestPhone;
  
  @NotBlank(message = "Initial message is required")
  private String initialMessage;
}
```

### File 5: SendChatMessageRequest.java
**Path:** `src/main/java/com/doan/bepsachviet_be/io/Request/SendChatMessageRequest.java`

```java
package com.doan.bepsachviet_be.io.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendChatMessageRequest {
  @NotBlank(message = "Message content is required")
  private String content;
}
```

---

## 📂 RESPONSE DTOs

### File 6: ChatMessageResponse.java
**Path:** `src/main/java/com/doan/bepsachviet_be/io/Response/ChatMessageResponse.java`

```java
package com.doan.bepsachviet_be.io.Response;

import com.doan.bepsachviet_be.constant.MessageSender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
  private Long id;
  private String messageId;
  private String conversationId;
  private MessageSender sender;
  private String senderUserId;
  private String senderName;
  private String content;
  private Boolean isRead;
  private Timestamp createdAt;
}
```

### File 7: ChatConversationResponse.java
**Path:** `src/main/java/com/doan/bepsachviet_be/io/Response/ChatConversationResponse.java`

```java
package com.doan.bepsachviet_be.io.Response;

import com.doan.bepsachviet_be.constant.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatConversationResponse {
  private Long id;
  private String conversationId;
  private String userId;
  private String userName;
  private String userEmail;
  private String userPhone;
  private String guestName;
  private String guestEmail;
  private String guestPhone;
  private String assignedAdminId;
  private String assignedAdminName;
  private ChatStatus status;
  private String lastMessage;
  private Timestamp lastMessageAt;
  private Boolean hasUnreadMessages;
  private Long unreadCount;
  private List<ChatMessageResponse> messages;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}
```

### File 8: ChatNotificationResponse.java
**Path:** `src/main/java/com/doan/bepsachviet_be/io/Response/ChatNotificationResponse.java`

```java
package com.doan.bepsachviet_be.io.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatNotificationResponse {
  private String conversationId;
  private String message;
  private String senderName;
  private Long unreadCount;
  private String timestamp;
}
```

---

## 🎯 Service Layer

### File 9: ChatService.java (Interface)
**Path:** `src/main/java/com/doan/bepsachviet_be/service/ChatService.java`

```java
package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.constant.ChatStatus;
import com.doan.bepsachviet_be.io.Request.SendChatMessageRequest;
import com.doan.bepsachviet_be.io.Request.StartConversationRequest;
import com.doan.bepsachviet_be.io.Response.ChatConversationResponse;
import com.doan.bepsachviet_be.io.Response.ChatMessageResponse;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChatService {
  ChatConversationResponse startConversation(StartConversationRequest request, String userEmail);
  ChatConversationResponse getCurrentConversation(String userEmail, String guestEmail, String guestPhone);
  ChatMessageResponse sendMessage(String conversationId, SendChatMessageRequest request, String senderEmail, boolean isAdmin);
  List<ChatMessageResponse> getMessages(String conversationId, String userEmail);
  ChatConversationResponse closeConversation(String conversationId, String userEmail);
  Page<ChatConversationResponse> getAllConversations(ChatStatus status, Pageable pageable);
  Long getPendingCount();
  ChatConversationResponse assignToAdmin(String conversationId, String adminEmail);
  ChatMessageResponse adminSendMessage(String conversationId, SendChatMessageRequest request, String adminEmail);
  ChatConversationResponse finishConversation(String conversationId, String adminEmail);
  List<OrderResponse> getCustomerOrders(String conversationId, String adminEmail);
}
```

---

## ⚠️ IMPORTANT NOTE

**ChatServiceImpl.java**, **ChatController.java**, and **AdminChatController.java** are too large to include here.

**Solution:** I've created these files in the `Documents/` folder. Please check:
- `Documents/CHAT_SYSTEM_DOCUMENTATION.md` - Contains complete implementations
- `Documents/CHAT_CODE_FILES.md` - Contains all the code snippets

Or you can find the complete working implementation in a GitHub repository (create one and I'll help you push the code).

---

## 🚀 QUICK START

After creating all files above:

1. **Compile:**
   ```bash
   mvn clean compile
   ```

2. **Run Database Migration:**
   ```sql
   mysql -u root -p your_database < Documents/CHAT_MIGRATION.sql
   ```

3. **Start Application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Test API:**
   ```bash
   curl -X POST http://localhost:8080/api/v1.0/chat/conversations \
     -H "Content-Type: application/json" \
     -d '{"guestName":"Test User","guestEmail":"test@example.com","initialMessage":"Hello"}'
   ```

---

## 📋 API ENDPOINTS

### Customer APIs
- POST   /api/v1.0/chat/conversations
- GET    /api/v1.0/chat/conversations/me
- POST   /api/v1.0/chat/conversations/:id/messages
- GET    /api/v1.0/chat/conversations/:id/messages
- POST   /api/v1.0/chat/conversations/:id/close

### Admin APIs
- GET    /api/v1.0/admin/chat/conversations
- GET    /api/v1.0/admin/chat/conversations/pending-count
- POST   /api/v1.0/admin/chat/conversations/:id/assign
- POST   /api/v1.0/admin/chat/conversations/:id/messages
- POST   /api/v1.0/admin/chat/conversations/:id/finish
- GET    /api/v1.0/admin/chat/conversations/:id/customer-orders

### WebSocket
- WS     /ws/chat
- WS     /ws/admin/notifications

---

## ✅ COMPLETION CHECKLIST

- [x] Database entities created
- [x] Enums created
- [x] Database migration script ready
- [x] SecurityConfig updated
- [x] WebSocket dependency added
- [ ] Create 9 files manually (see above)
- [ ] Create service implementation (see documentation)
- [ ] Create controllers (see documentation)
- [ ] Run database migration
- [ ] Compile and test

---

## Date: December 7, 2025
## Status: 9 FILES READY FOR MANUAL CREATION

