package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.CategoryDTO;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.CategoryRepository;
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
public class CategoryControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  public void setUp() {
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("$2a$10$dummyhash");
    testUser.setIsActive(true);
    testUser = userRepository.save(testUser);
  }

  @Test
  @DisplayName("Should create category successfully")
  public void shouldCreateCategorySuccessfully() throws Exception {
    CategoryDTO createDTO = new CategoryDTO();
    createDTO.setName("Work");
    createDTO.setColor("#FF5733");

    mockMvc
        .perform(
            post("/api/v1/categories")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Work"))
        .andExpect(jsonPath("$.color").value("#FF5733"));

    assertThat(categoryRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should get all categories for user")
  public void shouldGetAllCategoriesForUser() throws Exception {
    Category category1 = new Category();
    category1.setName("Work");
    category1.setUser(testUser);
    categoryRepository.save(category1);

    Category category2 = new Category();
    category2.setName("Personal");
    category2.setUser(testUser);
    categoryRepository.save(category2);

    mockMvc
        .perform(get("/api/v1/categories").header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Work"))
        .andExpect(jsonPath("$[1].name").value("Personal"));
  }

  @Test
  @DisplayName("Should get category by ID")
  public void shouldGetCategoryById() throws Exception {
    Category category = new Category();
    category.setName("Work");
    category.setColor("#FF5733");
    category.setUser(testUser);
    category = categoryRepository.save(category);

    mockMvc
        .perform(
            get("/api/v1/categories/" + category.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(category.getId()))
        .andExpect(jsonPath("$.name").value("Work"))
        .andExpect(jsonPath("$.color").value("#FF5733"));
  }

  @Test
  @DisplayName("Should update category successfully")
  public void shouldUpdateCategorySuccessfully() throws Exception {
    Category category = new Category();
    category.setName("Work");
    category.setUser(testUser);
    category = categoryRepository.save(category);

    CategoryDTO updateDTO = new CategoryDTO();
    updateDTO.setName("Work Updated");
    updateDTO.setColor("#0000FF");

    mockMvc
        .perform(
            put("/api/v1/categories/" + category.getId())
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Work Updated"))
        .andExpect(jsonPath("$.color").value("#0000FF"));
  }

  @Test
  @DisplayName("Should delete category successfully")
  public void shouldDeleteCategorySuccessfully() throws Exception {
    Category category = new Category();
    category.setName("Work");
    category.setUser(testUser);
    category = categoryRepository.save(category);

    mockMvc
        .perform(
            delete("/api/v1/categories/" + category.getId()).header("X-User-Id", testUser.getId()))
        .andExpect(status().isNoContent());

    assertThat(categoryRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should reject duplicate category name for same user")
  public void shouldRejectDuplicateCategoryNameForSameUser() throws Exception {
    Category category = new Category();
    category.setName("Work");
    category.setUser(testUser);
    categoryRepository.save(category);

    CategoryDTO createDTO = new CategoryDTO();
    createDTO.setName("Work");

    mockMvc
        .perform(
            post("/api/v1/categories")
                .header("X-User-Id", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

    assertThat(categoryRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should enforce user isolation for categories")
  public void shouldEnforceUserIsolationForCategories() throws Exception {
    User otherUser = new User();
    otherUser.setEmail("other@example.com");
    otherUser.setPasswordHash("$2a$10$dummyhash");
    otherUser.setIsActive(true);
    otherUser = userRepository.save(otherUser);

    Category category = new Category();
    category.setName("Work");
    category.setUser(testUser);
    category = categoryRepository.save(category);

    mockMvc
        .perform(get("/api/v1/categories/" + category.getId()).header("X-User-Id", otherUser.getId()))
        .andExpect(status().isNotFound());
  }
}
