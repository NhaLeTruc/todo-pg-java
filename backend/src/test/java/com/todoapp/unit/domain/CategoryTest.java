package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("Category Entity Unit Tests")
class CategoryTest {

  private Validator validator;
  private User user;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
  }

  @Test
  @DisplayName("Should create valid category")
  void shouldCreateValidCategory() {
    Category category = new Category();
    category.setName("Work");
    category.setColor("#FF5733");
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertTrue(violations.isEmpty());
    assertEquals("Work", category.getName());
    assertEquals("#FF5733", category.getColor());
    assertEquals(user, category.getUser());
  }

  @Test
  @DisplayName("Should reject category with null name")
  void shouldRejectNullName() {
    Category category = new Category();
    category.setName(null);
    category.setColor("#FF5733");
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
  }

  @Test
  @DisplayName("Should reject category with blank name")
  void shouldRejectBlankName() {
    Category category = new Category();
    category.setName("   ");
    category.setColor("#FF5733");
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject category name exceeding 100 characters")
  void shouldRejectNameExceeding100Characters() {
    Category category = new Category();
    category.setName("a".repeat(101));
    category.setColor("#FF5733");
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should accept category with null color")
  void shouldAcceptNullColor() {
    Category category = new Category();
    category.setName("Work");
    category.setColor(null);
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should accept category with valid hex color")
  void shouldAcceptValidHexColor() {
    Category category = new Category();
    category.setName("Work");
    category.setColor("#FF5733");
    category.setUser(user);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject category with null user")
  void shouldRejectNullUser() {
    Category category = new Category();
    category.setName("Work");
    category.setColor("#FF5733");
    category.setUser(null);

    Set<ConstraintViolation<Category>> violations = validator.validate(category);

    assertFalse(violations.isEmpty());
  }

  // Deleted useless "concept" tests:
  // - shouldEnforceCategoryNameUniquenessPerUser() - Just created two categories with same name
  //   and verified they had the same name. No actual validation logic tested.
  //   Comment even admitted "Actual uniqueness enforcement happens at service/repository level"
  // - shouldAllowSameCategoryNameForDifferentUsers() - Same issue, just compared objects,
  //   no validation logic present
  // These tests provided negative value by giving false confidence that validation exists

  @Test
  @DisplayName("Should allow setting createdAt and updatedAt timestamps")
  void shouldAllowSettingTimestamps() {
    Category category = new Category();
    category.setName("Work");
    category.setUser(user);
    category.setCreatedAt(LocalDateTime.now());
    category.setUpdatedAt(LocalDateTime.now());

    assertNotNull(category.getCreatedAt());
    assertNotNull(category.getUpdatedAt());
  }
}
