package com.dental.clinic.management.utils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for preventing abuse.
 * Limits the number of requests per IP address within a time window.
 */
@Component
public class RateLimiter {

    // Map to store request counts per IP: IP -> (timestamp, count)
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for the given identifier (e.g., IP address).
     *
     * @param identifier    unique identifier (e.g., IP address, email)
     * @param maxRequests   maximum number of requests allowed
     * @param windowMinutes time window in minutes
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String identifier, int maxRequests, int windowMinutes) {
        Instant now = Instant.now();
        
        // Clean up old entries periodically
        cleanupOldEntries(windowMinutes);
        
        RateLimitInfo info = requestCounts.computeIfAbsent(identifier, k -> new RateLimitInfo());
        
        synchronized (info) {
            // Check if time window has passed
            Duration duration = Duration.between(info.windowStart, now);
            if (duration.toMinutes() >= windowMinutes) {
                // Reset the window
                info.windowStart = now;
                info.count = 1;
                return true;
            }
            
            // Check if limit exceeded
            if (info.count >= maxRequests) {
                return false;
            }
            
            // Increment count
            info.count++;
            return true;
        }
    }

    /**
     * Get remaining time until rate limit resets for the given identifier.
     *
     * @param identifier    unique identifier (e.g., IP address, email)
     * @param windowMinutes time window in minutes
     * @return seconds until reset, or 0 if not rate limited
     */
    public long getSecondsUntilReset(String identifier, int windowMinutes) {
        RateLimitInfo info = requestCounts.get(identifier);
        if (info == null) {
            return 0;
        }
        
        Instant now = Instant.now();
        Duration duration = Duration.between(info.windowStart, now);
        long remainingMinutes = windowMinutes - duration.toMinutes();
        
        if (remainingMinutes <= 0) {
            return 0;
        }
        
        return remainingMinutes * 60 - duration.toSecondsPart();
    }

    /**
     * Clean up old entries to prevent memory leaks.
     */
    private void cleanupOldEntries(int windowMinutes) {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(windowMinutes * 2));
        requestCounts.entrySet().removeIf(entry -> 
            entry.getValue().windowStart.isBefore(cutoff)
        );
    }

    /**
     * Reset rate limit for a specific identifier.
     * Useful for testing or manual reset.
     */
    public void reset(String identifier) {
        requestCounts.remove(identifier);
    }

    /**
     * Inner class to store rate limit information.
     */
    private static class RateLimitInfo {
        Instant windowStart = Instant.now();
        int count = 0;
    }
}
