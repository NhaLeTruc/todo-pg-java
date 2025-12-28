package com.todoapp.presentation.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.todoapp.application.dto.TaskUpdateMessage;

@Component
public class TaskWebSocketHandler {

  private final SimpMessagingTemplate messagingTemplate;

  public TaskWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Send task update to a specific user
   *
   * @param userId User ID to send the message to
   * @param message Task update message
   */
  public void sendTaskUpdateToUser(Long userId, TaskUpdateMessage message) {
    messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/task-updates", message);
  }

  /**
   * Send task update to multiple users (e.g., when a task is shared)
   *
   * @param userIds List of user IDs
   * @param message Task update message
   */
  public void sendTaskUpdateToUsers(Iterable<Long> userIds, TaskUpdateMessage message) {
    for (Long userId : userIds) {
      sendTaskUpdateToUser(userId, message);
    }
  }

  /**
   * Broadcast task update to all connected users (use sparingly)
   *
   * @param message Task update message
   */
  public void broadcastTaskUpdate(TaskUpdateMessage message) {
    messagingTemplate.convertAndSend("/topic/task-updates", message);
  }
}
