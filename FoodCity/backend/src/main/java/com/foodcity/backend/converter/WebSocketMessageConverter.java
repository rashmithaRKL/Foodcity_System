package com.foodcity.backend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodcity.backend.payload.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WebSocketMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper;

    public WebSocketMessageConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
        try {
            Object payload = message.getPayload();
            String jsonString;

            if (payload instanceof byte[]) {
                jsonString = new String((byte[]) payload, StandardCharsets.UTF_8);
            } else if (payload instanceof String) {
                jsonString = (String) payload;
            } else {
                throw new IllegalArgumentException("Unsupported payload type: " + payload.getClass());
            }

            // Convert JSON to WebSocketMessage
            WebSocketMessage webSocketMessage = objectMapper.readValue(jsonString, WebSocketMessage.class);

            // Add message headers to metadata
            addHeadersToMetadata(webSocketMessage, message.getHeaders());

            return webSocketMessage;

        } catch (IOException e) {
            log.error("Error deserializing WebSocket message: {}", e.getMessage());
            throw new MessageConversionException("Failed to deserialize message", e);
        }
    }

    @Override
    public Message<?> toMessage(Object payload, MessageHeaders headers) {
        try {
            WebSocketMessage message;
            if (payload instanceof WebSocketMessage) {
                message = (WebSocketMessage) payload;
            } else {
                // Convert other types to WebSocketMessage
                message = convertToWebSocketMessage(payload);
            }

            // Add headers to metadata
            addHeadersToMetadata(message, headers);

            // Convert to JSON
            String jsonString = objectMapper.writeValueAsString(message);
            byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);

            return new GenericMessage<>(bytes, headers);

        } catch (JsonProcessingException e) {
            log.error("Error serializing WebSocket message: {}", e.getMessage());
            throw new MessageConversionException("Failed to serialize message", e);
        }
    }

    /**
     * Convert arbitrary object to WebSocketMessage
     */
    private WebSocketMessage convertToWebSocketMessage(Object payload) {
        try {
            // Convert payload to map
            Map<String, Object> payloadMap;
            if (payload instanceof Map) {
                payloadMap = (Map<String, Object>) payload;
            } else {
                String json = objectMapper.writeValueAsString(payload);
                payloadMap = objectMapper.readValue(json, Map.class);
            }

            return WebSocketMessage.builder()
                    .type(WebSocketMessage.Type.DATA_UPDATE)
                    .destination("/topic/data")
                    .payload(payloadMap)
                    .timestamp(java.time.LocalDateTime.now())
                    .status(WebSocketMessage.MessageStatus.SUCCESS)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error converting payload to WebSocketMessage: {}", e.getMessage());
            throw new MessageConversionException("Failed to convert payload", e);
        }
    }

    /**
     * Add message headers to metadata
     */
    private void addHeadersToMetadata(WebSocketMessage message, MessageHeaders headers) {
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        // Add relevant headers to metadata
        headers.forEach((key, value) -> {
            if (isRelevantHeader(key)) {
                metadata.put(key, value);
            }
        });

        // Add timestamp if not present
        if (!metadata.containsKey("timestamp")) {
            metadata.put("timestamp", System.currentTimeMillis());
        }

        message.setMetadata(metadata);
    }

    /**
     * Check if header should be included in metadata
     */
    private boolean isRelevantHeader(String header) {
        return !header.startsWith("nativeHeaders") &&
               !header.equals("simpMessageType") &&
               !header.equals("simpSessionAttributes");
    }

    /**
     * Custom exception for message conversion errors
     */
    public static class MessageConversionException extends RuntimeException {
        public MessageConversionException(String message) {
            super(message);
        }

        public MessageConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Convert WebSocketMessage to bytes
     */
    public byte[] convertToBytes(WebSocketMessage message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            log.error("Error converting message to bytes: {}", e.getMessage());
            throw new MessageConversionException("Failed to convert message to bytes", e);
        }
    }

    /**
     * Convert bytes to WebSocketMessage
     */
    public WebSocketMessage convertFromBytes(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, WebSocketMessage.class);
        } catch (IOException e) {
            log.error("Error converting bytes to message: {}", e.getMessage());
            throw new MessageConversionException("Failed to convert bytes to message", e);
        }
    }

    /**
     * Convert object to WebSocketMessage with custom type and destination
     */
    public WebSocketMessage convertToWebSocketMessage(Object payload, String type, String destination) {
        try {
            Map<String, Object> payloadMap;
            if (payload instanceof Map) {
                payloadMap = (Map<String, Object>) payload;
            } else {
                String json = objectMapper.writeValueAsString(payload);
                payloadMap = objectMapper.readValue(json, Map.class);
            }

            return WebSocketMessage.builder()
                    .type(type)
                    .destination(destination)
                    .payload(payloadMap)
                    .timestamp(java.time.LocalDateTime.now())
                    .status(WebSocketMessage.MessageStatus.SUCCESS)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error converting payload to WebSocketMessage: {}", e.getMessage());
            throw new MessageConversionException("Failed to convert payload", e);
        }
    }

    /**
     * Convert WebSocketMessage to specific type
     */
    public <T> T convertToType(WebSocketMessage message, Class<T> targetType) {
        try {
            String json = objectMapper.writeValueAsString(message.getPayload());
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            log.error("Error converting message to type {}: {}", targetType, e.getMessage());
            throw new MessageConversionException("Failed to convert message to type", e);
        }
    }

    /**
     * Get ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}