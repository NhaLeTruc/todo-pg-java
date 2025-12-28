package com.todoapp.infrastructure.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to add correlation IDs to requests for distributed tracing. Uses SLF4J MDC (Mapped
 * Diagnostic Context) to make correlation IDs available in logs.
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    try {
      // Check if correlation ID already exists in request header
      String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

      // If not present, generate a new one
      if (correlationId == null || correlationId.trim().isEmpty()) {
        correlationId = generateCorrelationId();
      }

      // Add to MDC for logging
      MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

      // Add to response header for client tracking
      httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

      // Continue the filter chain
      chain.doFilter(request, response);

    } finally {
      // Always clean up MDC to prevent memory leaks
      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }

  /**
   * Generates a new correlation ID using UUID.
   *
   * @return A unique correlation ID
   */
  private String generateCorrelationId() {
    return UUID.randomUUID().toString();
  }
}
