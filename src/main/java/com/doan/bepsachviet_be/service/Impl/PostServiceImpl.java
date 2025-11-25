package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.constant.PostStatus;
import com.doan.bepsachviet_be.entity.CategoryEntity;
import com.doan.bepsachviet_be.entity.PostEntity;
import com.doan.bepsachviet_be.io.Request.PostRequest;
import com.doan.bepsachviet_be.io.Response.PostListItem;
import com.doan.bepsachviet_be.io.Response.PostResponse;
import com.doan.bepsachviet_be.repository.CategoryRepository;
import com.doan.bepsachviet_be.repository.PostRepository;
import com.doan.bepsachviet_be.service.FileUploadService;
import com.doan.bepsachviet_be.service.PostService;
import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final FileUploadService fileUploadService;

  @Override
  @Transactional
  public PostResponse createPost(PostRequest request) {
    validateRequest(request);

    CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

    String slug = generateUniqueSlug(request.getSlug() != null ? request.getSlug() : request.getTitle());

    PostEntity entity = PostEntity.builder()
        .postId(UUID.randomUUID().toString())
        .title(request.getTitle())
        .slug(slug)
        .shortDescription(request.getShortDescription())
        .content(request.getContent())
        .thumbnailUrl(request.getThumbnailUrl())
        .author(request.getAuthor())
        .category(category)
        .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
        .status(request.getStatus())
        .build();

    return convertToResponse(postRepository.save(entity));
  }

  @Override
  @Transactional
  public PostResponse updatePost(String postId, PostRequest request) {
    PostEntity entity = postRepository.findByPostId(postId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

    if (request.getCategoryId() != null) {
      CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
      entity.setCategory(category);
    }

    if (request.getTitle() != null) {
      entity.setTitle(request.getTitle());
    }

    if (request.getSlug() != null && !request.getSlug().equals(entity.getSlug())) {
      String newSlug = generateUniqueSlug(request.getSlug());
      entity.setSlug(newSlug);
    }

    if (request.getShortDescription() != null) {
      entity.setShortDescription(request.getShortDescription());
    }

    if (request.getContent() != null) {
      entity.setContent(request.getContent());
    }

    handleThumbnailMutation(entity, request.getThumbnailUrl());

    if (request.getAuthor() != null) {
      entity.setAuthor(request.getAuthor());
    }

    if (request.getIsFeatured() != null) {
      entity.setIsFeatured(request.getIsFeatured());
    }

    if (request.getStatus() != null) {
      entity.setStatus(request.getStatus());
    }

    return convertToResponse(postRepository.save(entity));
  }

  @Override
  public PostResponse getPostBySlug(String slug) {
    return postRepository.findBySlug(slug)
        .map(this::convertToResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
  }

  @Override
  public PostResponse getPostById(String postId) {
    return postRepository.findByPostId(postId)
        .map(this::convertToResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
  }

  @Override
  public Page<PostListItem> listPosts(Pageable pageable, String categoryId) {
    Page<PostEntity> posts;

    if (categoryId != null && !categoryId.isBlank()) {
      posts = postRepository.findAllByCategory_CategoryIdAndStatus(
          categoryId, PostStatus.PUBLISHED, pageable);
    } else {
      posts = postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable);
    }

    return posts.map(this::convertToListItem);
  }

  @Override
  public Page<PostListItem> listAllPosts(Pageable pageable, String categoryId, String status) {
    Page<PostEntity> posts;

    PostStatus postStatus = null;
    if (status != null && !status.isBlank()) {
      try {
        postStatus = PostStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
      }
    }

    if (categoryId != null && !categoryId.isBlank()) {
      if (postStatus != null) {
        posts = postRepository.findAllByCategory_CategoryIdAndStatus(categoryId, postStatus, pageable);
      } else {
        posts = postRepository.findAllByCategory_CategoryId(categoryId, pageable);
      }
    } else {
      if (postStatus != null) {
        posts = postRepository.findAllByStatus(postStatus, pageable);
      } else {
        posts = postRepository.findAll(pageable);
      }
    }

    return posts.map(this::convertToListItem);
  }

  @Override
  public Page<PostListItem> listFeaturedPosts(Pageable pageable) {
    return postRepository.findAllByIsFeaturedAndStatus(true, PostStatus.PUBLISHED, pageable)
        .map(this::convertToListItem);
  }

  @Override
  public List<PostListItem> getRelatedPosts(String slug, int limit) {
    PostEntity currentPost = postRepository.findBySlug(slug)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

    Pageable pageable = PageRequest.of(0, limit);

    return postRepository.findRelatedPosts(
        currentPost.getCategory().getCategoryId(),
        currentPost.getPostId(),
        PostStatus.PUBLISHED,
        pageable
    ).stream().map(this::convertToListItem).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deletePost(String postId) {
    PostEntity entity = postRepository.findByPostId(postId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

    cleanupThumbnail(entity.getThumbnailUrl());
    postRepository.delete(entity);
  }

  private void validateRequest(PostRequest request) {
    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post title is required");
    }
    if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
    }
    if (request.getStatus() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
    }
  }

  private String generateUniqueSlug(String input) {
    String baseSlug = slugify(input);
    String slug = baseSlug;
    int counter = 1;

    while (postRepository.existsBySlug(slug)) {
      slug = baseSlug + "-" + counter;
      counter++;
    }

    return slug;
  }

  private String slugify(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }

    String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    String withoutAccents = pattern.matcher(normalized).replaceAll("");

    return withoutAccents.toLowerCase()
        .replaceAll("[đĐ]", "d")
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  private PostResponse convertToResponse(PostEntity entity) {
    return PostResponse.builder()
        .postId(entity.getPostId())
        .title(entity.getTitle())
        .slug(entity.getSlug())
        .shortDescription(entity.getShortDescription())
        .content(entity.getContent())
        .thumbnailUrl(entity.getThumbnailUrl())
        .author(entity.getAuthor())
        .categoryId(entity.getCategory().getCategoryId())
        .categoryName(entity.getCategory().getName())
        .isFeatured(entity.getIsFeatured())
        .status(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private PostListItem convertToListItem(PostEntity entity) {
    return PostListItem.builder()
        .postId(entity.getPostId())
        .title(entity.getTitle())
        .slug(entity.getSlug())
        .shortDescription(entity.getShortDescription())
        .thumbnailUrl(entity.getThumbnailUrl())
        .author(entity.getAuthor())
        .categoryName(entity.getCategory().getName())
        .isFeatured(entity.getIsFeatured())
        .status(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  private void handleThumbnailMutation(PostEntity entity, String newThumbnailUrl) {
    if (newThumbnailUrl == null) {
      return;
    }

    String trimmedNewUrl = newThumbnailUrl.trim();
    if (trimmedNewUrl.isBlank()) {
      cleanupThumbnail(entity.getThumbnailUrl());
      entity.setThumbnailUrl(null);
      return;
    }

    if (!trimmedNewUrl.equals(entity.getThumbnailUrl())) {
      cleanupThumbnail(entity.getThumbnailUrl());
      entity.setThumbnailUrl(trimmedNewUrl);
    }
  }

  private void cleanupThumbnail(String thumbnailUrl) {
    if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
      return;
    }
    fileUploadService.deleteFile(thumbnailUrl);
  }
}

