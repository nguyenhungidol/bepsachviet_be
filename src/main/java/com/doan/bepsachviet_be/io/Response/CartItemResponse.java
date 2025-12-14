package com.doan.bepsachviet_be.io.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
  private Long itemId;
  private String productId;
  private String productName;
  private String productDescription;
  private String productImageSrc;
  private BigDecimal productPrice;
  private Integer quantity;
  private BigDecimal subtotal;
  private Boolean isProductActive; // To check if product is still available
  private Integer availableStock; // Current stock available
  private Timestamp createdAt;
  private Timestamp updatedAt;
}