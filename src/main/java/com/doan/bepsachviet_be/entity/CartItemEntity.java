package com.doan.bepsachviet_be.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private CartEntity cart;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;
  @Column(nullable = false)
  private Integer quantity;
  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;
  @UpdateTimestamp
  private Timestamp updatedAt;
}