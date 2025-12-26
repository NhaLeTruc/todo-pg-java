package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  public void setUp() {
    taskRepository.deleteAll();
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
}
