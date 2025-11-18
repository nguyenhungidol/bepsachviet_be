package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.CategoryEntity;
import com.doan.bepsachviet_be.io.Request.CategoryRequest;
import com.doan.bepsachviet_be.io.Response.CategoryResponse;
import com.doan.bepsachviet_be.repository.CategoryRepository;
import com.doan.bepsachviet_be.service.CategoryService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public CategoryResponse createCategory(CategoryRequest request) {
    if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
    }
    CategoryEntity entity = CategoryEntity.builder()
        .categoryId(UUID.randomUUID().toString())
        .name(request.getName())
        .description(request.getDescription())
        .active(request.getActive() == null ? Boolean.TRUE : request.getActive())
        .build();
    return convertToResponse(categoryRepository.save(entity));
  }

  @Override
  public CategoryResponse updateCategory(String categoryId, CategoryRequest request) {
    CategoryEntity entity = categoryRepository.findByCategoryId(categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    if (request.getName() != null && !request.getName().equalsIgnoreCase(entity.getName())
        && categoryRepository.existsByNameIgnoreCase(request.getName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
    }
    entity.setName(request.getName() != null ? request.getName() : entity.getName());
    entity.setDescription(request.getDescription() != null ? request.getDescription() : entity.getDescription());
    if (request.getActive() != null) {
      entity.setActive(request.getActive());
    }
    return convertToResponse(categoryRepository.save(entity));
  }

  @Override
  public List<CategoryResponse> listCategories() {
    return categoryRepository.findAll().stream().map(this::convertToResponse).collect(Collectors.toList());
  }

  @Override
  public CategoryResponse getCategory(String categoryId) {
    return categoryRepository.findByCategoryId(categoryId)
        .map(this::convertToResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
  }

  @Override
  public void deleteCategory(String categoryId) {
    CategoryEntity entity = categoryRepository.findByCategoryId(categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    categoryRepository.delete(entity);
  }

  private CategoryResponse convertToResponse(CategoryEntity entity) {
    return CategoryResponse.builder()
        .categoryId(entity.getCategoryId())
        .name(entity.getName())
        .description(entity.getDescription())
        .active(entity.getActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}

