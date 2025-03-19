package com.foodcity.backend.validator;

import com.foodcity.backend.payload.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketMessageValidator {

    // Constants for validation
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_PAYLOAD_SIZE = 512 * 1024; // 512KB
    private static final int MAX_DESTINATION_LENGTH = 255;
    private static final int MAX_TYPE_LENGTH = 50;
    
    // Regular expressions for validation
    private static final Pattern DESTINATION_PATTERN = Pattern.compile("^/[a-zA-Z0-9/._-]+$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^[A-Z_]+$");
    
    // Message type whitelist
    private static final List<String> ALLOWED_TYPES = List.of(
        WebSocketMessage.Type.ANALYTICS_UPDATE,
        WebSocketMessage.Type.METRICS_UPDATE,
        WebSocketMessage.Type.DASHBOARD_UPDATE,
        WebSocketMessage.Type.TREND_UPDATE,
        WebSocketMessage.Type.FORECAST_UPDATE,
        WebSocketMessage.Type.ALERT,
        WebSocketMessage.Type.NOTIFICATION,
        WebSocketMessage.Type.WARNING,
        WebSocketMessage.Type.ERROR,
        WebSocketMessage.Type.STATUS_UPDATE,
        WebSocketMessage.Type.PROGRESS_UPDATE,
        WebSocketMessage.Type.COMPLETION,
        WebSocketMessage.Type.DATA_UPDATE,
        WebSocketMessage.Type.DATA_SYNC,
        WebSocketMessage.Type.DATA_VALIDATION,
        WebSocketMessage.Type.USER_ACTION,
        WebSocketMessage.Type.USER_PREFERENCE,
        WebSocketMessage.Type.USER_STATUS,
        WebSocketMessage.Type.SYSTEM_STATUS,
        WebSocketMessage.Type.SYSTEM_MAINTENANCE,
        WebSocketMessage.Type.SYSTEM_ERROR
    );

    /**
     * Validate a WebSocket message
     */
    public ValidationResult validateMessage(WebSocketMessage message) {
        ValidationResult result = new ValidationResult();

        // Check for null message
        if (message == null) {
            result.addError("Message cannot be null");
            return result;
        }

        // Validate message type
        validateType(message.getType(), result);

        // Validate destination
        validateDestination(message.getDestination(), result);

        // Validate timestamp
        validateTimestamp(message.getTimestamp(), result);

        // Validate payload
        validatePayload(message.getPayload(), result);

        // Validate metadata
        validateMetadata(message.getMetadata(), result);

        // Validate status
        validateStatus(message.getStatus(), result);

        // Check message size
        validateMessageSize(message, result);

        return result;
    }

    /**
     * Validate message type
     */
    private void validateType(String type, ValidationResult result) {
        if (type == null || type.isEmpty()) {
            result.addError("Message type cannot be null or empty");
            return;
        }

        if (type.length() > MAX_TYPE_LENGTH) {
            result.addError("Message type exceeds maximum length");
            return;
        }

        if (!TYPE_PATTERN.matcher(type).matches()) {
            result.addError("Invalid message type format");
            return;
        }

        if (!ALLOWED_TYPES.contains(type)) {
            result.addError("Unsupported message type: " + type);
        }
    }

    /**
     * Validate message destination
     */
    private void validateDestination(String destination, ValidationResult result) {
        if (destination == null || destination.isEmpty()) {
            result.addError("Message destination cannot be null or empty");
            return;
        }

        if (destination.length() > MAX_DESTINATION_LENGTH) {
            result.addError("Destination exceeds maximum length");
            return;
        }

        if (!DESTINATION_PATTERN.matcher(destination).matches()) {
            result.addError("Invalid destination format");
        }
    }

    /**
     * Validate message timestamp
     */
    private void validateTimestamp(java.time.LocalDateTime timestamp, ValidationResult result) {
        if (timestamp == null) {
            result.addError("Message timestamp cannot be null");
            return;
        }

        if (timestamp.isAfter(java.time.LocalDateTime.now().plusMinutes(1))) {
            result.addError("Message timestamp cannot be in the future");
        }
    }

    /**
     * Validate message payload
     */
    private void validatePayload(Map<String, Object> payload, ValidationResult result) {
        if (payload == null) {
            result.addError("Message payload cannot be null");
            return;
        }

        // Check payload size
        int payloadSize = estimatePayloadSize(payload);
        if (payloadSize > MAX_PAYLOAD_SIZE) {
            result.addError("Payload size exceeds maximum limit");
            return;
        }

        // Validate payload structure
        validatePayloadStructure(payload, result);
    }

    /**
     * Validate message metadata
     */
    private void validateMetadata(Map<String, Object> metadata, ValidationResult result) {
        if (metadata != null) {
            // Validate metadata structure
            validateMetadataStructure(metadata, result);
        }
    }

    /**
     * Validate message status
     */
    private void validateStatus(WebSocketMessage.MessageStatus status, ValidationResult result) {
        if (status == null) {
            result.addError("Message status cannot be null");
        }
    }

    /**
     * Validate message size
     */
    private void validateMessageSize(WebSocketMessage message, ValidationResult result) {
        int totalSize = estimateMessageSize(message);
        if (totalSize > MAX_MESSAGE_SIZE) {
            result.addError("Total message size exceeds maximum limit");
        }
    }

    /**
     * Validate payload structure
     */
    private void validatePayloadStructure(Map<String, Object> payload, ValidationResult result) {
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Validate key
            if (key == null || key.isEmpty()) {
                result.addError("Payload contains null or empty key");
                continue;
            }

            // Validate value
            if (value == null) {
                result.addError("Payload contains null value for key: " + key);
                continue;
            }

            // Validate nested structures
            if (value instanceof Map) {
                validatePayloadStructure((Map<String, Object>) value, result);
            }
        }
    }

    /**
     * Validate metadata structure
     */
    private void validateMetadataStructure(Map<String, Object> metadata, ValidationResult result) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Validate key
            if (key == null || key.isEmpty()) {
                result.addError("Metadata contains null or empty key");
                continue;
            }

            // Validate value
            if (value == null) {
                result.addError("Metadata contains null value for key: " + key);
            }
        }
    }

    /**
     * Estimate payload size
     */
    private int estimatePayloadSize(Map<String, Object> payload) {
        // Simple estimation based on string representation
        return payload.toString().length();
    }

    /**
     * Estimate total message size
     */
    private int estimateMessageSize(WebSocketMessage message) {
        int size = 0;
        
        // Add type size
        size += message.getType() != null ? message.getType().length() : 0;
        
        // Add destination size
        size += message.getDestination() != null ? message.getDestination().length() : 0;
        
        // Add payload size
        size += message.getPayload() != null ? estimatePayloadSize(message.getPayload()) : 0;
        
        // Add metadata size
        size += message.getMetadata() != null ? estimatePayloadSize(message.getMetadata()) : 0;
        
        return size;
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final List<String> errors;
        
        public ValidationResult() {
            this.errors = new ArrayList<>();
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}