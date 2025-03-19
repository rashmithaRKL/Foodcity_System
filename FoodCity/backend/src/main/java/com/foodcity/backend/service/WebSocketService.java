package com.foodcity.backend.service;

import com.foodcity.backend.payload.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessageSendingOperations messagingTemplate;
    
    // Track active subscriptions and their last update times
    private final Map<String, LocalDateTime> lastUpdates = new ConcurrentHashMap<>();
    
    // Send message to a specific destination
    public void sendMessage(String destination, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
            lastUpdates.put(destination, LocalDateTime.now());
            log.debug("Message sent to {}: {}", destination, message);
        } catch (Exception e) {
            log.error("Error sending message to {}: {}", destination, e.getMessage());
            handleMessageError(destination, message, e);
        }
    }

    // Send message to a specific user
    public void sendMessageToUser(String username, String destination, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(username, destination, message);
            log.debug("Message sent to user {} at {}: {}", username, destination, message);
        } catch (Exception e) {
            log.error("Error sending message to user {} at {}: {}", username, destination, e.getMessage());
            handleMessageError(destination, message, e);
        }
    }

    // Send analytics update
    public void sendAnalyticsUpdate(String metric, Map<String, Object> data) {
        WebSocketMessage message = WebSocketMessage.createAnalyticsUpdate(
            "/topic/analytics/" + metric,
            data
        );
        sendMessage("/topic/analytics/" + metric, message);
    }

    // Send alert
    public void sendAlert(String type, String message, WebSocketMessage.Priority priority) {
        WebSocketMessage alertMessage = WebSocketMessage.createAlert(
            "/topic/alerts/" + type,
            message,
            priority
        );
        sendMessage("/topic/alerts/" + type, alertMessage);
    }

    // Send system notification
    public void sendSystemNotification(String title, String message) {
        WebSocketMessage notification = WebSocketMessage.createNotification(
            "/topic/notifications/system",
            title,
            message
        );
        sendMessage("/topic/notifications/system", notification);
    }

    // Send user notification
    public void sendUserNotification(String username, String title, String message) {
        WebSocketMessage notification = WebSocketMessage.createNotification(
            "/user/queue/notifications",
            title,
            message
        );
        sendMessageToUser(username, "/queue/notifications", notification);
    }

    // Send progress update
    public void sendProgressUpdate(String destination, int progress, String details) {
        WebSocketMessage progressMessage = WebSocketMessage.createProgressUpdate(
            destination,
            progress,
            details
        );
        sendMessage(destination, progressMessage);
    }

    // Send error message
    public void sendError(String destination, String error, String details) {
        WebSocketMessage errorMessage = WebSocketMessage.createError(
            destination,
            error,
            details
        );
        sendMessage(destination, errorMessage);
    }

    // Send data sync message
    public void sendDataSync(String destination, Map<String, Object> data) {
        WebSocketMessage syncMessage = WebSocketMessage.createDataSync(
            destination,
            data
        );
        sendMessage(destination, syncMessage);
    }

    // Send system status update
    public void sendSystemStatus(String status, Map<String, Object> details) {
        WebSocketMessage statusMessage = WebSocketMessage.createSystemStatus(
            "/topic/system/status",
            status,
            details
        );
        sendMessage("/topic/system/status", statusMessage);
    }

    // Broadcast message to all connected clients
    public void broadcast(WebSocketMessage message) {
        sendMessage("/topic/broadcast", message);
    }

    // Send message to a specific group
    public void sendToGroup(String group, WebSocketMessage message) {
        sendMessage("/topic/groups/" + group, message);
    }

    // Send real-time analytics updates
    public void sendRealTimeAnalytics(String metric, Map<String, Object> data) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.ANALYTICS_UPDATE)
            .destination("/topic/analytics/realtime/" + metric)
            .timestamp(LocalDateTime.now())
            .payload(data)
            .status(WebSocketMessage.MessageStatus.SUCCESS)
            .build();
        
        sendMessage("/topic/analytics/realtime/" + metric, message);
    }

    // Send dashboard updates
    public void sendDashboardUpdate(Map<String, Object> dashboardData) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.DASHBOARD_UPDATE)
            .destination("/topic/dashboard/updates")
            .timestamp(LocalDateTime.now())
            .payload(dashboardData)
            .status(WebSocketMessage.MessageStatus.SUCCESS)
            .build();
        
        sendMessage("/topic/dashboard/updates", message);
    }

    // Handle message errors
    private void handleMessageError(String destination, WebSocketMessage message, Exception e) {
        // Create error message
        WebSocketMessage errorMessage = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.ERROR)
            .destination(destination)
            .timestamp(LocalDateTime.now())
            .payload(Map.of(
                "originalMessage", message,
                "error", e.getMessage()
            ))
            .status(WebSocketMessage.MessageStatus.ERROR)
            .build();
        
        try {
            // Try to send error message
            messagingTemplate.convertAndSend("/topic/errors", errorMessage);
        } catch (Exception ex) {
            log.error("Failed to send error message: {}", ex.getMessage());
        }
    }

    // Check if a destination has recent updates
    public boolean hasRecentUpdates(String destination, long thresholdSeconds) {
        LocalDateTime lastUpdate = lastUpdates.get(destination);
        if (lastUpdate == null) {
            return false;
        }
        return lastUpdate.plusSeconds(thresholdSeconds).isAfter(LocalDateTime.now());
    }

    // Clear old update timestamps
    public void clearOldUpdates(long thresholdSeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(thresholdSeconds);
        lastUpdates.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }
}