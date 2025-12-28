package com.todoapp.presentation.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.todoapp.application.dto.TaskShareDTO;
import com.todoapp.application.service.TaskShareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Sharing", description = "Task sharing and collaboration endpoints")
public class TaskShareController {

  private static final Logger logger = LoggerFactory.getLogger(TaskShareController.class);

  private final TaskShareService taskShareService;

  public TaskShareController(TaskShareService taskShareService) {
    this.taskShareService = taskShareService;
  }

  @PostMapping("/{taskId}/share")
  @Operation(summary = "Share task with user", description = "Share a task with another user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task shared successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskShareDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Task or user not found")
      })
  public ResponseEntity<TaskShareDTO> shareTask(
      @PathVariable Long taskId,
      @Valid @RequestBody TaskShareDTO shareDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Sharing task ID: {} by user ID: {}", taskId, userId);
    TaskShareDTO createdShare = taskShareService.shareTask(taskId, shareDTO, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdShare);
  }

  @DeleteMapping("/{taskId}/share/{sharedWithUserId}")
  @Operation(summary = "Revoke task share", description = "Revoke task access from a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Share revoked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not task owner"),
        @ApiResponse(responseCode = "404", description = "Task or share not found")
      })
  public ResponseEntity<Void> revokeShare(
      @PathVariable Long taskId,
      @PathVariable Long sharedWithUserId,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info(
        "Revoking share for task ID: {} from user ID: {} by user ID: {}",
        taskId,
        sharedWithUserId,
        userId);
    taskShareService.revokeShare(taskId, sharedWithUserId, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{taskId}/shares")
  @Operation(summary = "Get task shares", description = "Get all shares for a specific task")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Shares retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  public ResponseEntity<List<TaskShareDTO>> getTaskShares(
      @PathVariable Long taskId,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Fetching shares for task ID: {}", taskId);
    List<TaskShareDTO> shares = taskShareService.getTaskShares(taskId);
    return ResponseEntity.ok(shares);
  }
}
