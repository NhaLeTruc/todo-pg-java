package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.CommentDTO;
import com.todoapp.domain.model.Comment;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CommentRepository;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommentApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CommentRepository commentRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;
  private Task testTask;

  @BeforeEach
  public void setUp() {
    commentRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);

    testTask = new Task();
    testTask.setUser(testUser);
    testTask.setDescription("Test task");
    testTask.setPriority(Priority.MEDIUM);
    testTask = taskRepository.save(testTask);
  }

  @Test
  @DisplayName("Should add comment to task successfully")
  public void shouldAddCommentSuccessfully() throws Exception {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("This is a test comment");

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/comments")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.content").value("This is a test comment"))
        .andExpect(jsonPath("$.authorId").value(testUser.getId()))
        .andExpect(jsonPath("$.authorEmail").value("test@example.com"))
        .andExpect(jsonPath("$.isEdited").value(false))
        .andExpect(jsonPath("$.taskId").value(testTask.getId()));

    assertThat(commentRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should reject empty comment content")
  public void shouldRejectEmptyCommentContent() throws Exception {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("");

    mockMvc
        .perform(
            post("/api/v1/tasks/" + testTask.getId() + "/comments")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO)))
        .andExpect(status().isBadRequest());

    assertThat(commentRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should get all comments for a task")
  public void shouldGetAllCommentsForTask() throws Exception {
    Comment comment1 = new Comment();
    comment1.setTask(testTask);
    comment1.setAuthor(testUser);
    comment1.setContent("First comment");
    commentRepository.save(comment1);

    Comment comment2 = new Comment();
    comment2.setTask(testTask);
    comment2.setAuthor(testUser);
    comment2.setContent("Second comment");
    commentRepository.save(comment2);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + testTask.getId() + "/comments")
                .header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].content").exists())
        .andExpect(jsonPath("$[1].content").exists());
  }

  @Test
  @DisplayName("Should update comment successfully")
  public void shouldUpdateCommentSuccessfully() throws Exception {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Original content");
    comment = commentRepository.save(comment);

    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated content");

    mockMvc
        .perform(
            put("/api/v1/comments/" + comment.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("Updated content"))
        .andExpect(jsonPath("$.isEdited").value(true));
  }

  @Test
  @DisplayName("Should delete comment successfully")
  public void shouldDeleteCommentSuccessfully() throws Exception {
    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Test comment");
    comment = commentRepository.save(comment);

    mockMvc
        .perform(
            delete("/api/v1/comments/" + comment.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(commentRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should enforce comment authorization - only author can update")
  public void shouldEnforceCommentAuthorizationForUpdate() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Test comment");
    comment = commentRepository.save(comment);

    CommentDTO updateDTO = new CommentDTO();
    updateDTO.setContent("Updated by other user");

    mockMvc
        .perform(
            put("/api/v1/comments/" + comment.getId())
                .header("X-User-Id", otherUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());

    Comment unchangedComment = commentRepository.findById(comment.getId()).orElseThrow();
    assertThat(unchangedComment.getContent()).isEqualTo("Test comment");
    assertThat(unchangedComment.getIsEdited()).isFalse();
  }

  @Test
  @DisplayName("Should enforce comment authorization - only author can delete")
  public void shouldEnforceCommentAuthorizationForDelete() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Test comment");
    comment = commentRepository.save(comment);

    mockMvc
        .perform(
            delete("/api/v1/comments/" + comment.getId()).header("X-User-Id", otherUser.getId()))
        .andExpect(status().isBadRequest());

    assertThat(commentRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should reject comment on non-existent task")
  public void shouldRejectCommentOnNonExistentTask() throws Exception {
    CommentDTO commentDTO = new CommentDTO();
    commentDTO.setContent("Test comment");

    mockMvc
        .perform(
            post("/api/v1/tasks/99999/comments")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO)))
        .andExpect(status().isNotFound());

    assertThat(commentRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should enforce task access for viewing comments")
  public void shouldEnforceTaskAccessForViewingComments() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Comment comment = new Comment();
    comment.setTask(testTask);
    comment.setAuthor(testUser);
    comment.setContent("Test comment");
    commentRepository.save(comment);

    mockMvc
        .perform(
            get("/api/v1/tasks/" + testTask.getId() + "/comments")
                .header("X-User-Id", otherUser.getId()))
        .andExpect(status().isBadRequest());
  }
}
