package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.User;

@DisplayName("User Entity Unit Tests")
class UserTest {

  // Kept only the test that verifies actual business logic (@PrePersist hook)
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

  // Deleted trivial getter/setter tests:
  // - shouldCreateUserWithValidEmail() - Just tested setters set and getters get
  // - shouldAcceptValidEmailFormats() - Just tested setters set and getters get
  // - shouldStoreBCryptPasswordHash() - Just tested setters set and getters get
  // - shouldAllowSettingFullName() - Just tested setters set and getters get
  // - shouldAllowNullFullName() - Just tested setters set and getters get
  // - shouldTrackLastLoginTimestamp() - Just tested setters set and getters get
  // - shouldAllowDeactivatingUser() - Just tested setters set and getters get
  // - shouldAllowActivatingUser() - Just tested setters set and getters get
  // - shouldAllowSettingEmailVerified() - Just tested setters set and getters get
  // - shouldHaveTimestamps() - Just tested setters set and getters get
  //
  // These tests provided no value - they only verified that Lombok-generated
  // (or standard POJO) getters/setters work. If you're using Lombok, you don't
  // need to test its generated code. The only test kept verifies actual
  // business logic (the @PrePersist onCreate() method).
}
