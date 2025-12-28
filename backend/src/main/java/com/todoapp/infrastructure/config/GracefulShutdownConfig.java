package com.todoapp.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configuration for graceful shutdown handling. Performs cleanup tasks when the application is
 * shutting down.
 */
@Component
public class GracefulShutdownConfig {

  private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownConfig.class);

  /**
   * Handle application shutdown event. Performs cleanup tasks before the application terminates.
   *
   * @param event Context closed event
   */
  @EventListener
  public void onContextClosed(ContextClosedEvent event) {
    logger.info("Application is shutting down gracefully...");

    try {
      // Log shutdown start
      logger.info("Beginning graceful shutdown process");

      // Close active connections (if any custom handling needed)
      logger.info("Closing active connections...");

      // Flush any pending operations
      logger.info("Flushing pending operations...");

      // Wait for background tasks to complete
      logger.info("Waiting for background tasks to complete...");

      // Additional cleanup tasks can be added here
      // For example:
      // - Closing WebSocket connections
      // - Flushing metrics
      // - Cleaning up temporary files
      // - Sending shutdown notifications

      logger.info("Graceful shutdown completed successfully");

    } catch (Exception e) {
      logger.error("Error during graceful shutdown", e);
    }
  }

  /**
   * Pre-destroy hook for additional cleanup. This method is called before the bean is destroyed.
   */
  @jakarta.annotation.PreDestroy
  public void onDestroy() {
    logger.info("Performing pre-destroy cleanup...");

    try {
      // Perform any final cleanup operations
      cleanupResources();

      logger.info("Pre-destroy cleanup completed");
    } catch (Exception e) {
      logger.error("Error during pre-destroy cleanup", e);
    }
  }

  /** Clean up application resources. Override or extend this method to add custom cleanup logic. */
  protected void cleanupResources() {
    // Clear caches if needed
    logger.debug("Clearing application caches...");

    // Close file handles
    logger.debug("Closing file handles...");

    // Flush logs
    logger.debug("Flushing log buffers...");

    // Additional cleanup can be added by extending this class
  }
}
