package com.foodcity.backend.security;

import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final WebSocketService webSocketService;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            String sessionId = accessor.getSessionId();
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Handle connection
                handleConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                // Handle subscription
                handleSubscribe(accessor);
            } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                // Handle message sending
                handleMessageSend(accessor, message);
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                // Handle disconnection
                handleDisconnect(accessor);
            }

            // Update session activity
            if (sessionId != null) {
                WebSocketHandshakeInterceptor.WebSocketSession session = 
                    WebSocketHandshakeInterceptor.getSession(sessionId);
                if (session != null) {
                    session.updateLastActivity();
                }
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && sent) {
            String sessionId = accessor.getSessionId();
            StompCommand command = accessor.getCommand();
            
            // Log successful message delivery
            log.debug("Message sent successfully - Session: {}, Command: {}", sessionId, command);
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, 
                                  boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            String sessionId = accessor != null ? accessor.getSessionId() : "unknown";
            
            log.error("Error after message send - Session: {}, Error: {}", sessionId, ex.getMessage());
            
            // Notify client about error
            notifyClientError(sessionId, ex);
        }
    }

    // Private helper methods

    private void handleConnect(StompHeaderAccessor accessor) {
        try {
            // Extract JWT token
            String token = extractToken(accessor);
            
            if (token != null && tokenProvider.validateToken(token)) {
                // Set authentication
                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                accessor.setUser(auth);
                
                // Store user details in session
                storeUserDetails(accessor, auth);
                
                log.info("WebSocket connection authenticated - User: {}", 
                    auth.getName());
            } else {
                log.warn("Invalid token in WebSocket connection");
                throw new SecurityException("Invalid authentication token");
            }
        } catch (Exception e) {
            log.error("Error during WebSocket connection: {}", e.getMessage());
            throw new SecurityException("Connection authentication failed");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        try {
            String destination = accessor.getDestination();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (!isSubscriptionAllowed(destination, auth)) {
                log.warn("Unauthorized subscription attempt - User: {}, Destination: {}", 
                    auth.getName(), destination);
                throw new SecurityException("Unauthorized subscription");
            }
            
            log.debug("Subscription authorized - User: {}, Destination: {}", 
                auth.getName(), destination);
        } catch (Exception e) {
            log.error("Error during subscription: {}", e.getMessage());
            throw new SecurityException("Subscription failed");
        }
    }

    private void handleMessageSend(StompHeaderAccessor accessor, Message<?> message) {
        try {
            String destination = accessor.getDestination();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Validate message
            validateMessage(message);
            
            // Check authorization
            if (!isMessageAllowed(destination, auth)) {
                log.warn("Unauthorized message send attempt - User: {}, Destination: {}", 
                    auth.getName(), destination);
                throw new SecurityException("Unauthorized message");
            }
            
            log.debug("Message send authorized - User: {}, Destination: {}", 
                auth.getName(), destination);
        } catch (Exception e) {
            log.error("Error during message send: {}", e.getMessage());
            throw new SecurityException("Message send failed");
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            WebSocketHandshakeInterceptor.removeSession(sessionId);
            log.info("WebSocket disconnected - Session: {}", sessionId);
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String token = null;
        
        // Try to get token from headers
        if (accessor.getNativeHeader("Authorization") != null) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        return token;
    }

    private void storeUserDetails(StompHeaderAccessor accessor, Authentication auth) {
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            accessor.setSessionAttributes(Map.of(
                "userId", principal.getId(),
                "username", principal.getUsername(),
                "roles", principal.getAuthorities()
            ));
        }
    }

    private boolean isSubscriptionAllowed(String destination, Authentication auth) {
        // Implement subscription authorization logic
        // Example: Check if user has required roles for the destination
        return true; // Placeholder
    }

    private boolean isMessageAllowed(String destination, Authentication auth) {
        // Implement message authorization logic
        // Example: Check if user has required roles for the destination
        return true; // Placeholder
    }

    private void validateMessage(Message<?> message) {
        // Implement message validation logic
        // Example: Check message format, size, content, etc.
    }

    private void notifyClientError(String sessionId, Exception ex) {
        try {
            WebSocketMessage errorMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", "Message processing error",
                    "details", ex.getMessage()
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();
            
            webSocketService.sendMessage("/user/" + sessionId + "/queue/errors", errorMessage);
        } catch (Exception e) {
            log.error("Failed to send error notification to client: {}", e.getMessage());
        }
    }
}