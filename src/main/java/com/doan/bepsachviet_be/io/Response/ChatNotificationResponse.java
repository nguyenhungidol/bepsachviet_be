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

