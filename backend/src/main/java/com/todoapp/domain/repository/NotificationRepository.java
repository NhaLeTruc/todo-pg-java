package com.todoapp.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.Notification;
import com.todoapp.domain.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  /**
   * Find all unread notifications for a user, ordered by creation date descending.
   *
   * @param user The user
   * @return List of unread notifications
   */
  List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

  /**
   * Find all notifications for a user with pagination.
   *
   * @param user The user
   * @param pageable Pagination parameters
   * @return Page of notifications
   */
  Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  /**
   * Count unread notifications for a user.
   *
   * @param user The user
   * @return Number of unread notifications
   */
  long countByUserAndIsReadFalse(User user);

  /**
   * Find notifications by user ID and read status.
   *
   * @param userId The user ID
   * @param isRead Read status
   * @param pageable Pagination parameters
   * @return Page of notifications
   */
  @Query(
      "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC")
  Page<Notification> findByUserIdAndIsRead(
      @Param("userId") UUID userId, @Param("isRead") boolean isRead, Pageable pageable);

  /**
   * Mark all unread notifications as read for a user.
   *
   * @param userId The user ID
   */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
  void markAllAsReadForUser(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

  /**
   * Delete old read notifications (cleanup).
   *
   * @param before Delete notifications read before this date
   */
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :before")
  void deleteOldReadNotifications(@Param("before") LocalDateTime before);

  /**
   * Find notifications by user and related task.
   *
   * @param userId The user ID
   * @param taskId The task ID
   * @return List of notifications
   */
  @Query(
      "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.relatedTask.id = :taskId ORDER BY n.createdAt DESC")
  List<Notification> findByUserIdAndRelatedTaskId(
      @Param("userId") UUID userId, @Param("taskId") UUID taskId);
}
