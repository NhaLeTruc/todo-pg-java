package com.todoapp.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor for rate limiting API requests using Bucket4j. Enforces rate limits per IP address to
 * prevent abuse.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

  private final RateLimitConfig rateLimitConfig;

  public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
    this.rateLimitConfig = rateLimitConfig;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String key = getClientIP(request);
    Bucket bucket = rateLimitConfig.resolveBucket(key);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
      return true;
    } else {
      long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
      response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
      response.sendError(
          HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota");
      return false;
    }
  }

  /**
   * Gets the client IP address from the request. Checks for proxy headers first (X-Forwarded-For).
   *
   * @param request HTTP request
   * @return Client IP address
   */
  private String getClientIP(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
  }
}
