package com.doan.bepsachviet_be.io.Request;

import com.doan.bepsachviet_be.constant.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {

  @NotBlank(message = "Title is required")
  private String title;

  private String slug;

  private String shortDescription;

  private String content;

  private String thumbnailUrl;

  private String author;

  @NotBlank(message = "Category ID is required")
  private String categoryId;

  private Boolean isFeatured;

  @NotNull(message = "Status is required")
  private PostStatus status;
}

