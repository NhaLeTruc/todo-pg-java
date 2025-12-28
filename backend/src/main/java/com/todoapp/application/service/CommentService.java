package com.todoapp.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.todoapp.application.dto.CommentDTO;
import com.todoapp.domain.model.Comment;
import com.todoapp.domain.model.NotificationType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CommentRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CommentService {

  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

  public CommentService(
      CommentRepository commentRepository,
      TaskRepository taskRepository,
      UserRepository userRepository,
      NotificationService notificationService) {
    this.commentRepository = commentRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  public CommentDTO addComment(Long taskId, CommentDTO commentDTO, Long userId) {
    logger.debug("Adding comment to task ID: {} by user ID: {}", taskId, userId);

    // Validate content
    if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
      throw new IllegalArgumentException("Comment content cannot be empty");
    }

    // Find user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    // Find task and verify user has access
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    // Verify user owns the task (for now, only task owner can comment)
    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    // Create comment
    Comment comment = new Comment();
    comment.setTask(task);
    comment.setAuthor(user);
    comment.setContent(commentDTO.getContent().trim());
    comment.setIsEdited(false);

    Comment savedComment = commentRepository.save(comment);
    logger.info("Comment ID: {} created for task ID: {}", savedComment.getId(), taskId);

    // Process mentions and send notifications
    processMentions(savedComment, user, task);

    // Notify task owner if comment author is not the owner
    if (!task.getUser().getId().equals(userId)) {
      String message =
          String.format("%s commented on your task '%s'", user.getEmail(), task.getDescription());
      notificationService.createNotification(
          task.getUser(), NotificationType.TASK_COMMENTED, message, task);
    }

    return toDTO(savedComment);
  }

  public List<CommentDTO> getCommentsByTaskId(Long taskId, Long userId) {
    logger.debug("Fetching comments for task ID: {} by user ID: {}", taskId, userId);

    // Verify task exists and user has access
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

    if (!task.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("User does not have access to this task");
    }

    List<Comment> comments = commentRepository.findByTaskIdAndTaskUserId(taskId, userId);
    logger.debug("Found {} comments for task ID: {}", comments.size(), taskId);

    return comments.stream().map(this::toDTO).collect(Collectors.toList());
  }

  public CommentDTO updateComment(Long commentId, CommentDTO commentDTO, Long userId) {
    logger.debug("Updating comment ID: {} by user ID: {}", commentId, userId);

    // Validate content
    if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
      throw new IllegalArgumentException("Comment content cannot be empty");
    }

    // Find comment
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

    // Verify user is the author
    if (!comment.getAuthor().getId().equals(userId)) {
      throw new IllegalArgumentException("Only the comment author can update the comment");
    }

    // Update comment
    comment.setContent(commentDTO.getContent().trim());
    comment.setIsEdited(true);

    Comment updatedComment = commentRepository.save(comment);
    logger.info("Comment ID: {} updated successfully", commentId);

    return toDTO(updatedComment);
  }

  public void deleteComment(Long commentId, Long userId) {
    logger.debug("Deleting comment ID: {} by user ID: {}", commentId, userId);

    // Find comment
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

    // Verify user is the author
    if (!comment.getAuthor().getId().equals(userId)) {
      throw new IllegalArgumentException("Only the comment author can delete the comment");
    }

    commentRepository.delete(comment);
    logger.info("Comment ID: {} deleted successfully", commentId);
  }

  private void processMentions(Comment comment, User author, Task task) {
    String content = comment.getContent();
    Matcher matcher = MENTION_PATTERN.matcher(content);
    Set<String> mentionedUsernames = new HashSet<>();

    while (matcher.find()) {
      String username = matcher.group(1);
      mentionedUsernames.add(username);
    }

    for (String username : mentionedUsernames) {
      userRepository
          .findByEmail(username + "@example.com")
          .ifPresent(
              mentionedUser -> {
                // Don't notify the author of the comment
                if (!mentionedUser.getId().equals(author.getId())) {
                  String message =
                      String.format(
                          "%s mentioned you in a comment on task '%s': %s",
                          author.getEmail(), task.getDescription(), truncateContent(content, 50));
                  notificationService.createNotification(
                      mentionedUser, NotificationType.TASK_MENTIONED, message, task);
                }
              });
    }
  }

  private String truncateContent(String content, int maxLength) {
    if (content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...";
  }

  private CommentDTO toDTO(Comment comment) {
    CommentDTO dto = new CommentDTO();
    dto.setId(comment.getId());
    dto.setTaskId(comment.getTask().getId());
    dto.setAuthorId(comment.getAuthor().getId());
    dto.setAuthorEmail(comment.getAuthor().getEmail());
    dto.setContent(comment.getContent());
    dto.setIsEdited(comment.getIsEdited());
    dto.setCreatedAt(comment.getCreatedAt());
    dto.setUpdatedAt(comment.getUpdatedAt());
    return dto;
  }
}
