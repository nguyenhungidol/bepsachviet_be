package com.doan.bepsachviet_be.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.doan.bepsachviet_be.entity.ChatConversationEntity;
import com.doan.bepsachviet_be.constant.ChatStatus;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, Long> {

  Optional<ChatConversationEntity> findByConversationId(String conversationId);

  Page<ChatConversationEntity> findByStatusOrderByLastMessageAtDesc(ChatStatus status, Pageable pageable);

  Page<ChatConversationEntity> findByAssignedAdmin_UserIdOrderByLastMessageAtDesc(String adminUserId, Pageable pageable);

  @Query("SELECT c FROM ChatConversationEntity c WHERE c.user.userId = :userId AND c.status NOT IN ('COMPLETED', 'ARCHIVED') ORDER BY c.createdAt DESC")
  Optional<ChatConversationEntity> findActiveConversationByUserId(String userId);

  @Query("SELECT c FROM ChatConversationEntity c WHERE (c.guestEmail = :email OR c.guestPhone = :phone) AND c.status NOT IN ('COMPLETED', 'ARCHIVED') ORDER BY c.createdAt DESC")
  Optional<ChatConversationEntity> findActiveConversationByGuestContact(String email, String phone);

  long countByStatusAndHasUnreadMessages(ChatStatus status, Boolean hasUnreadMessages);

  long countByStatus(ChatStatus status);

  @Query("SELECT c FROM ChatConversationEntity c WHERE c.status IN :statuses ORDER BY c.lastMessageAt DESC")
  Page<ChatConversationEntity> findByStatusInOrderByLastMessageAtDesc(java.util.List<ChatStatus> statuses, Pageable pageable);

  Page<ChatConversationEntity> findByHasUnreadMessagesOrderByLastMessageAtDesc(Boolean hasUnreadMessages, Pageable pageable);

  Page<ChatConversationEntity> findAllByOrderByLastMessageAtDesc(Pageable pageable);

}



