package com.todoapp.infrastructure.websocket;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.todoapp.infrastructure.security.JwtTokenProvider;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  public AuthChannelInterceptor(
      JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      List<String> authorization = accessor.getNativeHeader("Authorization");

      if (authorization != null && !authorization.isEmpty()) {
        String token = authorization.get(0);

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
          token = token.substring(7);
        }

        if (jwtTokenProvider.validateToken(token)) {
          String username = jwtTokenProvider.getUsernameFromToken(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authentication);
          accessor.setUser(authentication);
        }
      }
    }

    return message;
  }
}
