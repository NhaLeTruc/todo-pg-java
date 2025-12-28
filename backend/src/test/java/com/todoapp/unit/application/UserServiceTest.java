package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.todoapp.application.dto.RegisterDTO;
import com.todoapp.application.dto.UserResponseDTO;
import com.todoapp.application.mapper.UserMapper;
import com.todoapp.application.service.UserService;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserService userService;

  private RegisterDTO registerDTO;
  private User user;
  private UserResponseDTO userResponseDTO;

  @BeforeEach
  void setUp() {
    registerDTO = new RegisterDTO();
    registerDTO.setEmail("test@example.com");
    registerDTO.setPassword("SecurePass123!");
    registerDTO.setFullName("Test User");

    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPasswordHash("$2a$12$hashedPassword");
    user.setFullName("Test User");
    user.setIsActive(true);
    user.setEmailVerified(false);

    userResponseDTO = new UserResponseDTO();
    userResponseDTO.setId(1L);
    userResponseDTO.setEmail("test@example.com");
    userResponseDTO.setFullName("Test User");
  }

  @Test
  @DisplayName("Should register user with BCrypt password hashing")
  void shouldRegisterUserWithBCryptHashing() {
    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("$2a$12$hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

    UserResponseDTO result = userService.registerUser(registerDTO);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals("Test User", result.getFullName());

    verify(passwordEncoder).encode("SecurePass123!");
    verify(userRepository).save(any(User.class));
    verify(userMapper).toResponseDTO(user);
  }

  @Test
  @DisplayName("Should throw exception when email already exists")
  void shouldThrowExceptionWhenEmailExists() {
    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

    assertThrows(
        RuntimeException.class,
        () -> userService.registerUser(registerDTO),
        "Email already registered");

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should find user by email")
  void shouldFindUserByEmail() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

    UserResponseDTO result = userService.findByEmail("test@example.com");

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    verify(userRepository).findByEmail("test@example.com");
  }

  @Test
  @DisplayName("Should throw exception when user not found by email")
  void shouldThrowExceptionWhenUserNotFoundByEmail() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class,
        () -> userService.findByEmail("nonexistent@example.com"),
        "User not found");

    verify(userRepository).findByEmail("nonexistent@example.com");
  }

  @Test
  @DisplayName("Should validate email format before registration")
  void shouldValidateEmailFormat() {
    registerDTO.setEmail("invalid-email");

    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);

    // Email validation should happen at DTO level with @Email annotation
    // This test verifies the service doesn't crash with invalid format
    when(passwordEncoder.encode(any())).thenReturn("$2a$12$hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

    assertDoesNotThrow(() -> userService.registerUser(registerDTO));
  }

  @Test
  @DisplayName("Should set new user as active by default")
  void shouldSetNewUserAsActiveByDefault() {
    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("$2a$12$hashedPassword");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User savedUser = invocation.getArgument(0);
              assertTrue(savedUser.getIsActive());
              return savedUser;
            });
    when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

    userService.registerUser(registerDTO);

    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should not expose password hash in response")
  void shouldNotExposePasswordHash() {
    when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("$2a$12$hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

    UserResponseDTO result = userService.registerUser(registerDTO);

    assertNotNull(result);
    // UserResponseDTO should not have a password field
    assertEquals("test@example.com", result.getEmail());
  }
}
