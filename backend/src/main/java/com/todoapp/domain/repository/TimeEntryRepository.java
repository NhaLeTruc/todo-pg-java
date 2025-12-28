package com.todoapp.domain.repository;

import com.todoapp.domain.model.TimeEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

  /**
   * Find all time entries for a task, ordered by creation date descending.
   *
   * @param taskId the task ID
   * @return list of time entries
   */
  List<TimeEntry> findByTaskIdOrderByCreatedAtDesc(Long taskId);

  /**
   * Find all time entries for a user, ordered by creation date descending.
   *
   * @param userId the user ID
   * @return list of time entries
   */
  List<TimeEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

  /**
   * Find all time entries for a user within a date range.
   *
   * @param userId the user ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of time entries
   */
  @Query(
      """
      SELECT te FROM TimeEntry te
      WHERE te.user.id = :userId
        AND (
          (te.entryType = 'TIMER' AND te.startTime >= :startDate AND te.startTime < :endDate)
          OR (te.entryType = 'MANUAL' AND te.loggedAt >= :startDate AND te.loggedAt < :endDate)
        )
      ORDER BY COALESCE(te.startTime, te.loggedAt) DESC
      """)
  List<TimeEntry> findByUserIdAndDateRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Find the active (running) timer for a task and user.
   *
   * <p>A timer is considered active if it's a TIMER entry with no end time.
   *
   * @param taskId the task ID
   * @param userId the user ID
   * @return the active timer, or empty if none exists
   */
  @Query(
      """
      SELECT te FROM TimeEntry te
      WHERE te.task.id = :taskId
        AND te.user.id = :userId
        AND te.entryType = 'TIMER'
        AND te.endTime IS NULL
      """)
  Optional<TimeEntry> findActiveTimerForTask(
      @Param("taskId") Long taskId, @Param("userId") Long userId);

  /**
   * Find any active (running) timer for a user across all tasks.
   *
   * @param userId the user ID
   * @return the active timer, or empty if none exists
   */
  @Query(
      """
      SELECT te FROM TimeEntry te
      WHERE te.user.id = :userId
        AND te.entryType = 'TIMER'
        AND te.endTime IS NULL
      """)
  Optional<TimeEntry> findActiveTimerForUser(@Param("userId") Long userId);

  /**
   * Get the total time tracked for a task in minutes.
   *
   * <p>For TIMER entries, calculates from start/end times. For MANUAL entries, sums the duration.
   *
   * @param taskId the task ID
   * @return total time in minutes
   */
  @Query(
      """
      SELECT COALESCE(SUM(
        CASE
          WHEN te.entryType = 'MANUAL' THEN te.durationMinutes
          WHEN te.entryType = 'TIMER' AND te.endTime IS NOT NULL
            THEN CAST(EXTRACT(EPOCH FROM (te.endTime - te.startTime)) / 60 AS INTEGER)
          ELSE 0
        END
      ), 0)
      FROM TimeEntry te
      WHERE te.task.id = :taskId
      """)
  int getTotalTimeForTask(@Param("taskId") Long taskId);

  /**
   * Get the total time tracked by a user in minutes within a date range.
   *
   * @param userId the user ID
   * @param startDate the start date
   * @param endDate the end date
   * @return total time in minutes
   */
  @Query(
      """
      SELECT COALESCE(SUM(
        CASE
          WHEN te.entryType = 'MANUAL' THEN te.durationMinutes
          WHEN te.entryType = 'TIMER' AND te.endTime IS NOT NULL
            THEN CAST(EXTRACT(EPOCH FROM (te.endTime - te.startTime)) / 60 AS INTEGER)
          ELSE 0
        END
      ), 0)
      FROM TimeEntry te
      WHERE te.user.id = :userId
        AND (
          (te.entryType = 'TIMER' AND te.startTime >= :startDate AND te.startTime < :endDate)
          OR (te.entryType = 'MANUAL' AND te.loggedAt >= :startDate AND te.loggedAt < :endDate)
        )
      """)
  int getTotalTimeForUserInRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Get time tracking statistics by task for a user within a date range.
   *
   * @param userId the user ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of task IDs with their total time
   */
  @Query(
      """
      SELECT te.task.id, COALESCE(SUM(
        CASE
          WHEN te.entryType = 'MANUAL' THEN te.durationMinutes
          WHEN te.entryType = 'TIMER' AND te.endTime IS NOT NULL
            THEN CAST(EXTRACT(EPOCH FROM (te.endTime - te.startTime)) / 60 AS INTEGER)
          ELSE 0
        END
      ), 0)
      FROM TimeEntry te
      WHERE te.user.id = :userId
        AND (
          (te.entryType = 'TIMER' AND te.startTime >= :startDate AND te.startTime < :endDate)
          OR (te.entryType = 'MANUAL' AND te.loggedAt >= :startDate AND te.loggedAt < :endDate)
        )
      GROUP BY te.task.id
      ORDER BY SUM(
        CASE
          WHEN te.entryType = 'MANUAL' THEN te.durationMinutes
          WHEN te.entryType = 'TIMER' AND te.endTime IS NOT NULL
            THEN CAST(EXTRACT(EPOCH FROM (te.endTime - te.startTime)) / 60 AS INTEGER)
          ELSE 0
        END
      ) DESC
      """)
  List<Object[]> getTimeStatsByTaskForUser(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Delete all time entries for a task.
   *
   * @param taskId the task ID
   */
  void deleteByTaskId(Long taskId);

  /**
   * Count time entries for a task.
   *
   * @param taskId the task ID
   * @return count of time entries
   */
  long countByTaskId(Long taskId);
}
