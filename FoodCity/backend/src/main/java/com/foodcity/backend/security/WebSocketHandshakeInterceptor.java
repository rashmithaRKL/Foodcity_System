package com.foodcity.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final String SESSION_ID_KEY = "sessionId";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String ROLES_KEY = "roles";
    private static final String AUTH_TOKEN_KEY = "authToken";

    // Track active WebSocket sessions
    private static final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // Get HTTP session
            HttpSession session = getHttpSession(request);
            if (session == null) {
                log.warn("No HTTP session found for WebSocket handshake");
                return false;
            }

            // Get authentication details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for WebSocket handshake");
                throw new AuthenticationCredentialsNotFoundException("User not authenticated");
            }

            // Extract user details
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Add session attributes
            attributes.put(SESSION_ID_KEY, session.getId());
            attributes.put(USER_ID_KEY, userPrincipal.getId());
            attributes.put(USERNAME_KEY, userPrincipal.getUsername());
            attributes.put(ROLES_KEY, userPrincipal.getAuthorities());
            attributes.put(AUTH_TOKEN_KEY, extractAuthToken(request));

            // Create and store WebSocket session
            WebSocketSession wsSession = new WebSocketSession(
                session.getId(),
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities()
            );
            activeSessions.put(session.getId(), wsSession);

            log.info("WebSocket handshake successful for user: {}", userPrincipal.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Error during WebSocket handshake: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("Error after WebSocket handshake: {}", exception.getMessage());
            String sessionId = getSessionId(request);
            if (sessionId != null) {
                activeSessions.remove(sessionId);
            }
        }
    }

    // Helper methods

    private HttpSession getHttpSession(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            return servletRequest.getServletRequest().getSession(false);
        }
        return null;
    }

    private String getSessionId(ServerHttpRequest request) {
        HttpSession session = getHttpSession(request);
        return session != null ? session.getId() : null;
    }

    private String extractAuthToken(ServerHttpRequest request) {
        String token = null;
        if (request.getHeaders().containsKey("Authorization")) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        return token;
    }

    // Session management methods

    public static WebSocketSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public static void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
        log.info("WebSocket session removed: {}", sessionId);
    }

    public static Map<String, WebSocketSession> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    public static boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    // Inner class to track session details
    public static class WebSocketSession {
        private final String sessionId;
        private final String userId;
        private final String username;
        private final java.util.Collection<?> roles;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivityAt;
        private final Map<String, Object> attributes;

        public WebSocketSession(String sessionId, String userId, String username, 
                              java.util.Collection<?> roles) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.roles = roles;
            this.createdAt = LocalDateTime.now();
            this.lastActivityAt = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        public void updateLastActivity() {
            this.lastActivityAt = LocalDateTime.now();
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public void removeAttribute(String key) {
            attributes.remove(key);
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public java.util.Collection<?> getRoles() {
            return roles;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getLastActivityAt() {
            return lastActivityAt;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public long getSessionDuration() {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
        }

        public long getInactivityDuration() {
            return java.time.Duration.between(lastActivityAt, LocalDateTime.now()).getSeconds();
        }
    }
}