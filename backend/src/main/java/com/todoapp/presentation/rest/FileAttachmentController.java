package com.todoapp.presentation.rest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.todoapp.application.dto.FileAttachmentDTO;
import com.todoapp.application.service.FileAttachmentService;
import com.todoapp.infrastructure.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "File Attachments", description = "Endpoints for managing file attachments")
public class FileAttachmentController {

  private static final Logger logger = LoggerFactory.getLogger(FileAttachmentController.class);

  private final FileAttachmentService fileAttachmentService;

  public FileAttachmentController(FileAttachmentService fileAttachmentService) {
    this.fileAttachmentService = fileAttachmentService;
  }

  /**
   * Upload a file attachment for a task.
   *
   * @param taskId The ID of the task
   * @param file The file to upload
   * @param currentUser The authenticated user
   * @return The created file attachment
   */
  @PostMapping(
      value = "/tasks/{taskId}/attachments",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload file attachment",
      description = "Upload a file attachment for a specific task")
  @ApiResponse(
      responseCode = "201",
      description = "File attachment created successfully",
      content = @Content(schema = @Schema(implementation = FileAttachmentDTO.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request or file too large")
  @ApiResponse(responseCode = "403", description = "User does not have access to this task")
  @ApiResponse(responseCode = "404", description = "Task not found")
  public ResponseEntity<FileAttachmentDTO> uploadAttachment(
      @Parameter(description = "ID of the task") @PathVariable Long taskId,
      @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    logger.info(
        "Uploading file attachment for task: {} by user: {}", taskId, currentUser.getUserId());

    FileAttachmentDTO attachment =
        fileAttachmentService.uploadFile(taskId, currentUser.getUserId(), file);

    return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
  }

  /**
   * Get all file attachments for a task.
   *
   * @param taskId The ID of the task
   * @param currentUser The authenticated user
   * @return List of file attachments
   */
  @GetMapping("/tasks/{taskId}/attachments")
  @Operation(
      summary = "Get task attachments",
      description = "Get all file attachments for a specific task")
  @ApiResponse(
      responseCode = "200",
      description = "List of file attachments",
      content = @Content(schema = @Schema(implementation = FileAttachmentDTO.class)))
  @ApiResponse(responseCode = "403", description = "User does not have access to this task")
  @ApiResponse(responseCode = "404", description = "Task not found")
  public ResponseEntity<List<FileAttachmentDTO>> getTaskAttachments(
      @Parameter(description = "ID of the task") @PathVariable Long taskId,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    logger.info("Getting attachments for task: {} by user: {}", taskId, currentUser.getUserId());

    List<FileAttachmentDTO> attachments =
        fileAttachmentService.getAttachmentsForTask(taskId, currentUser.getUserId());

    return ResponseEntity.ok(attachments);
  }

  /**
   * Download a file attachment.
   *
   * @param id The ID of the attachment
   * @param currentUser The authenticated user
   * @return The file content as a resource
   */
  @GetMapping("/attachments/{id}/download")
  @Operation(
      summary = "Download file attachment",
      description = "Download a file attachment by its ID")
  @ApiResponse(
      responseCode = "200",
      description = "File downloaded successfully",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
  @ApiResponse(responseCode = "403", description = "User does not have access to this attachment")
  @ApiResponse(responseCode = "404", description = "Attachment not found")
  @ApiResponse(
      responseCode = "409",
      description = "File is not safe to download (virus scan pending or infected)")
  public ResponseEntity<Resource> downloadAttachment(
      @Parameter(description = "ID of the attachment") @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    logger.info("Downloading attachment: {} by user: {}", id, currentUser.getUserId());

    // Get attachment metadata
    FileAttachmentDTO attachment = fileAttachmentService.getAttachment(id, currentUser.getUserId());

    // Download file stream
    InputStream fileStream = fileAttachmentService.downloadFile(id, currentUser.getUserId());
    Resource resource = new InputStreamResource(fileStream);

    // Set headers for file download
    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + attachment.getFileName() + "\"");
    headers.add(HttpHeaders.CONTENT_TYPE, attachment.getMimeType());
    headers.add(HttpHeaders.CONTENT_LENGTH, attachment.getFileSize().toString());

    return ResponseEntity.ok().headers(headers).body(resource);
  }

  /**
   * Delete a file attachment.
   *
   * @param id The ID of the attachment
   * @param currentUser The authenticated user
   * @return No content response
   */
  @DeleteMapping("/attachments/{id}")
  @Operation(summary = "Delete file attachment", description = "Delete a file attachment by its ID")
  @ApiResponse(responseCode = "204", description = "File attachment deleted successfully")
  @ApiResponse(responseCode = "403", description = "User does not have access to this attachment")
  @ApiResponse(responseCode = "404", description = "Attachment not found")
  public ResponseEntity<Void> deleteAttachment(
      @Parameter(description = "ID of the attachment") @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    logger.info("Deleting attachment: {} by user: {}", id, currentUser.getUserId());

    fileAttachmentService.deleteFile(id, currentUser.getUserId());

    return ResponseEntity.noContent().build();
  }

  /**
   * Get a single file attachment by ID.
   *
   * @param id The ID of the attachment
   * @param currentUser The authenticated user
   * @return The file attachment metadata
   */
  @GetMapping("/attachments/{id}")
  @Operation(
      summary = "Get file attachment",
      description = "Get file attachment metadata by its ID")
  @ApiResponse(
      responseCode = "200",
      description = "File attachment metadata",
      content = @Content(schema = @Schema(implementation = FileAttachmentDTO.class)))
  @ApiResponse(responseCode = "403", description = "User does not have access to this attachment")
  @ApiResponse(responseCode = "404", description = "Attachment not found")
  public ResponseEntity<FileAttachmentDTO> getAttachment(
      @Parameter(description = "ID of the attachment") @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    logger.info("Getting attachment metadata: {} by user: {}", id, currentUser.getUserId());

    FileAttachmentDTO attachment = fileAttachmentService.getAttachment(id, currentUser.getUserId());

    return ResponseEntity.ok(attachment);
  }
}
