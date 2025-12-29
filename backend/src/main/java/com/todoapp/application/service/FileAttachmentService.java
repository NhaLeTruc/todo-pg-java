package com.todoapp.application.service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.todoapp.application.dto.FileAttachmentDTO;
import com.todoapp.domain.model.FileAttachment;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.model.VirusScanStatus;
import com.todoapp.domain.repository.FileAttachmentRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.infrastructure.messaging.VirusScanService;
import com.todoapp.infrastructure.storage.FileStorageService;

@Service
public class FileAttachmentService {

  private static final Logger logger = LoggerFactory.getLogger(FileAttachmentService.class);

  private final FileAttachmentRepository fileAttachmentRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;
  private final VirusScanService virusScanService;

  // User storage quota: 1GB
  private static final long USER_STORAGE_QUOTA = 1024L * 1024L * 1024L; // 1GB in bytes

  public FileAttachmentService(
      FileAttachmentRepository fileAttachmentRepository,
      TaskRepository taskRepository,
      UserRepository userRepository,
      FileStorageService fileStorageService,
      VirusScanService virusScanService) {
    this.fileAttachmentRepository = fileAttachmentRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.fileStorageService = fileStorageService;
    this.virusScanService = virusScanService;
  }

  /**
   * Upload a file attachment for a task.
   *
   * @param taskId The ID of the task
   * @param userId The ID of the user uploading the file
   * @param file The multipart file to upload
   * @return The created file attachment DTO
   */
  @Transactional
  public FileAttachmentDTO uploadFile(Long taskId, Long userId, MultipartFile file) {
    // Validate inputs
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Fetch task and user
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Verify user has access to the task
    verifyUserAccessToTask(task, user);

    // Check user storage quota
    checkUserStorageQuota(userId, file.getSize());

    // Upload file to storage
    String storageKey;
    try {
      InputStream inputStream = file.getInputStream();
      storageKey =
          fileStorageService.uploadFile(
              file.getOriginalFilename(), inputStream, file.getContentType(), file.getSize());
    } catch (Exception e) {
      logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
      throw new RuntimeException("Failed to upload file", e);
    }

    // Create file attachment entity
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName(file.getOriginalFilename());
    attachment.setFileSize(file.getSize());
    attachment.setMimeType(file.getContentType());
    attachment.setStorageKey(storageKey);
    attachment.setScanStatus(VirusScanStatus.PENDING);

    // Save to database
    attachment = fileAttachmentRepository.save(attachment);

    // Queue for virus scanning
    virusScanService.queueForScanning(attachment.getId());

    logger.info(
        "File attachment created: {} for task: {} by user: {}", attachment.getId(), taskId, userId);

    return toDTO(attachment);
  }

  /**
   * Get all file attachments for a task.
   *
   * @param taskId The ID of the task
   * @param userId The ID of the requesting user
   * @return List of file attachment DTOs
   */
  @Transactional(readOnly = true)
  public List<FileAttachmentDTO> getAttachmentsForTask(Long taskId, Long userId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Verify user has access to the task
    verifyUserAccessToTask(task, user);

    List<FileAttachment> attachments = fileAttachmentRepository.findByTaskId(taskId);

    return attachments.stream().map(this::toDTO).collect(Collectors.toList());
  }

  /**
   * Download a file attachment.
   *
   * @param attachmentId The ID of the attachment
   * @param userId The ID of the requesting user
   * @return InputStream of the file content
   */
  @Transactional(readOnly = true)
  public InputStream downloadFile(UUID attachmentId, Long userId) {
    FileAttachment attachment =
        fileAttachmentRepository
            .findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("File attachment not found: " + attachmentId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Verify user has access to the attachment
    verifyUserAccessToTask(attachment.getTask(), user);

    // Check if file is safe to download
    if (!attachment.isDownloadable()) {
      throw new RuntimeException(
          "File is not safe to download. Scan status: " + attachment.getScanStatus());
    }

    // Download from storage
    return fileStorageService.downloadFile(attachment.getStorageKey());
  }

  /**
   * Delete a file attachment.
   *
   * @param attachmentId The ID of the attachment
   * @param userId The ID of the requesting user
   */
  @Transactional
  public void deleteFile(UUID attachmentId, Long userId) {
    FileAttachment attachment =
        fileAttachmentRepository
            .findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("File attachment not found: " + attachmentId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Verify user has access to the attachment
    verifyUserAccessToTask(attachment.getTask(), user);

    // Delete from storage
    try {
      fileStorageService.deleteFile(attachment.getStorageKey());
    } catch (Exception e) {
      logger.error("Failed to delete file from storage: {}", attachment.getStorageKey(), e);
      // Continue with database deletion even if storage deletion fails
    }

    // Delete from database
    fileAttachmentRepository.delete(attachment);

    logger.info("File attachment deleted: {} by user: {}", attachmentId, userId);
  }

  /**
   * Get a single file attachment by ID.
   *
   * @param attachmentId The ID of the attachment
   * @param userId The ID of the requesting user
   * @return The file attachment DTO
   */
  @Transactional(readOnly = true)
  public FileAttachmentDTO getAttachment(UUID attachmentId, Long userId) {
    FileAttachment attachment =
        fileAttachmentRepository
            .findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("File attachment not found: " + attachmentId));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    // Verify user has access to the attachment
    verifyUserAccessToTask(attachment.getTask(), user);

    return toDTO(attachment);
  }

  /**
   * Verify that a user has access to a task (either owns it or has it shared with them).
   *
   * @param task The task to check
   * @param user The user requesting access
   * @throws AccessDeniedException if user doesn't have access
   */
  private void verifyUserAccessToTask(Task task, User user) {
    // User owns the task
    if (task.getUser().getId().equals(user.getId())) {
      return;
    }

    // Check if task is shared with user (if sharing is implemented)
    // For now, only owner can access
    throw new AccessDeniedException("User does not have access to this task");
  }

  /**
   * Check if user has exceeded their storage quota.
   *
   * @param userId The user ID
   * @param additionalSize The size of the file being uploaded
   * @throws RuntimeException if quota exceeded
   */
  private void checkUserStorageQuota(Long userId, long additionalSize) {
    long currentUsage = fileAttachmentRepository.calculateTotalFileSizeByUserId(userId);
    long newUsage = currentUsage + additionalSize;

    if (newUsage > USER_STORAGE_QUOTA) {
      throw new RuntimeException(
          "User storage quota exceeded. Current usage: "
              + (currentUsage / (1024 * 1024))
              + "MB, Quota: "
              + (USER_STORAGE_QUOTA / (1024 * 1024))
              + "MB");
    }
  }

  /**
   * Convert FileAttachment entity to DTO.
   *
   * @param attachment The entity
   * @return The DTO
   */
  private FileAttachmentDTO toDTO(FileAttachment attachment) {
    FileAttachmentDTO dto = new FileAttachmentDTO();
    dto.setId(attachment.getId());
    dto.setTaskId(attachment.getTask().getId());
    dto.setUserId(attachment.getUser().getId());
    dto.setFileName(attachment.getFileName());
    dto.setFileSize(attachment.getFileSize());
    dto.setMimeType(attachment.getMimeType());
    dto.setStorageKey(attachment.getStorageKey());
    dto.setScanStatus(attachment.getScanStatus());
    dto.setCreatedAt(attachment.getCreatedAt());
    dto.setScannedAt(attachment.getScannedAt());
    dto.setDownloadable(attachment.isDownloadable());
    return dto;
  }
}
