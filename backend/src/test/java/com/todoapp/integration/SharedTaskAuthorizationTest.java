package com.todoapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;
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
public class SharedTaskAuthorizationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TaskShareRepository taskShareRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User taskOwner;
  private User viewUser;
  private User editUser;
  private User unauthorizedUser;
  private Task testTask;

  @BeforeEach
  public void setUp() {
    taskShareRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    taskOwner = new User();
    taskOwner.setEmail("owner@example.com");
    taskOwner.setPasswordHash("$2a$10$dummyhash");
    taskOwner.setIsActive(true);
    taskOwner = userRepository.save(taskOwner);

    viewUser = new User();
    viewUser.setEmail("viewer@example.com");
    viewUser.setPasswordHash("$2a$10$dummyhash");
    viewUser.setIsActive(true);
    viewUser = userRepository.save(viewUser);

    editUser = new User();
    editUser.setEmail("editor@example.com");
    editUser.setPasswordHash("$2a$10$dummyhash");
    editUser.setIsActive(true);
    editUser = userRepository.save(editUser);

    unauthorizedUser = new User();
    unauthorizedUser.setEmail("unauthorized@example.com");
    unauthorizedUser.setPasswordHash("$2a$10$dummyhash");
    unauthorizedUser.setIsActive(true);
    unauthorizedUser = userRepository.save(unauthorizedUser);

    testTask = new Task();
    testTask.setUser(taskOwner);
    testTask.setDescription("Shared test task");
    testTask.setPriority(Priority.MEDIUM);
    testTask = taskRepository.save(testTask);

    TaskShare viewShare = new TaskShare();
    viewShare.setTask(testTask);
    viewShare.setSharedWithUser(viewUser);
    viewShare.setSharedByUser(taskOwner);
    viewShare.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(viewShare);

    TaskShare editShare = new TaskShare();
    editShare.setTask(testTask);
    editShare.setSharedWithUser(editUser);
    editShare.setSharedByUser(taskOwner);
    editShare.setPermissionLevel(PermissionLevel.EDIT);
    taskShareRepository.save(editShare);
  }

  @Test
  @DisplayName("Should allow task owner to view their task")
  public void shouldAllowTaskOwnerToViewTheirTask() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", taskOwner.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testTask.getId()))
        .andExpect(jsonPath("$.description").value("Shared test task"));
  }

  @Test
  @DisplayName("Should allow user with VIEW permission to view shared task")
  public void shouldAllowUserWithViewPermissionToViewSharedTask() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", viewUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testTask.getId()))
        .andExpect(jsonPath("$.description").value("Shared test task"));
  }

  @Test
  @DisplayName("Should allow user with EDIT permission to view shared task")
  public void shouldAllowUserWithEditPermissionToViewSharedTask() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", editUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testTask.getId()));
  }

  @Test
  @DisplayName("Should deny unauthorized user from viewing task")
  public void shouldDenyUnauthorizedUserFromViewingTask() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", unauthorizedUser.getId()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should allow task owner to edit their task")
  public void shouldAllowTaskOwnerToEditTheirTask() throws Exception {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated by owner");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", taskOwner.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Updated by owner"));
  }

  @Test
  @DisplayName("Should allow user with EDIT permission to edit shared task")
  public void shouldAllowUserWithEditPermissionToEditSharedTask() throws Exception {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated by editor");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", editUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Updated by editor"));
  }

  @Test
  @DisplayName("Should deny user with VIEW permission from editing shared task")
  public void shouldDenyUserWithViewPermissionFromEditingSharedTask() throws Exception {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Attempt to update");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", viewUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should deny unauthorized user from editing task")
  public void shouldDenyUnauthorizedUserFromEditingTask() throws Exception {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Unauthorized update");

    mockMvc
        .perform(
            put("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", unauthorizedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should allow task owner to delete their task")
  public void shouldAllowTaskOwnerToDeleteTheirTask() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", taskOwner.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should deny user with EDIT permission from deleting shared task")
  public void shouldDenyUserWithEditPermissionFromDeletingSharedTask() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", editUser.getId()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should deny user with VIEW permission from deleting shared task")
  public void shouldDenyUserWithViewPermissionFromDeletingSharedTask() throws Exception {
    mockMvc
        .perform(delete("/api/v1/tasks/" + testTask.getId()).header("X-User-Id", viewUser.getId()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should deny unauthorized user from deleting task")
  public void shouldDenyUnauthorizedUserFromDeletingTask() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/tasks/" + testTask.getId())
                .header("X-User-Id", unauthorizedUser.getId()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should include shared tasks in shared-with-me endpoint")
  public void shouldIncludeSharedTasksInSharedWithMeEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/shared-with-me").header("X-User-Id", viewUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(testTask.getId()));

    mockMvc
        .perform(get("/api/v1/tasks/shared-with-me").header("X-User-Id", editUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(testTask.getId()));
  }

  @Test
  @DisplayName("Should not include shared tasks for unauthorized user")
  public void shouldNotIncludeSharedTasksForUnauthorizedUser() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tasks/shared-with-me")
                .header("X-User-Id", unauthorizedUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
