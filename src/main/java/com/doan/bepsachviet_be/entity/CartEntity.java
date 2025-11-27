package com.doan.bepsachviet_be.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CartItemEntity> items = new ArrayList<>();
  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;
  @UpdateTimestamp
  private Timestamp updatedAt;
}