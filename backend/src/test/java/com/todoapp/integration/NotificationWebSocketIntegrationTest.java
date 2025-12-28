package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.todoapp.application.service.NotificationService;
import com.todoapp.domain.model.*;
import com.todoapp.domain.repository.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class NotificationWebSocketIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private NotificationService notificationService;

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private NotificationRepository notificationRepository;

  private User testUser;
  private Task testTask;

  @BeforeEach
  void setUp() {
    // Clean up
    notificationRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setEmail("websocket@example.com");
    testUser.setPasswordHash("hashedpassword");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);

    // Create test task
    testTask = new Task();
    testTask.setDescription("WebSocket test task");
    testTask.setUser(testUser);
    testTask.setIsCompleted(false);
    testTask.setDueDate(LocalDateTime.now().plusHours(1));
    testTask = taskRepository.save(testTask);
  }

  @Test
  void testWebSocketNotificationDelivery() throws Exception {
    String wsUrl = "http://localhost:" + port + "/ws/notifications";
    WebSocketStompClient stompClient = new WebSocketStompClient(
        new SockJsClient(java.util.List.of(
            new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    AtomicReference<Notification> receivedNotification = new AtomicReference<>();
    StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/notifications",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return Notification.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedNotification.set((Notification) payload);
          }
        });

    // Create a notification
    notificationService.createNotification(
        testUser,
        NotificationType.TASK_DUE_SOON,
        "Your task is due in 1 hour",
        testTask);

    // Wait for notification to be received
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedNotification.get()).isNotNull());

    Notification notification = receivedNotification.get();
    assertThat(notification.getMessage()).isEqualTo("Your task is due in 1 hour");
    assertThat(notification.getType()).isEqualTo(NotificationType.TASK_DUE_SOON);
    assertThat(notification.getUser().getId()).isEqualTo(testUser.getId());

    session.disconnect();
    stompClient.stop();
  }

  @Test
  void testMultipleNotificationsViaWebSocket() throws Exception {
    String wsUrl = "http://localhost:" + port + "/ws/notifications";
    WebSocketStompClient stompClient = new WebSocketStompClient(
        new SockJsClient(java.util.List.of(
            new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    java.util.List<Notification> receivedNotifications = new java.util.ArrayList<>();
    StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/notifications",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return Notification.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedNotifications.add((Notification) payload);
          }
        });

    // Create multiple notifications
    notificationService.createNotification(
        testUser, NotificationType.TASK_DUE_SOON, "Notification 1", testTask);
    notificationService.createNotification(
        testUser, NotificationType.TASK_SHARED, "Notification 2", testTask);
    notificationService.createNotification(
        testUser, NotificationType.TASK_COMMENTED, "Notification 3", testTask);

    // Wait for all notifications
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedNotifications).hasSize(3));

    assertThat(receivedNotifications.get(0).getMessage()).isEqualTo("Notification 1");
    assertThat(receivedNotifications.get(1).getMessage()).isEqualTo("Notification 2");
    assertThat(receivedNotifications.get(2).getMessage()).isEqualTo("Notification 3");

    session.disconnect();
    stompClient.stop();
  }
}
