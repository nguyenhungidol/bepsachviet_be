package com.doan.bepsachviet_be.io.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemRequest {
  private Integer quantity;
}