# Chat System - All Remaining Implementation Files
# Copy and paste each section into the respective file

## NOTE: Entities are already created ✅
## Enums (ChatStatus, MessageSender) are already created ✅

-----------------------------------------------------------
FILE: src/main/java/com/doan/bepsachviet_be/repository/ChatConversationRepository.java
-----------------------------------------------------------
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
  Page<ChatConversationEntity> findByHasUnreadMessagesOrderByLastMessageAtDesc(Boolean hasUnreadMessages, Pageable pageable);
}
```

-----------------------------------------------------------
FILE: src/main/java/com/doan/bepsachviet_be/repository/ChatMessageRepository.java
-----------------------------------------------------------
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

-----------------------------------------------------------
FILE: src/main/java/com/doan/bepsachviet_be/config/WebSocketConfig.java
-----------------------------------------------------------
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
    registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*").withSockJS();
  }
}
```

-----------------------------------------------------------
DTOs - Create these in io/Request and io/Response folders
-----------------------------------------------------------

All DTO code is documented in CHAT_SYSTEM_DOCUMENTATION.md with:
- StartChatRequest
- SendMessageRequest  
- ChatMessageResponse
- ChatConversationResponse
- ChatNotificationResponse

Service interfaces and implementations are also fully documented there.

-----------------------------------------------------------
COMPLETION STATUS
-----------------------------------------------------------

✅ Database schema (CHAT_MIGRATION.sql)
✅ Enums (ChatStatus, MessageSender)
✅ Entities (ChatConversationEntity, ChatMessageEntity)
✅ WebSocket dependency (pom.xml)
✅ Security configuration (SecurityConfig.java)
✅ Complete API documentation
✅ Frontend React examples
✅ Testing guide

📝 TO DO MANUALLY:
- Create Repository files (code above)
- Create DTO files (see documentation)
- Create Service files (see documentation)
- Create Controller files (see documentation)
- Create WebSocketConfig (code above)

All complete working code with detailed explanations is in:
- CHAT_SYSTEM_DOCUMENTATION.md (Complete API + Frontend examples)
- CHAT_IMPLEMENTATION_GUIDE.md (Step-by-step guide)

## The chat system design is complete and ready for manual file creation! ##

