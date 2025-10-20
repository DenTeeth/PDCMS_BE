package com.dental.clinic.management.working_schedule.utils;

import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.repository.OvertimeRequestRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for generating Overtime Request IDs.
 * Format: OTRyymmddSSS (e.g., OTR251021005)
 * - OTR: Overtime Request prefix
 * - yy: Year (2 digits)
 * - mm: Month (2 digits)
 * - dd: Day (2 digits)
 * - SSS: Sequential number (3 digits, 001-999)
 */
@Component
public class OvertimeRequestIdGenerator {

    private static final String PREFIX = "OTR";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int SEQUENCE_LENGTH = 3;
    private static final int MAX_SEQUENCE = 999;

    private final OvertimeRequestRepository overtimeRequestRepository;

    public OvertimeRequestIdGenerator(OvertimeRequestRepository overtimeRequestRepository) {
        this.overtimeRequestRepository = overtimeRequestRepository;
    }

    /**
     * Generate a new overtime request ID for today's date.
     * @return generated ID in format OTRyymmddSSS
     * @throws IllegalStateException if maximum sequence number (999) is reached
     */
    public String generateId() {
        return generateId(LocalDate.now());
    }

    /**
     * Generate a new overtime request ID for a specific date.
     * @param date the date for which to generate the ID
     * @return generated ID in format OTRyymmddSSS
     * @throws IllegalStateException if maximum sequence number (999) is reached
     */
    public String generateId(LocalDate date) {
        String datePrefix = PREFIX + date.format(DATE_FORMATTER);
        
        // Find the latest request with this date prefix
        Optional<OvertimeRequest> latestRequest = overtimeRequestRepository.findLatestByDatePrefix(datePrefix);
        
        int nextSequence = 1;
        if (latestRequest.isPresent()) {
            String latestId = latestRequest.get().getRequestId();
            // Extract sequence number from the last 3 characters
            String sequenceStr = latestId.substring(latestId.length() - SEQUENCE_LENGTH);
            int currentSequence = Integer.parseInt(sequenceStr);
            nextSequence = currentSequence + 1;
            
            if (nextSequence > MAX_SEQUENCE) {
                throw new IllegalStateException(
                    String.format("Maximum sequence number (%d) reached for date %s", 
                        MAX_SEQUENCE, date)
                );
            }
        }
        
        // Format: OTRyymmddSSS
        return String.format("%s%0" + SEQUENCE_LENGTH + "d", datePrefix, nextSequence);
    }

    /**
     * Validate if an ID follows the correct format.
     * @param requestId the ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFormat(String requestId) {
        if (requestId == null || requestId.length() != 12) {
            return false;
        }
        
        if (!requestId.startsWith(PREFIX)) {
            return false;
        }
        
        // Check if date part and sequence are numeric
        String datePart = requestId.substring(3, 9); // yymmdd
        String sequencePart = requestId.substring(9, 12); // SSS
        
        try {
            Integer.parseInt(datePart);
            int sequence = Integer.parseInt(sequencePart);
            return sequence >= 1 && sequence <= MAX_SEQUENCE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extract the date from an overtime request ID.
     * @param requestId the ID to parse
     * @return the date, or null if invalid format
     */
    public static LocalDate extractDate(String requestId) {
        if (!isValidFormat(requestId)) {
            return null;
        }
        
        try {
            String datePart = requestId.substring(3, 9); // yymmdd
            return LocalDate.parse(datePart, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract the sequence number from an overtime request ID.
     * @param requestId the ID to parse
     * @return the sequence number, or -1 if invalid format
     */
    public static int extractSequence(String requestId) {
        if (!isValidFormat(requestId)) {
            return -1;
        }
        
        try {
            String sequencePart = requestId.substring(9, 12); // SSS
            return Integer.parseInt(sequencePart);
        } catch (Exception e) {
            return -1;
        }
    }
}
