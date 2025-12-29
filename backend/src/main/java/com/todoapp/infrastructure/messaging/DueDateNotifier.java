package com.todoapp.infrastructure.messaging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.service.NotificationService;
import com.todoapp.domain.model.NotificationType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.repository.TaskRepository;

@Service
public class DueDateNotifier {

  private static final Logger logger = LoggerFactory.getLogger(DueDateNotifier.class);

  private final TaskRepository taskRepository;
  private final NotificationService notificationService;

  public DueDateNotifier(TaskRepository taskRepository, NotificationService notificationService) {
    this.taskRepository = taskRepository;
    this.notificationService = notificationService;
  }

  /** Check for tasks due within 24 hours and send notifications. Runs every hour. */
  @Scheduled(cron = "0 0 * * * *") // Every hour
  @Transactional
  public void checkDueDates() {
    try {
      logger.info("Starting due date check for tasks due soon");

      List<Task> tasksDueSoon = taskRepository.findTasksDueSoon();

      for (Task task : tasksDueSoon) {
        try {
          String message =
              String.format(
                  "Task '%s' is due soon on %s",
                  task.getDescription(), formatDate(task.getDueDate()));

          notificationService.createNotification(
              task.getUser(), NotificationType.TASK_DUE_SOON, message, task);

          logger.debug(
              "Due soon notification sent for task: {} to user: {}",
              task.getId(),
              task.getUser().getId());

        } catch (Exception e) {
          logger.error("Failed to send due soon notification for task: {}", task.getId(), e);
        }
      }

      logger.info("Due date check completed. Notifications sent: {}", tasksDueSoon.size());

    } catch (Exception e) {
      logger.error("Error during due date check", e);
    }
  }

  /** Check for overdue tasks and send notifications. Runs every day at 9:00 AM. */
  @Scheduled(cron = "0 0 9 * * *") // Daily at 9:00 AM
  @Transactional
  public void checkOverdueTasks() {
    try {
      logger.info("Starting overdue task check");

      List<Task> overdueTasks = taskRepository.findOverdueTasks();

      for (Task task : overdueTasks) {
        try {
          String message =
              String.format(
                  "Task '%s' is overdue since %s",
                  task.getDescription(), formatDate(task.getDueDate()));

          notificationService.createNotification(
              task.getUser(), NotificationType.TASK_OVERDUE, message, task);

          logger.debug(
              "Overdue notification sent for task: {} to user: {}",
              task.getId(),
              task.getUser().getId());

        } catch (Exception e) {
          logger.error("Failed to send overdue notification for task: {}", task.getId(), e);
        }
      }

      logger.info("Overdue task check completed. Notifications sent: {}", overdueTasks.size());

    } catch (Exception e) {
      logger.error("Error during overdue task check", e);
    }
  }

  /**
   * Format date for display in notification message.
   *
   * @param date The date to format
   * @return Formatted date string
   */
  private String formatDate(LocalDateTime date) {
    if (date == null) {
      return "";
    }
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
  }
}
