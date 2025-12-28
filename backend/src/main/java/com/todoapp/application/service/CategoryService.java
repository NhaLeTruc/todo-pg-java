package com.todoapp.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.CategoryDTO;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
import com.todoapp.domain.repository.UserRepository;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public CategoryDTO createCategory(CategoryDTO categoryDTO, Long userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (categoryRepository.existsByNameAndUserId(categoryDTO.getName(), userId)) {
      throw new RuntimeException(
          "Category with name '" + categoryDTO.getName() + "' already exists");
    }

    Category category = new Category();
    category.setName(categoryDTO.getName());
    category.setColor(categoryDTO.getColor());
    category.setUser(user);

    Category savedCategory = categoryRepository.save(category);
    return mapToDTO(savedCategory);
  }

  @Transactional(readOnly = true)
  public List<CategoryDTO> getCategoriesByUserId(Long userId) {
    return categoryRepository.findByUserId(userId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CategoryDTO getCategoryById(Long id, Long userId) {
    Category category =
        categoryRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    return mapToDTO(category);
  }

  @Transactional
  public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, Long userId) {
    Category category =
        categoryRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    if (categoryRepository.existsByNameAndUserIdAndIdNot(categoryDTO.getName(), userId, id)) {
      throw new RuntimeException(
          "Category with name '" + categoryDTO.getName() + "' already exists");
    }

    category.setName(categoryDTO.getName());
    category.setColor(categoryDTO.getColor());

    Category updatedCategory = categoryRepository.save(category);
    return mapToDTO(updatedCategory);
  }

  @Transactional
  public void deleteCategory(Long id, Long userId) {
    Category category =
        categoryRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    categoryRepository.delete(category);
  }

  private CategoryDTO mapToDTO(Category category) {
    CategoryDTO dto = new CategoryDTO();
    dto.setId(category.getId());
    dto.setName(category.getName());
    dto.setColor(category.getColor());
    dto.setCreatedAt(category.getCreatedAt() != null ? category.getCreatedAt().toString() : null);
    dto.setUpdatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : null);
    return dto;
  }
}
