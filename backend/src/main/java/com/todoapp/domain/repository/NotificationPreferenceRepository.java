package com.todoapp.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.NotificationPreference;
import com.todoapp.domain.model.NotificationType;
import com.todoapp.domain.model.User;

@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, UUID> {

  /**
   * Find notification preference by user and notification type.
   *
   * @param user The user
   * @param notificationType The notification type
   * @return Optional notification preference
   */
  Optional<NotificationPreference> findByUserAndNotificationType(
      User user, NotificationType notificationType);

  /**
   * Find all notification preferences for a user.
   *
   * @param user The user
   * @return List of notification preferences
   */
  List<NotificationPreference> findByUser(User user);

  /**
   * Find notification preference by user ID and notification type.
   *
   * @param userId The user ID
   * @param notificationType The notification type
   * @return Optional notification preference
   */
  @Query(
      "SELECT np FROM NotificationPreference np WHERE np.user.id = :userId AND np.notificationType = :type")
  Optional<NotificationPreference> findByUserIdAndNotificationType(
      @Param("userId") UUID userId, @Param("type") NotificationType notificationType);

  /**
   * Find all users who have a specific notification type enabled via email.
   *
   * @param notificationType The notification type
   * @return List of notification preferences
   */
  @Query(
      "SELECT np FROM NotificationPreference np WHERE np.notificationType = :type AND np.emailEnabled = true")
  List<NotificationPreference> findByNotificationTypeAndEmailEnabled(
      @Param("type") NotificationType notificationType);

  /**
   * Find all users who have a specific notification type enabled in-app.
   *
   * @param notificationType The notification type
   * @return List of notification preferences
   */
  @Query(
      "SELECT np FROM NotificationPreference np WHERE np.notificationType = :type AND np.inAppEnabled = true")
  List<NotificationPreference> findByNotificationTypeAndInAppEnabled(
      @Param("type") NotificationType notificationType);

  /**
   * Check if user has any notification channel enabled for a specific type.
   *
   * @param userId The user ID
   * @param notificationType The notification type
   * @return true if at least one channel is enabled
   */
  @Query(
      "SELECT COUNT(np) > 0 FROM NotificationPreference np WHERE np.user.id = :userId AND np.notificationType = :type AND (np.inAppEnabled = true OR np.emailEnabled = true)")
  boolean hasAnyChannelEnabled(
      @Param("userId") UUID userId, @Param("type") NotificationType notificationType);
}
