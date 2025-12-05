package com.doan.bepsachviet_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal price;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal subtotal;

  @CreationTimestamp
  @Column(updatable = false)
  private Timestamp createdAt;
}

