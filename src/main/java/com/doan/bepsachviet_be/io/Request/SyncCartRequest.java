package com.doan.bepsachviet_be.io.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncCartRequest {
  private List<CartItemRequest> items;
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CartItemRequest {
    private String productId;
    private Integer quantity;
  }
}