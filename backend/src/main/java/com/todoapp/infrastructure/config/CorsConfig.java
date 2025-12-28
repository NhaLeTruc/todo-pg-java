package com.todoapp.infrastructure.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${app.cors.allowed-methods}")
  private String allowedMethods;

  @Value("${app.cors.allowed-headers}")
  private String allowedHeaders;

  @Value("${app.cors.allow-credentials}")
  private Boolean allowCredentials;

  @Value("${app.cors.max-age}")
  private Long maxAge;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);

    List<String> methods = Arrays.asList(allowedMethods.split(","));
    configuration.setAllowedMethods(methods);

    if ("*".equals(allowedHeaders)) {
      configuration.addAllowedHeader("*");
    } else {
      List<String> headers = Arrays.asList(allowedHeaders.split(","));
      configuration.setAllowedHeaders(headers);
    }

    configuration.setAllowCredentials(allowCredentials);
    configuration.setMaxAge(maxAge);

    configuration.setExposedHeaders(
        Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
