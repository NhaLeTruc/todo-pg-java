package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.User;

@DisplayName("User Entity Unit Tests")
class UserTest {

  @Test
  @DisplayName("Should create user with valid email")
  void shouldCreateUserWithValidEmail() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("$2a$12$hashedpassword");
    user.setFullName("Test User");

    assertNotNull(user);
    assertEquals("test@example.com", user.getEmail());
    assertEquals("$2a$12$hashedpassword", user.getPasswordHash());
    assertEquals("Test User", user.getFullName());
  }

  @Test
  @DisplayName("Should have default values for isActive and emailVerified")
  void shouldHaveDefaultValues() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");

    // Simulate @PrePersist
    user.onCreate();

    assertTrue(user.getIsActive());
    assertFalse(user.getEmailVerified());
  }

  @Test
  @DisplayName("Should accept valid email formats")
  void shouldAcceptValidEmailFormats() {
    String[] validEmails = {
      "user@example.com",
      "user.name@example.com",
      "user+tag@example.co.uk",
      "user_name@example-domain.com"
    };

    for (String email : validEmails) {
      User user = new User();
      user.setEmail(email);
      user.setPasswordHash("hashedPassword");

      assertEquals(email, user.getEmail());
    }
  }

  @Test
  @DisplayName("Should store BCrypt password hash")
  void shouldStoreBCryptPasswordHash() {
    User user = new User();
    user.setEmail("test@example.com");
    String bcryptHash = "$2a$12$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGH";
    user.setPasswordHash(bcryptHash);

    assertEquals(bcryptHash, user.getPasswordHash());
    assertTrue(user.getPasswordHash().startsWith("$2a$"));
  }

  @Test
  @DisplayName("Should allow setting full name")
  void shouldAllowSettingFullName() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setFullName("John Doe");

    assertEquals("John Doe", user.getFullName());
  }

  @Test
  @DisplayName("Should allow null full name")
  void shouldAllowNullFullName() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setFullName(null);

    assertNull(user.getFullName());
  }

  @Test
  @DisplayName("Should track last login timestamp")
  void shouldTrackLastLoginTimestamp() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setLastLoginAt(java.time.LocalDateTime.now());

    assertNotNull(user.getLastLoginAt());
  }

  @Test
  @DisplayName("Should allow deactivating user")
  void shouldAllowDeactivatingUser() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setIsActive(true);

    user.setIsActive(false);

    assertFalse(user.getIsActive());
  }

  @Test
  @DisplayName("Should allow activating user")
  void shouldAllowActivatingUser() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setIsActive(false);

    user.setIsActive(true);

    assertTrue(user.getIsActive());
  }

  @Test
  @DisplayName("Should allow setting email verified flag")
  void shouldAllowSettingEmailVerified() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setEmailVerified(false);

    user.setEmailVerified(true);

    assertTrue(user.getEmailVerified());
  }

  @Test
  @DisplayName("Should have created and updated timestamps")
  void shouldHaveTimestamps() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedPassword");
    user.setCreatedAt(java.time.LocalDateTime.now());
    user.setUpdatedAt(java.time.LocalDateTime.now());

    assertNotNull(user.getCreatedAt());
    assertNotNull(user.getUpdatedAt());
  }
}
