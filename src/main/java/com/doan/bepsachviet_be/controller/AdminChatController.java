// AdminChatController.java - Admin Chat API
// Path: src/main/java/com/doan/bepsachviet_be/controller/AdminChatController.java

package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.constant.ChatStatus;
import com.doan.bepsachviet_be.io.Request.SendChatMessageRequest;
import com.doan.bepsachviet_be.io.Response.ChatConversationResponse;
import com.doan.bepsachviet_be.io.Response.ChatMessageResponse;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import com.doan.bepsachviet_be.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatController {

  private final ChatService chatService;

  @GetMapping("/conversations")
  public ResponseEntity<Page<ChatConversationResponse>> getAllConversations(
      @RequestParam(required = false) ChatStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ChatConversationResponse> conversations = chatService.getAllConversations(status, pageable);
    return ResponseEntity.ok(conversations);
  }

  @GetMapping("/conversations/pending-count")
  public ResponseEntity<Map<String, Long>> getPendingCount() {
    Long count = chatService.getPendingCount();
    return ResponseEntity.ok(Map.of("pendingCount", count));
  }

  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<ChatConversationResponse> getConversation(
      @PathVariable String conversationId,
      Authentication authentication) {
    ChatConversationResponse response = chatService.getConversationById(conversationId, authentication.getName());
    log.info("Admin fetched conversation: {}", conversationId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<List<ChatMessageResponse>> getMessages(
      @PathVariable String conversationId,
      Authentication authentication) {
    List<ChatMessageResponse> messages = chatService.getMessagesForAdmin(conversationId, authentication.getName());
    log.info("Admin fetched messages for conversation: {}", conversationId);
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/conversations/{conversationId}/assign")
  public ResponseEntity<ChatConversationResponse> assignToMe(
      @PathVariable String conversationId,
      Authentication authentication) {
    ChatConversationResponse response = chatService.assignToAdmin(conversationId, authentication.getName());
    log.info("Admin {} assigned conversation {}", authentication.getName(), conversationId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<ChatMessageResponse> sendMessage(
      @PathVariable String conversationId,
      @Valid @RequestBody SendChatMessageRequest request,
      Authentication authentication) {
    ChatMessageResponse response = chatService.adminSendMessage(conversationId, request, authentication.getName());
    log.info("Admin sent message in conversation: {}", conversationId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/conversations/{conversationId}/finish")
  public ResponseEntity<ChatConversationResponse> finishConversation(
      @PathVariable String conversationId,
      Authentication authentication) {
    ChatConversationResponse response = chatService.finishConversation(conversationId, authentication.getName());
    log.info("Conversation {} marked as finished", conversationId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/conversations/{conversationId}/customer-orders")
  public ResponseEntity<List<OrderResponse>> getCustomerOrders(
      @PathVariable String conversationId,
      Authentication authentication) {
    List<OrderResponse> orders = chatService.getCustomerOrders(conversationId, authentication.getName());
    log.info("Fetched {} orders for conversation {}", orders.size(), conversationId);
    return ResponseEntity.ok(orders);
  }
}

