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

