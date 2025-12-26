package com.todoapp.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "recurrence_patterns",
    indexes = {
      @Index(name = "idx_recurrence_patterns_user_id", columnList = "user_id"),
      @Index(name = "idx_recurrence_patterns_frequency", columnList = "frequency")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurrencePattern {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "User is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull(message = "Frequency is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Frequency frequency;

  @Min(1)
  @Column(nullable = false)
  private Integer interval = 1;

  @Column(name = "days_of_week", length = 20)
  private String daysOfWeek;

  @Column(name = "day_of_month")
  private Integer dayOfMonth;

  @Column(name = "month_of_year")
  private Integer monthOfYear;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column private Integer occurrences;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
