package com.todoapp.application.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.TaskShareDTO;
import com.todoapp.domain.model.NotificationType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

@Service
@Transactional
public class TaskShareService {

  private static final Logger logger = LoggerFactory.getLogger(TaskShareService.class);

  private final TaskShareRepository taskShareRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public TaskShareService(
      TaskShareRepository taskShareRepository,
      TaskRepository taskRepository,
      UserRepository userRepository,
      NotificationService notificationService) {
    this.taskShareRepository = taskShareRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  public TaskShareDTO shareTask(Long taskId, TaskShareDTO shareDTO, Long sharingUserId) {
    logger.info(
        "Sharing task ID {} with user ID {} by user ID {}",
        taskId,
        shareDTO.getSharedWithUserId(),
        sharingUserId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    User sharingUser =
        userRepository
            .findById(sharingUserId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with ID: " + sharingUserId));

    if (!task.getUser().getId().equals(sharingUserId)) {
      throw new IllegalArgumentException("Only the task owner can share the task");
    }

    if (shareDTO.getSharedWithUserId().equals(sharingUserId)) {
      throw new IllegalArgumentException("Cannot share task with yourself");
    }

    User sharedWithUser =
        userRepository
            .findById(shareDTO.getSharedWithUserId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found with ID: " + shareDTO.getSharedWithUserId()));

    Optional<TaskShare> existingShare =
        taskShareRepository.findByTaskIdAndSharedWithUserId(taskId, shareDTO.getSharedWithUserId());

    TaskShare taskShare;
    if (existingShare.isPresent()) {
      taskShare = existingShare.get();
      taskShare.setPermissionLevel(shareDTO.getPermissionLevel());
      logger.info("Updating existing share with new permission level");
    } else {
      taskShare = new TaskShare();
      taskShare.setTask(task);
      taskShare.setSharedWithUser(sharedWithUser);
      taskShare.setSharedByUser(sharingUser);
      taskShare.setPermissionLevel(shareDTO.getPermissionLevel());
    }

    TaskShare savedShare = taskShareRepository.save(taskShare);

    // Send notification to the user the task is shared with
    if (!existingShare.isPresent()) {
      String message =
          String.format(
              "Task '%s' has been shared with you by %s with %s permission",
              task.getDescription(), sharingUser.getEmail(), shareDTO.getPermissionLevel());
      notificationService.createNotification(
          sharedWithUser, NotificationType.TASK_SHARED, message, task);
    }

    return toDTO(savedShare);
  }

  public void revokeShare(Long taskId, Long sharedWithUserId, Long userId) {
    logger.info(
        "Revoking share for task ID {} from user ID {} by user ID {}",
        taskId,
        sharedWithUserId,
        userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Only the task owner can revoke shares");
    }

    TaskShare taskShare =
        taskShareRepository
            .findByTaskIdAndSharedWithUserId(taskId, sharedWithUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Task share not found for this user"));

    taskShareRepository.delete(taskShare);
    logger.info("Share revoked successfully");
  }

  @Transactional(readOnly = true)
  public List<TaskShareDTO> getTaskShares(Long taskId) {
    logger.info("Fetching all shares for task ID {}", taskId);
    List<TaskShare> shares = taskShareRepository.findByTaskId(taskId);
    return shares.stream().map(this::toDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<TaskShareDTO> getTasksSharedWithUser(Long userId) {
    logger.info("Fetching all tasks shared with user ID {}", userId);
    List<TaskShare> shares = taskShareRepository.findBySharedWithUserId(userId);
    return shares.stream().map(this::toDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Optional<TaskShare> getTaskShareForUser(Long taskId, Long userId) {
    return taskShareRepository.findByTaskIdAndSharedWithUserId(taskId, userId);
  }

  private TaskShareDTO toDTO(TaskShare taskShare) {
    TaskShareDTO dto = new TaskShareDTO();
    dto.setId(taskShare.getId());
    dto.setTaskId(taskShare.getTask().getId());
    dto.setTaskDescription(taskShare.getTask().getDescription());
    dto.setSharedWithUserId(taskShare.getSharedWithUser().getId());
    dto.setSharedWithUserEmail(taskShare.getSharedWithUser().getEmail());
    dto.setSharedByUserId(taskShare.getSharedByUser().getId());
    dto.setSharedByUserEmail(taskShare.getSharedByUser().getEmail());
    dto.setPermissionLevel(taskShare.getPermissionLevel());
    dto.setSharedAt(taskShare.getSharedAt());
    return dto;
  }
}
