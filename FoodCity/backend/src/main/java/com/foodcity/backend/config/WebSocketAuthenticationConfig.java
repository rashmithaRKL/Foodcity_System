package com.foodcity.backend.config;

import com.foodcity.backend.security.JwtTokenProvider;
import com.foodcity.backend.security.WebSocketChannelInterceptor;
import com.foodcity.backend.security.WebSocketHandshakeInterceptor;
import com.foodcity.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider tokenProvider;
    private final WebSocketService webSocketService;

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Allow connection handshake for all
            .nullDestMatcher().permitAll()
            
            // Public endpoints
            .simpDestMatchers("/topic/public/**").permitAll()
            .simpDestMatchers("/app/public/**").permitAll()
            
            // User-specific endpoints require authentication
            .simpDestMatchers("/user/**").authenticated()
            .simpDestMatchers("/app/user/**").authenticated()
            
            // Admin endpoints require ADMIN role
            .simpDestMatchers("/topic/admin/**").hasRole("ADMIN")
            .simpDestMatchers("/app/admin/**").hasRole("ADMIN")
            
            // Analytics endpoints require specific roles
            .simpDestMatchers("/topic/analytics/**").hasAnyRole("ADMIN", "ANALYST")
            .simpDestMatchers("/app/analytics/**").hasAnyRole("ADMIN", "ANALYST")
            
            // Dashboard endpoints
            .simpDestMatchers("/topic/dashboard/**").hasAnyRole("ADMIN", "MANAGER")
            .simpDestMatchers("/app/dashboard/**").hasAnyRole("ADMIN", "MANAGER")
            
            // Inventory endpoints
            .simpDestMatchers("/topic/inventory/**").hasAnyRole("ADMIN", "INVENTORY")
            .simpDestMatchers("/app/inventory/**").hasAnyRole("ADMIN", "INVENTORY")
            
            // Order endpoints
            .simpDestMatchers("/topic/orders/**").hasAnyRole("ADMIN", "SALES")
            .simpDestMatchers("/app/orders/**").hasAnyRole("ADMIN", "SALES")
            
            // Customer endpoints
            .simpDestMatchers("/topic/customers/**").hasAnyRole("ADMIN", "SALES")
            .simpDestMatchers("/app/customers/**").hasAnyRole("ADMIN", "SALES")
            
            // Employee endpoints
            .simpDestMatchers("/topic/employees/**").hasAnyRole("ADMIN", "HR")
            .simpDestMatchers("/app/employees/**").hasAnyRole("ADMIN", "HR")
            
            // Payment endpoints
            .simpDestMatchers("/topic/payments/**").hasAnyRole("ADMIN", "FINANCE")
            .simpDestMatchers("/app/payments/**").hasAnyRole("ADMIN", "FINANCE")
            
            // System endpoints
            .simpDestMatchers("/topic/system/**").hasRole("ADMIN")
            .simpDestMatchers("/app/system/**").hasRole("ADMIN")
            
            // Deny all other messages by default
            .anyMessage().denyAll();
    }

    @Override
    protected void customizeClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor());
    }

    @Bean
    public ChannelInterceptor channelInterceptor() {
        return new WebSocketChannelInterceptor(
            tokenProvider,
            webSocketService,
            webSocketHandshakeInterceptor()
        );
    }

    @Bean
    public HandshakeInterceptor webSocketHandshakeInterceptor() {
        return new WebSocketHandshakeInterceptor();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket connections
        return true;
    }

    @Bean
    public WebSocketAuthenticationSecurityConfig webSocketAuthenticationSecurityConfig() {
        return new WebSocketAuthenticationSecurityConfig();
    }

    // Inner class for additional WebSocket security configuration
    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 98)
    public static class WebSocketAuthenticationSecurityConfig {

        // Additional security configurations can be added here
        // For example, custom authentication providers, session management, etc.
    }

    // Custom authentication provider for WebSocket connections
    @Bean
    public WebSocketAuthenticationProvider webSocketAuthenticationProvider() {
        return new WebSocketAuthenticationProvider();
    }

    // Inner class for WebSocket authentication provider
    private static class WebSocketAuthenticationProvider {
        
        public boolean authenticate(String token) {
            // Implement custom authentication logic here
            // For example, validate token, check user roles, etc.
            return true; // Placeholder
        }
        
        public Object getPrincipal(String token) {
            // Return the authenticated principal
            // For example, user details, session info, etc.
            return null; // Placeholder
        }
    }

    // Custom session registry for WebSocket sessions
    @Bean
    public WebSocketSessionRegistry webSocketSessionRegistry() {
        return new WebSocketSessionRegistry();
    }

    // Inner class for WebSocket session registry
    private static class WebSocketSessionRegistry {
        
        public void registerSession(String sessionId, Object principal) {
            // Register new WebSocket session
            // For example, store session details, user info, etc.
        }
        
        public void removeSession(String sessionId) {
            // Remove WebSocket session
            // For example, cleanup resources, update status, etc.
        }
        
        public Object getSession(String sessionId) {
            // Retrieve session information
            // For example, get user details, session status, etc.
            return null; // Placeholder
        }
    }

    // Custom message validator for WebSocket messages
    @Bean
    public WebSocketMessageValidator webSocketMessageValidator() {
        return new WebSocketMessageValidator();
    }

    // Inner class for WebSocket message validation
    private static class WebSocketMessageValidator {
        
        public boolean validateMessage(Object message) {
            // Implement message validation logic
            // For example, check format, size, content, etc.
            return true; // Placeholder
        }
        
        public boolean validateDestination(String destination) {
            // Implement destination validation logic
            // For example, check path, permissions, etc.
            return true; // Placeholder
        }
    }
}