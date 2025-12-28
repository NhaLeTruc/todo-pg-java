package com.todoapp.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private NotificationType type;

  @Column(name = "message", nullable = false, length = 500)
  private String message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_task_id")
  private Task relatedTask;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  // Constructors

  public Notification() {}

  // Getters and Setters

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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
    if (message == null || message.trim().isEmpty()) {
      throw new IllegalArgumentException("Message cannot be empty");
    }
    this.message = message;
  }

  public Task getRelatedTask() {
    return relatedTask;
  }

  public void setRelatedTask(Task relatedTask) {
    this.relatedTask = relatedTask;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
    if (read && this.readAt == null) {
      this.readAt = LocalDateTime.now();
    }
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

  // Business Methods

  /** Mark this notification as read. */
  public void markAsRead() {
    this.isRead = true;
    if (this.readAt == null) {
      this.readAt = LocalDateTime.now();
    }
  }

  /**
   * Check if this notification is unread.
   *
   * @return true if unread, false otherwise
   */
  public boolean isUnread() {
    return !this.isRead;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification)) return false;
    Notification that = (Notification) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
