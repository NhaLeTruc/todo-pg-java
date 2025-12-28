package com.todoapp.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.todoapp.domain.model.NotificationType;

public class NotificationDTO {

  private UUID id;
  private UUID userId;
  private NotificationType type;
  private String message;
  private UUID relatedTaskId;
  private String relatedTaskDescription;
  private boolean isRead;
  private LocalDateTime createdAt;
  private LocalDateTime readAt;

  // Constructors

  public NotificationDTO() {}

  public NotificationDTO(
      UUID id,
      UUID userId,
      NotificationType type,
      String message,
      UUID relatedTaskId,
      String relatedTaskDescription,
      boolean isRead,
      LocalDateTime createdAt,
      LocalDateTime readAt) {
    this.id = id;
    this.userId = userId;
    this.type = type;
    this.message = message;
    this.relatedTaskId = relatedTaskId;
    this.relatedTaskDescription = relatedTaskDescription;
    this.isRead = isRead;
    this.createdAt = createdAt;
    this.readAt = readAt;
  }

  // Getters and Setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public UUID getRelatedTaskId() {
    return relatedTaskId;
  }

  public void setRelatedTaskId(UUID relatedTaskId) {
    this.relatedTaskId = relatedTaskId;
  }

  public String getRelatedTaskDescription() {
    return relatedTaskDescription;
  }

  public void setRelatedTaskDescription(String relatedTaskDescription) {
    this.relatedTaskDescription = relatedTaskDescription;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getReadAt() {
    return readAt;
  }

  public void setReadAt(LocalDateTime readAt) {
    this.readAt = readAt;
  }
}
