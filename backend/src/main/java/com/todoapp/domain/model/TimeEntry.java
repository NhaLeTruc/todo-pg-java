package com.todoapp.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreatedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a time entry for tracking work on tasks.
 *
 * <p>Time entries can be created in two ways:
 *
 * <ul>
 *   <li>TIMER - User starts/stops a timer, tracking actual work time
 *   <li>MANUAL - User manually logs time spent on a task
 * </ul>
 *
 * <p>For TIMER entries, duration is calculated from startTime and endTime. For MANUAL entries,
 * duration is set directly.
 */
@Entity
@Table(
    name = "time_entries",
    indexes = {
      @Index(name = "idx_time_entry_task", columnList = "task_id"),
      @Index(name = "idx_time_entry_user", columnList = "user_id"),
      @Index(name = "idx_time_entry_task_user", columnList = "task_id,user_id"),
      @Index(name = "idx_time_entry_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, length = 20)
  private EntryType entryType;

  @Column(name = "start_time")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  @Column(name = "duration_minutes")
  private Integer durationMinutes;

  @Column(name = "logged_at")
  private LocalDateTime loggedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Get the duration in minutes for this time entry.
   *
   * <p>For TIMER entries, calculates duration from startTime and endTime. Returns null if timer is
   * still running (endTime is null).
   *
   * <p>For MANUAL entries, returns the manually set duration.
   *
   * @return duration in minutes, or null if timer is still running
   */
  public Integer getDurationMinutes() {
    if (entryType == EntryType.MANUAL) {
      return durationMinutes;
    } else if (entryType == EntryType.TIMER) {
      if (endTime == null) {
        return null;
      }
      Duration duration = Duration.between(startTime, endTime);
      return (int) duration.toMinutes();
    }
    return null;
  }

  /**
   * Check if this timer is currently running.
   *
   * @return true if this is a TIMER entry with no end time, false otherwise
   */
  public boolean isRunning() {
    return entryType == EntryType.TIMER && endTime == null;
  }

  /**
   * Stop a running timer by setting the end time to now.
   *
   * @throws IllegalStateException if timer is not running
   */
  public void stop() {
    if (!isRunning()) {
      throw new IllegalStateException("Timer is not running");
    }
    this.endTime = LocalDateTime.now();
  }

  /**
   * Validate this time entry before persisting.
   *
   * @throws IllegalArgumentException if validation fails
   */
  @PrePersist
  @PreUpdate
  public void validate() {
    if (task == null) {
      throw new IllegalArgumentException("Task is required");
    }
    if (user == null) {
      throw new IllegalArgumentException("User is required");
    }
    if (entryType == null) {
      throw new IllegalArgumentException("Entry type is required");
    }

    if (entryType == EntryType.TIMER) {
      validateTimerEntry();
    } else if (entryType == EntryType.MANUAL) {
      validateManualEntry();
    }
  }

  private void validateTimerEntry() {
    if (startTime == null) {
      throw new IllegalArgumentException("Start time is required for TIMER entries");
    }
    if (endTime != null && endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
  }

  private void validateManualEntry() {
    if (durationMinutes == null || durationMinutes <= 0) {
      throw new IllegalArgumentException("Duration must be positive for MANUAL entries");
    }
  }

  /** Set the creation timestamp before persisting. */
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (entryType == EntryType.MANUAL && loggedAt == null) {
      loggedAt = LocalDateTime.now();
    }
  }
}
