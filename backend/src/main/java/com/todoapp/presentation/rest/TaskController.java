package com.todoapp.presentation.rest;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.service.TaskService;
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

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
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
          Boolean completed) {
    logger.info(
        "Fetching tasks for user ID: {} (page: {}, size: {}, search: {}, completed: {})",
        userId,
        page,
        size,
        search,
        completed);

    Sort sort =
        Sort.by(
            Sort.Direction.fromString(sortDirection), sortBy != null ? sortBy : "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<TaskResponseDTO> tasks;
    if (search != null || completed != null) {
      tasks = taskService.searchTasks(userId, search, completed, pageable);
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
}
