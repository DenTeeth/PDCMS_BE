package com.dental.clinic.management.authentication.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

/**
 * Rule #3: Concurrent Login Prevention Service
 * 
 * Business Logic:
 * - Each user account can only have ONE active session at a time
 * - When user logs in from device A, then logs in from device B:
 *   → Device A's token is automatically invalidated
 * - Old token is added to blacklist
 * - New token becomes the active session
 * 
 * Implementation:
 * - Uses in-memory ConcurrentHashMap to track active tokens per username
 * - For production, consider using Redis for distributed systems
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtDecoder jwtDecoder;

    /**
     * Map: username → ActiveSession
     * Only stores the LATEST active session per user
     */
    private final ConcurrentHashMap<String, ActiveSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Register new login session
     * If user already has an active session, invalidate the old one
     * 
     * @param username User's username
     * @param newToken New JWT access token from login
     */
    public void registerNewSession(String username, String newToken) {
        try {
            Jwt jwt = jwtDecoder.decode(newToken);
            Instant expiresAt = jwt.getExpiresAt();
            
            // Check if user has existing active session
            ActiveSession oldSession = activeSessions.get(username);
            if (oldSession != null) {
                // Invalidate old session
                log.warn("Rule #3: User {} already has active session. Invalidating old token (device fingerprint: {})",
                        username, oldSession.getDeviceFingerprint());
                tokenBlacklistService.blacklistToken(oldSession.getToken());
            }

            // Extract device fingerprint from JWT claims (if available)
            String deviceFingerprint = jwt.getClaim("device_fingerprint");
            if (deviceFingerprint == null) {
                deviceFingerprint = "unknown";
            }

            // Register new session
            ActiveSession newSession = new ActiveSession(newToken, deviceFingerprint, expiresAt);
            activeSessions.put(username, newSession);
            
            log.info("✓ Rule #3: New session registered for user {} (device: {})", username, deviceFingerprint);

        } catch (Exception e) {
            log.error("Failed to register session for user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Manually invalidate user's active session (called on logout)
     * 
     * @param username User's username
     */
    public void invalidateSession(String username) {
        ActiveSession session = activeSessions.remove(username);
        if (session != null) {
            tokenBlacklistService.blacklistToken(session.getToken());
            log.info("✓ Session invalidated for user {} (manual logout)", username);
        }
    }

    /**
     * Check if token is the active session for the user
     * 
     * @param username User's username
     * @param token JWT access token
     * @return true if this is the active session, false otherwise
     */
    public boolean isActiveSession(String username, String token) {
        ActiveSession session = activeSessions.get(username);
        return session != null && session.getToken().equals(token);
    }

    /**
     * Cleanup expired sessions (called periodically)
     */
    public void cleanupExpiredSessions() {
        long currentTime = Instant.now().getEpochSecond();
        activeSessions.entrySet().removeIf(entry -> {
            Instant expiresAt = entry.getValue().getExpiresAt();
            if (expiresAt != null && expiresAt.getEpochSecond() < currentTime) {
                log.debug("Removing expired session for user: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Get active session count (for monitoring)
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Inner class to represent an active session
     */
    private static class ActiveSession {
        private final String token;
        private final String deviceFingerprint;
        private final Instant expiresAt;

        public ActiveSession(String token, String deviceFingerprint, Instant expiresAt) {
            this.token = token;
            this.deviceFingerprint = deviceFingerprint;
            this.expiresAt = expiresAt;
        }

        public String getToken() {
            return token;
        }

        public String getDeviceFingerprint() {
            return deviceFingerprint;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
