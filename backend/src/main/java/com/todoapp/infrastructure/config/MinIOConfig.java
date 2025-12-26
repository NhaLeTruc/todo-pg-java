package com.todoapp.infrastructure.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {

  private static final Logger logger = LoggerFactory.getLogger(MinIOConfig.class);

  @Value("${app.minio.endpoint}")
  private String endpoint;

  @Value("${app.minio.access-key}")
  private String accessKey;

  @Value("${app.minio.secret-key}")
  private String secretKey;

  @Value("${app.minio.bucket}")
  private String bucket;

  @Bean
  public MinioClient minioClient() {
    try {
      MinioClient minioClient =
          MinioClient.builder()
              .endpoint(endpoint)
              .credentials(accessKey, secretKey)
              .build();

      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());

      if (!bucketExists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        logger.info("MinIO bucket '{}' created successfully", bucket);
      } else {
        logger.info("MinIO bucket '{}' already exists", bucket);
      }

      return minioClient;
    } catch (Exception e) {
      logger.error("Failed to initialize MinIO client", e);
      throw new RuntimeException("Failed to initialize MinIO client", e);
    }
  }
}
