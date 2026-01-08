package com.dental.clinic.management.dashboard.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard WebSocket Controller
 * Handles client WebSocket connections and messages
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardWebSocketController {

    private final DashboardWebSocketService webSocketService;

    /**
     * Handle client subscription to dashboard updates
     * Sends initial connection confirmation
     */
    @SubscribeMapping("/dashboard/updates")
    public Map<String, Object> handleSubscription() {
        log.info("New client subscribed to dashboard updates");
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "CONNECTION_ESTABLISHED");
        response.put("message", "Connected to dashboard real-time updates");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * Handle client request to refresh specific dashboard data
     * Client can send message to /app/dashboard/refresh with dataType
     */
    @MessageMapping("/dashboard/refresh")
    @SendTo("/topic/dashboard/updates")
    public Map<String, Object> handleRefreshRequest(Map<String, String> request) {
        String dataType = request.get("dataType");
        log.info("Refresh request received for: {}", dataType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "REFRESH_ACKNOWLEDGED");
        response.put("dataType", dataType);
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Broadcast update to all clients
        webSocketService.broadcastDataUpdate(dataType);
        
        return response;
    }

    /**
     * Handle ping from client to keep connection alive
     */
    @MessageMapping("/dashboard/ping")
    @SendTo("/topic/dashboard/pong")
    public Map<String, Object> handlePing() {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PONG");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
