package com.todoapp.domain.repository;

import com.todoapp.domain.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  Page<Task> findByUserId(Long userId, Pageable pageable);

  Page<Task> findByUserIdAndIsCompleted(Long userId, Boolean isCompleted, Pageable pageable);

  @Query(
      "SELECT t FROM Task t WHERE t.user.id = :userId AND "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Task> searchByUserIdAndDescription(
      @Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);

  @Query(
      "SELECT t FROM Task t WHERE t.user.id = :userId AND t.isCompleted = :isCompleted AND "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Task> searchByUserIdAndDescriptionAndIsCompleted(
      @Param("userId") Long userId,
      @Param("searchTerm") String searchTerm,
      @Param("isCompleted") Boolean isCompleted,
      Pageable pageable);

  long countByUserIdAndIsCompleted(Long userId, Boolean isCompleted);
}
