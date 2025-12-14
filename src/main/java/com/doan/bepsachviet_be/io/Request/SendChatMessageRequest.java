package com.doan.bepsachviet_be.io.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendChatMessageRequest {
  @NotBlank(message = "Message content is required")
  private String content;
}

