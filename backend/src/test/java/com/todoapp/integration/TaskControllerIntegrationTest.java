package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
import com.todoapp.domain.repository.TagRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private TagRepository tagRepository;

  private User testUser;

  @BeforeEach
  public void setUp() {
    taskRepository.deleteAll();
    categoryRepository.deleteAll();
    tagRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);
  }

  @Test
  public void shouldCreateTaskSuccessfully() throws Exception {
    TaskCreateDTO createDTO = new TaskCreateDTO();
    createDTO.setDescription("Buy groceries");
    createDTO.setPriority(Priority.HIGH);

    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.description").value("Buy groceries"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.isCompleted").value(false))
        .andExpect(jsonPath("$.isOverdue").value(false));

    assertThat(taskRepository.count()).isEqualTo(1);
  }

  @Test
  public void shouldRejectTaskWithEmptyDescription() throws Exception {
    TaskCreateDTO createDTO = new TaskCreateDTO();
    createDTO.setDescription("");
    createDTO.setPriority(Priority.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

    assertThat(taskRepository.count()).isEqualTo(0);
  }

  @Test
  public void shouldRejectTaskWithNullDescription() throws Exception {
    TaskCreateDTO createDTO = new TaskCreateDTO();
    createDTO.setDescription(null);
    createDTO.setPriority(Priority.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

    assertThat(taskRepository.count()).isEqualTo(0);
  }

  @Test
  public void shouldGetAllUserTasks() throws Exception {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Task 1");
    task1.setPriority(Priority.HIGH);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Task 2");
    task2.setPriority(Priority.LOW);
    taskRepository.save(task2);

    mockMvc
        .perform(get("/api/v1/tasks").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].description").exists())
        .andExpect(jsonPath("$.content[1].description").exists());
  }

  @Test
  public void shouldGetTaskById() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Test task");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    mockMvc
        .perform(get("/api/v1/tasks/" + task.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task.getId()))
        .andExpect(jsonPath("$.description").value("Test task"))
        .andExpect(jsonPath("$.priority").value("MEDIUM"));
  }

  @Test
  public void shouldReturnNotFoundForNonExistentTask() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/99999").header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldFilterTasksByCompletionStatus() throws Exception {
    Task completedTask = new Task();
    completedTask.setUser(testUser);
    completedTask.setDescription("Completed task");
    completedTask.setPriority(Priority.MEDIUM);
    completedTask.setIsCompleted(true);
    completedTask.setCompletedAt(LocalDateTime.now());
    taskRepository.save(completedTask);

    Task activeTask = new Task();
    activeTask.setUser(testUser);
    activeTask.setDescription("Active task");
    activeTask.setPriority(Priority.HIGH);
    taskRepository.save(activeTask);

    mockMvc
        .perform(
            get("/api/v1/tasks").header("X-User-Id", testUser.getId()).param("completed", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Active task"));

    mockMvc
        .perform(
            get("/api/v1/tasks").header("X-User-Id", testUser.getId()).param("completed", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Completed task"));
  }

  @Test
  public void shouldSearchTasksByDescription() throws Exception {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Buy milk and bread");
    task1.setPriority(Priority.MEDIUM);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Call dentist");
    task2.setPriority(Priority.HIGH);
    taskRepository.save(task2);

    mockMvc
        .perform(get("/api/v1/tasks").header("X-User-Id", testUser.getId()).param("search", "milk"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Buy milk and bread"));
  }

  @Test
  public void shouldRespectPaginationParameters() throws Exception {
    for (int i = 1; i <= 25; i++) {
      Task task = new Task();
      task.setUser(testUser);
      task.setDescription("Task " + i);
      task.setPriority(Priority.MEDIUM);
      taskRepository.save(task);
    }

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(10))
        .andExpect(jsonPath("$.totalElements").value(25))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.number").value(0));

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("page", "1")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(10))
        .andExpect(jsonPath("$.number").value(1));
  }

  @Test
  public void shouldGetTaskCount() throws Exception {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Task 1");
    task1.setPriority(Priority.MEDIUM);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Task 2");
    task2.setPriority(Priority.HIGH);
    task2.setIsCompleted(true);
    taskRepository.save(task2);

    mockMvc
        .perform(get("/api/v1/tasks/count").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(2));

    mockMvc
        .perform(
            get("/api/v1/tasks/count")
                .header("X-User-Id", testUser.getId())
                .param("completed", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(1));
  }

  @Test
  public void shouldMarkTaskAsComplete() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task to complete");
    task.setPriority(Priority.MEDIUM);
    task.setIsCompleted(false);
    task = taskRepository.save(task);

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + task.getId() + "/complete")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task.getId()))
        .andExpect(jsonPath("$.isCompleted").value(true))
        .andExpect(jsonPath("$.completedAt").exists());

    Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getIsCompleted()).isTrue();
    assertThat(updatedTask.getCompletedAt()).isNotNull();
  }

  @Test
  public void shouldMarkTaskAsIncomplete() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Completed task");
    task.setPriority(Priority.HIGH);
    task.setIsCompleted(true);
    task.setCompletedAt(LocalDateTime.now());
    task = taskRepository.save(task);

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + task.getId() + "/uncomplete")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task.getId()))
        .andExpect(jsonPath("$.isCompleted").value(false))
        .andExpect(jsonPath("$.completedAt").doesNotExist());

    Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getIsCompleted()).isFalse();
    assertThat(updatedTask.getCompletedAt()).isNull();
  }

  @Test
  public void shouldToggleTaskCompletionMultipleTimes() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Toggle test task");
    task.setPriority(Priority.LOW);
    task = taskRepository.save(task);

    Long taskId = task.getId();

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + taskId + "/complete").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCompleted").value(true));

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + taskId + "/uncomplete").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCompleted").value(false));

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + taskId + "/complete").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCompleted").value(true));
  }

  @Test
  public void shouldReturnNotFoundWhenCompletingNonExistentTask() throws Exception {
    mockMvc
        .perform(patch("/api/v1/tasks/99999/complete").header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturnNotFoundWhenUncompletingNonExistentTask() throws Exception {
    mockMvc
        .perform(patch("/api/v1/tasks/99999/uncomplete").header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldNotAllowCompletingOtherUsersTask() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash2");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Task task = new Task();
    task.setUser(otherUser);
    task.setDescription("Other user's task");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    mockMvc
        .perform(
            patch("/api/v1/tasks/" + task.getId() + "/complete")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldUpdateTaskSuccessfully() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated description");
    updateDTO.setPriority(Priority.HIGH);

    mockMvc
        .perform(
            put("/api/v1/tasks/" + task.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task.getId()))
        .andExpect(jsonPath("$.description").value("Updated description"))
        .andExpect(jsonPath("$.priority").value("HIGH"));

    Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
    assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws Exception {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated description");

    mockMvc
        .perform(
            put("/api/v1/tasks/99999")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldNotAllowUpdatingOtherUsersTask() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash2");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Task task = new Task();
    task.setUser(otherUser);
    task.setDescription("Other user's task");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Trying to update");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + task.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldRejectUpdateWithEmptyDescription() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original description");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + task.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldDeleteTaskSuccessfully() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Task to delete");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    Long taskId = task.getId();

    mockMvc
        .perform(delete("/api/v1/tasks/" + taskId).header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(taskRepository.findById(taskId)).isEmpty();
  }

  @Test
  public void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tasks/99999").header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldNotAllowDeletingOtherUsersTask() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash2");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Task task = new Task();
    task.setUser(otherUser);
    task.setDescription("Other user's task");
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    mockMvc
        .perform(delete("/api/v1/tasks/" + task.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());

    assertThat(taskRepository.findById(task.getId())).isPresent();
  }

  @Test
  public void shouldSearchTasksByKeyword() throws Exception {
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

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "buy")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  public void shouldSearchTasksCaseInsensitive() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("IMPORTANT Meeting with CEO");
    task.setPriority(Priority.HIGH);
    taskRepository.save(task);

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "important")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("IMPORTANT Meeting with CEO"));
  }

  @Test
  public void shouldCombineSearchAndCompletionFilter() throws Exception {
    Task task1 = new Task();
    task1.setUser(testUser);
    task1.setDescription("Buy groceries");
    task1.setPriority(Priority.MEDIUM);
    task1.setIsCompleted(true);
    task1.setCompletedAt(LocalDateTime.now());
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
    task3.setCompletedAt(LocalDateTime.now());
    taskRepository.save(task3);

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "buy")
                .param("completed", "true")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[*].isCompleted").value(everyItem(is(true))));
  }

  @Test
  public void shouldReturnEmptyResultsForNoMatches() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Buy groceries");
    task.setPriority(Priority.MEDIUM);
    taskRepository.save(task);

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "meeting")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  public void shouldOnlyReturnCurrentUserTasksInSearch() throws Exception {
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

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "buy")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Buy groceries"));
  }

  @Test
  public void shouldRespectPaginationInSearch() throws Exception {
    for (int i = 1; i <= 15; i++) {
      Task task = new Task();
      task.setUser(testUser);
      task.setDescription("Task number " + i);
      task.setPriority(Priority.MEDIUM);
      taskRepository.save(task);
    }

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "task")
                .param("page", "0")
                .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.totalElements").value(15))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  public void shouldHandleEmptySearchTerm() throws Exception {
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

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("search", "")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  // ========================================
  // User Story 5: Priority and Due Date Integration Tests
  // ========================================

  @Test
  public void shouldSortTasksByPriorityDescending() throws Exception {
    LocalDateTime now = LocalDateTime.now();

    Task lowPriorityTask = new Task();
    lowPriorityTask.setUser(testUser);
    lowPriorityTask.setDescription("Low priority task");
    lowPriorityTask.setPriority(Priority.LOW);
    lowPriorityTask.setCreatedAt(now);
    taskRepository.save(lowPriorityTask);

    Task highPriorityTask = new Task();
    highPriorityTask.setUser(testUser);
    highPriorityTask.setDescription("High priority task");
    highPriorityTask.setPriority(Priority.HIGH);
    highPriorityTask.setCreatedAt(now.plusSeconds(1));
    taskRepository.save(highPriorityTask);

    Task mediumPriorityTask = new Task();
    mediumPriorityTask.setUser(testUser);
    mediumPriorityTask.setDescription("Medium priority task");
    mediumPriorityTask.setPriority(Priority.MEDIUM);
    mediumPriorityTask.setCreatedAt(now.plusSeconds(2));
    taskRepository.save(mediumPriorityTask);

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("sortBy", "priority")
                .param("sortDirection", "desc")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
        .andExpect(jsonPath("$.content[1].priority").value("MEDIUM"))
        .andExpect(jsonPath("$.content[2].priority").value("LOW"));
  }

  @Test
  public void shouldSortTasksByDueDateAscending() throws Exception {
    LocalDateTime now = LocalDateTime.now();

    Task taskDueSoon = new Task();
    taskDueSoon.setUser(testUser);
    taskDueSoon.setDescription("Task due tomorrow");
    taskDueSoon.setPriority(Priority.MEDIUM);
    taskDueSoon.setDueDate(now.plusDays(1));
    taskRepository.save(taskDueSoon);

    Task taskDueLater = new Task();
    taskDueLater.setUser(testUser);
    taskDueLater.setDescription("Task due next week");
    taskDueLater.setPriority(Priority.MEDIUM);
    taskDueLater.setDueDate(now.plusDays(7));
    taskRepository.save(taskDueLater);

    Task taskDueToday = new Task();
    taskDueToday.setUser(testUser);
    taskDueToday.setDescription("Task due today");
    taskDueToday.setPriority(Priority.MEDIUM);
    taskDueToday.setDueDate(now);
    taskRepository.save(taskDueToday);

    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("sortBy", "dueDate")
                .param("sortDirection", "asc")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.content[0].description").value("Task due today"))
        .andExpect(jsonPath("$.content[1].description").value("Task due tomorrow"))
        .andExpect(jsonPath("$.content[2].description").value("Task due next week"));
  }

  @Test
  public void shouldCreateTaskWithPriorityAndDueDate() throws Exception {
    LocalDateTime dueDate = LocalDateTime.now().plusDays(3);

    TaskCreateDTO createDTO = new TaskCreateDTO();
    createDTO.setDescription("Important task with deadline");
    createDTO.setPriority(Priority.HIGH);
    createDTO.setDueDate(dueDate);

    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.description").value("Important task with deadline"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.dueDate").exists())
        .andExpect(jsonPath("$.isOverdue").value(false))
        .andExpect(jsonPath("$.isCompleted").value(false));

    assertThat(taskRepository.count()).isEqualTo(1);
    Task savedTask = taskRepository.findAll().get(0);
    assertThat(savedTask.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(savedTask.getDueDate()).isNotNull();
  }

  @Test
  public void shouldMarkOverdueTaskCorrectly() throws Exception {
    LocalDateTime pastDate = LocalDateTime.now().minusDays(2);

    Task overdueTask = new Task();
    overdueTask.setUser(testUser);
    overdueTask.setDescription("Overdue task");
    overdueTask.setPriority(Priority.HIGH);
    overdueTask.setDueDate(pastDate);
    overdueTask.setIsCompleted(false);
    taskRepository.save(overdueTask);

    mockMvc
        .perform(get("/api/v1/tasks/" + overdueTask.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(overdueTask.getId()))
        .andExpect(jsonPath("$.isOverdue").value(true))
        .andExpect(jsonPath("$.isCompleted").value(false));
  }

  @Test
  public void shouldNotMarkCompletedTaskAsOverdue() throws Exception {
    LocalDateTime pastDate = LocalDateTime.now().minusDays(2);

    Task completedTask = new Task();
    completedTask.setUser(testUser);
    completedTask.setDescription("Completed overdue task");
    completedTask.setPriority(Priority.MEDIUM);
    completedTask.setDueDate(pastDate);
    completedTask.setIsCompleted(true);
    completedTask.setCompletedAt(LocalDateTime.now());
    taskRepository.save(completedTask);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + completedTask.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(completedTask.getId()))
        .andExpect(jsonPath("$.isOverdue").value(false))
        .andExpect(jsonPath("$.isCompleted").value(true));
  }

  @Test
  public void shouldUpdateTaskPriorityAndDueDate() throws Exception {
    Task task = new Task();
    task.setUser(testUser);
    task.setDescription("Original task");
    task.setPriority(Priority.LOW);
    task.setDueDate(null);
    taskRepository.save(task);

    LocalDateTime newDueDate = LocalDateTime.now().plusDays(5);
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated task");
    updateDTO.setPriority(Priority.HIGH);
    updateDTO.setDueDate(newDueDate);

    mockMvc
        .perform(
            put("/api/v1/tasks/" + task.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task.getId()))
        .andExpect(jsonPath("$.description").value("Updated task"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.dueDate").exists());

    Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(updatedTask.getDueDate()).isNotNull();
  }

  // ========================================
  // User Story 6: Task Isolation Between Users
  // ========================================

  @Test
  public void shouldIsolateTasksBetweenUsers() throws Exception {
    // Create two users
    User user1 = new User();
    user1.setEmail("user1@example.com");
    user1.setPasswordHash("$2a$10$dummyhash1");
    user1.setIsActive(true);
    user1 = userRepository.save(user1);

    User user2 = new User();
    user2.setEmail("user2@example.com");
    user2.setPasswordHash("$2a$10$dummyhash2");
    user2.setIsActive(true);
    user2 = userRepository.save(user2);

    // Create tasks for user1
    Task user1Task1 = new Task();
    user1Task1.setUser(user1);
    user1Task1.setDescription("User 1 Task 1");
    user1Task1.setPriority(Priority.HIGH);
    taskRepository.save(user1Task1);

    Task user1Task2 = new Task();
    user1Task2.setUser(user1);
    user1Task2.setDescription("User 1 Task 2");
    user1Task2.setPriority(Priority.MEDIUM);
    taskRepository.save(user1Task2);

    // Create tasks for user2
    Task user2Task1 = new Task();
    user2Task1.setUser(user2);
    user2Task1.setDescription("User 2 Task 1");
    user2Task1.setPriority(Priority.LOW);
    taskRepository.save(user2Task1);

    // User1 should only see their tasks
    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", user1.getId())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2));

    // User2 should only see their tasks
    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", user2.getId())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  public void shouldPreventUserFromAccessingOtherUsersTasks() throws Exception {
    // Create two users
    User user1 = new User();
    user1.setEmail("owner@example.com");
    user1.setPasswordHash("$2a$10$dummyhash");
    user1.setIsActive(true);
    user1 = userRepository.save(user1);

    User user2 = new User();
    user2.setEmail("attacker@example.com");
    user2.setPasswordHash("$2a$10$dummyhash");
    user2.setIsActive(true);
    user2 = userRepository.save(user2);

    // Create task for user1
    Task user1Task = new Task();
    user1Task.setUser(user1);
    user1Task.setDescription("User 1 Private Task");
    user1Task.setPriority(Priority.HIGH);
    user1Task = taskRepository.save(user1Task);

    // User2 tries to update user1's task - should fail
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Hacked task");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + user1Task.getId())
                .header("X-User-Id", user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());

    // Verify task was not modified
    Task unchangedTask = taskRepository.findById(user1Task.getId()).orElseThrow();
    assertThat(unchangedTask.getDescription()).isEqualTo("User 1 Private Task");
  }

  @Test
  public void shouldPreventUserFromDeletingOtherUsersTasks() throws Exception {
    // Create two users
    User user1 = new User();
    user1.setEmail("taskowner@example.com");
    user1.setPasswordHash("$2a$10$dummyhash");
    user1.setIsActive(true);
    user1 = userRepository.save(user1);

    User user2 = new User();
    user2.setEmail("nonowner@example.com");
    user2.setPasswordHash("$2a$10$dummyhash");
    user2.setIsActive(true);
    user2 = userRepository.save(user2);

    // Create task for user1
    Task user1Task = new Task();
    user1Task.setUser(user1);
    user1Task.setDescription("Important Task");
    user1Task.setPriority(Priority.HIGH);
    user1Task = taskRepository.save(user1Task);

    // User2 tries to delete user1's task - should fail
    mockMvc
        .perform(delete("/api/v1/tasks/" + user1Task.getId()).header("X-User-Id", user2.getId()))
        .andExpect(status().isNotFound());

    // Verify task still exists
    assertThat(taskRepository.findById(user1Task.getId())).isPresent();
  }

  @Test
  @DisplayName("Should filter tasks by category")
  public void shouldFilterTasksByCategory() throws Exception {
    // Create categories
    com.todoapp.domain.model.Category workCategory = new com.todoapp.domain.model.Category();
    workCategory.setName("Work");
    workCategory.setUser(testUser);
    workCategory = categoryRepository.save(workCategory);

    com.todoapp.domain.model.Category personalCategory = new com.todoapp.domain.model.Category();
    personalCategory.setName("Personal");
    personalCategory.setUser(testUser);
    personalCategory = categoryRepository.save(personalCategory);

    // Create tasks with categories
    Task task1 = new Task();
    task1.setDescription("Work task");
    task1.setUser(testUser);
    task1.setPriority(Priority.MEDIUM);
    task1.setCategory(workCategory);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Personal task");
    task2.setUser(testUser);
    task2.setPriority(Priority.LOW);
    task2.setCategory(personalCategory);
    taskRepository.save(task2);

    Task task3 = new Task();
    task3.setDescription("Uncategorized task");
    task3.setUser(testUser);
    task3.setPriority(Priority.HIGH);
    taskRepository.save(task3);

    // Filter by work category
    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("categoryId", workCategory.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Work task"))
        .andExpect(jsonPath("$.content[0].categoryId").value(workCategory.getId()));
  }

  @Test
  @DisplayName("Should filter tasks by tags")
  public void shouldFilterTasksByTags() throws Exception {
    // Create tags
    com.todoapp.domain.model.Tag urgentTag = new com.todoapp.domain.model.Tag();
    urgentTag.setName("urgent");
    urgentTag.setUser(testUser);
    urgentTag = tagRepository.save(urgentTag);

    com.todoapp.domain.model.Tag importantTag = new com.todoapp.domain.model.Tag();
    importantTag.setName("important");
    importantTag.setUser(testUser);
    importantTag = tagRepository.save(importantTag);

    // Create tasks with tags
    Task task1 = new Task();
    task1.setDescription("Urgent task");
    task1.setUser(testUser);
    task1.setPriority(Priority.HIGH);
    task1.getTags().add(urgentTag);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Important task");
    task2.setUser(testUser);
    task2.setPriority(Priority.MEDIUM);
    task2.getTags().add(importantTag);
    taskRepository.save(task2);

    Task task3 = new Task();
    task3.setDescription("Urgent and important task");
    task3.setUser(testUser);
    task3.setPriority(Priority.HIGH);
    task3.getTags().add(urgentTag);
    task3.getTags().add(importantTag);
    taskRepository.save(task3);

    // Filter by urgent tag
    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("tagIds", urgentTag.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
  @DisplayName("Should filter tasks by both category and tags")
  public void shouldFilterTasksByCategoryAndTags() throws Exception {
    // Create category
    com.todoapp.domain.model.Category workCategory = new com.todoapp.domain.model.Category();
    workCategory.setName("Work");
    workCategory.setUser(testUser);
    workCategory = categoryRepository.save(workCategory);

    // Create tag
    com.todoapp.domain.model.Tag urgentTag = new com.todoapp.domain.model.Tag();
    urgentTag.setName("urgent");
    urgentTag.setUser(testUser);
    urgentTag = tagRepository.save(urgentTag);

    // Create tasks
    Task task1 = new Task();
    task1.setDescription("Urgent work task");
    task1.setUser(testUser);
    task1.setPriority(Priority.HIGH);
    task1.setCategory(workCategory);
    task1.getTags().add(urgentTag);
    taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Non-urgent work task");
    task2.setUser(testUser);
    task2.setPriority(Priority.MEDIUM);
    task2.setCategory(workCategory);
    taskRepository.save(task2);

    Task task3 = new Task();
    task3.setDescription("Urgent personal task");
    task3.setUser(testUser);
    task3.setPriority(Priority.HIGH);
    task3.getTags().add(urgentTag);
    taskRepository.save(task3);

    // Filter by work category AND urgent tag
    mockMvc
        .perform(
            get("/api/v1/tasks")
                .header("X-User-Id", testUser.getId())
                .param("categoryId", workCategory.getId().toString())
                .param("tagIds", urgentTag.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Urgent work task"));
  }

  // ========================================
  // User Story 13: Batch Operations Integration Tests
  // ========================================

  @Test
  @DisplayName("Should batch complete multiple tasks successfully")
  public void shouldBatchCompleteTasksSuccessfully() throws Exception {
    // Create tasks
    Task task1 = new Task();
    task1.setDescription("Task 1");
    task1.setUser(testUser);
    task1.setPriority(Priority.MEDIUM);
    task1.setIsCompleted(false);
    task1 = taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Task 2");
    task2.setUser(testUser);
    task2.setPriority(Priority.HIGH);
    task2.setIsCompleted(false);
    task2 = taskRepository.save(task2);

    Task task3 = new Task();
    task3.setDescription("Task 3");
    task3.setUser(testUser);
    task3.setPriority(Priority.LOW);
    task3.setIsCompleted(false);
    task3 = taskRepository.save(task3);

    String requestBody =
        String.format(
            "{\"taskIds\":[%d,%d,%d],\"operationType\":\"COMPLETE\"}",
            task1.getId(), task2.getId(), task3.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());

    // Verify all tasks are completed
    Task updatedTask1 = taskRepository.findById(task1.getId()).orElseThrow();
    Task updatedTask2 = taskRepository.findById(task2.getId()).orElseThrow();
    Task updatedTask3 = taskRepository.findById(task3.getId()).orElseThrow();

    assertThat(updatedTask1.getIsCompleted()).isTrue();
    assertThat(updatedTask1.getCompletedAt()).isNotNull();
    assertThat(updatedTask2.getIsCompleted()).isTrue();
    assertThat(updatedTask2.getCompletedAt()).isNotNull();
    assertThat(updatedTask3.getIsCompleted()).isTrue();
    assertThat(updatedTask3.getCompletedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should batch delete multiple tasks successfully")
  public void shouldBatchDeleteTasksSuccessfully() throws Exception {
    // Create tasks
    Task task1 = new Task();
    task1.setDescription("Task to delete 1");
    task1.setUser(testUser);
    task1.setPriority(Priority.MEDIUM);
    task1 = taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Task to delete 2");
    task2.setUser(testUser);
    task2.setPriority(Priority.HIGH);
    task2 = taskRepository.save(task2);

    Long task1Id = task1.getId();
    Long task2Id = task2.getId();

    String requestBody =
        String.format("{\"taskIds\":[%d,%d],\"operationType\":\"DELETE\"}", task1Id, task2Id);

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());

    // Verify tasks are deleted
    assertThat(taskRepository.findById(task1Id)).isEmpty();
    assertThat(taskRepository.findById(task2Id)).isEmpty();
  }

  @Test
  @DisplayName("Should batch assign category to multiple tasks")
  public void shouldBatchAssignCategorySuccessfully() throws Exception {
    // Create category
    com.todoapp.domain.model.Category category = new com.todoapp.domain.model.Category();
    category.setName("Batch Category");
    category.setUser(testUser);
    category = categoryRepository.save(category);

    // Create tasks
    Task task1 = new Task();
    task1.setDescription("Task 1");
    task1.setUser(testUser);
    task1.setPriority(Priority.MEDIUM);
    task1 = taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Task 2");
    task2.setUser(testUser);
    task2.setPriority(Priority.HIGH);
    task2 = taskRepository.save(task2);

    String requestBody =
        String.format(
            "{\"taskIds\":[%d,%d],\"operationType\":\"ASSIGN_CATEGORY\",\"categoryId\":%d}",
            task1.getId(), task2.getId(), category.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());

    // Verify category is assigned
    Task updatedTask1 = taskRepository.findById(task1.getId()).orElseThrow();
    Task updatedTask2 = taskRepository.findById(task2.getId()).orElseThrow();

    assertThat(updatedTask1.getCategory()).isNotNull();
    assertThat(updatedTask1.getCategory().getId()).isEqualTo(category.getId());
    assertThat(updatedTask2.getCategory()).isNotNull();
    assertThat(updatedTask2.getCategory().getId()).isEqualTo(category.getId());
  }

  @Test
  @DisplayName("Should batch assign tags to multiple tasks")
  public void shouldBatchAssignTagsSuccessfully() throws Exception {
    // Create tags
    com.todoapp.domain.model.Tag tag1 = new com.todoapp.domain.model.Tag();
    tag1.setName("tag1");
    tag1.setUser(testUser);
    tag1 = tagRepository.save(tag1);

    com.todoapp.domain.model.Tag tag2 = new com.todoapp.domain.model.Tag();
    tag2.setName("tag2");
    tag2.setUser(testUser);
    tag2 = tagRepository.save(tag2);

    // Create tasks
    Task task1 = new Task();
    task1.setDescription("Task 1");
    task1.setUser(testUser);
    task1.setPriority(Priority.MEDIUM);
    task1 = taskRepository.save(task1);

    Task task2 = new Task();
    task2.setDescription("Task 2");
    task2.setUser(testUser);
    task2.setPriority(Priority.HIGH);
    task2 = taskRepository.save(task2);

    String requestBody =
        String.format(
            "{\"taskIds\":[%d,%d],\"operationType\":\"ASSIGN_TAGS\",\"tagIds\":[%d,%d]}",
            task1.getId(), task2.getId(), tag1.getId(), tag2.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());

    // Verify tags are assigned
    Task updatedTask1 = taskRepository.findById(task1.getId()).orElseThrow();
    Task updatedTask2 = taskRepository.findById(task2.getId()).orElseThrow();

    assertThat(updatedTask1.getTags()).hasSize(2);
    assertThat(updatedTask1.getTags())
        .extracting("id")
        .containsExactlyInAnyOrder(tag1.getId(), tag2.getId());
    assertThat(updatedTask2.getTags()).hasSize(2);
    assertThat(updatedTask2.getTags())
        .extracting("id")
        .containsExactlyInAnyOrder(tag1.getId(), tag2.getId());
  }

  @Test
  @DisplayName("Should skip tasks not owned by user in batch complete")
  public void shouldSkipNonOwnedTasksInBatchComplete() throws Exception {
    // Create another user
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    // Create task for current user
    Task ownTask = new Task();
    ownTask.setDescription("Own task");
    ownTask.setUser(testUser);
    ownTask.setPriority(Priority.MEDIUM);
    ownTask.setIsCompleted(false);
    ownTask = taskRepository.save(ownTask);

    // Create task for other user
    Task otherTask = new Task();
    otherTask.setDescription("Other user's task");
    otherTask.setUser(otherUser);
    otherTask.setPriority(Priority.HIGH);
    otherTask.setIsCompleted(false);
    otherTask = taskRepository.save(otherTask);

    String requestBody =
        String.format(
            "{\"taskIds\":[%d,%d],\"operationType\":\"COMPLETE\"}",
            ownTask.getId(), otherTask.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    // Verify only owned task is completed
    Task updatedOwnTask = taskRepository.findById(ownTask.getId()).orElseThrow();
    Task updatedOtherTask = taskRepository.findById(otherTask.getId()).orElseThrow();

    assertThat(updatedOwnTask.getIsCompleted()).isTrue();
    assertThat(updatedOtherTask.getIsCompleted()).isFalse();
  }

  @Test
  @DisplayName("Should skip tasks not owned by user in batch delete")
  public void shouldSkipNonOwnedTasksInBatchDelete() throws Exception {
    // Create another user
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    // Create task for current user
    Task ownTask = new Task();
    ownTask.setDescription("Own task");
    ownTask.setUser(testUser);
    ownTask.setPriority(Priority.MEDIUM);
    ownTask = taskRepository.save(ownTask);

    // Create task for other user
    Task otherTask = new Task();
    otherTask.setDescription("Other user's task");
    otherTask.setUser(otherUser);
    otherTask.setPriority(Priority.HIGH);
    otherTask = taskRepository.save(otherTask);

    Long ownTaskId = ownTask.getId();
    Long otherTaskId = otherTask.getId();

    String requestBody =
        String.format("{\"taskIds\":[%d,%d],\"operationType\":\"DELETE\"}", ownTaskId, otherTaskId);

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    // Verify only owned task is deleted
    assertThat(taskRepository.findById(ownTaskId)).isEmpty();
    assertThat(taskRepository.findById(otherTaskId)).isPresent();
  }

  @Test
  @DisplayName("Should reject batch operation with empty task list")
  public void shouldRejectBatchOperationWithEmptyTaskList() throws Exception {
    String requestBody = "{\"taskIds\":[],\"operationType\":\"COMPLETE\"}";

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should reject ASSIGN_CATEGORY operation without categoryId")
  public void shouldRejectAssignCategoryWithoutCategoryId() throws Exception {
    Task task = new Task();
    task.setDescription("Task 1");
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    String requestBody =
        String.format("{\"taskIds\":[%d],\"operationType\":\"ASSIGN_CATEGORY\"}", task.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should reject ASSIGN_TAGS operation without tagIds")
  public void shouldRejectAssignTagsWithoutTagIds() throws Exception {
    Task task = new Task();
    task.setDescription("Task 1");
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);
    task = taskRepository.save(task);

    String requestBody =
        String.format("{\"taskIds\":[%d],\"operationType\":\"ASSIGN_TAGS\"}", task.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should handle batch operation with non-existent task IDs gracefully")
  public void shouldHandleBatchOperationWithNonExistentTasks() throws Exception {
    Task task = new Task();
    task.setDescription("Valid task");
    task.setUser(testUser);
    task.setPriority(Priority.MEDIUM);
    task.setIsCompleted(false);
    task = taskRepository.save(task);

    // Include both valid and non-existent task IDs
    String requestBody =
        String.format(
            "{\"taskIds\":[%d,99999,88888],\"operationType\":\"COMPLETE\"}", task.getId());

    mockMvc
        .perform(
            post("/api/v1/tasks/batch")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    // Verify valid task is completed
    Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getIsCompleted()).isTrue();
  }
}
