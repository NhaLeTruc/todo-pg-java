package com.todoapp.application.mapper;

import com.todoapp.application.dto.UserResponseDTO;
import com.todoapp.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponseDTO toResponseDTO(User user) {
    UserResponseDTO dto = new UserResponseDTO();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFullName(user.getFullName());
    dto.setIsActive(user.getIsActive());
    dto.setEmailVerified(user.getEmailVerified());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setLastLoginAt(user.getLastLoginAt());
    return dto;
  }
}
