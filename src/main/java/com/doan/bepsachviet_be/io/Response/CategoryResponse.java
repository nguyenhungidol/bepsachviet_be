package com.doan.bepsachviet_be.io.Response;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
  private String categoryId;
  private String name;
  private String description;
  private Boolean active;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}

