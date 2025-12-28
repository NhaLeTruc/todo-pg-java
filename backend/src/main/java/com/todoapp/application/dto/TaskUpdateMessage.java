package com.todoapp.application.dto;

import java.time.Instant;

import com.todoapp.domain.model.Priority;

public record TaskUpdateMessage(
    Long taskId,
    String action, // CREATED, UPDATED, DELETED, COMPLETED, SHARED
    Long userId,
    String description,
    Boolean isCompleted,
    Priority priority,
    Instant dueDate,
    Instant timestamp) {

  public static TaskUpdateMessage created(Long taskId, Long userId, String description) {
    return new TaskUpdateMessage(
        taskId, "CREATED", userId, description, false, null, null, Instant.now());
  }

  public static TaskUpdateMessage updated(
      Long taskId,
      Long userId,
      String description,
      Boolean isCompleted,
      Priority priority,
      Instant dueDate) {
    return new TaskUpdateMessage(
        taskId, "UPDATED", userId, description, isCompleted, priority, dueDate, Instant.now());
  }

  public static TaskUpdateMessage deleted(Long taskId, Long userId) {
    return new TaskUpdateMessage(taskId, "DELETED", userId, null, null, null, null, Instant.now());
  }

  public static TaskUpdateMessage completed(Long taskId, Long userId, Boolean isCompleted) {
    return new TaskUpdateMessage(
        taskId, "COMPLETED", userId, null, isCompleted, null, null, Instant.now());
  }

  public static TaskUpdateMessage shared(Long taskId, Long userId, Long sharedWithUserId) {
    return new TaskUpdateMessage(
        taskId, "SHARED", sharedWithUserId, null, null, null, null, Instant.now());
  }
}
