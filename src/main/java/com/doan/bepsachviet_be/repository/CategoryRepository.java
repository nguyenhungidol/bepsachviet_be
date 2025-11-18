package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.entity.CategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
  Optional<CategoryEntity> findByCategoryId(String categoryId);
  boolean existsByNameIgnoreCase(String name);
}

