package com.todoapp.application.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

  private Long id;
  private String email;
  private String fullName;
  private Boolean isActive;
  private Boolean emailVerified;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;
}
