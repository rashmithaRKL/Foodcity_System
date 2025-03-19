package com.foodcity.backend.controller;

import com.foodcity.backend.payload.WebSocketMessage;
import com.foodcity.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;

    @MessageMapping("/message")
    @SendToUser("/queue/reply")
    public WebSocketMessage processMessage(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Received message from {}: {}", principal.getName(), message);
        
        // Process the message and return a response
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.STATUS_UPDATE)
                .destination("/queue/reply")
                .payload(Map.of("status", "Message received"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @SubscribeMapping("/analytics/dashboard")
    public WebSocketMessage subscribeToDashboard(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("New dashboard subscription from session: {}", sessionId);
        
        // Return initial dashboard data
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.DASHBOARD_UPDATE)
                .destination("/analytics/dashboard")
                .payload(Map.of("message", "Dashboard subscription successful"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @SubscribeMapping("/analytics/realtime")
    public WebSocketMessage subscribeToRealtime(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("New realtime analytics subscription from session: {}", sessionId);
        
        // Return initial realtime data
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ANALYTICS_UPDATE)
                .destination("/analytics/realtime")
                .payload(Map.of("message", "Realtime subscription successful"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @SubscribeMapping("/alerts")
    public WebSocketMessage subscribeToAlerts(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("New alerts subscription from session: {}", sessionId);
        
        // Return initial alerts data
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ALERT)
                .destination("/alerts")
                .payload(Map.of("message", "Alerts subscription successful"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @MessageMapping("/user/action")
    @SendToUser("/queue/response")
    public WebSocketMessage handleUserAction(@Payload WebSocketMessage message, Principal principal) {
        log.debug("User action from {}: {}", principal.getName(), message);
        
        // Process user action and return response
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.USER_ACTION)
                .destination("/queue/response")
                .payload(Map.of("status", "Action processed"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @MessageMapping("/analytics/request")
    @SendToUser("/queue/analytics")
    public WebSocketMessage handleAnalyticsRequest(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Analytics request from {}: {}", principal.getName(), message);
        
        // Process analytics request and return data
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ANALYTICS_UPDATE)
                .destination("/queue/analytics")
                .payload(Map.of("data", "Analytics data here"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }

    @MessageMapping("/system/status")
    public void updateSystemStatus(@Payload WebSocketMessage message, Principal principal) {
        log.debug("System status update from {}: {}", principal.getName(), message);
        
        // Broadcast system status to all clients
        webSocketService.sendSystemStatus(
            "System Status Updated",
            Map.of("details", message.getPayload())
        );
    }

    @MessageMapping("/notification")
    public void sendNotification(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Notification request from {}: {}", principal.getName(), message);
        
        // Send notification to specific user or broadcast
        if (message.getPayload().containsKey("username")) {
            String username = (String) message.getPayload().get("username");
            webSocketService.sendUserNotification(
                username,
                (String) message.getPayload().get("title"),
                (String) message.getPayload().get("message")
            );
        } else {
            webSocketService.sendSystemNotification(
                (String) message.getPayload().get("title"),
                (String) message.getPayload().get("message")
            );
        }
    }

    @MessageMapping("/alert")
    public void sendAlert(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Alert request from {}: {}", principal.getName(), message);
        
        // Send alert based on priority
        webSocketService.sendAlert(
            (String) message.getPayload().get("type"),
            (String) message.getPayload().get("message"),
            WebSocketMessage.Priority.valueOf((String) message.getPayload().get("priority"))
        );
    }

    @MessageMapping("/dashboard/update")
    public void updateDashboard(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Dashboard update from {}: {}", principal.getName(), message);
        
        // Broadcast dashboard update
        webSocketService.sendDashboardUpdate((Map<String, Object>) message.getPayload());
    }

    @MessageMapping("/analytics/sync")
    public void syncAnalytics(@Payload WebSocketMessage message, Principal principal) {
        log.debug("Analytics sync request from {}: {}", principal.getName(), message);
        
        // Sync analytics data
        webSocketService.sendDataSync(
            "/analytics/sync",
            (Map<String, Object>) message.getPayload()
        );
    }

    @MessageMapping("/error/report")
    @SendToUser("/queue/error")
    public WebSocketMessage handleErrorReport(@Payload WebSocketMessage message, Principal principal) {
        log.error("Error report from {}: {}", principal.getName(), message);
        
        // Process error report and return acknowledgment
        return WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .destination("/queue/error")
                .payload(Map.of("status", "Error report received"))
                .status(WebSocketMessage.MessageStatus.SUCCESS)
                .build();
    }
}