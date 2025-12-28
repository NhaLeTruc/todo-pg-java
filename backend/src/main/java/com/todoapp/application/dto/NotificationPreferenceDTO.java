package com.todoapp.application.dto;

import java.util.UUID;

import com.todoapp.domain.model.NotificationType;

public class NotificationPreferenceDTO {

  private UUID id;
  private Long userId;
  private NotificationType notificationType;
  private boolean inAppEnabled;
  private boolean emailEnabled;

  // Constructors

  public NotificationPreferenceDTO() {}

  public NotificationPreferenceDTO(
      UUID id,
      Long userId,
      NotificationType notificationType,
      boolean inAppEnabled,
      boolean emailEnabled) {
    this.id = id;
    this.userId = userId;
    this.notificationType = notificationType;
    this.inAppEnabled = inAppEnabled;
    this.emailEnabled = emailEnabled;
  }

  // Getters and Setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  public boolean isInAppEnabled() {
    return inAppEnabled;
  }

  public void setInAppEnabled(boolean inAppEnabled) {
    this.inAppEnabled = inAppEnabled;
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public void setEmailEnabled(boolean emailEnabled) {
    this.emailEnabled = emailEnabled;
  }
}
