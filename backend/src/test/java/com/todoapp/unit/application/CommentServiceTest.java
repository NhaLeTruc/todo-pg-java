package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.application.dto.CommentDTO;
import com.todoapp.application.service.CommentService;
import com.todoapp.domain.model.Comment;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CommentRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock private CommentRepository commentRepository;

  @Mock private TaskRepository taskRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private CommentService commentService;

  private User testUser;
  private Task testTask;
  private Comment testComment;

  @BeforeEach
  public void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");

    testTask = new Task();
    testTask.setId(1L);
    testTask.setDescription("Test task");
    testTask.setUser(testUser);

    testComment = new Comment();
    testComment.setId(1L);
    testComment.setTask(testTask);
    testComment.setAuthor(testUser);
    testComment.setContent("Test comment");
    testComment.setIsEdited(false);
    testComment.setCreatedAt(LocalDateTime.now());
    testComment.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should add comment successfully")
  public void shouldAddCommentSuccessfully() {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("New comment");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

    CommentDTO result = commentService.addComment(1L, commentDTO, 1L);

    assertNotNull(result);
    assertEquals("Test comment", result.getContent());
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should throw exception when task not found")
  public void shouldThrowExceptionWhenTaskNotFound() {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("New comment");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentService.addComment(99L, commentDTO, 1L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  public void shouldThrowExceptionWhenUserNotFound() {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("New comment");

    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentService.addComment(1L, commentDTO, 99L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should reject empty content")
  public void shouldRejectEmptyContent() {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("");

    assertThrows(
        IllegalArgumentException.class, () -> commentService.addComment(1L, commentDTO, 1L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should reject whitespace-only content")
  public void shouldRejectWhitespaceOnlyContent() {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("   ");

    assertThrows(
        IllegalArgumentException.class, () -> commentService.addComment(1L, commentDTO, 1L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should update comment successfully")
  public void shouldUpdateCommentSuccessfully() {
    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated comment");

    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

    CommentDTO result = commentService.updateComment(1L, updateDTO, 1L);

    assertNotNull(result);
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should set isEdited flag when updating comment")
  public void shouldSetIsEditedFlagWhenUpdating() {
    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated content");

    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class)))
        .thenAnswer(
            invocation -> {
              Comment comment = invocation.getArgument(0);
              assertTrue(comment.getIsEdited());
              return comment;
            });

    commentService.updateComment(1L, updateDTO, 1L);

    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent comment")
  public void shouldThrowExceptionWhenUpdatingNonExistentComment() {
    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated content");

    when(commentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentService.updateComment(99L, updateDTO, 1L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should throw exception when updating comment by non-author")
  public void shouldThrowExceptionWhenUpdatingCommentByNonAuthor() {
    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated content");

    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

    assertThrows(
        IllegalArgumentException.class, () -> commentService.updateComment(1L, updateDTO, 99L));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("Should delete comment successfully")
  public void shouldDeleteCommentSuccessfully() {
    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

    commentService.deleteComment(1L, 1L);

    verify(commentRepository, times(1)).delete(testComment);
  }

  @Test
  @DisplayName("Should throw exception when deleting comment by non-author")
  public void shouldThrowExceptionWhenDeletingCommentByNonAuthor() {
    when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

    assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(1L, 99L));

    verify(commentRepository, never()).delete(any(Comment.class));
  }
}
