package com.todoapp.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.todoapp.infrastructure.websocket.AuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final AuthChannelInterceptor authChannelInterceptor;

  public WebSocketConfig(AuthChannelInterceptor authChannelInterceptor) {
    this.authChannelInterceptor = authChannelInterceptor;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable a simple in-memory broker for user-specific queues
    config.enableSimpleBroker("/queue", "/topic");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Register STOMP endpoint with SockJS fallback for notifications
    registry.addEndpoint("/ws/notifications").setAllowedOriginPatterns("*").withSockJS();

    // Register STOMP endpoint for task updates
    registry.addEndpoint("/ws/tasks").setAllowedOriginPatterns("*").withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(authChannelInterceptor);
  }
}
