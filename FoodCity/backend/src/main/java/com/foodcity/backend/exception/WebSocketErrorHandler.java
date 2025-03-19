package com.foodcity.backend.exception;

import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    private final WebSocketService webSocketService;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        log.error("Error processing client message: {}", ex.getMessage());

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(clientMessage);
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        // Create error message
        WebSocketMessage errorMessage = createErrorMessage(ex, sessionId, destination);

        // Notify admins about the error
        notifyAdminsAboutError(errorMessage);

        // Log detailed error information
        logDetailedError(clientMessage, ex, sessionId);

        // Convert error message to STOMP error frame
        return createStompErrorFrame(headerAccessor, errorMessage);
    }

    @Override
    public Message<byte[]> handleErrorMessageToClient(Message<byte[]> errorMessage, Throwable ex) {
        log.error("Error sending message to client: {}", ex.getMessage());

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(errorMessage);
        String sessionId = headerAccessor.getSessionId();

        // Create error message
        WebSocketMessage errorResponse = WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", "Error processing request",
                    "details", ex.getMessage(),
                    "sessionId", sessionId
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();

        // Convert to STOMP message
        return createStompMessage(headerAccessor, errorResponse);
    }

    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor headerAccessor, byte[] payload, Throwable ex) {
        log.error("Internal WebSocket error: {}", ex.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        // Handle different types of exceptions
        WebSocketMessage errorMessage;
        if (ex instanceof MessageDeliveryException) {
            errorMessage = handleMessageDeliveryError((MessageDeliveryException) ex, sessionId);
        } else if (ex instanceof SecurityException) {
            errorMessage = handleSecurityError((SecurityException) ex, sessionId);
        } else {
            errorMessage = handleGenericError(ex, sessionId);
        }

        // Log error details
        logError(headerAccessor, ex, sessionId);

        // Return STOMP error frame
        return createStompErrorFrame(headerAccessor, errorMessage);
    }

    private WebSocketMessage createErrorMessage(Throwable ex, String sessionId, String destination) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", ex.getMessage(),
                    "type", ex.getClass().getSimpleName(),
                    "sessionId", sessionId,
                    "destination", destination != null ? destination : "unknown"
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .metadata(Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "errorClass", ex.getClass().getName()
                ))
                .build();
    }

    private void notifyAdminsAboutError(WebSocketMessage errorMessage) {
        try {
            webSocketService.sendMessage("/topic/admin/errors", errorMessage);
        } catch (Exception e) {
            log.error("Failed to notify admins about error: {}", e.getMessage());
        }
    }

    private void logDetailedError(Message<byte[]> clientMessage, Throwable ex, String sessionId) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(clientMessage);
        log.error("WebSocket Error Details:");
        log.error("Session ID: {}", sessionId);
        log.error("Command: {}", headerAccessor.getCommand());
        log.error("Destination: {}", headerAccessor.getDestination());
        log.error("Exception: {}", ex.getMessage());
        log.error("Stack trace:", ex);
    }

    private Message<byte[]> createStompErrorFrame(StompHeaderAccessor headerAccessor, WebSocketMessage errorMessage) {
        StompHeaderAccessor errorHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorHeaderAccessor.setSessionId(headerAccessor.getSessionId());
        errorHeaderAccessor.setMessage(errorMessage.getPayload().get("error").toString());
        errorHeaderAccessor.setLeaveMutable(true);

        byte[] payload = convertToBytes(errorMessage);
        return MessageBuilder.createMessage(payload, errorHeaderAccessor.getMessageHeaders());
    }

    private Message<byte[]> createStompMessage(StompHeaderAccessor headerAccessor, WebSocketMessage message) {
        StompHeaderAccessor messageHeaderAccessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        messageHeaderAccessor.setSessionId(headerAccessor.getSessionId());
        messageHeaderAccessor.setDestination(message.getDestination());
        messageHeaderAccessor.setLeaveMutable(true);

        byte[] payload = convertToBytes(message);
        return MessageBuilder.createMessage(payload, messageHeaderAccessor.getMessageHeaders());
    }

    private WebSocketMessage handleMessageDeliveryError(MessageDeliveryException ex, String sessionId) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", "Message delivery failed",
                    "details", ex.getMessage(),
                    "sessionId", sessionId
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();
    }

    private WebSocketMessage handleSecurityError(SecurityException ex, String sessionId) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", "Security error",
                    "details", "Access denied",
                    "sessionId", sessionId
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();
    }

    private WebSocketMessage handleGenericError(Throwable ex, String sessionId) {
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/user/queue/errors")
                .payload(Map.of(
                    "error", "Internal server error",
                    "details", ex.getMessage(),
                    "sessionId", sessionId
                ))
                .status(WebSocketMessage.MessageStatus.ERROR)
                .build();
    }

    private void logError(StompHeaderAccessor headerAccessor, Throwable ex, String sessionId) {
        log.error("WebSocket Error:");
        log.error("Session ID: {}", sessionId);
        log.error("Command: {}", headerAccessor.getCommand());
        log.error("Message Type: {}", headerAccessor.getMessageType());
        log.error("Exception: {}", ex.getMessage());
    }

    private byte[] convertToBytes(WebSocketMessage message) {
        try {
            return message.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error converting message to bytes: {}", e.getMessage());
            return "Error processing message".getBytes(StandardCharsets.UTF_8);
        }
    }
}