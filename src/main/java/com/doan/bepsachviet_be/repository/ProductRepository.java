package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.entity.ProductEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
  Optional<ProductEntity> findByProductId(String productId);
  List<ProductEntity> findAllByCategory_CategoryId(String categoryId);
  boolean existsByNameIgnoreCase(String name);
}

