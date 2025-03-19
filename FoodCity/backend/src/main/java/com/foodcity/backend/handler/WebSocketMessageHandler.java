package com.foodcity.backend.handler;

import com.foodcity.backend.converter.WebSocketMessageConverter;
import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.service.WebSocketService;
import com.foodcity.backend.service.WebSocketSessionManager;
import com.foodcity.backend.validator.WebSocketMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageHandler {

    private final WebSocketService webSocketService;
    private final WebSocketSessionManager sessionManager;
    private final WebSocketMessageConverter messageConverter;
    private final WebSocketMessageValidator messageValidator;

    // Message handlers registry
    private final Map<String, BiConsumer<WebSocketMessage, String>> messageHandlers = new ConcurrentHashMap<>();

    // Initialize message handlers
    {
        // Analytics handlers
        messageHandlers.put(WebSocketMessage.Type.ANALYTICS_UPDATE, this::handleAnalyticsUpdate);
        messageHandlers.put(WebSocketMessage.Type.METRICS_UPDATE, this::handleMetricsUpdate);
        messageHandlers.put(WebSocketMessage.Type.DASHBOARD_UPDATE, this::handleDashboardUpdate);
        messageHandlers.put(WebSocketMessage.Type.TREND_UPDATE, this::handleTrendUpdate);
        messageHandlers.put(WebSocketMessage.Type.FORECAST_UPDATE, this::handleForecastUpdate);

        // Alert handlers
        messageHandlers.put(WebSocketMessage.Type.ALERT, this::handleAlert);
        messageHandlers.put(WebSocketMessage.Type.NOTIFICATION, this::handleNotification);
        messageHandlers.put(WebSocketMessage.Type.WARNING, this::handleWarning);
        messageHandlers.put(WebSocketMessage.Type.ERROR, this::handleError);

        // Status handlers
        messageHandlers.put(WebSocketMessage.Type.STATUS_UPDATE, this::handleStatusUpdate);
        messageHandlers.put(WebSocketMessage.Type.PROGRESS_UPDATE, this::handleProgressUpdate);
        messageHandlers.put(WebSocketMessage.Type.COMPLETION, this::handleCompletion);

        // Data handlers
        messageHandlers.put(WebSocketMessage.Type.DATA_UPDATE, this::handleDataUpdate);
        messageHandlers.put(WebSocketMessage.Type.DATA_SYNC, this::handleDataSync);
        messageHandlers.put(WebSocketMessage.Type.DATA_VALIDATION, this::handleDataValidation);

        // User handlers
        messageHandlers.put(WebSocketMessage.Type.USER_ACTION, this::handleUserAction);
        messageHandlers.put(WebSocketMessage.Type.USER_PREFERENCE, this::handleUserPreference);
        messageHandlers.put(WebSocketMessage.Type.USER_STATUS, this::handleUserStatus);

        // System handlers
        messageHandlers.put(WebSocketMessage.Type.SYSTEM_STATUS, this::handleSystemStatus);
        messageHandlers.put(WebSocketMessage.Type.SYSTEM_MAINTENANCE, this::handleSystemMaintenance);
        messageHandlers.put(WebSocketMessage.Type.SYSTEM_ERROR, this::handleSystemError);
    }

    /**
     * Handle incoming WebSocket message
     */
    public void handleMessage(WebSocketMessage message, String sessionId) {
        try {
            // Validate message
            WebSocketMessageValidator.ValidationResult validationResult = 
                messageValidator.validateMessage(message);
            
            if (validationResult.hasErrors()) {
                handleValidationError(message, sessionId, validationResult);
                return;
            }

            // Update session activity
            sessionManager.updateSessionActivity(sessionId);

            // Get appropriate handler
            BiConsumer<WebSocketMessage, String> handler = messageHandlers.get(message.getType());
            if (handler != null) {
                handler.accept(message, sessionId);
            } else {
                handleUnknownMessageType(message, sessionId);
            }

        } catch (Exception e) {
            handleProcessingError(message, sessionId, e);
        }
    }

    // Analytics message handlers

    private void handleAnalyticsUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing analytics update from session {}", sessionId);
        webSocketService.sendMessage("/topic/analytics/updates", message);
    }

    private void handleMetricsUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing metrics update from session {}", sessionId);
        webSocketService.sendMessage("/topic/analytics/metrics", message);
    }

    private void handleDashboardUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing dashboard update from session {}", sessionId);
        webSocketService.sendMessage("/topic/dashboard/updates", message);
    }

    private void handleTrendUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing trend update from session {}", sessionId);
        webSocketService.sendMessage("/topic/analytics/trends", message);
    }

    private void handleForecastUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing forecast update from session {}", sessionId);
        webSocketService.sendMessage("/topic/analytics/forecasts", message);
    }

    // Alert message handlers

    private void handleAlert(WebSocketMessage message, String sessionId) {
        log.debug("Processing alert from session {}", sessionId);
        webSocketService.sendMessage("/topic/alerts", message);
    }

    private void handleNotification(WebSocketMessage message, String sessionId) {
        log.debug("Processing notification from session {}", sessionId);
        webSocketService.sendMessage("/topic/notifications", message);
    }

    private void handleWarning(WebSocketMessage message, String sessionId) {
        log.debug("Processing warning from session {}", sessionId);
        webSocketService.sendMessage("/topic/warnings", message);
    }

    private void handleError(WebSocketMessage message, String sessionId) {
        log.debug("Processing error from session {}", sessionId);
        webSocketService.sendMessage("/topic/errors", message);
    }

    // Status message handlers

    private void handleStatusUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing status update from session {}", sessionId);
        webSocketService.sendMessage("/topic/status", message);
    }

    private void handleProgressUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing progress update from session {}", sessionId);
        webSocketService.sendMessage("/topic/progress", message);
    }

    private void handleCompletion(WebSocketMessage message, String sessionId) {
        log.debug("Processing completion from session {}", sessionId);
        webSocketService.sendMessage("/topic/completion", message);
    }

    // Data message handlers

    private void handleDataUpdate(WebSocketMessage message, String sessionId) {
        log.debug("Processing data update from session {}", sessionId);
        webSocketService.sendMessage("/topic/data/updates", message);
    }

    private void handleDataSync(WebSocketMessage message, String sessionId) {
        log.debug("Processing data sync from session {}", sessionId);
        webSocketService.sendMessage("/topic/data/sync", message);
    }

    private void handleDataValidation(WebSocketMessage message, String sessionId) {
        log.debug("Processing data validation from session {}", sessionId);
        webSocketService.sendMessage("/topic/data/validation", message);
    }

    // User message handlers

    private void handleUserAction(WebSocketMessage message, String sessionId) {
        log.debug("Processing user action from session {}", sessionId);
        webSocketService.sendMessage("/topic/user/actions", message);
    }

    private void handleUserPreference(WebSocketMessage message, String sessionId) {
        log.debug("Processing user preference from session {}", sessionId);
        webSocketService.sendMessage("/topic/user/preferences", message);
    }

    private void handleUserStatus(WebSocketMessage message, String sessionId) {
        log.debug("Processing user status from session {}", sessionId);
        webSocketService.sendMessage("/topic/user/status", message);
    }

    // System message handlers

    private void handleSystemStatus(WebSocketMessage message, String sessionId) {
        log.debug("Processing system status from session {}", sessionId);
        webSocketService.sendMessage("/topic/system/status", message);
    }

    private void handleSystemMaintenance(WebSocketMessage message, String sessionId) {
        log.debug("Processing system maintenance from session {}", sessionId);
        webSocketService.sendMessage("/topic/system/maintenance", message);
    }

    private void handleSystemError(WebSocketMessage message, String sessionId) {
        log.debug("Processing system error from session {}", sessionId);
        webSocketService.sendMessage("/topic/system/errors", message);
    }

    // Error handlers

    private void handleValidationError(WebSocketMessage message, String sessionId, 
            WebSocketMessageValidator.ValidationResult validationResult) {
        log.error("Message validation failed for session {}: {}", 
            sessionId, validationResult.getErrorMessage());

        WebSocketMessage errorMessage = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.ERROR)
            .destination("/user/queue/errors")
            .payload(Map.of(
                "error", "Message validation failed",
                "details", validationResult.getErrors()
            ))
            .status(WebSocketMessage.MessageStatus.ERROR)
            .build();

        webSocketService.sendMessageToUser(sessionId, "/queue/errors", errorMessage);
    }

    private void handleUnknownMessageType(WebSocketMessage message, String sessionId) {
        log.warn("Unknown message type received from session {}: {}", sessionId, message.getType());

        WebSocketMessage errorMessage = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.ERROR)
            .destination("/user/queue/errors")
            .payload(Map.of(
                "error", "Unknown message type",
                "type", message.getType()
            ))
            .status(WebSocketMessage.MessageStatus.ERROR)
            .build();

        webSocketService.sendMessageToUser(sessionId, "/queue/errors", errorMessage);
    }

    private void handleProcessingError(WebSocketMessage message, String sessionId, Exception e) {
        log.error("Error processing message from session {}: {}", sessionId, e.getMessage());

        WebSocketMessage errorMessage = WebSocketMessage.builder()
            .type(WebSocketMessage.Type.ERROR)
            .destination("/user/queue/errors")
            .payload(Map.of(
                "error", "Message processing failed",
                "details", e.getMessage()
            ))
            .status(WebSocketMessage.MessageStatus.ERROR)
            .build();

        webSocketService.sendMessageToUser(sessionId, "/queue/errors", errorMessage);
    }

    /**
     * Register custom message handler
     */
    public void registerMessageHandler(String messageType, 
            BiConsumer<WebSocketMessage, String> handler) {
        messageHandlers.put(messageType, handler);
    }

    /**
     * Remove message handler
     */
    public void removeMessageHandler(String messageType) {
        messageHandlers.remove(messageType);
    }

    /**
     * Get registered message handlers
     */
    public Map<String, BiConsumer<WebSocketMessage, String>> getMessageHandlers() {
        return new HashMap<>(messageHandlers);
    }
}