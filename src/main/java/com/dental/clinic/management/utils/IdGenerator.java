package com.dental.clinic.management.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating custom IDs in the format: PREFIX-YYMMDD-SEQ
 * Example: CTC-251016-001, CTH-251016-002
 * 
 * Thread-safe implementation with daily counter reset.
 */
@Component
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    
    // Map to store counters for each prefix and date combination
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    
    /**
     * Generate a new ID with the given prefix
     * 
     * @param prefix The prefix for the ID (e.g., "CTC", "CTH")
     * @return Generated ID in format PREFIX-YYMMDD-SEQ (e.g., CTC-251016-001)
     */
    public synchronized String generateId(String prefix) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);
        String key = prefix + "-" + dateStr;
        
        // Get or create counter for this prefix-date combination
        AtomicInteger counter = counters.computeIfAbsent(key, k -> new AtomicInteger(0));
        
        // Increment and get the next sequence number
        int sequence = counter.incrementAndGet();
        
        // Format: PREFIX-YYMMDD-SEQ (e.g., CTC-251016-001)
        return String.format("%s-%s-%03d", prefix, dateStr, sequence);
    }
    
    /**
     * Clear counters for dates before today (cleanup old entries)
     * This method can be called periodically to prevent memory growth
     */
    public void cleanupOldCounters() {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        
        counters.keySet().removeIf(key -> !key.contains(todayStr));
    }
}
