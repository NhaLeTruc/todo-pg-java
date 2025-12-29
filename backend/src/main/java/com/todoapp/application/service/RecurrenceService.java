package com.todoapp.application.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.domain.model.Frequency;
import com.todoapp.domain.model.RecurrencePattern;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.repository.RecurrencePatternRepository;
import com.todoapp.domain.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for managing recurring task patterns and generating task instances. */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecurrenceService {

  private final RecurrencePatternRepository recurrencePatternRepository;
  private final TaskRepository taskRepository;

  /**
   * Generate the next task instance from a recurrence pattern.
   *
   * @param pattern the recurrence pattern
   * @return the date of the generated instance, or null if the pattern is completed
   */
  @Transactional
  public LocalDate generateNextInstance(RecurrencePattern pattern) {
    // Check if pattern is completed
    if (pattern.isCompleted()) {
      log.debug("Recurrence pattern {} is completed, skipping generation", pattern.getId());
      return null;
    }

    // Calculate next occurrence date
    LocalDate nextDate = calculateNextOccurrence(pattern);

    // Check if next date is beyond end date
    if (pattern.getEndDate() != null && nextDate.isAfter(pattern.getEndDate())) {
      log.debug(
          "Next occurrence {} is after end date {}, pattern completed",
          nextDate,
          pattern.getEndDate());
      return null;
    }

    // Create new task instance
    Task templateTask = pattern.getTask();
    Task newInstance =
        Task.builder()
            .description(templateTask.getDescription())
            .user(templateTask.getUser())
            .priority(templateTask.getPriority())
            .category(templateTask.getCategory())
            .isCompleted(false)
            .dueDate(nextDate.atStartOfDay())
            .build();

    taskRepository.save(newInstance);

    // Update pattern state
    pattern.setGeneratedCount(pattern.getGeneratedCount() + 1);
    pattern.setLastGeneratedDate(nextDate);
    recurrencePatternRepository.save(pattern);

    log.info(
        "Generated task instance for recurrence pattern {}, due date: {}, count: {}/{}",
        pattern.getId(),
        nextDate,
        pattern.getGeneratedCount(),
        pattern.getMaxOccurrences() != null ? pattern.getMaxOccurrences() : "unlimited");

    return nextDate;
  }

  /**
   * Calculate the next occurrence date based on the recurrence pattern.
   *
   * @param pattern the recurrence pattern
   * @return the next occurrence date
   */
  private LocalDate calculateNextOccurrence(RecurrencePattern pattern) {
    LocalDate baseDate =
        pattern.getLastGeneratedDate() != null
            ? pattern.getLastGeneratedDate()
            : pattern.getStartDate().minusDays(1);

    switch (pattern.getFrequency()) {
      case DAILY:
        return calculateNextDaily(baseDate, pattern.getIntervalValue());

      case WEEKLY:
        return calculateNextWeekly(baseDate, pattern.getIntervalValue(), pattern.getDaysOfWeek());

      case MONTHLY:
        return calculateNextMonthly(baseDate, pattern.getIntervalValue(), pattern.getDayOfMonth());

      default:
        throw new IllegalStateException("Unsupported frequency: " + pattern.getFrequency());
    }
  }

  /**
   * Calculate next daily occurrence.
   *
   * @param baseDate the base date to calculate from
   * @param interval the number of days between occurrences
   * @return the next occurrence date
   */
  private LocalDate calculateNextDaily(LocalDate baseDate, int interval) {
    return baseDate.plusDays(interval);
  }

  /**
   * Calculate next weekly occurrence.
   *
   * @param baseDate the base date to calculate from
   * @param interval the number of weeks between occurrences (1 for every week, 2 for bi-weekly,
   *     etc.)
   * @param daysOfWeek the days of week when task should occur
   * @return the next occurrence date
   */
  private LocalDate calculateNextWeekly(
      LocalDate baseDate, int interval, Set<DayOfWeek> daysOfWeek) {
    // Start from next day
    LocalDate nextDate = baseDate.plusDays(1);

    // If interval > 1, we need to find the next occurrence in the target week
    // For now, find the next matching day of week
    while (true) {
      if (daysOfWeek.contains(nextDate.getDayOfWeek())) {
        // Check if this is the right week based on interval
        long weeksBetween =
            (nextDate.toEpochDay() - baseDate.toEpochDay()) / 7; // Simplified week calculation
        if (weeksBetween >= interval || baseDate.equals(nextDate.minusDays(1))) {
          return nextDate;
        }
      }
      nextDate = nextDate.plusDays(1);

      // Safety check to prevent infinite loop
      if (nextDate.isAfter(baseDate.plusWeeks(interval * 4))) {
        // If we've gone too far, reset and find first matching day in target week
        LocalDate targetWeekStart = baseDate.plusWeeks(interval);
        nextDate = targetWeekStart;
        while (!daysOfWeek.contains(nextDate.getDayOfWeek())) {
          nextDate = nextDate.plusDays(1);
        }
        return nextDate;
      }
    }
  }

  /**
   * Calculate next monthly occurrence.
   *
   * @param baseDate the base date to calculate from
   * @param interval the number of months between occurrences
   * @param dayOfMonth the target day of month (1-31)
   * @return the next occurrence date
   */
  private LocalDate calculateNextMonthly(LocalDate baseDate, int interval, int dayOfMonth) {
    LocalDate nextMonth = baseDate.plusMonths(interval);

    // Handle cases where dayOfMonth doesn't exist in target month (e.g., Feb 31)
    int actualDayOfMonth = Math.min(dayOfMonth, nextMonth.lengthOfMonth());

    return LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), actualDayOfMonth);
  }

  /**
   * Process all pending recurrence patterns and generate task instances.
   *
   * @return the number of patterns processed
   */
  @Transactional
  public int processPendingRecurrences() {
    LocalDate today = LocalDate.now();
    List<RecurrencePattern> pendingPatterns =
        recurrencePatternRepository.findPendingPatterns(today);

    int processed = 0;
    for (RecurrencePattern pattern : pendingPatterns) {
      try {
        LocalDate generatedDate = generateNextInstance(pattern);
        if (generatedDate != null) {
          processed++;
        }
      } catch (Exception e) {
        log.error(
            "Error generating instance for recurrence pattern {}: {}",
            pattern.getId(),
            e.getMessage(),
            e);
      }
    }

    log.info("Processed {} recurrence patterns", processed);
    return processed;
  }

  /**
   * Create a new recurrence pattern for a task.
   *
   * @param task the task to create a recurrence pattern for
   * @param frequency the recurrence frequency
   * @param intervalValue the interval between recurrences
   * @param startDate the start date
   * @param endDate the end date (optional)
   * @param daysOfWeek the days of week for weekly recurrence (optional)
   * @param dayOfMonth the day of month for monthly recurrence (optional)
   * @param maxOccurrences the maximum number of occurrences (optional)
   * @return the created recurrence pattern
   */
  @Transactional
  public RecurrencePattern createRecurrencePattern(
      Task task,
      Frequency frequency,
      int intervalValue,
      LocalDate startDate,
      LocalDate endDate,
      Set<DayOfWeek> daysOfWeek,
      Integer dayOfMonth,
      Integer maxOccurrences) {

    RecurrencePattern.RecurrencePatternBuilder builder =
        RecurrencePattern.builder()
            .task(task)
            .frequency(frequency)
            .intervalValue(intervalValue)
            .startDate(startDate)
            .endDate(endDate)
            .maxOccurrences(maxOccurrences);

    if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
      builder.daysOfWeek(daysOfWeek);
    }

    if (dayOfMonth != null) {
      builder.dayOfMonth(dayOfMonth);
    }

    RecurrencePattern pattern = builder.build();
    return recurrencePatternRepository.save(pattern);
  }

  /**
   * Update an existing recurrence pattern.
   *
   * @param patternId the pattern ID
   * @param frequency the recurrence frequency
   * @param intervalValue the interval between recurrences
   * @param endDate the end date (optional)
   * @param daysOfWeek the days of week for weekly recurrence (optional)
   * @param dayOfMonth the day of month for monthly recurrence (optional)
   * @param maxOccurrences the maximum number of occurrences (optional)
   * @return the updated recurrence pattern
   */
  @Transactional
  public RecurrencePattern updateRecurrencePattern(
      Long patternId,
      Frequency frequency,
      int intervalValue,
      LocalDate endDate,
      Set<DayOfWeek> daysOfWeek,
      Integer dayOfMonth,
      Integer maxOccurrences) {

    RecurrencePattern pattern =
        recurrencePatternRepository
            .findById(patternId)
            .orElseThrow(
                () -> new IllegalArgumentException("Recurrence pattern not found: " + patternId));

    pattern.setFrequency(frequency);
    pattern.setIntervalValue(intervalValue);
    pattern.setEndDate(endDate);
    pattern.setMaxOccurrences(maxOccurrences);

    if (daysOfWeek != null) {
      pattern.setDaysOfWeek(daysOfWeek);
    }

    if (dayOfMonth != null) {
      pattern.setDayOfMonth(dayOfMonth);
    }

    return recurrencePatternRepository.save(pattern);
  }

  /**
   * Delete a recurrence pattern.
   *
   * @param patternId the pattern ID
   */
  @Transactional
  public void deleteRecurrencePattern(Long patternId) {
    recurrencePatternRepository.deleteById(patternId);
    log.info("Deleted recurrence pattern {}", patternId);
  }

  /**
   * Get recurrence pattern by task ID.
   *
   * @param taskId the task ID
   * @return the recurrence pattern, or null if not found
   */
  public RecurrencePattern getByTaskId(Long taskId) {
    return recurrencePatternRepository.findByTaskId(taskId).orElse(null);
  }
}
