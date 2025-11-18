package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.CategoryRequest;
import com.doan.bepsachviet_be.io.Response.CategoryResponse;
import com.doan.bepsachviet_be.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/categories")
  public List<CategoryResponse> listCategories() {
    return categoryService.listCategories();
  }

  @GetMapping("/categories/{categoryId}")
  public CategoryResponse getCategory(@PathVariable String categoryId) {
    return categoryService.getCategory(categoryId);
  }

  @PostMapping("/admin/categories")
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @PutMapping("/admin/categories/{categoryId}")
  public CategoryResponse updateCategory(@PathVariable String categoryId,
                                         @RequestBody CategoryRequest request) {
    return categoryService.updateCategory(categoryId, request);
  }

  @DeleteMapping("/admin/categories/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCategory(@PathVariable String categoryId) {
    categoryService.deleteCategory(categoryId);
  }
}

