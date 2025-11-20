package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.CategoryEntity;
import com.doan.bepsachviet_be.entity.ProductEntity;
import com.doan.bepsachviet_be.io.Request.ProductRequest;
import com.doan.bepsachviet_be.io.Response.ProductResponse;
import com.doan.bepsachviet_be.repository.CategoryRepository;
import com.doan.bepsachviet_be.repository.ProductRepository;
import com.doan.bepsachviet_be.service.ProductService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public ProductResponse createProduct(ProductRequest request) {
    validateRequest(request);
    CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
    ProductEntity entity = ProductEntity.builder()
        .productId(UUID.randomUUID().toString())
        .name(request.getName())
        .description(request.getDescription())
        .imageSrc(request.getImageSrc())
        .price(request.getPrice())
        .ocUrl(request.getOcUrl())
        .category(category)
        .build();
    return convertToResponse(productRepository.save(entity));
  }

  @Override
  public ProductResponse updateProduct(String productId, ProductRequest request) {
    ProductEntity entity = productRepository.findByProductId(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    if (request.getCategoryId() != null) {
      CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
      entity.setCategory(category);
    }
    entity.setName(request.getName() != null ? request.getName() : entity.getName());
    entity.setDescription(request.getDescription() != null ? request.getDescription() : entity.getDescription());
    entity.setImageSrc(request.getImageSrc() != null ? request.getImageSrc() : entity.getImageSrc());
    entity.setPrice(request.getPrice() != null ? request.getPrice() : entity.getPrice());
    entity.setOcUrl(request.getOcUrl() != null ? request.getOcUrl() : entity.getOcUrl());
    return convertToResponse(productRepository.save(entity));
  }

  @Override
  public List<ProductResponse> listProducts() {
    return productRepository.findAll().stream().map(this::convertToResponse).collect(Collectors.toList());
  }

  @Override
  public ProductResponse getProduct(String productId) {
    return productRepository.findByProductId(productId)
        .map(this::convertToResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  @Override
  public List<ProductResponse> listProductsByCategory(String categoryId) {
    return productRepository.findAllByCategory_CategoryId(categoryId)
        .stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteProduct(String productId) {
    ProductEntity entity = productRepository.findByProductId(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    productRepository.delete(entity);
  }

  private void validateRequest(ProductRequest request) {
    if (request.getName() == null || request.getName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
    }
    if (request.getPrice() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price is required");
    }
    if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
    }
  }

  private ProductResponse convertToResponse(ProductEntity entity) {
    return ProductResponse.builder()
        .productId(entity.getProductId())
        .name(entity.getName())
        .description(entity.getDescription())
        .imageSrc(entity.getImageSrc())
        .price(entity.getPrice())
        .ocUrl(entity.getOcUrl())
        .categoryId(entity.getCategory().getCategoryId())
        .categoryName(entity.getCategory().getName())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}

