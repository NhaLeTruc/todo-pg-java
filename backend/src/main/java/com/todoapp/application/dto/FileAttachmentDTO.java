package com.todoapp.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todoapp.domain.model.VirusScanStatus;

public class FileAttachmentDTO {

  private UUID id;
  private Long taskId;
  private Long userId;
  private String fileName;
  private Long fileSize;
  private String mimeType;
  private String storageKey;
  private VirusScanStatus scanStatus;
  private LocalDateTime createdAt;
  private LocalDateTime scannedAt;
  private boolean downloadable;

  // Constructors

  public FileAttachmentDTO() {}

  public FileAttachmentDTO(
      UUID id,
      Long taskId,
      Long userId,
      String fileName,
      Long fileSize,
      String mimeType,
      String storageKey,
      VirusScanStatus scanStatus,
      LocalDateTime createdAt,
      LocalDateTime scannedAt,
      boolean downloadable) {
    this.id = id;
    this.taskId = taskId;
    this.userId = userId;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.storageKey = storageKey;
    this.scanStatus = scanStatus;
    this.createdAt = createdAt;
    this.scannedAt = scannedAt;
    this.downloadable = downloadable;
  }

  // Getters and Setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
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
    this.storageKey = storageKey;
  }

  public VirusScanStatus getScanStatus() {
    return scanStatus;
  }

  public void setScanStatus(VirusScanStatus scanStatus) {
    this.scanStatus = scanStatus;
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

  public boolean isDownloadable() {
    return downloadable;
  }

  public void setDownloadable(boolean downloadable) {
    this.downloadable = downloadable;
  }
}
