package com.foodcity.backend.config;

import com.foodcity.backend.converter.WebSocketMessageConverter;
import com.foodcity.backend.exception.WebSocketErrorHandler;
import com.foodcity.backend.handler.WebSocketMessageHandler;
import com.foodcity.backend.processor.WebSocketMessageProcessor;
import com.foodcity.backend.security.WebSocketChannelInterceptor;
import com.foodcity.backend.security.WebSocketHandshakeInterceptor;
import com.foodcity.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
@RequiredArgsConstructor
@Import({
    WebSocketSecurityConfig.class,
    WebSocketAuthenticationConfig.class
})
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final WebSocketService webSocketService;
    private final WebSocketMessageHandler messageHandler;
    private final WebSocketMessageProcessor messageProcessor;
    private final WebSocketErrorHandler errorHandler;
    private final WebSocketChannelInterceptor channelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        config.enableSimpleBroker(
            "/topic",    // For broadcast messages
            "/queue",    // For user-specific messages
            "/user"      // For user-specific notifications
        );
        
        // Set prefix for messages bound for message-handling methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Set prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
        
        // Configure broker settings
        config.configureBrokerChannel()
            .taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(8)
            .keepAliveSeconds(60);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            // Configure allowed origins
            .setAllowedOriginPatterns("*")
            // Add handshake interceptor
            .addInterceptors(handshakeInterceptor())
            // Enable SockJS fallback
            .withSockJS()
            .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
            .setWebSocketEnabled(true)
            .setSessionCookieNeeded(false);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            // Set message size limits
            .setMessageSizeLimit(128 * 1024)     // 128KB
            .setSendBufferSizeLimit(512 * 1024)  // 512KB
            .setSendTimeLimit(20000)             // 20 seconds
            .setTimeToFirstMessage(30000);       // 30 seconds
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor)
            .taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(8)
            .keepAliveSeconds(60);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(8)
            .keepAliveSeconds(60);
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(webSocketMessageConverter());
        return false;
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new WebSocketHandshakeInterceptor();
    }

    @Bean
    public WebSocketMessageConverter webSocketMessageConverter() {
        return new WebSocketMessageConverter();
    }

    @Bean
    public WebSocketMessageProcessor.TimestampProcessor timestampProcessor() {
        return new WebSocketMessageProcessor.TimestampProcessor();
    }

    @Bean
    public WebSocketMessageProcessor.MetadataProcessor metadataProcessor() {
        return new WebSocketMessageProcessor.MetadataProcessor();
    }

    @Bean
    public WebSocketMessageProcessor.PayloadEnrichmentProcessor payloadEnrichmentProcessor() {
        return new WebSocketMessageProcessor.PayloadEnrichmentProcessor();
    }

    @Bean
    public WebSocketMessageProcessor.MessageSizeProcessor messageSizeProcessor() {
        return new WebSocketMessageProcessor.MessageSizeProcessor();
    }

    @Bean
    public WebSocketMessageProcessor.CompressionProcessor compressionProcessor() {
        return new WebSocketMessageProcessor.CompressionProcessor();
    }

    @Bean
    public WebSocketMessageProcessor.EncryptionProcessor encryptionProcessor() {
        return new WebSocketMessageProcessor.EncryptionProcessor();
    }

    /**
     * Configure WebSocket session management
     */
    @Bean
    public WebSocketSessionManagementConfigurer sessionManagementConfigurer() {
        return new WebSocketSessionManagementConfigurer();
    }

    /**
     * Inner class for session management configuration
     */
    public static class WebSocketSessionManagementConfigurer {
        
        public void configure(WebSocketTransportRegistration registration) {
            registration
                .setSendTimeLimit(15 * 1000)        // 15 seconds
                .setSendBufferSizeLimit(512 * 1024) // 512KB
                .setMessageSizeLimit(128 * 1024)    // 128KB
                .setTimeToFirstMessage(30 * 1000);  // 30 seconds
        }
    }

    /**
     * Configure error handling
     */
    @Bean
    public WebSocketErrorHandlingConfigurer errorHandlingConfigurer() {
        return new WebSocketErrorHandlingConfigurer(errorHandler);
    }

    /**
     * Inner class for error handling configuration
     */
    @RequiredArgsConstructor
    public static class WebSocketErrorHandlingConfigurer {
        
        private final WebSocketErrorHandler errorHandler;
        
        public void configure(WebSocketTransportRegistration registration) {
            registration.setErrorHandler(errorHandler);
        }
    }

    /**
     * Configure message processing
     */
    @Bean
    public WebSocketMessageProcessingConfigurer messageProcessingConfigurer() {
        return new WebSocketMessageProcessingConfigurer(messageProcessor);
    }

    /**
     * Inner class for message processing configuration
     */
    @RequiredArgsConstructor
    public static class WebSocketMessageProcessingConfigurer {
        
        private final WebSocketMessageProcessor messageProcessor;
        
        public void configure() {
            // Register default processors
            messageProcessor.registerProcessor(new WebSocketMessageProcessor.TimestampProcessor());
            messageProcessor.registerProcessor(new WebSocketMessageProcessor.MetadataProcessor());
            messageProcessor.registerProcessor(new WebSocketMessageProcessor.PayloadEnrichmentProcessor());
            messageProcessor.registerProcessor(new WebSocketMessageProcessor.MessageSizeProcessor());
        }
    }
}