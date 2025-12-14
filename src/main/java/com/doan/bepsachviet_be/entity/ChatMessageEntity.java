package com.doan.bepsachviet_be.entity;

import com.doan.bepsachviet_be.constant.MessageSender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private ChatConversationEntity conversation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MessageSender sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_user_id")
  private UserEntity senderUser;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;
}

