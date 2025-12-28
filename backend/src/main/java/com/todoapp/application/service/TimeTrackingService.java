package com.todoapp.application.service;

import com.todoapp.application.dto.TimeEntryDTO;
import com.todoapp.application.mapper.TimeEntryMapper;
import com.todoapp.domain.model.EntryType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TimeEntry;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TimeEntryRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing time tracking entries.
 *
 * <p>Handles both timer-based tracking (start/stop) and manual time logging.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TimeTrackingService {

  private final TimeEntryRepository timeEntryRepository;
  private final TaskRepository taskRepository;
  private final TimeEntryMapper timeEntryMapper;

  /**
   * Start a timer for a task.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @param notes optional notes
   * @return the created time entry
   * @throws ResourceNotFoundException if task not found
   * @throws IllegalArgumentException if user does not own task
   * @throws IllegalStateException if an active timer already exists for this task
   */
  public TimeEntryDTO startTimer(Long taskId, Long userId, String notes) {
    log.debug("Starting timer for task ID: {} by user ID: {}", taskId, userId);

    Task task = getTaskAndValidateOwnership(taskId, userId);

    Optional<TimeEntry> activeTimer = timeEntryRepository.findActiveTimerForTask(taskId, userId);
    if (activeTimer.isPresent()) {
      throw new IllegalStateException(
          "An active timer already exists for this task. Please stop it first.");
    }

    TimeEntry timeEntry =
        TimeEntry.builder()
            .task(task)
            .user(task.getUser())
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now())
            .notes(notes)
            .build();

    TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
    log.info("Timer started with ID: {} for task ID: {}", savedEntry.getId(), taskId);

    return timeEntryMapper.toDTO(savedEntry);
  }

  /**
   * Stop a running timer.
   *
   * @param timeEntryId the time entry ID
   * @param userId the user ID
   * @return the updated time entry
   * @throws ResourceNotFoundException if time entry not found
   * @throws IllegalArgumentException if user does not own time entry
   * @throws IllegalStateException if timer is not running
   */
  public TimeEntryDTO stopTimer(Long timeEntryId, Long userId) {
    log.debug("Stopping timer ID: {} by user ID: {}", timeEntryId, userId);

    TimeEntry timeEntry =
        timeEntryRepository
            .findById(timeEntryId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Time entry not found with ID: " + timeEntryId));

    if (!timeEntry.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not own this time entry");
    }

    if (!timeEntry.isRunning()) {
      throw new IllegalStateException("Timer is not running");
    }

    timeEntry.stop();
    TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

    log.info(
        "Timer stopped with ID: {}, duration: {} minutes",
        savedEntry.getId(),
        savedEntry.getDurationMinutes());

    return timeEntryMapper.toDTO(savedEntry);
  }

  /**
   * Log time manually for a task.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @param durationMinutes the duration in minutes
   * @param notes optional notes
   * @param loggedAt when the time was logged (null for now)
   * @return the created time entry
   * @throws ResourceNotFoundException if task not found
   * @throws IllegalArgumentException if user does not own task or duration is invalid
   */
  public TimeEntryDTO logManualTime(
      Long taskId, Long userId, Integer durationMinutes, String notes, LocalDateTime loggedAt) {
    log.debug(
        "Logging manual time for task ID: {} by user ID: {}, duration: {} minutes",
        taskId,
        userId,
        durationMinutes);

    Task task = getTaskAndValidateOwnership(taskId, userId);

    if (durationMinutes == null || durationMinutes <= 0) {
      throw new IllegalArgumentException("Duration must be positive");
    }

    TimeEntry timeEntry =
        TimeEntry.builder()
            .task(task)
            .user(task.getUser())
            .entryType(EntryType.MANUAL)
            .durationMinutes(durationMinutes)
            .notes(notes)
            .loggedAt(loggedAt != null ? loggedAt : LocalDateTime.now())
            .build();

    TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
    log.info(
        "Manual time entry created with ID: {} for task ID: {}, duration: {} minutes",
        savedEntry.getId(),
        taskId,
        durationMinutes);

    return timeEntryMapper.toDTO(savedEntry);
  }

  /**
   * Get all time entries for a task.
   *
   * @param taskId the task ID
   * @param userId the user ID (for access control)
   * @return list of time entries
   * @throws ResourceNotFoundException if task not found
   * @throws IllegalArgumentException if user does not have access to task
   */
  public List<TimeEntryDTO> getTimeEntriesForTask(Long taskId, Long userId) {
    log.debug("Fetching time entries for task ID: {} by user ID: {}", taskId, userId);

    Task task = getTaskAndValidateAccess(taskId, userId);

    List<TimeEntry> entries = timeEntryRepository.findByTaskIdOrderByCreatedAtDesc(taskId);

    log.debug("Found {} time entries for task ID: {}", entries.size(), taskId);
    return entries.stream().map(timeEntryMapper::toDTO).collect(Collectors.toList());
  }

  /**
   * Get the active timer for a task, if one exists.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @return the active timer, or empty if none exists
   * @throws ResourceNotFoundException if task not found
   * @throws IllegalArgumentException if user does not have access to task
   */
  public Optional<TimeEntryDTO> getActiveTimer(Long taskId, Long userId) {
    log.debug("Checking for active timer on task ID: {} by user ID: {}", taskId, userId);

    Task task = getTaskAndValidateAccess(taskId, userId);

    Optional<TimeEntry> activeTimer = timeEntryRepository.findActiveTimerForTask(taskId, userId);

    return activeTimer.map(timeEntryMapper::toDTO);
  }

  /**
   * Get any active timer for a user across all tasks.
   *
   * @param userId the user ID
   * @return the active timer, or empty if none exists
   */
  public Optional<TimeEntryDTO> getActiveTimerForUser(Long userId) {
    log.debug("Checking for active timer for user ID: {}", userId);

    Optional<TimeEntry> activeTimer = timeEntryRepository.findActiveTimerForUser(userId);

    return activeTimer.map(timeEntryMapper::toDTO);
  }

  /**
   * Get the total time tracked for a task in minutes.
   *
   * @param taskId the task ID
   * @return total time in minutes
   */
  public int getTotalTimeForTask(Long taskId) {
    log.debug("Calculating total time for task ID: {}", taskId);

    int totalTime = timeEntryRepository.getTotalTimeForTask(taskId);

    log.debug("Total time for task ID: {} is {} minutes", taskId, totalTime);
    return totalTime;
  }

  /**
   * Get the total time tracked by a user in a date range.
   *
   * @param userId the user ID
   * @param startDate the start date
   * @param endDate the end date
   * @return total time in minutes
   */
  public int getTotalTimeForUserInRange(
      Long userId, LocalDateTime startDate, LocalDateTime endDate) {
    log.debug(
        "Calculating total time for user ID: {} from {} to {}", userId, startDate, endDate);

    int totalTime = timeEntryRepository.getTotalTimeForUserInRange(userId, startDate, endDate);

    log.debug("Total time for user ID: {} is {} minutes", userId, totalTime);
    return totalTime;
  }

  /**
   * Get all time entries for a user in a date range.
   *
   * @param userId the user ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of time entries
   */
  public List<TimeEntryDTO> getTimeEntriesForUserInRange(
      Long userId, LocalDateTime startDate, LocalDateTime endDate) {
    log.debug("Fetching time entries for user ID: {} from {} to {}", userId, startDate, endDate);

    List<TimeEntry> entries =
        timeEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate);

    log.debug("Found {} time entries for user ID: {}", entries.size(), userId);
    return entries.stream().map(timeEntryMapper::toDTO).collect(Collectors.toList());
  }

  /**
   * Delete a time entry.
   *
   * @param timeEntryId the time entry ID
   * @param userId the user ID
   * @throws ResourceNotFoundException if time entry not found
   * @throws IllegalArgumentException if user does not own time entry
   */
  public void deleteTimeEntry(Long timeEntryId, Long userId) {
    log.debug("Deleting time entry ID: {} by user ID: {}", timeEntryId, userId);

    TimeEntry timeEntry =
        timeEntryRepository
            .findById(timeEntryId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Time entry not found with ID: " + timeEntryId));

    if (!timeEntry.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not own this time entry");
    }

    timeEntryRepository.delete(timeEntry);
    log.info("Time entry ID: {} deleted successfully", timeEntryId);
  }

  /**
   * Update the notes for a time entry.
   *
   * @param timeEntryId the time entry ID
   * @param userId the user ID
   * @param notes the new notes
   * @return the updated time entry
   * @throws ResourceNotFoundException if time entry not found
   * @throws IllegalArgumentException if user does not own time entry
   */
  public TimeEntryDTO updateNotes(Long timeEntryId, Long userId, String notes) {
    log.debug("Updating notes for time entry ID: {} by user ID: {}", timeEntryId, userId);

    TimeEntry timeEntry =
        timeEntryRepository
            .findById(timeEntryId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Time entry not found with ID: " + timeEntryId));

    if (!timeEntry.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not own this time entry");
    }

    timeEntry.setNotes(notes);
    TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

    log.info("Notes updated for time entry ID: {}", timeEntryId);
    return timeEntryMapper.toDTO(savedEntry);
  }

  private Task getTaskAndValidateOwnership(Long taskId, Long userId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not own this task");
    }

    return task;
  }

  private Task getTaskAndValidateAccess(Long taskId, Long userId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    return task;
  }
}
