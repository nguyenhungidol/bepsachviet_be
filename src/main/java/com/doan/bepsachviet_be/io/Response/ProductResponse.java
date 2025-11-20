package com.doan.bepsachviet_be.io.Response;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
  private String productId;
  private String name;
  private String description;
  private String imageSrc;
  private BigDecimal price;
  private String ocUrl;
  private String categoryId;
  private String categoryName;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}

