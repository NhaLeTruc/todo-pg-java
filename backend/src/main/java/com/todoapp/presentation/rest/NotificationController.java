package com.todoapp.presentation.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todoapp.application.dto.NotificationDTO;
import com.todoapp.application.dto.NotificationPreferenceDTO;
import com.todoapp.application.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

  private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  @Operation(
      summary = "Get all unread notifications",
      description = "Retrieves all unread notifications for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Fetching unread notifications for user ID: {}", userId);
    List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
    return ResponseEntity.ok(notifications);
  }

  @GetMapping("/count")
  @Operation(
      summary = "Get unread notification count",
      description = "Retrieves the count of unread notifications for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Long> getUnreadCount(
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Fetching unread notification count for user ID: {}", userId);
    long count = notificationService.getUnreadCount(userId);
    return ResponseEntity.ok(count);
  }

  @PutMapping("/{notificationId}/read")
  @Operation(
      summary = "Mark notification as read",
      description = "Marks a specific notification as read for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<NotificationDTO> markAsRead(
      @PathVariable String notificationId,
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Marking notification ID: {} as read for user ID: {}", notificationId, userId);
    NotificationDTO notification = notificationService.markAsRead(notificationId, userId);
    return ResponseEntity.ok(notification);
  }

  @DeleteMapping("/{notificationId}")
  @Operation(
      summary = "Delete notification",
      description = "Deletes a specific notification for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Void> deleteNotification(
      @PathVariable String notificationId,
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Deleting notification ID: {} for user ID: {}", notificationId, userId);
    notificationService.deleteNotification(notificationId, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/read-all")
  @Operation(
      summary = "Mark all notifications as read",
      description = "Marks all unread notifications as read for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "All notifications marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Void> markAllAsRead(
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Marking all notifications as read for user ID: {}", userId);
    notificationService.markAllAsRead(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/preferences")
  @Operation(
      summary = "Get notification preferences",
      description = "Retrieves notification preferences for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<NotificationPreferenceDTO>> getPreferences(
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info("Fetching notification preferences for user ID: {}", userId);
    List<NotificationPreferenceDTO> preferences = notificationService.getPreferences(userId);
    return ResponseEntity.ok(preferences);
  }

  @PutMapping("/preferences")
  @Operation(
      summary = "Update notification preferences",
      description = "Updates notification preferences for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid preference data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<NotificationPreferenceDTO> updatePreference(
      @RequestBody NotificationPreferenceDTO preferenceDTO,
      @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    logger.info(
        "Updating notification preferences for user ID: {} and type: {}",
        userId,
        preferenceDTO.getNotificationType());
    NotificationPreferenceDTO updatedPreference =
        notificationService.updatePreference(userId, preferenceDTO);
    return ResponseEntity.ok(updatedPreference);
  }
}
