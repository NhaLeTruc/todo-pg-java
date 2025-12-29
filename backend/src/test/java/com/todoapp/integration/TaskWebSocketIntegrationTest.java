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

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.dto.TaskUpdateMessage;
import com.todoapp.application.service.TaskService;
import com.todoapp.domain.model.*;
import com.todoapp.domain.repository.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class TaskWebSocketIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TaskService taskService;

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Clean up
    taskRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setEmail("taskws@example.com");
    testUser.setPasswordHash("hashedpassword");
    testUser.setFullName("Task WS User");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);
  }

  @Test
  void testTaskCreatedWebSocketNotification() throws Exception {
    String wsUrl = "http://localhost:" + port + "/ws/tasks";
    WebSocketStompClient stompClient =
        new WebSocketStompClient(
            new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    AtomicReference<TaskUpdateMessage> receivedMessage = new AtomicReference<>();
    StompSession session =
        stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/task-updates",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return TaskUpdateMessage.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessage.set((TaskUpdateMessage) payload);
          }
        });

    // Create a task
    TaskCreateDTO taskDTO = new TaskCreateDTO();
    taskDTO.setDescription("WebSocket test task");
    taskDTO.setDueDate(LocalDateTime.now().plusHours(1));
    taskService.createTask(taskDTO, testUser.getId());

    // Wait for message to be received
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedMessage.get()).isNotNull());

    TaskUpdateMessage message = receivedMessage.get();
    assertThat(message.action()).isEqualTo("CREATED");
    assertThat(message.description()).isEqualTo("WebSocket test task");
    assertThat(message.userId()).isEqualTo(testUser.getId());
    assertThat(message.isCompleted()).isFalse();

    session.disconnect();
    stompClient.stop();
  }

  @Test
  void testTaskUpdatedWebSocketNotification() throws Exception {
    // Create initial task
    Task task = new Task();
    task.setDescription("Initial description");
    task.setUser(testUser);
    task.setIsCompleted(false);
    task = taskRepository.save(task);

    String wsUrl = "http://localhost:" + port + "/ws/tasks";
    WebSocketStompClient stompClient =
        new WebSocketStompClient(
            new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    AtomicReference<TaskUpdateMessage> receivedMessage = new AtomicReference<>();
    StompSession session =
        stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/task-updates",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return TaskUpdateMessage.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessage.set((TaskUpdateMessage) payload);
          }
        });

    // Update the task
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated description");
    updateDTO.setPriority(Priority.HIGH);
    taskService.updateTask(task.getId(), updateDTO, testUser.getId());

    // Wait for message to be received
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedMessage.get()).isNotNull());

    TaskUpdateMessage message = receivedMessage.get();
    assertThat(message.action()).isEqualTo("UPDATED");
    assertThat(message.description()).isEqualTo("Updated description");
    assertThat(message.priority()).isEqualTo(Priority.HIGH);

    session.disconnect();
    stompClient.stop();
  }

  @Test
  void testTaskCompletedWebSocketNotification() throws Exception {
    // Create initial task
    Task task = new Task();
    task.setDescription("Task to complete");
    task.setUser(testUser);
    task.setIsCompleted(false);
    task = taskRepository.save(task);

    String wsUrl = "http://localhost:" + port + "/ws/tasks";
    WebSocketStompClient stompClient =
        new WebSocketStompClient(
            new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    AtomicReference<TaskUpdateMessage> receivedMessage = new AtomicReference<>();
    StompSession session =
        stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/task-updates",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return TaskUpdateMessage.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessage.set((TaskUpdateMessage) payload);
          }
        });

    // Toggle completion
    taskService.toggleCompletion(task.getId(), testUser.getId());

    // Wait for message to be received
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedMessage.get()).isNotNull());

    TaskUpdateMessage message = receivedMessage.get();
    assertThat(message.action()).isEqualTo("COMPLETED");
    assertThat(message.isCompleted()).isTrue();

    session.disconnect();
    stompClient.stop();
  }

  @Test
  void testTaskDeletedWebSocketNotification() throws Exception {
    // Create initial task
    Task task = new Task();
    task.setDescription("Task to delete");
    task.setUser(testUser);
    task.setIsCompleted(false);
    task = taskRepository.save(task);
    Long taskId = task.getId();

    String wsUrl = "http://localhost:" + port + "/ws/tasks";
    WebSocketStompClient stompClient =
        new WebSocketStompClient(
            new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    AtomicReference<TaskUpdateMessage> receivedMessage = new AtomicReference<>();
    StompSession session =
        stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/task-updates",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return TaskUpdateMessage.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessage.set((TaskUpdateMessage) payload);
          }
        });

    // Delete the task
    taskService.deleteTask(taskId, testUser.getId());

    // Wait for message to be received
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedMessage.get()).isNotNull());

    TaskUpdateMessage message = receivedMessage.get();
    assertThat(message.action()).isEqualTo("DELETED");
    assertThat(message.taskId()).isEqualTo(taskId);

    session.disconnect();
    stompClient.stop();
  }

  @Test
  void testMultipleTaskUpdatesViaWebSocket() throws Exception {
    String wsUrl = "http://localhost:" + port + "/ws/tasks";
    WebSocketStompClient stompClient =
        new WebSocketStompClient(
            new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    java.util.List<TaskUpdateMessage> receivedMessages = new java.util.ArrayList<>();
    StompSession session =
        stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

    session.subscribe(
        "/user/queue/task-updates",
        new StompFrameHandler() {
          @Override
          public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
            return TaskUpdateMessage.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessages.add((TaskUpdateMessage) payload);
          }
        });

    // Create multiple tasks
    TaskCreateDTO task1DTO = new TaskCreateDTO();
    task1DTO.setDescription("Task 1");
    taskService.createTask(task1DTO, testUser.getId());

    TaskCreateDTO task2DTO = new TaskCreateDTO();
    task2DTO.setDescription("Task 2");
    taskService.createTask(task2DTO, testUser.getId());

    TaskCreateDTO task3DTO = new TaskCreateDTO();
    task3DTO.setDescription("Task 3");
    taskService.createTask(task3DTO, testUser.getId());

    // Wait for all messages
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(receivedMessages).hasSize(3));

    assertThat(receivedMessages.get(0).action()).isEqualTo("CREATED");
    assertThat(receivedMessages.get(1).action()).isEqualTo("CREATED");
    assertThat(receivedMessages.get(2).action()).isEqualTo("CREATED");

    assertThat(receivedMessages.get(0).description()).isEqualTo("Task 1");
    assertThat(receivedMessages.get(1).description()).isEqualTo("Task 2");
    assertThat(receivedMessages.get(2).description()).isEqualTo("Task 3");

    session.disconnect();
    stompClient.stop();
  }
}
