package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.io.Request.PostRequest;
import com.doan.bepsachviet_be.io.Response.PostListItem;
import com.doan.bepsachviet_be.io.Response.PostResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

  PostResponse createPost(PostRequest request);

  PostResponse updatePost(String postId, PostRequest request);

  PostResponse getPostBySlug(String slug);

  PostResponse getPostById(String postId);

  Page<PostListItem> listPosts(Pageable pageable, String categoryId);

  Page<PostListItem> listAllPosts(Pageable pageable, String categoryId, String status);

  Page<PostListItem> listFeaturedPosts(Pageable pageable);

  List<PostListItem> getRelatedPosts(String slug, int limit);

  void deletePost(String postId);
}

