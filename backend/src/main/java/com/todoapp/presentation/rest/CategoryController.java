package com.todoapp.presentation.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.todoapp.application.dto.CategoryDTO;
import com.todoapp.application.service.CategoryService;
import com.todoapp.infrastructure.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  @Operation(summary = "Get all categories for current user")
  public ResponseEntity<List<CategoryDTO>> getAllCategories(
      @AuthenticationPrincipal UserPrincipal principal) {
    List<CategoryDTO> categories = categoryService.getCategoriesByUserId(principal.getUserId());
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID")
  public ResponseEntity<CategoryDTO> getCategoryById(
      @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    CategoryDTO category = categoryService.getCategoryById(id, principal.getUserId());
    return ResponseEntity.ok(category);
  }

  @PostMapping
  @Operation(summary = "Create new category")
  public ResponseEntity<CategoryDTO> createCategory(
      @Valid @RequestBody CategoryDTO categoryDTO,
      @AuthenticationPrincipal UserPrincipal principal) {
    CategoryDTO createdCategory =
        categoryService.createCategory(categoryDTO, principal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category")
  public ResponseEntity<CategoryDTO> updateCategory(
      @PathVariable Long id,
      @Valid @RequestBody CategoryDTO categoryDTO,
      @AuthenticationPrincipal UserPrincipal principal) {
    CategoryDTO updatedCategory =
        categoryService.updateCategory(id, categoryDTO, principal.getUserId());
    return ResponseEntity.ok(updatedCategory);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category")
  public ResponseEntity<Void> deleteCategory(
      @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    categoryService.deleteCategory(id, principal.getUserId());
    return ResponseEntity.noContent().build();
  }
}
