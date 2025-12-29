package com.todoapp.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.FileAttachment;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.VirusScanStatus;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, UUID> {

  /**
   * Find all file attachments for a specific task.
   *
   * @param task The task entity
   * @return List of file attachments
   */
  List<FileAttachment> findByTask(Task task);

  /**
   * Find all file attachments for a specific task ID.
   *
   * @param taskId The task ID
   * @return List of file attachments
   */
  @Query("SELECT fa FROM FileAttachment fa WHERE fa.task.id = :taskId ORDER BY fa.createdAt DESC")
  List<FileAttachment> findByTaskId(@Param("taskId") Long taskId);

  /**
   * Find a file attachment by its storage key.
   *
   * @param storageKey The storage key
   * @return Optional file attachment
   */
  Optional<FileAttachment> findByStorageKey(String storageKey);

  /**
   * Find all file attachments with a specific virus scan status.
   *
   * @param scanStatus The virus scan status
   * @return List of file attachments
   */
  List<FileAttachment> findByScanStatus(VirusScanStatus scanStatus);

  /**
   * Find all file attachments for a specific user.
   *
   * @param userId The user ID
   * @return List of file attachments
   */
  @Query("SELECT fa FROM FileAttachment fa WHERE fa.user.id = :userId ORDER BY fa.createdAt DESC")
  List<FileAttachment> findByUserId(@Param("userId") Long userId);

  /**
   * Count file attachments for a specific task.
   *
   * @param taskId The task ID
   * @return Number of attachments
   */
  @Query("SELECT COUNT(fa) FROM FileAttachment fa WHERE fa.task.id = :taskId")
  long countByTaskId(@Param("taskId") Long taskId);

  /**
   * Calculate total file size for a specific user.
   *
   * @param userId The user ID
   * @return Total file size in bytes
   */
  @Query("SELECT COALESCE(SUM(fa.fileSize), 0) FROM FileAttachment fa WHERE fa.user.id = :userId")
  long calculateTotalFileSizeByUserId(@Param("userId") Long userId);

  /**
   * Find file attachment by ID and user ID (for authorization).
   *
   * @param id The attachment ID
   * @param userId The user ID
   * @return Optional file attachment
   */
  @Query("SELECT fa FROM FileAttachment fa WHERE fa.id = :id AND fa.user.id = :userId")
  Optional<FileAttachment> findByIdAndUserId(@Param("id") UUID id, @Param("userId") Long userId);

  /**
   * Delete all file attachments for a specific task.
   *
   * @param taskId The task ID
   */
  @Query("DELETE FROM FileAttachment fa WHERE fa.task.id = :taskId")
  void deleteByTaskId(@Param("taskId") Long taskId);
}
