package com.todoapp.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.todoapp.domain.model.Notification;

@Service
public class EmailNotifier {

  private static final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

  private static final String EMAIL_QUEUE = "email-notification-queue";

  /**
   * Send notification email to user.
   *
   * <p>Note: This is a simulated implementation. In production, this would integrate with an email
   * service like SendGrid, AWS SES, or SMTP server.
   *
   * @param notification The notification to send
   */
  public void sendNotificationEmail(Notification notification) {
    try {
      String userEmail = notification.getUser().getEmail();
      String subject = getEmailSubject(notification);
      String body = getEmailBody(notification);

      logger.info(
          "Sending email notification to: {}, subject: {}, type: {}",
          userEmail,
          subject,
          notification.getType());

      // In production, implement actual email sending logic here
      // Example: emailService.send(userEmail, subject, body);

      // For now, just log the email details
      logger.debug("Email body: {}", body);

    } catch (Exception e) {
      logger.error("Failed to send email notification", e);
      throw new RuntimeException("Email sending failed", e);
    }
  }

  /**
   * Process email notifications from RabbitMQ queue. This method is a consumer that processes email
   * notifications asynchronously.
   *
   * @param notificationId The notification ID
   */
  @RabbitListener(queues = EMAIL_QUEUE)
  public void processEmailNotification(String notificationId) {
    try {
      logger.info("Processing email notification from queue: {}", notificationId);

      // In production:
      // 1. Fetch notification from database
      // 2. Send email via email service
      // 3. Update notification status

      logger.info("Email notification processed: {}", notificationId);

    } catch (Exception e) {
      logger.error("Error processing email notification: {}", notificationId, e);
    }
  }

  /**
   * Get email subject based on notification type.
   *
   * @param notification The notification
   * @return Email subject
   */
  private String getEmailSubject(Notification notification) {
    return switch (notification.getType()) {
      case TASK_DUE_SOON -> "Task Due Soon: " + getTaskDescription(notification);
      case TASK_OVERDUE -> "Task Overdue: " + getTaskDescription(notification);
      case TASK_SHARED -> "Task Shared With You: " + getTaskDescription(notification);
      case TASK_COMMENTED -> "New Comment on Task: " + getTaskDescription(notification);
      case TASK_MENTIONED -> "You Were Mentioned in a Comment";
      case TASK_ASSIGNED -> "Task Assigned to You: " + getTaskDescription(notification);
      case REMINDER -> "Reminder: " + getTaskDescription(notification);
    };
  }

  /**
   * Get email body based on notification.
   *
   * @param notification The notification
   * @return Email body
   */
  private String getEmailBody(Notification notification) {
    StringBuilder body = new StringBuilder();
    body.append("Hello,\n\n");
    body.append(notification.getMessage());
    body.append("\n\n");

    if (notification.getRelatedTask() != null) {
      body.append("Task: ").append(notification.getRelatedTask().getDescription()).append("\n");
      if (notification.getRelatedTask().getDueDate() != null) {
        body.append("Due Date: ").append(notification.getRelatedTask().getDueDate()).append("\n");
      }
      body.append("\n");
    }

    body.append("Click here to view details in the application.\n\n");
    body.append("Best regards,\n");
    body.append("TODO App Team");

    return body.toString();
  }

  /**
   * Get task description from notification.
   *
   * @param notification The notification
   * @return Task description or empty string
   */
  private String getTaskDescription(Notification notification) {
    if (notification.getRelatedTask() != null) {
      return notification.getRelatedTask().getDescription();
    }
    return "";
  }
}
