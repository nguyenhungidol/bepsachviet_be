package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.PostRequest;
import com.doan.bepsachviet_be.io.Response.PostListItem;
import com.doan.bepsachviet_be.io.Response.PostResponse;
import com.doan.bepsachviet_be.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  /**
   * GET /posts - Lấy danh sách bài viết với phân trang và lọc theo danh mục
   */
  @GetMapping("/posts")
  public Page<PostListItem> listPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String categoryId,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") String sortDirection) {

    Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    return postService.listPosts(pageable, categoryId);
  }

  /**
   * GET /posts/{slug} - Xem chi tiết bài viết theo slug
   */
  @GetMapping("/posts/{slug}")
  public PostResponse getPostBySlug(@PathVariable String slug) {
    return postService.getPostBySlug(slug);
  }

  /**
   * GET /posts/featured - Lấy danh sách tin nổi bật
   */
  @GetMapping("/posts/featured")
  public Page<PostListItem> getFeaturedPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return postService.listFeaturedPosts(pageable);
  }

  /**
   * GET /posts/related - Lấy bài viết liên quan (cùng category)
   */
  @GetMapping("/posts/related")
  public List<PostListItem> getRelatedPosts(
      @RequestParam String slug,
      @RequestParam(defaultValue = "5") int limit) {
    return postService.getRelatedPosts(slug, limit);
  }

  /**
   * POST /admin/posts - Tạo bài viết mới (Admin only)
   */
  @PostMapping("/admin/posts")
  @ResponseStatus(HttpStatus.CREATED)
  public PostResponse createPost(@Valid @RequestBody PostRequest request) {
    return postService.createPost(request);
  }

  /**
   * PUT /admin/posts/{postId} - Sửa bài viết (Admin only)
   */
  @PutMapping("/admin/posts/{postId}")
  public PostResponse updatePost(
      @PathVariable String postId,
      @Valid @RequestBody PostRequest request) {
    return postService.updatePost(postId, request);
  }

  /**
   * DELETE /admin/posts/{postId} - Xóa bài viết (Admin only)
   */
  @DeleteMapping("/admin/posts/{postId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePost(@PathVariable String postId) {
    postService.deletePost(postId);
  }

  /**
   * GET /admin/posts - Lấy tất cả bài viết (bao gồm DRAFT) (Admin only)
   */
  @GetMapping("/admin/posts")
  public Page<PostListItem> listAllPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") String sortDirection) {

    Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    return postService.listAllPosts(pageable, categoryId, status);
  }

  /**
   * GET /admin/posts/{postId} - Lấy chi tiết bài viết theo postId (Admin only)
   */
  @GetMapping("/admin/posts/{postId}")
  public PostResponse getPostById(@PathVariable String postId) {
    return postService.getPostById(postId);
  }
}

