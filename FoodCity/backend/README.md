# FoodCity Backend WebSocket Implementation

## Overview
This implementation provides a robust WebSocket infrastructure for real-time communication in the FoodCity application. It includes secure connections, message validation, session management, error handling, and a scalable message processing pipeline.

## Architecture Components

### Core WebSocket Components
- `WebSocketConfig`: Base WebSocket configuration
- `WebSocketController`: Handles WebSocket endpoints
- `WebSocketService`: Core WebSocket service
- `WebSocketMessage`: Message payload structure

### Security Components
- `WebSocketSecurityConfig`: Security configuration
- `WebSocketAuthenticationConfig`: Authentication setup
- `WebSocketHandshakeInterceptor`: Connection handshake security
- `WebSocketChannelInterceptor`: Message channel security

### Message Handling
- `WebSocketMessageHandler`: Message handling logic
- `WebSocketMessageFactory`: Message creation
- `WebSocketMessageConverter`: Message conversion
- `WebSocketMessageProcessor`: Message processing pipeline
- `WebSocketMessageValidator`: Message validation

### Session Management
- `WebSocketSessionManager`: Session tracking and management
- `WebSocketEventListener`: WebSocket lifecycle events

### Error Handling
- `WebSocketErrorHandler`: Error handling and recovery
- `WebSocketConfiguration`: Central configuration

## Usage

### Client-Side Connection
```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Connect with authentication
stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, frame => {
    console.log('Connected:', frame);
}, error => {
    console.error('Error:', error);
});
```

### Subscribe to Topics
```javascript
// Analytics updates
stompClient.subscribe('/topic/analytics', message => {
    console.log('Analytics update:', JSON.parse(message.body));
});

// User-specific notifications
stompClient.subscribe('/user/queue/notifications', message => {
    console.log('Notification:', JSON.parse(message.body));
});

// System alerts
stompClient.subscribe('/topic/alerts', message => {
    console.log('Alert:', JSON.parse(message.body));
});
```

### Send Messages
```javascript
// Send analytics update
stompClient.send("/app/analytics", {}, JSON.stringify({
    type: "ANALYTICS_UPDATE",
    payload: {
        metric: "sales",
        value: 1500
    }
}));

// Send user action
stompClient.send("/app/user/action", {}, JSON.stringify({
    type: "USER_ACTION",
    payload: {
        action: "PURCHASE",
        productId: "123"
    }
}));
```

## Message Types

### Analytics Messages
- ANALYTICS_UPDATE
- METRICS_UPDATE
- DASHBOARD_UPDATE
- TREND_UPDATE
- FORECAST_UPDATE

### Alert Messages
- ALERT
- NOTIFICATION
- WARNING
- ERROR

### Status Messages
- STATUS_UPDATE
- PROGRESS_UPDATE
- COMPLETION

### Data Messages
- DATA_UPDATE
- DATA_SYNC
- DATA_VALIDATION

### User Messages
- USER_ACTION
- USER_PREFERENCE
- USER_STATUS

### System Messages
- SYSTEM_STATUS
- SYSTEM_MAINTENANCE
- SYSTEM_ERROR

## Security

### Authentication
All WebSocket connections require JWT authentication. The token should be included in the connection headers:
```javascript
const headers = {
    'Authorization': 'Bearer ' + jwtToken
};
stompClient.connect(headers, onConnect, onError);
```

### Authorization
Different message destinations have different authorization requirements:
- `/topic/public/**`: Public access
- `/topic/analytics/**`: ADMIN, ANALYST roles
- `/topic/dashboard/**`: ADMIN, MANAGER roles
- `/user/**`: Authenticated users
- `/topic/admin/**`: ADMIN role only

## Error Handling
The system includes comprehensive error handling:
- Connection errors
- Message validation errors
- Processing errors
- Security errors

Errors are sent to clients through dedicated error channels:
- `/user/queue/errors`: User-specific errors
- `/topic/errors`: System-wide errors

## Message Processing Pipeline
Messages go through a processing pipeline that includes:
1. Validation
2. Authentication/Authorization
3. Conversion
4. Processing
5. Delivery

## Session Management
The system maintains session information including:
- Active connections
- User details
- Subscriptions
- Activity timestamps

## Monitoring
Monitor WebSocket connections and messages through:
- `/topic/admin/connections`: Connection events
- `/topic/admin/metrics`: System metrics
- `/topic/system/status`: System status updates

## Best Practices
1. Always handle connection errors
2. Implement reconnection logic
3. Validate messages before sending
4. Handle all received messages
5. Clean up subscriptions when done
6. Implement proper error handling
7. Use appropriate message types
8. Follow security guidelines

## Dependencies
- Spring WebSocket
- STOMP WebSocket
- SockJS Client
- JWT Authentication
- Jackson JSON

## Configuration
Key configuration settings in `application.properties`:
```properties
# WebSocket
websocket.allowed-origins=*
websocket.endpoint=/ws
websocket.message-size-limit=128KB
websocket.send-buffer-limit=512KB
websocket.send-time-limit=20000
websocket.session-timeout=30m

# Security
websocket.security.allowed-origins=*
websocket.security.allowed-methods=*