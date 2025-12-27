package com.todoapp.presentation.rest;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.application.service.TaskService;
import com.todoapp.domain.model.Task;
import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

  private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public TaskController(TaskService taskService, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @PostMapping
  @Operation(summary = "Create a new task", description = "Creates a new task for the current user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> createTask(
      @Valid @RequestBody TaskCreateDTO createDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Creating task for user ID: {}", userId);
    TaskResponseDTO createdTask = taskService.createTask(createDTO, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
  }

  @GetMapping
  @Operation(summary = "Get user tasks", description = "Retrieves paginated list of user's tasks")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Page<TaskResponseDTO>> getUserTasks(
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId,
      @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
      @Parameter(description = "Sort direction (asc/desc)")
          @RequestParam(defaultValue = "desc")
          String sortDirection,
      @Parameter(description = "Search term for filtering")
          @RequestParam(required = false)
          String search,
      @Parameter(description = "Filter by completion status")
          @RequestParam(required = false)
          Boolean completed,
      @Parameter(description = "Filter by category ID")
          @RequestParam(required = false)
          Long categoryId,
      @Parameter(description = "Filter by tag IDs (comma-separated)")
          @RequestParam(required = false)
          java.util.List<Long> tagIds) {
    logger.info(
        "Fetching tasks for user ID: {} (page: {}, size: {}, search: {}, completed: {}, categoryId: {}, tagIds: {})",
        userId,
        page,
        size,
        search,
        completed,
        categoryId,
        tagIds);

    Sort sort =
        Sort.by(
            Sort.Direction.fromString(sortDirection), sortBy != null ? sortBy : "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<TaskResponseDTO> tasks;
    if (search != null || completed != null) {
      tasks = taskService.searchTasks(userId, search, completed, pageable);
    } else if (categoryId != null || (tagIds != null && !tagIds.isEmpty())) {
      tasks = taskService.getTasksWithFilters(userId, categoryId, tagIds, pageable);
    } else {
      tasks = taskService.getUserTasks(userId, pageable);
    }

    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> getTaskById(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Fetching task ID: {} for user ID: {}", id, userId);
    TaskResponseDTO task = taskService.getTaskById(id, userId);
    return ResponseEntity.ok(task);
  }

  @GetMapping("/count")
  @Operation(summary = "Get task count", description = "Retrieves total task count for the user")
  @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  public ResponseEntity<Long> getTaskCount(
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId,
      @Parameter(description = "Filter by completion status")
          @RequestParam(required = false)
          Boolean completed) {
    logger.info("Getting task count for user ID: {} (completed: {})", userId, completed);
    long count = taskService.getTaskCount(userId, completed);
    return ResponseEntity.ok(count);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update task", description = "Updates an existing task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> updateTask(
      @PathVariable Long id,
      @Valid @RequestBody TaskUpdateDTO updateDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Updating task ID: {} for user ID: {}", id, userId);
    TaskResponseDTO task = taskService.updateTask(id, updateDTO, userId);
    return ResponseEntity.ok(task);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete task", description = "Deletes a task")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Void> deleteTask(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Deleting task ID: {} for user ID: {}", id, userId);
    taskService.deleteTask(id, userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/complete")
  @Operation(summary = "Mark task as complete", description = "Marks a task as completed")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task marked as complete",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> completeTask(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Marking task ID: {} as complete for user ID: {}", id, userId);
    TaskResponseDTO task = taskService.toggleCompletion(id, userId);
    return ResponseEntity.ok(task);
  }

  @PatchMapping("/{id}/uncomplete")
  @Operation(summary = "Mark task as incomplete", description = "Marks a task as incomplete")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task marked as incomplete",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> uncompleteTask(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Marking task ID: {} as incomplete for user ID: {}", id, userId);
    TaskResponseDTO task = taskService.toggleCompletion(id, userId);
    return ResponseEntity.ok(task);
  }

  @GetMapping("/shared-with-me")
  @Operation(
      summary = "Get shared tasks",
      description = "Get all tasks that have been shared with the current user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Shared tasks retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<TaskResponseDTO>> getSharedTasks(
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Fetching shared tasks for user ID: {}", userId);
    List<Task> sharedTasks = taskService.getSharedTasksForUser(userId);
    List<TaskResponseDTO> taskDTOs =
        sharedTasks.stream().map(taskMapper::toResponseDTO).collect(Collectors.toList());
    return ResponseEntity.ok(taskDTOs);
  }

  @GetMapping("/{id}/subtasks")
  @Operation(summary = "Get subtasks", description = "Retrieves all subtasks for a parent task")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Subtasks retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parent task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<TaskResponseDTO>> getSubtasks(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Fetching subtasks for parent task ID: {} by user ID: {}", id, userId);
    List<TaskResponseDTO> subtasks = taskService.getSubtasks(id, userId);
    return ResponseEntity.ok(subtasks);
  }

  @PostMapping("/{id}/subtasks")
  @Operation(
      summary = "Create subtask",
      description = "Creates a new subtask under the specified parent task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Subtask created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or max depth exceeded"),
        @ApiResponse(responseCode = "404", description = "Parent task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<TaskResponseDTO> createSubtask(
      @PathVariable Long id,
      @Valid @RequestBody TaskCreateDTO createDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Creating subtask for parent task ID: {} by user ID: {}", id, userId);
    TaskResponseDTO createdSubtask = taskService.createSubtask(id, createDTO, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdSubtask);
  }

  @GetMapping("/{id}/has-subtasks")
  @Operation(
      summary = "Check if task has subtasks",
      description = "Returns whether a task has any subtasks (used for delete confirmation)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Boolean> taskHasSubtasks(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Checking if task ID: {} has subtasks", id);
    boolean hasSubtasks = taskService.taskHasSubtasks(id, userId);
    return ResponseEntity.ok(hasSubtasks);
  }
}
