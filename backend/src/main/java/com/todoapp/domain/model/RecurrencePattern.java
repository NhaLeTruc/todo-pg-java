package com.todoapp.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a recurrence pattern for recurring tasks. */
@Entity
@Table(
    name = "recurrence_patterns",
    indexes = {
      @Index(name = "idx_recurrence_patterns_task_id", columnList = "task_id"),
      @Index(name = "idx_recurrence_patterns_frequency", columnList = "frequency"),
      @Index(name = "idx_recurrence_patterns_start_date", columnList = "start_date")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrencePattern {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Task is required")
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @NotNull(message = "Frequency cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Frequency frequency;

  @Min(1)
  @Column(name = "interval_value", nullable = false)
  @Builder.Default
  private Integer intervalValue = 1;

  @NotNull(message = "Start date cannot be null")
  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  /** Comma-separated list of DayOfWeek enum values for weekly recurrence. */
  @Column(name = "days_of_week", length = 100)
  private String daysOfWeekString;

  @Column(name = "day_of_month")
  private Integer dayOfMonth;

  @Column(name = "max_occurrences")
  private Integer maxOccurrences;

  @Column(name = "generated_count", nullable = false)
  @Builder.Default
  private Integer generatedCount = 0;

  @Column(name = "last_generated_date")
  private LocalDate lastGeneratedDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Get the days of week as a set. */
  public Set<DayOfWeek> getDaysOfWeek() {
    if (daysOfWeekString == null || daysOfWeekString.isEmpty()) {
      return new HashSet<>();
    }
    return Set.of(daysOfWeekString.split(",")).stream()
        .map(String::trim)
        .map(DayOfWeek::valueOf)
        .collect(Collectors.toSet());
  }

  /** Set the days of week from a set. */
  public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      this.daysOfWeekString = null;
    } else {
      this.daysOfWeekString =
          daysOfWeek.stream().map(DayOfWeek::name).sorted().collect(Collectors.joining(","));
    }
  }

  /**
   * Check if the recurrence pattern is completed.
   *
   * @return true if the pattern has reached its max occurrences or end date
   */
  public boolean isCompleted() {
    // Check if max occurrences reached
    if (maxOccurrences != null && generatedCount >= maxOccurrences) {
      return true;
    }

    // Check if end date reached
    if (endDate != null && lastGeneratedDate != null && !lastGeneratedDate.isBefore(endDate)) {
      return true;
    }

    return false;
  }

  /** Validate the recurrence pattern before persisting. */
  @PrePersist
  @PreUpdate
  private void validate() {
    if (frequency == null) {
      throw new IllegalArgumentException("Frequency cannot be null");
    }

    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    if (intervalValue == null || intervalValue < 1) {
      throw new IllegalArgumentException("Interval value must be at least 1");
    }

    if (endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    if (maxOccurrences != null && maxOccurrences < 1) {
      throw new IllegalArgumentException("Max occurrences must be at least 1");
    }

    // Frequency-specific validation
    switch (frequency) {
      case WEEKLY:
        if (daysOfWeekString == null || daysOfWeekString.isEmpty()) {
          throw new IllegalArgumentException("Weekly recurrence must specify days of week");
        }
        break;
      case MONTHLY:
        if (dayOfMonth == null) {
          throw new IllegalArgumentException("Monthly recurrence must specify day of month");
        }
        if (dayOfMonth < 1 || dayOfMonth > 31) {
          throw new IllegalArgumentException("Day of month must be between 1 and 31");
        }
        break;
      case DAILY:
        // No additional validation needed for daily
        break;
    }
  }

  /** Custom builder to handle validation and proper initialization. */
  public static class RecurrencePatternBuilder {
    public RecurrencePattern build() {
      RecurrencePattern pattern = new RecurrencePattern();
      pattern.id = this.id;
      pattern.task = this.task;
      pattern.frequency = this.frequency;
      pattern.intervalValue = this.intervalValue != null ? this.intervalValue : 1;
      pattern.startDate = this.startDate;
      pattern.endDate = this.endDate;
      pattern.dayOfMonth = this.dayOfMonth;
      pattern.maxOccurrences = this.maxOccurrences;
      pattern.generatedCount = this.generatedCount != null ? this.generatedCount : 0;
      pattern.lastGeneratedDate = this.lastGeneratedDate;
      pattern.createdAt = this.createdAt;
      pattern.updatedAt = this.updatedAt;

      // Handle days of week
      if (this.daysOfWeek$value != null) {
        pattern.setDaysOfWeek(this.daysOfWeek$value);
      }

      // Validate before returning
      pattern.validate();

      return pattern;
    }

    private Set<DayOfWeek> daysOfWeek$value;
    private boolean daysOfWeek$set;

    public RecurrencePatternBuilder daysOfWeek(Set<DayOfWeek> daysOfWeek) {
      this.daysOfWeek$value = daysOfWeek;
      this.daysOfWeek$set = true;
      return this;
    }
  }
}
