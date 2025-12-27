package com.todoapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.LoginDTO;
import com.todoapp.application.dto.RegisterDTO;
import com.todoapp.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class AuthControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  public void setUp() {
    userRepository.deleteAll();
  }

  @Test
  public void shouldRegisterUserSuccessfully() throws Exception {
    RegisterDTO registerDTO = new RegisterDTO();
    registerDTO.setEmail("newuser@example.com");
    registerDTO.setPassword("SecurePass123!");
    registerDTO.setFullName("New User");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.fullName").value("New User"))
        .andExpect(jsonPath("$.isActive").value(true));

    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  public void shouldRejectDuplicateEmailRegistration() throws Exception {
    RegisterDTO registerDTO = new RegisterDTO();
    registerDTO.setEmail("duplicate@example.com");
    registerDTO.setPassword("SecurePass123!");
    registerDTO.setFullName("First User");

    // First registration
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated());

    // Duplicate registration
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isConflict());

    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  public void shouldLoginWithValidCredentials() throws Exception {
    // Register user first
    RegisterDTO registerDTO = new RegisterDTO();
    registerDTO.setEmail("loginuser@example.com");
    registerDTO.setPassword("SecurePass123!");
    registerDTO.setFullName("Login User");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated());

    // Login
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setEmail("loginuser@example.com");
    loginDTO.setPassword("SecurePass123!");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.email").value("loginuser@example.com"))
        .andExpect(jsonPath("$.fullName").value("Login User"))
        .andExpect(jsonPath("$.userId").exists());
  }

  @Test
  public void shouldRejectLoginWithInvalidPassword() throws Exception {
    // Register user
    RegisterDTO registerDTO = new RegisterDTO();
    registerDTO.setEmail("testuser@example.com");
    registerDTO.setPassword("CorrectPassword123!");
    registerDTO.setFullName("Test User");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated());

    // Try login with wrong password
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setEmail("testuser@example.com");
    loginDTO.setPassword("WrongPassword123!");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldRejectLoginWithNonexistentEmail() throws Exception {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setEmail("nonexistent@example.com");
    loginDTO.setPassword("SomePassword123!");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldLogoutSuccessfully() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/logout"))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldHashPasswordOnRegistration() throws Exception {
    RegisterDTO registerDTO = new RegisterDTO();
    registerDTO.setEmail("hashtest@example.com");
    registerDTO.setPassword("PlainTextPassword123!");
    registerDTO.setFullName("Hash Test");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated());

    // Verify password is hashed in database
    var user = userRepository.findByEmail("hashtest@example.com").orElseThrow();
    assertThat(user.getPasswordHash()).isNotEqualTo("PlainTextPassword123!");
    assertThat(user.getPasswordHash()).startsWith("$2a$"); // BCrypt hash
  }
}
