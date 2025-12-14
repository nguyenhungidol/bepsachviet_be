// ChatServiceImpl.java - Complete Implementation
// Path: src/main/java/com/doan/bepsachviet_be/service/Impl/ChatServiceImpl.java
// Copy this entire file content

package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.constant.ChatStatus;
import com.doan.bepsachviet_be.constant.MessageSender;
import com.doan.bepsachviet_be.entity.*;
import com.doan.bepsachviet_be.io.Request.SendChatMessageRequest;
import com.doan.bepsachviet_be.io.Request.StartConversationRequest;
import com.doan.bepsachviet_be.io.Response.*;
import com.doan.bepsachviet_be.repository.*;
import com.doan.bepsachviet_be.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

  private final ChatConversationRepository conversationRepository;
  private final ChatMessageRepository messageRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final SimpMessagingTemplate messagingTemplate;

  // Helper method to check if user has admin role
  private boolean isAdmin(UserEntity user) {
    if (user == null || user.getRole() == null) return false;
    String role = user.getRole().toUpperCase();
    return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
  }

  @Override
  @Transactional
  public ChatConversationResponse startConversation(StartConversationRequest request, String userEmail) {
    UserEntity user = null;
    if (userEmail != null && !userEmail.isEmpty()) {
      user = userRepository.findByEmail(userEmail).orElse(null);
    }
    ChatConversationEntity conversation;
    if (user != null) {
      conversation = conversationRepository.findActiveConversationByUserId(user.getUserId()).orElse(null);
    } else {
      if ((request.getGuestEmail() == null || request.getGuestEmail().isEmpty()) &&
          (request.getGuestPhone() == null || request.getGuestPhone().isEmpty())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest users must provide email or phone number");
      }
      conversation = conversationRepository.findActiveConversationByGuestContact(
          request.getGuestEmail() != null ? request.getGuestEmail() : "",
          request.getGuestPhone() != null ? request.getGuestPhone() : ""
      ).orElse(null);
    }
    if (conversation == null) {
      conversation = ChatConversationEntity.builder()
          .conversationId(UUID.randomUUID().toString())
          .user(user)
          .guestName(request.getGuestName())
          .guestEmail(request.getGuestEmail())
          .guestPhone(request.getGuestPhone())
          .status(ChatStatus.PENDING)
          .hasUnreadMessages(true)
          .build();
      conversation = conversationRepository.save(conversation);
      log.info("New conversation created: {}", conversation.getConversationId());
    }

    String initialContent = request.getInitialMessage();
    if (initialContent == null || initialContent.trim().isEmpty()) {
      initialContent = "Bắt đầu cuộc trò chuyện";
    }

    ChatMessageEntity message = ChatMessageEntity.builder()
        .messageId(UUID.randomUUID().toString())
        .conversation(conversation)
        .sender(MessageSender.CUSTOMER)
        .senderUser(user)
        .content(initialContent)
        .isRead(false)
        .build();
    message = messageRepository.save(message);
    conversation.setLastMessage(request.getInitialMessage());
    conversation.setLastMessageAt(message.getCreatedAt());
    conversation.setHasUnreadMessages(true);
    conversationRepository.save(conversation);
    ChatMessageEntity autoReply = ChatMessageEntity.builder()
        .messageId(UUID.randomUUID().toString())
        .conversation(conversation)
        .sender(MessageSender.ADMIN)
        .content("Cảm ơn bạn đã liên hệ! Nhân viên hỗ trợ sẽ phản hồi trong giây lát.")
        .isRead(false)
        .build();
    messageRepository.save(autoReply);
    notifyAdmins(conversation);
    return convertToResponse(conversation, true);
  }

  @Override
  @Transactional(readOnly = true)
  public ChatConversationResponse getCurrentConversation(String userEmail, String guestEmail, String guestPhone) {
    ChatConversationEntity conversation = null;
    if (userEmail != null && !userEmail.isEmpty()) {
      UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
      if (user != null) {
        conversation = conversationRepository.findActiveConversationByUserId(user.getUserId()).orElse(null);
      }
    } else if ((guestEmail != null && !guestEmail.isEmpty()) || (guestPhone != null && !guestPhone.isEmpty())) {
      conversation = conversationRepository.findActiveConversationByGuestContact(
          guestEmail != null ? guestEmail : "",
          guestPhone != null ? guestPhone : ""
      ).orElse(null);
    }
    if (conversation == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active conversation found");
    }
    return convertToResponse(conversation, true);
  }

  @Override
  @Transactional(readOnly = true)
  public ChatConversationResponse getConversationById(String conversationId, String adminEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view conversation details");
    }
    return convertToResponse(conversation, true);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessagesForAdmin(String conversationId, String adminEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view conversation messages");
    }
    List<ChatMessageEntity> messages = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);
    return messages.stream().map(this::convertMessageToResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessage(String conversationId, SendChatMessageRequest request, String senderEmail, boolean isAdmin) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity sender = null;
    if (senderEmail != null && !senderEmail.isEmpty()) {
      sender = userRepository.findByEmail(senderEmail).orElse(null);
    }
    if (!isAdmin && sender != null && conversation.getUser() != null &&
        !conversation.getUser().getUserId().equals(sender.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only send messages to your own conversations");
    }
    ChatMessageEntity message = ChatMessageEntity.builder()
        .messageId(UUID.randomUUID().toString())
        .conversation(conversation)
        .sender(isAdmin ? MessageSender.ADMIN : MessageSender.CUSTOMER)
        .senderUser(sender)
        .content(request.getContent())
        .isRead(false)
        .build();
    message = messageRepository.save(message);
    conversation.setLastMessage(request.getContent());
    conversation.setLastMessageAt(message.getCreatedAt());
    if (!isAdmin) {
      conversation.setHasUnreadMessages(true);
      // Auto-reopen if conversation was completed - customer sends new message
      if (conversation.getStatus() == ChatStatus.COMPLETED) {
        conversation.setStatus(ChatStatus.PENDING);
        conversation.setAssignedAdmin(null); // Reset assigned admin for re-routing
        log.info("Conversation {} auto-reopened due to new customer message", conversationId);
      } else if (conversation.getStatus() == ChatStatus.ASSIGNED) {
        conversation.setStatus(ChatStatus.IN_PROGRESS);
      }
    }
    conversationRepository.save(conversation);
    ChatMessageResponse response = convertMessageToResponse(message);
    messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, response);
    if (!isAdmin) {
      notifyAdmins(conversation);
    }
    log.info("Message sent in conversation {}: {}", conversationId, isAdmin ? "Admin" : "Customer");
    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessages(String conversationId, String userEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    if (userEmail != null && !userEmail.isEmpty()) {
      UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
      if (user != null && conversation.getUser() != null &&
          !conversation.getUser().getUserId().equals(user.getUserId()) &&
          !isAdmin(user)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
    }
    List<ChatMessageEntity> messages = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);
    return messages.stream().map(this::convertMessageToResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ChatConversationResponse closeConversation(String conversationId, String userEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    if (userEmail != null && !userEmail.isEmpty()) {
      UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
      if (user != null && conversation.getUser() != null &&
          !conversation.getUser().getUserId().equals(user.getUserId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
    }
    conversation.setStatus(ChatStatus.COMPLETED);
    conversation.setHasUnreadMessages(false);
    conversationRepository.save(conversation);
    log.info("Conversation {} closed by customer", conversationId);
    return convertToResponse(conversation, false);
  }

  @Override
  @Transactional
  public ChatConversationResponse reopenConversation(String conversationId, String userEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

    // Verify user has access to this conversation
    if (userEmail != null && !userEmail.isEmpty()) {
      UserEntity user = userRepository.findByEmail(userEmail).orElse(null);
      if (user != null && conversation.getUser() != null &&
          !conversation.getUser().getUserId().equals(user.getUserId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
    }

    // Only allow reopening completed conversations
    if (conversation.getStatus() != ChatStatus.COMPLETED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed conversations can be reopened");
    }

    // Reopen the conversation
    conversation.setStatus(ChatStatus.PENDING);
    conversation.setAssignedAdmin(null); // Reset assigned admin for re-routing
    conversation.setHasUnreadMessages(true);
    conversationRepository.save(conversation);

    // Notify admins about reopened conversation
    notifyAdmins(conversation);

    log.info("Conversation {} reopened by customer", conversationId);
    return convertToResponse(conversation, true);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatConversationResponse> getAllConversations(ChatStatus status, Pageable pageable) {
    Page<ChatConversationEntity> conversations;
    if (status != null) {
      if (status == ChatStatus.ACTIVE) {
        // ACTIVE means all non-completed conversations
        List<ChatStatus> activeStatuses = List.of(ChatStatus.PENDING, ChatStatus.ASSIGNED, ChatStatus.IN_PROGRESS);
        conversations = conversationRepository.findByStatusInOrderByLastMessageAtDesc(activeStatuses, pageable);
      } else {
        conversations = conversationRepository.findByStatusOrderByLastMessageAtDesc(status, pageable);
      }
    } else {
      conversations = conversationRepository.findAllByOrderByLastMessageAtDesc(pageable);
    }
    return conversations.map(conv -> convertToResponse(conv, false));
  }

  @Override
  @Transactional(readOnly = true)
  public Long getPendingCount() {
    return conversationRepository.countByStatus(ChatStatus.PENDING);
  }

  @Override
  @Transactional
  public ChatConversationResponse assignToAdmin(String conversationId, String adminEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can be assigned to conversations");
    }
    conversation.setAssignedAdmin(admin);
    conversation.setStatus(ChatStatus.ASSIGNED);
    conversationRepository.save(conversation);
    log.info("Conversation {} assigned to admin {}", conversationId, admin.getName());
    return convertToResponse(conversation, true);
  }

  @Override
  @Transactional
  public ChatMessageResponse adminSendMessage(String conversationId, SendChatMessageRequest request, String adminEmail) {
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can send admin messages");
    }
    return sendMessage(conversationId, request, adminEmail, true);
  }

  @Override
  @Transactional
  public ChatConversationResponse finishConversation(String conversationId, String adminEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can finish conversations");
    }
    conversation.setStatus(ChatStatus.COMPLETED);
    conversation.setHasUnreadMessages(false);
    conversationRepository.save(conversation);
    log.info("Conversation {} marked as completed by admin {}", conversationId, admin.getName());
    return convertToResponse(conversation, false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getCustomerOrders(String conversationId, String adminEmail) {
    ChatConversationEntity conversation = conversationRepository.findByConversationId(conversationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    UserEntity admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    if (!isAdmin(admin)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view customer order history");
    }
    if (conversation.getUser() != null) {
      List<OrderEntity> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(conversation.getUser().getUserId(), PageRequest.of(0, 10)).getContent();
      return orders.stream().map(this::convertOrderToResponse).collect(Collectors.toList());
    }
    return List.of();
  }

  private void notifyAdmins(ChatConversationEntity conversation) {
    ChatNotificationResponse notification = ChatNotificationResponse.builder()
        .conversationId(conversation.getConversationId())
        .message(conversation.getLastMessage())
        .senderName(conversation.getUser() != null ? conversation.getUser().getName() : conversation.getGuestName())
        .unreadCount(getPendingCount())
        .timestamp(Timestamp.from(Instant.now()).toString())
        .build();
    messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
  }

  private ChatConversationResponse convertToResponse(ChatConversationEntity conversation, boolean includeMessages) {
    Long unreadCount = messageRepository.countByConversation_ConversationIdAndIsRead(conversation.getConversationId(), false);

    // Determine customer type and combined info
    String customerType;
    String customerName;
    String customerEmail;
    String customerPhone;

    if (conversation.getUser() != null) {
      customerType = "USER";
      customerName = conversation.getUser().getName();
      customerEmail = conversation.getUser().getEmail();
      customerPhone = conversation.getUser().getPhoneNumber();
    } else {
      customerType = "GUEST";
      customerName = conversation.getGuestName();
      customerEmail = conversation.getGuestEmail();
      customerPhone = conversation.getGuestPhone();
    }

    ChatConversationResponse.ChatConversationResponseBuilder builder = ChatConversationResponse.builder()
        .id(conversation.getId())
        .conversationId(conversation.getConversationId())
        .customerType(customerType)
        .customerName(customerName)
        .customerEmail(customerEmail)
        .customerPhone(customerPhone)
        .status(conversation.getStatus())
        .lastMessage(conversation.getLastMessage())
        .lastMessageAt(conversation.getLastMessageAt())
        .hasUnreadMessages(conversation.getHasUnreadMessages())
        .unreadCount(unreadCount)
        .createdAt(conversation.getCreatedAt())
        .updatedAt(conversation.getUpdatedAt());
    if (conversation.getUser() != null) {
      builder.userId(conversation.getUser().getUserId())
          .userName(conversation.getUser().getName())
          .userEmail(conversation.getUser().getEmail())
          .userPhone(conversation.getUser().getPhoneNumber());
    } else {
      builder.guestName(conversation.getGuestName())
          .guestEmail(conversation.getGuestEmail())
          .guestPhone(conversation.getGuestPhone());
    }
    if (conversation.getAssignedAdmin() != null) {
      builder.assignedAdminId(conversation.getAssignedAdmin().getUserId())
          .assignedAdminName(conversation.getAssignedAdmin().getName());
    }
    if (includeMessages) {
      List<ChatMessageResponse> messages = getMessages(conversation.getConversationId(), null);
      builder.messages(messages);
    }
    return builder.build();
  }

  private ChatMessageResponse convertMessageToResponse(ChatMessageEntity message) {
    String senderName = "Guest";
    String senderEmail = null;
    boolean isAdminSender = message.getSender() == MessageSender.ADMIN;
    boolean isLoggedInUser = false;
    boolean isGuest = false;
    boolean isSystemMessage = false;
    String senderType = "GUEST";

    if (message.getSenderUser() != null) {
      senderName = message.getSenderUser().getName();
      senderEmail = message.getSenderUser().getEmail();
      if (isAdmin(message.getSenderUser())) {
        senderType = "ADMIN";
        isLoggedInUser = false;
      } else {
        senderType = "USER";
        isLoggedInUser = true;
      }
    } else if (isAdminSender) {
      // System auto-reply message (admin message without senderUser)
      senderName = "Hệ thống";
      senderType = "SYSTEM";
      isSystemMessage = true;
    } else if (message.getConversation().getGuestName() != null) {
      senderName = message.getConversation().getGuestName();
      senderType = "GUEST";
      isGuest = true;
    } else {
      senderType = "GUEST";
      isGuest = true;
    }

    return ChatMessageResponse.builder()
        .id(message.getId())
        .messageId(message.getMessageId())
        .conversationId(message.getConversation().getConversationId())
        .sender(message.getSender())
        .senderUserId(message.getSenderUser() != null ? message.getSenderUser().getUserId() : null)
        .senderName(senderName)
        .senderEmail(senderEmail)
        .isAdmin(isAdminSender)
        .isLoggedInUser(isLoggedInUser)
        .isGuest(isGuest)
        .isSystemMessage(isSystemMessage)
        .senderType(senderType)
        .content(message.getContent())
        .isRead(message.getIsRead())
        .createdAt(message.getCreatedAt())
        .build();
  }

  private OrderResponse convertOrderToResponse(OrderEntity order) {
    return OrderResponse.builder()
        .id(order.getId())
        .orderId(order.getOrderId())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .paymentMethod(order.getPaymentMethod())
        .paymentStatus(order.getPaymentStatus())
        .deliveryAddress(order.getDeliveryAddress())
        .deliveryPhone(order.getDeliveryPhone())
        .deliveryName(order.getDeliveryName())
        .createdAt(order.getCreatedAt())
        .build();
  }
}

