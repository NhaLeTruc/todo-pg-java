package com.todoapp.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.RegisterDTO;
import com.todoapp.application.dto.UserResponseDTO;
import com.todoapp.application.mapper.UserMapper;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.UserRepository;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserMapper userMapper;

  @Transactional
  public UserResponseDTO registerUser(RegisterDTO registerDTO) {
    logger.info("Registering new user: {}", registerDTO.getEmail());

    if (userRepository.existsByEmail(registerDTO.getEmail())) {
      throw new RuntimeException("Email already registered");
    }

    User user = new User();
    user.setEmail(registerDTO.getEmail());
    user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
    user.setFullName(registerDTO.getFullName());
    user.setIsActive(true);
    user.setEmailVerified(false);

    User savedUser = userRepository.save(user);
    logger.info("User registered successfully: {}", savedUser.getEmail());

    return userMapper.toResponseDTO(savedUser);
  }

  public UserResponseDTO findByEmail(String email) {
    logger.debug("Finding user by email: {}", email);

    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    return userMapper.toResponseDTO(user);
  }

  public User getUserEntityByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  public User getUserEntityById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
