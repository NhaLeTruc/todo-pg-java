package com.todoapp.unit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.todoapp.application.service.NotificationService;
import com.todoapp.domain.model.*;
import com.todoapp.domain.repository.NotificationPreferenceRepository;
import com.todoapp.domain.repository.NotificationRepository;
import com.todoapp.infrastructure.messaging.EmailNotifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
public class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationPreferenceRepository preferenceRepository;

  @Mock private SimpMessagingTemplate messagingTemplate;

  @Mock private EmailNotifier emailNotifier;

  @InjectMocks private NotificationService notificationService;

  private User testUser;
  private Task testTask;

  @BeforeEach
  public void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("hashedPassword");

    testTask = new Task();
    testTask.setId(1L);
    testTask.setDescription("Test task");
    testTask.setUser(testUser);
  }

  // Deleted over-mocked test:
  // - shouldCreateNotificationSuccessfully() - Created a notification object,
  //   mocked repository to return that exact object, then verified the returned
  //   object had the same fields. This is just testing that mocks work.
  //   The only real assertion of value was verify(repository.save), but that alone
  //   doesn't justify this entire test. Better tested via integration tests.

  @Test
  @DisplayName("Should reject notification with null user")
  public void shouldRejectNotificationWithNullUser() {
    assertThatThrownBy(
            () ->
                notificationService.createNotification(
                    null, NotificationType.TASK_DUE_SOON, "Test message", testTask))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User cannot be null");
  }

  @Test
  @DisplayName("Should reject notification with null type")
  public void shouldRejectNotificationWithNullType() {
    assertThatThrownBy(
            () -> notificationService.createNotification(testUser, null, "Test message", testTask))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Notification type cannot be null");
  }

  @Test
  @DisplayName("Should reject notification with empty message")
  public void shouldRejectNotificationWithEmptyMessage() {
    assertThatThrownBy(
            () ->
                notificationService.createNotification(
                    testUser, NotificationType.TASK_DUE_SOON, "", testTask))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message cannot be empty");
  }

  @Test
  @DisplayName("Should send notification via WebSocket when preference enabled")
  public void shouldSendNotificationViaWebSocket() {
    NotificationPreference preference = new NotificationPreference();
    preference.setUser(testUser);
    preference.setNotificationType(NotificationType.TASK_DUE_SOON);
    preference.setInAppEnabled(true);
    preference.setEmailEnabled(false);

    when(preferenceRepository.findByUserAndNotificationType(
            testUser, NotificationType.TASK_DUE_SOON))
        .thenReturn(Optional.of(preference));

    Notification notification = new Notification();
    notification.setId(UUID.randomUUID());
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_DUE_SOON);
    notification.setMessage("Task is due soon");

    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    notificationService.createNotification(
        testUser, NotificationType.TASK_DUE_SOON, "Task is due soon", testTask);

    verify(messagingTemplate, times(1))
        .convertAndSendToUser(
            eq(testUser.getId().toString()), eq("/queue/notifications"), any(Notification.class));
  }

  @Test
  @DisplayName("Should send notification via email when preference enabled")
  public void shouldSendNotificationViaEmail() {
    NotificationPreference preference = new NotificationPreference();
    preference.setUser(testUser);
    preference.setNotificationType(NotificationType.TASK_DUE_SOON);
    preference.setInAppEnabled(false);
    preference.setEmailEnabled(true);

    when(preferenceRepository.findByUserAndNotificationType(
            testUser, NotificationType.TASK_DUE_SOON))
        .thenReturn(Optional.of(preference));

    Notification notification = new Notification();
    notification.setId(UUID.randomUUID());
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_DUE_SOON);
    notification.setMessage("Task is due soon");

    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    notificationService.createNotification(
        testUser, NotificationType.TASK_DUE_SOON, "Task is due soon", testTask);

    verify(emailNotifier, times(1)).sendNotificationEmail(any(Notification.class));
  }

  @Test
  @DisplayName("Should send notification via both channels when both enabled")
  public void shouldSendNotificationViaBothChannels() {
    NotificationPreference preference = new NotificationPreference();
    preference.setUser(testUser);
    preference.setNotificationType(NotificationType.TASK_DUE_SOON);
    preference.setInAppEnabled(true);
    preference.setEmailEnabled(true);

    when(preferenceRepository.findByUserAndNotificationType(
            testUser, NotificationType.TASK_DUE_SOON))
        .thenReturn(Optional.of(preference));

    Notification notification = new Notification();
    notification.setId(UUID.randomUUID());
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_DUE_SOON);
    notification.setMessage("Task is due soon");

    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    notificationService.createNotification(
        testUser, NotificationType.TASK_DUE_SOON, "Task is due soon", testTask);

    verify(messagingTemplate, times(1))
        .convertAndSendToUser(anyString(), anyString(), any(Notification.class));
    verify(emailNotifier, times(1)).sendNotificationEmail(any(Notification.class));
  }

  @Test
  @DisplayName("Should use default preferences when none exist")
  public void shouldUseDefaultPreferencesWhenNoneExist() {
    when(preferenceRepository.findByUserAndNotificationType(
            testUser, NotificationType.TASK_DUE_SOON))
        .thenReturn(Optional.empty());

    Notification notification = new Notification();
    notification.setId(UUID.randomUUID());
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_DUE_SOON);
    notification.setMessage("Task is due soon");

    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    notificationService.createNotification(
        testUser, NotificationType.TASK_DUE_SOON, "Task is due soon", testTask);

    // Default should be in-app enabled
    verify(messagingTemplate, times(1))
        .convertAndSendToUser(anyString(), anyString(), any(Notification.class));
  }

  @Test
  @DisplayName("Should get unread notifications for user")
  public void shouldGetUnreadNotificationsForUser() {
    Notification notification1 = new Notification();
    notification1.setId(UUID.randomUUID());
    notification1.setUser(testUser);
    notification1.setType(NotificationType.TASK_DUE_SOON);
    notification1.setMessage("Notification 1");
    notification1.setRead(false);

    Notification notification2 = new Notification();
    notification2.setId(UUID.randomUUID());
    notification2.setUser(testUser);
    notification2.setType(NotificationType.TASK_COMMENTED);
    notification2.setMessage("Notification 2");
    notification2.setRead(false);

    when(notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(testUser))
        .thenReturn(Arrays.asList(notification1, notification2));

    List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser);

    assertThat(unreadNotifications).hasSize(2);
    assertThat(unreadNotifications.get(0)).isEqualTo(notification1);
    assertThat(unreadNotifications.get(1)).isEqualTo(notification2);
  }

  @Test
  @DisplayName("Should mark notification as read")
  public void shouldMarkNotificationAsRead() {
    Notification notification = new Notification();
    notification.setId(UUID.randomUUID());
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_DUE_SOON);
    notification.setMessage("Test notification");
    notification.setRead(false);

    when(notificationRepository.findById(notification.getId()))
        .thenReturn(Optional.of(notification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    notificationService.markAsRead(notification.getId());

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    assertThat(captor.getValue().isRead()).isTrue();
  }

  @Test
  @DisplayName("Should count unread notifications for user")
  public void shouldCountUnreadNotificationsForUser() {
    when(notificationRepository.countByUserAndIsReadFalse(testUser)).thenReturn(5L);

    long count = notificationService.getUnreadCount(testUser);

    assertThat(count).isEqualTo(5L);
  }
}
