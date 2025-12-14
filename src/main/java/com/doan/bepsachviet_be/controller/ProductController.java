package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.ProductRequest;
import com.doan.bepsachviet_be.io.Response.ProductResponse;
import com.doan.bepsachviet_be.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping("/products")
  public List<ProductResponse> listProducts() {
    return productService.listProducts();
  }

  @GetMapping("/products/{productId}")
  public ProductResponse getProduct(@PathVariable String productId) {
    return productService.getProduct(productId);
  }

  @GetMapping("/categories/{categoryId}/products")
  public List<ProductResponse> listProductsByCategory(@PathVariable String categoryId) {
    return productService.listProductsByCategory(categoryId);
  }

  @GetMapping("/admin/products")
  public List<ProductResponse> listAllProductsAdmin() {
    return productService.listAllProductsForAdmin();
  }

  @PostMapping("/admin/products")
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
    return productService.createProduct(request);
  }

  @PutMapping("/admin/products/{productId}")
  public ProductResponse updateProduct(@PathVariable String productId,
      @Valid @RequestBody ProductRequest request) {
    return productService.updateProduct(productId, request);
  }

  @DeleteMapping("/admin/products/{productId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProduct(@PathVariable String productId) {
    productService.deleteProduct(productId);
  }
}

