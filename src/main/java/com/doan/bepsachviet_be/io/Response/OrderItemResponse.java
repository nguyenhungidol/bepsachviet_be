package com.doan.bepsachviet_be.io.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {

  private Long id;
  private String productId;
  private String productName;
  private String productImage;
  private Integer quantity;
  private BigDecimal price;
  private BigDecimal subtotal;
}

