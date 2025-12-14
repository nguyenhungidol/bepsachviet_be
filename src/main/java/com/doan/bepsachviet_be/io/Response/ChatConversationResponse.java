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

  // Customer type: "USER" for logged-in user, "GUEST" for anonymous guest
  private String customerType;

  // User info (for logged-in customers)
  private String userId;
  private String userName;
  private String userEmail;
  private String userPhone;

  // Guest info (for anonymous customers)
  private String guestName;
  private String guestEmail;
  private String guestPhone;

  // Combined customer info for easy frontend use
  private String customerName;   // Either userName or guestName
  private String customerEmail;  // Either userEmail or guestEmail
  private String customerPhone;  // Either userPhone or guestPhone

  // Admin info
  private String assignedAdminId;
  private String assignedAdminName;

  // Status
  private ChatStatus status;
  private String lastMessage;
  private Timestamp lastMessageAt;
  private Boolean hasUnreadMessages;
  private Long unreadCount;

  // Messages
  private List<ChatMessageResponse> messages;

  // Timestamps
  private Timestamp createdAt;
  private Timestamp updatedAt;
}

