package com.todoapp.application.service;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;
import jakarta.transaction.Transactional;
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
  private final TaskMapper taskMapper;

  public TaskService(
      TaskRepository taskRepository, UserRepository userRepository, TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
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
}
