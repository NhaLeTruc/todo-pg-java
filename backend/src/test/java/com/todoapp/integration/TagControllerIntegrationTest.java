package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.TagDTO;
import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TagRepository;
import com.todoapp.domain.repository.UserRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TagControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TagRepository tagRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  public void setUp() {
    tagRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);
  }

  @Test
  @DisplayName("Should create tag successfully")
  public void shouldCreateTagSuccessfully() throws Exception {
    TagDTO createDTO = new TagDTO();
    createDTO.setName("urgent");
    createDTO.setColor("#FF0000");

    mockMvc
        .perform(
            post("/api/v1/tags")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("urgent"))
        .andExpect(jsonPath("$.color").value("#FF0000"));

    assertThat(tagRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should get all tags for user")
  public void shouldGetAllTagsForUser() throws Exception {
    Tag tag1 = new Tag();
    tag1.setName("urgent");
    tag1.setUser(testUser);
    tagRepository.save(tag1);

    Tag tag2 = new Tag();
    tag2.setName("important");
    tag2.setUser(testUser);
    tagRepository.save(tag2);

    mockMvc
        .perform(get("/api/v1/tags").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("urgent"))
        .andExpect(jsonPath("$[1].name").value("important"));
  }

  @Test
  @DisplayName("Should get tag by ID")
  public void shouldGetTagById() throws Exception {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setColor("#FF0000");
    tag.setUser(testUser);
    tag = tagRepository.save(tag);

    mockMvc
        .perform(get("/api/v1/tags/" + tag.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(tag.getId()))
        .andExpect(jsonPath("$.name").value("urgent"))
        .andExpect(jsonPath("$.color").value("#FF0000"));
  }

  @Test
  @DisplayName("Should update tag successfully")
  public void shouldUpdateTagSuccessfully() throws Exception {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(testUser);
    tag = tagRepository.save(tag);

    TagDTO updateDTO = new TagDTO();
    updateDTO.setName("very-urgent");
    updateDTO.setColor("#FF0000");

    mockMvc
        .perform(
            put("/api/v1/tags/" + tag.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("very-urgent"))
        .andExpect(jsonPath("$.color").value("#FF0000"));
  }

  @Test
  @DisplayName("Should delete tag successfully")
  public void shouldDeleteTagSuccessfully() throws Exception {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(testUser);
    tag = tagRepository.save(tag);

    mockMvc
        .perform(delete("/api/v1/tags/" + tag.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(tagRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should reject duplicate tag name for same user")
  public void shouldRejectDuplicateTagNameForSameUser() throws Exception {
    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(testUser);
    tagRepository.save(tag);

    TagDTO createDTO = new TagDTO();
    createDTO.setName("urgent");

    mockMvc
        .perform(
            post("/api/v1/tags")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

    assertThat(tagRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should enforce user isolation for tags")
  public void shouldEnforceUserIsolationForTags() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Tag tag = new Tag();
    tag.setName("urgent");
    tag.setUser(testUser);
    tag = tagRepository.save(tag);

    mockMvc
        .perform(get("/api/v1/tags/" + tag.getId()).header("X-User-Id", otherUser.getId()))
        .andExpect(status().isNotFound());
  }
}
