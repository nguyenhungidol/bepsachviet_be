package com.doan.bepsachviet_be.io.Response;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.constant.PaymentMethod;
import com.doan.bepsachviet_be.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

  private Long id;
  private String orderId;
  private String userId;
  private String userName;
  private String userEmail;
  private String deliveryName;
  private String deliveryPhone;
  private String deliveryAddress;
  private String notes;
  private OrderStatus status;
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
  private String transactionId;
  private BigDecimal totalAmount;
  private List<OrderItemResponse> orderItems;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}

