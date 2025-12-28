package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.NotificationPreferenceDTO;
import com.todoapp.domain.model.*;
import com.todoapp.domain.repository.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private NotificationRepository notificationRepository;

  @Autowired private NotificationPreferenceRepository preferenceRepository;

  private User testUser;
  private Task testTask;

  @BeforeEach
  void setUp() {
    // Clean up
    notificationRepository.deleteAll();
    preferenceRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("hashedpassword");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);

    // Create test task
    testTask = new Task();
    testTask.setDescription("Test task");
    testTask.setUser(testUser);
    testTask.setIsCompleted(false);
    testTask.setDueDate(LocalDateTime.now().plusDays(1));
    testTask = taskRepository.save(testTask);
  }

  @Test
  void testGetUnreadNotifications() throws Exception {
    // Create test notifications
    Notification notification1 = new Notification();
    notification1.setUser(testUser);
    notification1.setType(NotificationType.TASK_DUE_SOON);
    notification1.setMessage("Task due soon");
    notification1.setRelatedTask(testTask);
    notification1.setRead(false);
    notificationRepository.save(notification1);

    Notification notification2 = new Notification();
    notification2.setUser(testUser);
    notification2.setType(NotificationType.TASK_SHARED);
    notification2.setMessage("Task shared");
    notification2.setRead(false);
    notificationRepository.save(notification2);

    mockMvc
        .perform(get("/api/v1/notifications").requestAttr("userId", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].message").exists())
        .andExpect(jsonPath("$[0].type").exists());
  }

  @Test
  void testGetUnreadCount() throws Exception {
    // Create test notifications
    for (int i = 0; i < 5; i++) {
      Notification notification = new Notification();
      notification.setUser(testUser);
      notification.setType(NotificationType.TASK_COMMENTED);
      notification.setMessage("Comment " + i);
      notification.setRead(false);
      notificationRepository.save(notification);
    }

    mockMvc
        .perform(get("/api/v1/notifications/count").requestAttr("userId", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string("5"));
  }

  @Test
  void testMarkNotificationAsRead() throws Exception {
    // Create test notification
    Notification notification = new Notification();
    notification.setUser(testUser);
    notification.setType(NotificationType.TASK_MENTIONED);
    notification.setMessage("You were mentioned");
    notification.setRead(false);
    notification = notificationRepository.save(notification);

    String notificationId = notification.getId().toString();

    mockMvc
        .perform(
            put("/api/v1/notifications/" + notificationId + "/read")
                .requestAttr("userId", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isRead").value(true))
        .andExpect(jsonPath("$.readAt").exists());

    // Verify in database
    Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
    assertThat(updated.isRead()).isTrue();
    assertThat(updated.getReadAt()).isNotNull();
  }

  @Test
  void testDeleteNotification() throws Exception {
    // Create test notification
    Notification notification = new Notification();
    notification.setUser(testUser);
    notification.setType(NotificationType.REMINDER);
    notification.setMessage("Reminder");
    notification.setRead(false);
    notification = notificationRepository.save(notification);

    String notificationId = notification.getId().toString();

    mockMvc
        .perform(
            delete("/api/v1/notifications/" + notificationId)
                .requestAttr("userId", testUser.getId()))
        .andExpect(status().isNoContent());

    // Verify deletion
    assertThat(notificationRepository.findById(notification.getId())).isEmpty();
  }

  @Test
  void testMarkAllAsRead() throws Exception {
    // Create multiple unread notifications
    for (int i = 0; i < 3; i++) {
      Notification notification = new Notification();
      notification.setUser(testUser);
      notification.setType(NotificationType.TASK_OVERDUE);
      notification.setMessage("Overdue " + i);
      notification.setRead(false);
      notificationRepository.save(notification);
    }

    mockMvc
        .perform(put("/api/v1/notifications/read-all").requestAttr("userId", testUser.getId()))
        .andExpect(status().isNoContent());

    // Verify all marked as read
    List<Notification> notifications =
        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(testUser);
    assertThat(notifications).isEmpty();
  }

  @Test
  void testGetNotificationPreferences() throws Exception {
    mockMvc
        .perform(get("/api/v1/notifications/preferences").requestAttr("userId", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(7)) // All notification types
        .andExpect(jsonPath("$[0].notificationType").exists())
        .andExpect(jsonPath("$[0].inAppEnabled").exists())
        .andExpect(jsonPath("$[0].emailEnabled").exists());
  }

  @Test
  void testUpdateNotificationPreference() throws Exception {
    NotificationPreferenceDTO preferenceDTO = new NotificationPreferenceDTO();
    preferenceDTO.setNotificationType(NotificationType.TASK_DUE_SOON);
    preferenceDTO.setInAppEnabled(true);
    preferenceDTO.setEmailEnabled(true);

    mockMvc
        .perform(
            put("/api/v1/notifications/preferences")
                .requestAttr("userId", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferenceDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notificationType").value("TASK_DUE_SOON"))
        .andExpect(jsonPath("$.inAppEnabled").value(true))
        .andExpect(jsonPath("$.emailEnabled").value(true));

    // Verify in database
    List<NotificationPreference> preferences = preferenceRepository.findByUser(testUser);
    assertThat(preferences).hasSize(1);
    assertThat(preferences.get(0).getNotificationType()).isEqualTo(NotificationType.TASK_DUE_SOON);
    assertThat(preferences.get(0).isInAppEnabled()).isTrue();
    assertThat(preferences.get(0).isEmailEnabled()).isTrue();
  }

  @Test
  void testCannotAccessOtherUsersNotifications() throws Exception {
    // Create another user
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("hashedpassword");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    // Create notification for other user
    Notification notification = new Notification();
    notification.setUser(otherUser);
    notification.setType(NotificationType.TASK_SHARED);
    notification.setMessage("Other user notification");
    notification.setRead(false);
    notification = notificationRepository.save(notification);

    String notificationId = notification.getId().toString();

    // Try to mark as read with different user
    mockMvc
        .perform(
            put("/api/v1/notifications/" + notificationId + "/read")
                .requestAttr("userId", testUser.getId()))
        .andExpect(status().is4xxClientError());
  }
}
