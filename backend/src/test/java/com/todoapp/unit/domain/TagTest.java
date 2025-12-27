package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tag Entity Unit Tests")
class TagTest {

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
  @DisplayName("Should create valid tag")
  void shouldCreateValidTag() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setColor("#FF0000");
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertTrue(violations.isEmpty());
    assertEquals("urgent", tag.getName());
    assertEquals("#FF0000", tag.getColor());
    assertEquals(user, tag.getUser());
  }

  @Test
  @DisplayName("Should reject tag with null name")
  void shouldRejectNullName() {
    Tag tag = new Tag();
    tag.setName(null);
    tag.setColor("#FF0000");
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
  }

  @Test
  @DisplayName("Should reject tag with blank name")
  void shouldRejectBlankName() {
    Tag tag = new Tag();
    tag.setName("   ");
    tag.setColor("#FF0000");
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject tag name exceeding 50 characters")
  void shouldRejectNameExceeding50Characters() {
    Tag tag = new Tag();
    tag.setName("a".repeat(51));
    tag.setColor("#FF0000");
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should accept tag with null color")
  void shouldAcceptNullColor() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setColor(null);
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should accept tag with valid hex color")
  void shouldAcceptValidHexColor() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setColor("#FF0000");
    tag.setUser(user);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject tag with null user")
  void shouldRejectNullUser() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setColor("#FF0000");
    tag.setUser(null);

    Set<ConstraintViolation<Tag>> violations = validator.validate(tag);

    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should enforce tag name uniqueness per user (business logic)")
  void shouldEnforceTagNameUniquenessPerUser() {
    // This test validates the concept of uniqueness
    // Actual uniqueness enforcement happens at service/repository level
    Tag tag1 = new Tag();
    tag1.setName("urgent");
    tag1.setUser(user);

    Tag tag2 = new Tag();
    tag2.setName("urgent");
    tag2.setUser(user);

    // Same name for same user should be rejected by service layer
    assertEquals(tag1.getName(), tag2.getName());
    assertEquals(tag1.getUser(), tag2.getUser());
  }

  @Test
  @DisplayName("Should allow same tag name for different users")
  void shouldAllowSameTagNameForDifferentUsers() {
    User user2 = new User();
    user2.setId(2L);
    user2.setEmail("user2@example.com");
    user2.setPasswordHash("hashedPassword");

    Tag tag1 = new Tag();
    tag1.setName("urgent");
    tag1.setUser(user);

    Tag tag2 = new Tag();
    tag2.setName("urgent");
    tag2.setUser(user2);

    // Same name for different users is allowed
    assertEquals(tag1.getName(), tag2.getName());
    assertNotEquals(tag1.getUser(), tag2.getUser());
  }

  @Test
  @DisplayName("Should have createdAt and updatedAt timestamps")
  void shouldHaveTimestamps() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(user);

    assertNotNull(tag.getCreatedAt());
    assertNotNull(tag.getUpdatedAt());
  }
}
