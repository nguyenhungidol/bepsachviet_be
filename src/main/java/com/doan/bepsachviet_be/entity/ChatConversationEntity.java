package com.doan.bepsachviet_be.entity;

import com.doan.bepsachviet_be.constant.ChatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatConversationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String conversationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Column
  private String guestName;

  @Column
  private String guestEmail;

  @Column
  private String guestPhone;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_admin_id")
  private UserEntity assignedAdmin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ChatStatus status = ChatStatus.PENDING;

  @Column(length = 500)
  private String lastMessage;

  @Column
  private Timestamp lastMessageAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean hasUnreadMessages = true;

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ChatMessageEntity> messages = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  private Timestamp updatedAt;
}

