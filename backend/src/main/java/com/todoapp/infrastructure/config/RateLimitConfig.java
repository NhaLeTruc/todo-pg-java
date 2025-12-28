package com.todoapp.infrastructure.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Rate limiting configuration using Bucket4j. Implements token bucket algorithm to prevent API
 * abuse and ensure fair resource usage.
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

  /**
   * In-memory bucket storage per IP address. In production, consider using Redis or another
   * distributed cache.
   */
  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  /**
   * Rate limit: 100 requests per minute per IP address. Refill: 100 tokens every minute (greedy
   * refill strategy).
   */
  private static final long CAPACITY = 100;

  private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

  /**
   * Creates or retrieves a bucket for the given key (typically IP address).
   *
   * @param key The identifier (e.g., IP address or user ID)
   * @return Bucket instance for rate limiting
   */
  public Bucket resolveBucket(String key) {
    return cache.computeIfAbsent(key, k -> createNewBucket());
  }

  /**
   * Creates a new bucket with configured rate limits.
   *
   * @return New Bucket instance
   */
  private Bucket createNewBucket() {
    Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.greedy(CAPACITY, REFILL_PERIOD));
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Register the rate limit interceptor.
   *
   * @param registry Interceptor registry
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(rateLimitInterceptor())
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/v1/auth/login", "/api/v1/auth/register");
  }

  /**
   * Bean for rate limit interceptor.
   *
   * @return RateLimitInterceptor instance
   */
  @Bean
  public RateLimitInterceptor rateLimitInterceptor() {
    return new RateLimitInterceptor(this);
  }
}
