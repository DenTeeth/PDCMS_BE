package com.dental.clinic.management.authentication.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis-based JWT token blacklist service.
 *
 * Features:
 * - Blacklist JWT access tokens after logout
 * - Auto-expire tokens using Redis TTL (no manual cleanup needed)
 * - Distributed system support (multiple backend instances)
 * - Production-ready with Railway Redis support
 *
 * Redis Key Format:
 * - Key: "jwt-blacklist:{token}"
 * - Value: reason (LOGOUT, PASSWORD_CHANGED, etc.)
 * - TTL: Token's expiry time
 *
 * Migration Note:
 * - Previous version used ConcurrentHashMap (in-memory, single instance only)
 * - Current version uses Redis (distributed, multi-instance safe)
 *
 * @author PDCMS Team
 * @since 2024
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt-blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtDecoder jwtDecoder;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate, JwtDecoder jwtDecoder) {
        this.redisTemplate = redisTemplate;
        this.jwtDecoder = jwtDecoder;
        log.info("TokenBlacklistService initialized with Redis backend");
    }

    /**
     * Add token to blacklist when user logs out
     * Token automatically expires from Redis when JWT expires
     *
     * @param token  JWT access token to blacklist
     * @param reason Reason for blacklisting (LOGOUT, PASSWORD_CHANGED, etc.)
     */
    public void blacklistToken(String token, String reason) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Long expiryTime = jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : null;

            if (expiryTime != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                long ttlSeconds = expiryTime - currentTime;

                if (ttlSeconds > 0) {
                    String key = BLACKLIST_PREFIX + token;
                    redisTemplate.opsForValue().set(key, reason, Duration.ofSeconds(ttlSeconds));
                    log.debug("Token blacklisted with reason '{}', TTL: {} seconds", reason, ttlSeconds);
                } else {
                    log.debug("Token already expired, skipping blacklist");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
            // Token already invalid, no need to blacklist
        }
    }

    /**
     * Overloaded method for backward compatibility
     * Default reason: LOGOUT
     */
    public void blacklistToken(String token) {
        blacklistToken(token, "LOGOUT");
    }

    /**
     * Check if token is blacklisted
     *
     * @param token JWT access token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Get blacklist reason for a token
     *
     * @param token JWT access token
     * @return blacklist reason or null if not blacklisted
     */
    public String getBlacklistReason(String token) {
        String key = BLACKLIST_PREFIX + token;
        Object reason = redisTemplate.opsForValue().get(key);
        return reason != null ? reason.toString() : null;
    }

    /**
     * Blacklist all tokens for a user (e.g., on password change)
     * Note: This requires storing user->token mapping in Redis
     * For now, this is a placeholder for future enhancement
     *
     * @param username Username to blacklist all tokens for
     */
    public void blacklistAllUserTokens(String username) {
        // TODO: Implement user->token mapping for this feature
        log.warn("blacklistAllUserTokens not yet implemented for username: {}", username);
    }
}
