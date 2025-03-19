package com.foodcity.backend.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    
    // Track active sessions and their subscriptions
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Create new session tracking object
        WebSocketSession session = new WebSocketSession(sessionId);
        
        // Add user details if authenticated
        if (headerAccessor.getUser() != null) {
            session.setUsername(headerAccessor.getUser().getName());
        }
        
        // Store session
        activeSessions.put(sessionId, session);
        
        log.info("New WebSocket Connection - Session ID: {}, Total Active Sessions: {}", 
                sessionId, activeSessions.size());
        
        // Notify admins about new connection
        notifyAdminsAboutConnection(session, true);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        WebSocketSession session = activeSessions.remove(sessionId);
        
        if (session != null) {
            log.info("WebSocket Disconnection - Session ID: {}, Username: {}, Total Active Sessions: {}", 
                    sessionId, session.getUsername(), activeSessions.size());
            
            // Notify admins about disconnection
            notifyAdminsAboutConnection(session, false);
            
            // Clean up any user-specific resources
            cleanupUserResources(session);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.addSubscription(destination);
            
            log.info("New subscription - Session ID: {}, Destination: {}, Total Subscriptions: {}", 
                    sessionId, destination, session.getSubscriptions().size());
            
            // Handle specific subscription types
            handleSpecificSubscription(session, destination);
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.removeSubscription(destination);
            
            log.info("Unsubscription - Session ID: {}, Destination: {}, Remaining Subscriptions: {}", 
                    sessionId, destination, session.getSubscriptions().size());
            
            // Clean up subscription-specific resources
            cleanupSubscriptionResources(session, destination);
        }
    }

    // Helper methods

    private void notifyAdminsAboutConnection(WebSocketSession session, boolean isConnection) {
        Map<String, Object> message = Map.of(
            "type", isConnection ? "CONNECTION" : "DISCONNECTION",
            "sessionId", session.getSessionId(),
            "username", session.getUsername(),
            "timestamp", System.currentTimeMillis(),
            "activeConnections", activeSessions.size()
        );
        
        messagingTemplate.convertAndSend("/topic/admin/connections", message);
    }

    private void handleSpecificSubscription(WebSocketSession session, String destination) {
        // Handle different types of subscriptions
        if (destination.startsWith("/topic/analytics")) {
            handleAnalyticsSubscription(session, destination);
        } else if (destination.startsWith("/topic/alerts")) {
            handleAlertsSubscription(session, destination);
        } else if (destination.startsWith("/user/queue")) {
            handleUserSpecificSubscription(session, destination);
        }
    }

    private void handleAnalyticsSubscription(WebSocketSession session, String destination) {
        // Send initial analytics data
        Map<String, Object> initialData = Map.of(
            "type", "INITIAL_DATA",
            "destination", destination,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(
            session.getSessionId(),
            destination,
            initialData
        );
    }

    private void handleAlertsSubscription(WebSocketSession session, String destination) {
        // Send any pending alerts
        Map<String, Object> pendingAlerts = Map.of(
            "type", "PENDING_ALERTS",
            "destination", destination,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(
            session.getSessionId(),
            destination,
            pendingAlerts
        );
    }

    private void handleUserSpecificSubscription(WebSocketSession session, String destination) {
        // Handle user-specific subscriptions
        Map<String, Object> userData = Map.of(
            "type", "USER_DATA",
            "destination", destination,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(
            session.getSessionId(),
            destination,
            userData
        );
    }

    private void cleanupUserResources(WebSocketSession session) {
        // Clean up any user-specific resources or subscriptions
        session.getSubscriptions().forEach(subscription -> 
            cleanupSubscriptionResources(session, subscription));
    }

    private void cleanupSubscriptionResources(WebSocketSession session, String subscription) {
        // Clean up resources based on subscription type
        if (subscription.startsWith("/topic/analytics")) {
            cleanupAnalyticsResources(session, subscription);
        } else if (subscription.startsWith("/topic/alerts")) {
            cleanupAlertsResources(session, subscription);
        } else if (subscription.startsWith("/user/queue")) {
            cleanupUserSpecificResources(session, subscription);
        }
    }

    private void cleanupAnalyticsResources(WebSocketSession session, String subscription) {
        // Cleanup analytics-related resources
        log.debug("Cleaning up analytics resources for session: {}, subscription: {}", 
                session.getSessionId(), subscription);
    }

    private void cleanupAlertsResources(WebSocketSession session, String subscription) {
        // Cleanup alerts-related resources
        log.debug("Cleaning up alerts resources for session: {}, subscription: {}", 
                session.getSessionId(), subscription);
    }

    private void cleanupUserSpecificResources(WebSocketSession session, String subscription) {
        // Cleanup user-specific resources
        log.debug("Cleaning up user-specific resources for session: {}, subscription: {}", 
                session.getSessionId(), subscription);
    }

    // Inner class to track session details
    private static class WebSocketSession {
        private final String sessionId;
        private String username;
        private final Map<String, Long> subscriptions = new ConcurrentHashMap<>();
        
        public WebSocketSession(String sessionId) {
            this.sessionId = sessionId;
            this.username = "anonymous";
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Map<String, Long> getSubscriptions() {
            return subscriptions;
        }
        
        public void addSubscription(String destination) {
            subscriptions.put(destination, System.currentTimeMillis());
        }
        
        public void removeSubscription(String destination) {
            subscriptions.remove(destination);
        }
    }
}