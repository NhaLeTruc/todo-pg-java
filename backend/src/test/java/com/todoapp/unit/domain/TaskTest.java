package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Task Entity Unit Tests")
class TaskTest {

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("hashedPassword");
    testUser.setFullName("Test User");
  }

  @Test
  @DisplayName("Should create task with valid description")
  void shouldCreateTaskWithValidDescription() {
    String description = "Valid task description";

    Task task = new Task();
    task.setUser(testUser);
    task.setDescription(description);
    task.setPriority(Priority.MEDIUM);

    assertNotNull(task);
    assertEquals(description, task.getDescription());
    assertEquals(testUser, task.getUser());
    assertEquals(Priority.MEDIUM, task.getPriority());
    assertFalse(task.getIsCompleted());
  }

  @Test
  @DisplayName("Should reject empty description")
  void shouldRejectEmptyDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription(""),
        "Empty description should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("Should reject null description")
  void shouldRejectNullDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription(null),
        "Null description should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("Should reject whitespace-only description")
  void shouldRejectWhitespaceOnlyDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription("   "),
        "Whitespace-only description should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("Should have default priority MEDIUM when not specified")
  void shouldHaveDefaultPriorityMedium() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");

    assertEquals(Priority.MEDIUM, task.getPriority());
  }

  @Test
  @DisplayName("Should have isCompleted false by default")
  void shouldHaveIsCompletedFalseByDefault() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");

    assertFalse(task.getIsCompleted());
  }

  @Test
  @DisplayName("Should set created timestamp automatically")
  void shouldSetCreatedTimestampAutomatically() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setCreatedAt(LocalDateTime.now());

    assertNotNull(task.getCreatedAt());
  }

  @Test
  @DisplayName("Should allow all priority levels")
  void shouldAllowAllPriorityLevels() {
    for (Priority priority : Priority.values()) {
      Task task = new Task();
      task.setUser(testUser);
      task.setDescription("Test task");
      task.setPriority(priority);

      assertEquals(priority, task.getPriority());
    }
  }

  @Test
  @DisplayName("Should mark task as complete")
  void shouldMarkTaskAsComplete() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setIsCompleted(false);

    task.markComplete();

    assertTrue(task.getIsCompleted());
    assertNotNull(task.getCompletedAt());
  }

  @Test
  @DisplayName("Should mark task as incomplete")
  void shouldMarkTaskAsIncomplete() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setIsCompleted(true);
    task.setCompletedAt(LocalDateTime.now());

    task.markIncomplete();

    assertFalse(task.getIsCompleted());
    assertNull(task.getCompletedAt());
  }

  @Test
  @DisplayName("Should allow marking already completed task as complete")
  void shouldAllowMarkingAlreadyCompletedTaskAsComplete() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setIsCompleted(true);
    task.setCompletedAt(LocalDateTime.now());

    LocalDateTime firstCompletedAt = task.getCompletedAt();

    task.markComplete();

    assertTrue(task.getIsCompleted());
    assertNotNull(task.getCompletedAt());
  }

  @Test
  @DisplayName("Should allow marking already incomplete task as incomplete")
  void shouldAllowMarkingAlreadyIncompleteTaskAsIncomplete() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setIsCompleted(false);

    task.markIncomplete();

    assertFalse(task.getIsCompleted());
    assertNull(task.getCompletedAt());
  }
}
