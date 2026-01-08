package com.dental.clinic.management.dashboard.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard WebSocket Service
 * Broadcasts real-time updates to connected dashboard clients
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast dashboard data update notification
     * Clients should refetch data when they receive this
     */
    public void broadcastDataUpdate(String dataType) {
        log.info("Broadcasting dashboard update for: {}", dataType);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DATA_UPDATE");
        message.put("dataType", dataType); // e.g., "REVENUE", "APPOINTMENTS", "OVERVIEW"
        message.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/dashboard/updates", message);
    }

    /**
     * Broadcast alert notification
     */
    public void broadcastAlert(String severity, String message, String alertType) {
        log.info("Broadcasting dashboard alert: {} - {}", severity, message);
        
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "ALERT");
        alert.put("severity", severity); // WARNING, ERROR, INFO
        alert.put("message", message);
        alert.put("alertType", alertType); // REVENUE_DROP, HIGH_DEBT, etc.
        alert.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/dashboard/alerts", alert);
    }

    /**
     * Broadcast specific metric change
     */
    public void broadcastMetricChange(String metricName, Object oldValue, Object newValue) {
        log.info("Broadcasting metric change: {} from {} to {}", metricName, oldValue, newValue);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "METRIC_CHANGE");
        message.put("metricName", metricName);
        message.put("oldValue", oldValue);
        message.put("newValue", newValue);
        message.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/dashboard/metrics", message);
    }

    /**
     * Send personalized update to specific user
     */
    public void sendUserUpdate(Integer userId, String updateType, Map<String, Object> data) {
        log.info("Sending personalized update to user {}: {}", userId, updateType);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", updateType);
        message.put("data", data);
        message.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSendToUser(
            userId.toString(), 
            "/queue/dashboard/updates", 
            message
        );
    }
}
