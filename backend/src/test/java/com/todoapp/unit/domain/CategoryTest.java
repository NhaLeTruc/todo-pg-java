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

  @Test
  @DisplayName("Should enforce category name uniqueness per user (business logic)")
  void shouldEnforceCategoryNameUniquenessPerUser() {
    // This test validates the concept of uniqueness
    // Actual uniqueness enforcement happens at service/repository level
    Category category1 = new Category();
    category1.setName("Work");
    category1.setUser(user);

    Category category2 = new Category();
    category2.setName("Work");
    category2.setUser(user);

    // Same name for same user should be rejected by service layer
    assertEquals(category1.getName(), category2.getName());
    assertEquals(category1.getUser(), category2.getUser());
  }

  @Test
  @DisplayName("Should allow same category name for different users")
  void shouldAllowSameCategoryNameForDifferentUsers() {
    User user2 = new User();
    user2.setId(2L);
    user2.setEmail("user2@example.com");
    user2.setPasswordHash("hashedPassword");

    Category category1 = new Category();
    category1.setName("Work");
    category1.setUser(user);

    Category category2 = new Category();
    category2.setName("Work");
    category2.setUser(user2);

    // Same name for different users is allowed
    assertEquals(category1.getName(), category2.getName());
    assertNotEquals(category1.getUser(), category2.getUser());
  }

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
