package com.todoapp.domain.model;

public enum NotificationType {
  TASK_DUE_SOON, // Task is due within 24 hours
  TASK_OVERDUE, // Task is past due date
  TASK_SHARED, // Task was shared with user
  TASK_COMMENTED, // New comment on task
  TASK_MENTIONED, // User was mentioned in comment
  TASK_ASSIGNED, // Task was assigned to user
  REMINDER // General reminder
}
