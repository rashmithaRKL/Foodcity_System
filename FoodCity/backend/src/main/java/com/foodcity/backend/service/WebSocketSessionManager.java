package com.foodcity.backend.service;

import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.security.WebSocketHandshakeInterceptor.WebSocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final WebSocketService webSocketService;
    
    // Session timeout configurations
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private static final long INACTIVE_TIMEOUT_MINUTES = 15;
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;
    
    // Store active sessions
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    // Store user-session mappings
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // Store subscription mappings
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    /**
     * Register a new WebSocket session
     */
    public void registerSession(String sessionId, WebSocketSession session) {
        SessionInfo sessionInfo = new SessionInfo(session);
        activeSessions.put(sessionId, sessionInfo);
        
        // Add to user sessions mapping
        userSessions.computeIfAbsent(session.getUsername(), 
            k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        
        log.info("Session registered - ID: {}, User: {}", sessionId, session.getUsername());
        
        // Send welcome message
        sendWelcomeMessage(sessionId, session);
    }

    /**
     * Remove a WebSocket session
     */
    public void removeSession(String sessionId) {
        SessionInfo sessionInfo = activeSessions.remove(sessionId);
        if (sessionInfo != null) {
            // Remove from user sessions mapping
            String username = sessionInfo.session.getUsername();
            Set<String> userSessionIds = userSessions.get(username);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(username);
                }
            }
            
            // Remove subscriptions
            sessionSubscriptions.remove(sessionId);
            
            log.info("Session removed - ID: {}, User: {}", sessionId, username);
        }
    }

    /**
     * Add subscription for a session
     */
    public void addSubscription(String sessionId, String destination) {
        sessionSubscriptions.computeIfAbsent(sessionId, 
            k -> ConcurrentHashMap.newKeySet()).add(destination);
        
        log.debug("Subscription added - Session: {}, Destination: {}", sessionId, destination);
    }

    /**
     * Remove subscription from a session
     */
    public void removeSubscription(String sessionId, String destination) {
        Set<String> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            subscriptions.remove(destination);
            if (subscriptions.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }
        
        log.debug("Subscription removed - Session: {}, Destination: {}", sessionId, destination);
    }

    /**
     * Update session activity
     */
    public void updateSessionActivity(String sessionId) {
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        if (sessionInfo != null) {
            sessionInfo.lastActivity = LocalDateTime.now();
        }
    }

    /**
     * Get active session by ID
     */
    public WebSocketSession getSession(String sessionId) {
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        return sessionInfo != null ? sessionInfo.session : null;
    }

    /**
     * Get all sessions for a user
     */
    public Set<String> getUserSessions(String username) {
        return userSessions.getOrDefault(username, ConcurrentHashMap.newKeySet());
    }

    /**
     * Get all subscriptions for a session
     */
    public Set<String> getSessionSubscriptions(String sessionId) {
        return sessionSubscriptions.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
    }

    /**
     * Check if session is active
     */
    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * Get total active sessions count
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Get sessions by subscription
     */
    public Set<String> getSessionsBySubscription(String destination) {
        return sessionSubscriptions.entrySet().stream()
            .filter(entry -> entry.getValue().contains(destination))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * Scheduled task to clean up expired sessions
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo sessionInfo = entry.getValue();
            String sessionId = entry.getKey();
            
            // Check session timeout
            if (ChronoUnit.MINUTES.between(sessionInfo.createdAt, now) > SESSION_TIMEOUT_MINUTES) {
                handleSessionExpiration(sessionId, "Session timeout");
                return true;
            }
            
            // Check inactivity timeout
            if (ChronoUnit.MINUTES.between(sessionInfo.lastActivity, now) > INACTIVE_TIMEOUT_MINUTES) {
                handleSessionExpiration(sessionId, "Inactivity timeout");
                return true;
            }
            
            return false;
        });
    }

    /**
     * Scheduled task to send heartbeat to active sessions
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_SECONDS * 1000)
    public void sendHeartbeat() {
        WebSocketMessage heartbeat = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.SYSTEM_STATUS)
            .destination("/topic/heartbeat")
            .payload(Map.of(
                "timestamp", System.currentTimeMillis(),
                "type", "HEARTBEAT"
            ))
            .status(WebSocketMessage.MessageStatus.SUCCESS)
            .build();
        
        activeSessions.keySet().forEach(sessionId -> {
            try {
                webSocketService.sendMessage("/user/" + sessionId + "/heartbeat", heartbeat);
            } catch (Exception e) {
                log.error("Failed to send heartbeat to session {}: {}", sessionId, e.getMessage());
            }
        });
    }

    // Private helper methods

    private void sendWelcomeMessage(String sessionId, WebSocketSession session) {
        WebSocketMessage welcome = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.SYSTEM_STATUS)
            .destination("/user/queue/welcome")
            .payload(Map.of(
                "message", "Welcome " + session.getUsername(),
                "sessionId", sessionId,
                "timestamp", System.currentTimeMillis()
            ))
            .status(WebSocketMessage.MessageStatus.SUCCESS)
            .build();
        
        webSocketService.sendMessage("/user/" + sessionId + "/queue/welcome", welcome);
    }

    private void handleSessionExpiration(String sessionId, String reason) {
        try {
            // Send expiration message to client
            WebSocketMessage expiration = WebSocketMessage.builder()
                .type(WebSocketMessage.Type.SYSTEM_STATUS)
                .destination("/user/queue/expiration")
                .payload(Map.of(
                    "reason", reason,
                    "sessionId", sessionId,
                    "timestamp", System.currentTimeMillis()
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();
            
            webSocketService.sendMessage("/user/" + sessionId + "/queue/expiration", expiration);
        } catch (Exception e) {
            log.error("Failed to send expiration message to session {}: {}", sessionId, e.getMessage());
        } finally {
            // Clean up session
            removeSession(sessionId);
        }
    }

    // Inner class to track session information
    private static class SessionInfo {
        private final WebSocketSession session;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivity;
        
        public SessionInfo(WebSocketSession session) {
            this.session = session;
            this.createdAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
        }
    }
}