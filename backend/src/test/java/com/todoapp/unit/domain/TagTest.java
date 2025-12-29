package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

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

  // Deleted useless "concept" tests (identical to CategoryTest issues):
  // - shouldEnforceTagNameUniquenessPerUser() - Just created two tags with same name
  //   and verified they had the same name. No actual validation logic tested.
  // - shouldAllowSameTagNameForDifferentUsers() - Same issue, just compared objects,
  //   no validation logic present
  // These tests provided negative value by giving false confidence

  @Test
  @DisplayName("Should allow setting createdAt and updatedAt timestamps")
  void shouldAllowSettingTimestamps() {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(user);
    tag.setCreatedAt(java.time.LocalDateTime.now());
    tag.setUpdatedAt(java.time.LocalDateTime.now());

    assertNotNull(tag.getCreatedAt());
    assertNotNull(tag.getUpdatedAt());
  }
}
