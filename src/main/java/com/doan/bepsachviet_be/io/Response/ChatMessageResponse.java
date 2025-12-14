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
  private MessageSender sender;           // CUSTOMER or ADMIN
  private String senderUserId;
  private String senderName;
  private String senderEmail;             // Email of sender (for logged-in users)
  private Boolean isAdmin;                // true if sender is admin
  private Boolean isLoggedInUser;         // true if sender is logged-in user (not guest)
  private Boolean isGuest;                // true if sender is guest (not logged in)
  private Boolean isSystemMessage;        // true for auto-reply messages
  private String senderType;              // "ADMIN", "USER", "GUEST", "SYSTEM" for easy frontend differentiation
  private String content;
  private Boolean isRead;
  private Timestamp createdAt;
}

