package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.io.Request.ProductRequest;
import com.doan.bepsachviet_be.io.Response.ProductResponse;
import java.util.List;

public interface ProductService {
  ProductResponse createProduct(ProductRequest request);
  ProductResponse updateProduct(String productId, ProductRequest request);
  List<ProductResponse> listProducts();
  ProductResponse getProduct(String productId);
  List<ProductResponse> listProductsByCategory(String categoryId);
  void deleteProduct(String productId);
  List<ProductResponse> listAllProductsForAdmin();
}

