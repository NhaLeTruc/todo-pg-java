package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.todoapp.application.dto.CategoryDTO;
import com.todoapp.application.service.CategoryService;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
import com.todoapp.domain.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private CategoryService categoryService;

  private User user;
  private Category category;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    category = new Category();
    category.setId(1L);
    category.setName("Work");
    category.setColor("#FF5733");
    category.setUser(user);
  }

  @Test
  @DisplayName("Should create category successfully")
  void shouldCreateCategorySuccessfully() {
    CategoryDTO categoryDTO = new CategoryDTO();
    categoryDTO.setName("Work");
    categoryDTO.setColor("#FF5733");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(categoryRepository.existsByNameAndUserId("Work", 1L)).thenReturn(false);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);

    CategoryDTO result = categoryService.createCategory(categoryDTO, 1L);

    assertNotNull(result);
    assertEquals("Work", result.getName());
    assertEquals("#FF5733", result.getColor());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("Should reject duplicate category name for same user")
  void shouldRejectDuplicateCategoryName() {
    CategoryDTO categoryDTO = new CategoryDTO();
    categoryDTO.setName("Work");
    categoryDTO.setColor("#FF5733");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(categoryRepository.existsByNameAndUserId("Work", 1L)).thenReturn(true);

    assertThrows(
        RuntimeException.class, () -> categoryService.createCategory(categoryDTO, 1L));

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("Should get all categories for user")
  void shouldGetAllCategoriesForUser() {
    Category category2 = new Category();
    category2.setId(2L);
    category2.setName("Personal");
    category2.setUser(user);

    when(categoryRepository.findByUserId(1L)).thenReturn(Arrays.asList(category, category2));

    List<CategoryDTO> results = categoryService.getCategoriesByUserId(1L);

    assertNotNull(results);
    assertEquals(2, results.size());
    assertEquals("Work", results.get(0).getName());
    assertEquals("Personal", results.get(1).getName());
  }

  @Test
  @DisplayName("Should get category by id")
  void shouldGetCategoryById() {
    when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

    CategoryDTO result = categoryService.getCategoryById(1L, 1L);

    assertNotNull(result);
    assertEquals("Work", result.getName());
  }

  @Test
  @DisplayName("Should throw exception when category not found")
  void shouldThrowExceptionWhenCategoryNotFound() {
    when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(1L, 1L));
  }

  @Test
  @DisplayName("Should update category successfully")
  void shouldUpdateCategorySuccessfully() {
    CategoryDTO updateDTO = new CategoryDTO();
    updateDTO.setName("Updated Work");
    updateDTO.setColor("#00FF00");

    when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));
    when(categoryRepository.existsByNameAndUserIdAndIdNot("Updated Work", 1L, 1L))
        .thenReturn(false);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);

    CategoryDTO result = categoryService.updateCategory(1L, updateDTO, 1L);

    assertNotNull(result);
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("Should delete category successfully")
  void shouldDeleteCategorySuccessfully() {
    when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

    categoryService.deleteCategory(1L, 1L);

    verify(categoryRepository).delete(category);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent category")
  void shouldThrowExceptionWhenDeletingNonExistentCategory() {
    when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(1L, 1L));

    verify(categoryRepository, never()).delete(any(Category.class));
  }
}
