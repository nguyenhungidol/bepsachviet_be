package com.doan.bepsachviet_be.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import com.doan.bepsachviet_be.service.ChatService;
import com.doan.bepsachviet_be.io.Response.ChatMessageResponse;
import com.doan.bepsachviet_be.io.Response.ChatConversationResponse;
import com.doan.bepsachviet_be.io.Request.StartConversationRequest;
import com.doan.bepsachviet_be.io.Request.SendChatMessageRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

  private final ChatService chatService;

  // ------------------------------
  // 1. Start a new conversation
  // ------------------------------
  @PostMapping("/conversations")
  public ResponseEntity<ChatConversationResponse> startConversation(
      @Valid @RequestBody StartConversationRequest request,
      Authentication authentication) {

    String userEmail = authentication != null ? authentication.getName() : null;
    ChatConversationResponse response = chatService.startConversation(request, userEmail);

    log.info("Conversation started: {}", response.getConversationId());
    return ResponseEntity.ok(response);
  }

  // ------------------------------
  // 2. Close a conversation
  // ------------------------------
  @PostMapping("/conversations/{conversationId}/close")
  public ResponseEntity<ChatConversationResponse> closeConversation(
      @PathVariable String conversationId,
      Authentication authentication) {

    String userEmail = authentication != null ? authentication.getName() : null;
    ChatConversationResponse response = chatService.closeConversation(conversationId, userEmail);

    log.info("Conversation closed: {}", conversationId);
    return ResponseEntity.ok(response);
  }

  // ------------------------------
  // 2.1. Reopen a completed conversation
  // ------------------------------
  @PostMapping("/conversations/{conversationId}/reopen")
  public ResponseEntity<ChatConversationResponse> reopenConversation(
      @PathVariable String conversationId,
      Authentication authentication) {

    String userEmail = authentication != null ? authentication.getName() : null;
    ChatConversationResponse response = chatService.reopenConversation(conversationId, userEmail);

    log.info("Conversation reopened: {}", conversationId);
    return ResponseEntity.ok(response);
  }

  // ------------------------------
  // 3. Send a message
  // ------------------------------
  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<ChatMessageResponse> sendMessage(
      @PathVariable String conversationId,
      @Valid @RequestBody SendChatMessageRequest request,
      Authentication authentication) {

    String senderEmail = authentication != null ? authentication.getName() : null;
    ChatMessageResponse response =
        chatService.sendMessage(conversationId, request, senderEmail, false);

    log.info("Message sent in conversation: {}", conversationId);
    return ResponseEntity.ok(response);
  }

  // ------------------------------
  // 4. Get all messages in a conversation
  // ------------------------------
  @GetMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<List<ChatMessageResponse>> getMessages(
      @PathVariable String conversationId,
      Authentication authentication) {

    String userEmail = authentication != null ? authentication.getName() : null;
    List<ChatMessageResponse> messages =
        chatService.getMessages(conversationId, userEmail);

    return ResponseEntity.ok(messages);
  }

  // ------------------------------
  // 5. Get current conversation (for logged in or guest)
  // ------------------------------
  @GetMapping("/conversations/me")
  public ResponseEntity<ChatConversationResponse> getCurrentConversation(
      @RequestParam(required = false) String guestEmail,
      @RequestParam(required = false) String guestPhone,
      Authentication authentication) {

    String userEmail = authentication != null ? authentication.getName() : null;
    ChatConversationResponse response =
        chatService.getCurrentConversation(userEmail, guestEmail, guestPhone);

    return ResponseEntity.ok(response);
  }
}
