package com.foodcity.backend.factory;

import com.foodcity.backend.payload.WebSocketMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketMessageFactory {

    /**
     * Create analytics message
     */
    public WebSocketMessage createAnalyticsMessage(String subType, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ANALYTICS_UPDATE)
                .destination("/topic/analytics/" + subType)
                .payload(data)
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("analytics", subType))
                .build();
    }

    /**
     * Create alert message
     */
    public WebSocketMessage createAlertMessage(String message, WebSocketMessage.Priority priority) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ALERT)
                .destination("/topic/alerts")
                .payload(Map.of(
                    "message", message,
                    "priority", priority,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("alert", priority.toString()))
                .build();
    }

    /**
     * Create notification message
     */
    public WebSocketMessage createNotificationMessage(String title, String message, String type) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.NOTIFICATION)
                .destination("/topic/notifications")
                .payload(Map.of(
                    "title", title,
                    "message", message,
                    "type", type,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("notification", type))
                .build();
    }

    /**
     * Create error message
     */
    public WebSocketMessage createErrorMessage(String error, String details) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/topic/errors")
                .payload(Map.of(
                    "error", error,
                    "details", details,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.ERROR)
                .metadata(createMetadata("error", null))
                .build();
    }

    /**
     * Create status update message
     */
    public WebSocketMessage createStatusMessage(String status, Map<String, Object> details) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.STATUS_UPDATE)
                .destination("/topic/status")
                .payload(Map.of(
                    "status", status,
                    "details", details,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("status", status))
                .build();
    }

    /**
     * Create progress update message
     */
    public WebSocketMessage createProgressMessage(int progress, String details) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.PROGRESS_UPDATE)
                .destination("/topic/progress")
                .payload(Map.of(
                    "progress", progress,
                    "details", details,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("progress", String.valueOf(progress)))
                .build();
    }

    /**
     * Create data update message
     */
    public WebSocketMessage createDataMessage(String dataType, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.DATA_UPDATE)
                .destination("/topic/data/" + dataType)
                .payload(data)
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("data", dataType))
                .build();
    }

    /**
     * Create user-specific message
     */
    public WebSocketMessage createUserMessage(String userId, String type, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(type)
                .destination("/user/" + userId + "/queue/messages")
                .payload(data)
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("user", userId))
                .build();
    }

    /**
     * Create system message
     */
    public WebSocketMessage createSystemMessage(String type, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.SYSTEM_STATUS)
                .destination("/topic/system/" + type)
                .payload(data)
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("system", type))
                .build();
    }

    /**
     * Create heartbeat message
     */
    public WebSocketMessage createHeartbeatMessage() {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.SYSTEM_STATUS)
                .destination("/topic/heartbeat")
                .payload(Map.of(
                    "type", "HEARTBEAT",
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("system", "heartbeat"))
                .build();
    }

    /**
     * Create broadcast message
     */
    public WebSocketMessage createBroadcastMessage(String message) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.NOTIFICATION)
                .destination("/topic/broadcast")
                .payload(Map.of(
                    "message", message,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .metadata(createMetadata("broadcast", null))
                .build();
    }

    /**
     * Create validation error message
     */
    public WebSocketMessage createValidationErrorMessage(String error, List<String> details) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/topic/errors/validation")
                .payload(Map.of(
                    "error", error,
                    "details", details,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.ERROR)
                .metadata(createMetadata("validation", "error"))
                .build();
    }

    /**
     * Create session expired message
     */
    public WebSocketMessage createSessionExpiredMessage(String sessionId) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.SYSTEM_STATUS)
                .destination("/user/" + sessionId + "/queue/session")
                .payload(Map.of(
                    "status", "EXPIRED",
                    "sessionId", sessionId,
                    "timestamp", System.currentTimeMillis()
                ))
                .timestamp(LocalDateTime.now())
                .status(WebSocketMessage.MessageStatus.ERROR)
                .metadata(createMetadata("session", "expired"))
                .build();
    }

    // Private helper methods

    private Map<String, Object> createMetadata(String category, String subCategory) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", category);
        if (subCategory != null) {
            metadata.put("subCategory", subCategory);
        }
        metadata.put("createdAt", System.currentTimeMillis());
        return metadata;
    }
}