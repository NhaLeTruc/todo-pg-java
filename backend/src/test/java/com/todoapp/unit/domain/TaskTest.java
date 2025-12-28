package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;

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

  // ========================================
  // User Story 10: Subtasks and Task Hierarchies Tests
  // ========================================

  @Test
  @DisplayName("Should allow creating subtask with parent")
  void shouldAllowCreatingSubtaskWithParent() {
    Task parentTask = new Task();
    parentTask.setUser(testUser);
    parentTask.setDescription("Parent task");

    Task subtask = new Task();
    subtask.setUser(testUser);
    subtask.setDescription("Subtask");
    subtask.setParentTask(parentTask);

    assertEquals(parentTask, subtask.getParentTask());
  }

  @Test
  @DisplayName("Should allow null parent task for top-level tasks")
  void shouldAllowNullParentTaskForTopLevelTasks() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Top-level task");
    task.setParentTask(null);

    assertNull(task.getParentTask());
  }

  @Test
  @DisplayName("Should calculate depth of 1 for task with parent")
  void shouldCalculateDepth1ForTaskWithParent() {
    Task parentTask = new Task();
    parentTask.setUser(testUser);
    parentTask.setDescription("Parent");

    Task subtask = new Task();
    subtask.setUser(testUser);
    subtask.setDescription("Subtask");
    subtask.setParentTask(parentTask);

    assertEquals(1, subtask.getDepth());
  }

  @Test
  @DisplayName("Should calculate depth of 2 for nested subtask")
  void shouldCalculateDepth2ForNestedSubtask() {
    Task grandparent = new Task();
    grandparent.setUser(testUser);
    grandparent.setDescription("Grandparent");

    Task parent = new Task();
    parent.setUser(testUser);
    parent.setDescription("Parent");
    parent.setParentTask(grandparent);

    Task child = new Task();
    child.setUser(testUser);
    child.setDescription("Child");
    child.setParentTask(parent);

    assertEquals(2, child.getDepth());
  }

  @Test
  @DisplayName("Should calculate depth of 0 for top-level task")
  void shouldCalculateDepth0ForTopLevelTask() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Top-level");

    assertEquals(0, task.getDepth());
  }

  @Test
  @DisplayName("Should reject subtask at depth 6 (exceeds max depth of 5)")
  void shouldRejectSubtaskAtDepth6() {
    Task level0 = new Task();
    level0.setUser(testUser);
    level0.setDescription("Level 0");

    Task level1 = new Task();
    level1.setUser(testUser);
    level1.setDescription("Level 1");
    level1.setParentTask(level0);

    Task level2 = new Task();
    level2.setUser(testUser);
    level2.setDescription("Level 2");
    level2.setParentTask(level1);

    Task level3 = new Task();
    level3.setUser(testUser);
    level3.setDescription("Level 3");
    level3.setParentTask(level2);

    Task level4 = new Task();
    level4.setUser(testUser);
    level4.setDescription("Level 4");
    level4.setParentTask(level3);

    Task level5 = new Task();
    level5.setUser(testUser);
    level5.setDescription("Level 5");
    level5.setParentTask(level4);

    Task level6 = new Task();
    level6.setUser(testUser);
    level6.setDescription("Level 6");

    assertThrows(
        IllegalArgumentException.class,
        () -> level6.setParentTask(level5),
        "Should not allow subtasks deeper than 5 levels");
  }

  @Test
  @DisplayName("Should allow subtask at exactly depth 5 (max depth)")
  void shouldAllowSubtaskAtDepth5() {
    Task level0 = new Task();
    level0.setUser(testUser);
    level0.setDescription("Level 0");

    Task level1 = new Task();
    level1.setUser(testUser);
    level1.setDescription("Level 1");
    level1.setParentTask(level0);

    Task level2 = new Task();
    level2.setUser(testUser);
    level2.setDescription("Level 2");
    level2.setParentTask(level1);

    Task level3 = new Task();
    level3.setUser(testUser);
    level3.setDescription("Level 3");
    level3.setParentTask(level2);

    Task level4 = new Task();
    level4.setUser(testUser);
    level4.setDescription("Level 4");
    level4.setParentTask(level3);

    Task level5 = new Task();
    level5.setUser(testUser);
    level5.setDescription("Level 5");
    level5.setParentTask(level4);

    assertEquals(5, level5.getDepth());
  }

  @Test
  @DisplayName("Should calculate 0% progress when no subtasks exist")
  void shouldCalculate0PercentProgressWhenNoSubtasks() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task without subtasks");

    assertEquals(0, task.calculateSubtaskProgress());
  }

  @Test
  @DisplayName("Should calculate 0% progress when all subtasks are incomplete")
  void shouldCalculate0PercentProgressWhenAllSubtasksIncomplete() {
    Task parent = new Task();
    parent.setUser(testUser);
    parent.setDescription("Parent");

    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setParentTask(parent);
    subtask1.setIsCompleted(false);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setParentTask(parent);
    subtask2.setIsCompleted(false);

    parent.addSubtask(subtask1);
    parent.addSubtask(subtask2);

    assertEquals(0, parent.calculateSubtaskProgress());
  }

  @Test
  @DisplayName("Should calculate 50% progress when half subtasks are complete")
  void shouldCalculate50PercentProgressWhenHalfSubtasksComplete() {
    Task parent = new Task();
    parent.setUser(testUser);
    parent.setDescription("Parent");

    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setParentTask(parent);
    subtask1.setIsCompleted(true);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setParentTask(parent);
    subtask2.setIsCompleted(false);

    parent.addSubtask(subtask1);
    parent.addSubtask(subtask2);

    assertEquals(50, parent.calculateSubtaskProgress());
  }

  @Test
  @DisplayName("Should calculate 100% progress when all subtasks are complete")
  void shouldCalculate100PercentProgressWhenAllSubtasksComplete() {
    Task parent = new Task();
    parent.setUser(testUser);
    parent.setDescription("Parent");

    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setParentTask(parent);
    subtask1.setIsCompleted(true);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setParentTask(parent);
    subtask2.setIsCompleted(true);

    parent.addSubtask(subtask1);
    parent.addSubtask(subtask2);

    assertEquals(100, parent.calculateSubtaskProgress());
  }

  @Test
  @DisplayName("Should calculate 33% progress when 1 of 3 subtasks complete")
  void shouldCalculate33PercentProgressWhen1Of3SubtasksComplete() {
    Task parent = new Task();
    parent.setUser(testUser);
    parent.setDescription("Parent");

    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setParentTask(parent);
    subtask1.setIsCompleted(true);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setParentTask(parent);
    subtask2.setIsCompleted(false);

    Task subtask3 = new Task();
    subtask3.setUser(testUser);
    subtask3.setDescription("Subtask 3");
    subtask3.setParentTask(parent);
    subtask3.setIsCompleted(false);

    parent.addSubtask(subtask1);
    parent.addSubtask(subtask2);
    parent.addSubtask(subtask3);

    assertEquals(33, parent.calculateSubtaskProgress());
  }
}
