package com.todoapp.application.service;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
import com.todoapp.domain.repository.TagRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskService {

  private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final TaskMapper taskMapper;

  public TaskService(
      TaskRepository taskRepository,
      UserRepository userRepository,
      CategoryRepository categoryRepository,
      TagRepository tagRepository,
      TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.tagRepository = tagRepository;
    this.taskMapper = taskMapper;
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

    if (!task.getUser().getId().equals(userId)) {
      throw new ResourceNotFoundException("Task not found with ID: " + taskId);
    }

    return taskMapper.toResponseDTO(task);
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

    if (!task.getUser().getId().equals(userId)) {
      throw new ResourceNotFoundException("Task not found with ID: " + taskId);
    }

    if (task.getIsCompleted()) {
      task.markIncomplete();
      logger.info("Task ID: {} marked as incomplete", taskId);
    } else {
      task.markComplete();
      logger.info("Task ID: {} marked as complete", taskId);
    }

    Task savedTask = taskRepository.save(task);
    return taskMapper.toResponseDTO(savedTask);
  }

  public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO updateDTO, Long userId) {
    logger.debug("Updating task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new ResourceNotFoundException("Task not found with ID: " + taskId);
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
    return taskMapper.toResponseDTO(savedTask);
  }

  public void deleteTask(Long taskId, Long userId) {
    logger.debug("Deleting task ID: {} by user ID: {}", taskId, userId);

    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new ResourceNotFoundException("Task not found with ID: " + taskId);
    }

    taskRepository.delete(task);
    logger.info("Task ID: {} deleted successfully", taskId);
  }
}
