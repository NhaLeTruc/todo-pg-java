package com.todoapp.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.RecurrencePattern;

/** Repository interface for RecurrencePattern entity operations. */
@Repository
public interface RecurrencePatternRepository extends JpaRepository<RecurrencePattern, Long> {

  /**
   * Find a recurrence pattern by its associated task ID.
   *
   * @param taskId the task ID
   * @return optional recurrence pattern
   */
  Optional<RecurrencePattern> findByTaskId(Long taskId);

  /**
   * Find all pending recurrence patterns that need to generate instances.
   *
   * <p>A pattern is pending if:
   *
   * <ul>
   *   <li>It has not reached its max occurrences (if set)
   *   <li>The current date is on or after the start date
   *   <li>The current date is before or equal to the end date (if set)
   *   <li>Either no instances have been generated, or the next instance date is on or before the
   *       current date
   * </ul>
   *
   * @param currentDate the current date to check against
   * @return list of pending recurrence patterns
   */
  @Query(
      """
      SELECT rp FROM RecurrencePattern rp
      WHERE (rp.maxOccurrences IS NULL OR rp.generatedCount < rp.maxOccurrences)
        AND rp.startDate <= :currentDate
        AND (rp.endDate IS NULL OR rp.endDate >= :currentDate)
        AND (rp.lastGeneratedDate IS NULL
             OR FUNCTION('DATE_ADD', rp.lastGeneratedDate, rp.intervalValue, 'DAY') <= :currentDate)
      ORDER BY rp.startDate ASC, rp.id ASC
      """)
  List<RecurrencePattern> findPendingPatterns(@Param("currentDate") LocalDate currentDate);

  /**
   * Find all recurrence patterns for a specific user.
   *
   * @param userId the user ID
   * @return list of recurrence patterns
   */
  @Query("SELECT rp FROM RecurrencePattern rp WHERE rp.task.user.id = :userId")
  List<RecurrencePattern> findByUserId(@Param("userId") Long userId);

  /**
   * Find all active (not completed) recurrence patterns for a specific user.
   *
   * @param userId the user ID
   * @return list of active recurrence patterns
   */
  @Query(
      """
      SELECT rp FROM RecurrencePattern rp
      WHERE rp.task.user.id = :userId
        AND (rp.maxOccurrences IS NULL OR rp.generatedCount < rp.maxOccurrences)
        AND (rp.endDate IS NULL OR rp.endDate >= CURRENT_DATE)
      """)
  List<RecurrencePattern> findActiveByUserId(@Param("userId") Long userId);

  /**
   * Delete recurrence pattern by task ID.
   *
   * @param taskId the task ID
   */
  void deleteByTaskId(Long taskId);
}
