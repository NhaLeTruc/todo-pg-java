package com.todoapp.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.todoapp.domain.model.EntryType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for time entry data transfer.
 *
 * <p>Used for both TIMER and MANUAL time entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Time entry for tracking work on tasks")
public class TimeEntryDTO {

  @Schema(description = "Time entry ID", example = "1")
  private Long id;

  @Schema(description = "Task ID", example = "10", required = true)
  @NotNull(message = "Task ID is required")
  private Long taskId;

  @Schema(description = "User ID", example = "5", required = true)
  @NotNull(message = "User ID is required")
  private Long userId;

  @Schema(description = "Entry type (TIMER or MANUAL)", example = "TIMER", required = true)
  @NotNull(message = "Entry type is required")
  private EntryType entryType;

  @Schema(description = "Start time for TIMER entries", example = "2025-01-15T10:30:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startTime;

  @Schema(description = "End time for TIMER entries", example = "2025-01-15T11:45:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endTime;

  @Schema(description = "Duration in minutes", example = "75")
  @Min(value = 1, message = "Duration must be positive")
  private Integer durationMinutes;

  @Schema(
      description = "When the time was logged (for MANUAL entries)",
      example = "2025-01-15T14:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime loggedAt;

  @Schema(description = "Notes about the work done", example = "Fixed bug in authentication flow")
  private String notes;

  @Schema(description = "When the entry was created", example = "2025-01-15T10:30:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "Whether this timer is currently running", example = "true")
  private boolean running;

  /**
   * Create a DTO for starting a new timer.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @param notes optional notes
   * @return time entry DTO
   */
  public static TimeEntryDTO forStartTimer(Long taskId, Long userId, String notes) {
    return TimeEntryDTO.builder()
        .taskId(taskId)
        .userId(userId)
        .entryType(EntryType.TIMER)
        .notes(notes)
        .build();
  }

  /**
   * Create a DTO for manual time logging.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @param durationMinutes the duration in minutes
   * @param notes optional notes
   * @param loggedAt when the time was logged (null for now)
   * @return time entry DTO
   */
  public static TimeEntryDTO forManualLog(
      Long taskId, Long userId, Integer durationMinutes, String notes, LocalDateTime loggedAt) {
    return TimeEntryDTO.builder()
        .taskId(taskId)
        .userId(userId)
        .entryType(EntryType.MANUAL)
        .durationMinutes(durationMinutes)
        .notes(notes)
        .loggedAt(loggedAt)
        .build();
  }
}
