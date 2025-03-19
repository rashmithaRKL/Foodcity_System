package com.foodcity.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    private String type;
    private String destination;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
    private Map<String, Object> metadata;
    private MessageStatus status;

    // Message Types
    public static class Type {
        // Analytics Messages
        public static final String ANALYTICS_UPDATE = "ANALYTICS_UPDATE";
        public static final String METRICS_UPDATE = "METRICS_UPDATE";
        public static final String DASHBOARD_UPDATE = "DASHBOARD_UPDATE";
        public static final String TREND_UPDATE = "TREND_UPDATE";
        public static final String FORECAST_UPDATE = "FORECAST_UPDATE";
        
        // Alert Messages
        public static final String ALERT = "ALERT";
        public static final String NOTIFICATION = "NOTIFICATION";
        public static final String WARNING = "WARNING";
        public static final String ERROR = "ERROR";
        
        // Status Messages
        public static final String STATUS_UPDATE = "STATUS_UPDATE";
        public static final String PROGRESS_UPDATE = "PROGRESS_UPDATE";
        public static final String COMPLETION = "COMPLETION";
        
        // Data Messages
        public static final String DATA_UPDATE = "DATA_UPDATE";
        public static final String DATA_SYNC = "DATA_SYNC";
        public static final String DATA_VALIDATION = "DATA_VALIDATION";
        
        // User Messages
        public static final String USER_ACTION = "USER_ACTION";
        public static final String USER_PREFERENCE = "USER_PREFERENCE";
        public static final String USER_STATUS = "USER_STATUS";
        
        // System Messages
        public static final String SYSTEM_STATUS = "SYSTEM_STATUS";
        public static final String SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    }

    // Message Status
    public enum MessageStatus {
        SUCCESS,
        ERROR,
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    // Message Priority
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Static factory methods for common message types
    public static WebSocketMessage createAnalyticsUpdate(String destination, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(Type.ANALYTICS_UPDATE)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(data)
                .status(MessageStatus.SUCCESS)
                .build();
    }

    public static WebSocketMessage createAlert(String destination, String message, Priority priority) {
        return WebSocketMessage.builder()
                .type(Type.ALERT)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "message", message,
                    "priority", priority
                ))
                .status(MessageStatus.SUCCESS)
                .build();
    }

    public static WebSocketMessage createStatusUpdate(String destination, String status, String details) {
        return WebSocketMessage.builder()
                .type(Type.STATUS_UPDATE)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "status", status,
                    "details", details
                ))
                .status(MessageStatus.SUCCESS)
                .build();
    }

    public static WebSocketMessage createError(String destination, String error, String details) {
        return WebSocketMessage.builder()
                .type(Type.ERROR)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "error", error,
                    "details", details
                ))
                .status(MessageStatus.ERROR)
                .build();
    }

    public static WebSocketMessage createProgressUpdate(String destination, int progress, String details) {
        return WebSocketMessage.builder()
                .type(Type.PROGRESS_UPDATE)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "progress", progress,
                    "details", details
                ))
                .status(MessageStatus.PROCESSING)
                .build();
    }

    public static WebSocketMessage createNotification(String destination, String title, String message) {
        return WebSocketMessage.builder()
                .type(Type.NOTIFICATION)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "title", title,
                    "message", message
                ))
                .status(MessageStatus.SUCCESS)
                .build();
    }

    public static WebSocketMessage createDataSync(String destination, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type(Type.DATA_SYNC)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(data)
                .status(MessageStatus.SUCCESS)
                .build();
    }

    public static WebSocketMessage createSystemStatus(String destination, String status, Map<String, Object> details) {
        return WebSocketMessage.builder()
                .type(Type.SYSTEM_STATUS)
                .destination(destination)
                .timestamp(LocalDateTime.now())
                .payload(Map.of(
                    "status", status,
                    "details", details
                ))
                .status(MessageStatus.SUCCESS)
                .build();
    }

    // Utility methods
    public boolean isSuccess() {
        return MessageStatus.SUCCESS.equals(this.status);
    }

    public boolean isError() {
        return MessageStatus.ERROR.equals(this.status);
    }

    public boolean isProcessing() {
        return MessageStatus.PROCESSING.equals(this.status);
    }

    public boolean isCompleted() {
        return MessageStatus.COMPLETED.equals(this.status);
    }

    public boolean isFailed() {
        return MessageStatus.FAILED.equals(this.status);
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = Map.of(key, value);
        } else {
            this.metadata.put(key, value);
        }
    }

    public void updateStatus(MessageStatus newStatus) {
        this.status = newStatus;
        this.timestamp = LocalDateTime.now();
    }

    public void updatePayload(Map<String, Object> newPayload) {
        this.payload = newPayload;
        this.timestamp = LocalDateTime.now();
    }
}