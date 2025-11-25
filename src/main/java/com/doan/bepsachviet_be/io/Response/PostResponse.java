package com.doan.bepsachviet_be.io.Response;

import com.doan.bepsachviet_be.constant.PostStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
  private String postId;
  private String title;
  private String slug;
  private String shortDescription;
  private String content;
  private String thumbnailUrl;
  private String author;
  private String categoryId;
  private String categoryName;
  private Boolean isFeatured;
  private PostStatus status;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}

