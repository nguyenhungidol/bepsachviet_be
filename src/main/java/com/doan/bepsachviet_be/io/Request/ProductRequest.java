package com.doan.bepsachviet_be.io.Request;

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
  private String name;
  private String description;
  private String imageSrc;
  private BigDecimal price;
  private String ocUrl;
  private String categoryId;
}

