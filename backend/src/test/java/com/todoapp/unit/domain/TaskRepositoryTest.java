package com.todoapp.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository Unit Tests")
public class TaskRepositoryTest {

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);
  }

  @Test
  @DisplayName("Should find tasks by description search term")
  void shouldFindTasksByDescriptionSearchTerm() {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Buy groceries from the store");
    task1.setPriority(Priority.MEDIUM);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Call doctor for appointment");
    task2.setPriority(Priority.HIGH);
    taskRepository.save(task2);

    Task task3 = new Task();
    task3.setUser(testUser);
    task3.setDescription("Buy concert tickets");
    task3.setPriority(Priority.LOW);
    taskRepository.save(task3);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "buy", pageable);

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .extracting(Task::getDescription)
        .containsExactlyInAnyOrder("Buy groceries from the store", "Buy concert tickets");
  }

  @Test
  @DisplayName("Should find tasks by description search term case-insensitive")
  void shouldFindTasksByDescriptionSearchTermCaseInsensitive() {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("IMPORTANT Meeting with CEO");
    task1.setPriority(Priority.HIGH);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Review important documents");
    task2.setPriority(Priority.MEDIUM);
    taskRepository.save(task2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "important", pageable);

    assertThat(results.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("Should return empty results when search term doesn't match")
  void shouldReturnEmptyResultsWhenSearchTermDoesNotMatch() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Buy groceries");
    task.setPriority(Priority.MEDIUM);
    taskRepository.save(task);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "meeting", pageable);

    assertThat(results.getContent()).isEmpty();
  }

  @Test
  @DisplayName("Should find tasks by description and completion status")
  void shouldFindTasksByDescriptionAndCompletionStatus() {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Buy groceries");
    task1.setPriority(Priority.MEDIUM);
    task1.setIsCompleted(true);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Buy tickets");
    task2.setPriority(Priority.HIGH);
    task2.setIsCompleted(false);
    taskRepository.save(task2);

    Task task3 = new Task();
    task3.setUser(testUser);
    task3.setDescription("Buy flowers");
    task3.setPriority(Priority.LOW);
    task3.setIsCompleted(true);
    taskRepository.save(task3);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescriptionAndIsCompleted(
            testUser.getId(), "buy", true, pageable);

    assertThat(results.getContent()).hasSize(2);
    assertThat(results.getContent())
        .allMatch(
            task -> task.getIsCompleted() && task.getDescription().toLowerCase().contains("buy"));
  }

  @Test
  @DisplayName("Should only return tasks for specified user")
  void shouldOnlyReturnTasksForSpecifiedUser() {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash2");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Buy groceries");
    task1.setPriority(Priority.MEDIUM);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(otherUser);
    task2.setDescription("Buy tickets");
    task2.setPriority(Priority.HIGH);
    taskRepository.save(task2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "buy", pageable);

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getContent().get(0).getUser().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @DisplayName("Should find tasks with partial word match")
  void shouldFindTasksWithPartialWordMatch() {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Complete programming assignment");
    task.setPriority(Priority.HIGH);
    taskRepository.save(task);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "gram", pageable);

    assertThat(results.getContent()).hasSize(1);
    assertThat(results.getContent().get(0).getDescription()).contains("programming");
  }

  @Test
  @DisplayName("Should handle empty search term")
  void shouldHandleEmptySearchTerm() {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Task 1");
    task1.setPriority(Priority.MEDIUM);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Task 2");
    task2.setPriority(Priority.HIGH);
    taskRepository.save(task2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "", pageable);

    assertThat(results.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("Should respect pagination limits")
  void shouldRespectPaginationLimits() {
    for (int i = 1; i <= 15; i++) {
      Task task = new Task();
      task.setUser(testUser);
      task.setDescription("Task number " + i);
      task.setPriority(Priority.MEDIUM);
      taskRepository.save(task);
    }

    Pageable pageable = PageRequest.of(0, 5);
    Page<Task> results =
        taskRepository.searchByUserIdAndDescription(testUser.getId(), "task", pageable);

    assertThat(results.getContent()).hasSize(5);
    assertThat(results.getTotalElements()).isEqualTo(15);
    assertThat(results.getTotalPages()).isEqualTo(3);
  }
}
