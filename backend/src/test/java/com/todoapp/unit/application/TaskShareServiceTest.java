package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.application.dto.TaskShareDTO;
import com.todoapp.application.service.NotificationService;
import com.todoapp.application.service.TaskShareService;
import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TaskShareServiceTest {

  @Mock private TaskShareRepository taskShareRepository;

  @Mock private TaskRepository taskRepository;

  @Mock private UserRepository userRepository;

  @Mock private NotificationService notificationService;

  @InjectMocks private TaskShareService taskShareService;

  private User owner;
  private User sharedWith;
  private Task task;
  private TaskShare taskShare;

  @BeforeEach
  public void setUp() {
    owner = new User();
    owner.setId(1L);
    owner.setEmail("owner@example.com");

    sharedWith = new User();
    sharedWith.setId(2L);
    sharedWith.setEmail("shared@example.com");

    task = new Task();
    task.setId(1L);
    task.setDescription("Test task");
    task.setUser(owner);

    taskShare = new TaskShare();
    taskShare.setId(1L);
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);
    taskShare.setSharedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should share task successfully with VIEW permission")
  public void shouldShareTaskSuccessfullyWithViewPermission() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    when(userRepository.findById(2L)).thenReturn(Optional.of(sharedWith));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L)).thenReturn(Optional.empty());
    when(taskShareRepository.save(any(TaskShare.class))).thenReturn(taskShare);

    TaskShareDTO result = taskShareService.shareTask(1L, shareDTO, 1L);

    assertNotNull(result);
    assertEquals(2L, result.getSharedWithUserId());
    assertEquals(PermissionLevel.VIEW, result.getPermissionLevel());
    verify(taskShareRepository, times(1)).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should share task successfully with EDIT permission")
  public void shouldShareTaskSuccessfullyWithEditPermission() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.EDIT);

    taskShare.setPermissionLevel(PermissionLevel.EDIT);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    when(userRepository.findById(2L)).thenReturn(Optional.of(sharedWith));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L)).thenReturn(Optional.empty());
    when(taskShareRepository.save(any(TaskShare.class))).thenReturn(taskShare);

    TaskShareDTO result = taskShareService.shareTask(1L, shareDTO, 1L);

    assertNotNull(result);
    assertEquals(PermissionLevel.EDIT, result.getPermissionLevel());
  }

  @Test
  @DisplayName("Should throw exception when task not found")
  public void shouldThrowExceptionWhenTaskNotFound() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> taskShareService.shareTask(99L, shareDTO, 1L));

    verify(taskShareRepository, never()).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should throw exception when sharing user not found")
  public void shouldThrowExceptionWhenSharingUserNotFound() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> taskShareService.shareTask(1L, shareDTO, 99L));

    verify(taskShareRepository, never()).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should throw exception when shared-with user not found")
  public void shouldThrowExceptionWhenSharedWithUserNotFound() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(99L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> taskShareService.shareTask(1L, shareDTO, 1L));

    verify(taskShareRepository, never()).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should throw exception when non-owner tries to share task")
  public void shouldThrowExceptionWhenNonOwnerTriesToShare() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    User nonOwner = new User();
    nonOwner.setId(3L);
    nonOwner.setEmail("nonowner@example.com");

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(3L)).thenReturn(Optional.of(nonOwner));

    assertThrows(
        IllegalArgumentException.class, () -> taskShareService.shareTask(1L, shareDTO, 3L));

    verify(taskShareRepository, never()).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should prevent sharing task with self")
  public void shouldPreventSharingTaskWithSelf() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(1L);
    shareDTO.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

    assertThrows(
        IllegalArgumentException.class, () -> taskShareService.shareTask(1L, shareDTO, 1L));

    verify(taskShareRepository, never()).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should update existing share permission")
  public void shouldUpdateExistingSharePermission() {
    TaskShareDTO shareDTO = new TaskShareDTO();
    shareDTO.setSharedWithUserId(2L);
    shareDTO.setPermissionLevel(PermissionLevel.EDIT);

    TaskShare existingShare = new TaskShare();
    existingShare.setId(1L);
    existingShare.setTask(task);
    existingShare.setSharedWithUser(sharedWith);
    existingShare.setSharedByUser(owner);
    existingShare.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    when(userRepository.findById(2L)).thenReturn(Optional.of(sharedWith));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L))
        .thenReturn(Optional.of(existingShare));
    when(taskShareRepository.save(any(TaskShare.class))).thenReturn(existingShare);

    TaskShareDTO result = taskShareService.shareTask(1L, shareDTO, 1L);

    assertNotNull(result);
    assertEquals(PermissionLevel.EDIT, result.getPermissionLevel());
    verify(taskShareRepository, times(1)).save(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should revoke task share successfully")
  public void shouldRevokeTaskShareSuccessfully() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L))
        .thenReturn(Optional.of(taskShare));

    taskShareService.revokeShare(1L, 2L, 1L);

    verify(taskShareRepository, times(1)).delete(taskShare);
  }

  @Test
  @DisplayName("Should throw exception when revoking non-existent share")
  public void shouldThrowExceptionWhenRevokingNonExistentShare() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskShareService.revokeShare(1L, 99L, 1L));

    verify(taskShareRepository, never()).delete(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should throw exception when non-owner tries to revoke share")
  public void shouldThrowExceptionWhenNonOwnerTriesToRevoke() {
    User nonOwner = new User();
    nonOwner.setId(3L);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    assertThrows(IllegalArgumentException.class, () -> taskShareService.revokeShare(1L, 2L, 3L));

    verify(taskShareRepository, never()).delete(any(TaskShare.class));
  }

  @Test
  @DisplayName("Should get all shares for a task")
  public void shouldGetAllSharesForTask() {
    TaskShare share2 = new TaskShare();
    share2.setTask(task);
    share2.setSharedWithUser(new User());
    share2.getSharedWithUser().setId(3L);
    share2.getSharedWithUser().setEmail("user3@example.com");
    share2.setSharedByUser(owner);
    share2.setPermissionLevel(PermissionLevel.EDIT);

    when(taskShareRepository.findByTaskId(1L)).thenReturn(List.of(taskShare, share2));

    List<TaskShareDTO> shares = taskShareService.getTaskShares(1L);

    assertNotNull(shares);
    assertEquals(2, shares.size());
    verify(taskShareRepository, times(1)).findByTaskId(1L);
  }

  @Test
  @DisplayName("Should get all tasks shared with user")
  public void shouldGetAllTasksSharedWithUser() {
    when(taskShareRepository.findBySharedWithUserId(2L)).thenReturn(List.of(taskShare));

    List<TaskShareDTO> shares = taskShareService.getTasksSharedWithUser(2L);

    assertNotNull(shares);
    assertEquals(1, shares.size());
    assertEquals(1L, shares.get(0).getTaskId());
    verify(taskShareRepository, times(1)).findBySharedWithUserId(2L);
  }
}
