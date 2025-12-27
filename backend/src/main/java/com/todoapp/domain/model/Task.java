package com.todoapp.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "tasks",
    indexes = {
      @Index(name = "idx_tasks_user_id", columnList = "user_id"),
      @Index(name = "idx_tasks_is_completed", columnList = "isCompleted"),
      @Index(name = "idx_tasks_priority", columnList = "priority"),
      @Index(name = "idx_tasks_due_date", columnList = "dueDate"),
      @Index(name = "idx_tasks_created_at", columnList = "createdAt"),
      @Index(name = "idx_tasks_user_completed", columnList = "user_id, isCompleted")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "User is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_task_id")
  private Task parentTask;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recurrence_pattern_id")
  private RecurrencePattern recurrencePattern;

  @NotBlank(message = "Description is required")
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_completed", nullable = false)
  private Boolean isCompleted = false;

  @NotNull(message = "Priority is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Priority priority = Priority.MEDIUM;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(nullable = false)
  private Integer position = 0;

  @Column(name = "estimated_duration_minutes")
  private Integer estimatedDurationMinutes;

  @Column(name = "actual_duration_minutes")
  private Integer actualDurationMinutes;

  @Min(0)
  @Max(5)
  @Column(nullable = false)
  private Integer depth = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "task_tags",
      joinColumns = @JoinColumn(name = "task_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (isCompleted == null) {
      isCompleted = false;
    }
    if (priority == null) {
      priority = Priority.MEDIUM;
    }
    if (position == null) {
      position = 0;
    }
    if (depth == null) {
      depth = 0;
    }
    validateDescription();
  }

  @PreUpdate
  protected void onUpdate() {
    validateDescription();
  }

  public void setDescription(String description) {
    this.description = description;
    validateDescription();
  }

  private void validateDescription() {
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Description cannot be empty");
    }
  }

  public boolean isOverdue() {
    if (dueDate == null || isCompleted) {
      return false;
    }
    return LocalDateTime.now().isAfter(dueDate);
  }

  public void markComplete() {
    this.isCompleted = true;
    this.completedAt = LocalDateTime.now();
  }

  public void markIncomplete() {
    this.isCompleted = false;
    this.completedAt = null;
  }
}
