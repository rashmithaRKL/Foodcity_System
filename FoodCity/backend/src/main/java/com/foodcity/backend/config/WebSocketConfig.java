package com.foodcity.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to send messages to clients
        // Prefix for messages FROM server TO client
        config.enableSimpleBroker(
            "/topic",     // For general subscriptions
            "/queue",     // For user-specific subscriptions
            "/user"       // For user-specific notifications
        );
        
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Enable SockJS fallback options
                .setAllowedOrigins("*")
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(128 * 1024)    // Max message size 128KB
            .setSendBufferSizeLimit(512 * 1024) // Max buffer size 512KB
            .setSendTimeLimit(20000)            // 20 second send timeout
            .setTimeToFirstMessage(30000);      // 30 second initial connection timeout
    }

    // WebSocket channel mappings for different types of real-time updates
    public static class Destinations {
        // Analytics Updates
        public static final String ANALYTICS_UPDATES = "/topic/analytics/updates";
        public static final String DASHBOARD_METRICS = "/topic/analytics/dashboard";
        public static final String SALES_METRICS = "/topic/analytics/sales";
        public static final String REVENUE_METRICS = "/topic/analytics/revenue";
        public static final String CUSTOMER_METRICS = "/topic/analytics/customers";
        public static final String INVENTORY_METRICS = "/topic/analytics/inventory";
        public static final String EMPLOYEE_METRICS = "/topic/analytics/employees";
        
        // Real-time Alerts
        public static final String ALERTS = "/topic/alerts";
        public static final String LOW_STOCK_ALERTS = "/topic/alerts/low-stock";
        public static final String SALES_ALERTS = "/topic/alerts/sales";
        public static final String SYSTEM_ALERTS = "/topic/alerts/system";
        
        // User-specific Updates
        public static final String USER_NOTIFICATIONS = "/user/queue/notifications";
        public static final String USER_ALERTS = "/user/queue/alerts";
        public static final String USER_TASKS = "/user/queue/tasks";
        
        // Order Updates
        public static final String ORDER_UPDATES = "/topic/orders/updates";
        public static final String NEW_ORDERS = "/topic/orders/new";
        public static final String ORDER_STATUS = "/topic/orders/status";
        
        // Inventory Updates
        public static final String INVENTORY_UPDATES = "/topic/inventory/updates";
        public static final String STOCK_LEVELS = "/topic/inventory/stock-levels";
        public static final String PRICE_UPDATES = "/topic/inventory/prices";
        
        // Customer Updates
        public static final String CUSTOMER_UPDATES = "/topic/customers/updates";
        public static final String NEW_CUSTOMERS = "/topic/customers/new";
        public static final String CUSTOMER_ACTIVITY = "/topic/customers/activity";
        
        // Employee Updates
        public static final String EMPLOYEE_UPDATES = "/topic/employees/updates";
        public static final String EMPLOYEE_STATUS = "/topic/employees/status";
        public static final String EMPLOYEE_TASKS = "/topic/employees/tasks";
        
        // Payment Updates
        public static final String PAYMENT_UPDATES = "/topic/payments/updates";
        public static final String NEW_PAYMENTS = "/topic/payments/new";
        public static final String PAYMENT_STATUS = "/topic/payments/status";
        
        // Report Updates
        public static final String REPORT_UPDATES = "/topic/reports/updates";
        public static final String REPORT_GENERATION = "/topic/reports/generation";
        public static final String REPORT_COMPLETION = "/topic/reports/completion";
    }

    // Message types for different kinds of updates
    public static class MessageTypes {
        // Analytics Message Types
        public static final String METRIC_UPDATE = "METRIC_UPDATE";
        public static final String TREND_UPDATE = "TREND_UPDATE";
        public static final String FORECAST_UPDATE = "FORECAST_UPDATE";
        
        // Alert Message Types
        public static final String INFO_ALERT = "INFO_ALERT";
        public static final String WARNING_ALERT = "WARNING_ALERT";
        public static final String ERROR_ALERT = "ERROR_ALERT";
        public static final String CRITICAL_ALERT = "CRITICAL_ALERT";
        
        // Status Message Types
        public static final String STATUS_UPDATE = "STATUS_UPDATE";
        public static final String PROGRESS_UPDATE = "PROGRESS_UPDATE";
        public static final String COMPLETION_UPDATE = "COMPLETION_UPDATE";
        
        // Notification Message Types
        public static final String USER_NOTIFICATION = "USER_NOTIFICATION";
        public static final String SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";
        public static final String TASK_NOTIFICATION = "TASK_NOTIFICATION";
        
        // Data Message Types
        public static final String DATA_UPDATE = "DATA_UPDATE";
        public static final String DATA_SYNC = "DATA_SYNC";
        public static final String DATA_VALIDATION = "DATA_VALIDATION";
    }
}