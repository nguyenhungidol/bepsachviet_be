package com.doan.bepsachviet_be.io.Request;

import lombok.Data;

@Data
public class StartConversationRequest {
  private String initialMessage;
  private String guestPhone;
  private String guestEmail;
  private String guestName;
}






