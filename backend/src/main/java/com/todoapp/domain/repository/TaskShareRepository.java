package com.todoapp.domain.repository;

import com.todoapp.domain.model.TaskShare;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskShareRepository extends JpaRepository<TaskShare, Long> {

  @Query("SELECT ts FROM TaskShare ts WHERE ts.task.id = :taskId")
  List<TaskShare> findByTaskId(@Param("taskId") Long taskId);

  @Query(
      "SELECT ts FROM TaskShare ts WHERE ts.task.id = :taskId AND ts.sharedWithUser.id ="
          + " :sharedWithUserId")
  Optional<TaskShare> findByTaskIdAndSharedWithUserId(
      @Param("taskId") Long taskId, @Param("sharedWithUserId") Long sharedWithUserId);

  @Query("SELECT ts FROM TaskShare ts WHERE ts.sharedWithUser.id = :userId")
  List<TaskShare> findBySharedWithUserId(@Param("userId") Long userId);

  @Query(
      "SELECT ts FROM TaskShare ts WHERE ts.task.id = :taskId AND ts.sharedWithUser.id = :userId"
          + " AND ts.permissionLevel = 'EDIT'")
  Optional<TaskShare> findEditPermissionByTaskIdAndUserId(
      @Param("taskId") Long taskId, @Param("userId") Long userId);

  @Query("SELECT COUNT(ts) FROM TaskShare ts WHERE ts.task.id = :taskId")
  long countByTaskId(@Param("taskId") Long taskId);

  @Query(
      "SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TaskShare ts WHERE ts.task.id ="
          + " :taskId AND ts.sharedWithUser.id = :userId")
  boolean existsByTaskIdAndSharedWithUserId(
      @Param("taskId") Long taskId, @Param("userId") Long userId);
}
