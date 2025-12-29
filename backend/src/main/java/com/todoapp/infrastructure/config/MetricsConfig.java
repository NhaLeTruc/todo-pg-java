package com.todoapp.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * Micrometer metrics configuration for performance monitoring. Configures custom metrics, timers,
 * and performance tracking.
 */
@Configuration
public class MetricsConfig {

  /**
   * Customize meter registry with application-specific tags and settings.
   *
   * @return Meter registry customizer
   */
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> {
      // Add common tags to all metrics
      registry.config().commonTags("application", "todo-backend", "environment", getEnvironment());

      // Configure distribution statistics for better percentile accuracy
      registry
          .config()
          .meterFilter(
              new MeterFilter() {
                @Override
                public DistributionStatisticConfig configure(
                    io.micrometer.core.instrument.Meter.Id id, DistributionStatisticConfig config) {
                  if (id.getType() == io.micrometer.core.instrument.Meter.Type.TIMER) {
                    return DistributionStatisticConfig.builder()
                        .percentilesHistogram(true)
                        .percentiles(0.5, 0.75, 0.95, 0.99)
                        .minimumExpectedValue(Duration.ofMillis(1).toNanos())
                        .maximumExpectedValue(Duration.ofSeconds(10).toNanos())
                        .serviceLevelObjectives(
                            Duration.ofMillis(100).toNanos(),
                            Duration.ofMillis(200).toNanos(),
                            Duration.ofMillis(500).toNanos(),
                            Duration.ofSeconds(1).toNanos(),
                            Duration.ofSeconds(2).toNanos())
                        .build()
                        .merge(config);
                  }
                  return config;
                }
              });

      // Configure custom meter filters
      registry.config().meterFilter(MeterFilter.denyNameStartsWith("jvm.threads.states"));
      registry.config().meterFilter(MeterFilter.denyNameStartsWith("process.files"));
    };
  }

  /**
   * Enable @Timed annotation support for method timing.
   *
   * @param registry Meter registry
   * @return Timed aspect
   */
  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  // WebMvcTagsContributor has been replaced with Observation API in Spring Boot 3.x
  // Custom HTTP metrics can be added through ObservationRegistry and custom conventions
  // if needed in the future

  /**
   * Get current environment from system properties or environment variables.
   *
   * @return Environment name
   */
  private String getEnvironment() {
    String env = System.getProperty("spring.profiles.active");
    if (env == null) {
      env = System.getenv("SPRING_PROFILES_ACTIVE");
    }
    return env != null ? env : "default";
  }

  /**
   * Custom metric for database query performance. This can be used in repositories to track query
   * execution time.
   *
   * @param registry Meter registry
   * @return Timer for database queries
   */
  @Bean
  public Timer databaseQueryTimer(MeterRegistry registry) {
    return Timer.builder("database.query.time")
        .description("Database query execution time")
        .tag("type", "query")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .register(registry);
  }

  /**
   * Custom metric for cache hit/miss tracking.
   *
   * @param registry Meter registry
   */
  @Bean
  public void configureCacheMetrics(MeterRegistry registry) {
    // Cache hit rate
    registry.gauge(
        "cache.hit.rate", java.util.Collections.emptyList(), this, value -> 0.0); // Placeholder
  }

  /**
   * Custom metric for business operations.
   *
   * @param registry Meter registry
   * @return Timer for business operations
   */
  @Bean
  public Timer businessOperationTimer(MeterRegistry registry) {
    return Timer.builder("business.operation.time")
        .description("Business operation execution time")
        .tag("type", "operation")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(registry);
  }
}
