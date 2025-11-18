package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.io.Request.CategoryRequest;
import com.doan.bepsachviet_be.io.Response.CategoryResponse;
import java.util.List;

public interface CategoryService {
  CategoryResponse createCategory(CategoryRequest request);
  CategoryResponse updateCategory(String categoryId, CategoryRequest request);
  List<CategoryResponse> listCategories();
  CategoryResponse getCategory(String categoryId);
  void deleteCategory(String categoryId);
}

