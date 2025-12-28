package com.todoapp.infrastructure.messaging;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.todoapp.application.service.RecurrenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job that processes pending recurrence patterns and generates task instances.
 *
 * <p>This processor runs on a fixed schedule and checks for all recurrence patterns that need to
 * generate new task instances based on their schedules.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecurrenceProcessor {

  private final RecurrenceService recurrenceService;

  /**
   * Process all pending recurrence patterns.
   *
   * <p>Scheduled to run every minute to check for tasks that need to be generated. In production,
   * this could be adjusted based on requirements (e.g., every 5 minutes, every hour, etc.).
   */
  @Scheduled(cron = "${recurrence.processor.cron:0 * * * * *}") // Every minute by default
  public void processRecurrences() {
    log.debug("Starting recurrence pattern processing");

    try {
      int processed = recurrenceService.processPendingRecurrences();

      if (processed > 0) {
        log.info("Processed {} recurrence patterns", processed);
      } else {
        log.debug("No pending recurrence patterns to process");
      }
    } catch (Exception e) {
      log.error("Error processing recurrence patterns: {}", e.getMessage(), e);
    }
  }

  /**
   * Alternative: Process recurrences triggered by a message queue event.
   *
   * <p>This can be used in addition to or instead of the scheduled job, allowing for on-demand
   * processing triggered by events (e.g., when a new recurrence pattern is created).
   *
   * @param message the trigger message (can be empty)
   */
  // Uncomment to enable RabbitMQ-triggered processing
  /*
  @RabbitListener(queues = RabbitMQConfig.RECURRENCE_QUEUE)
  public void processRecurrenceMessage(String message) {
    log.info("Received recurrence processing trigger: {}", message);
    processRecurrences();
  }
  */
}
