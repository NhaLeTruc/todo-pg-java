package com.todoapp.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_users_email", columnList = "email"),
      @Index(name = "idx_users_is_active", columnList = "isActive"),
      @Index(name = "idx_users_created_at", columnList = "createdAt")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @NotBlank(message = "Password hash is required")
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Size(max = 255, message = "Full name must not exceed 255 characters")
  @Column(name = "full_name", length = 255)
  private String fullName;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "email_verified", nullable = false)
  private Boolean emailVerified = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @PrePersist
  protected void onCreate() {
    if (isActive == null) {
      isActive = true;
    }
    if (emailVerified == null) {
      emailVerified = false;
    }
  }
}
