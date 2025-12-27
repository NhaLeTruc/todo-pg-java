package com.todoapp.domain.repository;

import com.todoapp.domain.model.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

  @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.author.id = :authorId")
  Optional<Comment> findByIdAndAuthorId(@Param("id") Long id, @Param("authorId") Long authorId);

  @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND c.task.user.id = :userId ORDER BY c.createdAt DESC")
  List<Comment> findByTaskIdAndTaskUserId(
      @Param("taskId") Long taskId, @Param("userId") Long userId);

  long countByTaskId(Long taskId);
}
