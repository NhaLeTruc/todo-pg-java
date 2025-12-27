package com.todoapp.presentation.rest;

import com.todoapp.application.dto.LoginDTO;
import com.todoapp.application.dto.LoginResponseDTO;
import com.todoapp.application.dto.RegisterDTO;
import com.todoapp.application.dto.UserResponseDTO;
import com.todoapp.application.service.AuthService;
import com.todoapp.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Creates a new user account")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Email already registered")
      })
  public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
    logger.info("Registration request for email: {}", registerDTO.getEmail());

    try {
      UserResponseDTO user = userService.registerUser(registerDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(user);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("Email already registered")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      throw e;
    }
  }

  @PostMapping("/login")
  @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "User account deactivated")
      })
  public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
    logger.info("Login request for email: {}", loginDTO.getEmail());

    try {
      LoginResponseDTO response = authService.login(loginDTO);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("Invalid email or password")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      if (e.getMessage().contains("deactivated")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      throw e;
    }
  }

  @PostMapping("/logout")
  @Operation(summary = "User logout", description = "Logs out the current user (client-side token removal)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Logout successful")
      })
  public ResponseEntity<Void> logout() {
    // With JWT, logout is primarily handled client-side by removing the token
    // This endpoint is provided for consistency and future server-side logout logic
    logger.info("Logout request received");
    return ResponseEntity.ok().build();
  }
}
