package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.entity.ProductEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
  Optional<ProductEntity> findByProductId(String productId);

  // Find active products only
  Optional<ProductEntity> findByProductIdAndIsActive(String productId, Boolean isActive);

  List<ProductEntity> findAllByCategory_CategoryId(String categoryId);

  // Find active products by category
  List<ProductEntity> findAllByCategory_CategoryIdAndIsActive(String categoryId, Boolean isActive);

  // Find all active products
  List<ProductEntity> findAllByIsActive(Boolean isActive);

  boolean existsByNameIgnoreCase(String name);
}

