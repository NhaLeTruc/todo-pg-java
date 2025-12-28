package com.todoapp.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.FileAttachment;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.model.VirusScanStatus;

public class FileAttachmentTest {

  private Task task;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    task = new Task();
    task.setId(1L);
    task.setDescription("Test task");
    task.setUser(user);
  }

  @Test
  @DisplayName("Should create file attachment with valid data")
  public void shouldCreateFileAttachmentWithValidData() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("document.pdf");
    attachment.setFileSize(1024L);
    attachment.setMimeType("application/pdf");
    attachment.setStorageKey("uploads/123/document.pdf");
    attachment.setScanStatus(VirusScanStatus.PENDING);

    assertThat(attachment.getFileName()).isEqualTo("document.pdf");
    assertThat(attachment.getFileSize()).isEqualTo(1024L);
    assertThat(attachment.getMimeType()).isEqualTo("application/pdf");
    assertThat(attachment.getStorageKey()).isEqualTo("uploads/123/document.pdf");
    assertThat(attachment.getScanStatus()).isEqualTo(VirusScanStatus.PENDING);
    assertThat(attachment.getTask()).isEqualTo(task);
    assertThat(attachment.getUser()).isEqualTo(user);
  }

  @Test
  @DisplayName("Should reject file attachment with size exceeding 25MB")
  public void shouldRejectFileAttachmentExceedingMaxSize() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("large-file.zip");
    attachment.setMimeType("application/zip");
    attachment.setStorageKey("uploads/123/large-file.zip");

    long maxSize = 25 * 1024 * 1024; // 25MB
    long oversizedFile = maxSize + 1;

    assertThatThrownBy(() -> attachment.setFileSize(oversizedFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File size exceeds maximum allowed size");
  }

  @Test
  @DisplayName("Should accept file attachment at exactly 25MB")
  public void shouldAcceptFileAttachmentAtMaxSize() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("max-size-file.zip");
    attachment.setMimeType("application/zip");
    attachment.setStorageKey("uploads/123/max-size-file.zip");

    long maxSize = 25 * 1024 * 1024; // 25MB

    attachment.setFileSize(maxSize);

    assertThat(attachment.getFileSize()).isEqualTo(maxSize);
  }

  @Test
  @DisplayName("Should reject file attachment with zero or negative size")
  public void shouldRejectFileAttachmentWithInvalidSize() {
    FileAttachment attachment = new FileAttachment();

    assertThatThrownBy(() -> attachment.setFileSize(0L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File size must be greater than 0");

    assertThatThrownBy(() -> attachment.setFileSize(-1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File size must be greater than 0");
  }

  @Test
  @DisplayName("Should update scan status from PENDING to CLEAN")
  public void shouldUpdateScanStatusToClean() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("safe-document.pdf");
    attachment.setFileSize(1024L);
    attachment.setMimeType("application/pdf");
    attachment.setStorageKey("uploads/123/safe-document.pdf");
    attachment.setScanStatus(VirusScanStatus.PENDING);

    attachment.setScanStatus(VirusScanStatus.CLEAN);

    assertThat(attachment.getScanStatus()).isEqualTo(VirusScanStatus.CLEAN);
  }

  @Test
  @DisplayName("Should update scan status from PENDING to INFECTED")
  public void shouldUpdateScanStatusToInfected() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("malware.exe");
    attachment.setFileSize(2048L);
    attachment.setMimeType("application/x-msdownload");
    attachment.setStorageKey("uploads/123/malware.exe");
    attachment.setScanStatus(VirusScanStatus.PENDING);

    attachment.setScanStatus(VirusScanStatus.INFECTED);

    assertThat(attachment.getScanStatus()).isEqualTo(VirusScanStatus.INFECTED);
  }

  @Test
  @DisplayName("Should reject null fileName")
  public void shouldRejectNullFileName() {
    FileAttachment attachment = new FileAttachment();

    assertThatThrownBy(() -> attachment.setFileName(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File name cannot be null or empty");
  }

  @Test
  @DisplayName("Should reject empty fileName")
  public void shouldRejectEmptyFileName() {
    FileAttachment attachment = new FileAttachment();

    assertThatThrownBy(() -> attachment.setFileName(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File name cannot be null or empty");
  }

  @Test
  @DisplayName("Should reject blank fileName")
  public void shouldRejectBlankFileName() {
    FileAttachment attachment = new FileAttachment();

    assertThatThrownBy(() -> attachment.setFileName("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File name cannot be null or empty");
  }

  @Test
  @DisplayName("Should reject null storageKey")
  public void shouldRejectNullStorageKey() {
    FileAttachment attachment = new FileAttachment();

    assertThatThrownBy(() -> attachment.setStorageKey(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Storage key cannot be null or empty");
  }

  @Test
  @DisplayName("Should sanitize fileName with special characters")
  public void shouldSanitizeFileNameWithSpecialCharacters() {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);

    String unsafeFileName = "../../../etc/passwd";
    attachment.setFileName(unsafeFileName);

    // File name should be sanitized to prevent path traversal
    assertThat(attachment.getFileName()).doesNotContain("..");
    assertThat(attachment.getFileName()).doesNotContain("/");
  }

  @Test
  @DisplayName("Should track upload timestamp")
  public void shouldTrackUploadTimestamp() throws InterruptedException {
    FileAttachment attachment = new FileAttachment();
    attachment.setTask(task);
    attachment.setUser(user);
    attachment.setFileName("test.pdf");
    attachment.setFileSize(1024L);
    attachment.setMimeType("application/pdf");
    attachment.setStorageKey("uploads/123/test.pdf");
    attachment.setScanStatus(VirusScanStatus.PENDING);

    // Simulate database save which would set timestamps
    attachment.setCreatedAt(java.time.LocalDateTime.now());

    assertThat(attachment.getCreatedAt()).isNotNull();
  }
}
