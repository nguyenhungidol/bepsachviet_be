package com.doan.bepsachviet_be.io.Response;
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
public class CartResponse {
  private Long cartId;
  private String userId;
  private List<CartItemResponse> items;
  private Integer totalItems;
  private BigDecimal totalPrice;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}