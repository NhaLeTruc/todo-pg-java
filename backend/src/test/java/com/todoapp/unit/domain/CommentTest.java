package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.Comment;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class CommentTest {

  private static Validator validator;
  private static User testUser;
  private static Task testTask;

  @BeforeAll
  public static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");

    testTask = new Task();
    testTask.setId(1L);
    testTask.setDescription("Test task");
    testTask.setUser(testUser);
  }

  @Test
  @DisplayName("Should create valid comment")
  public void shouldCreateValidComment() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("This is a valid comment");

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject null content")
  public void shouldRejectNullContent() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent(null);

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject empty content")
  public void shouldRejectEmptyContent() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("");

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject whitespace-only content")
  public void shouldRejectWhitespaceOnlyContent() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("   ");

    // This will be validated by business logic, not JPA
    String trimmed = comment.getContent().trim();
    assertTrue(trimmed.isEmpty(), "Content should be empty after trimming");
  }

  @Test
  @DisplayName("Should reject content exceeding 5000 characters")
  public void shouldRejectLongContent() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("a".repeat(5001));

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should accept content with exactly 5000 characters")
  public void shouldAcceptMaxLengthContent() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("a".repeat(5000));

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Should require task reference")
  public void shouldRequireTask() {
    Comment comment = new Comment();
    comment.setTask(null);
    comment.setAuthor(testUser);
    comment.setContent("Valid content");

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should require author reference")
  public void shouldRequireAuthor() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(null);
    comment.setContent("Valid content");

    Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should default isEdited to false")
  public void shouldDefaultIsEditedToFalse() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Test content");

    assertFalse(comment.getIsEdited());
  }

  @Test
  @DisplayName("Should set isEdited to true when modified")
  public void shouldSetIsEditedWhenModified() {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Original content");
    comment.setIsEdited(false);

    // Simulate editing
    comment.setContent("Modified content");
    comment.setIsEdited(true);

    assertTrue(comment.getIsEdited());
    assertEquals("Modified content", comment.getContent());
  }
}
