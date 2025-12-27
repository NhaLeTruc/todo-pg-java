package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import java.util.List;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SubtaskApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;
  private Task parentTask;

  @BeforeEach
  public void setUp() {
    taskRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);

    parentTask = new Task();
    parentTask.setUser(testUser);
    parentTask.setDescription("Parent task");
    parentTask.setPriority(Priority.MEDIUM);
    parentTask = taskRepository.save(parentTask);
  }

  @Test
  @DisplayName("Should create subtask successfully")
  public void shouldCreateSubtaskSuccessfully() throws Exception {
    TaskCreateDTO subtaskDTO = new TaskCreateDTO();
    subtaskDTO.setDescription("First subtask");
    subtaskDTO.setPriority(Priority.LOW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + parentTask.getId() + "/subtasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subtaskDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.description").value("First subtask"))
        .andExpect(jsonPath("$.depth").value(1));

    List<Task> subtasks = taskRepository.findByParentTaskId(parentTask.getId());
    assertThat(subtasks).hasSize(1);
    assertThat(subtasks.get(0).getDescription()).isEqualTo("First subtask");
    assertThat(subtasks.get(0).getDepth()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should get subtasks for parent task")
  public void shouldGetSubtasksForParentTask() throws Exception {
    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setPriority(Priority.LOW);
    subtask1.setParentTask(parentTask);
    taskRepository.save(subtask1);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setPriority(Priority.MEDIUM);
    subtask2.setParentTask(parentTask);
    taskRepository.save(subtask2);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + parentTask.getId() + "/subtasks")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].description").exists())
        .andExpect(jsonPath("$[1].description").exists());
  }

  @Test
  @DisplayName("Should check if task has subtasks")
  public void shouldCheckIfTaskHasSubtasks() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tasks/" + parentTask.getId() + "/has-subtasks")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(false));

    Task subtask = new Task();
    subtask.setUser(testUser);
    subtask.setDescription("Subtask");
    subtask.setPriority(Priority.LOW);
    subtask.setParentTask(parentTask);
    taskRepository.save(subtask);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + parentTask.getId() + "/has-subtasks")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(true));
  }

  @Test
  @DisplayName("Should delete task with subtasks (cascade)")
  public void shouldDeleteTaskWithSubtasksCascade() throws Exception {
    Task subtask1 = new Task();
    subtask1.setUser(testUser);
    subtask1.setDescription("Subtask 1");
    subtask1.setPriority(Priority.LOW);
    subtask1.setParentTask(parentTask);
    taskRepository.save(subtask1);

    Task subtask2 = new Task();
    subtask2.setUser(testUser);
    subtask2.setDescription("Subtask 2");
    subtask2.setPriority(Priority.MEDIUM);
    subtask2.setParentTask(parentTask);
    taskRepository.save(subtask2);

    assertThat(taskRepository.findByParentTaskId(parentTask.getId())).hasSize(2);

    mockMvc
        .perform(
            delete("/api/v1/tasks/" + parentTask.getId())
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(taskRepository.findById(parentTask.getId())).isEmpty();
    assertThat(taskRepository.findByParentTaskId(parentTask.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should create nested subtasks up to depth 5")
  public void shouldCreateNestedSubtasksUpToDepth5() throws Exception {
    Task level1 = new Task();
    level1.setUser(testUser);
    level1.setDescription("Level 1");
    level1.setPriority(Priority.MEDIUM);
    level1.setParentTask(parentTask);
    level1 = taskRepository.save(level1);

    Task level2 = new Task();
    level2.setUser(testUser);
    level2.setDescription("Level 2");
    level2.setPriority(Priority.MEDIUM);
    level2.setParentTask(level1);
    level2 = taskRepository.save(level2);

    Task level3 = new Task();
    level3.setUser(testUser);
    level3.setDescription("Level 3");
    level3.setPriority(Priority.MEDIUM);
    level3.setParentTask(level2);
    level3 = taskRepository.save(level3);

    Task level4 = new Task();
    level4.setUser(testUser);
    level4.setDescription("Level 4");
    level4.setPriority(Priority.MEDIUM);
    level4.setParentTask(level3);
    level4 = taskRepository.save(level4);

    Task level5 = new Task();
    level5.setUser(testUser);
    level5.setDescription("Level 5");
    level5.setPriority(Priority.MEDIUM);
    level5.setParentTask(level4);
    level5 = taskRepository.save(level5);

    assertThat(level5.getDepth()).isEqualTo(5);

    TaskCreateDTO subtaskDTO = new TaskCreateDTO();
    subtaskDTO.setDescription("Level 5 via API");
    subtaskDTO.setPriority(Priority.LOW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + level4.getId() + "/subtasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subtaskDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.depth").value(5));
  }

  @Test
  @DisplayName("Should reject subtask creation beyond depth 5")
  public void shouldRejectSubtaskCreationBeyondDepth5() throws Exception {
    Task level1 = new Task();
    level1.setUser(testUser);
    level1.setDescription("Level 1");
    level1.setPriority(Priority.MEDIUM);
    level1.setParentTask(parentTask);
    level1 = taskRepository.save(level1);

    Task level2 = new Task();
    level2.setUser(testUser);
    level2.setDescription("Level 2");
    level2.setPriority(Priority.MEDIUM);
    level2.setParentTask(level1);
    level2 = taskRepository.save(level2);

    Task level3 = new Task();
    level3.setUser(testUser);
    level3.setDescription("Level 3");
    level3.setPriority(Priority.MEDIUM);
    level3.setParentTask(level2);
    level3 = taskRepository.save(level3);

    Task level4 = new Task();
    level4.setUser(testUser);
    level4.setDescription("Level 4");
    level4.setPriority(Priority.MEDIUM);
    level4.setParentTask(level3);
    level4 = taskRepository.save(level4);

    Task level5 = new Task();
    level5.setUser(testUser);
    level5.setDescription("Level 5");
    level5.setPriority(Priority.MEDIUM);
    level5.setParentTask(level4);
    level5 = taskRepository.save(level5);

    TaskCreateDTO subtaskDTO = new TaskCreateDTO();
    subtaskDTO.setDescription("Level 6 - should fail");
    subtaskDTO.setPriority(Priority.LOW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + level5.getId() + "/subtasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subtaskDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 404 when creating subtask for non-existent parent")
  public void shouldReturn404WhenCreatingSubtaskForNonExistentParent() throws Exception {
    TaskCreateDTO subtaskDTO = new TaskCreateDTO();
    subtaskDTO.setDescription("Orphan subtask");
    subtaskDTO.setPriority(Priority.LOW);

    mockMvc
        .perform(
            post("/api/v1/tasks/99999/subtasks")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subtaskDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when getting subtasks for non-existent parent")
  public void shouldReturn404WhenGettingSubtasksForNonExistentParent() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/99999/subtasks").header("X-User-Id", testUser.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should prevent unauthorized user from creating subtask")
  public void shouldPreventUnauthorizedUserFromCreatingSubtask() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    TaskCreateDTO subtaskDTO = new TaskCreateDTO();
    subtaskDTO.setDescription("Unauthorized subtask");
    subtaskDTO.setPriority(Priority.LOW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + parentTask.getId() + "/subtasks")
                .header("X-User-Id", otherUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subtaskDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should prevent unauthorized user from viewing subtasks")
  public void shouldPreventUnauthorizedUserFromViewingSubtasks() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + parentTask.getId() + "/subtasks")
                .header("X-User-Id", otherUser.getId()))
        .andExpect(status().isBadRequest());
  }
}
