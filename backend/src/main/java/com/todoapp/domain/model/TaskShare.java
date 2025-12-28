package com.todoapp.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "task_shares",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"task_id", "shared_with_user_id"})})
public class TaskShare {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  @NotNull(message = "Task is required")
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shared_with_user_id", nullable = false)
  @NotNull(message = "Shared with user is required")
  private User sharedWithUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shared_by_user_id", nullable = false)
  @NotNull(message = "Shared by user is required")
  private User sharedByUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission_level", nullable = false, length = 10)
  @NotNull(message = "Permission level is required")
  private PermissionLevel permissionLevel;

  @CreationTimestamp
  @Column(name = "shared_at", nullable = false, updatable = false)
  private LocalDateTime sharedAt;

  public TaskShare() {
    this.sharedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public User getSharedWithUser() {
    return sharedWithUser;
  }

  public void setSharedWithUser(User sharedWithUser) {
    this.sharedWithUser = sharedWithUser;
  }

  public User getSharedByUser() {
    return sharedByUser;
  }

  public void setSharedByUser(User sharedByUser) {
    this.sharedByUser = sharedByUser;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TaskShare)) return false;
    TaskShare taskShare = (TaskShare) o;
    return id != null && id.equals(taskShare.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "TaskShare{"
        + "id="
        + id
        + ", taskId="
        + (task != null ? task.getId() : null)
        + ", sharedWithUserId="
        + (sharedWithUser != null ? sharedWithUser.getId() : null)
        + ", permissionLevel="
        + permissionLevel
        + ", sharedAt="
        + sharedAt
        + '}';
  }
}
