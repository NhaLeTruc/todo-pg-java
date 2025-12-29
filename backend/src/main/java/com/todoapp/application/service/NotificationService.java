package com.todoapp.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.NotificationDTO;
import com.todoapp.application.dto.NotificationPreferenceDTO;
import com.todoapp.domain.model.*;
import com.todoapp.domain.repository.NotificationPreferenceRepository;
import com.todoapp.domain.repository.NotificationRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.infrastructure.messaging.EmailNotifier;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationRepository notificationRepository;
  private final NotificationPreferenceRepository preferenceRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EmailNotifier emailNotifier;
  private final UserRepository userRepository;

  public NotificationService(
      NotificationRepository notificationRepository,
      NotificationPreferenceRepository preferenceRepository,
      SimpMessagingTemplate messagingTemplate,
      EmailNotifier emailNotifier,
      UserRepository userRepository) {
    this.notificationRepository = notificationRepository;
    this.preferenceRepository = preferenceRepository;
    this.messagingTemplate = messagingTemplate;
    this.emailNotifier = emailNotifier;
    this.userRepository = userRepository;
  }

  /**
   * Create and send a notification to a user.
   *
   * @param user The recipient user
   * @param type The notification type
   * @param message The notification message
   * @param relatedTask The related task (optional)
   * @return The created notification
   */
  @Transactional
  public Notification createNotification(
      User user, NotificationType type, String message, Task relatedTask) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("Notification type cannot be null");
    }
    if (message == null || message.trim().isEmpty()) {
      throw new IllegalArgumentException("Message cannot be empty");
    }

    // Create notification
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setType(type);
    notification.setMessage(message);
    notification.setRelatedTask(relatedTask);
    notification.setRead(false);

    notification = notificationRepository.save(notification);

    // Get user preferences
    NotificationPreference preference =
        preferenceRepository
            .findByUserAndNotificationType(user, type)
            .orElse(getDefaultPreference(user, type));

    // Send via enabled channels
    if (preference.isInAppEnabled()) {
      sendViaWebSocket(notification);
    }
    if (preference.isEmailEnabled()) {
      sendViaEmail(notification);
    }

    logger.info(
        "Notification created: id={}, type={}, user={}", notification.getId(), type, user.getId());

    return notification;
  }

  /**
   * Get unread notifications for a user.
   *
   * @param user The user
   * @return List of unread notifications
   */
  @Transactional(readOnly = true)
  public List<Notification> getUnreadNotifications(User user) {
    return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
  }

  /**
   * Get unread notification count for a user.
   *
   * @param user The user
   * @return Number of unread notifications
   */
  @Transactional(readOnly = true)
  public long getUnreadCount(User user) {
    return notificationRepository.countByUserAndIsReadFalse(user);
  }

  /**
   * Mark a notification as read.
   *
   * @param notificationId The notification ID
   */
  @Transactional
  public void markAsRead(UUID notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

    notification.markAsRead();
    notificationRepository.save(notification);

    logger.info("Notification marked as read: id={}", notificationId);
  }

  /**
   * Get notification preferences for a user.
   *
   * @param userId The user ID
   * @param type The notification type
   * @return Notification preference
   */
  @Transactional(readOnly = true)
  public NotificationPreference getPreference(Long userId, NotificationType type) {
    return preferenceRepository.findByUserIdAndNotificationType(userId, type).orElse(null);
  }

  /**
   * Update notification preference for a user.
   *
   * @param userId The user ID
   * @param type The notification type
   * @param inAppEnabled Whether in-app notifications are enabled
   * @param emailEnabled Whether email notifications are enabled
   * @return Updated preference
   */
  @Transactional
  public NotificationPreference updatePreference(
      Long userId, NotificationType type, boolean inAppEnabled, boolean emailEnabled) {
    Optional<NotificationPreference> existing =
        preferenceRepository.findByUserIdAndNotificationType(userId, type);

    NotificationPreference preference;
    if (existing.isPresent()) {
      preference = existing.get();
    } else {
      preference = new NotificationPreference();
      User user = new User();
      user.setId(userId);
      preference.setUser(user);
      preference.setNotificationType(type);
    }

    preference.setInAppEnabled(inAppEnabled);
    preference.setEmailEnabled(emailEnabled);

    return preferenceRepository.save(preference);
  }

  /**
   * Convert Notification entity to DTO.
   *
   * @param notification The notification entity
   * @return Notification DTO
   */
  public NotificationDTO toDTO(Notification notification) {
    NotificationDTO dto = new NotificationDTO();
    dto.setId(notification.getId());
    dto.setUserId(notification.getUser().getId());
    dto.setType(notification.getType());
    dto.setMessage(notification.getMessage());
    dto.setRead(notification.isRead());
    dto.setCreatedAt(notification.getCreatedAt());
    dto.setReadAt(notification.getReadAt());

    if (notification.getRelatedTask() != null) {
      dto.setRelatedTaskId(notification.getRelatedTask().getId());
      dto.setRelatedTaskDescription(notification.getRelatedTask().getDescription());
    }

    return dto;
  }

  /**
   * Convert list of notifications to DTOs.
   *
   * @param notifications List of notification entities
   * @return List of notification DTOs
   */
  public List<NotificationDTO> toDTOList(List<Notification> notifications) {
    return notifications.stream().map(this::toDTO).collect(Collectors.toList());
  }

  /**
   * Send notification via WebSocket.
   *
   * @param notification The notification to send
   */
  private void sendViaWebSocket(Notification notification) {
    try {
      String destination = "/queue/notifications";
      String userId = notification.getUser().getId().toString();
      messagingTemplate.convertAndSendToUser(userId, destination, notification);
      logger.debug("Notification sent via WebSocket: id={}", notification.getId());
    } catch (Exception e) {
      logger.error("Failed to send notification via WebSocket", e);
    }
  }

  /**
   * Send notification via email.
   *
   * @param notification The notification to send
   */
  private void sendViaEmail(Notification notification) {
    try {
      emailNotifier.sendNotificationEmail(notification);
      logger.debug("Notification sent via email: id={}", notification.getId());
    } catch (Exception e) {
      logger.error("Failed to send notification via email", e);
    }
  }

  /**
   * Get default notification preference.
   *
   * @param user The user
   * @param type The notification type
   * @return Default preference (in-app enabled, email disabled)
   */
  private NotificationPreference getDefaultPreference(User user, NotificationType type) {
    NotificationPreference preference = new NotificationPreference(user, type);
    preference.setInAppEnabled(true);
    preference.setEmailEnabled(false);
    return preference;
  }

  // Overloaded methods for controller usage with Long userId

  @Transactional(readOnly = true)
  public List<NotificationDTO> getUnreadNotifications(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    return toDTOList(getUnreadNotifications(user));
  }

  @Transactional(readOnly = true)
  public long getUnreadCount(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    return getUnreadCount(user);
  }

  @Transactional
  public NotificationDTO markAsRead(String notificationId, Long userId) {
    UUID uuid = UUID.fromString(notificationId);
    Notification notification =
        notificationRepository
            .findById(uuid)
            .orElseThrow(
                () -> new ResourceNotFoundException("Notification not found: " + notificationId));

    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("You can only mark your own notifications as read");
    }

    notification.markAsRead();
    notificationRepository.save(notification);
    logger.info("Notification marked as read: id={}", notificationId);

    return toDTO(notification);
  }

  @Transactional
  public void deleteNotification(String notificationId, Long userId) {
    UUID uuid = UUID.fromString(notificationId);
    Notification notification =
        notificationRepository
            .findById(uuid)
            .orElseThrow(
                () -> new ResourceNotFoundException("Notification not found: " + notificationId));

    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("You can only delete your own notifications");
    }

    notificationRepository.delete(notification);
    logger.info("Notification deleted: id={}", notificationId);
  }

  @Transactional
  public void markAllAsRead(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    List<Notification> unreadNotifications =
        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    for (Notification notification : unreadNotifications) {
      notification.markAsRead();
    }
    notificationRepository.saveAll(unreadNotifications);

    logger.info("All notifications marked as read for user: {}", userId);
  }

  @Transactional(readOnly = true)
  public List<NotificationPreferenceDTO> getPreferences(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    List<NotificationPreference> preferences = preferenceRepository.findByUser(user);

    // If no preferences exist, return default preferences for all types
    if (preferences.isEmpty()) {
      return java.util.Arrays.stream(NotificationType.values())
          .map(type -> toPreferenceDTO(getDefaultPreference(user, type)))
          .collect(Collectors.toList());
    }

    return preferences.stream().map(this::toPreferenceDTO).collect(Collectors.toList());
  }

  @Transactional
  public NotificationPreferenceDTO updatePreference(Long userId, NotificationPreferenceDTO dto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    NotificationType type = dto.getNotificationType();
    Optional<NotificationPreference> existing =
        preferenceRepository.findByUserAndNotificationType(user, type);

    NotificationPreference preference;
    if (existing.isPresent()) {
      preference = existing.get();
    } else {
      preference = new NotificationPreference();
      preference.setUser(user);
      preference.setNotificationType(type);
    }

    preference.setInAppEnabled(dto.isInAppEnabled());
    preference.setEmailEnabled(dto.isEmailEnabled());

    preference = preferenceRepository.save(preference);
    logger.info("Notification preference updated for user {} and type {}", userId, type);

    return toPreferenceDTO(preference);
  }

  private NotificationPreferenceDTO toPreferenceDTO(NotificationPreference preference) {
    NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
    dto.setId(preference.getId());
    dto.setUserId(preference.getUser().getId());
    dto.setNotificationType(preference.getNotificationType());
    dto.setInAppEnabled(preference.isInAppEnabled());
    dto.setEmailEnabled(preference.isEmailEnabled());
    return dto;
  }
}
