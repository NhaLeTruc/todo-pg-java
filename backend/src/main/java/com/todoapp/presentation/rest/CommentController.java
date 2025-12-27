package com.todoapp.presentation.rest;

import com.todoapp.application.dto.CommentDTO;
import com.todoapp.application.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comments", description = "Task comment management endpoints")
public class CommentController {

  private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/tasks/{taskId}/comments")
  @Operation(summary = "Add comment to task", description = "Adds a new comment to a task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  public ResponseEntity<CommentDTO> addComment(
      @PathVariable Long taskId,
      @Valid @RequestBody CommentDTO commentDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Adding comment to task ID: {} by user ID: {}", taskId, userId);
    CommentDTO createdComment = commentService.addComment(taskId, commentDTO, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
  }

  @GetMapping("/tasks/{taskId}/comments")
  @Operation(
      summary = "Get task comments",
      description = "Retrieves all comments for a specific task")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  public ResponseEntity<List<CommentDTO>> getTaskComments(
      @PathVariable Long taskId,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Fetching comments for task ID: {} by user ID: {}", taskId, userId);
    List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId, userId);
    return ResponseEntity.ok(comments);
  }

  @PutMapping("/comments/{id}")
  @Operation(summary = "Update comment", description = "Updates an existing comment")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comment updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not comment author"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
      })
  public ResponseEntity<CommentDTO> updateComment(
      @PathVariable Long id,
      @Valid @RequestBody CommentDTO commentDTO,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Updating comment ID: {} by user ID: {}", id, userId);
    CommentDTO updatedComment = commentService.updateComment(id, commentDTO, userId);
    return ResponseEntity.ok(updatedComment);
  }

  @DeleteMapping("/comments/{id}")
  @Operation(summary = "Delete comment", description = "Deletes a comment")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not comment author"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
      })
  public ResponseEntity<Void> deleteComment(
      @PathVariable Long id,
      @Parameter(description = "User ID (temporary - will be from JWT)")
          @RequestHeader(value = "X-User-Id", defaultValue = "1")
          Long userId) {
    logger.info("Deleting comment ID: {} by user ID: {}", id, userId);
    commentService.deleteComment(id, userId);
    return ResponseEntity.noContent().build();
  }
}
