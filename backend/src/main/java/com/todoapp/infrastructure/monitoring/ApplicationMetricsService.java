package com.todoapp.infrastructure.monitoring;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Service for recording application-specific metrics. Provides convenient methods for tracking
 * business operations and events.
 */
@Service
public class ApplicationMetricsService {

  private final MeterRegistry meterRegistry;

  // Task-related counters
  private final Counter taskCreatedCounter;
  private final Counter taskCompletedCounter;
  private final Counter taskDeletedCounter;

  // User-related counters
  private final Counter userRegistrationCounter;
  private final Counter userLoginCounter;
  private final Counter userLoginFailureCounter;

  // Notification-related counters
  private final Counter notificationSentCounter;
  private final Counter notificationFailureCounter;

  // File-related counters
  private final Counter fileUploadCounter;
  private final Counter fileUploadFailureCounter;

  // Comment-related counters
  private final Counter commentCreatedCounter;

  // Timers for operations
  private final Timer taskOperationTimer;
  private final Timer databaseOperationTimer;
  private final Timer emailSendTimer;
  private final Timer fileUploadTimer;

  public ApplicationMetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    // Initialize task counters
    this.taskCreatedCounter =
        Counter.builder("tasks.created").description("Total tasks created").register(meterRegistry);

    this.taskCompletedCounter =
        Counter.builder("tasks.completed")
            .description("Total tasks completed")
            .register(meterRegistry);

    this.taskDeletedCounter =
        Counter.builder("tasks.deleted").description("Total tasks deleted").register(meterRegistry);

    // Initialize user counters
    this.userRegistrationCounter =
        Counter.builder("users.registered")
            .description("Total user registrations")
            .register(meterRegistry);

    this.userLoginCounter =
        Counter.builder("users.login.success")
            .description("Successful user logins")
            .register(meterRegistry);

    this.userLoginFailureCounter =
        Counter.builder("users.login.failure")
            .description("Failed user login attempts")
            .register(meterRegistry);

    // Initialize notification counters
    this.notificationSentCounter =
        Counter.builder("notifications.sent")
            .description("Total notifications sent")
            .register(meterRegistry);

    this.notificationFailureCounter =
        Counter.builder("notifications.failed")
            .description("Failed notification attempts")
            .register(meterRegistry);

    // Initialize file counters
    this.fileUploadCounter =
        Counter.builder("files.uploaded")
            .description("Total files uploaded")
            .register(meterRegistry);

    this.fileUploadFailureCounter =
        Counter.builder("files.upload.failed")
            .description("Failed file uploads")
            .register(meterRegistry);

    // Initialize comment counters
    this.commentCreatedCounter =
        Counter.builder("comments.created")
            .description("Total comments created")
            .register(meterRegistry);

    // Initialize timers
    this.taskOperationTimer =
        Timer.builder("tasks.operation.time")
            .description("Time to complete task operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

    this.databaseOperationTimer =
        Timer.builder("database.operation.time")
            .description("Time to complete database operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

    this.emailSendTimer =
        Timer.builder("email.send.time")
            .description("Time to send email notifications")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

    this.fileUploadTimer =
        Timer.builder("files.upload.time")
            .description("Time to upload files")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
  }

  // Task metrics
  public void recordTaskCreated() {
    taskCreatedCounter.increment();
  }

  public void recordTaskCompleted() {
    taskCompletedCounter.increment();
  }

  public void recordTaskDeleted() {
    taskDeletedCounter.increment();
  }

  // User metrics
  public void recordUserRegistration() {
    userRegistrationCounter.increment();
  }

  public void recordUserLogin() {
    userLoginCounter.increment();
  }

  public void recordUserLoginFailure() {
    userLoginFailureCounter.increment();
  }

  // Notification metrics
  public void recordNotificationSent(String notificationType) {
    notificationSentCounter.increment();
    meterRegistry.counter("notifications.sent.by.type", "type", notificationType).increment();
  }

  public void recordNotificationFailure(String notificationType) {
    notificationFailureCounter.increment();
    meterRegistry.counter("notifications.failed.by.type", "type", notificationType).increment();
  }

  // File metrics
  public void recordFileUpload(long fileSizeBytes) {
    fileUploadCounter.increment();
    meterRegistry.summary("files.size.bytes", "operation", "upload").record(fileSizeBytes);
  }

  public void recordFileUploadFailure() {
    fileUploadFailureCounter.increment();
  }

  // Comment metrics
  public void recordCommentCreated() {
    commentCreatedCounter.increment();
  }

  // Timer recording methods
  public Timer.Sample startTaskOperationTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordTaskOperation(Timer.Sample sample) {
    sample.stop(taskOperationTimer);
  }

  public Timer.Sample startDatabaseOperationTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordDatabaseOperation(Timer.Sample sample) {
    sample.stop(databaseOperationTimer);
  }

  public Timer.Sample startEmailSendTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordEmailSend(Timer.Sample sample) {
    sample.stop(emailSendTimer);
  }

  public Timer.Sample startFileUploadTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordFileUpload(Timer.Sample sample) {
    sample.stop(fileUploadTimer);
  }

  // Custom gauges
  public void recordActiveUsers(int count) {
    meterRegistry.gauge("users.active.count", count);
  }

  public void recordPendingTasks(int count) {
    meterRegistry.gauge("tasks.pending.count", count);
  }

  public void recordCacheSize(String cacheName, long size) {
    meterRegistry.gauge(
        "cache.size",
        java.util.Collections.singletonList(
            io.micrometer.core.instrument.Tag.of("cache", cacheName)),
        size);
  }
}
