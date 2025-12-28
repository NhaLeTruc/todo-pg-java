package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.todoapp.application.dto.TaskShareDTO;
import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskShareApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TaskShareRepository taskShareRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;
  private User otherUser;
  private Task testTask;

  @BeforeEach
  public void setUp() {
    taskShareRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);

    otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    testTask = new Task();
    testTask.setUser(testUser);
    testTask.setDescription("Test task");
    testTask.setPriority(Priority.MEDIUM);
    testTask = taskRepository.save(testTask);
  }

  @Test
  @DisplayName("Should share task successfully with VIEW permission")
  public void shouldShareTaskSuccessfullyWithViewPermission() throws Exception {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(otherUser.getId());
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/share")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shareDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.taskId").value(testTask.getId()))
        .andExpect(jsonPath("$.sharedWithUserId").value(otherUser.getId()))
        .andExpect(jsonPath("$.permissionLevel").value("VIEW"));

    assertThat(taskShareRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should share task successfully with EDIT permission")
  public void shouldShareTaskSuccessfullyWithEditPermission() throws Exception {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(otherUser.getId());
    shareDTO.setPermissionLevel(PermissionLevel.EDIT);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/share")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shareDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.permissionLevel").value("EDIT"));

    assertThat(taskShareRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should reject sharing task with self")
  public void shouldRejectSharingTaskWithSelf() throws Exception {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(testUser.getId());
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/share")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shareDTO)))
        .andExpect(status().isBadRequest());

    assertThat(taskShareRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should reject sharing by non-owner")
  public void shouldRejectSharingByNonOwner() throws Exception {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(otherUser.getId());
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/share")
                .header("X-User-Id", otherUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shareDTO)))
        .andExpect(status().isBadRequest());

    assertThat(taskShareRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should update existing share permission")
  public void shouldUpdateExistingSharePermission() throws Exception {
    TaskShare existingShare = new TaskShare();
    existingShare.setTask(testTask);
    existingShare.setSharedWithUser(otherUser);
    existingShare.setSharedByUser(testUser);
    existingShare.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(existingShare);

    TaskShareDTO updateDTO = new TaskShareDTO();
    updateDTO.setSharedWithUserId(otherUser.getId());
    updateDTO.setPermissionLevel(PermissionLevel.EDIT);

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/share")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.permissionLevel").value("EDIT"));

    assertThat(taskShareRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should revoke task share successfully")
  public void shouldRevokeTaskShareSuccessfully() throws Exception {
    TaskShare share = new TaskShare();
    share.setTask(testTask);
    share.setSharedWithUser(otherUser);
    share.setSharedByUser(testUser);
    share.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(share);

    mockMvc
        .perform(
            delete("/api/v1/tasks/" + testTask.getId() + "/share/" + otherUser.getId())
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(taskShareRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should reject revoking share by non-owner")
  public void shouldRejectRevokingShareByNonOwner() throws Exception {
    TaskShare share = new TaskShare();
    share.setTask(testTask);
    share.setSharedWithUser(otherUser);
    share.setSharedByUser(testUser);
    share.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(share);

    mockMvc
        .perform(
            delete("/api/v1/tasks/" + testTask.getId() + "/share/" + otherUser.getId())
                .header("X-User-Id", otherUser.getId()))
        .andExpect(status().isBadRequest());

    assertThat(taskShareRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should get all shares for a task")
  public void shouldGetAllSharesForTask() throws Exception {
    User user3 = new User();
    user3.setEmail("user3@example.com");
    user3.setPasswordHash("$2a$10$dummyhash");
    user3.setIsActive(true);
    user3 = userRepository.save(user3);

    TaskShare share1 = new TaskShare();
    share1.setTask(testTask);
    share1.setSharedWithUser(otherUser);
    share1.setSharedByUser(testUser);
    share1.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(share1);

    TaskShare share2 = new TaskShare();
    share2.setTask(testTask);
    share2.setSharedWithUser(user3);
    share2.setSharedByUser(testUser);
    share2.setPermissionLevel(PermissionLevel.EDIT);
    taskShareRepository.save(share2);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + testTask.getId() + "/shares")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("Should get tasks shared with user")
  public void shouldGetTasksSharedWithUser() throws Exception {
    Task task2 = new Task();
    task2.setUser(testUser);
    task2.setDescription("Task 2");
    task2.setPriority(Priority.HIGH);
    task2 = taskRepository.save(task2);

    TaskShare share1 = new TaskShare();
    share1.setTask(testTask);
    share1.setSharedWithUser(otherUser);
    share1.setSharedByUser(testUser);
    share1.setPermissionLevel(PermissionLevel.VIEW);
    taskShareRepository.save(share1);

    TaskShare share2 = new TaskShare();
    share2.setTask(task2);
    share2.setSharedWithUser(otherUser);
    share2.setSharedByUser(testUser);
    share2.setPermissionLevel(PermissionLevel.EDIT);
    taskShareRepository.save(share2);

    mockMvc
        .perform(get("/api/v1/tasks/shared-with-me").header("X-User-Id", otherUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }
}
