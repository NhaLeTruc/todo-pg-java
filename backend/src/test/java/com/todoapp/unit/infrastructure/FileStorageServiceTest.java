package com.todoapp.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.infrastructure.storage.FileStorageService;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

  @Mock private MinioClient minioClient;

  @InjectMocks private FileStorageService fileStorageService;

  private String bucketName;

  @BeforeEach
  public void setUp() {
    bucketName = "task-attachments";
    // Using reflection to set bucket name or assuming it's injected
    try {
      java.lang.reflect.Field bucketField = FileStorageService.class.getDeclaredField("bucketName");
      bucketField.setAccessible(true);
      bucketField.set(fileStorageService, bucketName);
    } catch (Exception e) {
      // If reflection fails, the service should have @Value injected bucket name
    }
  }

  @Test
  @DisplayName("Should upload file successfully to MinIO")
  public void shouldUploadFileSuccessfully() throws Exception {
    String fileName = "test-document.pdf";
    byte[] fileContent = "Test file content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "application/pdf";
    long fileSize = fileContent.length;

    String storageKey = fileStorageService.uploadFile(fileName, inputStream, contentType, fileSize);

    assertThat(storageKey).isNotNull();
    assertThat(storageKey).contains(fileName);

    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Should generate unique storage key for each upload")
  public void shouldGenerateUniqueStorageKey() throws Exception {
    String fileName = "duplicate.pdf";
    byte[] fileContent = "Content".getBytes();
    InputStream inputStream1 = new ByteArrayInputStream(fileContent);
    InputStream inputStream2 = new ByteArrayInputStream(fileContent);
    String contentType = "application/pdf";
    long fileSize = fileContent.length;

    String storageKey1 =
        fileStorageService.uploadFile(fileName, inputStream1, contentType, fileSize);
    String storageKey2 =
        fileStorageService.uploadFile(fileName, inputStream2, contentType, fileSize);

    assertThat(storageKey1).isNotEqualTo(storageKey2);
    assertThat(storageKey1).contains(fileName);
    assertThat(storageKey2).contains(fileName);

    verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Should sanitize file name with special characters")
  public void shouldSanitizeFileNameWithSpecialCharacters() throws Exception {
    String unsafeFileName = "../../../etc/passwd";
    byte[] fileContent = "Malicious content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "text/plain";
    long fileSize = fileContent.length;

    String storageKey =
        fileStorageService.uploadFile(unsafeFileName, inputStream, contentType, fileSize);

    assertThat(storageKey).doesNotContain("..");
    assertThat(storageKey).doesNotContain("/etc/");

    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Should reject upload with null input stream")
  public void shouldRejectUploadWithNullInputStream() {
    String fileName = "test.pdf";
    String contentType = "application/pdf";
    long fileSize = 1024L;

    assertThatThrownBy(() -> fileStorageService.uploadFile(fileName, null, contentType, fileSize))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Input stream cannot be null");
  }

  @Test
  @DisplayName("Should reject upload with null or empty file name")
  public void shouldRejectUploadWithInvalidFileName() {
    byte[] fileContent = "Content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "application/pdf";
    long fileSize = fileContent.length;

    assertThatThrownBy(
            () -> fileStorageService.uploadFile(null, inputStream, contentType, fileSize))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File name cannot be null or empty");

    InputStream inputStream2 = new ByteArrayInputStream(fileContent);
    assertThatThrownBy(() -> fileStorageService.uploadFile("", inputStream2, contentType, fileSize))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File name cannot be null or empty");
  }

  @Test
  @DisplayName("Should download file successfully from MinIO")
  public void shouldDownloadFileSuccessfully() throws Exception {
    String storageKey = "uploads/123/test-document.pdf";
    byte[] fileContent = "Downloaded content".getBytes();
    InputStream mockInputStream = new ByteArrayInputStream(fileContent);

    when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockInputStream);

    InputStream result = fileStorageService.downloadFile(storageKey);

    assertThat(result).isNotNull();
    byte[] downloadedContent = result.readAllBytes();
    assertThat(downloadedContent).isEqualTo(fileContent);

    verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
  }

  @Test
  @DisplayName("Should delete file successfully from MinIO")
  public void shouldDeleteFileSuccessfully() throws Exception {
    String storageKey = "uploads/123/test-document.pdf";

    fileStorageService.deleteFile(storageKey);

    verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  @DisplayName("Should handle MinIO upload errors gracefully")
  public void shouldHandleMinIOUploadErrors() throws Exception {
    String fileName = "error-file.pdf";
    byte[] fileContent = "Content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "application/pdf";
    long fileSize = fileContent.length;

    doThrow(new RuntimeException("MinIO connection error"))
        .when(minioClient)
        .putObject(any(PutObjectArgs.class));

    assertThatThrownBy(
            () -> fileStorageService.uploadFile(fileName, inputStream, contentType, fileSize))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to upload file");
  }

  @Test
  @DisplayName("Should handle MinIO download errors gracefully")
  public void shouldHandleMinIODownloadErrors() throws Exception {
    String storageKey = "uploads/123/missing-file.pdf";

    when(minioClient.getObject(any(GetObjectArgs.class)))
        .thenThrow(new RuntimeException("Object not found"));

    assertThatThrownBy(() -> fileStorageService.downloadFile(storageKey))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to download file");
  }

  @Test
  @DisplayName("Should handle MinIO delete errors gracefully")
  public void shouldHandleMinIODeleteErrors() throws Exception {
    String storageKey = "uploads/123/error-file.pdf";

    doThrow(new RuntimeException("MinIO error"))
        .when(minioClient)
        .removeObject(any(RemoveObjectArgs.class));

    assertThatThrownBy(() -> fileStorageService.deleteFile(storageKey))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to delete file");
  }

  @Test
  @DisplayName("Should preserve file extension in storage key")
  public void shouldPreserveFileExtensionInStorageKey() throws Exception {
    String fileName = "important-report.xlsx";
    byte[] fileContent = "Excel content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    long fileSize = fileContent.length;

    String storageKey = fileStorageService.uploadFile(fileName, inputStream, contentType, fileSize);

    assertThat(storageKey).endsWith(".xlsx");

    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Should handle file upload with zero size")
  public void shouldRejectFileUploadWithZeroSize() {
    String fileName = "empty.txt";
    byte[] fileContent = new byte[0];
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "text/plain";
    long fileSize = 0L;

    assertThatThrownBy(
            () -> fileStorageService.uploadFile(fileName, inputStream, contentType, fileSize))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File size must be greater than 0");
  }

  @Test
  @DisplayName("Should reject file upload exceeding maximum size")
  public void shouldRejectFileUploadExceedingMaxSize() {
    String fileName = "huge-file.zip";
    byte[] fileContent = "Content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(fileContent);
    String contentType = "application/zip";
    long maxSize = 25 * 1024 * 1024; // 25MB
    long oversizedFile = maxSize + 1;

    assertThatThrownBy(
            () -> fileStorageService.uploadFile(fileName, inputStream, contentType, oversizedFile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("File size exceeds maximum allowed size");
  }
}
