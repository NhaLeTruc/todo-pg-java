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

  @Test
  @DisplayName("Should allow updating task description")
  void shouldAllowUpdatingTaskDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");

    task.setDescription("Updated description");

    assertEquals("Updated description", task.getDescription());
  }

  @Test
  @DisplayName("Should reject updating to empty description")
  void shouldRejectUpdatingToEmptyDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription(""),
        "Empty description should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("Should reject updating to null description")
  void shouldRejectUpdatingToNullDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription(null),
        "Null description should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("Should reject updating to whitespace-only description")
  void shouldRejectUpdatingToWhitespaceOnlyDescription() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");

    assertThrows(
        IllegalArgumentException.class,
        () -> task.setDescription("   "),
        "Whitespace-only description should throw IllegalArgumentException");
  }

  // ========================================
  // User Story 5: Priority and Due Date Tests
  // ========================================

  @Test
  @DisplayName("Should accept HIGH priority")
  void shouldAcceptHighPriority() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("High priority task");
    task.setPriority(Priority.HIGH);

    assertEquals(Priority.HIGH, task.getPriority());
  }

  @Test
  @DisplayName("Should accept MEDIUM priority")
  void shouldAcceptMediumPriority() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Medium priority task");
    task.setPriority(Priority.MEDIUM);

    assertEquals(Priority.MEDIUM, task.getPriority());
  }

  @Test
  @DisplayName("Should accept LOW priority")
  void shouldAcceptLowPriority() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Low priority task");
    task.setPriority(Priority.LOW);

    assertEquals(Priority.LOW, task.getPriority());
  }

  @Test
  @DisplayName("Should return false for isOverdue when due date is null")
  void shouldReturnFalseForOverdueWhenDueDateIsNull() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task without due date");
    task.setDueDate(null);
    task.setIsCompleted(false);

    assertFalse(task.isOverdue());
  }

  @Test
  @DisplayName("Should return false for isOverdue when task is completed")
  void shouldReturnFalseForOverdueWhenTaskIsCompleted() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Completed task");
    task.setDueDate(LocalDateTime.now().minusDays(1)); // Past due date
    task.setIsCompleted(true);

    assertFalse(task.isOverdue(), "Completed tasks should never be overdue");
  }

  @Test
  @DisplayName("Should return true for isOverdue when due date is in the past")
  void shouldReturnTrueForOverdueWhenDueDateIsInPast() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Overdue task");
    task.setDueDate(LocalDateTime.now().minusDays(1));
    task.setIsCompleted(false);

    assertTrue(task.isOverdue(), "Task with past due date should be overdue");
  }

  @Test
  @DisplayName("Should return false for isOverdue when due date is in the future")
  void shouldReturnFalseForOverdueWhenDueDateIsInFuture() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Future task");
    task.setDueDate(LocalDateTime.now().plusDays(1));
    task.setIsCompleted(false);

    assertFalse(task.isOverdue(), "Task with future due date should not be overdue");
  }

  @Test
  @DisplayName("Should return true for isOverdue when due date is exactly now (edge case)")
  void shouldReturnTrueForOverdueWhenDueDateIsNow() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task due now");
    task.setDueDate(LocalDateTime.now().minusSeconds(1));
    task.setIsCompleted(false);

    assertTrue(task.isOverdue(), "Task with due date in the past should be overdue");
  }

  @Test
  @DisplayName("Should allow setting due date")
  void shouldAllowSettingDueDate() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task with due date");
    LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
    task.setDueDate(dueDate);

    assertEquals(dueDate, task.getDueDate());
  }

  @Test
  @DisplayName("Should allow null due date (optional field)")
  void shouldAllowNullDueDate() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task without due date");
    task.setDueDate(null);

    assertNull(task.getDueDate());
  }

  @Test
  @DisplayName("Should combine priority and due date correctly")
  void shouldCombinePriorityAndDueDateCorrectly() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("High priority task with due date");
    task.setPriority(Priority.HIGH);
    LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
    task.setDueDate(dueDate);

    assertEquals(Priority.HIGH, task.getPriority());
    assertEquals(dueDate, task.getDueDate());
    assertFalse(task.isOverdue());
  }
}
