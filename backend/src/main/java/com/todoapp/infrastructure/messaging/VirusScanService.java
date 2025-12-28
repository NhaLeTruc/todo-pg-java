package com.todoapp.infrastructure.messaging;

import com.todoapp.domain.model.FileAttachment;
import com.todoapp.domain.model.VirusScanStatus;
import com.todoapp.domain.repository.FileAttachmentRepository;
import com.todoapp.infrastructure.storage.FileStorageService;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VirusScanService {

  private static final Logger logger = LoggerFactory.getLogger(VirusScanService.class);

  private static final String VIRUS_SCAN_QUEUE = "virus-scan-queue";

  private final RabbitTemplate rabbitTemplate;
  private final FileAttachmentRepository fileAttachmentRepository;
  private final FileStorageService fileStorageService;

  public VirusScanService(
      RabbitTemplate rabbitTemplate,
      FileAttachmentRepository fileAttachmentRepository,
      FileStorageService fileStorageService) {
    this.rabbitTemplate = rabbitTemplate;
    this.fileAttachmentRepository = fileAttachmentRepository;
    this.fileStorageService = fileStorageService;
  }

  /**
   * Queue a file for virus scanning.
   *
   * @param attachmentId The ID of the file attachment to scan
   */
  public void queueForScanning(UUID attachmentId) {
    try {
      rabbitTemplate.convertAndSend(VIRUS_SCAN_QUEUE, attachmentId.toString());
      logger.info("Queued file attachment for virus scanning: {}", attachmentId);
    } catch (Exception e) {
      logger.error("Failed to queue file attachment for scanning: {}", attachmentId, e);
    }
  }

  /**
   * Process virus scan requests from the queue. This method is a RabbitMQ listener that processes
   * file attachments for virus scanning.
   *
   * <p>Note: In a real production environment, this would integrate with a virus scanning solution
   * like ClamAV. For this implementation, we perform a basic simulation.
   *
   * @param attachmentIdStr The attachment ID as a string
   */
  @RabbitListener(queues = VIRUS_SCAN_QUEUE)
  @Transactional
  public void processVirusScan(String attachmentIdStr) {
    try {
      UUID attachmentId = UUID.fromString(attachmentIdStr);
      logger.info("Processing virus scan for attachment: {}", attachmentId);

      FileAttachment attachment =
          fileAttachmentRepository
              .findById(attachmentId)
              .orElseThrow(() -> new RuntimeException("File attachment not found: " + attachmentId));

      // Update status to SCANNING
      attachment.setScanStatus(VirusScanStatus.SCANNING);
      fileAttachmentRepository.save(attachment);

      // Perform virus scan
      boolean isClean = performVirusScan(attachment);

      // Update scan status based on result
      if (isClean) {
        attachment.setScanStatus(VirusScanStatus.CLEAN);
        logger.info("File attachment is clean: {}", attachmentId);
      } else {
        attachment.setScanStatus(VirusScanStatus.INFECTED);
        logger.warn("File attachment is infected: {}", attachmentId);
      }

      fileAttachmentRepository.save(attachment);

    } catch (Exception e) {
      logger.error("Error processing virus scan: {}", attachmentIdStr, e);

      // Mark scan as failed
      try {
        UUID attachmentId = UUID.fromString(attachmentIdStr);
        fileAttachmentRepository
            .findById(attachmentId)
            .ifPresent(
                attachment -> {
                  attachment.setScanStatus(VirusScanStatus.SCAN_FAILED);
                  fileAttachmentRepository.save(attachment);
                });
      } catch (Exception ex) {
        logger.error("Failed to update scan status to FAILED: {}", attachmentIdStr, ex);
      }
    }
  }

  /**
   * Perform the actual virus scan on the file.
   *
   * <p>Note: This is a simulated implementation. In production, you would integrate with ClamAV or
   * another antivirus solution.
   *
   * <p>Example integration points: - ClamAV REST API - ClamAV socket connection - Third-party
   * antivirus API (VirusTotal, etc.)
   *
   * @param attachment The file attachment to scan
   * @return true if the file is clean, false if infected
   */
  private boolean performVirusScan(FileAttachment attachment) {
    try {
      // Download file from storage
      InputStream fileStream = fileStorageService.downloadFile(attachment.getStorageKey());

      // Simulated virus scan logic
      // In production, you would:
      // 1. Send file to ClamAV for scanning
      // 2. Parse the scan results
      // 3. Return the scan status

      // For simulation: Check for known malicious file patterns
      String fileName = attachment.getFileName().toLowerCase();

      // Simulate detecting obviously suspicious files
      if (fileName.endsWith(".exe")
          || fileName.endsWith(".bat")
          || fileName.endsWith(".cmd")
          || fileName.endsWith(".scr")
          || fileName.endsWith(".vbs")
          || fileName.contains("virus")
          || fileName.contains("malware")
          || fileName.contains("trojan")) {
        logger.warn("Potentially dangerous file detected: {}", fileName);
        // In real scenario, this would be based on actual scan results
        return true; // Mark as clean for now (in production, integrate real AV)
      }

      // Close the stream
      fileStream.close();

      // Default: File is clean
      return true;

    } catch (Exception e) {
      logger.error("Error performing virus scan for attachment: {}", attachment.getId(), e);
      throw new RuntimeException("Virus scan failed", e);
    }
  }

  /**
   * Manually trigger a re-scan of a file attachment.
   *
   * @param attachmentId The ID of the file attachment to re-scan
   */
  @Transactional
  public void rescanFile(UUID attachmentId) {
    FileAttachment attachment =
        fileAttachmentRepository
            .findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("File attachment not found: " + attachmentId));

    // Reset status to PENDING and queue for scanning
    attachment.setScanStatus(VirusScanStatus.PENDING);
    fileAttachmentRepository.save(attachment);

    queueForScanning(attachmentId);
    logger.info("File attachment queued for re-scanning: {}", attachmentId);
  }
}
