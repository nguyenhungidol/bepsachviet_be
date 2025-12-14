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
  ChatConversationResponse getConversationById(String conversationId, String adminEmail);
  ChatMessageResponse sendMessage(String conversationId, SendChatMessageRequest request, String senderEmail, boolean isAdmin);
  List<ChatMessageResponse> getMessages(String conversationId, String userEmail);
  List<ChatMessageResponse> getMessagesForAdmin(String conversationId, String adminEmail);
  ChatConversationResponse closeConversation(String conversationId, String userEmail);
  ChatConversationResponse reopenConversation(String conversationId, String userEmail);
  Page<ChatConversationResponse> getAllConversations(ChatStatus status, Pageable pageable);
  Long getPendingCount();
  ChatConversationResponse assignToAdmin(String conversationId, String adminEmail);
  ChatMessageResponse adminSendMessage(String conversationId, SendChatMessageRequest request, String adminEmail);
  ChatConversationResponse finishConversation(String conversationId, String adminEmail);
  List<OrderResponse> getCustomerOrders(String conversationId, String adminEmail);
}

