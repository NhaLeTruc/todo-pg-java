package com.todoapp.unit.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.todoapp.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;
  private String secretKey;
  private long expirationMs;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    // Test-only secret key - NOT used in production (production uses application.properties)
    secretKey = "TEST_SECRET_KEY_FOR_UNIT_TESTS_MUST_BE_AT_LEAST_256_BITS_LONG_1234567890";
    expirationMs = 86400000L; // 24 hours

    ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secretKey);
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationMs);
  }

  @Test
  @DisplayName("Should generate valid JWT token")
  void shouldGenerateValidJwtToken() {
    String email = "test@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
  }

  @Test
  @DisplayName("Should extract email from valid token")
  void shouldExtractEmailFromValidToken() {
    String email = "test@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);
    String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("Should extract user ID from valid token")
  void shouldExtractUserIdFromValidToken() {
    String email = "test@example.com";
    Long userId = 123L;

    String token = jwtTokenProvider.generateToken(email, userId);
    Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("Should validate correct token")
  void shouldValidateCorrectToken() {
    String email = "test@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);
    boolean isValid = jwtTokenProvider.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should reject invalid token")
  void shouldRejectInvalidToken() {
    String invalidToken = "invalid.token.here";

    boolean isValid = jwtTokenProvider.validateToken(invalidToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject expired token")
  void shouldRejectExpiredToken() throws InterruptedException {
    // Set very short expiration for testing
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1L); // 1ms

    String email = "test@example.com";
    Long userId = 1L;
    String token = jwtTokenProvider.generateToken(email, userId);

    // Wait for token to expire
    Thread.sleep(10);

    boolean isValid = jwtTokenProvider.validateToken(token);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject malformed token")
  void shouldRejectMalformedToken() {
    String malformedToken = "not.a.jwt";

    boolean isValid = jwtTokenProvider.validateToken(malformedToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should include issued at timestamp")
  void shouldIncludeIssuedAtTimestamp() {
    String email = "test@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);

    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    assertNotNull(claims.getIssuedAt());
    assertTrue(claims.getIssuedAt().before(new Date(System.currentTimeMillis() + 1000)));
  }

  @Test
  @DisplayName("Should include expiration timestamp")
  void shouldIncludeExpirationTimestamp() {
    String email = "test@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);

    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    assertNotNull(claims.getExpiration());
    assertTrue(claims.getExpiration().after(new Date()));
  }

  @Test
  @DisplayName("Should handle special characters in email")
  void shouldHandleSpecialCharactersInEmail() {
    String email = "user+tag@example.com";
    Long userId = 1L;

    String token = jwtTokenProvider.generateToken(email, userId);
    String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("Should generate different tokens for different users")
  void shouldGenerateDifferentTokensForDifferentUsers() {
    String token1 = jwtTokenProvider.generateToken("user1@example.com", 1L);
    String token2 = jwtTokenProvider.generateToken("user2@example.com", 2L);

    assertNotEquals(token1, token2);
  }

  @Test
  @DisplayName("Should generate consistent tokens for same user")
  void shouldGenerateConsistentTokensForSameUser() {
    String email = "test@example.com";
    Long userId = 1L;

    String token1 = jwtTokenProvider.generateToken(email, userId);
    // Small delay to ensure different timestamps
    String token2 = jwtTokenProvider.generateToken(email, userId);

    // Tokens should be different due to different issue times
    // but should decode to same email/userId
    assertEquals(email, jwtTokenProvider.getEmailFromToken(token1));
    assertEquals(email, jwtTokenProvider.getEmailFromToken(token2));
    assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token1));
    assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token2));
  }
}
