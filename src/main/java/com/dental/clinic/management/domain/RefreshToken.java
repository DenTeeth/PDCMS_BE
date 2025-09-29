package com.dental.clinic.management.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A RefreshToken entity.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash; // SHA-256 hash của actual refresh token

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public RefreshToken() {
    }

    public RefreshToken(String id, String accountId, String tokenHash, LocalDateTime expiresAt) {
        this.id = id;
        this.accountId = accountId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
