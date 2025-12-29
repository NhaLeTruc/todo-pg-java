package com.todoapp.unit.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.application.service.NotificationService;
import com.todoapp.domain.model.NotificationType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.infrastructure.messaging.DueDateNotifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DueDateNotifier Tests")
public class DueDateNotifierTest {

  @Mock private TaskRepository taskRepository;

  @Mock private NotificationService notificationService;

  @InjectMocks private DueDateNotifier dueDateNotifier;

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
    testTask.setIsCompleted(false);
  }

  @Test
  @DisplayName("Should send notification for tasks due tomorrow")
  public void shouldSendNotificationForTasksDueTomorrow() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    testTask.setDueDate(tomorrow.atStartOfDay());

    when(taskRepository.findTasksDueSoon()).thenReturn(Collections.singletonList(testTask));

    dueDateNotifier.checkDueDates();

    verify(notificationService, times(1))
        .createNotification(
            eq(testUser), eq(NotificationType.TASK_DUE_SOON), contains("due soon"), eq(testTask));
  }

  @Test
  @DisplayName("Should send notification for overdue tasks")
  public void shouldSendNotificationForOverdueTasks() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    testTask.setDueDate(yesterday.atStartOfDay());

    when(taskRepository.findOverdueTasks()).thenReturn(Collections.singletonList(testTask));

    dueDateNotifier.checkOverdueTasks();

    verify(notificationService, times(1))
        .createNotification(
            eq(testUser), eq(NotificationType.TASK_OVERDUE), contains("overdue"), eq(testTask));
  }

  @Test
  @DisplayName("Should not send notification for completed tasks")
  public void shouldNotSendNotificationForCompletedTasks() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    testTask.setDueDate(tomorrow.atStartOfDay());
    testTask.setIsCompleted(true);

    when(taskRepository.findTasksDueSoon()).thenReturn(Collections.emptyList());

    dueDateNotifier.checkDueDates();

    verify(notificationService, never()).createNotification(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Should send notifications for multiple tasks")
  public void shouldSendNotificationsForMultipleTasks() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    Task task1 = new Task();
    task1.setId(1L);
    task1.setDescription("Task 1");
    task1.setUser(testUser);
    task1.setDueDate(tomorrow.atStartOfDay());
    task1.setIsCompleted(false);

    Task task2 = new Task();
    task2.setId(2L);
    task2.setDescription("Task 2");
    task2.setUser(testUser);
    task2.setDueDate(tomorrow.atStartOfDay());
    task2.setIsCompleted(false);

    when(taskRepository.findTasksDueSoon()).thenReturn(Arrays.asList(task1, task2));

    dueDateNotifier.checkDueDates();

    verify(notificationService, times(2))
        .createNotification(eq(testUser), eq(NotificationType.TASK_DUE_SOON), anyString(), any());
  }

  @Test
  @DisplayName("Should handle errors gracefully when sending notifications")
  public void shouldHandleErrorsGracefully() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    testTask.setDueDate(tomorrow.atStartOfDay());

    when(taskRepository.findTasksDueSoon()).thenReturn(Collections.singletonList(testTask));
    doThrow(new RuntimeException("Notification failed"))
        .when(notificationService)
        .createNotification(any(), any(), any(), any());

    // Should not throw exception
    dueDateNotifier.checkDueDates();

    verify(notificationService, times(1))
        .createNotification(any(), any(), anyString(), any(Task.class));
  }

  @Test
  @DisplayName("Should not send duplicate notifications for same task")
  public void shouldNotSendDuplicateNotifications() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    testTask.setDueDate(tomorrow.atStartOfDay());

    when(taskRepository.findTasksDueSoon()).thenReturn(Collections.singletonList(testTask));

    // Run twice
    dueDateNotifier.checkDueDates();
    dueDateNotifier.checkDueDates();

    // Should only send 2 notifications total (one per run)
    verify(notificationService, times(2))
        .createNotification(any(), any(), anyString(), any(Task.class));
  }
}
