package com.foodcity.backend.processor;

import com.foodcity.backend.converter.WebSocketMessageConverter;
import com.foodcity.backend.factory.WebSocketMessageFactory;
import com.foodcity.backend.handler.WebSocketMessageHandler;
import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.service.WebSocketService;
import com.foodcity.backend.validator.WebSocketMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageProcessor {

    private final WebSocketService webSocketService;
    private final WebSocketMessageHandler messageHandler;
    private final WebSocketMessageConverter messageConverter;
    private final WebSocketMessageValidator messageValidator;
    private final WebSocketMessageFactory messageFactory;

    // Thread pool for async processing
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // Message processors registry
    private final List<MessageProcessor> processors = new ArrayList<>();

    /**
     * Process incoming message
     */
    public void processMessage(WebSocketMessage message, String sessionId) {
        try {
            // Validate message
            WebSocketMessageValidator.ValidationResult validationResult = 
                messageValidator.validateMessage(message);
            
            if (validationResult.hasErrors()) {
                handleValidationError(message, sessionId, validationResult);
                return;
            }

            // Apply processors
            WebSocketMessage processedMessage = applyProcessors(message);

            // Handle message
            CompletableFuture.runAsync(() -> 
                messageHandler.handleMessage(processedMessage, sessionId), executorService)
                .exceptionally(throwable -> {
                    handleProcessingError(message, sessionId, throwable);
                    return null;
                });

        } catch (Exception e) {
            handleProcessingError(message, sessionId, e);
        }
    }

    /**
     * Process outgoing message
     */
    public WebSocketMessage processOutgoingMessage(WebSocketMessage message) {
        try {
            // Validate message
            WebSocketMessageValidator.ValidationResult validationResult = 
                messageValidator.validateMessage(message);
            
            if (validationResult.hasErrors()) {
                throw new MessageProcessingException("Message validation failed: " + 
                    validationResult.getErrorMessage());
            }

            // Apply processors
            return applyProcessors(message);

        } catch (Exception e) {
            log.error("Error processing outgoing message: {}", e.getMessage());
            throw new MessageProcessingException("Failed to process outgoing message", e);
        }
    }

    /**
     * Process batch messages
     */
    public void processBatchMessages(List<WebSocketMessage> messages, String sessionId) {
        messages.forEach(message -> processMessage(message, sessionId));
    }

    /**
     * Register message processor
     */
    public void registerProcessor(MessageProcessor processor) {
        processors.add(processor);
        log.info("Registered message processor: {}", processor.getClass().getSimpleName());
    }

    /**
     * Remove message processor
     */
    public void removeProcessor(MessageProcessor processor) {
        processors.remove(processor);
        log.info("Removed message processor: {}", processor.getClass().getSimpleName());
    }

    // Private helper methods

    private WebSocketMessage applyProcessors(WebSocketMessage message) {
        WebSocketMessage processedMessage = message;
        for (MessageProcessor processor : processors) {
            processedMessage = processor.process(processedMessage);
        }
        return processedMessage;
    }

    private void handleValidationError(WebSocketMessage message, String sessionId, 
            WebSocketMessageValidator.ValidationResult validationResult) {
        log.error("Message validation failed for session {}: {}", 
            sessionId, validationResult.getErrorMessage());

        WebSocketMessage errorMessage = messageFactory.createValidationErrorMessage(
            "Message validation failed",
            validationResult.getErrors()
        );

        webSocketService.sendMessageToUser(sessionId, "/queue/errors", errorMessage);
    }

    private void handleProcessingError(WebSocketMessage message, String sessionId, Throwable throwable) {
        log.error("Error processing message from session {}: {}", sessionId, throwable.getMessage());

        WebSocketMessage errorMessage = messageFactory.createErrorMessage(
            "Message processing failed",
            throwable.getMessage()
        );

        webSocketService.sendMessageToUser(sessionId, "/queue/errors", errorMessage);
    }

    /**
     * Message processor interface
     */
    public interface MessageProcessor {
        WebSocketMessage process(WebSocketMessage message);
    }

    /**
     * Timestamp processor
     */
    @Component
    public static class TimestampProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            message.setTimestamp(java.time.LocalDateTime.now());
            return message;
        }
    }

    /**
     * Metadata processor
     */
    @Component
    public static class MetadataProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            Map<String, Object> metadata = message.getMetadata();
            if (metadata == null) {
                metadata = new java.util.HashMap<>();
            }
            metadata.put("processedAt", System.currentTimeMillis());
            message.setMetadata(metadata);
            return message;
        }
    }

    /**
     * Payload enrichment processor
     */
    @Component
    public static class PayloadEnrichmentProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            Map<String, Object> payload = message.getPayload();
            if (payload == null) {
                payload = new java.util.HashMap<>();
            }
            payload.put("processedTimestamp", System.currentTimeMillis());
            message.setPayload(payload);
            return message;
        }
    }

    /**
     * Message size processor
     */
    @Component
    public static class MessageSizeProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            Map<String, Object> metadata = message.getMetadata();
            if (metadata == null) {
                metadata = new java.util.HashMap<>();
            }
            // Estimate message size
            int size = message.toString().length();
            metadata.put("messageSize", size);
            message.setMetadata(metadata);
            return message;
        }
    }

    /**
     * Message compression processor
     */
    @Component
    public static class CompressionProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            // Implement compression logic if needed
            return message;
        }
    }

    /**
     * Message encryption processor
     */
    @Component
    public static class EncryptionProcessor implements MessageProcessor {
        @Override
        public WebSocketMessage process(WebSocketMessage message) {
            // Implement encryption logic if needed
            return message;
        }
    }

    /**
     * Custom exception for message processing errors
     */
    public static class MessageProcessingException extends RuntimeException {
        public MessageProcessingException(String message) {
            super(message);
        }

        public MessageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}