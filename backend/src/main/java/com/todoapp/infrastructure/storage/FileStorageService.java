package com.todoapp.infrastructure.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.*;
import io.minio.errors.*;

@Service
public class FileStorageService {

  private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

  private final MinioClient minioClient;

  @Value("${minio.bucket-name:task-attachments}")
  private String bucketName;

  // Maximum file size: 25MB
  private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

  public FileStorageService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  /**
   * Upload a file to MinIO storage.
   *
   * @param fileName The original file name
   * @param inputStream The file content as input stream
   * @param contentType The MIME type of the file
   * @param fileSize The size of the file in bytes
   * @return The storage key (object name) of the uploaded file
   * @throws RuntimeException if upload fails
   */
  public String uploadFile(
      String fileName, InputStream inputStream, String contentType, long fileSize) {
    validateUploadParameters(fileName, inputStream, fileSize);

    // Generate unique storage key
    String storageKey = generateStorageKey(fileName);

    try {
      // Ensure bucket exists
      ensureBucketExists();

      // Upload file to MinIO
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(storageKey).stream(
                  inputStream, fileSize, -1)
              .contentType(contentType)
              .build());

      logger.info("File uploaded successfully: {}", storageKey);
      return storageKey;

    } catch (ErrorResponseException
        | InsufficientDataException
        | InternalException
        | InvalidKeyException
        | InvalidResponseException
        | IOException
        | NoSuchAlgorithmException
        | ServerException
        | XmlParserException e) {
      logger.error("Failed to upload file: {}", fileName, e);
      throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
    }
  }

  /**
   * Download a file from MinIO storage.
   *
   * @param storageKey The storage key (object name) of the file
   * @return InputStream of the file content
   * @throws RuntimeException if download fails
   */
  public InputStream downloadFile(String storageKey) {
    try {
      InputStream stream =
          minioClient.getObject(
              GetObjectArgs.builder().bucket(bucketName).object(storageKey).build());

      logger.info("File downloaded successfully: {}", storageKey);
      return stream;

    } catch (ErrorResponseException
        | InsufficientDataException
        | InternalException
        | InvalidKeyException
        | InvalidResponseException
        | IOException
        | NoSuchAlgorithmException
        | ServerException
        | XmlParserException e) {
      logger.error("Failed to download file: {}", storageKey, e);
      throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
    }
  }

  /**
   * Delete a file from MinIO storage.
   *
   * @param storageKey The storage key (object name) of the file
   * @throws RuntimeException if deletion fails
   */
  public void deleteFile(String storageKey) {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucketName).object(storageKey).build());

      logger.info("File deleted successfully: {}", storageKey);

    } catch (ErrorResponseException
        | InsufficientDataException
        | InternalException
        | InvalidKeyException
        | InvalidResponseException
        | IOException
        | NoSuchAlgorithmException
        | ServerException
        | XmlParserException e) {
      logger.error("Failed to delete file: {}", storageKey, e);
      throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
    }
  }

  /**
   * Check if a file exists in MinIO storage.
   *
   * @param storageKey The storage key (object name) of the file
   * @return true if the file exists, false otherwise
   */
  public boolean fileExists(String storageKey) {
    try {
      minioClient.statObject(
          StatObjectArgs.builder().bucket(bucketName).object(storageKey).build());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** Ensure the bucket exists, creating it if necessary. */
  private void ensureBucketExists() {
    try {
      boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        logger.info("Bucket created: {}", bucketName);
      }
    } catch (Exception e) {
      logger.error("Failed to ensure bucket exists: {}", bucketName, e);
      throw new RuntimeException("Failed to ensure bucket exists", e);
    }
  }

  /**
   * Generate a unique storage key for the file.
   *
   * @param fileName The original file name
   * @return The generated storage key
   */
  private String generateStorageKey(String fileName) {
    String sanitizedFileName = sanitizeFileName(fileName);
    String extension = getFileExtension(sanitizedFileName);
    String uniqueId = UUID.randomUUID().toString();

    return String.format("uploads/%s/%s%s", uniqueId.substring(0, 8), uniqueId, extension);
  }

  /**
   * Sanitize the file name by removing potentially dangerous characters.
   *
   * @param fileName The original file name
   * @return The sanitized file name
   */
  private String sanitizeFileName(String fileName) {
    if (fileName == null) {
      return "file";
    }
    // Remove path separators and null bytes
    String sanitized = fileName.replaceAll("[/\\\\\\x00]", "");
    // Remove path traversal attempts
    sanitized = sanitized.replaceAll("\\.\\.", "");
    // Trim whitespace
    sanitized = sanitized.trim();
    return sanitized.isEmpty() ? "file" : sanitized;
  }

  /**
   * Extract the file extension from the file name.
   *
   * @param fileName The file name
   * @return The file extension (including the dot), or empty string if no extension
   */
  private String getFileExtension(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "";
    }
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex);
    }
    return "";
  }

  /**
   * Validate upload parameters.
   *
   * @param fileName The file name
   * @param inputStream The input stream
   * @param fileSize The file size
   * @throws IllegalArgumentException if any parameter is invalid
   */
  private void validateUploadParameters(String fileName, InputStream inputStream, long fileSize) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be null or empty");
    }
    if (inputStream == null) {
      throw new IllegalArgumentException("Input stream cannot be null");
    }
    if (fileSize <= 0) {
      throw new IllegalArgumentException("File size must be greater than 0");
    }
    if (fileSize > MAX_FILE_SIZE) {
      throw new IllegalArgumentException(
          "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
    }
  }
}
