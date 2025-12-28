package com.todoapp.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "file_attachments")
public class FileAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "mime_type", nullable = false, length = 100)
  private String mimeType;

  @Column(name = "storage_key", nullable = false, unique = true, length = 500)
  private String storageKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "scan_status", nullable = false, length = 20)
  private VirusScanStatus scanStatus = VirusScanStatus.PENDING;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "scanned_at")
  private LocalDateTime scannedAt;

  // Maximum file size: 25MB
  private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

  // Constructors

  public FileAttachment() {}

  // Getters and Setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be null or empty");
    }
    // Sanitize file name to prevent path traversal attacks
    this.fileName = sanitizeFileName(fileName);
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    if (fileSize == null || fileSize <= 0) {
      throw new IllegalArgumentException("File size must be greater than 0");
    }
    if (fileSize > MAX_FILE_SIZE) {
      throw new IllegalArgumentException(
          "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
    }
    this.fileSize = fileSize;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String storageKey) {
    if (storageKey == null || storageKey.trim().isEmpty()) {
      throw new IllegalArgumentException("Storage key cannot be null or empty");
    }
    this.storageKey = storageKey;
  }

  public VirusScanStatus getScanStatus() {
    return scanStatus;
  }

  public void setScanStatus(VirusScanStatus scanStatus) {
    this.scanStatus = scanStatus;
    if (scanStatus != VirusScanStatus.PENDING && this.scannedAt == null) {
      this.scannedAt = LocalDateTime.now();
    }
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getScannedAt() {
    return scannedAt;
  }

  public void setScannedAt(LocalDateTime scannedAt) {
    this.scannedAt = scannedAt;
  }

  // Business Methods

  /**
   * Sanitizes the file name by removing potentially dangerous characters and path traversal
   * attempts.
   *
   * @param fileName The original file name
   * @return The sanitized file name
   */
  private String sanitizeFileName(String fileName) {
    if (fileName == null) {
      return null;
    }
    // Remove path separators and null bytes
    String sanitized = fileName.replaceAll("[/\\\\\\x00]", "");
    // Remove path traversal attempts
    sanitized = sanitized.replaceAll("\\.\\.", "");
    // Trim whitespace
    sanitized = sanitized.trim();
    return sanitized;
  }

  /**
   * Validates the file size against the maximum allowed size.
   *
   * @param size The file size to validate
   * @throws IllegalArgumentException if the file size exceeds the maximum
   */
  public void validateFileSize(long size) {
    if (size > MAX_FILE_SIZE) {
      throw new IllegalArgumentException(
          "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
    }
  }

  /**
   * Validates the file name to ensure it doesn't contain path traversal or invalid characters.
   *
   * @param fileName The file name to validate
   * @throws IllegalArgumentException if the file name is invalid
   */
  public void validateFilename(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be empty");
    }
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new IllegalArgumentException("Filename contains invalid characters");
    }
    if (fileName.matches(".*[<>:\"].*")) {
      throw new IllegalArgumentException("Filename contains invalid characters");
    }
  }

  /**
   * Generates a unique storage key for the file based on the task ID and file name.
   *
   * @return The generated storage key
   */
  public String generateStorageKey() {
    if (this.task == null || this.task.getId() == null) {
      throw new IllegalStateException("Task must be set before generating storage key");
    }
    if (this.fileName == null) {
      throw new IllegalStateException("File name must be set before generating storage key");
    }

    String extension = "";
    int dotIndex = this.fileName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < this.fileName.length() - 1) {
      extension = this.fileName.substring(dotIndex);
    }

    return String.format(
        "attachments/%s/%s%s", this.task.getId(), UUID.randomUUID(), extension);
  }

  /**
   * Checks if the file is safe to download based on its virus scan status.
   *
   * @return true if the file is downloadable, false otherwise
   */
  public boolean isDownloadable() {
    return this.scanStatus == VirusScanStatus.CLEAN
        || this.scanStatus == VirusScanStatus.SCAN_FAILED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FileAttachment)) return false;
    FileAttachment that = (FileAttachment) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
