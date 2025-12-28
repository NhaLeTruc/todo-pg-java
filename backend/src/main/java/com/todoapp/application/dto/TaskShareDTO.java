package com.todoapp.application.dto;

import java.time.LocalDateTime;

import com.todoapp.domain.model.PermissionLevel;

import jakarta.validation.constraints.NotNull;

public class TaskShareDTO {

  private Long id;
  private Long taskId;
  private String taskDescription;

  @NotNull(message = "Shared with user ID is required")
  private Long sharedWithUserId;

  private String sharedWithUserEmail;
  private Long sharedByUserId;
  private String sharedByUserEmail;

  @NotNull(message = "Permission level is required")
  private PermissionLevel permissionLevel;

  private LocalDateTime sharedAt;

  public TaskShareDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  public Long getSharedWithUserId() {
    return sharedWithUserId;
  }

  public void setSharedWithUserId(Long sharedWithUserId) {
    this.sharedWithUserId = sharedWithUserId;
  }

  public String getSharedWithUserEmail() {
    return sharedWithUserEmail;
  }

  public void setSharedWithUserEmail(String sharedWithUserEmail) {
    this.sharedWithUserEmail = sharedWithUserEmail;
  }

  public Long getSharedByUserId() {
    return sharedByUserId;
  }

  public void setSharedByUserId(Long sharedByUserId) {
    this.sharedByUserId = sharedByUserId;
  }

  public String getSharedByUserEmail() {
    return sharedByUserEmail;
  }

  public void setSharedByUserEmail(String sharedByUserEmail) {
    this.sharedByUserEmail = sharedByUserEmail;
  }

  public PermissionLevel getPermissionLevel() {
    return permissionLevel;
  }

  public void setPermissionLevel(PermissionLevel permissionLevel) {
    this.permissionLevel = permissionLevel;
  }

  public LocalDateTime getSharedAt() {
    return sharedAt;
  }

  public void setSharedAt(LocalDateTime sharedAt) {
    this.sharedAt = sharedAt;
  }

  @Override
  public String toString() {
    return "TaskShareDTO{"
        + "id="
        + id
        + ", taskId="
        + taskId
        + ", sharedWithUserId="
        + sharedWithUserId
        + ", permissionLevel="
        + permissionLevel
        + ", sharedAt="
        + sharedAt
        + '}';
  }
}
