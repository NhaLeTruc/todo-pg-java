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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.todoapp.application.dto.LoginDTO;
import com.todoapp.application.dto.LoginResponseDTO;
import com.todoapp.application.service.AuthService;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.infrastructure.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @InjectMocks private AuthService authService;

  private User user;
  private LoginDTO loginDTO;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPasswordHash("$2a$12$hashedPassword");
    user.setFullName("Test User");
    user.setIsActive(true);

    loginDTO = new LoginDTO();
    loginDTO.setEmail("test@example.com");
    loginDTO.setPassword("SecurePass123!");
  }

  @Test
  @DisplayName("Should authenticate user with valid credentials")
  void shouldAuthenticateUserWithValidCredentials() {
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())).thenReturn(true);
    when(jwtTokenProvider.generateToken(user.getEmail(), user.getId()))
        .thenReturn("jwt.token.here");
    when(userRepository.save(any(User.class))).thenReturn(user);

    LoginResponseDTO result = authService.login(loginDTO);

    assertNotNull(result);
    assertEquals("jwt.token.here", result.getToken());
    assertEquals("test@example.com", result.getEmail());
    assertEquals("Test User", result.getFullName());

    verify(passwordEncoder).matches(loginDTO.getPassword(), user.getPasswordHash());
    verify(jwtTokenProvider).generateToken(user.getEmail(), user.getId());
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> authService.login(loginDTO), "Invalid email or password");

    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtTokenProvider, never()).generateToken(any(), any());
  }

  @Test
  @DisplayName("Should throw exception when password is incorrect")
  void shouldThrowExceptionWhenPasswordIncorrect() {
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())).thenReturn(false);

    assertThrows(
        RuntimeException.class, () -> authService.login(loginDTO), "Invalid email or password");

    verify(jwtTokenProvider, never()).generateToken(any(), any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when user is inactive")
  void shouldThrowExceptionWhenUserInactive() {
    user.setIsActive(false);
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())).thenReturn(true);

    assertThrows(
        RuntimeException.class, () -> authService.login(loginDTO), "User account is deactivated");

    verify(jwtTokenProvider, never()).generateToken(any(), any());
  }

  @Test
  @DisplayName("Should update last login timestamp on successful login")
  void shouldUpdateLastLoginTimestamp() {
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())).thenReturn(true);
    when(jwtTokenProvider.generateToken(any(), any())).thenReturn("jwt.token.here");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User savedUser = invocation.getArgument(0);
              assertNotNull(savedUser.getLastLoginAt());
              assertTrue(savedUser.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));
              return savedUser;
            });

    authService.login(loginDTO);

    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Should generate JWT token on successful authentication")
  void shouldGenerateJwtTokenOnSuccess() {
    when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())).thenReturn(true);
    when(jwtTokenProvider.generateToken(user.getEmail(), user.getId()))
        .thenReturn("generated.jwt.token");
    when(userRepository.save(any(User.class))).thenReturn(user);

    LoginResponseDTO result = authService.login(loginDTO);

    assertEquals("generated.jwt.token", result.getToken());
    verify(jwtTokenProvider).generateToken("test@example.com", 1L);
  }

  // Deleted tautological tests:
  // - shouldValidateToken() - Just tested that mock returns what we told it to
  // - shouldExtractEmailFromToken() - Just tested that mock returns what we told it to
  // These tests provided no value - they only verified method delegation which
  // the compiler already guarantees. No business logic was being tested.
}
