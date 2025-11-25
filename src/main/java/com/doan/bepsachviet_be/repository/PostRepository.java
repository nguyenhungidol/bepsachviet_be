package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.constant.PostStatus;
import com.doan.bepsachviet_be.entity.PostEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

  Optional<PostEntity> findByPostId(String postId);

  Optional<PostEntity> findBySlug(String slug);

  boolean existsBySlug(String slug);

  Page<PostEntity> findAllByStatus(PostStatus status, Pageable pageable);

  Page<PostEntity> findAllByCategory_CategoryId(String categoryId, Pageable pageable);

  Page<PostEntity> findAllByCategory_CategoryIdAndStatus(String categoryId, PostStatus status, Pageable pageable);

  Page<PostEntity> findAllByIsFeaturedAndStatus(Boolean isFeatured, PostStatus status, Pageable pageable);

  @Query("SELECT p FROM PostEntity p WHERE p.category.categoryId = :categoryId AND p.postId != :currentPostId AND p.status = :status ORDER BY p.createdAt DESC")
  List<PostEntity> findRelatedPosts(@Param("categoryId") String categoryId, @Param("currentPostId") String currentPostId, @Param("status") PostStatus status, Pageable pageable);
}

