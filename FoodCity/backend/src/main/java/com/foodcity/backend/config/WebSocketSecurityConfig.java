package com.foodcity.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.SimpMessageType;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Allow all connections to the WebSocket endpoint
            .simpTypeMatchers(SimpMessageType.CONNECT).permitAll()
            .simpTypeMatchers(SimpMessageType.HEARTBEAT).permitAll()
            .simpTypeMatchers(SimpMessageType.UNSUBSCRIBE).permitAll()
            .simpTypeMatchers(SimpMessageType.DISCONNECT).permitAll()

            // Configure security for different message destinations
            
            // Public topics that anyone can subscribe to
            .simpSubscribeDestMatchers("/topic/public/**").permitAll()
            .simpDestMatchers("/app/public/**").permitAll()

            // Analytics endpoints - require appropriate roles
            .simpSubscribeDestMatchers("/topic/analytics/**").hasAnyRole("ADMIN", "MANAGER", "ANALYST")
            .simpDestMatchers("/app/analytics/**").hasAnyRole("ADMIN", "MANAGER", "ANALYST")

            // Dashboard endpoints
            .simpSubscribeDestMatchers("/topic/dashboard/**").hasAnyRole("ADMIN", "MANAGER")
            .simpDestMatchers("/app/dashboard/**").hasAnyRole("ADMIN", "MANAGER")

            // Alert endpoints
            .simpSubscribeDestMatchers("/topic/alerts/**").authenticated()
            .simpDestMatchers("/app/alerts/**").hasAnyRole("ADMIN", "MANAGER")

            // System status endpoints
            .simpSubscribeDestMatchers("/topic/system/**").authenticated()
            .simpDestMatchers("/app/system/**").hasRole("ADMIN")

            // User-specific endpoints
            .simpSubscribeDestMatchers("/user/queue/**").authenticated()
            .simpDestMatchers("/app/user/**").authenticated()

            // Inventory endpoints
            .simpSubscribeDestMatchers("/topic/inventory/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY")
            .simpDestMatchers("/app/inventory/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY")

            // Order endpoints
            .simpSubscribeDestMatchers("/topic/orders/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
            .simpDestMatchers("/app/orders/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

            // Customer endpoints
            .simpSubscribeDestMatchers("/topic/customers/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
            .simpDestMatchers("/app/customers/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

            // Employee endpoints
            .simpSubscribeDestMatchers("/topic/employees/**").hasAnyRole("ADMIN", "HR")
            .simpDestMatchers("/app/employees/**").hasAnyRole("ADMIN", "HR")

            // Payment endpoints
            .simpSubscribeDestMatchers("/topic/payments/**").hasAnyRole("ADMIN", "FINANCE")
            .simpDestMatchers("/app/payments/**").hasAnyRole("ADMIN", "FINANCE")

            // Report endpoints
            .simpSubscribeDestMatchers("/topic/reports/**").hasAnyRole("ADMIN", "MANAGER", "ANALYST")
            .simpDestMatchers("/app/reports/**").hasAnyRole("ADMIN", "MANAGER", "ANALYST")

            // Notification endpoints
            .simpSubscribeDestMatchers("/topic/notifications/**").authenticated()
            .simpDestMatchers("/app/notifications/**").hasAnyRole("ADMIN", "MANAGER")

            // Error endpoints
            .simpSubscribeDestMatchers("/topic/errors/**").hasRole("ADMIN")
            .simpDestMatchers("/app/errors/**").hasRole("ADMIN")

            // Group-specific endpoints
            .simpSubscribeDestMatchers("/topic/groups/**").authenticated()
            .simpDestMatchers("/app/groups/**").authenticated()

            // Audit endpoints
            .simpSubscribeDestMatchers("/topic/audit/**").hasRole("ADMIN")
            .simpDestMatchers("/app/audit/**").hasRole("ADMIN")

            // Default deny all other messages
            .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket connections
        return true;
    }
}