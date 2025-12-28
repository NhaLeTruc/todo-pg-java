package com.todoapp.application.dto;

import com.todoapp.domain.model.Frequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for recurrence pattern creation and updates. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Recurrence pattern for recurring tasks")
public class RecurrencePatternDTO {

  @Schema(description = "Recurrence pattern ID (null for new patterns)", example = "1")
  private Long id;

  @NotNull(message = "Frequency is required")
  @Schema(description = "Frequency of recurrence", example = "WEEKLY", required = true)
  private Frequency frequency;

  @NotNull(message = "Interval value is required")
  @Min(value = 1, message = "Interval value must be at least 1")
  @Schema(description = "Interval between recurrences", example = "1", required = true)
  @Builder.Default
  private Integer intervalValue = 1;

  @NotNull(message = "Start date is required")
  @Schema(
      description = "Date when recurrence starts",
      example = "2025-01-01",
      required = true,
      type = "string",
      format = "date")
  private LocalDate startDate;

  @Schema(
      description = "Date when recurrence ends (optional)",
      example = "2025-12-31",
      type = "string",
      format = "date")
  private LocalDate endDate;

  @Schema(
      description = "Days of week for weekly recurrence (MONDAY, TUESDAY, etc.)",
      example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
  private Set<DayOfWeek> daysOfWeek;

  @Schema(description = "Day of month for monthly recurrence (1-31)", example = "15")
  private Integer dayOfMonth;

  @Schema(description = "Maximum number of occurrences (optional)", example = "10")
  private Integer maxOccurrences;

  @Schema(description = "Number of instances already generated", example = "5")
  private Integer generatedCount;

  @Schema(
      description = "Date of last generated instance",
      example = "2025-01-15",
      type = "string",
      format = "date")
  private LocalDate lastGeneratedDate;

  @Schema(description = "Whether the recurrence pattern is completed", example = "false")
  private Boolean completed;
}
