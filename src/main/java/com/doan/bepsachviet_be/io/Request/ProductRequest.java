package com.doan.bepsachviet_be.io.Request;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
  @NotBlank(message = "Product ID is required")
  private String productId;

  @NotBlank(message = "Product name is required")
  private String name;

  private String description;
  private String imageSrc;
  private BigDecimal price;
  private String ocUrl;
  private Integer stockQuantity;
  private Boolean isActive;
  private String categoryId;
}

