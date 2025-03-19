# FoodCity System

## Overview
FoodCity is a comprehensive food ordering and management system with real-time features powered by WebSocket technology. The system provides real-time updates for orders, inventory, analytics, and customer interactions.

## Project Structure

### Backend Components
- **WebSocket Infrastructure**
  - Real-time communication
  - Secure connections
  - Message validation
  - Session management
  - Error handling

- **Core Services**
  - Order Management
  - Inventory Control
  - Customer Management
  - Payment Processing
  - Analytics & Reporting
  - Employee Management

### Technology Stack
- **Backend**
  - Spring Boot
  - Spring WebSocket
  - Spring Security
  - MongoDB
  - JWT Authentication

## Features

### Real-time Capabilities
1. **Order Updates**
   - Live order tracking
   - Real-time status updates
   - Instant notifications

2. **Inventory Management**
   - Real-time stock updates
   - Low stock alerts
   - Automatic reorder notifications

3. **Analytics Dashboard**
   - Live sales metrics
   - Real-time revenue tracking
   - Customer behavior analytics

4. **Customer Engagement**
   - Instant messaging
   - Live support
   - Real-time feedback

### Security Features
- JWT Authentication
- WebSocket security
- Role-based access control
- Session management
- Request validation

## Getting Started

### Prerequisites
- Java 11 or higher
- MongoDB
- Maven

### Installation

1. Clone the repository
```bash
git clone https://github.com/rashmithaRKL/Foodcity_System.git
```

2. Navigate to backend directory
```bash
cd FoodCity/backend
```

3. Install dependencies
```bash
mvn install
```

4. Configure application.properties
```properties
# Database Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/foodcity

# JWT Configuration
jwt.secret=yourSecretKey
jwt.expiration=86400000

# WebSocket Configuration
websocket.allowed-origins=*
```

5. Run the application
```bash
mvn spring-boot:run
```

## WebSocket Endpoints

### Public Topics
- `/topic/public/**` - Public messages
- `/topic/notifications` - System notifications

### Authenticated Endpoints
- `/topic/orders/**` - Order updates
- `/topic/inventory/**` - Inventory updates
- `/topic/analytics/**` - Analytics updates

### User-Specific Endpoints
- `/user/queue/notifications` - User notifications
- `/user/queue/orders` - User's order updates

## API Documentation

### Authentication
```javascript
// Connect with authentication
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, frame => {
    console.log('Connected:', frame);
});
```

### Subscribe to Updates
```javascript
// Subscribe to order updates
stompClient.subscribe('/topic/orders', message => {
    console.log('Order update:', JSON.parse(message.body));
});

// Subscribe to notifications
stompClient.subscribe('/user/queue/notifications', message => {
    console.log('Notification:', JSON.parse(message.body));
});
```

### Send Messages
```javascript
// Send order update
stompClient.send("/app/orders", {}, JSON.stringify({
    type: "ORDER_UPDATE",
    orderId: "123",
    status: "PREPARING"
}));
```

## Error Handling
- Connection errors
- Message validation errors
- Authentication errors
- Session timeouts

## Monitoring
- Active connections
- Message statistics
- System health
- Performance metrics

## Security Considerations
1. Always use HTTPS in production
2. Implement rate limiting
3. Validate all messages
4. Monitor for suspicious activity
5. Regular security audits

## Best Practices
1. Handle connection errors gracefully
2. Implement reconnection logic
3. Validate messages before sending
4. Clean up subscriptions
5. Follow security guidelines

## Contributing
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Support
For support, email support@foodcity.com or create an issue in the repository.

## Authors
- Rashmitha RKL

## Acknowledgments
- Spring Framework team
- MongoDB team
- All contributors