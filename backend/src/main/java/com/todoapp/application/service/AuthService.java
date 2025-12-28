package com.todoapp.application.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.LoginDTO;
import com.todoapp.application.dto.LoginResponseDTO;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.infrastructure.security.JwtTokenProvider;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Transactional
  public LoginResponseDTO login(LoginDTO loginDTO) {
    logger.info("Login attempt for email: {}", loginDTO.getEmail());

    User user =
        userRepository
            .findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
      logger.warn("Invalid password for email: {}", loginDTO.getEmail());
      throw new RuntimeException("Invalid email or password");
    }

    if (!user.getIsActive()) {
      logger.warn("Inactive user attempted login: {}", loginDTO.getEmail());
      throw new RuntimeException("User account is deactivated");
    }

    // Update last login timestamp
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    // Generate JWT token
    String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());

    logger.info("User logged in successfully: {}", user.getEmail());

    LoginResponseDTO response = new LoginResponseDTO();
    response.setToken(token);
    response.setEmail(user.getEmail());
    response.setFullName(user.getFullName());
    response.setUserId(user.getId());

    return response;
  }

  public boolean validateToken(String token) {
    return jwtTokenProvider.validateToken(token);
  }

  public String getEmailFromToken(String token) {
    return jwtTokenProvider.getEmailFromToken(token);
  }

  public Long getUserIdFromToken(String token) {
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
