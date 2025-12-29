package com.todoapp.application.service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.dto.TaskUpdateMessage;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.RecurrencePattern;
import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
import com.todoapp.domain.repository.RecurrencePatternRepository;
import com.todoapp.domain.repository.TagRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TaskService {

  private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final TaskShareRepository taskShareRepository;
  private final RecurrencePatternRepository recurrencePatternRepository;
  private final TaskMapper taskMapper;
  private final com.todoapp.presentation.websocket.TaskWebSocketHandler webSocketHandler;
  private RecurrenceService recurrenceService; // Lazy injection to avoid circular dependency

  public TaskService(
      TaskRepository taskRepository,
      UserRepository userRepository,
      CategoryRepository categoryRepository,
      TagRepository tagRepository,
      TaskShareRepository taskShareRepository,
      RecurrencePatternRepository recurrencePatternRepository,
      TaskMapper taskMapper,
      com.todoapp.presentation.websocket.TaskWebSocketHandler webSocketHandler) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.tagRepository = tagRepository;
    this.taskShareRepository = taskShareRepository;
    this.recurrencePatternRepository = recurrencePatternRepository;
    this.taskMapper = taskMapper;
    this.webSocketHandler = webSocketHandler;
  }

  /**
   * Set the recurrence service (used for lazy injection to avoid circular dependency).
   *
   * @param recurrenceService the recurrence service
   */
  public void setRecurrenceService(RecurrenceService recurrenceService) {
    this.recurrenceService = recurrenceService;
  }

  public TaskResponseDTO createTask(TaskCreateDTO createDTO, Long userId) {
    logger.debug("Creating task for user ID: {}", userId);

    if (createDTO.getDescription() == null || createDTO.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException("Description cannot be empty");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    Task task = taskMapper.toEntity(createDTO, user);

    // Handle category assignment
    if (createDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findByIdAndUserId(createDTO.getCategoryId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category not found with ID: " + createDTO.getCategoryId()));
      task.setCategory(category);
    }

    // Handle tag assignments
    if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
      List<Tag> tags = tagRepository.findByIdInAndUserId(createDTO.getTagIds(), userId);
      if (tags.size() != createDTO.getTagIds().size()) {
        throw new ResourceNotFoundException("One or more tags not found");
      }
      task.setTags(tags);
    }

    Task savedTask = taskRepository.save(task);

    logger.info("Task created with ID: {} for user ID: {}", savedTask.getId(), userId);

    // Broadcast WebSocket update to task owner
    TaskUpdateMessage message =
        TaskUpdateMessage.created(savedTask.getId(), userId, savedTask.getDescription());
    webSocketHandler.sendTaskUpdateToUser(userId, message);

    return taskMapper.toResponseDTO(savedTask);
  }

  public Page<TaskResponseDTO> getUserTasks(Long userId, Pageable pageable) {
    logger.debug("Fetching tasks for user ID: {} with pagination: {}", userId, pageable);

    Page<Task> tasks = taskRepository.findByUserId(userId, pageable);

    logger.debug("Found {} tasks for user ID: {}", tasks.getTotalElements(), userId);
    return tasks.map(taskMapper::toResponseDTO);
  }

  public TaskResponseDTO getTaskById(Long taskId, Long userId) {
    logger.debug("Fetching task ID: {} for user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!hasTaskAccess(task, userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    return taskMapper.toResponseDTO(task);
  }

  /**
   * Get Task entity by ID for internal service operations.
   *
   * @param taskId The task ID
   * @param userId The user ID for authorization
   * @return Task entity
   * @throws ResourceNotFoundException if task not found
   * @throws IllegalArgumentException if user does not have access
   */
  public Task getTaskEntityById(Long taskId, Long userId) {
    logger.debug("Fetching task entity ID: {} for user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!hasTaskAccess(task, userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    return task;
  }

  private boolean hasTaskAccess(Task task, Long userId) {
    if (task.getUser().getId().equals(userId)) {
      return true;
    }
    return taskShareRepository.findByTaskIdAndSharedWithUserId(task.getId(), userId).isPresent();
  }

  private boolean hasEditPermission(Task task, Long userId) {
    if (task.getUser().getId().equals(userId)) {
      return true;
    }
    return taskShareRepository
        .findByTaskIdAndSharedWithUserId(task.getId(), userId)
        .map(share -> share.getPermissionLevel() == PermissionLevel.EDIT)
        .orElse(false);
  }

  public Page<TaskResponseDTO> searchTasks(
      Long userId, String searchTerm, Boolean isCompleted, Pageable pageable) {
    logger.debug(
        "Searching tasks for user ID: {} with term: '{}', completed: {}",
        userId,
        searchTerm,
        isCompleted);

    Page<Task> tasks;

    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      if (isCompleted != null) {
        tasks =
            taskRepository.searchByUserIdAndDescriptionAndIsCompleted(
                userId, searchTerm.trim(), isCompleted, pageable);
      } else {
        tasks = taskRepository.searchByUserIdAndDescription(userId, searchTerm.trim(), pageable);
      }
    } else if (isCompleted != null) {
      tasks = taskRepository.findByUserIdAndIsCompleted(userId, isCompleted, pageable);
    } else {
      tasks = taskRepository.findByUserId(userId, pageable);
    }

    logger.debug("Search returned {} tasks", tasks.getTotalElements());
    return tasks.map(taskMapper::toResponseDTO);
  }

  public Page<TaskResponseDTO> getTasksWithFilters(
      Long userId, Long categoryId, List<Long> tagIds, Pageable pageable) {
    logger.debug(
        "Fetching tasks for user ID: {} with filters - categoryId: {}, tagIds: {}",
        userId,
        categoryId,
        tagIds);

    Page<Task> tasks;

    if (categoryId != null && tagIds != null && !tagIds.isEmpty()) {
      tasks = taskRepository.findByUserIdWithFilters(userId, categoryId, tagIds, pageable);
    } else if (categoryId != null) {
      tasks = taskRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
    } else if (tagIds != null && !tagIds.isEmpty()) {
      tasks = taskRepository.findByUserIdAndTagIdsIn(userId, tagIds, pageable);
    } else {
      tasks = taskRepository.findByUserId(userId, pageable);
    }

    logger.debug("Filter returned {} tasks", tasks.getTotalElements());
    return tasks.map(taskMapper::toResponseDTO);
  }

  public long getTaskCount(Long userId, Boolean isCompleted) {
    if (isCompleted != null) {
      return taskRepository.countByUserIdAndIsCompleted(userId, isCompleted);
    }
    return taskRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
  }

  public TaskResponseDTO toggleCompletion(Long taskId, Long userId) {
    logger.debug("Toggling completion for task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!hasEditPermission(task, userId)) {
      throw new IllegalArgumentException("User does not have edit permission for this task");
    }

    if (task.getIsCompleted()) {
      task.markIncomplete();
      logger.info("Task ID: {} marked as incomplete", taskId);
    } else {
      task.markComplete();
      logger.info("Task ID: {} marked as complete", taskId);
    }

    Task savedTask = taskRepository.save(task);

    // Broadcast WebSocket update to owner and collaborators
    TaskUpdateMessage message =
        TaskUpdateMessage.completed(savedTask.getId(), userId, savedTask.getIsCompleted());
    broadcastToTaskCollaborators(savedTask, message);

    return taskMapper.toResponseDTO(savedTask);
  }

  public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO updateDTO, Long userId) {
    logger.debug("Updating task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!hasEditPermission(task, userId)) {
      throw new IllegalArgumentException("User does not have edit permission for this task");
    }

    if (updateDTO.getDescription() != null) {
      task.setDescription(updateDTO.getDescription());
    }

    if (updateDTO.getPriority() != null) {
      task.setPriority(updateDTO.getPriority());
    }

    if (updateDTO.getDueDate() != null) {
      task.setDueDate(updateDTO.getDueDate());
    }

    if (updateDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findByIdAndUserId(updateDTO.getCategoryId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category not found with ID: " + updateDTO.getCategoryId()));
      task.setCategory(category);
    }

    if (updateDTO.getTagIds() != null) {
      if (updateDTO.getTagIds().isEmpty()) {
        task.setTags(Collections.emptyList());
      } else {
        List<Tag> tags = tagRepository.findByIdInAndUserId(updateDTO.getTagIds(), userId);
        if (tags.size() != updateDTO.getTagIds().size()) {
          throw new ResourceNotFoundException("One or more tags not found");
        }
        task.setTags(tags);
      }
    }

    if (updateDTO.getEstimatedDurationMinutes() != null) {
      task.setEstimatedDurationMinutes(updateDTO.getEstimatedDurationMinutes());
    }

    Task savedTask = taskRepository.save(task);
    logger.info("Task ID: {} updated successfully", taskId);

    // Broadcast WebSocket update to owner and collaborators
    TaskUpdateMessage message =
        TaskUpdateMessage.updated(
            savedTask.getId(),
            userId,
            savedTask.getDescription(),
            savedTask.getIsCompleted(),
            savedTask.getPriority(),
            savedTask.getDueDate() != null
                ? savedTask.getDueDate().atZone(ZoneId.systemDefault()).toInstant()
                : null);
    broadcastToTaskCollaborators(savedTask, message);

    return taskMapper.toResponseDTO(savedTask);
  }

  public void deleteTask(Long taskId, Long userId) {
    logger.debug("Deleting task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Only the task owner can delete the task");
    }

    if (task.hasSubtasks()) {
      int subtaskCount = task.getSubtasks().size();
      logger.info(
          "Task ID: {} has {} subtasks that will be deleted due to cascade", taskId, subtaskCount);
    }

    // Broadcast WebSocket update before deletion
    TaskUpdateMessage message = TaskUpdateMessage.deleted(taskId, userId);
    broadcastToTaskCollaborators(task, message);

    taskRepository.delete(task);
    logger.info("Task ID: {} deleted successfully", taskId);
  }

  public boolean taskHasSubtasks(Long taskId, Long userId) {
    logger.debug("Checking if task ID: {} has subtasks", taskId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    return task.hasSubtasks();
  }

  public List<Task> getSharedTasksForUser(Long userId) {
    logger.debug("Fetching shared tasks for user ID: {}", userId);
    List<TaskShare> shares = taskShareRepository.findBySharedWithUserId(userId);
    return shares.stream().map(TaskShare::getTask).collect(java.util.stream.Collectors.toList());
  }

  public TaskResponseDTO createSubtask(Long parentTaskId, TaskCreateDTO createDTO, Long userId) {
    logger.debug("Creating subtask for parent task ID: {} by user ID: {}", parentTaskId, userId);

    if (createDTO.getDescription() == null || createDTO.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException("Description cannot be empty");
    }

    Task parentTask =
        taskRepository
            .findById(parentTaskId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Parent task not found with ID: " + parentTaskId));

    if (!hasEditPermission(parentTask, userId)) {
      throw new IllegalArgumentException("User does not have edit permission for the parent task");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    Task subtask = taskMapper.toEntity(createDTO, user);

    // Handle category assignment
    if (createDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findByIdAndUserId(createDTO.getCategoryId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category not found with ID: " + createDTO.getCategoryId()));
      subtask.setCategory(category);
    }

    // Handle tag assignments
    if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
      List<Tag> tags = tagRepository.findByIdInAndUserId(createDTO.getTagIds(), userId);
      if (tags.size() != createDTO.getTagIds().size()) {
        throw new ResourceNotFoundException("One or more tags not found");
      }
      subtask.setTags(tags);
    }

    // Set parent task - this will validate depth and set depth field
    subtask.setParentTask(parentTask);

    Task savedSubtask = taskRepository.save(subtask);

    logger.info(
        "Subtask created with ID: {} for parent task ID: {} at depth: {}",
        savedSubtask.getId(),
        parentTaskId,
        savedSubtask.getDepth());
    return taskMapper.toResponseDTO(savedSubtask);
  }

  public List<TaskResponseDTO> getSubtasks(Long parentTaskId, Long userId) {
    logger.debug("Fetching subtasks for parent task ID: {} by user ID: {}", parentTaskId, userId);

    Task parentTask =
        taskRepository
            .findById(parentTaskId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Parent task not found with ID: " + parentTaskId));

    if (!hasTaskAccess(parentTask, userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    List<Task> subtasks = taskRepository.findByParentTaskId(parentTaskId);

    logger.debug("Found {} subtasks for parent task ID: {}", subtasks.size(), parentTaskId);
    return subtasks.stream()
        .map(taskMapper::toResponseDTO)
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Get the recurrence pattern for a task.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @return the recurrence pattern, or null if the task doesn't have one
   */
  public RecurrencePattern getRecurrencePattern(Long taskId, Long userId) {
    logger.debug("Fetching recurrence pattern for task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!hasTaskAccess(task, userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    return recurrencePatternRepository.findByTaskId(taskId).orElse(null);
  }

  /**
   * Batch complete multiple tasks.
   *
   * @param taskIds the list of task IDs to complete
   * @param userId the user ID
   */
  @org.springframework.transaction.annotation.Transactional
  public void batchComplete(List<Long> taskIds, Long userId) {
    logger.debug("Batch completing {} tasks for user ID: {}", taskIds.size(), userId);

    int completed = 0;
    for (Long taskId : taskIds) {
      try {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getUser().getId().equals(userId)) {
          if (!task.getIsCompleted()) {
            task.markComplete();
            taskRepository.save(task);
            completed++;
          }
        }
      } catch (Exception e) {
        logger.warn("Failed to complete task ID: {} - {}", taskId, e.getMessage());
      }
    }

    logger.info("Batch completed {} tasks for user ID: {}", completed, userId);
  }

  /**
   * Batch delete multiple tasks.
   *
   * @param taskIds the list of task IDs to delete
   * @param userId the user ID
   */
  @org.springframework.transaction.annotation.Transactional
  public void batchDelete(List<Long> taskIds, Long userId) {
    logger.debug("Batch deleting {} tasks for user ID: {}", taskIds.size(), userId);

    int deleted = 0;
    for (Long taskId : taskIds) {
      try {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getUser().getId().equals(userId)) {
          taskRepository.delete(task);
          deleted++;
        }
      } catch (Exception e) {
        logger.warn("Failed to delete task ID: {} - {}", taskId, e.getMessage());
      }
    }

    logger.info("Batch deleted {} tasks for user ID: {}", deleted, userId);
  }

  /**
   * Batch assign category to multiple tasks.
   *
   * @param taskIds the list of task IDs
   * @param categoryId the category ID to assign
   * @param userId the user ID
   */
  @org.springframework.transaction.annotation.Transactional
  public void batchUpdateCategory(List<Long> taskIds, Long categoryId, Long userId) {
    logger.debug(
        "Batch updating category to {} for {} tasks by user ID: {}",
        categoryId,
        taskIds.size(),
        userId);

    Category category = null;
    if (categoryId != null) {
      category =
          categoryRepository
              .findByIdAndUserId(categoryId, userId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    int updated = 0;
    for (Long taskId : taskIds) {
      try {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getUser().getId().equals(userId)) {
          task.setCategory(category);
          taskRepository.save(task);
          updated++;
        }
      } catch (Exception e) {
        logger.warn("Failed to update category for task ID: {} - {}", taskId, e.getMessage());
      }
    }

    logger.info("Batch updated category for {} tasks by user ID: {}", updated, userId);
  }

  /**
   * Batch assign tags to multiple tasks.
   *
   * @param taskIds the list of task IDs
   * @param tagIds the list of tag IDs to assign
   * @param userId the user ID
   */
  @org.springframework.transaction.annotation.Transactional
  public void batchUpdateTags(List<Long> taskIds, List<Long> tagIds, Long userId) {
    logger.debug("Batch updating tags for {} tasks by user ID: {}", taskIds.size(), userId);

    List<Tag> tags = Collections.emptyList();
    if (tagIds != null && !tagIds.isEmpty()) {
      tags = tagRepository.findByIdInAndUserId(tagIds, userId);
      if (tags.size() != tagIds.size()) {
        throw new ResourceNotFoundException("One or more tags not found");
      }
    }

    int updated = 0;
    for (Long taskId : taskIds) {
      try {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null && task.getUser().getId().equals(userId)) {
          task.setTags(tags);
          taskRepository.save(task);
          updated++;
        }
      } catch (Exception e) {
        logger.warn("Failed to update tags for task ID: {} - {}", taskId, e.getMessage());
      }
    }

    logger.info("Batch updated tags for {} tasks by user ID: {}", updated, userId);
  }

  /**
   * Broadcast WebSocket message to task owner and all collaborators
   *
   * @param task the task
   * @param message the WebSocket message to broadcast
   */
  private void broadcastToTaskCollaborators(Task task, TaskUpdateMessage message) {
    try {
      // Send to task owner
      webSocketHandler.sendTaskUpdateToUser(task.getUser().getId(), message);

      // Send to all collaborators (users with shared access)
      List<TaskShare> shares = taskShareRepository.findByTaskId(task.getId());
      List<Long> collaboratorIds =
          shares.stream().map(share -> share.getSharedWithUser().getId()).toList();

      if (!collaboratorIds.isEmpty()) {
        webSocketHandler.sendTaskUpdateToUsers(collaboratorIds, message);
        logger.debug(
            "Broadcasted task update to {} collaborators for task ID: {}",
            collaboratorIds.size(),
            task.getId());
      }
    } catch (Exception e) {
      logger.error(
          "Failed to broadcast WebSocket message for task ID: {} - {}",
          task.getId(),
          e.getMessage());
    }
  }
}
