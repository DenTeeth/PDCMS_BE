package com.dental.clinic.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Real-time Dashboard Updates
 * Enables STOMP messaging over WebSocket for live statistics
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Prefix for messages FROM server TO client
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws/dashboard")
                .setAllowedOriginPatterns("*") // Configure based on your CORS policy
                .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSocket
    }
}
